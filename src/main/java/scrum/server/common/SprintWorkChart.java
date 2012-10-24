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

public class SprintWorkChart extends Chart {

	private static final String REMAINED_SERIES = "Remained";
	private static final String EXTRA_SERIES = "Extra";
	private static final String INITIAL_SERIES = "Initial";
	private static final String AVG = "average";

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		Project project = sprint.getProject();
		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		barDataset.addValue(0, INITIAL_SERIES, AVG);
		barDataset.addValue(0, EXTRA_SERIES, AVG);
		barDataset.addValue(0, REMAINED_SERIES, AVG);

		int maxValue = 0;
		int sumBurned = 0;
		int sumRemained = 0;
		int sumInitial = 0;
		int count = 10;

		int burned;
		int remained;
		int initial;

		for (Sprint completedSprint : project.getFormerSprints(count)) {

			if (completedSprint.getVelocity() == null || completedSprint.getVelocity().intValue() == 0) continue;
			initial = completedSprint.getInitialWork();
			barDataset.addValue(initial, INITIAL_SERIES, completedSprint.getLabel());
			sumInitial += initial;

			burned = Math.max(completedSprint.getAllBurnedWork() - initial, 0);
			barDataset.addValue(burned, EXTRA_SERIES, completedSprint.getLabel());
			sumBurned += burned;

			remained = completedSprint.getAllRemainedWork();
			barDataset.addValue(remained, REMAINED_SERIES, completedSprint.getLabel());
			sumRemained += remained;

			maxValue = Math.max(initial + burned + remained, maxValue);
		}

		barDataset.setValue(getAverage(sumInitial, count), INITIAL_SERIES, AVG);
		barDataset.setValue(getAverage(sumBurned, count), EXTRA_SERIES, AVG);
		barDataset.setValue(getAverage(sumRemained, count), REMAINED_SERIES, AVG);

		JFreeChart chart = createStackedBarChart(barDataset);
		// setChartMarker(chart, avarageRemained, maxValue);
		setUpperBoundary(chart, maxValue);
		createPic(out, width, height, chart);
	}

	private int getAverage(int sum, int count) {
		int avarage = 0;
		if (count > 0 && sum > 0) {
			avarage = sum / count;
		}
		return avarage;
	}

}
