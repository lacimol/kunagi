package scrum.server.admin;

import ilarkesto.base.Str;
import ilarkesto.base.time.DateAndTime;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;
import ilarkesto.ui.web.HtmlRenderer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scrum.client.ApplicationInfo;
import scrum.server.ScrumConfig;
import scrum.server.ScrumWebApplication;
import scrum.server.WebSession;
import scrum.server.common.AHttpServlet;

public class LoginServlet extends AHttpServlet {

	private static Log log = Log.get(LoginServlet.class);
	private static final long serialVersionUID = 1;

	private static ScrumWebApplication webApplication;
	private ApplicationInfo applicationInfo;
	private ScrumConfig config;
	private UserDao userDao;
	private SystemConfig systemConfig;

	@Override
	protected void onRequest(HttpServletRequest req, HttpServletResponse resp, WebSession session) throws IOException {
		if (session.getUser() != null) {
			resp.sendRedirect(getStartPage());
			return;
		}

		if (req.getParameter("createAccount") != null) {
			createAccount(req.getParameter("username"), req.getParameter("email"), req.getParameter("password"), resp,
				session);
			return;
		}

		if (req.getParameter("passwordRequest") != null) {
			passwordRequest(req.getParameter("email"), resp, session);
			return;
		}

		String username = req.getParameter("username");
		if (username != null) {
			login(username, req.getParameter("password"), resp, session);
			return;
		}

		renderLoginPage(resp, null, null, null, req.getParameter("showPasswordRequest") != null,
			req.getParameter("showCreateAccount") != null);
	}

	private void passwordRequest(String login, HttpServletResponse resp, WebSession session)
			throws UnsupportedEncodingException, IOException {
		if (login == null || Str.isBlank(login)) {
			renderLoginPage(resp, login, null, "E-Mail required.", true, false);
			return;
		}

		login = login.toLowerCase();
		User user = null;
		if (login.contains("@")) {
			user = userDao.getUserByEmail(login);
		}
		if (user == null) {
			user = userDao.getUserByName(login);
		}

		if (user == null) {
			renderLoginPage(resp, login, login, "User '" + login + "' does not exist.", true, false);
			return;
		}

		if (user.isAdmin()) {
			renderLoginPage(resp, login, login, "Admins can not request new passwords.", true, false);
			return;
		}

		if (!user.isEmailVerified()) {
			renderLoginPage(resp, login, login, "User '" + login
					+ "' has no verified email. Please contact the admin: " + systemConfig.getAdminEmail(), true, false);
			return;
		}

		user.triggerNewPasswordRequest();
		renderLoginPage(resp, login, login, "New password has been sent to " + login, false, false);
	}

	private void createAccount(String username, String email, String password, HttpServletResponse resp,
			WebSession session) throws UnsupportedEncodingException, IOException {

		if (Str.isBlank(username)) username = null;
		if (Str.isBlank(email)) email = null;
		if (Str.isBlank(password)) password = null;

		if (username == null) {
			renderLoginPage(resp, username, email, "Creating account failed. Username required.", false, true);
			return;
		}
		if (systemConfig.isUserEmailMandatory() && email == null) {
			renderLoginPage(resp, username, email, "Creating account failed. E-Mail required.", false, true);
			return;
		}
		if (password == null) {
			renderLoginPage(resp, username, email, "Creating account failed. Password required.", false, true);
			return;
		}

		if (Str.containsNonLetterOrDigit(username)) {
			renderLoginPage(resp, username, email, "Creating account failed. Name '" + username
					+ "' contains an illegal character. Only letters and digits allowed.", false, true);
			return;
		}

		if (email != null && !Str.isEmail(email)) {
			renderLoginPage(resp, username, email, "Creating account failed. Illegal email address.", false, true);
			return;
		}

		if (userDao.getUserByName(username) != null) {
			renderLoginPage(resp, username, email, "Creating account failed. Name '" + username + "' is already used.",
				false, true);
			log.warn("Registration failed. User name already exists:", username);
			return;
		}

		if (email != null && userDao.getUserByEmail(email) != null) {
			renderLoginPage(resp, username, email, "Creating account failed. Email '" + email + "' is already used.",
				false, true);
			log.warn("Registration failed. User email already exists:", email);
			return;
		}

		User user = userDao.postUser(email, username, password);
		user.setLastLoginDateAndTime(DateAndTime.now());
		user.triggerEmailVerification();
		webApplication.triggerRegisterNotification(user);

		session.setUser(user);
		resp.sendRedirect(getStartPage());
	}

