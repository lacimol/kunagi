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

package scrum.server.common;

import ilarkesto.core.time.Date;

import java.io.OutputStream;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import scrum.server.sprint.Sprint;
import scrum.server.sprint.Task;

public class TaskRangeChart extends Chart {

	private int maxTaskNr = 35;

	public TaskRangeChart() {}

	public TaskRangeChart(int maxTaskNr) {
		this.maxTaskNr = maxTaskNr;
	}

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		TaskSeriesCollection dataset = new TaskSeriesCollection();
		final TaskSeries s1 = new TaskSeries("S1");
		dataset.add(s1);

		int count = 0;
		Date begin;
		Date end;
		Set<Task> tasks = sprint.getTasks();
		for (Task task : tasks) {
			begin = task.getBurnBegin(sprint);
			end = task.getBurnEnd(sprint);
			if (task.getOwner() != null && task.getBurnedWork() > 0) {
				s1.add(getGanttTask(task, begin, end));
				if (++count >= maxTaskNr) break;
			}
		}

		final JFreeChart chart = createGanttChart(dataset, -1);
		setDayDateAxis(s1, chart, sprint);
		createPic(out, width, Math.min(count * 30, height), chart);
	}
}
