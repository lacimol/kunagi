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

		PagePanel sprintWorkText = new PagePanel();
		sprintWorkText.addHeader("Sprint details");
		sprintWorkText.addSection(new SprintWorkTextWidget());

		// TODO more stats
		// Widget upper = TableBuilder.column(5, sprintRange, currentSprintRange);
		// Widget left = TableBuilder.column(5, velocity);
		// Widget right = TableBuilder.column(5, sprintWork);
		// Widget lower = TableBuilder.row(5, left, right);
		//
		// return TableBuilder.column(5, upper, lower);

		Widget left = TableBuilder.column(5, sprintRange, currentSprintRange, velocity, sprintWork);
		Widget right = TableBuilder.column(5, sprintWorkText);

		return TableBuilder.row(5, left, right);
	}

}