	private String getStartPage() {
		return webApplication.isDevelopmentMode() ? "index.html?gwt.codesvr=127.0.0.1:9997" : "";
	}

	private void login(String username, String password, HttpServletResponse resp, WebSession session)
			throws UnsupportedEncodingException, IOException {
		username = username.toLowerCase();
		User user = null;
		if (username.contains("@")) {
			user = userDao.getUserByEmail(username);
		}
		if (user == null) {
			user = userDao.getUserByName(username);
		}

		if (user == null || user.matchesPassword(password) == false) {
			renderLoginPage(resp, username, null, "Login failed.", false, false);
			return;
		}

		if (user.isDisabled()) {
			renderLoginPage(resp, username, null, "User is disabled.", false, false);
			return;
		}

		user.setLastLoginDateAndTime(DateAndTime.now());
		session.setUser(user);
		resp.sendRedirect(getStartPage());
	}

	@Override
	protected void onInit(ServletConfig servletConfig) {
		super.onInit(servletConfig);
		webApplication = ScrumWebApplication.get();
		userDao = webApplication.getUserDao();
		applicationInfo = webApplication.getApplicationInfo();
		config = webApplication.getConfig();
		systemConfig = webApplication.getSystemConfig();
	}

	private void renderLoginPage(HttpServletResponse resp, String username, String email, String message,
			boolean passwordRequest, boolean createAccount) throws UnsupportedEncodingException, IOException {
		String charset = IO.UTF_8;
		resp.setContentType("text/html");

		HtmlRenderer html = new HtmlRenderer(resp.getOutputStream(), charset);
		html.startHTMLstandard();

		html.startHEAD(applicationInfo.getName() + " Login", "EN");
		html.META("X-UA-Compatible", "chrome=1");
		// TODO html.LINKfavicon();
		html.LINKcss("scrum.ScrumGwtApplication/screen.css");
		html.endHEAD();

		html.startBODY();
		html.startDIV("loginPage");
		html.startDIV("panel");
		html.IMG("kunagi.png", "Kunagi", null, 172, 85);
		html.DIV("separator", null);
		if (message != null) renderMessage(html, message);
		if (!createAccount && !passwordRequest) renderLoginForm(html, username);
		if (passwordRequest) renderPasswordRequestForm(html, username);
		if (createAccount) renderCreateAccountForm(html, username, email);
		html.DIV("separator", null);
		html.startDIV("kunagiLink");
		html.text("Kunagi " + webApplication.getReleaseLabel() + " | ");
		html.A("http://kunagi.org", "kunagi.org");
		html.endDIV();
		html.endDIV();

		html.endDIV();
		html.comment(applicationInfo.toString());

		html.SCRIPTjavascript(null, "document.getElementById('username').focus();");

		String analyticsId = config.getGoogleAnalyticsId();
		if (analyticsId != null) html.googleAnalytics(analyticsId);
		html.endBODY();
		html.endHTML();
		html.flush();
	}

