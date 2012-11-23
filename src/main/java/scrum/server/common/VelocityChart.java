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

import java.io.OutputStream;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import scrum.server.project.Project;
import scrum.server.sprint.Sprint;

public class VelocityChart extends Chart {

	private static final String AVG = "average";

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		Project project = sprint.getProject();
		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		barDataset.addValue(0, "S1", AVG);

		double maxVelocity = 0.0;
		double sum = 0;
		int count = 10;
		Float velocity;
		for (Sprint completedSprint : project.getFormerSprints(count)) {
			velocity = completedSprint.getVelocity();
			if (velocity == null || velocity.intValue() == 0) continue;
			barDataset.addValue(velocity, "S1", completedSprint.getLabel());
			maxVelocity = Math.max(velocity, maxVelocity);
			sum += velocity;
		}

		Double average = 0.0;
		if (count > 0 && sum > 0) {
			average = sum / count;
		}
		barDataset.setValue(average.intValue(), "S1", AVG);

		JFreeChart chart = createBarChart(barDataset);
		setChartMarker(chart, average, maxVelocity);
		setUpperBoundary(chart, maxVelocity + 25);
		createPic(out, width, height, chart);
	}

}
