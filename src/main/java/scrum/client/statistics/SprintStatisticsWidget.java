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

import java.util.LinkedList;
import java.util.List;

import scrum.client.admin.User;
import scrum.client.common.AScrumWidget;
import scrum.client.project.Project;
import scrum.client.workspace.PagePanel;

import com.google.gwt.user.client.ui.Widget;

public class SprintStatisticsWidget extends AScrumWidget {

	@Override
	protected Widget onInitialization() {

		PagePanel teamBurndown = new PagePanel();
		teamBurndown.addHeader("Team burndown");
		teamBurndown.addSection(new UserWorkWidget());

		PagePanel efficiency = new PagePanel();
		efficiency.addHeader("Team efficiency");
		efficiency.addSection(new EfficiencyWidget());

		PagePanel accomplish = new PagePanel();
		accomplish.addHeader("Team accomplishment");
		accomplish.addSection(new AccomplishWidget());

		// burned hours
		Project project = getCurrentProject();
		List<User> team = new LinkedList<User>(project.getTeamMembers());
		User currentUser = getCurrentUser();
		if (team.contains(currentUser)) {
			team.remove(currentUser);
			team.add(0, currentUser);
		}
		// yesterday
		Date yesterday = Date.today().prevDay();
		PagePanel yesterdayBurnHours = new PagePanel();
		yesterdayBurnHours.addHeader("Team burned hours at " + yesterday);
		Date today = Date.today();
		PagePanel todayBurnHours = new PagePanel();
		todayBurnHours.addHeader("Team burned hours at today");
		for (User user : team) {
			todayBurnHours.addSection(new BurnHoursWidget(today, user));
			yesterdayBurnHours.addSection(new BurnHoursWidget(yesterday, user));
		}

		Widget left = TableBuilder.column(5, teamBurndown, efficiency, accomplish);
		Widget right = TableBuilder.column(5, todayBurnHours, yesterdayBurnHours);

		return TableBuilder.row(5, left, right);
	}

}
