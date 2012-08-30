/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
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
package scrum.server.sprint;

import ilarkesto.base.Str;
import ilarkesto.base.Utl;
import ilarkesto.base.time.Date;
import ilarkesto.core.logging.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import scrum.client.common.WeekdaySelector;
import scrum.client.journal.Change;
import scrum.server.ScrumWebApplication;
import scrum.server.admin.User;
import scrum.server.common.Numbered;
import scrum.server.issues.Issue;
import scrum.server.journal.ChangeDao;
import scrum.server.project.Project;
import scrum.server.project.Requirement;
import scrum.server.release.Release;
import scrum.server.task.TaskDaySnapshot;

public class Sprint extends GSprint implements Numbered {

	private static final Log log = Log.get(Sprint.class);

	public static final String TEAM = "team";

	// --- dependencies ---

	private static transient ChangeDao changeDao;

	public static void setChangeDao(ChangeDao changeDao) {
		Sprint.changeDao = changeDao;
	}

	// --- ---

	public List<Requirement> getClosedRequirementsAsList() {
		return Utl.sort(getClosedRequirements(), getRequirementsOrderComparator());
	}

	public Set<Requirement> getClosedRequirements() {
		Set<Requirement> requirements = getRequirements();
		Iterator<Requirement> iterator = requirements.iterator();
		while (iterator.hasNext()) {
			Requirement requirement = iterator.next();
			if (!requirement.isClosed()) iterator.remove();
		}
		return requirements;
	}

	public Release getNextRelease() {
		List<Release> releases = new ArrayList<Release>(getReleases());
		Collections.sort(releases, Release.DATE_REVERSE_COMPARATOR);
		return releases.isEmpty() ? null : Utl.getElement(releases, 0);
	}

	public void pullRequirement(Requirement requirement, User user) {
		requirement.setSprint(this);
		for (Task task : requirement.getTasksInSprint()) {
			task.reset();
		}
		moveToBottom(requirement);
		getDaySnapshot(Date.today()).updateWithCurrentSprint();

		changeDao.postChange(requirement, user, "sprintId", null, getId());
	}

	public void kickRequirement(Requirement requirement, User user) {
		int burned = 0;
		for (Task task : requirement.getTasksInSprint()) {
			burned += task.getBurnedWork();
			task.reset();
		}

		requirement.setClosed(false);
		requirement.setSprint(null);
		requirement.setDirty(burned > 0);
		requirement.getProject().moveRequirementToTop(requirement);
		SprintDaySnapshot daySnapshot = getDaySnapshot(Date.today());
		daySnapshot.addBurnedWorkFromDeleted(burned);
		daySnapshot.updateWithCurrentSprint();

		changeDao.postChange(requirement, user, "sprintId", getId(), null);
	}

	public void moveToBottom(Requirement requirement) {
		List<String> orderIds = getRequirementsOrderIds();
		orderIds.remove(requirement.getId());
		orderIds.add(requirement.getId());
		setRequirementsOrderIds(orderIds);
	}

