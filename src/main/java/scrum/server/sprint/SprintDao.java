package scrum.server.sprint;

import ilarkesto.base.Utl;
import ilarkesto.base.time.Date;
import ilarkesto.fp.Predicate;

import java.util.Set;

import scrum.server.project.Project;

public class SprintDao extends GSprintDao {

	public Sprint getSprintByNumber(final int number, final Project project) {
		return getEntity(new Predicate<Sprint>() {

			@Override
			public boolean test(Sprint sprint) {
				return sprint.isNumber(number) && sprint.isProject(project);
			}
		});
	}

	@Override
	public Sprint newEntityInstance() {
		Sprint sprint = super.newEntityInstance();
		sprint.setLabel("New Sprint");
		return sprint;
	}

	// --- test data ---

	public Sprint createTestSprint(Project project) {
		Date begin = Date.beforeDays(15);
		Date end = Date.inDays(15);

		Sprint sprint = newEntityInstance();
		sprint.setProject(project);
		sprint.setLabel("Our first Sprint!");
		sprint.setBegin(begin);
		sprint.setEnd(end);
		if (end.isPast()) sprint.setVelocity(20f);
		saveEntity(sprint);

		project.setCurrentSprint(sprint);

		return sprint;
	}

	public Sprint createTestSprint(Project project, Date begin, Date end, int count) {
		Sprint sprint = newEntityInstance();
		sprint.setProject(project);
		sprint.setLabel("Sprint " + count);
		sprint.setBegin(begin);
		sprint.setEnd(end);
		sprint.setGoal("Sprint from " + sprint.getBegin() + " to " + sprint.getEnd());
		if (end.isPast()) sprint.setVelocity(Float.valueOf(Utl.randomInt(10, 100)));
		saveEntity(sprint);

		return sprint;
	}

	public void createTestFormerSprints(Project project) {
		Set<Sprint> sprints = project.getSprints();
		sprints.add(createTestSprint(project, Date.today().addDays(-20), Date.today().addDays(-15), 3));
		sprints.add(createTestSprint(project, Date.today().addDays(-30), Date.today().addDays(-20), 2));
		sprints.add(createTestSprint(project, Date.today().addDays(-43), Date.today().addDays(-30), 1));
		for (Sprint sprint : sprints) {
			project.setCurrentSprint(sprint);
			project.addTestRequirements();
			sprint.burndownTasksRandomly(sprint.getBegin(), sprint.getEnd());
			saveEntity(sprint);
		}
	}
}
