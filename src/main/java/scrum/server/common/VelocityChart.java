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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import scrum.server.project.Project;
import scrum.server.sprint.Sprint;

public class VelocityChart extends Chart {

	private static final Log LOG = Log.get(VelocityChart.class);
	private static final String AVG = "average";

	public static byte[] createBurndownChartAsByteArray(Sprint sprint, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new VelocityChart().writeChart(out, sprint, width, height);
		return out.toByteArray();
	}

	public void writeChart(OutputStream out, String sprintId, int width, int height) {
		Sprint sprint = sprintDao.getById(sprintId);
		if (sprint == null) throw new IllegalArgumentException("Sprint " + sprintId + " does not exist.");
		writeChart(out, sprint, width, height);
	}

	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		Project project = sprint.getProject();
		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		barDataset.addValue(0, "S1", AVG);

		List<Sprint> sprints = new ArrayList<Sprint>(project.getSprints());
		Collections.sort(sprints, Sprint.REVERSE_END_DATE_COMPARATOR);
		int maxVelocity = 0;
		float sum = 0;
		int count = 0;
		Float velocity;
		for (Sprint completedSprint : sprints) {
			velocity = completedSprint.getVelocity();
			if (velocity == null || velocity.intValue() == 0) continue;
			barDataset.addValue(velocity, "S1", completedSprint.getLabel());
			maxVelocity = Math.max(velocity.intValue(), maxVelocity);
			sum += velocity;
			count++;
			if (count >= 10) break;
		}

		int avarage = 0;
		if (count > 0 && sum > 0) {
			avarage = (int) sum / count;
		}
		barDataset.setValue(avarage, "S1", AVG);

		JFreeChart chart = createBarChart(barDataset);
		setChartMarker(chart, avarage, maxVelocity);
		setUpperBoundary(chart, maxVelocity + 25);
		createPic(out, width, height, chart);
	}

}
