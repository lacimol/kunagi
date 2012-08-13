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
package scrum.client.sprint;

import ilarkesto.core.scope.Scope;
import ilarkesto.core.time.Date;
import ilarkesto.gwt.client.EntityDoesNotExistException;
import ilarkesto.gwt.client.HyperlinkWidget;
import ilarkesto.gwt.client.editor.AFieldModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import scrum.client.ScrumGwt;
import scrum.client.admin.Auth;
import scrum.client.admin.User;
import scrum.client.collaboration.ForumSupport;
import scrum.client.common.LabelSupport;
import scrum.client.common.ReferenceSupport;
import scrum.client.common.ShowEntityAction;
import scrum.client.project.Project;
import scrum.client.project.Requirement;
import scrum.client.task.BurnHours;
import scrum.client.task.TaskDaySnapshot;
import scrum.client.tasks.WhiteboardWidget;

import com.google.gwt.user.client.ui.Widget;

public class Task extends GTask implements ReferenceSupport, LabelSupport, ForumSupport {

	public static final int INIT_EFFORT = 1;
	public static final String REFERENCE_PREFIX = "tsk";

	public Sprint getSprint() {
		return getRequirement().getSprint();
	}

	public Task(Requirement requirement) {
		setRequirement(requirement);
		// setLabel("New Task");
		setRemainingWork(INIT_EFFORT);
	}

	public Task(Map data) {
		super(data);
	}

	public boolean isInCurrentSprint() {
		return getRequirement().isInCurrentSprint();
	}

	@Override
	public void updateLocalModificationTime() {
		super.updateLocalModificationTime();
		try {
			Requirement requirement = getRequirement();
			if (requirement != null) requirement.updateLocalModificationTime();
		} catch (EntityDoesNotExistException ex) {
			return;
		}
	}

	public boolean isBlocked() {
		if (!isImpedimentSet()) return false;
		return getImpediment().isOpen();
	}

	public void claim() {
		User user = Scope.get().getComponent(Auth.class).getUser();
		boolean ownerchange = !isOwner(user);
		if (isClosed()) {
			setUnDone(user);
		} else {
			setOwner(user);
		}
		setInitialWork(getRemainingWork());
	}

	public String getLongLabel(boolean showOwner, boolean showRequirement) {
		StringBuilder sb = new StringBuilder();
		sb.append(getLabel());
		if (showOwner && isOwnerSet()) {
			sb.append(" (").append(getOwnerName()).append(')');
		}
		if (showRequirement) {
			Requirement requirement = getRequirement();
			sb.append(" (").append(requirement.getReference()).append(" ").append(requirement.getLabel()).append(')');
		}
		return sb.toString();
	}

	@Override
	public String getReference() {
		return REFERENCE_PREFIX + getNumber();
	}

	public void setDone(User user) {
		if (user == null)
			throw new IllegalArgumentException("a Task cannot be set done without claiming Task ownership");
		setOwner(user);
		setRemainingWork(0);
	}

	public void setUnDone(User user) {
		setOwner(user);
		setRemainingWork(1);
		getRequirement().setClosed(false);
	}

	public void setUnOwned() {
		setOwner(null);
		getRequirement().setClosed(false);
	}

	public boolean isClosed() {
		return getRemainingWork() == 0;
	}

	public String getOwnerName() {
		return getOwner() == null ? "" : getOwner().getName();
	}

	public String getWorkText() {
		String work;
		int burned = getBurnedWork();
		if (isClosed()) {
			work = String.valueOf(burned);
		} else {
			int remaining = getRemainingWork();
			if (isOwnerSet()) {
				int total = remaining + burned;
				work = burned + " of " + total;
			} else {
				work = String.valueOf(remaining);
			}
		}
		return work + " hrs";
	}

	@Override
	public String toHtml() {
		return ScrumGwt.toHtml(this, getLabel());
	}

	@Override
	public String toString() {
		return getReference();
	}

	public void incrementBurnedWork() {
		setBurnedWork(getBurnedWork() + 1);
	}

	public void decrementBurnedWork() {
		if (getBurnedWork() == 0) return;
		setBurnedWork(getBurnedWork() - 1);
	}

	public void adjustRemainingWork(int burned) {
		int remaining = getRemainingWork();
		if (remaining == 0) return;
		remaining -= burned;
		if (remaining < 1) remaining = 1;
		setRemainingWork(remaining);
	}

	public void incrementRemainingWork() {
		setRemainingWork(getRemainingWork() + 1);
	}

