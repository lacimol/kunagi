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

import java.util.Comparator;
import java.util.Map;

public class TaskDaySnapshot extends GTaskDaySnapshot {

	public TaskDaySnapshot(Map data) {
		super(data);
	}

	public static final Comparator<TaskDaySnapshot> DATE_COMPARATOR = new Comparator<TaskDaySnapshot>() {

		@Override
		public int compare(TaskDaySnapshot a, TaskDaySnapshot b) {
			return a.getDate().compareTo(b.getDate());
		}
	};

	public static final Comparator<TaskDaySnapshot> REVERSE_DATE_COMPARATOR = new Comparator<TaskDaySnapshot>() {

		@Override
		public int compare(TaskDaySnapshot a, TaskDaySnapshot b) {
			return DATE_COMPARATOR.compare(b, a);
		}
	};

}