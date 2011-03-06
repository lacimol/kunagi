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
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.DefaultCategoryDataset;

import scrum.server.ScrumWebApplication;
import scrum.server.admin.User;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.Task;

public class AccomplishChart extends Chart {

	private static final Log LOG = Log.get(AccomplishChart.class);
	private static final String TEAM_AVG = TEAM + " avg";

	public static byte[] createBurndownChartAsByteArray(Sprint sprint, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new AccomplishChart().writeChart(out, sprint, width, height);
		return out.toByteArray();
	}

	public void writeChart(OutputStream out, String sprintId, int width, int height) {
		Sprint sprint = sprintDao.getById(sprintId);
		if (sprint == null) throw new IllegalArgumentException("Sprint " + sprintId + " does not exist.");
		writeChart(out, sprint, width, height);
	}

	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		barDataset.addValue(0, "S1", TEAM_AVG);

		Double burnedHours = 0.0;
		Integer teamMembersCount = sprint.getProject().getTeamMembersCount();
		for (User user : sprint.getProject().getTeamMembers()) {
			burnedHours = getUserBurnedHours(sprint, user.getName());
			LOG.info(user, "'s burnedHours: " + burnedHours);
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
		setChartMarker(chart, teamAvg, maxWorkHours);
		setUpperBoundary(chart, maxWorkHours);
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
