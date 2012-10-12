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
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import scrum.server.project.Requirement;
import scrum.server.sprint.Sprint;

public class StoryCountThemeChart extends Chart {

	private static final Log LOG = Log.get(StoryCountThemeChart.class);

	public static byte[] createBurndownChartAsByteArray(Sprint sprint, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new StoryCountThemeChart().writeChart(out, sprint, width, height);
		return out.toByteArray();
	}

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		DefaultPieDataset dataset = new DefaultPieDataset();
		Map<String, Integer> themes = new HashMap<String, Integer>();

		int noThemes = 0;
		for (Requirement req : sprint.getRequirements()) {
			for (String theme : req.getThemes()) {
				Integer value = themes.get(theme);
				if (value == null) {
					value = 0;
				}
				themes.put(theme, ++value);
			}
			if (req.getThemes().size() == 0) {
				themes.put("No themes", ++noThemes);
			}
		}

		for (Map.Entry<String, Integer> theme : themes.entrySet()) {
			dataset.setValue(theme.getKey(), new Double(theme.getValue()));
		}

		final JFreeChart chart = createAmountPieChart(dataset);
		createPic(out, width, height, chart);
	}
}
