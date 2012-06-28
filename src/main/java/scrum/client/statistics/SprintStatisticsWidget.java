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

import ilarkesto.gwt.client.Date;
import ilarkesto.gwt.client.TableBuilder;
import scrum.client.project.Project;
import scrum.client.workspace.PagePanel;

import com.google.gwt.user.client.ui.Widget;

public class SprintStatisticsWidget extends TeamBurnHoursWidget {

	@Override
	protected Widget onInitialization() {

		PagePanel efficiency = new PagePanel();
		efficiency.addHeader("Team efficiency");
		efficiency.addSection(new EfficiencyWidget());

		PagePanel accomplish = new PagePanel();
		accomplish.addHeader("Team accomplishment");
		accomplish.addSection(new AccomplishWidget());

		PagePanel storyTheme = new PagePanel();
		storyTheme.addHeader("Story themes");
		storyTheme.addSection(new StoryThemeWidget());

		// burned hours
		Project project = getCurrentProject();
		Date yesterday = project.getCurrentSprint().getLastWorkDay();
		PagePanel yesterdayTeamBurnHours = createBurnHoursPanel(project, yesterday);
		PagePanel todayTeamBurnHours = createBurnHoursPanel(project, Date.today());

		Widget left = TableBuilder.column(5, efficiency, accomplish, storyTheme);
		Widget right = TableBuilder.column(5, todayTeamBurnHours, yesterdayTeamBurnHours);

		return TableBuilder.row(5, left, right);
	}

}
