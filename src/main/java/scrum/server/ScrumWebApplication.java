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
import ilarkesto.base.Utl;
import ilarkesto.base.time.DateAndTime;
import ilarkesto.concurrent.TaskManager;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.di.app.WebApplicationStarter;
import ilarkesto.gwt.server.AGwtConversation;
import ilarkesto.io.IO;
import ilarkesto.persistence.AEntity;
import ilarkesto.webapp.AWebApplication;
import ilarkesto.webapp.AWebSession;
import ilarkesto.webapp.DestroyTimeoutedSessionsTask;
import ilarkesto.webapp.Servlet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import scrum.server.common.SprintRangeChart;
import scrum.server.common.SprintWorkChart;
import scrum.server.common.StoryThemeChart;
import scrum.server.common.TaskRangeChart;
import scrum.server.common.UserBurndownChart;
import scrum.server.common.VelocityChart;
import scrum.server.journal.ProjectEvent;
import scrum.server.pr.EmailSender;
import scrum.server.pr.SubscriptionService;
import scrum.server.project.DeleteOldProjectsTask;
import scrum.server.project.HomepageUpdaterTask;
import scrum.server.project.Project;

public class ScrumWebApplication extends GScrumWebApplication {

	private static final int DATA_VERSION = 33;

	private static final int MAX_UPDATE_TRY_NR = 3;
	private static int updateTryNr = 0;

	private static final Log log = Log.get(ScrumWebApplication.class);

	private BurndownChart burndownChart;
	private EfficiencyChart efficiencyChart;
	private VelocityChart velocityChart;
	private SprintWorkChart sprintWorkChart;
	private SprintRangeChart sprintRangeChart;
	private AccomplishChart accomplishChart;
	private StoryThemeChart storyThemeChart;
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

	public SprintWorkChart getSprintWorkChart() {
		if (sprintWorkChart == null) {
			sprintWorkChart = new SprintWorkChart();
			sprintWorkChart.setSprintDao(getSprintDao());
		}
		return sprintWorkChart;
	}

	public SprintRangeChart getSprintRangeChart() {
		if (sprintRangeChart == null) {
			sprintRangeChart = new SprintRangeChart(12);
			sprintRangeChart.setSprintDao(getSprintDao());
		}
		return sprintRangeChart;
	}

	public SprintRangeChart getCurrentSprintRangeChart() {
		SprintRangeChart sprintRangeChart = new SprintRangeChart(3);
		sprintRangeChart.setSprintDao(getSprintDao());
		return sprintRangeChart;
	}

	public TaskRangeChart getTaskRangeChart() {
		TaskRangeChart taskRangeChart = new TaskRangeChart();
		taskRangeChart.setSprintDao(getSprintDao());
		return taskRangeChart;
	}

	public StoryThemeChart getStoryThemeChart() {
		if (storyThemeChart == null) {
			storyThemeChart = new StoryThemeChart();
			storyThemeChart.setSprintDao(getSprintDao());
		}
		return storyThemeChart;
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
		boolean defaultAdminPassword = isAdminPasswordDefault();
		return new ApplicationInfo(getApplicationLabel(), getReleaseLabel(), getBuild(), defaultAdminPassword,
				getCurrentRelease(), getApplicationDataDir());
	}

	@Override
	public String getApplicationLabel() {
		return "Kunagi";
	}

	private String currentRelease;

	public String getCurrentRelease() {
		if (!getSystemConfig().isVersionCheckEnabled() || updateTryNr >= MAX_UPDATE_TRY_NR) return null;
		if (currentRelease == null) {
			String url = "http://kunagi.org/current-release.properties";
			log.info("Checking current release:", url, "Trying: ", ++updateTryNr);
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
			String password = getSystemConfig().getDefaultUserPassword();
			log.warn("No users. Creating initial user <admin> with password <" + password + ">");
			User admin = getUserDao().postUserWithDefaultPassword("admin");
			admin.setPassword(password);
			admin.setAdmin(true);
			getTransactionService().commit();
		}

		getReleaseDao().resetScripts();
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

		// return relative path if base is not defined
		if (Str.isBlank(prefix)) return relativePath;

		// concat base and relative and return
		if (prefix.endsWith("/")) {
			if (relativePath.startsWith("/")) return Str.removeSuffix(prefix, "/") + relativePath;
			return prefix + relativePath;
		}
		if (relativePath.startsWith("/")) return prefix + relativePath;
		return prefix + "/" + relativePath;
	}

