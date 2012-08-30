/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package scrum.server.common;

import ilarkesto.base.PermissionDeniedException;
import ilarkesto.base.Str;
import ilarkesto.base.time.DateAndTime;
import ilarkesto.core.logging.Log;
import ilarkesto.core.logging.LogRecord;
import ilarkesto.io.IO;
import ilarkesto.persistence.AEntity;
import ilarkesto.ui.web.HtmlRenderer;
import ilarkesto.webapp.Servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scrum.client.ApplicationInfo;
import scrum.client.ScrumGwtApplication;
import scrum.server.KunagiRootConfig;
import scrum.server.ScrumWebApplication;
import scrum.server.WebSession;
import scrum.server.admin.SystemConfig;
import scrum.server.admin.User;
import scrum.server.admin.UserDao;
import scrum.server.project.Project;

public abstract class AHttpServlet extends HttpServlet {

	protected static final int LOGIN_TOKEN_COOKIE_MAXAGE = 1209600; // 14 days

	private static final Log log = Log.get(AHttpServlet.class);

	protected static ScrumWebApplication webApplication;
	protected KunagiRootConfig config;
	protected ApplicationInfo applicationInfo;
	protected SystemConfig systemConfig;
	protected UserDao userDao;

	protected abstract void onRequest(HttpServletRequest req, HttpServletResponse resp, WebSession session)
			throws IOException;

