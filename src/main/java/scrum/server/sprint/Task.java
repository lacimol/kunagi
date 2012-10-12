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
package scrum.server.sprint;

import ilarkesto.core.time.Date;

import java.util.List;

import scrum.client.common.LabelSupport;
import scrum.client.common.ReferenceSupport;
import scrum.server.admin.User;
import scrum.server.common.Numbered;
import scrum.server.project.Project;
import scrum.server.task.TaskDaySnapshot;

public class Task extends GTask implements Numbered, ReferenceSupport, LabelSupport {

	public String getReferenceAndLabel() {
		return getReference() + " " + getLabel();
	}

	@Override
	public String getReference() {
		return scrum.client.sprint.Task.REFERENCE_PREFIX + getNumber();
	}

	@Override
	public void updateNumber() {
		if (getNumber() == 0) setNumber(getRequirement().getProject().generateTaskNumber());
	}

	public boolean isProject(Project project) {
		return getRequirement().isProject(project);
	}

	public boolean isClosed() {
		return getRemainingWork() == 0;
	}

	public String getOwnerName() {
		return getOwner() == null ? "Not claimed yet" : getOwner().getName();
	}

	public String getFullLabel() {
		return this.getLabel() + getWorkLabel();
	}

	public String getWorkLabel() {
		return "Owner: " + this.getOwnerName() + ", estimated: " + getInitialWork() + " hrs, burned: "
				+ this.getBurnedWork() + " hrs, efficiency: " + (int) (getEfficiency() * 100) + "%";
	}

	public Float getEfficiency() {
		if (getInitialWork() == 0 || getBurnedWork() == 0) return 0f;
		return Float.valueOf(getInitialWork()) / this.getBurnedWork();
	}

	public int getEfficiencyRate() {
		return (int) (getEfficiency() * 100);
	}

	public boolean isSprint(Sprint sprint) {
		if (isClosedInPastSprintSet()) return false;
		return getRequirement().isSprint(sprint);
	}

	public void reset() {
		setOwner(null);
		setBurnedWork(0);
	}

	@Override
	public void ensureIntegrity() {
		super.ensureIntegrity();
		updateNumber();
	}

	public Project getProject() {
		return getRequirement().getProject();
	}

	@Override
	public boolean isVisibleFor(User user) {
		return getProject().isVisibleFor(user);
	}

	@Override
	public String toString() {
		return getReferenceAndLabel();
	}

	public TaskDaySnapshot getDaySnapshot(Date date) {
		return taskDaySnapshotDao.getTaskDaySnapshot(this, date, true);
	}

	public List<TaskDaySnapshot> getTaskDaySnapshots(Sprint sprint) {
		return taskDaySnapshotDao.getTaskDaySnapshots(sprint, this);
	}

	public Date getBurnBegin(Sprint sprint) {
		Date begin = null;
		for (TaskDaySnapshot shot : getTaskDaySnapshots(sprint)) {
			if (shot.getBurnedWork() > 0 && (begin == null || begin.isBefore(shot.getDate()))) {
				begin = shot.getDate();
				break;
			}
		}
		return begin;
	}

	public Date getBurnEnd(Sprint sprint) {
		Date end = sprint.getEnd();
		int formerBurned = 0;
		for (TaskDaySnapshot shot : getTaskDaySnapshots(sprint)) {
			if (shot.getBurnedWork() > 0 && shot.getBurnedWork() > formerBurned) {
				formerBurned = shot.getBurnedWork();
				end = shot.getDate();
			}
		}
		return end;
	}

	public boolean isOwnersTask(String userName) {
		return (getOwner() != null && userName.equals(getOwner().getName()));
	}

	public void addBurn(int burned) {
		setBurnedWork(getBurnedWork() + burned);
	}

}