	public void close() {

		Project project = getProject();
		SprintReport report = sprintReportDao.postSprintReport(this);
		report.setRequirementsOrderIds(getRequirementsOrderIds());
		report.setBurnedWork(getBurnedWork());

		float velocity = 0;
		StringBuilder releaseNotes = new StringBuilder();
		releaseNotes.append("'''Stories from ").append(getReferenceAndLabel()).append("'''\n\n");
		Collection<Requirement> completedRequirements = new ArrayList<Requirement>();
		Collection<Requirement> incompletedRequirements = new ArrayList<Requirement>();
		Collection<TeamMemberSnapshot> teamMemberStatistics = new ArrayList<TeamMemberSnapshot>();

		List<Requirement> requirements = new ArrayList<Requirement>(getRequirements());
		Collections.sort(requirements, getRequirementsOrderComparator());

		for (Requirement requirement : requirements) {
			releaseNotes.append("* " + (requirement.isClosed() ? "" : "(UNFINISHED) "))
					.append(requirement.getReferenceAndLabel()).append("\n");
			for (Task task : requirement.getTasksInSprint()) {
				if (task.isClosed()) {
					report.addClosedTask(task);
				} else {
					report.addOpenTask(task);
				}
			}
			if (requirement.isClosed()) {
				completedRequirements.add(requirement);
				changeDao
						.postChange(requirement, null, Change.REQ_COMPLETED_IN_SPRINT, requirement.getLabel(), getId());
			} else {
				project.addRequirementsOrderId(incompletedRequirements.size(), requirement.getId());
				incompletedRequirements.add(requirement);
				changeDao.postChange(requirement, null, Change.REQ_REJECTED_IN_SPRINT, requirement.getLabel(), getId());
			}
		}

		report.setCompletedRequirements(completedRequirements);
		report.setRejectedRequirements(incompletedRequirements);
		// create and persist
		for (User user : project.getTeamMembers()) {
			TeamMemberSnapshot snapshot = teamMemberSnapshotDao.getSnapshot(this, user, true);
			teamMemberStatistics.add(snapshot);
		}
		report.setTeamMemberStatistics(teamMemberStatistics);

		setCompletedRequirementsData(SprintHistoryHelper.encodeRequirementsAndTasks(completedRequirements));
		setIncompletedRequirementsData(SprintHistoryHelper.encodeRequirementsAndTasks(incompletedRequirements));
		setTeamMembersData(SprintHistoryHelper.encodeTeamMemberData(this, project.getTeamMembers()));

		for (Requirement requirement : requirements) {
			List<Task> tasks = new ArrayList<Task>(requirement.getTasksInSprint());
			if (requirement.isClosed()) {
				Float work = requirement.getEstimatedWork();
				if (work != null) velocity += work;
			} else {
				for (Task task : tasks) {
					if (task.isClosed()) {
						task.setClosedInPastSprint(this);
					} else {
						task.reset();
					}
				}
			}
			requirement.setSprint(null);
		}

		Set<Issue> fixedIssues = getFixedIssues();
		if (!fixedIssues.isEmpty()) {
			releaseNotes.append("'''\nFixed bugs'''\n\n");
			for (Issue issue : fixedIssues) {
				releaseNotes.append("* ").append(issue.getReferenceAndLabel()).append("\n");
			}
		}

		for (Release release : getReleases()) {
			StringBuilder sb = new StringBuilder();
			if (release.isReleaseNotesSet()) {
				sb.append(release.getReleaseNotes());
				sb.append("\n\n");
			}
			sb.append(releaseNotes.toString());
			release.setReleaseNotes(sb.toString());
		}
		setVelocity(velocity);

		setProductOwners(project.getProductOwners());
		setScrumMasters(project.getScrumMasters());
		setTeamMembers(project.getTeamMembers());
		int roundedVelocity = Math.round(velocity);
		if (roundedVelocity > 0) project.setVelocity(roundedVelocity);
	}

	public Set<Issue> getFixedIssues() {
		Set<Issue> fixedIssues = new HashSet<Issue>();
		for (Release release : getReleases()) {
			for (Issue issue : release.getFixIssues()) {
				if (issue.isFixed()) fixedIssues.add(issue);
			}
		}
		return fixedIssues;
	}

	public String getProductOwnersAsString() {
		return Str.concat(User.getNames(getProductOwners()), ", ");
	}

	public String getScrumMastersAsString() {
		return Str.concat(User.getNames(getScrumMasters()), ", ");
	}

	public String getTeamMembersAsString() {
		return Str.concat(User.getNames(getTeamMembers()), ", ");
	}

	public List<SprintDaySnapshot> getDaySnapshots() {
		return sprintDaySnapshotDao.getSprintDaySnapshots(this);
	}

	public Set<SprintDaySnapshot> getExistingDaySnapshots() {
		return sprintDaySnapshotDao.getSprintDaySnapshotsBySprint(this);
	}

	public List<TaskDaySnapshot> getTaskDaySnapshots() {
		List<TaskDaySnapshot> shots = new ArrayList<TaskDaySnapshot>();
		List<Task> tasks = new ArrayList<Task>(getTasks());
		for (Task task : tasks) {
			shots.addAll(task.getTaskDaySnapshots(this));
		}
		return shots;
	}

	public Integer getLengthInDays() {
		if (!isBeginSet() || !isEndSet()) return null;
		return getBegin().getPeriodTo(getEnd()).toDays();
	}

	public Integer getLengthInWorkDays() {
		if (!isBeginSet() || !isEndSet()) return null;
		Date date = getBegin();
		int days = 0;
		WeekdaySelector freeDays = getProject().getFreeDaysAsWeekdaySelector();
		while (date.isBeforeOrSame(getEnd()) && date.isPastOrToday()) {
			if (!freeDays.isFree(date.getWeekday().getDayOfWeek())) {
				days++;
			}
			date = date.nextDay();
		}
		return days;
	}

	public SprintDaySnapshot getDaySnapshot(Date date) {
		return sprintDaySnapshotDao.getSprintDaySnapshot(this, date, true);
	}

	public int getRemainingWork() {
		int sum = 0;
		for (Task task : getTasks()) {
			Integer effort = task.getRemainingWork();
			if (effort != null) sum += effort;
		}
		return sum;
	}

	public int getBurnedWork() {
		int sum = 0;
		for (Task task : getTasks()) {
			sum += task.getBurnedWork();
		}
		return sum;
	}

	public int getInitialWork() {
		return getDaySnapshot(getBegin()).getRemainingWork();
	}

