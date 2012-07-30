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

package scrum.client.task;

import ilarkesto.core.time.Date;

import java.util.Comparator;

import scrum.client.sprint.Task;

public class BurnHours {

	private Task task;
	private Date date;
	private int remainingWork;
	private int burnedWork;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getRemainingWork() {
		return remainingWork;
	}

	public void setRemainingWork(int remainingWork) {
		this.remainingWork = remainingWork;
	}

	public int getBurnedWork() {
		return burnedWork;
	}

	public void setBurnedWork(int burnedWork) {
		this.burnedWork = burnedWork;
	}

	public static final Comparator<BurnHours> DATE_COMPARATOR = new Comparator<BurnHours>() {

		@Override
		public int compare(BurnHours a, BurnHours b) {
			return a.getDate().compareTo(b.getDate());
		}
	};

	public static final Comparator<BurnHours> REVERSE_DATE_COMPARATOR = new Comparator<BurnHours>() {

		@Override
		public int compare(BurnHours a, BurnHours b) {
			return DATE_COMPARATOR.compare(b, a);
		}
	};
}
