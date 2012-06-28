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

import java.util.List;

import scrum.client.admin.User;
import scrum.client.common.AScrumWidget;
import scrum.client.project.Project;
import scrum.client.workspace.PagePanel;

public abstract class TeamBurnHoursWidget extends AScrumWidget {

	public PagePanel createBurnHoursPanel(Project project, Date date) {

		PagePanel burnHoursPanel = new PagePanel();
		StringBuffer header = new StringBuffer("Team burned hours at " + date);
		if (project.isFreeDay(date)) {
			header.append(" (free day)");
		}
		burnHoursPanel.addHeader(header.toString());

		User currentUser = getCurrentUser();
		List<User> team = project.getTeamStartWithCurrent(currentUser);
		for (User user : team) {
			burnHoursPanel.addSection(new BurnHoursWidget(date, user));
		}

		return burnHoursPanel;
	}

}
