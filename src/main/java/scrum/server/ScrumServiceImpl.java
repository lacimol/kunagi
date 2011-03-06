package scrum.server;

import ilarkesto.auth.Auth;
import ilarkesto.auth.AuthenticationFailedException;
import ilarkesto.auth.WrongPasswordException;
import ilarkesto.base.PermissionDeniedException;
import ilarkesto.base.Reflect;
import ilarkesto.base.Utl;
import ilarkesto.base.time.Date;
import ilarkesto.base.time.DateAndTime;
import ilarkesto.core.logging.Log;
import ilarkesto.integration.ldap.Ldap;
import ilarkesto.persistence.ADao;
import ilarkesto.persistence.AEntity;
import ilarkesto.persistence.EntityDoesNotExistException;
import ilarkesto.webapp.AWebApplication;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scrum.client.DataTransferObject;
import scrum.client.admin.SystemMessage;
import scrum.server.admin.ProjectUserConfig;
import scrum.server.admin.SystemConfig;
import scrum.server.admin.User;
import scrum.server.admin.UserDao;
import scrum.server.collaboration.ChatMessage;
import scrum.server.collaboration.Comment;
import scrum.server.collaboration.CommentDao;
import scrum.server.collaboration.EmoticonDao;
import scrum.server.collaboration.Subject;
import scrum.server.collaboration.Wikipage;
import scrum.server.common.Numbered;
import scrum.server.common.Transient;
import scrum.server.files.File;
import scrum.server.impediments.Impediment;
import scrum.server.issues.Issue;
import scrum.server.issues.IssueDao;
import scrum.server.journal.Change;
import scrum.server.journal.ChangeDao;
import scrum.server.journal.ProjectEvent;
import scrum.server.journal.ProjectEventDao;
import scrum.server.pr.BlogEntry;
import scrum.server.pr.BlogEntryDao;
import scrum.server.project.Project;
import scrum.server.project.ProjectDao;
import scrum.server.project.Requirement;
import scrum.server.project.RequirementDao;
import scrum.server.release.Release;
import scrum.server.release.ReleaseDao;
import scrum.server.risks.Risk;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.Task;
import scrum.server.task.TaskDaySnapshot;

public class ScrumServiceImpl extends GScrumServiceImpl {

	private static final Log LOG = Log.get(ScrumServiceImpl.class);
	private static final long serialVersionUID = 1;

	// --- dependencies ---

	private transient ProjectDao projectDao;
	private transient UserDao userDao;
	private transient RequirementDao requirementDao;
	private transient IssueDao issueDao;
	private transient ReleaseDao releaseDao;
	private transient CommentDao commentDao;
	private transient ScrumWebApplication webApplication;
	private transient ProjectEventDao projectEventDao;
	private transient EmoticonDao emoticonDao;
	private transient ChangeDao changeDao;
	private transient BlogEntryDao blogEntryDao;

	public void setReleaseDao(ReleaseDao releaseDao) {
		this.releaseDao = releaseDao;
	}

	public void setChangeDao(ChangeDao changeDao) {
		this.changeDao = changeDao;
	}

	public void setEmoticonDao(EmoticonDao emoticonDao) {
		this.emoticonDao = emoticonDao;
	}

	public void setProjectEventDao(ProjectEventDao projectEventDao) {
		this.projectEventDao = projectEventDao;
	}

	public void setWebApplication(ScrumWebApplication webApplication) {
		this.webApplication = webApplication;
	}

	public void setRequirementDao(RequirementDao requirementDao) {
		this.requirementDao = requirementDao;
	}

	public void setIssueDao(IssueDao issueDao) {
		this.issueDao = issueDao;
	}