	public void decrementRemainingWork() {
		int work = getRemainingWork();
		if (work <= 0) return;
		setRemainingWork(work - 1);
	}

	public static int sumBurnedWork(Iterable<Task> tasks) {
		int sum = 0;
		for (Task task : tasks) {
			sum += task.getBurnedWork();
		}
		return sum;
	}

	public static int sumRemainingWork(Iterable<Task> tasks) {
		int sum = 0;
		for (Task task : tasks) {
			sum += task.getRemainingWork();
		}
		return sum;
	}

	public Project getProject() {
		return getRequirement().getProject();
	}

	@Override
	public boolean isEditable() {
		if (getClosedInPastSprint() != null) return false;
		if (!isInCurrentSprint()) return false;
		if (!getProject().isTeamMember(Scope.get().getComponent(Auth.class).getUser())) return false;
		return true;
	}

	@Override
	public Widget createForumItemWidget() {
		return new HyperlinkWidget(new ShowEntityAction(WhiteboardWidget.class, this, getLabel()));
	}

	private transient AFieldModel<String> ownerModel;

	public AFieldModel<String> getOwnerModel() {
		if (ownerModel == null) ownerModel = new AFieldModel<String>() {

			@Override
			public String getValue() {
				return getOwnerName();
			}
		};
		return ownerModel;
	}

	private transient AFieldModel<String> workTextModel;

	public AFieldModel<String> getWorkTextModel() {
		if (workTextModel == null) workTextModel = new AFieldModel<String>() {

			@Override
			public String getValue() {
				return getWorkText();
			}
		};
		return workTextModel;
	}

	/**
	 * XXX duplicated UserBurndownCahrt.getUserBurnedHours
	 */
	public List<BurnHours> getTaskDaySnapshotsInSprint(Date date, Sprint sprint) {

		List<BurnHours> results = new ArrayList<BurnHours>();
		List<TaskDaySnapshot> taskDaySnapshots = getCurrentTaskDaySnapshots(sprint);

		int previousSnapshotBurn = 0;
		int burnedWork;
		BurnHours burnHours = null;
		for (TaskDaySnapshot snapshot : taskDaySnapshots) {
			burnHours = copySnapshot(snapshot);
			burnedWork = burnHours.getBurnedWork();
			if (previousSnapshotBurn != 0 && burnedWork > 0) {
				burnedWork -= previousSnapshotBurn;
				burnHours.setBurnedWork(burnedWork);
			}

			if ((date == null || date.equals(burnHours.getDate()))
					&& burnHours.getDate().isSameOrAfter(sprint.getBegin())) {
				results.add(burnHours);
			}
			previousSnapshotBurn = snapshot.getBurnedWork();
		}

		return results;
	}

	private BurnHours copySnapshot(TaskDaySnapshot snapshot) {
		BurnHours newSnapshot = new BurnHours();
		newSnapshot.setBurnedWork(snapshot.getBurnedWork());
		newSnapshot.setDate(snapshot.getDate());
		newSnapshot.setRemainingWork(snapshot.getRemainingWork());
		newSnapshot.setTask(snapshot.getTask());
		return newSnapshot;
	}

	public List<TaskDaySnapshot> getCurrentTaskDaySnapshots(Sprint sprint) {
		List<TaskDaySnapshot> currentTaskDaySnapshots = new ArrayList<TaskDaySnapshot>();
		for (TaskDaySnapshot t : this.getTaskDaySnapshots()) {
			if (sprint.getBegin().isSameOrBefore(t.getDate())) {
				currentTaskDaySnapshots.add(t);
			}
		}
		Collections.sort(currentTaskDaySnapshots, TaskDaySnapshot.DATE_COMPARATOR);
		return currentTaskDaySnapshots;
	}

	public boolean containsAndBurned(List<BurnHours> taskDaySnapshots) {
		boolean isInBurnList = false;
		for (BurnHours taskDaySnapshot : taskDaySnapshots) {
			int burnedWork = taskDaySnapshot.getBurnedWork();
			if (burnedWork > 0) {
				if (taskDaySnapshot.getTask().equals(this)) {
					isInBurnList = true;
				}
			}
		}
		return isInBurnList;
	}

	public Float getEfficiency() {
		if (getInitialWork() == 0 || getBurnedWork() == 0) return 0f;
		return Float.valueOf(getInitialWork()) / this.getBurnedWork();
	}

	public int getEfficiencyRate() {
		return (int) (getEfficiency() * 100);
	}

}
