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

import ilarkesto.core.scope.Scope;
import ilarkesto.gwt.client.HyperlinkWidget;
import ilarkesto.gwt.client.SwitchingNavigatorWidget;
import ilarkesto.gwt.client.TableBuilder;

import java.util.List;

import scrum.client.admin.User;
import scrum.client.common.AScrumWidget;
import scrum.client.project.Project;
import scrum.client.workspace.PagePanel;
import scrum.client.workspace.ProjectWorkspaceWidgets;

import com.google.gwt.user.client.ui.Widget;

public class UserStatisticsWidget extends AScrumWidget {

	@Override
	protected Widget onInitialization() {
		ProjectWorkspaceWidgets widgets = Scope.get().getComponent(ProjectWorkspaceWidgets.class);

		SwitchingNavigatorWidget nav = widgets.getSidebar().getNavigator();

		PagePanel usersBurndown = new PagePanel();
		Project project = getCurrentProject();
		User currentUser = getCurrentUser();
		List<User> team = project.getTeamStartWithCurrent(currentUser);
		for (User user : team) {
			usersBurndown.addHeader(user.getName() + "'s burndown",
				new HyperlinkWidget(nav.createSwitchAction(widgets.getSprintBacklog())));
			usersBurndown.addSection(new UserWorkWidget(user.getName()));
		}

		PagePanel burnHours = new PagePanel();
		burnHours.addHeader("Team burned hours", new HyperlinkWidget(nav.createSwitchAction(widgets.getWhiteboard())),
			new HyperlinkWidget(nav.createSwitchAction(widgets.getIssueList())));
		burnHours.addSection(new BurnHoursWidget());

		Widget left = TableBuilder.column(5, usersBurndown);
		Widget right = TableBuilder.column(5, burnHours);

		return TableBuilder.row(5, left, right);
	}

}
