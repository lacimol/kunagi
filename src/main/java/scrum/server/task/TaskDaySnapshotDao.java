/*
 * Copyright 2011 Laszlo Molnar <lacimol@gmail.com>
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

package scrum.server.task;

import ilarkesto.core.time.Date;
import ilarkesto.fp.Predicate;

import java.util.LinkedList;
import java.util.List;

import scrum.server.sprint.Sprint;
import scrum.server.sprint.Task;

public class TaskDaySnapshotDao extends GTaskDaySnapshotDao {

	public TaskDaySnapshot getTaskDaySnapshot(final Task task, final Date date, boolean autoCreate) {
		TaskDaySnapshot snapshot = getEntity(new Predicate<TaskDaySnapshot>() {

			@Override
			public boolean test(TaskDaySnapshot e) {
				return e.isTask(task) && e.isDate(date);
			}
		});

		if (autoCreate && snapshot == null) {
			snapshot = newEntityInstance();
			snapshot.setTask(task);
			snapshot.setDate(date);
			saveEntity(snapshot);
		}

		return snapshot;
	}

	public List<TaskDaySnapshot> getTaskDaySnapshots(Sprint sprint, Task task) {
		List<TaskDaySnapshot> ret = new LinkedList<TaskDaySnapshot>();
		Date date = sprint.getBegin();
		Date end = sprint.getEnd();
		TaskDaySnapshot previousSnapshot = null;
		while (date.isBeforeOrSame(end) && date.isPastOrToday()) {
			TaskDaySnapshot snapshot = getTaskDaySnapshot(task, date, false);
			if (snapshot == null) {
				snapshot = new TaskDaySnapshot();
				snapshot.setTask(task);
				snapshot.setDate(date);
				if (previousSnapshot != null) {
					snapshot.setRemainingWork(previousSnapshot.getRemainingWork());
					snapshot.setBurnedWork(previousSnapshot.getBurnedWork());
				}
			}
			ret.add(snapshot);
			previousSnapshot = snapshot;
			date = date.nextDay();
		}
		return ret;
	}

}