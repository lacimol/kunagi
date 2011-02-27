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
import ilarkesto.base.Utl;
import ilarkesto.base.time.Date;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.Layer;

import scrum.server.admin.User;
import scrum.server.css.ScreenCssBuilder;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.SprintDao;

public class Chart {

	protected static final Color COLOR_PAST_LINE = Utl.parseHtmlColor(ScreenCssBuilder.cBurndownLine);
	protected static final Color COLOR_PROJECTION_LINE = Utl.parseHtmlColor(ScreenCssBuilder.cBurndownProjectionLine);
	protected static final Color COLOR_OPTIMUM_LINE = Utl.parseHtmlColor(ScreenCssBuilder.cBurndownOptimalLine);

	protected static final String TEAM = "team";

	// --- dependencies ---

	protected SprintDao sprintDao;

	public void setSprintDao(SprintDao sprintDao) {
		this.sprintDao = sprintDao;
	}

	// --- ---

	public static Map<String, Color> userColors = new HashMap<String, Color>();
	static {
		userColors.put("team", Color.RED);
		userColors.put("black", Color.BLACK);
		userColors.put("darkred", Color.decode("#8B0000"));
		userColors.put("darkgreen", Color.decode("#008400"));
		userColors.put("darkblue", Color.decode("#00008B"));
		userColors.put("darkorange", Color.decode("#FF8C00"));
		userColors.put("darkorchid", Color.decode("#9932CC"));
		userColors.put("darkslateblue", Color.decode("#0000FB"));
		userColors.put("darkgray", Color.DARK_GRAY);
		userColors.put("orange", Color.ORANGE);
		userColors.put("green", Color.GREEN);
	}

	public int getWorkingHoursPerDay(Integer hours) {
		// default is 7 houts/day/user
		return hours == null ? 7 : hours;
	}

