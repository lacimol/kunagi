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

import ilarkesto.core.logging.Log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.jfree.chart.JFreeChart;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import scrum.server.project.Project;
import scrum.server.sprint.Sprint;

public class SprintRangeChart extends Chart {

	private static final Log LOG = Log.get(SprintRangeChart.class);
	private int maxFormerSprintNr = 10;
	private boolean includeCurrentSprint;
	private boolean includeNextSprint;

	public SprintRangeChart() {}

	public SprintRangeChart(boolean includeCurrentSprint, boolean includeNextSprint) {
		this.includeCurrentSprint = includeCurrentSprint;
		this.includeNextSprint = includeNextSprint;
		maxFormerSprintNr = 1;
	}

	public static byte[] createBurndownChartAsByteArray(Sprint sprint, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new SprintRangeChart().writeChart(out, sprint, width, height);
		return out.toByteArray();
	}

	public void writeChart(OutputStream out, String sprintId, int width, int height) {
		Sprint sprint = sprintDao.getById(sprintId);
		if (sprint == null) throw new IllegalArgumentException("Sprint " + sprintId + " does not exist.");
		writeChart(out, sprint, width, height);
	}

	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		Project project = sprint.getProject();
		TaskSeriesCollection dataset = new TaskSeriesCollection();
		final TaskSeries s1 = new TaskSeries("S1");
		dataset.add(s1);

		int currentSprintColumn = 0;
		if (maxFormerSprintNr > 1) {
			int count = 0;
			for (Sprint formerSprint : project.getReverseFormerSprints()) {
				if (formerSprint.getLengthInDays() > 0) {
					if (sprint.getId().equals(formerSprint.getId())) {
						currentSprintColumn = s1.getItemCount();
					}
					s1.add(getGanttTask(formerSprint, formerSprint.getLabel()));
					if (++count >= maxFormerSprintNr) break;
				}
			}
		} else {
			Sprint prevSprint = project.getPrevSprint();
			if (prevSprint != null) {
				s1.add(getGanttTask(prevSprint, prevSprint.getLabel() + " (previous)"));
			}
		}

		if (includeCurrentSprint) {
			String description = sprint.getLabel();
			if (s1.get(description) == null) {
				currentSprintColumn = s1.getItemCount();
				s1.add(getGanttTask(sprint, description + " (current)"));
			}
		}
		if (includeNextSprint) {
			if (project.getNextSprint() != null) {
				s1.add(getGanttTask(project.getNextSprint(), project.getNextSprint().getLabel() + " (next)"));
			}
		}

		final JFreeChart chart = createGanttChart(dataset, currentSprintColumn);
		setDateAxis(s1, chart);
		createPic(out, width, height, chart);
	}

}