	public int getAllBurnedWork() {
		return getDaySnapshot(getEnd()).getBurnedWork();
	}

	public int getAllRemainedWork() {
		return getDaySnapshot(getEnd()).getRemainingWork();
	}

	public Set<Task> getTasks() {
		return taskDao.getTasksBySprint(this);
	}

	public String getReference() {
		return scrum.client.sprint.Sprint.REFERENCE_PREFIX + getNumber();
	}

	@Override
	public void updateNumber() {
		if (getNumber() == 0) setNumber(getProject().generateSprintNumber());
	}

	@Override
	public void ensureIntegrity() {
		super.ensureIntegrity();
		updateNumber();

		Project project = getProject();

		if (project.isCurrentSprint(this)) {
			if (!isBeginSet()) setBegin(Date.today());
			if (!isEndSet()) setEnd(getBegin().addDays(14));

			// auto stretch sprint
			if (getEnd().isYesterday()) setEnd(Date.today());

			updateNextSprintDates();
		}

		if (project.isNextSprint(this)) {
			// auto move next sprint
			if (isBeginSet() && getBegin().isPast()) {
				int len = 0;
				if (isEndSet()) len = getLengthInDays();
				setBegin(Date.today());
				if (len > 0) setEnd(Date.inDays(len));
			}
		}

		if (isBeginSet() && isEndSet() && getBegin().isAfter(getEnd())) setEnd(getBegin());

		// delete when not current and end date older than 4 weeks
		// if (isEndSet() && !getProject().isCurrentSprint(this) && getEnd().isPast()
		// && getEnd().getPeriodToNow().toWeeks() > 4) {
		// LOG.info("Deleting sprint, which ended on", getEnd(), "->", toString());
		// getDao().deleteEntity(this);
		// }

	}

	public void updateNextSprintDates() {
		Project project = getProject();
		if (!project.isCurrentSprint(this)) return;

		Sprint nextSprint = project.getNextSprint();
		if (nextSprint == null) return;

		Date nextBegin = nextSprint.getBegin();
		if (nextBegin == null) return;

		if (getEnd().isAfter(nextBegin)) {
			Integer length = nextSprint.getLengthInDays();
			nextSprint.setBegin(getEnd());
			if (length != null) {
				nextSprint.setEnd(nextSprint.getBegin().addDays(length));
			}
		}
	}

	@Override
	public boolean isVisibleFor(User user) {
		return getProject().isVisibleFor(user);
	}

	public String getReferenceAndLabel() {
		return getReference() + " " + getLabel();
	}

	@Override
	public String toString() {
		return getReferenceAndLabel();
	}

	public void burndownTasksRandomly(Date begin, Date end) {

		int days = getBegin().getPeriodTo(getEnd()).toDays();
		int totalRemaining = getRemainingWork();
		int defaultWorkPerDay = totalRemaining / days;
		Set<User> teamMembers = getProject().getTeamMembers();
		ArrayList<User> team = new ArrayList<User>(teamMembers);

		getDaySnapshot(begin).updateWithCurrentSprint();

		Date currentDay = begin.nextDay();
		while (currentDay.isBeforeOrSame(end) && currentDay.isBeforeOrSame(getEnd())) {
			if (!currentDay.getWeekday().isWeekend()) {
				int toBurnPerDay = Utl.randomInt(0, defaultWorkPerDay * 2);
				totalRemaining = getRemainingWork();
				if (totalRemaining > 0) {
					int userIndex = 0;
					Collections.shuffle(team);
					for (Task task : getTasks()) {
						if (userIndex >= team.size()) {
							break;
						}

						if (toBurnPerDay == 0) break;
						int remaining = task.getRemainingWork();
						if (remaining > 0) {
							// claim
							if (task.getOwner() == null) {
								// random efficiency
								int factor = remaining / 3;
								task.setInitialWork(remaining + Utl.randomInt(-factor, factor));
								task.setOwner(team.get(userIndex));
							}
							// burn
							int burn = Math.min(ScrumWebApplication.get().getSystemConfig().getWorkingHoursPerDay(),
								remaining);
							task.addBurn(burn);
							task.setRemainingWork(remaining - burn);
							userIndex++;
							toBurnPerDay -= burn;
						}
						task.getDaySnapshot(currentDay).updateWithCurrentTask();

					}
				}
			}
			getDaySnapshot(currentDay).updateWithCurrentSprint();
			currentDay = currentDay.nextDay();
		}
	}

	private transient Comparator<Requirement> requirementsOrderComparator;

