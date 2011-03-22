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
import ilarkesto.core.logging.Log;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;

import scrum.server.ScrumWebApplication;
import scrum.server.admin.User;
import scrum.server.css.ScreenCssBuilder;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.SprintDao;

public abstract class Chart {

	private static final Log LOG = Log.get(Chart.class);

	protected static final Color COLOR_PAST_LINE = Utl.parseHtmlColor(ScreenCssBuilder.cBurndownLine);
	protected static final Color COLOR_PROJECTION_LINE = Utl.parseHtmlColor(ScreenCssBuilder.cBurndownProjectionLine);
	protected static final Color COLOR_OPTIMUM_LINE = Utl.parseHtmlColor(ScreenCssBuilder.cBurndownOptimalLine);

	protected static final String TEAM = "team";

	// --- dependencies ---

	protected SprintDao sprintDao;

	public void setSprintDao(SprintDao sprintDao) {
		this.sprintDao = sprintDao;
	}

	public void writeChart(OutputStream out, String sprintId, int width, int height) {
		Sprint sprint = sprintDao.getById(sprintId);
		if (sprint == null) throw new IllegalArgumentException("Sprint " + sprintId + " does not exist.");
		writeChart(out, sprint, width, height);
	}

	public abstract void writeChart(OutputStream out, Sprint sprint, int width, int height);

	// --- ---

	public static Map<String, Color> userColors = new HashMap<String, Color>();
	static {
		userColors.put("team", Color.decode("#006000"));
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

	public int getWorkingHoursPerDay() {
		// default is 7 hours/day/user
		Integer hours = ScrumWebApplication.get().getSystemConfig().getWorkingHoursPerDay();
		return hours == null ? 7 : hours;
	}

	public static JFreeChart createXYLineChart(Date firstDay, Date lastDay, int dateMarkTickUnit, float widthPerDay,
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

	public JFreeChart createEfficiencyChart(final CategoryDataset dataset, Sprint sprint) {

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

	public JFreeChart createBarChart(final CategoryDataset dataset) {
		return createBarChart(dataset, null, new StandardCategoryItemLabelGenerator());
	}

	public JFreeChart createBarChart(final CategoryDataset dataset, Sprint sprint,
			StandardCategoryItemLabelGenerator itemLabelGenerator) {

		final JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, false,
			true, false);

		final CategoryPlot plot = getChartBasicData(chart);
		setChartNumberAxis(chart);
		final CategoryItemRenderer renderer = getColorRenderer(dataset, sprint, plot);
		renderer.setSeriesItemLabelGenerator(0, itemLabelGenerator);
		renderer.setSeriesItemLabelGenerator(1, itemLabelGenerator);
		renderer.setBaseItemLabelsVisible(true);
		renderer.setSeriesPaint(0, COLOR_PAST_LINE);
		plot.setRenderer(renderer);

		return chart;
	}

	public JFreeChart createStackedBarChart(final CategoryDataset dataset) {

		final JFreeChart chart = ChartFactory.createStackedBarChart("", "", "", dataset, PlotOrientation.VERTICAL,
			true, true, false);

		final CategoryPlot plot = getChartBasicData(chart);
		setChartNumberAxis(chart);
		final CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
		renderer.setSeriesItemLabelGenerator(1, new StandardCategoryItemLabelGenerator());
		renderer.setSeriesItemLabelGenerator(2, new StandardCategoryItemLabelGenerator());
		renderer.setBaseItemLabelsVisible(true);
		renderer.setSeriesPaint(0, COLOR_PAST_LINE);
		renderer.setSeriesPaint(1, COLOR_PROJECTION_LINE);
		renderer.setSeriesPaint(2, COLOR_OPTIMUM_LINE);
		renderer.setSeriesItemLabelPaint(0, COLOR_OPTIMUM_LINE);
		renderer.setSeriesItemLabelPaint(1, COLOR_OPTIMUM_LINE);
		renderer.setSeriesItemLabelPaint(2, COLOR_PAST_LINE);

		return chart;
	}

	public JFreeChart createGanttChart(final TaskSeriesCollection dataset, int currentSprintColumn) {

		final JFreeChart chart = ChartFactory.createGanttChart("", "", "", dataset, false, true, false);

		final CategoryPlot plot = getChartBasicData(chart);
		plot.setDomainGridlinePaint(COLOR_PROJECTION_LINE);
		plot.setRangeGridlinePaint(COLOR_PROJECTION_LINE);

		final GanttRenderer renderer = new CustomGanttRenderer(currentSprintColumn);
		renderer.setSeriesPaint(0, COLOR_PROJECTION_LINE);
		renderer.setSeriesPaint(1, COLOR_OPTIMUM_LINE);
		plot.setRenderer(renderer);

		return chart;
	}

	@SuppressWarnings("unchecked")
	public JFreeChart createPieChart(final DefaultPieDataset dataset) {

		final JFreeChart chart = ChartFactory.createPieChart("", dataset, false, true, false);
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setNoDataMessage("No data available");
		plot.setLabelGap(0.02);
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1}"));
		plot.setForegroundAlpha(0.90f);
		// expode
		List<String> keys = dataset.getKeys();
		for (String key : keys) {
			// raise bugs
			if ("Bug".equals(key)) {
				plot.setExplodePercent(key, 0.15);
			} else {
				plot.setExplodePercent(key, 0.03);
			}
		}

		LegendTitle legend = new LegendTitle(plot);
		legend.setPosition(RectangleEdge.RIGHT);
		plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1}"));
		chart.addSubtitle(legend);

		return chart;
	}

