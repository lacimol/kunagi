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
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import scrum.server.ScrumWebApplication;
import scrum.server.admin.User;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.Task;

public class AccomplishChart extends Chart {

	private static final Log LOG = Log.get(AccomplishChart.class);
	private static final String TEAM_AVG = Sprint.TEAM + " avg";

	public static byte[] createBurndownChartAsByteArray(Sprint sprint, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new AccomplishChart().writeChart(out, sprint, width, height);
		return out.toByteArray();
	}

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		barDataset.addValue(0, "Burned", TEAM_AVG);

		Double burnedHours = 0.0;
		Double remainingHours = 0.0;
		Integer teamMembersCount = sprint.getProject().getTeamMembersCount();
		for (User user : sprint.getProject().getTeamMembers()) {
			burnedHours = getUserBurnedHours(sprint, user.getName());
			remainingHours = getUserRemainingHours(sprint, user.getName());
			LOG.debug(user, "'s burnedHours: ", burnedHours, ", remining: ", remainingHours);
			barDataset.addValue(burnedHours, "Burned", user.getName());
			barDataset.addValue(remainingHours, "Remaining", user.getName());
		}
		burnedHours = getUserBurnedHours(sprint, Sprint.TEAM);
		remainingHours = getUserRemainingHours(sprint, Sprint.TEAM);
		int teamAvg = (int) (burnedHours / teamMembersCount);
		barDataset.setValue(teamAvg, "Burned", TEAM_AVG);
		barDataset.setValue((int) (remainingHours / teamMembersCount), "Remaining", TEAM_AVG);

		JFreeChart chart = createStackedBarChart(barDataset);
		int maxWorkHours = sprint.getLengthInWorkDays()
				* ScrumWebApplication.get().getSystemConfig().getWorkingHoursPerDay();
		setChartMarker(chart, teamAvg, maxWorkHours);
		setUpperBoundary(chart, Math.min(maxWorkHours + 5, (int) (burnedHours + remainingHours)));
		createPic(out, width, height, chart);
	}

	private Double getUserBurnedHours(Sprint sprint, String userName) {

		Double allBurnedHours = 0.0;
		// List<Task> sprintTasks = new LinkedList<Task>(sprint.getProject().getTasks());
		List<Task> sprintTasks = new LinkedList<Task>(sprint.getTasks());

		for (Task task : sprintTasks) {
			if (userName.equals(Sprint.TEAM) || (task.getOwner() != null && userName.equals(task.getOwner().getName()))) {
				allBurnedHours += task.getBurnedWork();
			}
		}

		return allBurnedHours;
	}

	private Double getUserRemainingHours(Sprint sprint, String userName) {

		Double allRemainingHours = 0.0;
		List<Task> sprintTasks = new LinkedList<Task>(sprint.getProject().getTasks());

		for (Task task : sprintTasks) {
			if (task.getOwner() != null && (userName.equals(Sprint.TEAM) || userName.equals(task.getOwner().getName()))) {
				allRemainingHours += task.getRemainingWork();
			}
		}

		return allRemainingHours;
	}

}
