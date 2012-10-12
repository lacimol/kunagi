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
package scrum.client.sprint;

import ilarkesto.gwt.client.AFieldValueWidget;
import ilarkesto.gwt.client.TableBuilder;
import ilarkesto.gwt.client.editor.RichtextEditorWidget;
import ilarkesto.gwt.client.editor.TextOutputWidget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import scrum.client.ScrumGwt;
import scrum.client.common.AScrumWidget;
import scrum.client.common.SimpleValueWidget;
import scrum.client.sprint.SprintHistoryHelper.StoryInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class SprintWidget extends AScrumWidget {

	private Sprint sprint;

	public SprintWidget(Sprint sprint) {
		super();
		this.sprint = sprint;
	}

	@Override
	protected Widget onInitialization() {
		boolean completed = sprint.isCompleted();

		TableBuilder tb = ScrumGwt.createFieldTable();
		tb.setColumnWidths("80px", "100px", "80px", "100px", "80px");

		int cols = 6;
		if (!completed) tb.addFieldRow("Label", sprint.getLabelModel(), cols - 1);
		if (!completed || sprint.getGoal() != null) {
			tb.addFieldRow("Goal", new RichtextEditorWidget(sprint.getGoalModel()), cols - 1);
		}
		if (!completed || (sprint.getReleases() != null && !sprint.getReleases().isEmpty())) {
			tb.addFieldRow("Releases", new AFieldValueWidget() {

				@Override
				protected void onUpdate() {
					setContent(ScrumGwt.createToHtmlItemsWidget(sprint.getReleases()));
				}
			});
		}

		if (completed) {
			tb.addFieldRow("Velocity", new TextOutputWidget(sprint.getVelocityModel()), cols - 1);
		}

		tb.addField("Begin", sprint.getBeginModel());
		tb.addField("Length", sprint.getLengthInDaysModel());
		tb.addFieldRow("End", sprint.getEndModel());

		if (!completed) {
			tb.addFieldLabel("Stories");
			tb.addField("Completed", new AFieldValueWidget() {

				@Override
				protected void onUpdate() {
					setText(getCurrentProject().formatEfford(getSprint().getCompletedRequirementWork()));
				}
			});
			tb.addField("Estimated", new AFieldValueWidget() {

				@Override
				protected void onUpdate() {
					setText(getCurrentProject().formatEfford(getSprint().getEstimatedRequirementWork()));
				}
			});
			tb.nextRow();

			tb.addFieldLabel("Tasks");
			tb.addField("Burned", new AFieldValueWidget() {

				@Override
				protected void onUpdate() {
					setHours(getSprint().getBurnedWork());
				}
			});
			tb.addField("Remaining", new AFieldValueWidget() {

				@Override
				protected void onUpdate() {
					setHours(getSprint().getRemainingWork());
				}
			}, 2);
			tb.nextRow();
		} else {
			// completed
			List<StoryInfo> stories = SprintHistoryHelper.parseRequirementsAndTasks(sprint
					.getCompletedRequirementsData());
			if (sprint.getSprintReport() == null) {
				if (!stories.isEmpty()) {
					tb.addFieldRow("Completed Stories", new RichtextEditorWidget(getSprint()
							.getCompletedRequirementLabelsModel()), cols - 1);
				}
			}
		}

		if (!completed || sprint.getPlanningNote() != null) {
			tb.addFieldRow("Planning Note", new RichtextEditorWidget(sprint.getPlanningNoteModel()), cols - 1);
		}
		if (!completed || sprint.getReviewNote() != null) {
			tb.addFieldRow("Review Note", new RichtextEditorWidget(sprint.getReviewNoteModel()), cols - 1);
		}
		if (!completed || sprint.getRetrospectiveNote() != null) {
			tb.addFieldRow("Retrospective Note", new RichtextEditorWidget(sprint.getRetrospectiveNoteModel()), cols - 1);
		}

		Widget up = TableBuilder.row(10, tb.createTable(), ScrumGwt.createEmoticonsAndComments(sprint));
		if (sprint.hasTeamMemberStat()) {
			// new EfficiencyWidget(sprint)
			return TableBuilder.column(10, up, createTeamMemberStatTable());
		}
		return up;

	}

	private FlexTable createTeamMemberStatTable() {

		TableBuilder teamMemberStats = ScrumGwt.createFieldTable();
		List<TeamMemberSnapshot> teamMemberStatistics = new ArrayList<TeamMemberSnapshot>(sprint.getSprintReport()
				.getTeamMemberStatistics());
		Collections.sort(teamMemberStatistics);
		for (final TeamMemberSnapshot snapshot : teamMemberStatistics) {
			String name = snapshot.getTeamMember().getName();
			int efficiency = (int) (snapshot.getEfficiency() * 100);
			teamMemberStats.addField(name, new SimpleValueWidget(efficiency + "%"));

		}
		return teamMemberStats.createTable();

	}

	public Sprint getSprint() {
		return sprint;
	}

}
