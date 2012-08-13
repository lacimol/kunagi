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

import java.util.List;

import scrum.client.ScrumGwt;
import scrum.client.admin.User;
import scrum.client.common.AScrumWidget;
import scrum.client.project.Project;
import scrum.client.sprint.Sprint;
import scrum.client.sprint.Task;
import scrum.client.task.BurnHours;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class BurnHoursWidget extends AScrumWidget {

	private static final int MIN_BURN_HOUR_PER_DAY = 6;

	private HTML html;

	@Override
	protected Widget onInitialization() {
		html = new HTML();
		return html;
	}

	private Date date;

	private User user;

	private boolean useDateAtView;

	/**
	 * All users, every day
	 */
	public BurnHoursWidget() {
		this.useDateAtView = true;
	}

	public BurnHoursWidget(Date date, User user) {
		this.date = date;
		this.user = user;
		this.useDateAtView = false;
	}

	@Override
	protected void onUpdate() {
		Project project = getCurrentProject();
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='BurnHoursWidget'>");

		Sprint currentSprint = project.getCurrentSprint();
		List<User> team = project.getTeamStartWithCurrent(user);
		for (User user : team) {

			List<Task> tasks = currentSprint.getTasks(user);
			// user has no task
			if (tasks.isEmpty()) {
				continue;
			}

			if (this.user == null || this.user.equals(user)) {
				// collect
				List<BurnHours> taskDaySnapshots = currentSprint.getAllSortedTaskSnapshots(tasks, date);
				int burnedHours = currentSprint.getBurnedHours(taskDaySnapshots);
				List<Task> currentTasks = currentSprint.getClaimedTasks(user);

				// write
				boolean hasClaimedTaskToday = !currentTasks.isEmpty() && Date.today().equals(date);
				if (burnedHours > 0 || hasClaimedTaskToday) {

					sb.append(createUserBurnInfo(user, burnedHours));

					for (BurnHours taskDaySnapshot : taskDaySnapshots) {
						int burnedWork = taskDaySnapshot.getBurnedWork();
						if (burnedWork > 0) {
							sb.append(createTaskListElement(taskDaySnapshot, burnedWork));
						}
					}

					if (hasClaimedTaskToday) {
						// current work
						for (Task currentTask : currentTasks) {

							boolean isInBurnList = currentTask.containsAndBurned(taskDaySnapshots);
							if (!isInBurnList && Date.today().equals(date)) {
								sb.append("<li>").append(currentTask.toHtml()).append("</li>");
							}

						}
					}

					sb.append("</ul></div>");
				}
			}
		}

		sb.append("</div>");
		html.setHTML(sb.toString());
	}

	private String createUserBurnInfo(User user, int burnedHours) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='BurnHoursWidget-user'>");
		sb.append("<span style='color: ").append(user.getColor()).append("; font-weight:bold;'>");
		sb.append(user.getName().toUpperCase());
		sb.append("</span> has burned <span style='");
		if (burnedHours < MIN_BURN_HOUR_PER_DAY) {
			sb.append("color: red; ");
		}
		sb.append("font-weight:bold;'>");
		sb.append(burnedHours);
		sb.append("</span> hours on <ul>");
		return sb.toString();
	}

	private String createTaskListElement(BurnHours taskDaySnapshot, int burnedWork) {

		StringBuilder sb = new StringBuilder();
		boolean remained = taskDaySnapshot.getRemainingWork() > 0;
		Task task = taskDaySnapshot.getTask();
		String dateStr = useDateAtView ? (taskDaySnapshot.getDate() + ", ") : "";
		String remainedStr = remained ? ", remained " + taskDaySnapshot.getRemainingWork() + " hours" : "";
		String requirement = ScrumGwt.createHtmlReference(task.getRequirement());
		sb.append(remained ? "<li style='font-weight: bold;'>" : "<li>").append(dateStr).append(task.toHtml())
				.append(" (").append(requirement).append("), burned: ").append(burnedWork).append(remainedStr)
				.append("</li>");
		return sb.toString();
	}

}
