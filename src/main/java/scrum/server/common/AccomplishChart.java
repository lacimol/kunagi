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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;

import scrum.server.ScrumWebApplication;
import scrum.server.admin.User;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.Task;

public class AccomplishChart extends Chart {

	private static final String TEAM_AVG = TEAM + " avg";
	private Integer teamMembersCount;

	public byte[] createBurndownChartAsByteArray(Sprint sprint, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new AccomplishChart().writeSprintBurndownChart(out, sprint, width, height);
		return out.toByteArray();
	}

	public void writeChart(OutputStream out, String sprintId, int width, int height) {
		Sprint sprint = sprintDao.getById(sprintId);
		if (sprint == null) throw new IllegalArgumentException("Sprint " + sprintId + " does not exist.");
		teamMembersCount = sprint.getProject().getTeamMembersCount();
		writeSprintBurndownChart(out, sprint, width, height);
	}

	void writeSprintBurndownChart(OutputStream out, Sprint sprint, int width, int height) {

		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		barDataset.addValue(0, "S1", TEAM_AVG);

		Double burnedHours = null;
		for (User user : sprint.getProject().getTeamMembers()) {
			burnedHours = getUserBurnedHours(sprint, user.getName());
			if (burnedHours > 0) {
				barDataset.addValue(burnedHours, "S1", user.getName());
			} else {
				teamMembersCount--;
			}
		}
		burnedHours = getUserBurnedHours(sprint, TEAM);
		int teamAvg = (int) (burnedHours / teamMembersCount);
		barDataset.setValue(teamAvg, "S1", TEAM_AVG);

		JFreeChart chart = createBarChart(barDataset, sprint, new StandardCategoryItemLabelGenerator());
		int maxWorkHours = sprint.getLengthInWorkDays()
				* getWorkingHoursPerDay(ScrumWebApplication.get().getSystemConfig().getWorkingHoursPerDay());
		final IntervalMarker target = new IntervalMarker(teamAvg, maxWorkHours);
		target.setPaint(new Color(222, 222, 255, 128));
		chart.getCategoryPlot().addRangeMarker(target, Layer.BACKGROUND);
		double upperBoundary = Math.min(maxWorkHours * 1.1f, maxWorkHours + 3);
		chart.getCategoryPlot().getRangeAxis().setUpperBound(upperBoundary);
		try {
			ChartUtilities.writeScaledChartAsPNG(out, chart, width, height, 1, 1);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Double getUserBurnedHours(Sprint sprint, String userName) {

		Double allBurnedHours = 0.0;
		List<Task> sprintTasks = new LinkedList<Task>(sprint.getProject().getTasks());

		for (Task task : sprintTasks) {
			if (userName.equals(TEAM) || (task.getOwner() != null && userName.equals(task.getOwner().getName()))) {
				allBurnedHours += task.getBurnedWork();
			}
		}

		return allBurnedHours;
	}

}