	private CategoryItemRenderer getColorRenderer(final CategoryDataset dataset, Sprint sprint, final CategoryPlot plot) {
		return sprint != null ? new CustomRenderer(getColors(dataset, sprint)) : plot.getRenderer();
	}

	private CategoryPlot getChartBasicData(final JFreeChart chart) {
		chart.setBackgroundPaint(Color.WHITE);

		// get a reference to the plot for further customization...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setRangeGridlinesVisible(true);
		plot.setDomainGridlinesVisible(true);
		plot.setNoDataMessage("NO DATA!");
		plot.getDomainAxis().setMaximumCategoryLabelLines(2);

		return plot;
	}

	private void setChartNumberAxis(final JFreeChart chart) {
		final CategoryPlot plot = chart.getCategoryPlot();
		// change the margin at the top of the range axis...
		final ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		rangeAxis.setLowerMargin(0.15);
		rangeAxis.setUpperMargin(0.15);
	}

	public void setChartMarker(JFreeChart chart, int avg, int max) {
		final IntervalMarker target = new IntervalMarker(avg, max);
		target.setPaint(new Color(222, 222, 255, 128));
		chart.getCategoryPlot().addRangeMarker(target, Layer.BACKGROUND);
	}

	public void setUpperBoundary(JFreeChart chart, int max) {
		double upperBoundary = Math.min(max * 1.15f, max + 15);
		chart.getCategoryPlot().getRangeAxis().setUpperBound(upperBoundary);
	}

	public void createPic(OutputStream out, int width, int height, JFreeChart chart) {
		try {
			ChartUtilities.writeScaledChartAsPNG(out, chart, width, height, 1, 1);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setDateAxis(final TaskSeriesCollection dataset, final JFreeChart chart) {
		DateAxis dateAx = (DateAxis) chart.getCategoryPlot().getRangeAxis();
		dateAx.setUpperMargin(0.01);
		dateAx.setLowerMargin(0.01);
		Date maximumDate = new Date(dateAx.getMaximumDate());
		Date minimumDate = new Date(dateAx.getMinimumDate());
		int count = Math.max(20, minimumDate.getPeriodTo(maximumDate).toDays());
		if (count > 90) {
			dateAx.setTickUnit(new DateTickUnit(DateTickUnit.MONTH, 1, new SimpleDateFormat("MMM.yyyy")));
		} else if (count > 50) {
			dateAx.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 7, new SimpleDateFormat("dd.MMM")));
		} else if (count > 30) {
			dateAx.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 2, new SimpleDateFormat("dd")));
		} else {
			dateAx.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 1, new SimpleDateFormat("dd")));
		}
	}

	public void setDayDateAxis(final TaskSeries s1, final JFreeChart chart, Sprint sprint) {
		DateAxis dateAx = (DateAxis) chart.getCategoryPlot().getRangeAxis();
		dateAx.setUpperMargin(0.01);
		dateAx.setLowerMargin(0.01);
		dateAx.setRange(sprint.getBegin().toJavaDate(), sprint.getEnd().toJavaDate());
		if (s1.getItemCount() > 90) {
			dateAx.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 7, new SimpleDateFormat("MMM.yyyy")));
		} else if (s1.getItemCount() > 30) {
			dateAx.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 3, new SimpleDateFormat("dd.MMM")));
		} else if (s1.getItemCount() > 15) {
			dateAx.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 1, new SimpleDateFormat("dd")));
		} else {
			dateAx.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 1, new SimpleDateFormat("dd.MMM")));
		}
	}

	public Task getGanttTask(Sprint sprint, String label) {
		return new Task(label, new SimpleTimePeriod(sprint.getBegin().toJavaDate(), sprint.getEnd().toJavaDate()));
	}

	public Task getGanttTask(scrum.server.sprint.Task task, Date begin, Date end) {
		String label = task.getLabel().substring(0, Math.min(task.getLabel().length(), 40));
		// if it untouched than show empty line
		if (begin == null) { return new Task(label, new SimpleTimePeriod(end.toJavaDate(), end.toJavaDate())); }
		// want to see less than one day long tasks too
		if (begin.isAfterOrSame(end)) {
			end = end.addDays(1);
		}
		return new Task(label, new SimpleTimePeriod(begin.toJavaDate(), end.toJavaDate()));
	}

	private Paint[] getColors(final CategoryDataset dataset, Sprint sprint) {
		List<Paint> colors = new ArrayList<Paint>();
		colors.add(userColors.get(TEAM));
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
			if (row > 0) {
				// Color p = (Color) this.colors[column % this.colors.length];
				// return p.brighter();
				return COLOR_PROJECTION_LINE;
			}
			return this.colors[column % this.colors.length];
		}

		@Override
		public Paint getSeriesItemLabelPaint(int series) {
			return Color.BLACK;
		}

	}

	class CustomGanttRenderer extends GanttRenderer {

		private int currentSprintRow;

		public CustomGanttRenderer(int currentSprintRow) {
			this.currentSprintRow = currentSprintRow;
		}

		@Override
		public Paint getItemPaint(int row, int column) {
			if (column == currentSprintRow) { return COLOR_PAST_LINE; }
			return super.getItemPaint(row, column);
		}

	}

}
