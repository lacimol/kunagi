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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import scrum.client.ScrumGwt;
import scrum.client.admin.User;
import scrum.client.common.AScrumWidget;
import scrum.client.issues.Issue;
import scrum.client.project.Project;
import scrum.client.project.Requirement;
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

		List<User> lazyUsers = new ArrayList<User>();
		List<User> team = project.getTeamStartWithCurrent(user);
		for (User user : team) {
			List<Task> tasks = project.getUserTasks(user);
			List<Issue> issues = project.getUserBugs(user);

			// user has no task
			if (tasks.isEmpty() && issues.isEmpty()) {
				lazyUsers.add(user);
				continue;
			}

			if (this.user == null || this.user.equals(user)) {
				// collect
				List<BurnHours> taskDaySnapshots = getAllTaskSnapshotsByUser(user, tasks, issues);
				int burnedHours = getBurnedHours(taskDaySnapshots);
				List<Task> currentTasks = project.getClaimedTasks(user);

				// write
				boolean hasClaimedTaskToday = !currentTasks.isEmpty() && Date.today().equals(date);
				if (burnedHours > 0 || hasClaimedTaskToday) {
					String color = project.getUserConfig(user).getColor();
					String userName = user.getName().toUpperCase();
					sb.append("<div class='BurnHoursWidget-user'>");
					sb.append("<span style='color: ").append(color).append("; font-weight:bold;'>");
					sb.append(userName);
					sb.append("</span> has burned <span style='");
					if (burnedHours < MIN_BURN_HOUR_PER_DAY) {
						sb.append("color: red; ");
					}
					sb.append("font-weight:bold;'>");
					sb.append(burnedHours);
					sb.append("</span> hours on <ul>");

					for (BurnHours taskDaySnapshot : taskDaySnapshots) {
						int burnedWork = taskDaySnapshot.getBurnedWork();
						if (burnedWork > 0) {
							sb.append(createTaskListElement(taskDaySnapshot, burnedWork));
						}
					}

					// current work
					for (Task currentTask : currentTasks) {

						boolean isInBurnList = isInSnapshots(taskDaySnapshots, currentTask);
						if (!isInBurnList && Date.today().equals(date)) {
							sb.append("<li>").append(currentTask.toHtml()).append("</li>");
						}

					}

					sb.append("</ul></div>");
				} else {
					// user has no burn
					lazyUsers.add(user);
				}
			}
		}

		// for (User user : lazyUsers) {
		// if (this.user == null || this.user.equals(user)) {
		// String color = project.getUserConfig(user).getColor();
		// sb.append("<div class='BurnHoursWidget-user'>");
		// sb.append("<span style='color: ").append(color).append(";font-weight:bold;'>");
		// sb.append(user.getName().toUpperCase());
		// sb.append("</span> has burned <span style='color: red;'>nothing</span></div>");
		// }
		// }

		sb.append("</div>");
		html.setHTML(sb.toString());
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

	private boolean isInSnapshots(List<BurnHours> taskDaySnapshots, Task currentTask) {
		boolean isInBurnList = false;
		for (BurnHours taskDaySnapshot : taskDaySnapshots) {
			int burnedWork = taskDaySnapshot.getBurnedWork();
			if (burnedWork > 0) {
				if (taskDaySnapshot.getTask().equals(currentTask)) {
					isInBurnList = true;
				}
			}
		}
		return isInBurnList;
	}

	private List<BurnHours> getAllTaskSnapshotsByUser(User user, List<Task> tasks, List<Issue> issues) {
		List<BurnHours> taskDaySnapshots = new ArrayList<BurnHours>();

		for (Issue issue : issues) {
			for (Requirement req : issue.getRequirements()) {
				taskDaySnapshots.addAll(getTaskSnapshotsByUserAt(user, req));
			}
		}
		List<Requirement> requirements = new ArrayList<Requirement>(getRequirements(tasks));
		for (Requirement req : requirements) {
			taskDaySnapshots.addAll(getTaskSnapshotsByUserAt(user, req));
		}

		List<BurnHours> result = getBurnedSnapshots(taskDaySnapshots);

		Collections.sort(result, BurnHours.DATE_COMPARATOR);
		return result;
	}

	private int getBurnedHours(List<BurnHours> taskDaySnapshots) {
		int burnedHours = 0;
		for (BurnHours taskDaySnapshot : taskDaySnapshots) {
			burnedHours += taskDaySnapshot.getBurnedWork();
		}
		return burnedHours;
	}

	private List<BurnHours> getBurnedSnapshots(List<BurnHours> taskDaySnapshots) {

		List<BurnHours> result = new ArrayList<BurnHours>();
		for (BurnHours taskDaySnapshot : taskDaySnapshots) {
			result.add(taskDaySnapshot);
		}
		return result;
	}

	private List<BurnHours> getTaskSnapshotsByUserAt(User user, Requirement req) {

		List<BurnHours> results = new ArrayList<BurnHours>();
		List<Task> usersTasks = req.getUserTasks(user);
		for (Task task : usersTasks) {
			results.addAll(task.getTaskDaySnapshotsInSprint(date, getCurrentSprint()));
		}
		return results;
	}

	private Set<Requirement> getRequirements(List<Task> tasks) {
		Set<Requirement> reqs = new HashSet<Requirement>();
		for (Task task : tasks) {
			reqs.add(task.getRequirement());
		}
		return reqs;
	}

}