	private void renderLoginForm(HtmlRenderer html, String username) {
		html.H2("Login");
		html.startFORM(null, "loginForm", false);
		html.startTABLE().setAlignCenter();

		html.startTR();
		html.startTD();
		html.LABEL("username", "Username / E-Mail");
		html.endTD();
		html.startTD();
		html.LABEL("password", "Password");
		html.endTD();
		html.TD(" ");
		html.endTR();

		html.startTR();
		html.startTD();
		html.INPUTtext("username", "username", username, 80);
		html.endTD();
		html.startTD();
		html.INPUTpassword("password", "password", 80, "");
		html.endTD();
		html.startTD();
		html.INPUTsubmit("login", "Login", null, 's');
		html.endTD();
		html.endTR();

		html.endTABLE();
		html.endFORM();

		html.BR();
		html.A("login.html?showPasswordRequest=true", "Forgot your password?");

		if (!systemConfig.isRegistrationDisabled()) {
			html.nbsp();
			html.nbsp();
			html.A("login.html?showCreateAccount=true", "Create new account");
		}

		if (webApplication.isAdminPasswordDefault()) {
			html.DIV("separator", null);
			html.startDIV("configMessage");
			html.html("<h2>Warning!</h2>The administrator user <strong>admin</strong> has the default password <strong>"
					+ scrum.client.admin.User.INITIAL_PASSWORD + "</strong>. Please change it.");
			html.endDIV();
		}

		if (systemConfig.isLoginPageMessageSet()) {
			html.DIV("separator", null);
			html.startDIV("configMessage");
			html.html(systemConfig.getLoginPageMessage());
			html.endDIV();
		}
	}

	private void renderPasswordRequestForm(HtmlRenderer html, String username) {
		html.H2("Request new password");
		html.startFORM(null, "passwordRequestForm", false);
		html.startTABLE().setAlignCenter();

		html.startTR();
		html.startTD();
		html.LABEL("email", "E-Mail");
		html.endTD();
		html.TD(" ");
		html.endTR();

		html.startTR();
		html.startTD();
		html.INPUTtext("email", "email", username, 80);
		html.endTD();
		html.startTD();
		html.INPUTsubmit("passwordRequest", "Request password", null, 's');
		html.endTD();
		html.endTR();

		html.endTABLE();
		html.endFORM();

		html.BR();
		html.A("login.html", "Back to Login");
	}

	private void renderCreateAccountForm(HtmlRenderer html, String username, String email) {
		html.H2("Create account");
		html.startDIV("createAccount");
		html.startFORM(null, "loginForm", false);
		html.startTABLE().setAlignCenter();

		html.startTR();
		html.startTD();
		html.LABEL("username", "Username");
		html.endTD();
		html.startTD();
		html.INPUTtext("username", "username", username, 80);
		html.endTD();
		html.endTR();

		html.startTR();
		html.startTD();
		if (!webApplication.getSystemConfig().isUserEmailMandatory()) html.startDIV("optionalLabel");
		html.LABEL("email", "E-Mail");
		if (!webApplication.getSystemConfig().isUserEmailMandatory()) html.endDIV();
		html.endTD();
		html.startTD();
		html.INPUTtext("email", "email", email, 80);
		html.endTD();
		html.endTR();

		html.startTR();
		html.startTD();
		html.LABEL("password", "Password");
		html.endTD();
		html.startTD();
		html.INPUTpassword("password", "password", 80, "");
		html.endTD();
		html.endTR();

		html.startTR();
		html.TD("");
		html.startTD();
		html.INPUTsubmit("createAccount", "Create account", null, 's');
		html.endTD();
		html.endTR();

		html.endTABLE();
		html.endFORM();
		html.endDIV();

		html.BR();
		html.A("login.html", "Back to Login");

		if (systemConfig.isRegisterPageMessageSet()) {
			html.DIV("separator", null);
			html.startDIV("configMessage");
			html.html(systemConfig.getRegisterPageMessage());
			html.endDIV();
		}
	}

	private void renderMessage(HtmlRenderer html, String message) {
		html.startDIV("message");
		html.text(message);
		html.endDIV();
	}

}