	public void setProjectDao(ProjectDao projectDao) {
		this.projectDao = projectDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void setCommentDao(CommentDao commentDao) {
		this.commentDao = commentDao;
	}

	public void setBlogEntryDao(BlogEntryDao blogEntryDao) {
		this.blogEntryDao = blogEntryDao;
	}

	// --- ---

	private void onStartConversation(GwtConversation conversation) {
		User user = conversation.getSession().getUser();
		if (user == null) throw new PermissionDeniedException("Login required.");
		conversation.clearRemoteEntities();
		conversation.getNextData().applicationInfo = webApplication.getApplicationInfo();
		conversation.sendToClient(webApplication.getSystemConfig());
		conversation.getNextData().setUserId(user.getId());
		conversation.sendUserScopeDataToClient(user);
	}

	@Override
	public void onSendIssueReplyEmail(GwtConversation conversation, String issueId, String from, String to,
			String subject, String text) {
		assertProjectSelected(conversation);
		Issue issue = issueDao.getById(issueId);
		webApplication.sendEmail(from, to, subject, text);
		User user = conversation.getSession().getUser();
		postProjectEvent(conversation, user.getName() + " emailed a response to " + issue.getReferenceAndLabel(), issue);
		Change change = changeDao.postChange(issue, user, "@reply", null, text);
		conversation.sendToClient(change);
	}

	@Override
	public void onCreateExampleProject(GwtConversation conversation) {
		User user = conversation.getSession().getUser();
		Project project = projectDao.postExampleProject(user, user, user);
		conversation.sendToClient(project);
	}

	@Override
	public void onSearch(GwtConversation conversation, String text) {
		Project project = conversation.getProject();
		if (project == null) return;
		List<AEntity> foundEntities = project.search(text);
		LOG.debug("Found entities for search", "\"" + text + "\"", "->", foundEntities);
		conversation.sendToClient(foundEntities);
	}

	@Override
	public void onUpdateSystemMessage(GwtConversation conversation, SystemMessage systemMessage) {
		User user = conversation.getSession().getUser();
		if (user == null || user.isAdmin() == false) throw new PermissionDeniedException();
		webApplication.updateSystemMessage(systemMessage);
	}

	@Override
	public void onLogout(GwtConversation conversation) {
		WebSession session = conversation.getSession();
		webApplication.destroyWebSession(session, getThreadLocalRequest().getSession());
	}

	@Override
	public void onResetPassword(GwtConversation conversation, String userId) {
		if (!conversation.getSession().getUser().isAdmin()) throw new PermissionDeniedException();
		User user = userDao.getById(userId);
		if (webApplication.getSystemConfig().isSmtpServerSet() && user.isEmailSet()) {
			user.triggerPasswordReset();
		} else {
			user.setPassword(webApplication.getSystemConfig().getDefaultUserPassword());
		}
	}

	@Override
	public void onChangePassword(GwtConversation conversation, String oldPassword, String newPassword) {
		User user = conversation.getSession().getUser();
		if (!user.isAdmin() && user.matchesPassword(oldPassword) == false) throw new WrongPasswordException();

		user.setPassword(newPassword);

		LOG.info("password changed by", user);
	}

	@Override
	public void onCreateEntity(GwtConversation conversation, String type, Map properties) {
		String id = (String) properties.get("id");
		if (id == null) throw new NullPointerException("id == null");

		ADao dao = getDaoService().getDaoByName(type);
		AEntity entity = dao.newEntityInstance(id);
		entity.updateProperties(properties);
		User currentUser = conversation.getSession().getUser();
		Project currentProject = conversation.getProject();

		if (entity instanceof Numbered) {
			((Numbered) entity).updateNumber();
		}

		if (entity instanceof Project) {
			Project project = (Project) entity;
			project.addParticipant(currentUser);
			project.addAdmin(currentUser);
			project.addProductOwner(currentUser);
			project.addScrumMaster(currentUser);
			project.addTeamMember(currentUser);
		}

		if (entity instanceof Comment) {
			Comment comment = (Comment) entity;
			comment.setDateAndTime(DateAndTime.now());
			postProjectEvent(conversation, comment.getAuthor().getName() + " commented on " + comment.getParent(),
				comment.getParent());
			currentProject.updateHomepage(comment.getParent(), true);
		}

		if (entity instanceof ChatMessage) {
			ChatMessage chatMessage = (ChatMessage) entity;
			chatMessage.setDateAndTime(DateAndTime.now());
		}

		if (entity instanceof Impediment) {
			Impediment impediment = (Impediment) entity;
			impediment.setDate(Date.today());
		}

		if (entity instanceof Issue) {
			Issue issue = (Issue) entity;
			issue.setDate(DateAndTime.now());
			issue.setCreator(currentUser);
		}

		if (entity instanceof Task) {
			Task task = (Task) entity;
			Requirement requirement = task.getRequirement();
			requirement.setRejectDate(null);
			requirement.setClosed(false);
			sendToClients(conversation, requirement);
		}

		if (entity instanceof BlogEntry) {
			BlogEntry blogEntry = (BlogEntry) entity;
			blogEntry.setDateAndTime(DateAndTime.now());
			blogEntry.addAuthor(currentUser);
		}

		if (!(entity instanceof Transient)) dao.saveEntity(entity);

		sendToClients(conversation, entity);

		if (entity instanceof Task || entity instanceof Requirement || entity instanceof Wikipage
				|| entity instanceof Risk || entity instanceof Impediment || entity instanceof Issue
				|| entity instanceof BlogEntry) {
			User user = currentUser;
			Change change = changeDao.postChange(entity, user, "@created", null, null);
			conversation.sendToClient(change);
		}

		if (currentUser != null && currentProject != null) {
			ProjectUserConfig config = currentProject.getUserConfig(currentUser);
			config.touch();
			sendToClients(conversation, config);
		}
	}

	@Override
	public void onDeleteEntity(GwtConversation conversation, String entityId) {
		AEntity entity = getDaoService().getEntityById(entityId);
		User user = conversation.getSession().getUser();
		if (!Auth.isDeletable(entity, user)) throw new PermissionDeniedException();

		if (entity instanceof File) {
			File file = (File) entity;
			file.deleteFile();
		}

		ADao dao = getDaoService().getDao(entity);
		dao.deleteEntity(entity);

		Project project = conversation.getProject();
		if (project != null) {
			for (GwtConversation c : webApplication.getConversationsByProject(project, conversation)) {
				c.getNextData().addDeletedEntity(entityId);
			}
		}

		if (user != null && project != null) {
			ProjectUserConfig config = project.getUserConfig(user);
			config.touch();
			sendToClients(conversation, config);
		}
	}

	@Override
	public void onChangeProperties(GwtConversation conversation, String entityId, Map properties) {
		AEntity entity = getDaoService().getEntityById(entityId);
		User currentUser = conversation.getSession().getUser();
		if (!Auth.isEditable(entity, currentUser)) throw new PermissionDeniedException();

		Sprint previousRequirementSprint = entity instanceof Requirement ? ((Requirement) entity).getSprint() : null;

		if (entity instanceof Requirement) {
			postChangeIfChanged(conversation, entity, properties, currentUser, "description");
			postChangeIfChanged(conversation, entity, properties, currentUser, "testDescription");
			postChangeIfChanged(conversation, entity, properties, currentUser, "sprintId");
			postChangeIfChanged(conversation, entity, properties, currentUser, "closed");
			postChangeIfChanged(conversation, entity, properties, currentUser, "issueId");
		}
		if (entity instanceof Task) {
			// update sprint day snapshot before change
			conversation.getProject().getCurrentSprint().getDaySnapshot(Date.today()).updateWithCurrentSprint();
			((Task) entity).getDaySnapshot(Date.today()).updateWithCurrentTask();
		}
		if (entity instanceof Wikipage) {
			postChangeIfChanged(conversation, entity, properties, currentUser, "text");
		}
		if (entity instanceof Risk) {
			postChangeIfChanged(conversation, entity, properties, currentUser, "description");
			postChangeIfChanged(conversation, entity, properties, currentUser, "probability");
			postChangeIfChanged(conversation, entity, properties, currentUser, "impact");
			postChangeIfChanged(conversation, entity, properties, currentUser, "probabilityMitigation");
			postChangeIfChanged(conversation, entity, properties, currentUser, "impactMitigation");
		}
		if (entity instanceof Impediment) {
			postChangeIfChanged(conversation, entity, properties, currentUser, "description");
			postChangeIfChanged(conversation, entity, properties, currentUser, "solution");
			postChangeIfChanged(conversation, entity, properties, currentUser, "closed");
		}
		if (entity instanceof Issue) {
			postChangeIfChanged(conversation, entity, properties, currentUser, "description");
			postChangeIfChanged(conversation, entity, properties, currentUser, "statement");
			postChangeIfChanged(conversation, entity, properties, currentUser, "closeDate");
			postChangeIfChanged(conversation, entity, properties, currentUser, "storyId");
		}
		if (entity instanceof BlogEntry) {
			postChangeIfChanged(conversation, entity, properties, currentUser, "text");
		}

		entity.updateProperties(properties);

		if (entity instanceof Task) onTaskChanged(conversation, (Task) entity, properties);
		if (entity instanceof Requirement)
			onRequirementChanged(conversation, (Requirement) entity, properties, previousRequirementSprint);
		if (entity instanceof Impediment) onImpedimentChanged(conversation, (Impediment) entity, properties);
		if (entity instanceof Issue) onIssueChanged(conversation, (Issue) entity, properties);
		if (entity instanceof BlogEntry) onBlogEntryChanged(conversation, (BlogEntry) entity, properties);
		if (entity instanceof Comment) onCommentChanged(conversation, (Comment) entity);
		if (entity instanceof SystemConfig) onSystemConfigChanged(conversation, (SystemConfig) entity, properties);

		Project currentProject = conversation.getProject();
		if (currentUser != null && currentProject != null) {
			ProjectUserConfig config = currentProject.getUserConfig(currentUser);
			config.touch();
			sendToClients(conversation, config);
		}

		conversation.clearRemoteEntitiesByType(Change.class);
		sendToClients(conversation, entity);
	}

	private void onSystemConfigChanged(GwtConversation conversation, SystemConfig config, Map properties) {
		if (properties.containsKey("url")) {
			webApplication.getConfig().setUrl(config.getUrl());
		}
	}

	private void onCommentChanged(GwtConversation conversation, Comment comment) {
		conversation.getProject().updateHomepage(comment.getParent(), false);
	}

	private void onBlogEntryChanged(GwtConversation conversation, BlogEntry blogEntry, Map properties) {
		User currentUser = conversation.getSession().getUser();

		if (properties.containsKey("text")) {
			blogEntry.addAuthor(currentUser);
		}

		if (properties.containsKey("published")) {
			if (blogEntry.isPublished()) {
				postProjectEvent(conversation,
					currentUser.getName() + " published " + blogEntry.getReferenceAndLabel(), blogEntry);
			}
			blogEntry.getProject().updateHomepage();
		}
	}

	private void onIssueChanged(GwtConversation conversation, Issue issue, Map properties) {
		User currentUser = conversation.getSession().getUser();

		if (properties.containsKey("closeDate")) {
			if (issue.isClosed()) {
				issue.setCloseDate(Date.today());
				postProjectEvent(conversation, currentUser.getName() + " closed " + issue.getReferenceAndLabel(), issue);
			} else {
				postProjectEvent(conversation, currentUser.getName() + " reopened " + issue.getReferenceAndLabel(),
					issue);
			}
		}

		if (properties.containsKey("ownerId") && issue.isOwnerSet()) {
			postProjectEvent(conversation, currentUser.getName() + " claimed " + issue.getReferenceAndLabel(), issue);

			Release nextRelease = issue.getProject().getNextRelease();
			if (issue.isFixReleasesEmpty() && nextRelease != null) {
				issue.setFixReleases(Collections.singleton(nextRelease));
				sendToClients(conversation, issue);
			}
		}

		if (properties.containsKey("fixDate")) {
			if (issue.isFixed()) {
				postProjectEvent(conversation, currentUser.getName() + " fixed " + issue.getReferenceAndLabel(), issue);
			} else {
				postProjectEvent(conversation,
					currentUser.getName() + " rejected fix for " + issue.getReferenceAndLabel(), issue);
			}
		}

		if (properties.containsKey("urgent")) {
			if (issue.isBug()) {
				Release currentRelease = issue.getProject().getCurrentRelease();
				if (issue.isAffectedReleasesEmpty() && currentRelease != null) {
					issue.setAffectedReleases(Collections.singleton(currentRelease));
					sendToClients(conversation, issue);
				}
			}
		}

		issue.getProject().updateHomepage(issue, false);
	}

	private void onImpedimentChanged(GwtConversation conversation, Impediment impediment, Map properties) {
		User currentUser = conversation.getSession().getUser();
		if (impediment.isClosed() && properties.containsKey("closed")) {
			impediment.setDate(Date.today());
			postProjectEvent(conversation, currentUser.getName() + " closed " + impediment.getReferenceAndLabel(),
				impediment);
		}
	}

	private void onRequirementChanged(GwtConversation conversation, Requirement requirement, Map properties,
			Sprint previousRequirementSprint) {
		Project currentProject = conversation.getProject();
		Sprint sprint = requirement.getSprint();
		boolean inCurrentSprint = sprint != null && currentProject.isCurrentSprint(sprint);
		User currentUser = conversation.getSession().getUser();

		if (properties.containsKey("description") || properties.containsKey("testDescription")
				|| properties.containsKey("qualitysIds")) {
			requirement.setDirty(true);
			conversation.sendToClient(requirement);
		}

		if (properties.containsKey("rejectDate") && requirement.isRejectDateSet()) {
			postProjectEvent(conversation, currentUser.getName() + " rejected " + requirement.getReferenceAndLabel(),
				requirement);
		}

		if (properties.containsKey("accepted") && requirement.isRejectDateSet()) {
			postProjectEvent(conversation, currentUser.getName() + " accepted " + requirement.getReferenceAndLabel(),
				requirement);
		}

		if (sprint != previousRequirementSprint) {
			if (properties.containsKey("sprintId")) {
				if (inCurrentSprint) {
					postProjectEvent(conversation,
						currentUser.getName() + " pulled " + requirement.getReferenceAndLabel() + " to current sprint",
						requirement);
				} else {
					postProjectEvent(conversation,
						currentUser.getName() + " kicked " + requirement.getReferenceAndLabel()
								+ " from current sprint", requirement);
				}
			}
		}

		if (properties.containsKey("estimatedWork")) {
			requirement.initializeEstimationVotes();
			requirement.setDirty(false);
			requirement.setWorkEstimationVotingShowoff(false);
			requirement.setWorkEstimationVotingActive(false);
			conversation.sendToClient(requirement);
		}

		requirement.getProject().getCurrentSprintSnapshot().update();
	}

	private void onTaskChanged(GwtConversation conversation, Task task, Map properties) {
		// update sprint day snapshot after change
		conversation.getProject().getCurrentSprint().getDaySnapshot(Date.today()).updateWithCurrentSprint();
		// update and send back task day snapshot
		TaskDaySnapshot taskDaySnapshot = task.getDaySnapshot(Date.today());
		taskDaySnapshot.updateWithCurrentTask();
		sendToClients(conversation, taskDaySnapshot);
		Requirement requirement = task.getRequirement();
		if (requirement.isInCurrentSprint()) {
			User currentUser = conversation.getSession().getUser();
			if (task.isClosed() && properties.containsKey("remainingWork")) {
				String event = currentUser.getName() + " closed " + task.getReferenceAndLabel();
				if (requirement.isTasksClosed()) {
					event += ", all tasks closed in " + requirement.getReferenceAndLabel();
				}
				postProjectEvent(conversation, event, task);
			} else if (task.isOwnerSet() && properties.containsKey("ownerId")) {
				postProjectEvent(conversation, currentUser.getName() + " claimed " + task.getReferenceAndLabel(), task);
			}
			if (!task.isOwnerSet() && properties.containsKey("ownerId")) {
				postProjectEvent(conversation, currentUser.getName() + " unclaimed " + task.getReferenceAndLabel(),
					task);
			}
			if (!task.isClosed() && requirement.isRejectDateSet()) {
				requirement.setRejectDate(null);
				sendToClients(conversation, requirement);
			}
		}
	}

	@Override
	public void onSelectProject(GwtConversation conversation, String projectId) {
		Project project = projectDao.getById(projectId);
		User user = conversation.getSession().getUser();
		if (!project.isVisibleFor(user))
			throw new PermissionDeniedException("Project '" + project + "' is not visible for user '" + user + "'");

		project.setLastOpenedDateAndTime(DateAndTime.now());
		conversation.setProject(project);
		user.setCurrentProject(project);
		ProjectUserConfig config = project.getUserConfig(user);
		config.touch();

		conversation.sendToClient(project);
		conversation.sendToClient(project.getSprints());
		conversation.sendToClient(project.getParticipants());
		Set<Requirement> requirements = project.getRequirements();
		conversation.sendToClient(requirements);
		for (Requirement requirement : requirements) {
			conversation.sendToClient(requirement.getEstimationVotes());
		}
		conversation.sendToClient(project.getQualitys());
		conversation.sendToClient(project.getTasks());
		conversation.sendToClient(project.getUserConfigs());
		conversation.sendToClient(project.getWikipages());
		conversation.sendToClient(project.getImpediments());
		conversation.sendToClient(project.getRisks());
		conversation.sendToClient(project.getLatestProjectEvents());
		conversation.sendToClient(project.getCalendarEvents());
		conversation.sendToClient(project.getFiles());
		conversation.sendToClient(project.getOpenIssues());
		conversation.sendToClient(project.getReleases());
		conversation.sendToClient(project.getBlogEntrys());
		conversation.sendToClient(project.getTaskDaySnapshots());

		sendToClients(conversation, config);
	}

	@Override
	public void onCloseProject(GwtConversation conversation) {
		Project project = conversation.getProject();
		if (project != null && conversation.getSession().getGwtConversations().size() < 2) {
			ProjectUserConfig config = project.getUserConfig(conversation.getSession().getUser());
			config.reset();
			sendToClients(conversation, config);
		}
		conversation.clearRemoteEntities();
		conversation.setProject(null);
	}

	@Override
	public void onRequestForum(GwtConversation conversation, boolean all) {
		Project project = conversation.getProject();
		Set<AEntity> parents = new HashSet<AEntity>();
		for (Subject subject : project.getSubjects()) {
			if (subject.getComments().isEmpty()) parents.add(subject);
		}
		for (Comment comment : project.getLatestComments()) {
			AEntity parent = comment.getParent();
			if (!all && !conversation.isAvailableOnClient(parent)
					&& comment.getDateAndTime().getPeriodToNow().abs().toDays() > 7) continue;
			conversation.sendToClient(comment);
			parents.add(parent);
		}
		conversation.sendToClient(parents);
	}

	@Override
	public void onRequestImpediments(GwtConversation conversation) {
		assertProjectSelected(conversation);
		Project project = conversation.getProject();
		conversation.sendToClient(project.getImpediments());
	}

	@Override
	public void onRequestRisks(GwtConversation conversation) {
		assertProjectSelected(conversation);
		Project project = conversation.getProject();
		conversation.sendToClient(project.getRisks());
	}

	@Override
	public void onRequestAcceptedIssues(GwtConversation conversation) {
		assertProjectSelected(conversation);
		Project project = conversation.getProject();
		conversation.sendToClient(project.getAcceptedIssues());
	}

	@Override
	public void onRequestClosedIssues(GwtConversation conversation) {
		assertProjectSelected(conversation);
		Project project = conversation.getProject();
		conversation.sendToClient(project.getClosedIssues());
	}

	@Override
	public void onRequestReleaseIssues(GwtConversation conversation, String releaseId) {
		assertProjectSelected(conversation);
		Project project = conversation.getProject();
		Release release = releaseDao.getById(releaseId);
		if (!release.isProject(project)) throw new PermissionDeniedException();
		conversation.sendToClient(release.getIssues());
	}

	@Override
	public void onRequestEntity(GwtConversation conversation, String entityId) {
		assertProjectSelected(conversation);

		try {
			AEntity entity = getDaoService().getById(entityId);
			if (!Auth.isVisible(entity, conversation.getSession().getUser())) throw new PermissionDeniedException();
			// TODO check if entity is from project
			conversation.sendToClient(entity);
		} catch (EntityDoesNotExistException ex) {
			// nop
		}
	}

	@Override
	public void onRequestEntityByReference(GwtConversation conversation, String reference) {
		assertProjectSelected(conversation);
		Project project = conversation.getProject();

		AEntity entity = project.getEntityByReference(reference);
		if (entity == null) {
			LOG.info("Requested entity not found:", reference);
		} else {
			conversation.sendToClient(entity);
		}
	}

	@Override
	public void onRequestComments(GwtConversation conversation, String parentId) {
		conversation.sendToClient(commentDao.getCommentsByParentId(parentId));
	}

	@Override
	public void onRequestChanges(GwtConversation conversation, String parentId) {
		conversation.sendToClient(changeDao.getChangesByParentId(parentId));
	}

	@Override
	public void onRequestRequirementEstimationVotes(GwtConversation conversation, String requirementId) {
		assertProjectSelected(conversation);
		Project project = conversation.getProject();
		Requirement requirement = requirementDao.getById(requirementId);
		if (!requirement.isProject(project)) throw new PermissionDeniedException();
		conversation.sendToClient(requirement.getEstimationVotes());
	}

	@Override
	public void onActivateRequirementEstimationVoting(GwtConversation conversation, String requirementId) {
		Requirement requirement = requirementDao.getById(requirementId);
		if (requirement == null || !requirement.isProject(conversation.getProject()))
			throw new PermissionDeniedException();
		requirement.initializeEstimationVotes();
		requirement.setWorkEstimationVotingActive(true);
		requirement.setWorkEstimationVotingShowoff(false);
		sendToClients(conversation, requirement);
		sendToClients(conversation, requirement.getEstimationVotes());
	}

	@Override
	public void onSwitchToNextSprint(GwtConversation conversation) {
		assertProjectSelected(conversation);
		Project project = conversation.getProject();
		Sprint oldSprint = project.getCurrentSprint();
		for (Requirement requirement : oldSprint.getRequirements()) {
			if (!requirement.isClosed()) {
				requirement.setDirty(true);
				sendToClients(conversation, requirement);
			}
		}
		project.switchToNextSprint();
		sendToClients(conversation, project.getSprints());
		sendToClients(conversation, project.getRequirements());
		sendToClients(conversation, project.getTasks());
		sendToClients(conversation, project);
	}

	@Override
	public void onUpdateProjectHomepage(GwtConversation conversation) {
		assertProjectSelected(conversation);
		Project project = conversation.getProject();
		project.updateHomepage();
	}

	@Override
	public void onSendTestEmail(GwtConversation conversation) {
		webApplication.sendEmail(null, null, "Kunagi email test", "Kunagi email test");
	}

	@Override
	public void onTestLdap(GwtConversation conversation) {
		SystemConfig config = webApplication.getSystemConfig();
		try {
			Ldap.authenticateUserGetEmail(config.getLdapUrl(), config.getLdapUser(), config.getLdapPassword(),
				config.getLdapBaseDn(), config.getLdapUserFilterRegex(), "dummyUser", "dummyPassword");
		} catch (AuthenticationFailedException ex) {}
	}

	@Override
	public void onTouchLastActivity(GwtConversation conversation) {
		Project project = conversation.getProject();
		if (project == null) return;
		project.getUserConfig(conversation.getSession().getUser()).touch();
	}

	@Override
	public void onPing(GwtConversation conversation) {
		// nop
	}

	@Override
	public void onSleep(GwtConversation conversation, long millis) {
		Utl.sleep(millis);
	}

	// --- helper ---

	@Override
	public DataTransferObject startConversation(int conversationNumber) {
		LOG.debug("startConversation");
		WebSession session = (WebSession) getSession();
		GwtConversation conversation = session.getGwtConversation(-1);
		ilarkesto.di.Context context = ilarkesto.di.Context.get();
		context.setName("gwt-srv:startSession");
		context.bindCurrentThread();
		try {
			onStartConversation(conversation);
		} catch (Throwable t) {
			handleServiceMethodException(conversation.getNumber(), "startSession", t);
			throw new RuntimeException(t);
		}
		scrum.client.DataTransferObject ret = (scrum.client.DataTransferObject) conversation.popNextData();
		onServiceMethodExecuted(context);
		return ret;
	}

	private void postChangeIfChanged(GwtConversation conversation, AEntity entity, Map properties, User user,
			String property) {
		if (properties.containsKey(property)) {
			boolean reference = property.endsWith("Id");
			Object oldValue = Reflect.getProperty(entity, property);
			Object newValue = properties.get(property);
			Change change = changeDao.postChange(entity, user, property, oldValue, newValue);
			conversation.sendToClient(change);
		}
	}

	private void postProjectEvent(GwtConversation conversation, String message, AEntity subject) {
		assertProjectSelected(conversation);
		ProjectEvent event = projectEventDao.postEvent(conversation.getProject(), message, subject);
		sendToClients(conversation, event);
		sendToClients(conversation, event.createChatMessage());
	}

	private void sendToClients(GwtConversation conversation, Collection<? extends AEntity> entities) {
		for (AEntity entity : entities) {
			sendToClients(conversation, entity);
		}
	}

	private void sendToClients(GwtConversation conversation, AEntity entity) {
		conversation.sendToClient(entity);
		webApplication.sendToOtherConversationsByProject(conversation, entity);
	}

	private void assertProjectSelected(GwtConversation conversation) {
		if (conversation.getProject() == null) throw new RuntimeException("No project selected.");
	}

	@Override
	protected Class<? extends AWebApplication> getWebApplicationClass() {
		return ScrumWebApplication.class;
	}

	@Override
	protected AWebApplication getWebApplication() {
		return webApplication;
	}

	@Override
	public void onConvertIssueToStory(GwtConversation conversation, String issueId) {
		Issue issue = issueDao.getById(issueId);
		Requirement story = requirementDao.postRequirement(issue);
		issue.setStatement(issue.getStatement() + "\n\n" + "Created Story " + story.getReference()
				+ " in Product Backlog.");
		issue.setCloseDate(Date.today());
		sendToClients(conversation, story);
		sendToClients(conversation, issue);
		User currentUser = conversation.getSession().getUser();
		postProjectEvent(conversation,
			currentUser.getName() + " created " + story.getReference() + " from " + issue.getReferenceAndLabel(), issue);
		changeDao.postChange(issue, currentUser, "storyId", null, story.getId());
		changeDao.postChange(story, currentUser, "issueId", null, issue.getId());
	}
}
