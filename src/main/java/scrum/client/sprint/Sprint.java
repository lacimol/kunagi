package scrum.client.sprint;

import ilarkesto.core.base.Utl;
import ilarkesto.core.scope.Scope;
import ilarkesto.gwt.client.Date;
import ilarkesto.gwt.client.Gwt;
import ilarkesto.gwt.client.HyperlinkWidget;
import ilarkesto.gwt.client.TimePeriod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import scrum.client.ScrumGwt;
import scrum.client.admin.Auth;
import scrum.client.admin.User;
import scrum.client.collaboration.ForumSupport;
import scrum.client.collaboration.Wiki;
import scrum.client.common.LabelSupport;
import scrum.client.common.ReferenceSupport;
import scrum.client.common.ShowEntityAction;
import scrum.client.common.WeekdaySelector;
import scrum.client.impediments.Impediment;
import scrum.client.project.Project;
import scrum.client.project.Requirement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;

public class Sprint extends GSprint implements ForumSupport, ReferenceSupport, LabelSupport {

	public static final String REFERENCE_PREFIX = "spr";

	private transient Comparator<Task> tasksOrderComparator;
	private transient Comparator<Requirement> requirementsOrderComparator;

	public Sprint(Project project, String label) {
		setProject(project);
		setLabel(label);
	}

	public Sprint(Map data) {
		super(data);
	}

	public void updateRequirementsOrder() {
		List<Requirement> requirements = getRequirements();
		Collections.sort(requirements, getRequirementsOrderComparator());
		updateRequirementsOrder(requirements);
	}

	public void updateRequirementsOrder(List<Requirement> requirements) {
		setRequirementsOrderIds(Gwt.getIdsAsList(requirements));
	}

	public List<Requirement> getCompletedUnclosedRequirements() {
		List<Requirement> ret = new ArrayList<Requirement>();
		for (Requirement req : getRequirements()) {
			if (req.isTasksClosed() && !req.isClosed() && !req.isRejected()) ret.add(req);
		}
		return ret;
	}

	public Integer getLengthInDays() {
		TimePeriod lenght = getLength();
		return lenght == null ? null : lenght.toDays();
	}

	public TimePeriod getLength() {
		Date begin = getBegin();
		Date end = getEnd();
		if (begin == null || end == null) return null;
		return getBegin().getPeriodTo(getEnd()).addDays(1);
	}

	public void setLengthInDays(Integer lenght) {
		if (lenght == null || lenght <= 0) return;
		Date begin = getBegin();
		if (begin == null) {
			begin = getProject().getCurrentSprint().getEnd();
			setBegin(begin);
		}
		Date end = begin.addDays(lenght - 1);
		setEnd(end);
	}

	public List<Task> getTasksBlockedBy(Impediment impediment) {
		List<Task> ret = new ArrayList<Task>();
		for (Requirement requirement : getRequirements()) {
			ret.addAll(requirement.getTasksBlockedBy(impediment));
		}
		return ret;
	}

	public String getChartUrl(int width, int height) {
		return GWT.getModuleBaseURL() + "sprintBurndownChart.png?sprintId=" + getId() + "&width=" + width + "&height="
				+ height;
	}

