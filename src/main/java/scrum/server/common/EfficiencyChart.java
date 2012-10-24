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

import scrum.server.admin.User;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.UserEfficiency;

public class EfficiencyChart extends Chart {

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		UserEfficiency efficiency = sprint.getTeamEfficiency();
		barDataset.addValue(efficiency.getEfficiency(), "S1", Sprint.TEAM + efficiency.getBurnedHoursPerInitial());

		for (User user : sprint.getProject().getTeamMembers()) {
			efficiency = sprint.getUserEfficiency(user.getName());
			barDataset.addValue(efficiency.getEfficiency(), "S1",
				user.getName() + efficiency.getBurnedHoursPerInitial());
		}

		JFreeChart chart = createEfficiencyChart(barDataset, sprint);
		createPic(out, width, height, chart);
	}

}
