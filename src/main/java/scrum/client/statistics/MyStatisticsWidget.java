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
import ilarkesto.gwt.client.Date;
import ilarkesto.gwt.client.HyperlinkWidget;
import ilarkesto.gwt.client.SwitchingNavigatorWidget;
import ilarkesto.gwt.client.TableBuilder;
import scrum.client.admin.User;
import scrum.client.common.AScrumWidget;
import scrum.client.common.WeekdaySelector;
import scrum.client.workspace.PagePanel;
import scrum.client.workspace.ProjectWorkspaceWidgets;

import com.google.gwt.user.client.ui.Widget;

public class MyStatisticsWidget extends AScrumWidget {

	private static final int daysBefore = 7;

	@Override
	protected Widget onInitialization() {
		ProjectWorkspaceWidgets widgets = Scope.get().getComponent(ProjectWorkspaceWidgets.class);

		SwitchingNavigatorWidget nav = widgets.getSidebar().getNavigator();

		PagePanel teamBurndown = new PagePanel();
		teamBurndown.addHeader("Team burndown");
		teamBurndown.addSection(new UserWorkWidget());

		User currentUser = getCurrentUser();
		PagePanel currentUserBurndown = new PagePanel();
		currentUserBurndown.addHeader("My burndown",
			new HyperlinkWidget(nav.createSwitchAction(widgets.getSprintBacklog())));
		currentUserBurndown.addSection(new UserWorkWidget(currentUser.getName()));

		PagePanel todayBurnHours = new PagePanel();
		WeekdaySelector freeDays = getCurrentProject().getFreeDaysWeekdaySelectorModel().getValue();
		StringBuffer header = new StringBuffer("My burned hours at today");
		if (freeDays.isFree(Date.today().getWeekday() + 1)) {
			header.append(" (free day)");
		}
		todayBurnHours.addHeader(header.toString(),
			new HyperlinkWidget(nav.createSwitchAction(widgets.getWhiteboard())),
			new HyperlinkWidget(nav.createSwitchAction(widgets.getIssueList())));
		todayBurnHours.addSection(new BurnHoursWidget(Date.today(), currentUser));

		PagePanel beforeBurnHours = new PagePanel();
		Date dateBefore = null;
		for (int x = 1; x < daysBefore; x++) {
			dateBefore = Date.today().addDays(-x);
			if (dateBefore.isSameOrAfter(getCurrentSprint().getBegin())) {
				header = new StringBuffer("My burned hours at " + dateBefore);
				if (freeDays.isFree(dateBefore.getWeekday() + 1)) {
					header.append(" (free day)");
				}
				beforeBurnHours.addHeader(header.toString());
				beforeBurnHours.addSection(new BurnHoursWidget(dateBefore, currentUser));
			}
		}

		Widget left = TableBuilder.column(5, teamBurndown, currentUserBurndown);
		Widget right = TableBuilder.column(5, todayBurnHours, beforeBurnHours);

		return TableBuilder.row(5, left, right);
	}

}