	protected void onInit(ServletConfig config) {}

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (webApplication != null && webApplication.isShuttingDown()) {
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Application shutting down");
			return;
		}
		Servlet.preventCaching(resp);
		try {
			onRequest(req, resp, (WebSession) ScrumWebApplication.get().getWebSession(req));
		} catch (Throwable ex) {
			log.fatal("GET failed:", getClass().getName(), "->", ex);
		}
	}

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (webApplication != null && webApplication.isShuttingDown()) {
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Application shutting down");
			return;
		}
		Servlet.preventCaching(resp);
		try {
			onRequest(req, resp, (WebSession) ScrumWebApplication.get().getWebSession(req));
		} catch (Throwable ex) {
			log.fatal("POST failed:", getClass().getName(), "->", ex);
		}
	}

	@Override
	public final void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		try {
			webApplication = ScrumWebApplication.get(servletConfig);
			config = webApplication.getConfig();
			applicationInfo = webApplication.getApplicationInfo();
			systemConfig = webApplication.getSystemConfig();
			userDao = webApplication.getUserDao();
			onInit(servletConfig);
		} catch (Throwable ex) {
			log.fatal("Init failed:", getClass().getName(), "->", ex);
			throw new ServletException(ex);
		}
	}

	@Override
	public final void init() throws ServletException {
		super.init();
	}

	// --- helper ---

	protected HtmlRenderer createDefaultHtmlWithHeader(HttpServletResponse resp, String subtitle) throws IOException {
		return createDefaultHtmlWithHeader(resp, subtitle, 0, null);
	}

	protected HtmlRenderer createDefaultHtmlWithHeader(HttpServletResponse resp, String subtitle, int refreshSeconds,
			String refreshUrl) throws IOException {
		String charset = IO.UTF_8;
		resp.setContentType("text/html");
		HtmlRenderer html = new HtmlRenderer(resp.getOutputStream(), charset);
		html.startHTMLstandard();
		String title = "Kunagi";
		if (config.isShowRelease()) title += " " + applicationInfo.getRelease();
		title += " " + subtitle;
		if (systemConfig.isInstanceNameSet()) title += " @ " + systemConfig.getInstanceName();
		html.startHEAD(title, "EN");
		html.META("X-UA-Compatible", "IE=edge");
		if (!Str.isBlank(refreshUrl)) html.METArefresh(refreshSeconds, refreshUrl);
		html.LINKfavicon();
		html.endHEAD();
		return html;
	}

	protected void logsTable(HtmlRenderer html, List<LogRecord> logs) {
		startTABLE(html);
		headersRow(html, "Level", "Logger", "Message", "Context");
		for (LogRecord log : logs) {
			String color = "#666";
			if (log.level.isErrorOrWorse()) color = "#c00";
			if (log.level.isWarn()) color = "#990";
			if (log.level.isInfo()) color = "#000";
			valuesRowColored(html, color, log.level, log.name, log.getParametersAsString(), log.context);
		}
		endTABLE(html);
	}

	protected void startTABLE(HtmlRenderer html) {
		html.startTABLE();
	}

	protected void headersRow(HtmlRenderer html, String... headers) {
		html.startTR();

		for (String header : headers) {
			html.startTH().setStyle(getLabelStyle());
			html.text(header);
			html.endTH();
		}

		html.endTR();
		html.flush();
	}

	protected void valuesRowColored(HtmlRenderer html, String color, Object... values) {
		html.startTR();

		for (Object value : values) {
			html.startTD().setStyle(getValueStyle() + " color: " + color + ";");
			html.text(value);
			html.endTD();
		}

		html.endTR();
		html.flush();
	}

	protected void valuesRow(HtmlRenderer html, Object... values) {
		html.startTR();

		for (Object value : values) {
			html.startTD().setStyle(getValueStyle());
			html.text(value);
			html.endTD();
		}

		html.endTR();
		html.flush();
	}

	protected void keyValueRow(HtmlRenderer html, String key, Object value) {
		html.startTR();

		html.startTD().setStyle(getLabelStyle());
		html.text(key);
		html.endTD();

		html.startTD().setStyle(getValueStyle());
		html.text(value);
		html.endTD();

		html.endTR();
		html.flush();
	}

	protected void endTABLE(HtmlRenderer html) {
		html.endTABLE();
		html.flush();
	}

	protected void sectionHeader(HtmlRenderer html, String title) {
		html.H2(title);
	}

	private String getLabelStyle() {
		return "color: #999; font-weight: normal; padding: 2px 20px 2px 5px; text-align: left;";
	}

	private String getValueStyle() {
		return "font-family: mono; padding: 2px 20px 2px 5px;";
	}

	protected String getDefaultStartPage() {
		return webApplication.isDevelopmentMode() ? "index.html?gwt.codesvr=127.0.0.1:9997" : "";
	}

	protected void adminLinks(HtmlRenderer html, HttpServletRequest req) {
		html.startP();
		html.text("[ ");
		html.A("admin.html", "Admin page");
		html.text(" ] [ ");
		html.A("logs.html", "Latest logs");
		html.text(" ] [ ");
		html.A(Servlet.getBaseUrl(req), "Kunagi");
		html.text(" ]");
		html.endP();
	}

	protected boolean tokenLogin(HttpServletRequest req, HttpServletResponse resp, WebSession session)
			throws IOException {
		String loginToken = Servlet.getCookieValue(req, ScrumGwtApplication.LOGIN_TOKEN_COOKIE);
		if (!Str.isBlank(loginToken)) {
			User user = userDao.getUserByLoginToken(loginToken);
			if (user != null) {
				user.setLastLoginDateAndTime(DateAndTime.now());
				session.setUser(user);
				Servlet.setCookie(resp, ScrumGwtApplication.LOGIN_TOKEN_COOKIE, user.getLoginToken(),
					LOGIN_TOKEN_COOKIE_MAXAGE);
				return true;
			}
		}
		return false;
	}

	protected void redirectToLogin(HttpServletRequest req, HttpServletResponse resp, WebSession session)
			throws IOException {
		String url = "login.html";
		String token = Str.cutFrom(req.getRequestURI(), "#");
		if (!Str.isBlank(token)) url += "?historyToken=" + Str.encodeUrlParameter(token);
		url = webApplication.createUrl(url);
		log.debug("Redirecting to", url);
		resp.sendRedirect(url);
	}

	public static <E extends AEntity> E getEntityByParameter(HttpServletRequest req, Class<E> type) {
		return getEntityByParameter(req, "entityId", type);
	}

	public static <E extends AEntity> E getEntityByParameter(HttpServletRequest req, String parameterName, Class<E> type) {
		String id = req.getParameter(parameterName);
		if (id == null) throw new RuntimeException(parameterName + "==null");
		return (E) ScrumWebApplication.get().getDaoService().getById(id);
	}

	public static Project getProject(WebSession session, HttpServletRequest req) {
		Project project = getEntityByParameter(req, "projectId", Project.class);
		if (!project.isVisibleFor(session.getUser())) throw new PermissionDeniedException();
		return project;
	}

}