	protected static JFreeChart createChart(Date firstDay, Date lastDay, int dateMarkTickUnit, float widthPerDay,
			DefaultXYDataset data, double max, int height) {

		double valueLabelTickUnit = calculateTick(max, height);
		double upperBoundary = Math.min(max * 1.1f, max + 3);

		JFreeChart chart = ChartFactory.createXYLineChart("", "", "", data, PlotOrientation.VERTICAL, false, true,
			false);

		XYItemRenderer renderer = chart.getXYPlot().getRenderer();

		renderer.setSeriesPaint(0, COLOR_PAST_LINE);
		renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		renderer.setSeriesPaint(1, COLOR_PROJECTION_LINE);
		renderer.setSeriesStroke(1, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f,
				new float[] { 3f }, 0));
		renderer.setSeriesPaint(2, COLOR_OPTIMUM_LINE);
		renderer.setSeriesStroke(2, new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));

		DateAxis domainAxis1 = new DateAxis();
		domainAxis1.setLabelFont(new Font(domainAxis1.getLabelFont().getName(), Font.PLAIN, 7));
		// String dateFormat = "        EE d.";
		String dateFormat = "d.";
		widthPerDay -= 5;
		if (widthPerDay > 40) {
			dateFormat = "EE " + dateFormat;
		}
		if (widthPerDay > 10) {
			float spaces = widthPerDay / 2.7f;
			dateFormat = Str.multiply(" ", (int) spaces) + dateFormat;
		}
		domainAxis1.setDateFormatOverride(new SimpleDateFormat(dateFormat, Locale.US));
		domainAxis1.setTickUnit(new DateTickUnit(DateTickUnit.DAY, dateMarkTickUnit));
		domainAxis1.setAxisLineVisible(false);
		Range range = new Range(firstDay.toMillis(), lastDay.nextDay().toMillis());
		domainAxis1.setRange(range);

		DateAxis domainAxis2 = new DateAxis();
		domainAxis2.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 1));
		domainAxis2.setTickMarksVisible(false);
		domainAxis2.setTickLabelsVisible(false);
		domainAxis2.setRange(range);

		chart.getXYPlot().setDomainAxis(0, domainAxis2);
		chart.getXYPlot().setDomainAxis(1, domainAxis1);
		chart.getXYPlot().setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

		NumberAxis rangeAxis = new NumberAxis();
		rangeAxis.setLabelFont(new Font(rangeAxis.getLabelFont().getName(), Font.PLAIN, 6));
		rangeAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		rangeAxis.setTickUnit(new NumberTickUnit(valueLabelTickUnit));

		rangeAxis.setLowerBound(0);
		rangeAxis.setUpperBound(upperBoundary);

		chart.getXYPlot().setRangeAxis(rangeAxis);

		chart.getXYPlot().getRenderer().setBaseStroke(new BasicStroke(2f));

		chart.setBackgroundPaint(Color.WHITE);

		return chart;
	}

	private static double calculateTick(double max, int height) {
		double tick = 1.0;
		// 270/11~25
		int count = height / 11;

		while (max / tick > count) {
			tick *= 2;
			if (max / tick <= count) break;
			tick *= 2.5;
			if (max / tick <= count) break;
			tick *= 2;
		}
		return tick;
	}

	static double getMaximum(DefaultXYDataset data) {
		double max = 0;
		for (int i = 0; i < data.getSeriesCount(); i++) {
			for (int j = 0; j < data.getItemCount(i); j++) {
				double value = data.getYValue(i, j);
				if (value > max) {
					max = value;
				}
			}
		}
		return max;
	}

	protected static double[][] toArray(List<Double> a, List<Double> b) {
		int min = Math.min(a.size(), b.size());
		double[][] array = new double[2][min];
		for (int i = 0; i < min; i++) {
			array[0][i] = a.get(i);
			array[1][i] = b.get(i);
		}
		return array;
	}

	protected JFreeChart createEfficiencyChart(final CategoryDataset dataset, Sprint sprint) {

		StandardCategoryItemLabelGenerator itemLabelGenerator = new StandardCategoryItemLabelGenerator("{2}",
				new DecimalFormat("###%"));
		JFreeChart chart = createBarChart(dataset, sprint, itemLabelGenerator);
		final IntervalMarker target = new IntervalMarker(1, 2.5);
		target.setPaint(new Color(222, 222, 255, 128));
		CategoryPlot plot = chart.getCategoryPlot();
		plot.addRangeMarker(target, Layer.BACKGROUND);
		plot.getDomainAxis().setMaximumCategoryLabelLines(3);
		return chart;

	}

	protected JFreeChart createBarChart(final CategoryDataset dataset, Sprint sprint,
			StandardCategoryItemLabelGenerator itemLabelGenerator) {
		final JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, false,
			true, false);

		chart.setBackgroundPaint(Color.WHITE);

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setRangeGridlinesVisible(true);
		plot.setDomainGridlinesVisible(true);
		plot.setNoDataMessage("NO DATA!");

		final CategoryItemRenderer renderer = new CustomRenderer(getColors(dataset, sprint));
		renderer.setSeriesItemLabelGenerator(0, itemLabelGenerator);
		renderer.setBaseItemLabelsVisible(true);
		renderer.setSeriesPaint(0, COLOR_OPTIMUM_LINE);
		plot.setRenderer(renderer);

		// change the margin at the top of the range axis...
		final ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		rangeAxis.setLowerMargin(0.15);
		rangeAxis.setUpperMargin(0.15);

		plot.getDomainAxis().setMaximumCategoryLabelLines(2);
		return chart;
	}

	private Paint[] getColors(final CategoryDataset dataset, Sprint sprint) {
		List<Paint> colors = new ArrayList<Paint>();
		double teamValue = dataset.getValue(0, 0).doubleValue();
		if (teamValue < 0.9) {
			colors.add(userColors.get(TEAM));
		} else {
			colors.add(Color.decode("#005000"));
		}
		for (User user : sprint.getProject().getTeamMembers()) {
			colors.add(userColors.get(user.getColor()));
		}
		return colors.toArray(new Paint[colors.size()]);
	}

	/**
	 * A custom renderer that returns a different color for each item in a single series.
	 */
	class CustomRenderer extends BarRenderer {

		/** The colors. */
		private Paint[] colors;

		/**
		 * Creates a new renderer.
		 * 
		 * @param colors the colors.
		 */
		public CustomRenderer(final Paint[] colors) {
			this.colors = colors;
		}

		/**
		 * Returns the paint for an item. Overrides the default behaviour inherited from
		 * AbstractSeriesRenderer.
		 * 
		 * @param row the series.
		 * @param column the category.
		 * 
		 * @return The item color.
		 */
		@Override
		public Paint getItemPaint(final int row, final int column) {
			return this.colors[column % this.colors.length];
		}
	}

}
