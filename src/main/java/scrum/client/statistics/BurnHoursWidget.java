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
import java.util.LinkedList;
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

	private HTML html;

	@Override
	protected Widget onInitialization() {
		html = new HTML();
		return html;
	}

	private Date date;

	private User user;

	/**
	 * All users, every day
	 */
	public BurnHoursWidget() {}

	public BurnHoursWidget(Date date, User user) {
		this.date = date;
		this.user = user;
	}

	@Override
	protected void onUpdate() {
		Project project = getCurrentProject();
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='BurnHoursWidget'>");

		List<User> lazyUsers = new ArrayList<User>();
		List<User> team = new LinkedList<User>(project.getTeamMembers());
		if (user != null && team.contains(user)) {
			team.remove(user);
			team.add(0, user);
		}
		for (User user : team) {
			List<Task> tasks = project.getUserTasks(user);
			List<Issue> issues = project.getUserBugs(user);

			if (tasks.isEmpty() && issues.isEmpty()) {
				lazyUsers.add(user);
				continue;
			}

			if (this.user == null || this.user.equals(user)) {
				// collect
				List<BurnHours> taskDaySnapshots = getAllTaskSnapshotsByUser(user, tasks, issues);
				int burnedHours = getBurnedHours(taskDaySnapshots);
				// write
				sb.append("<div class='BurnHoursWidget-user'>");
				sb.append("<span style='color: ").append(project.getUserConfig(user).getColor()).append(";'>");
				sb.append(user.getName());
				if (burnedHours > 0) {
					sb.append("</span> has burned " + burnedHours + " hours on ");
					sb.append("<ul>");

					for (BurnHours taskDaySnapshot : taskDaySnapshots) {
						if (taskDaySnapshot.getBurnedWork() > 0) {
							boolean remained = taskDaySnapshot.getRemainingWork() > 0;
							Task task = taskDaySnapshot.getTask();
							sb.append(remained ? "<li style='font-weight: bold;'>" : "<li>")
									.append(taskDaySnapshot.getDate() + ", ").append(task.toHtml()).append(" (")
									.append(ScrumGwt.createHtmlReference(task.getRequirement())).append("), burned: ")
									.append(taskDaySnapshot.getBurnedWork())
									.append(", remained " + taskDaySnapshot.getRemainingWork() + " hours</li>");
						}
					}

					sb.append("</ul></div>");
				} else {
					sb.append("</span> has burned <span style='color: red;'>nothing</span>");
				}
			}
		}

		for (User user : lazyUsers) {
			if (this.user == null || this.user.equals(user)) {
				sb.append("<div class='BurnHoursWidget-user'>");
				sb.append("<span style='color: ").append(project.getUserConfig(user).getColor()).append(";'>");
				sb.append(user.getName());
				sb.append("</span> has burned <span style='color: red;'>nothing</span></div>");
			}
		}

		sb.append("</div>");
		html.setHTML(sb.toString());
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
		Collections.sort(taskDaySnapshots, BurnHours.DATE_COMPARATOR);
		return taskDaySnapshots;
	}

	private int getBurnedHours(List<BurnHours> taskDaySnapshots) {
		int burnedHours = 0;
		for (BurnHours taskDaySnapshot : taskDaySnapshots) {
			burnedHours += taskDaySnapshot.getBurnedWork();
		}
		return burnedHours;
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
