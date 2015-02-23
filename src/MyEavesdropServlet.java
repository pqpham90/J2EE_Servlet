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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

public class MyEavesdropServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	Map<String, String> userData;
	Map<String, String> loggedInUsers;

	public MyEavesdropServlet() {
		super();
	}

	@Override
	public void init(ServletConfig config) {
		userData = new HashMap<String, String>();
		loggedInUsers = new HashMap<String, String>();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Cookie cookies[] = request.getCookies();
		boolean friend = false;
		for (int i = 0; cookies != null && i < cookies.length; i++) {
			Cookie ck = cookies[i];
			String cookieName = ck.getName();
			String cookieValue = ck.getValue();
			if ((cookieName != null && cookieName.equals("not-a-stranger-anymore"))
					&& cookieValue != null && cookieValue.equals("friend")) {
				response.getWriter().println("Hello, friend.");
				response.getWriter().println("Domain:" + ck.getDomain());
				response.getWriter().println("Path:" + ck.getPath());
				friend = true;
			}
		}
		if (!friend) {
			Cookie cookie = new Cookie("not-a-stranger-anymore", "friend");
			cookie.setDomain("localhost");
			cookie.setPath("/assignment2" + request.getServletPath());
			cookie.setMaxAge(1000);
			response.addCookie(cookie);
			response.getWriter().println("Hello, stranger.");
		}

		response.getWriter().println("Hello world.");
		try {
			String source = "http://eavesdrop.openstack.org/irclogs/%23heat/";
			Document doc = Jsoup.connect(source).get();
			Elements links = doc.select("body a");

			ListIterator<Element> iter = links.listIterator();
			while(iter.hasNext()) {
				Element e = (Element) iter.next();
				String s = e.html();
				s = s.replace("#", "%23");
				response.getWriter().println(source + s);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		String value = request.getParameter("param1");
		response.getWriter().println("Param:param1" + " Value:" + value);

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

		userData.put("V1", "V2");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (request.getParameter("username") != null &&
				request.getParameter("password") != null) {
			Cookie cookie = new Cookie("logged_in", "me");
			cookie.setMaxAge(1000);
			response.addCookie(cookie);

			response.getWriter().println("<html>");
			response.getWriter().println("<br>Welcom, " + request.getParameter("username"));
			response.getWriter().println("<form action=\"/qp/queryparam\" method=\"post\">");
			response.getWriter().println("<input type=\"text\" name=\"type\">");
			response.getWriter().println("<input type=\"text\" name=\"project\">");
			response.getWriter().println("<input type=\"text\" name=\"year\">");
			response.getWriter().println("<input type=\"submit\"></input>");
			response.getWriter().println("</form>");
			response.getWriter().println("</html>");
			return;
		}

		String filename = getServletContext().getRealPath("/WEB-INF/userData.txt");
		System.out.println("FileName:" + filename);

		FileOutputStream f = new FileOutputStream(filename);
		PrintWriter pw = new PrintWriter(f);
		Cookie cookies[] = request.getCookies();
		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equals("logged_in")) {
				Iterator it = userData.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
					String x = entry.getKey() + ":" + entry.getValue();
					pw.println(x);
				}
			}
			pw.flush();
			pw.close();
			f.flush();
			f.close();
		}
	}
}