	public String createUrl(Project project, AEntity entity) {
		String hashtag = "#project=" + project.getId();
		if (entity != null) hashtag += "|entity=" + entity.getId();
		return createUrl(hashtag);
	}

	private void createTestData() {
		log.warn("Creating test data");

		List<User> users = new ArrayList<User>();
		User po = getUserDao().postUserWithDefaultPassword("cartman");
		users.add(po);
		users.add(getUserDao().postUserWithDefaultPassword("homer"));
		users.add(getUserDao().postUserWithDefaultPassword("duke"));
		users.add(getUserDao().postUserWithDefaultPassword("spinne"));

		User admin = getUserDao().getUserByName("admin");
		User sm = admin;
		getProjectDao().postExampleProject(admin, po, sm, users);

		getTransactionService().commit();
	}

	@Override
	protected void scheduleTasks(TaskManager tm) {
		tm.scheduleWithFixedDelay(autowire(new DestroyTimeoutedSessionsTask()), Tm.MINUTE);
		tm.scheduleWithFixedDelay(autowire(new HomepageUpdaterTask()), Tm.HOUR);
		tm.scheduleWithFixedDelay(autowire(getSubscriptionService().new Task()), Tm.MINUTE);

		if (getConfig().isDisableUsersWithUnverifiedEmails())
			tm.scheduleWithFixedDelay(autowire(new DisableUsersWithUnverifiedEmailsTask()), Tm.SECOND * 10, Tm.HOUR);
		if (getConfig().isDisableInactiveUsers())
			tm.scheduleWithFixedDelay(autowire(new DisableInactiveUsersTask()), Tm.SECOND * 20, Tm.HOUR);
		if (getConfig().isDeleteOldProjects())
			tm.scheduleWithFixedDelay(autowire(new DeleteOldProjectsTask()), Tm.SECOND * 30, Tm.HOUR * 25);
		if (getConfig().isDeleteDisabledUsers())
			tm.scheduleWithFixedDelay(autowire(new DeleteDisabledUsersTask()), Tm.MINUTE * 3, Tm.HOUR * 26);
	}

	@Override
	public String getApplicationDataDir() {
		return getConfig().getDataPath();
	}

	@Override
	protected void onShutdownWebApplication() {
		getSubscriptionService().flush();
		getTransactionService().commit();
	}

	@Override
	public Url getHomeUrl() {
		return new Url("index.html");
	}

	private String getBaseUrl() {
		String url = getSystemConfig().getUrl();
		return url == null ? "http://localhost:8888/kunagi/" : url;
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
		return admin.matchesPassword(getSystemConfig().getDefaultUserPassword());
	}

	public synchronized void postProjectEvent(Project project, String message, AEntity subject) {
		ProjectEvent event = getProjectEventDao().postEvent(project, message, subject);
		sendToConversationsByProject(project, event);
		sendToConversationsByProject(project, event.postChatMessage());
		Utl.sleep(100);
	}

	public void sendToConversationsByProject(GwtConversation conversation, AEntity entity) {
		sendToConversationsByProject(conversation.getProject(), entity);
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
		sb.append("Kunagi URL: ").append(createUrl(null)).append("\n");
		sb.append("Name: ").append(user.getName()).append("\n");
		sb.append("Email: ").append(user.getEmail()).append("\n");
		sb.append("OpenID: ").append(user.getOpenId()).append("\n");
		sb.append("Date/Time: ").append(DateAndTime.now()).append("\n");
		sb.append("Host: ").append(host).append("\n");
		String subject = user.getLabel() + " registered on " + getBaseUrl();
		try {
			getEmailSender().sendEmail((String) null, null, subject, sb.toString());
		} catch (Throwable ex) {
			log.error("Sending notification email failed:", subject, ex);
		}
	}

	private EmailSender emailSender;

	public EmailSender getEmailSender() {
		if (emailSender == null) emailSender = autowire(new EmailSender());
		return emailSender;
	}

	private SubscriptionService subscriptionService;

	public SubscriptionService getSubscriptionService() {
		if (subscriptionService == null) subscriptionService = autowire(new SubscriptionService());
		return subscriptionService;
	}

}
