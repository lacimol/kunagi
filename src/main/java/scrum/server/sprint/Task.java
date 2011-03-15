package scrum.server.sprint;

import ilarkesto.base.time.Date;

import java.util.List;

import scrum.server.admin.User;
import scrum.server.common.Numbered;
import scrum.server.project.Project;
import scrum.server.task.TaskDaySnapshot;

public class Task extends GTask implements Numbered {

	public String getReferenceAndLabel() {
		return getReference() + " " + getLabel();
	}

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

	public boolean isSprint(Sprint sprint) {
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

		if (!getRequirement().isInCurrentSprint()) {
			if (getRequirement().isClosed()) {
				getDao().deleteEntity(this);
			} else {
				reset();
			}
		}

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

}
