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
import org.jfree.data.general.DefaultPieDataset;

import scrum.server.admin.User;
import scrum.server.project.Project;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.UserEfficiency;

public class TeamMemberBurnPieChart extends Chart {

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		DefaultPieDataset dataset = new DefaultPieDataset();

		Project project = sprint.getProject();
		int allInitialHours = 0;
		for (User user : project.getTeamMembers()) {
			allInitialHours += project.getUserEfficiency(user.getName()).getInitialBurnableHours();
		}

		UserEfficiency efficiency = null;
		for (User user : project.getTeamMembers()) {
			efficiency = project.getUserEfficiency(user.getName());
			int percent = (int) (((double) efficiency.getInitialBurnableHours() / (double) allInitialHours) * 100.0);
			dataset.setValue(user.getName() + " (" + efficiency.getInitialBurnableHours() + ")", percent);
		}

		final JFreeChart chart = createPercentPieChart(dataset);
		createPic(out, width, height, chart);
	}

}