	public Comparator<Requirement> getRequirementsOrderComparator() {
		if (requirementsOrderComparator == null) requirementsOrderComparator = new Comparator<Requirement>() {

			@Override
			public int compare(Requirement a, Requirement b) {
				List<String> order = getRequirementsOrderIds();
				int additional = order.size();
				int ia = order.indexOf(a.getId());
				if (ia < 0) {
					ia = additional;
					additional++;
				}
				int ib = order.indexOf(b.getId());
				if (ib < 0) {
					ib = additional;
					additional++;
				}
				return ia - ib;
			}
		};
		return requirementsOrderComparator;
	}

	public static final Comparator<Sprint> END_DATE_COMPARATOR = new Comparator<Sprint>() {

		@Override
		public int compare(Sprint a, Sprint b) {
			return Utl.compare(b.getEnd(), a.getEnd());
		}

	};

	public static final Comparator<Sprint> REVERSE_END_DATE_COMPARATOR = new Comparator<Sprint>() {

		@Override
		public int compare(Sprint a, Sprint b) {
			return Utl.compare(a.getEnd(), b.getEnd());
		}

	};

	public UserEfficiency getTeamEfficiency() {
		return getUserEfficiency(TEAM);
	}

	public UserEfficiency getUserEfficiency(String userName) {

		UserEfficiency result = new UserEfficiency();
		Float efficiency = 0.00f;
		Integer allBurnedHours = 0;
		Integer initialBurnableHours = 0;
		int initialWork = 0;

		SprintReport sprintReport = getSprintReport();
		if (sprintReport != null && isClosed()) {
			// sprint history
			for (TeamMemberSnapshot snapshot : sprintReport.getTeamMemberStatistics()) {
				String teamMember = snapshot.getTeamMember().getName();
				log.info(teamMember + "'s stored efficiency: " + snapshot.getEfficiency());
				if (TEAM.equals(userName) || userName.equals(teamMember)) {
					allBurnedHours += snapshot.getBurnedWork();
					initialWork = snapshot.getInitialWork();
					initialBurnableHours += initialWork == 0 ? snapshot.getBurnedWork() : initialWork;
				}
			}

		} else {

			List<Task> sprintTasks = new LinkedList<Task>(this.getTasks());
			for (Task task : sprintTasks) {
				if ((TEAM.equals(userName) || task.isOwnersTask(userName)) && task.isClosed()) {
					allBurnedHours += task.getBurnedWork();
					initialWork = task.getInitialWork();
					initialBurnableHours += initialWork == 0 ? task.getBurnedWork() : initialWork;
				}
			}

		}

		if (allBurnedHours > 0 && initialBurnableHours > 0) {
			efficiency = initialBurnableHours.floatValue() / allBurnedHours.floatValue();
		}

		result.setEfficiency(BigDecimal.valueOf(efficiency).setScale(2, RoundingMode.HALF_UP).floatValue());
		result.setAllBurnedHours(allBurnedHours);
		result.setInitialBurnableHours(initialBurnableHours);
		result.setBurnedHoursPerInitial(" (" + initialBurnableHours + "/" + allBurnedHours + ")");
		log.debug(userName + "'s UserEfficiency: " + efficiency + result.getBurnedHoursPerInitial());
		return result;
	}

	public boolean isClosed() {
		return getVelocity() != null;
	}

	public int getUserBurnAt(User user, ilarkesto.core.time.Date date) {
		int burnedWork = 0;
		int burnedWorkSum = 0;

		for (Task task : this.getTasks()) {
			if (user.equals(task.getOwner())) {
				int previousSnapshotBurn = 0;
				List<TaskDaySnapshot> taskDaySnapshots = task.getTaskDaySnapshots(this);
				Collections.sort(taskDaySnapshots, TaskDaySnapshot.DATE_COMPARATOR);
				for (TaskDaySnapshot snapshot : taskDaySnapshots) {

					burnedWork = snapshot.getBurnedWork();
					if (previousSnapshotBurn != 0 && burnedWork > 0) {
						burnedWork -= previousSnapshotBurn;
					}

					ilarkesto.core.time.Date burnDate = snapshot.getDate();
					if (burnDate.equals(date) && burnDate.isSameOrAfter(this.getBegin())) {
						burnedWorkSum += burnedWork;
					}
					previousSnapshotBurn = snapshot.getBurnedWork();
				}
			}

		}
		return burnedWorkSum;
	}

	public Date getLastWorkDay() {
		Date begin = getBegin();
		Date lastWorkDay = Date.today().prevDay();
		WeekdaySelector freeDays = getProject().getFreeDaysAsWeekdaySelector();
		int dayOfWeek = lastWorkDay.getWeekday().getDayOfWeek();
		int count = 0;
		while (freeDays.isFree(dayOfWeek) && count < 28 && !begin.isAfter(lastWorkDay)) {
			lastWorkDay = lastWorkDay.prevDay();
			dayOfWeek = lastWorkDay.getWeekday().getDayOfWeek();
			count++;
		}
		return lastWorkDay;
	}

}
