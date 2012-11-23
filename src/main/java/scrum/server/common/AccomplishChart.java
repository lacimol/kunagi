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

import java.io.OutputStream;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import scrum.server.ScrumWebApplication;
import scrum.server.admin.User;
import scrum.server.sprint.Sprint;

public class AccomplishChart extends Chart {

	private static final String TEAM_AVG = Sprint.TEAM + " avg";

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		barDataset.addValue(0, "Burned", TEAM_AVG);

		Double burnedHours = 0.0;
		Double remainingHours = 0.0;

		for (User user : sprint.getProject().getTeamMembers()) {
			String userName = user.getName();
			burnedHours = sprint.getUserBurnedHours(userName);
			remainingHours = sprint.getUserRemainingHours(userName);
			LOG.debug(user, "'s burnedHours: ", burnedHours, ", remining: ", remainingHours);
			barDataset.addValue(burnedHours, "Burned", userName);
			barDataset.addValue(remainingHours, "Remaining", userName);
		}
		burnedHours = sprint.getUserBurnedHours(Sprint.TEAM);
		remainingHours = sprint.getUserRemainingHours(Sprint.TEAM);

		Integer teamMembersCount = sprint.getProject().getTeamMembersCount();
		Double teamAvg = burnedHours / teamMembersCount;
		barDataset.setValue(teamAvg.intValue(), "Burned", TEAM_AVG);
		barDataset.setValue((int) (remainingHours / teamMembersCount), "Remaining", TEAM_AVG);

		JFreeChart chart = createStackedBarChart(barDataset);
		Double maxWorkHours = (double) sprint.getLengthInWorkDays()
				* ScrumWebApplication.get().getSystemConfig().getWorkingHoursPerDay();
		setChartMarker(chart, teamAvg, maxWorkHours);
		setUpperBoundary(chart, Math.min(maxWorkHours + 5, (burnedHours + remainingHours)));
		createPic(out, width, height, chart);
	}

}
