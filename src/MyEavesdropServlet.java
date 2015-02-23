import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pqpham90 on 2/19/15.
 */
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
		Document doc = Jsoup.connect("http://tv.atmovies.com.tw/tv/attv.cfm?action=channeltime&channel_id=CH06").get();
		System.out.println(doc);
	}
}