	public String getTaskBurndownChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=taskBurndownChart";
	}

	public String getWorkChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=workChart";
	}

	public String getUserChartUrl(int width, int height, String userName) {
		return getWorkChartUrl(width, height) + "&userName=" + userName;
	}

	public String getEfficiencyChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=efficiencyChart";
	}

	public String getAccomplishChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=accomplishChart";
	}

	public String getVelocityChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=velocityChart";
	}

	public String getSprintWorkChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=sprintWorkChart";
	}

	public String getSprintRangeChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=sprintRangeChart";
	}

	public String getCurrentSprintRangeChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=currentSprintRangeChart";
	}

	public String getTaskRangeChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=taskRangeChart";
	}

	public String getStoryThemeChartUrl(int width, int height) {
		return getChartUrl(width, height) + "&chart=storyThemeChart";
	}

	public boolean isCompleted() {
		return getVelocity() != null;
	}

	public float getEstimatedRequirementWork() {
		float sum = 0;
		for (Requirement requirement : getRequirements()) {
			Float work = requirement.getEstimatedWork();
			if (work != null) sum += work;
		}
		return sum;
	}

	public float getCompletedRequirementWork() {
		float sum = 0;
		for (Requirement requirement : getRequirements()) {
			if (!requirement.isClosed()) continue;
			Float work = requirement.getEstimatedWork();
			if (work != null) sum += work;
		}
		return sum;
	}

	public List<Requirement> getDecidableUndecidedRequirements() {
		List<Requirement> ret = new ArrayList<Requirement>();
		for (Requirement requirement : getRequirements()) {
			if (requirement.isDecidable() && !requirement.isClosed()) ret.add(requirement);
		}
		return ret;
	}

	public List<Task> getUnclaimedTasks(boolean sorted) {
		List<Task> ret = new ArrayList<Task>();
		List<Requirement> requirements = getRequirements();
		if (sorted) Collections.sort(requirements, getRequirementsOrderComparator());
		for (Requirement requirement : requirements) {
			ret.addAll(requirement.getUnclaimedTasks());
		}
		return ret;
	}

	public List<Task> getTasks(User user) {
		List<Task> ret = new ArrayList<Task>();
		for (Requirement requirement : getRequirements()) {
			for (Task task : requirement.getTasks()) {
				if (user == null) {
					if (!task.isOwnerSet()) {
						ret.add(task);
					}
				} else {
					if (task.isOwner(user)) {
						ret.add(task);
					}
				}
			}
		}
		return ret;
	}

	public List<Task> getClaimedTasks(User user) {
		List<Task> ret = new ArrayList<Task>();
		for (Requirement requirement : getRequirements()) {
			ret.addAll(requirement.getClaimedTasks(user));
		}
		return ret;
	}

	public int getBurnedWorkInClosedTasks() {
		int sum = 0;
		for (Requirement requirement : getRequirements()) {
			sum += requirement.getBurnedWorkInClosedTasks();
		}
		return sum;
	}

	public int getBurnedWork() {
		return Requirement.sumBurnedWork(getRequirements());
	}

	public int getBurnedWorkInClaimedTasks() {
		int sum = 0;
		for (Requirement requirement : getRequirements()) {
			sum += requirement.getBurnedWorkInClaimedTasks();
		}
		return sum;
	}

	public int getRemainingWorkInClaimedTasks() {
		int sum = 0;
		for (Requirement requirement : getRequirements()) {
			sum += requirement.getRemainingWorkInClaimedTasks();
		}
		return sum;
	}

	public int getRemainingWorkInUnclaimedTasks() {
		int sum = 0;
		for (Requirement requirement : getRequirements()) {
			sum += requirement.getRemainingWorkInUnclaimedTasks();
		}
		return sum;
	}

	public int getRemainingWork() {
		int sum = 0;
		for (Requirement requirement : getRequirements()) {
			sum += requirement.getRemainingWork();
		}
		return sum;
	}

	@Override
	public String toHtml() {
		return ScrumGwt.toHtml(this, getLabel());
	}

	@Override
	public String toString() {
		return getReference() + " " + getLabel();
	}

	@Override
	public boolean isEditable() {
		if (isCompleted()) return false;
		if (!getProject().isProductOwner(Scope.get().getComponent(Auth.class).getUser())) return false;
		return true;
	}

	@Override
	public boolean isPlanningEditable() {
		if (isCompleted()) return false;
		return true;
	}

	@Override
	public boolean isRetrospectiveEditable() {
		if (!getProject().isScrumMaster(Scope.get().getComponent(Auth.class).getUser())) return false;
		return true;
	}

	@Override
	public boolean isReviewEditable() {
		if (!getProject().isProductOwner(Scope.get().getComponent(Auth.class).getUser())) return false;
		return true;
	}

	@Override
	public boolean isDatesEditable() {
		if (isCompleted()) return false;
		if (!getProject().isProductOwner(Scope.get().getComponent(Auth.class).getUser())) return false;
		return true;
	}

	@Override
	public String getGoalTemplate() {
		return Scope.get().getComponent(Wiki.class).getTemplate("sprint.goal");
	}

	@Override
	public String getPlanningNoteTemplate() {
		return Scope.get().getComponent(Wiki.class).getTemplate("sprint.planning");
	}

	@Override
	public String getRetrospectiveNoteTemplate() {
		return Scope.get().getComponent(Wiki.class).getTemplate("sprint.retrospective");
	}

	@Override
	public String getReviewNoteTemplate() {
		return Scope.get().getComponent(Wiki.class).getTemplate("sprint.review");
	}

	public boolean isCurrent() {
		return getProject().isCurrentSprint(this);
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

	@Override
	public Widget createForumItemWidget() {
		String label = isCurrent() ? "Sprint Backlog" : "Sprint";
		return new HyperlinkWidget(new ShowEntityAction(this, label));
	}

	@Override
	public String getReference() {
		return REFERENCE_PREFIX + getNumber();
	}

	private transient LengthInDaysModel lengthInDaysModel;

	public LengthInDaysModel getLengthInDaysModel() {
		if (lengthInDaysModel == null) lengthInDaysModel = new LengthInDaysModel();
		return lengthInDaysModel;
	}

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

	public Comparator<Task> getTasksOrderComparator() {
		if (tasksOrderComparator == null) tasksOrderComparator = new Comparator<Task>() {

			@Override
			public int compare(Task a, Task b) {
				Requirement ar = a.getRequirement();
				Requirement br = b.getRequirement();
				if (ar != br) return getRequirementsOrderComparator().compare(ar, br);
				List<String> order = ar.getTasksOrderIds();
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
		return tasksOrderComparator;
	}

	protected class LengthInDaysModel extends ilarkesto.gwt.client.editor.AIntegerEditorModel {

		@Override
		public String getId() {
			return "Sprint_lengthInDays";
		}

		@Override
		public java.lang.Integer getValue() {
			Integer length = getLengthInDays();
			return length == null || length <= 0 ? null : length;
		}

		@Override
		public void setValue(java.lang.Integer value) {
			setLengthInDays(value);
		}

		@Override
		public void increment() {
			Integer length = getValue();
			if (length == null) length = 0;
			setLengthInDays(length + 1);
		}

		@Override
		public void decrement() {
			Integer lenght = getValue();
			if (lenght == null || lenght < 2) return;
			setLengthInDays(lenght - 1);
		}

		@Override
		public boolean isEditable() {
			return Sprint.this.isEditable();
		}

		@Override
		public boolean isMandatory() {
			return true;
		}

		@Override
		public String getTooltip() {
			return "The lenght of the sprint in days.";
		}

		@Override
		protected void onChangeValue(java.lang.Integer oldValue, java.lang.Integer newValue) {
			super.onChangeValue(oldValue, newValue);
			addUndo(this, oldValue);
		}

	}

	public Date getLastWorkDay() {
		Date begin = getBegin();
		Date lastWorkDay = Date.today().prevDay();
		WeekdaySelector freeDays = getProject().getFreeDaysWeekdaySelectorModel().getValue();
		int dayOfWeek = lastWorkDay.getWeekday() + 1;
		int count = 0;
		while (freeDays.isFree(dayOfWeek) && count < 28 && !begin.isAfter(lastWorkDay)) {
			lastWorkDay = lastWorkDay.prevDay();
			dayOfWeek = lastWorkDay.getWeekday() + 1;
			count++;
		}
		return lastWorkDay;
	}

}
