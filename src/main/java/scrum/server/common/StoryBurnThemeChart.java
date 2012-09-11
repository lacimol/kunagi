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

import ilarkesto.core.logging.Log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import scrum.server.project.Requirement;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.SprintReport;
import scrum.server.sprint.Task;

public class StoryBurnThemeChart extends Chart {

	private static final Log LOG = Log.get(StoryBurnThemeChart.class);

	public static byte[] createBurndownChartAsByteArray(Sprint sprint, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new StoryBurnThemeChart().writeChart(out, sprint, width, height);
		return out.toByteArray();
	}

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		DefaultPieDataset dataset = new DefaultPieDataset();
		Map<String, Integer> themes = new HashMap<String, Integer>();

		SprintReport sprintReport = sprint.getSprintReport();
		Collection<Requirement> reqs = sprint.getRequirements();
		if (sprintReport != null) {
			reqs = sprintReport.getCompletedRequirementsAsList();
			reqs.addAll(sprintReport.getRejectedRequirementsAsList());
		}

		int noThemes = 0;
		int allBurnedWork = 0;
		for (Requirement req : reqs) {
			allBurnedWork = getAllBurnedWorkForReq(sprint, req);
			for (String theme : req.getThemes()) {
				Integer value = themes.get(theme);
				if (value == null) {
					value = 0;
				}
				value += allBurnedWork;
				themes.put(theme, value);
			}
			if (req.getThemes().size() == 0) {
				noThemes += allBurnedWork;
				themes.put("No themes", noThemes);
			}
		}

		// TODO percent
		for (Map.Entry<String, Integer> theme : themes.entrySet()) {
			dataset.setValue(theme.getKey(), new Double(theme.getValue()));
		}

		final JFreeChart chart = createPieChart(dataset);
		createPic(out, width, height, chart);
	}

	private int getAllBurnedWorkForReq(Sprint sprint, Requirement req) {

		SprintReport sprintReport = sprint.getSprintReport();
		Collection<Task> allTasks = req.getTasks();
		if (sprintReport != null) {
			allTasks = sprintReport.getClosedTasks(req);
			allTasks.addAll(sprintReport.getOpenTasks(req));

		}

		int allBurnedWork = 0;
		for (Task task : allTasks) {
			allBurnedWork += task.getBurnedWork();
		}
		return allBurnedWork;

	}
}
