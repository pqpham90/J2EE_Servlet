import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;

public class MyEavesdropServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// container for the history
	LinkedHashMap<Integer, String> userHistory;

	// mapping for valid years
	HashMap<Integer, Integer> validYears;
	int hashIndex;

	@Override
	public void init(ServletConfig config) {
		// add the valid years into the hash
		validYears = new HashMap<Integer, Integer>();
		for (int i = 2010; i <= 2015; i++) {
			validYears.put(i, i);
		}
	}

	// in charge of the username and session processing
	public void manageSession (HttpServletRequest request, HttpServletResponse response) throws IOException {
		String sessionFlag = request.getParameter("session");
		String username = request.getParameter("username");

		Cookie[] cookies = request.getCookies();

		// process the username and session parameters and decide what to do
		if(sessionFlag != null) {
			if (sessionFlag.compareTo("start") == 0) {
				if (cookies == null) {
					if (username != null) {
						if(username.contains(" ")) {
							response.getWriter().print("Spaces are not allowed in the username\n");
						}
						else {
							Cookie cookie = new Cookie("logged_in", username);
							cookie.setMaxAge(1000);
							response.addCookie(cookie);
							userHistory = new LinkedHashMap<Integer, String>();
							hashIndex = 0;
							response.getWriter().println("Starting session for: " + username + "\n");
						}
					}
					else {
						response.getWriter().println("Please provide a username to start the session\n");
					}
				}
				else {
					response.getWriter().print("Please end the current session with user: ");
					response.getWriter().println(cookies[0].getValue() + "\n");
				}
			}
			else if (sessionFlag.compareTo("end") == 0) {
				if (cookies != null) {
					if(username != null) {
						if (request.getParameter("username").compareTo(cookies[0].getValue()) == 0) {
							Cookie cookie = new Cookie("logged_in", username);
							cookie.setMaxAge(0);
							response.addCookie(cookie);
							userHistory = null;
							hashIndex = 0;
							response.getWriter().println("Ending session for: " + username + "\n");
						}
						else {
							response.getWriter().print("Wrong user, current session is with: ");
							response.getWriter().println(cookies[0].getValue() + "\n");
						}
					} else {
						response.getWriter().print("Please end session with username: ");
						response.getWriter().println(cookies[0].getValue() + "\n");
					}
				} else {
					response.getWriter().println("Protip: you should start a session to end one\n");
				}
			}
			else {
				response.getWriter().print("Invalid value for session, ");

				if(cookies == null) {
					response.getWriter().println("did you mean to start?\n");
				}
				else {
					response.getWriter().println("did you mean to end?\n");
				}
			}
		}
		else if (cookies == null) {
			response.getWriter().println("No session started\n");
		}
	}

	// prints the contents of the page
	public void printData(HttpServletResponse response, String source) throws IOException {
		try {
			Document doc = Jsoup.connect(source).get();
			Elements links = doc.select("body a");

			ListIterator<Element> iter = links.listIterator();
			while(iter.hasNext()) {
				Element e = iter.next();
				String s = e.html();
				s = s.replace("#", "%23");
				response.getWriter().println(source + s);
			}

			if (userHistory != null) {
				userHistory.put(hashIndex++, source);
			}
		}
		catch (IllegalArgumentException e) {
			response.getWriter().println("No valid data to retrieve");
		}
		catch (UnknownHostException e) {
			response.getWriter().println("URL not found");
		}
		catch (HttpStatusException e) {
			response.getWriter().println("URL not found");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// prints the contents of the history
	public void printHistory(HttpServletResponse response) throws IOException {
		if (userHistory != null) {
			int size = userHistory.size();

			for(int i = 0; i < size; i++) {
				response.getWriter().println(userHistory.get(i));
			}
		}
	}

	// in charge of processing the query
	public void processQuery(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String type = request.getParameter("type");
		String project = request.getParameter("project");
		String year = request.getParameter("year");

		String username = request.getParameter("username");
		String session = request.getParameter("session");

		// provide a source link to build on if queries are correct
		String source = "";

		// check to see if the query requirements have been met
		if (session == null && username == null) {
			source = "http://eavesdrop.openstack.org/";
			if (type == null || project == null) {
				response.getWriter().println("Please provide a parameter for type and project\n");
			}
			else  {
				if (type.compareTo("irclogs") == 0) {
					source += "irclogs" + "/" + project.replace("#", "%23") + "/";
				}
				else if (type.compareTo("meetings") == 0 && year != null) {
					if(validYears.containsKey(Integer.parseInt(year))) {
						source += "meetings/" + project + "/" + year + "/";
					}
					else {
						response.getWriter().println("Year not in the range of 2010-2015\n");
						source = "";
					}
				}
				else {
					response.getWriter().println("Please provide a year parameter\n");
					source = "";
				}
			}
		}

		response.getWriter().println("Visited URLs");
		printHistory(response);

		response.getWriter().println("\nURL Data");
		if (source.compareTo("") != 0) {
			printData(response, source);
		}
		else {
			response.getWriter().println("No valid data to retrieve");
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String session = request.getParameter("session");
		Cookie[] cookies = request.getCookies();

		manageSession(request, response);

		if (cookies != null) {
			if (request.getQueryString() == null) {
				response.getWriter().println("No query to process\n");
			}
		}

		processQuery(request, response);
	}
}