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

package scrum.server.common;

import ilarkesto.base.Str;
import ilarkesto.base.Sys;
import ilarkesto.base.Utl;
import ilarkesto.base.time.Date;
import ilarkesto.io.IO;
import ilarkesto.testng.ATest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import scrum.TestUtil;
import scrum.server.admin.User;
import scrum.server.issues.Issue;
import scrum.server.project.Project;
import scrum.server.project.Requirement;
import scrum.server.sprint.Sprint;

public class ChartTest extends ATest {

	@BeforeSuite
	public void init() {
		Sys.setHeadless(true);
		TestUtil.initialize();
		createProject();
	}

	Project project;

	@Test
	public void accomplishChart() throws IOException {

		BufferedOutputStream out = getOutputStream("/accomplishChart.png");
		new AccomplishChart().writeChart(out, project.getCurrentSprint(), 1000, 500);
		out.close();

	}

	@Test
	public void efficiencyChart() throws IOException {

		BufferedOutputStream out = getOutputStream("/efficiencyChart.png");
		new EfficiencyChart().writeChart(out, project.getCurrentSprint(), 1000, 500);
		out.close();

	}

	@Test
	public void userBurndownChart() throws IOException {

		Set<User> teamMembers = project.getTeamMembers();
		BufferedOutputStream out = getOutputStream("/userBurndown.png");
		new UserBurndownChart().writeChart(out, project.getCurrentSprint(), 1000, 500,
			((User) teamMembers.toArray()[Utl.randomInt(0, teamMembers.size() - 1)]).getName());
		out.close();

	}

	@Test
	public void teamBurndownChart() throws IOException {

		BufferedOutputStream out = getOutputStream("/teamBurndown.png");
		new UserBurndownChart().writeChart(out, project.getCurrentSprint(), 1000, 500, null);
		out.close();

	}

	private BufferedOutputStream getOutputStream(String fileName) throws FileNotFoundException {
		File file = new File(OUTPUT_DIR + fileName);
		IO.createDirectory(file.getParentFile());
		return new BufferedOutputStream(new FileOutputStream(file));
	}

	private void createProject() {
		if (project == null) {
			project = TestUtil.createProject(TestUtil.getAdmin());
			// project.addTeamMember(TestUtil.getAdmin());
			for (User user : TestUtil.getTestUsers()) {
				project.addTeamMember(user);
			}
			project.addParticipants(project.getAdmins());
			project.addParticipants(project.getTeamMembers());

			Sprint sprint = TestUtil.createSprint(project, Date.today().addDays(-14), Date.today());
			project.setCurrentSprint(sprint);

			sprint.setLabel(Str.generateRandomSentence(2, 4));
			sprint.setGoal(Str.generateRandomParagraph());

			createTasksForSprint(sprint);

			sprint.burndownTasksRandomly(sprint.getBegin(), Date.today());
		}
	}

	private void createTasksForSprint(Sprint sprint) {
		Requirement story;
		for (int i = 1; i <= 5; i++) {
			story = TestUtil.createRequirement(project, i);
			story.setSprint(sprint);
			story.setEstimatedWorkAsString(Utl.randomElement(scrum.client.project.Requirement.WORK_ESTIMATION_VALUES));
			story.setDirty(false);
			for (int j = 1; j <= 3; j++) {
				TestUtil.createTask(story, j, 16);
			}
		}
		for (int i = 6; i <= 10; i++) {
			TestUtil.createRequirement(project, i);
		}

		Issue issue;
		for (int i = 1; i <= 5; i++) {
			issue = TestUtil.createIssue(project, i);
			issue.setAcceptDate(Date.today());
			issue.setUrgent(true);
			if (i == 1) TestUtil.createComments(issue, 2);
		}
		for (int i = 6; i <= 8; i++) {
			issue = TestUtil.createIssue(project, i);
			issue.setAcceptDate(Date.today());
		}
	}

	@AfterSuite
	public void cleanup() {
		if (project != null) TestUtil.getApp().getProjectDao().deleteEntity(project);
	}

}
