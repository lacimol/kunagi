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
import scrum.server.admin.SystemConfig;
import scrum.server.admin.User;
import scrum.server.sprint.Sprint;

public class ArrearsChart extends Chart {

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();

		SystemConfig systemConfig = ScrumWebApplication.get().getSystemConfig();
		int maxWorkHours = sprint.getLengthInWorkDays() * systemConfig.getWorkingHoursPerDay();
		Double burnedHours = sprint.getUserBurnedHours(Sprint.TEAM);
		int teamMemberSize = sprint.getProject().getTeamMembers().size();
		int teamMaxWorkHours = maxWorkHours * teamMemberSize;
		int arrears = teamMaxWorkHours - burnedHours.intValue();
		barDataset.setValue(burnedHours / teamMaxWorkHours, "Burned", Sprint.TEAM + " (" + arrears + " hrs)");

		for (User user : sprint.getProject().getTeamMembers()) {
			String userName = user.getName();
			burnedHours = sprint.getUserBurnedHours(userName);
			arrears = maxWorkHours - burnedHours.intValue();
			barDataset.addValue(burnedHours / maxWorkHours, "Burned", userName + " (" + arrears + " hrs)");
		}

		JFreeChart chart = createEfficiencyChart(barDataset, sprint);

		setChartMarker(chart, 0.85, 1.15);
		setUpperBoundary(chart, 1.0);
		createPic(out, width, height, chart);
	}

}
