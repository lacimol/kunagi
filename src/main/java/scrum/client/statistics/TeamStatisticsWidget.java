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

import ilarkesto.core.time.Date;
import ilarkesto.gwt.client.TableBuilder;
import scrum.client.project.Project;
import scrum.client.workspace.PagePanel;

import com.google.gwt.user.client.ui.Widget;

public class TeamStatisticsWidget extends TeamBurnHoursWidget {

	@Override
	protected Widget onInitialization() {

		PagePanel teamBurndown = new PagePanel();
		teamBurndown.addHeader("Team burndown");
		teamBurndown.addSection(new FullUserWorkWidget());

		PagePanel arrears = new PagePanel();
		arrears.addHeader("Team burn percent and arrears");
		arrears.addSection(new ArrearsWidget());

		Project project = getCurrentProject();
		// burned hours
		Date yesterday = project.getCurrentSprint().getLastWorkDay();
		PagePanel yesterdayTeamBurnHours = createBurnHoursPanel(project, yesterday);
		PagePanel todayTeamBurnHours = createBurnHoursPanel(project, Date.today());

		Widget middle = TableBuilder.column(5, teamBurndown, arrears, yesterdayTeamBurnHours, todayTeamBurnHours);

		return TableBuilder.row(5, middle);
	}

}
