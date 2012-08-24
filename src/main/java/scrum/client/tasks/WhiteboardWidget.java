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
	private ButtonWidget pullNextButton;
	private ButtonWidget hideShowButton;

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

		pullNextButton = new ButtonWidget(new PullNextRequirementAction(sprint));

		whiteboardWrapper = new SimplePanel();
		whiteboardWrapper.setVisible(getCurrentProject().isTeamMember(getCurrentUser()));
		whiteboardHeader = new HTML();

		PagePanel page = new PagePanel();
		page.addHeader(whiteboardHeader, pullNextButton, whiteboardWrapper);
		page.addSection(grid);
		userGuide = new UserGuideWidget(getLocalizer().views().whiteboard(), sprint.getRequirements().size() < 3,
				getCurrentUser().getHideUserGuideWhiteboardModel());
		page.addSection(userGuide);
		return page;
	}

	private String getPageHeader() {
		return whiteboardManager.isMyRequirementsVisible() ? "Whiteboard (only my tasks)" : "Whiteboard";
	}

	@Override
	protected void onUpdate() {
		Sprint sprint = getCurrentProject().getCurrentSprint();
		whiteboardHeader.setHTML(getPageHeader());
		hideShowButton = new ButtonWidget(whiteboardManager.isMyRequirementsVisible() ? new HideMyWhiteboardAction()
				: new ShowMyWhiteboardAction());
		whiteboardWrapper.setWidget(hideShowButton);

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
		pullNextButton.update();
		hideShowButton.update();
		// super.onUpdate();
	}

	private void setLabels() {
		setWidget(0, 0, openLabel, "33%", "WhiteboardWidget-header");
		setWidget(0, 1, ownedLabel, "33%", "WhiteboardWidget-header");
		setWidget(0, 2, doneLabel, "33%", "WhiteboardWidget-header");
	}

	private void setWidgets(int row, Requirement requirement) {
		// null
		setWidget(row, 0, openTasks.get(requirement), "33%", "WhiteboardWidget-open");
		setWidget(row, 1, ownedTasks.get(requirement), "33%", "WhiteboardWidget-owned");
		setWidget(row, 2, closedTasks.get(requirement), "33%", "WhiteboardWidget-done");
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
		list.setSelectionManager(getSelectionManager());
		list.addAdditionalStyleName("WhiteboardWidget-requirement-list");
		list.setDnd(false);
		list.setDndSorting(false);
		list.setObjects(requirement);
		return list;
		// Label label = new Label(requirement.getReference() + " " + requirement.getLabel());
		// label.setStyleName("WhiteboardWidget-requirement-label");
		// return label;
	}

	private void updateTaskLists(Requirement requirement) {
		if (requirement == null) return;

		TaskListWidget openTasksList = openTasks.get(requirement);
		TaskListWidget ownedTasksList = ownedTasks.get(requirement);
		TaskListWidget closedTasksList = closedTasks.get(requirement);

		Task selectedTaskInOpen = openTasksList.getSelectedTask();
		Task selectedTaskInOwned = ownedTasksList.getSelectedTask();
		Task selectedTaskInClosed = closedTasksList.getSelectedTask();

		List<Task> openTaskList = new ArrayList<Task>();
		List<Task> ownedTaskList = new ArrayList<Task>();
		List<Task> closedTaskList = new ArrayList<Task>();
		for (Task task : requirement.getTasksInSprint()) {
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

		openTasksList.setTasks(openTaskList);
		ownedTasksList.setTasks(ownedTaskList);
		closedTasksList.setTasks(closedTaskList);

		if (selectedTaskInOpen != null && !openTaskList.contains(selectedTaskInOpen))
			selectionManager.select(selectedTaskInOpen);
		if (selectedTaskInOwned != null && !ownedTaskList.contains(selectedTaskInOwned))
			selectionManager.select(selectedTaskInOwned);
		if (selectedTaskInClosed != null && !closedTaskList.contains(selectedTaskInClosed))
			selectionManager.select(selectedTaskInClosed);
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

}
