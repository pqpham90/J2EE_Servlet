import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ListIterator;

public class MyEavesdropServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void manageSession (HttpServletRequest request, HttpServletResponse response) throws IOException {
		String sessionFlag = request.getParameter("session");
		String username = request.getParameter("username");

		Cookie[] cookies = request.getCookies();

		if(sessionFlag != null) {
			if (sessionFlag.compareTo("start") == 0) {
				if (cookies == null) {
					if (username != null) {
						Cookie cookie = new Cookie("logged_in", username);
						cookie.setMaxAge(1000);
						response.addCookie(cookie);
						response.getWriter().println("Starting session for: " + username + "\n");
					}
					else {
						response.getWriter().println("Please provide a username to start the session");
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
							response.getWriter().println("Ending session for: " + username + "\n");
						}
						else {
							response.getWriter().print("Wrong user, current session is with: ");
							response.getWriter().println(cookies[0].getValue() + "\n");
						}
					}
					else {
						response.getWriter().print("Please end session with username: ");
						response.getWriter().println(cookies[0].getValue() + "\n");
					}
				}
				else {
					response.getWriter().println("Protip: you should start a session to end one");
				}
			}
			else {
				response.getWriter().print("Invalid value for session, ");

				if(cookies == null) {
					response.getWriter().println("did you mean to start?");
				}
				else {
					response.getWriter().println("did you mean to end?");
				}
			}
		}
		else if (cookies == null) {
			response.getWriter().println("Please start a session");
		}
	}

	public void printData(HttpServletResponse response) throws IOException {
		response.getWriter().println("Hello world.");
		try {
			String source = "http://eavesdrop.openstack.org/irclogs/%23heat/";
			Document doc = Jsoup.connect(source).get();
			Elements links = doc.select("body a");

			ListIterator<Element> iter = links.listIterator();
			while(iter.hasNext()) {
				Element e = iter.next();
				String s = e.html();
				s = s.replace("#", "%23");
				response.getWriter().println(source + s);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public void processQuery(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String type = request.getParameter("type");
		String project = request.getParameter("project");
		String year = request.getParameter("year");


		response.getWriter().println("Param type: " + type);
		response.getWriter().println("Param project: " + project);
		response.getWriter().println("Param year: " + year);

		System.out.println("");

		// Response is URLEncoded
		response.getWriter().println("Query String:" + request.getQueryString());

		// How to decode the URL encoded value?
		String queryString = URLDecoder.decode(request.getQueryString(), "UTF-8");

		response.getWriter().println("Decoded Query String:" + queryString);


		// Regarding requests

		response.getWriter().println("Request URL:" + request.getRequestURL());
		response.getWriter().println("Request URI:" + request.getRequestURI());
		response.getWriter().println("Servlet Path:" + request.getServletPath());

            /*
                response.getWriter().println("<html>");
                response.getWriter().println("<form action=\"/qp/queryparam\" method=\"post\">");
                response.getWriter().println("<input type=\"text\" name=\"username\">");
                response.getWriter().println("<input type=\"text\" name=\"password\">");
                response.getWriter().println("<input type=\"submit\">Log in </input>");
                response.getWriter().println("</form>");
                response.getWriter().println("</html>");
                */

		//response.getWriter().println("<html>");
		//response.getWriter().println("Hello again.");
		//response.getWriter().println("</html>");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String session = request.getParameter("session");
		Cookie[] cookies = request.getCookies();

		manageSession(request, response);

		if (cookies != null) {
			processQuery(request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}
}