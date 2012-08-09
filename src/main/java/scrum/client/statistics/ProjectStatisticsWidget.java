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

package scrum.client.statistics;

import ilarkesto.gwt.client.TableBuilder;
import scrum.client.common.AScrumWidget;
import scrum.client.workspace.PagePanel;

import com.google.gwt.user.client.ui.Widget;

public class ProjectStatisticsWidget extends AScrumWidget {

	@Override
	protected Widget onInitialization() {

		PagePanel velocity = new PagePanel();
		velocity.addHeader("Velocity history");
		velocity.addSection(new VelocityWidget());

		PagePanel sprintWork = new PagePanel();
		sprintWork.addHeader("Sprint work hours history");
		sprintWork.addSection(new SprintWorkWidget());

		PagePanel sprintRange = new PagePanel();
		sprintRange.addHeader("Sprints long-range history");
		sprintRange.addSection(new SprintRangeWidget());

		PagePanel currentSprintRange = new PagePanel();
		currentSprintRange.addHeader("Current sprint range");
		currentSprintRange.addSection(new CurrentSprintRangeWidget());

		PagePanel projectEfficiency = new PagePanel();
		projectEfficiency.addHeader("Project team member's avg efficiency");
		projectEfficiency.addSection(new ProjectEfficiencyWidget());

		PagePanel sprintDetails = new PagePanel();
		sprintDetails.addHeader("Sprint details");
		sprintDetails.addSection(new SprintWorkTextWidget());

		Widget left = TableBuilder.column(5, sprintRange, currentSprintRange, velocity, projectEfficiency, sprintWork);
		Widget right = TableBuilder.column(5, sprintDetails);

		return TableBuilder.row(5, left, right);
	}

}
