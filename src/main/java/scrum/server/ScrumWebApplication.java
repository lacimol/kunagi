/*
 * Copyright 2008, 2009, 2010 Witoslaw Koczewski, Artjom Kochtchi, Fabian Hager, Kacper Grubalski.
 * 
 * This file is part of Kunagi.
 * 
 * Kunagi is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Kunagi is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with Foobar. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package scrum.server;

import ilarkesto.auth.OpenId;
import ilarkesto.base.Sys;
import ilarkesto.base.Tm;
import ilarkesto.base.Url;
import ilarkesto.base.time.DateAndTime;
import ilarkesto.concurrent.TaskManager;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.di.app.WebApplicationStarter;
import ilarkesto.email.Eml;
import ilarkesto.gwt.server.AGwtConversation;
import ilarkesto.io.IO;
import ilarkesto.persistence.AEntity;
import ilarkesto.webapp.AWebApplication;
import ilarkesto.webapp.AWebSession;
import ilarkesto.webapp.DestroyTimeoutedSessionsTask;
import ilarkesto.webapp.Servlet;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import scrum.client.ApplicationInfo;
import scrum.client.admin.SystemMessage;
import scrum.server.admin.DeleteDisabledUsersTask;
import scrum.server.admin.DisableInactiveUsersTask;
import scrum.server.admin.DisableUsersWithUnverifiedEmailsTask;
import scrum.server.admin.ProjectUserConfig;
import scrum.server.admin.SystemConfig;
import scrum.server.admin.User;
import scrum.server.admin.UserDao;
import scrum.server.common.AccomplishChart;
import scrum.server.common.BurndownChart;
import scrum.server.common.EfficiencyChart;
import scrum.server.common.UserBurndownChart;
import scrum.server.common.VelocityChart;
import scrum.server.project.DeleteOldProjectsTask;
import scrum.server.project.HomepageUpdaterTask;
import scrum.server.project.Project;

public class ScrumWebApplication extends GScrumWebApplication {

	private static final int DATA_VERSION = 23;

	private static final Log log = Log.get(ScrumWebApplication.class);

	private BurndownChart burndownChart;
	private EfficiencyChart efficiencyChart;
	private VelocityChart velocityChart;
	private AccomplishChart accomplishChart;
	private KunagiRootConfig config;
	private ScrumEntityfilePreparator entityfilePreparator;
	private SystemMessage systemMessage;

	public ScrumWebApplication(KunagiRootConfig config) {
		this.config = config;
	}

	public ScrumWebApplication() {
		this(null);
	}

	@Override
	protected int getDataVersion() {
		return DATA_VERSION;
	}

	// --- composites ---

	public BurndownChart getBurndownChart() {
		if (burndownChart == null) {
			burndownChart = new BurndownChart();
			burndownChart.setSprintDao(getSprintDao());
		}
		return burndownChart;
	}

	public UserBurndownChart getUserBurndownChart() {
		UserBurndownChart userBurndownChart = new UserBurndownChart();
		userBurndownChart.setSprintDao(getSprintDao());
		return userBurndownChart;
	}

	public AccomplishChart getAccomplishChart() {
		if (accomplishChart == null) {
			accomplishChart = new AccomplishChart();
			accomplishChart.setSprintDao(getSprintDao());
		}
		return accomplishChart;
	}

	public EfficiencyChart getEfficiencyChart() {
		if (efficiencyChart == null) {
			efficiencyChart = new EfficiencyChart();
			efficiencyChart.setSprintDao(getSprintDao());
		}
		return efficiencyChart;
	}

	public VelocityChart getVelocityChart() {
		if (velocityChart == null) {
			velocityChart = new VelocityChart();
			velocityChart.setSprintDao(getSprintDao());
		}
		return velocityChart;
	}

	public SystemConfig getSystemConfig() {
		return getSystemConfigDao().getSystemConfig();
	}

	public KunagiRootConfig getConfig() {
		if (config == null) config = new KunagiRootConfig(getApplicationName());
		return config;
	}

	public ScrumEntityfilePreparator getEntityfilePreparator() {
		if (entityfilePreparator == null) {
			entityfilePreparator = new ScrumEntityfilePreparator();
			entityfilePreparator.setBackupDir(getApplicationDataDir() + "/backup/entities");
		}
		return entityfilePreparator;
	}

	public ApplicationInfo getApplicationInfo() {
		User admin = getUserDao().getUserByName("admin");
		boolean defaultAdminPassword = false;
		if (admin != null && admin.matchesPassword(scrum.client.admin.User.INITIAL_PASSWORD)) {
			defaultAdminPassword = true;
		}
		return new ApplicationInfo("Kunagi", getReleaseLabel(), getBuild(), defaultAdminPassword, getCurrentRelease(),
				getApplicationDataDir());
	}

	private String currentRelease;

	public String getCurrentRelease() {
		if (!getSystemConfig().isVersionCheckEnabled()) return null;
		if (currentRelease == null) {
			String url = "http://kunagi.org/current-release.properties";
			log.info("Checking current release:", url);
			try {
				Properties currentReleaseProperties = IO.loadPropertiesFromUrl(url, IO.UTF_8);
				currentRelease = currentReleaseProperties.getProperty("currentRelease");
				log.info("   ", currentRelease);
			} catch (Throwable ex) {
				return null;
			}
		}
		return currentRelease;
	}

	// --- ---

	@Override
	public void ensureIntegrity() {
		if (getConfig().isStartupDelete()) {
			log.warn("DELETING ALL ENTITIES (set startup.delete=false in config.properties to prevent this behavior)");
			IO.delete(getApplicationDataDir() + "/entities");
		}

		super.ensureIntegrity();
	}

	@Override
	protected void onStartWebApplication() {
		Log.setDebugEnabled(isDevelopmentMode() || getConfig().isLoggingDebug());

		deleteOldBackupFiles(getApplicationDataDir() + "/backup");

		String url = getConfig().getUrl();
		if (!Str.isBlank(url)) getSystemConfig().setUrl(url);

		if (getUserDao().getEntities().isEmpty()) {
			String password = getConfig().getInitialPassword();
			log.warn("No users. Creating initial user <admin> with password <" + password + ">");
			User admin = getUserDao().postUserWithDefaultPassword("admin");
			admin.setPassword(password);
			admin.setAdmin(true);
			getTransactionService().commit();
		}

		getProjectDao().scanFiles();
		getTransactionService().commit();

		String httpProxy = getConfig().getHttpProxyHost();
		if (!Str.isBlank(httpProxy)) {
			int httpProxyPort = getConfig().getHttpProxyPort();
			log.info("Setting HTTP proxy:", httpProxy + ":" + httpProxyPort);
			Sys.setHttpProxy(httpProxy, httpProxyPort);
			OpenId.setHttpProxy(httpProxy, httpProxyPort);
		}

		// test data
		if (getConfig().isCreateTestData() && getProjectDao().getEntities().isEmpty()) createTestData();

		for (ProjectUserConfig config : getProjectUserConfigDao().getEntities()) {
			config.reset();
		}
	}

	public String createUrl(String relativePath) {
		if (relativePath == null) relativePath = "";
		String prefix = getBaseUrl();
		if (Str.isBlank(prefix)) return relativePath;
		if (prefix.endsWith("/")) {
			if (relativePath.startsWith("/")) return prefix.substring(0, prefix.length() - 1) + relativePath;
			return prefix + relativePath;
		}
		if (relativePath.startsWith("/")) return prefix + relativePath;
		return prefix + "/" + relativePath;
	}

	private void createTestData() {
		log.warn("Creating test data");

		getUserDao().postUserWithDefaultPassword("homer");
		getUserDao().postUserWithDefaultPassword("cartman");
		getUserDao().postUserWithDefaultPassword("duke");
		getUserDao().postUserWithDefaultPassword("spinne");

		getProjectDao().postExampleProject(getUserDao().getUserByName("admin"), getUserDao().getUserByName("cartman"),
			getUserDao().getUserByName("admin"));

		getTransactionService().commit();
	}

	@Override
	protected void scheduleTasks(TaskManager tm) {
		tm.scheduleWithFixedDelay(autowire(new DestroyTimeoutedSessionsTask()), Tm.MINUTE);
		tm.scheduleWithFixedDelay(autowire(new HomepageUpdaterTask()), Tm.HOUR);

		if (getConfig().isDisableUsersWithUnverifiedEmails())
			tm.scheduleWithFixedDelay(autowire(new DisableUsersWithUnverifiedEmailsTask()), Tm.HOUR);
		if (getConfig().isDisableInactiveUsers())
			tm.scheduleWithFixedDelay(autowire(new DisableInactiveUsersTask()), Tm.HOUR);
		if (getConfig().isDeleteOldProjects())
			tm.scheduleWithFixedDelay(autowire(new DeleteOldProjectsTask()), Tm.SECOND, Tm.HOUR * 25);
		if (getConfig().isDeleteDisabledUsers())
			tm.scheduleWithFixedDelay(autowire(new DeleteDisabledUsersTask()), Tm.MINUTE * 3, Tm.HOUR * 26);
	}

	@Override
	public String getApplicationDataDir() {
		return getConfig().getDataPath();
	}

	@Override
	protected void onShutdownWebApplication() {}

	@Override
	public Url getHomeUrl() {
		return new Url("index.html");
	}

	public String getBaseUrl() {
		String url = getSystemConfig().getUrl();
		return url == null ? "http://localhost:8080/kunagi/" : url;
	}

	private UserDao userDao;

	public UserDao getUserDao() {
		if (userDao == null) {
			userDao = new UserDao();
			autowire(userDao);
		}
		return userDao;
	}

	public boolean isAdminPasswordDefault() {
		User admin = userDao.getUserByName("admin");
		if (admin == null) return false;
		return admin.matchesPassword(scrum.client.admin.User.INITIAL_PASSWORD);
	}

	public void sendToOtherConversationsByProject(GwtConversation conversation, AEntity entity) {
		for (AGwtConversation c : getConversationsByProject(conversation.getProject(), conversation)) {
			c.sendToClient(entity);
		}
	}

	public void sendToConversationsByProject(Project project, AEntity entity) {
		for (AGwtConversation c : getConversationsByProject(project, null)) {
			c.sendToClient(entity);
		}
	}

	public void sendToClients(AEntity entity) {
		for (AGwtConversation c : getGwtConversations()) {
			c.sendToClient(entity);
		}
	}

	public Set<GwtConversation> getConversationsByProject(Project project, GwtConversation exception) {
		Set<GwtConversation> ret = new HashSet<GwtConversation>();
		for (Object element : getGwtConversations()) {
			if (element == exception) continue;
			GwtConversation conversation = (GwtConversation) element;
			if (project != null && project.equals(conversation.getProject())) ret.add(conversation);
		}
		return ret;
	}

	public Set<User> getConversationUsersByProject(Project project) {
		Set<User> ret = new HashSet<User>();
		for (GwtConversation conversation : getConversationsByProject(project, null)) {
			User user = conversation.getSession().getUser();
			if (user != null) ret.add(user);
		}
		return ret;
	}

	@Override
	protected AWebSession createWebSession(HttpServletRequest httpRequest) {
		WebSession session = new WebSession(context, httpRequest);
		autowire(session);
		return session;
	}

	public void updateSystemMessage(SystemMessage systemMessage) {
		this.systemMessage = systemMessage;
		for (AGwtConversation conversation : getGwtConversations()) {
			log.debug("Sending SystemMessage to:", conversation);
			((GwtConversation) conversation).getNextData().systemMessage = systemMessage;
		}
	}

	public SystemMessage getSystemMessage() {
		return systemMessage;
	}

	public static ScrumWebApplication get() {
		return (ScrumWebApplication) AWebApplication.get();
	}

	public static synchronized ScrumWebApplication get(ServletConfig servletConfig) {
		if (AWebApplication.isStarted()) return get();
		return (ScrumWebApplication) WebApplicationStarter.startWebApplication(ScrumWebApplication.class.getName(),
			Servlet.getContextPath(servletConfig));
	}

	public void triggerRegisterNotification(User user, String host) {
		StringBuilder sb = new StringBuilder();
		sb.append("Kunagi URL: ").append(getBaseUrl()).append("\n");
		sb.append("Name: ").append(user.getName()).append("\n");
		sb.append("Email: ").append(user.getEmail()).append("\n");
		sb.append("OpenID: ").append(user.getOpenId()).append("\n");
		sb.append("Date/Time: ").append(DateAndTime.now()).append("\n");
		sb.append("Host: ").append(host).append("\n");
		String subject = user.getLabel() + " registered on " + getBaseUrl();
		try {
			sendEmail(null, null, subject, sb.toString());
		} catch (Throwable ex) {
			log.error("Sending notification email failed:", subject, ex);
		}
	}

	public void sendEmail(String from, String to, String subject, String text) {
		Session session = createSmtpSession();
		if (session == null) return;
		SystemConfig config = getSystemConfig();

		if (Str.isBlank(from)) from = config.getSmtpFrom();
		if (Str.isBlank(from)) {
			log.error("Missing configuration: smtpFrom");
			return;
		}

		if (Str.isBlank(to)) to = config.getAdminEmail();
		if (Str.isBlank(to)) {
			log.error("Missing configuration: adminEmail");
			return;
		}

		if (Str.isBlank(subject)) subject = "Kunagi";

		MimeMessage message = Eml.createTextMessage(session, subject, text, from, to);
		Eml.sendSmtpMessage(session, message);
	}

	public Session createSmtpSession() {
		SystemConfig config = getSystemConfig();
		String smtpServer = config.getSmtpServer();
		Integer smtpPort = config.getSmtpPort();
		boolean smtpTls = config.isSmtpTls();
		if (smtpServer == null) {
			log.error("Missing configuration: smtpServer");
			return null;
		}
		return Eml.createSmtpSession(smtpServer, smtpPort, smtpTls, config.getSmtpUser(), config.getSmtpPassword());
	}

}
