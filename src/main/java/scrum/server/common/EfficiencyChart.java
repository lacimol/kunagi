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

import ilarkesto.base.time.Date;
import ilarkesto.core.logging.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import scrum.server.admin.User;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.Task;

public class EfficiencyChart extends Chart {

	private static final Log LOG = Log.get(EfficiencyChart.class);

	public byte[] createBurndownChartAsByteArray(Sprint sprint, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new EfficiencyChart().writeSprintBurndownChart(out, sprint, width, height);
		return out.toByteArray();
	}

	public void writeChart(OutputStream out, String sprintId, int width, int height) {
		Sprint sprint = sprintDao.getById(sprintId);
		if (sprint == null) throw new IllegalArgumentException("Sprint " + sprintId + " does not exist.");
		writeSprintBurndownChart(out, sprint, width, height);
	}

	void writeSprintBurndownChart(OutputStream out, Sprint sprint, int width, int height) {

		Date firstDay = sprint.getBegin();
		Date lastDay = sprint.getEnd();

		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		UserEfficiency efficiency = getUserEfficiency(sprint, firstDay, lastDay, TEAM);
		barDataset.addValue(efficiency.getEfficiency(), "S1", TEAM + efficiency.getBurnedHoursPerInitial());

		for (User user : sprint.getProject().getTeamMembers()) {
			efficiency = getUserEfficiency(sprint, firstDay, lastDay, user.getName());
			if (efficiency.getEfficiency() > 0) {
				barDataset.addValue(efficiency.getEfficiency(), "S1", user.getName()
						+ efficiency.getBurnedHoursPerInitial());
			}
		}

		JFreeChart chart = createEfficiencyChart(barDataset, sprint);
		try {
			ChartUtilities.writeScaledChartAsPNG(out, chart, width, height, 1, 1);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private UserEfficiency getUserEfficiency(Sprint sprint, Date firstDay, Date lastDay, String userName) {

		UserEfficiency result = new UserEfficiency();
		Double efficiency = 0.0;
		Double allBurnedHours = 0.0;
		Double initialBurnableHours = 0.0;
		List<Task> sprintTasks = new LinkedList<Task>(sprint.getProject().getTasks());
		int initialWork = 0;

		for (Task task : sprintTasks) {
			if ((userName.equals(TEAM) || (task.getOwner() != null && userName.equals(task.getOwner().getName())))
					&& task.isClosed()) {
				allBurnedHours += task.getBurnedWork();
				initialWork = task.getInitialWork();
				initialBurnableHours += initialWork == 0 ? task.getBurnedWork() : initialWork;
			}
		}
		if (allBurnedHours > 0 && initialBurnableHours > 0) {
			efficiency = initialBurnableHours / allBurnedHours;
		}
		LOG.debug(userName + "'s UserEfficiency: " + efficiency + "(" + initialBurnableHours + "/" + allBurnedHours
				+ ")");
		result.setEfficiency(efficiency);
		result.setBurnedHoursPerInitial(" (" + initialBurnableHours.intValue() + "/" + allBurnedHours.intValue() + ")");
		return result;
	}

	class UserEfficiency {

		private Double efficiency;

		private String burnedHoursPerInitial;

		public Double getEfficiency() {
			return efficiency;
		}

		public void setEfficiency(Double efficiency) {
			this.efficiency = efficiency;
		}

		public String getBurnedHoursPerInitial() {
			return burnedHoursPerInitial;
		}

		public void setBurnedHoursPerInitial(String burnedHoursPerInitial) {
			this.burnedHoursPerInitial = burnedHoursPerInitial;
		}

	}
}
