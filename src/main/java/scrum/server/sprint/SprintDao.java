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
import ilarkesto.core.scope.In;
import ilarkesto.fp.Predicate;
import java.util.Set;

import java.util.Arrays;

import scrum.server.project.Project;
import scrum.server.project.Requirement;
import scrum.server.project.RequirementDao;

public class SprintDao extends GSprintDao {

	@In
	private RequirementDao requirementDao;

	@In
	private TaskDao taskDao;

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

	public void createTestHistorySprint(Project project, Date begin, Date end) {
		Sprint sprint = newEntityInstance();
		sprint.setProject(project);
		sprint.setLabel(Str.generateRandomSentence());
		sprint.setBegin(begin);
		sprint.setEnd(end);

		for (int i = 0; i < Utl.randomInt(2, 10); i++) {
			Requirement requirement = requirementDao.postRequirement(project, Str.generateRandomSentence(),
				Utl.randomElement(Arrays.asList(scrum.client.project.Requirement.WORK_ESTIMATION_FLOAT_VALUES)));
			requirement.setSprint(sprint);
			for (int j = 0; j < Utl.randomInt(2, 5); j++) {
				Task task = taskDao.postTask(requirement, Str.generateRandomSentence(), 0);
				task.setBurnedWork(Utl.randomInt(2, 10));
			}
			if (i == 0) {
				taskDao.postTask(requirement, "Incomplete task", 1);
			} else {
				requirement.setClosed(true);
			}
		}

		sprint.close();

		saveEntity(sprint);
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
