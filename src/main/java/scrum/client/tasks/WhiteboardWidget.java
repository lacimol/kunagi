package scrum.client.tasks;

import ilarkesto.core.scope.Scope;
import ilarkesto.gwt.client.ButtonWidget;
import ilarkesto.gwt.client.Gwt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scrum.client.admin.User;
import scrum.client.common.AScrumWidget;
import scrum.client.common.BlockListSelectionManager;
import scrum.client.common.BlockListWidget;
import scrum.client.common.ElementPredicate;
import scrum.client.common.UserGuideWidget;
import scrum.client.context.UserHighlightSupport;
import scrum.client.project.Requirement;
import scrum.client.sprint.PullNextRequirementAction;
import scrum.client.sprint.Sprint;
import scrum.client.sprint.Task;
import scrum.client.workspace.PagePanel;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class WhiteboardWidget extends AScrumWidget implements TaskBlockContainer, UserHighlightSupport {

	private WhiteboardManager whiteboardManager;
	private SimplePanel whiteboardWrapper;
	private HTML whiteboardHeader;

	private Grid grid;
	private HTML openLabel;
	private HTML ownedLabel;
	private HTML doneLabel;

	private Map<Requirement, BlockListWidget<Requirement>> requirementLists;
	private Map<Requirement, TaskListWidget> openTasks;
	private Map<Requirement, TaskListWidget> ownedTasks;
	private Map<Requirement, TaskListWidget> closedTasks;
	private BlockListSelectionManager selectionManager = new BlockListSelectionManager();

	private ElementPredicate<Task> predicate;

	private List<Requirement> knownRequirements = Collections.emptyList();
	private UserGuideWidget userGuide;

	private Sprint sprint;

	@Override
	protected Widget onInitialization() {
		whiteboardManager = Scope.get().getComponent(WhiteboardManager.class);
		sprint = getCurrentSprint();
		predicate = null;

		requirementLists = new HashMap<Requirement, BlockListWidget<Requirement>>();

		openLabel = new HTML();
		openLabel.setStyleName("WhiteboardWidget-columnLabel");
		openLabel.addStyleName("WhiteboardWidget-columnLabel-open");
		openTasks = new HashMap<Requirement, TaskListWidget>();

		ownedLabel = new HTML();
		ownedLabel.setStyleName("WhiteboardWidget-columnLabel");
		ownedLabel.addStyleName("WhiteboardWidget-columnLabel-owned");
		ownedTasks = new HashMap<Requirement, TaskListWidget>();

		doneLabel = new HTML();
		doneLabel.setStyleName("WhiteboardWidget-columnLabel");
		doneLabel.addStyleName("WhiteboardWidget-columnLabel-done");
		closedTasks = new HashMap<Requirement, TaskListWidget>();

		grid = new Grid();
		grid.setWidth("100%");
		grid.setCellPadding(0);
		grid.setCellSpacing(0);

		PagePanel page = new PagePanel();
		whiteboardWrapper = new SimplePanel();
		whiteboardWrapper.setVisible(sprint.getProject().isTeamMember(getCurrentUser()));
		whiteboardHeader = new HTML();
		page.addHeader(whiteboardHeader, new ButtonWidget(new PullNextRequirementAction(getCurrentSprint())),
			whiteboardWrapper);
		page.addSection(grid);
		userGuide = new UserGuideWidget(getLocalizer().views().whiteboard(), getCurrentProject().getCurrentSprint()
				.getRequirements().size() < 3, getCurrentUser().getHideUserGuideWhiteboardModel());
		page.addSection(userGuide);
		return page;
	}

	private String getPageHeader() {
		return whiteboardManager.isMyRequirementsVisible() ? "Whiteboard (only my tasks)" : "Whiteboard";
	}

	@Override
	protected void onUpdate() {

		whiteboardHeader.setHTML(getPageHeader());
		whiteboardWrapper.setWidget(new ButtonWidget(
				whiteboardManager.isMyRequirementsVisible() ? new HideMyWhiteboardAction()
						: new ShowMyWhiteboardAction()));

		openLabel.setHTML("<strong>Free Tasks</strong> (" + hours(sprint.getRemainingWorkInUnclaimedTasks())
				+ " to do)");
		ownedLabel.setHTML("<strong>Claimed Tasks</strong> (" + hours(sprint.getRemainingWorkInClaimedTasks())
				+ " to do, " + hours(sprint.getBurnedWorkInClaimedTasks()) + " done)");
		doneLabel.setHTML("<strong>Completed Tasks</strong> (" + hours(sprint.getBurnedWorkInClosedTasks()) + " done)");

		List<Requirement> requirements = getRequirements(sprint);
		Collections.sort(requirements, sprint.getRequirementsOrderComparator());

		if (requirements.equals(knownRequirements)) {
			// quick update without recreating whole gui
			for (Requirement requirement : requirements) {
				updateTaskLists(requirement);
			}
			super.onUpdate();
			return;
		}
		knownRequirements = requirements;
		selectionManager = new BlockListSelectionManager();
		grid.resize((requirements.size() * 2) + 1, 3);
		updateTasks(requirements);
		setLabels();
		// grid.getColumnFormatter().setWidth(0, "1*");
		// grid.getColumnFormatter().setWidth(1, "1*");
		// grid.getColumnFormatter().setWidth(2, "1*");

		int row = 1;
		for (Requirement requirement : requirements) {

			grid.setWidget(row, 0, getRequirementList(requirement));
			grid.getCellFormatter().getElement(row, 0).setAttribute("colspan", "3");
			row++;

			updateTaskLists(requirement);
			setWidgets(row, requirement);
			row++;
		}

		userGuide.update();
		super.onUpdate();
	}

	private void setLabels() {
		setWidget(0, 0, openLabel, "33%", "WhiteboardWidget-header");
		setWidget(0, 1, ownedLabel, "33%", "WhiteboardWidget-header");
		setWidget(0, 2, doneLabel, "33%", "WhiteboardWidget-header");
	}

	private void setWidgets(int row, Requirement requirement) {
		// grid.setWidget(row, 0, new Label(requirement.getLabel()));
		setWidget(row, 0, openTasks.get(requirement), null, "WhiteboardWidget-open");
		setWidget(row, 1, ownedTasks.get(requirement), null, "WhiteboardWidget-owned");
		setWidget(row, 2, closedTasks.get(requirement), null, "WhiteboardWidget-done");
	}

	private List<Requirement> getRequirements(Sprint sprint) {
		List<Requirement> results = new ArrayList<Requirement>();
		if (whiteboardManager.isMyRequirementsVisible()) {
			for (Requirement req : sprint.getRequirements()) {
				if (hasCurrentUserTask(req)) {
					results.add(req);
				}
			}
		} else {
			results = sprint.getRequirements();
		}
		return results;
	}

	/**
	 * True, if requirement has a task which owned by current user
	 * 
	 * @param requirement
	 * @return
	 */
	private boolean hasCurrentUserTask(Requirement requirement) {

		boolean hasReqUserTask = false;
		User currentUser = getCurrentUser();
		for (Task task : requirement.getTasks()) {
			if (currentUser.equals(task.getOwner())) {
				hasReqUserTask = true;
				break;
			}
		}
		return hasReqUserTask;

	}

	private boolean isTaskListable(Task task) {
		boolean isTaskListable = true;
		if (whiteboardManager.isMyRequirementsVisible()) {
			isTaskListable = task.isOwnerSet() && task.getOwner().equals(getCurrentUser());
		}
		return isTaskListable;
	}

	private void updateTasks(List<Requirement> requirements) {
		for (Requirement requirement : requirements) {
			openTasks.put(requirement, new TaskListWidget(requirement, this, new UnclaimTaskDropAction(requirement),
					true));
			ownedTasks.put(requirement, new TaskListWidget(requirement, this, new ClaimTaskDropAction(requirement),
					false));
			closedTasks.put(requirement, new TaskListWidget(requirement, this, new CloseTaskDropAction(requirement),
					false));
		}
	}

	private BlockListWidget<Requirement> getRequirementList(Requirement requirement) {
		BlockListWidget<Requirement> list = requirementLists.get(requirement);
		if (list == null) {
			list = createRequirementList(requirement);
			requirementLists.put(requirement, list);
		}
		return list;
	}

	private BlockListWidget<Requirement> createRequirementList(Requirement requirement) {
		BlockListWidget<Requirement> list = new BlockListWidget<Requirement>(RequirementInWhiteboardBlock.FACTORY);
		list.addAdditionalStyleName("WhiteboardWidget-requirement-list");
		list.setDndSorting(false);
		list.setObjects(requirement);
		return list;
		// Label label = new Label(requirement.getReference() + " " + requirement.getLabel());
		// label.setStyleName("WhiteboardWidget-requirement-label");
		// return label;
	}

	private void updateTaskLists(Requirement requirement) {

		List<Task> openTaskList = new ArrayList<Task>();
		List<Task> ownedTaskList = new ArrayList<Task>();
		List<Task> closedTaskList = new ArrayList<Task>();
		for (Task task : requirement.getTasks()) {
			if (isTaskListable(task)) {
				if (task.isClosed()) {
					closedTaskList.add(task);
				} else if (task.isOwnerSet()) {
					ownedTaskList.add(task);
				} else {
					openTaskList.add(task);
				}
			}
		}

		openTasks.get(requirement).setTasks(openTaskList);
		ownedTasks.get(requirement).setTasks(ownedTaskList);
		closedTasks.get(requirement).setTasks(closedTaskList);

	}

	private void updateHighlighting() {
		for (Requirement requirement : openTasks.keySet()) {
			openTasks.get(requirement).setTaskHighlighting(predicate);
			ownedTasks.get(requirement).setTaskHighlighting(predicate);
			closedTasks.get(requirement).setTaskHighlighting(predicate);
		}
	}

	@Override
	public void highlightUser(User user) {
		setTaskHighlighting(user == null ? null : new ByUserPredicate(user));
	}

	public void setTaskHighlighting(ElementPredicate<Task> predicate) {
		this.predicate = predicate;
		updateHighlighting();
	}

	public void clearTaskHighlighting() {
		this.predicate = null;
		updateHighlighting();
	}

	private void setWidget(int row, int col, Widget widget, String width, String className) {
		grid.setWidget(row, col, widget);
		if (width != null || className != null) {
			Element td = grid.getCellFormatter().getElement(row, col);
			if (width != null) td.setAttribute("width", width);
			if (className != null) td.setClassName(className);
		}
	}

	@Override
	public BlockListSelectionManager getSelectionManager() {
		return selectionManager;
	}

	public void selectRequirement(Requirement requirement) {
		if (requirement == null) return;
		BlockListWidget<Requirement> list = getRequirementList(requirement);
		if (list == null) return;
		list.showObject(requirement);
	}

	@Override
	public void selectTask(Task task) {
		if (task == null) return;
		Requirement requirement = task.getRequirement();
		updateTaskLists(requirement);
		selectionManager.select(task);
		update();
	}

	@Override
	protected boolean isResetRequired() {
		return sprint != getCurrentSprint();
	}

	@Override
	public boolean isShowOwner() {
		return true;
	}

	@Override
	public boolean isShowRequirement() {
		return false;
	}

	@Override
	public boolean isWideMode() {
		return false;
	}

	private String hours(Integer i) {
		return Gwt.formatHours(i);
	}

	private static class ByUserPredicate implements ElementPredicate<Task> {

		private final User user;

		public ByUserPredicate(User user) {
			this.user = user;
		}

		@Override
		public boolean contains(Task element) {
			return element.getOwner() != null && element.getOwner().equals(user);
		}
	}

	public HTML getOpenLabel() {
		return openLabel;
	}

	public HTML getOwnedLabel() {
		return ownedLabel;
	}

	public HTML getDoneLabel() {
		return doneLabel;
	}

}
