package scrum.server.common;

import javax.servlet.http.HttpServletRequest;

public class SpamChecker {

	public static void check(HttpServletRequest req) {
		String spamPreventionCode = req.getParameter("spamPreventionCode");
		if (!"no-spam".equals(spamPreventionCode)) throw new RuntimeException("HTTP Request identified as SPAM.");
	}

}
