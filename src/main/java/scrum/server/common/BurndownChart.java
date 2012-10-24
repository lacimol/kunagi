package scrum.server.common;

import ilarkesto.core.time.Date;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;

import scrum.client.common.WeekdaySelector;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.SprintDaySnapshot;

public class BurndownChart extends Chart {

	public static byte[] createBurndownChartAsByteArray(Sprint sprint, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new BurndownChart().writeChart(out, sprint, width, height);
		return out.toByteArray();
	}

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {
		List<SprintDaySnapshot> snapshots = sprint.getDaySnapshots();
		if (snapshots.isEmpty()) {
			Date date = Date.today();
			date = Date.latest(date, sprint.getBegin());
			date = Date.earliest(date, sprint.getEnd());
			sprint.getDaySnapshot(date).updateWithCurrentSprint();
			snapshots = sprint.getDaySnapshots();
		}

		WeekdaySelector freeDays = sprint.getProject().getFreeDaysAsWeekdaySelector();

		writeSprintBurndownChart(out, snapshots, sprint, freeDays, width, height);
	}

	// private void writeProjectBurndownChart(OutputStream out, List<ProjectSprintSnapshot> snapshots, Date
	// firstDay,
	// Date lastDay, WeekdaySelector freeDays, int width, int height) {
	// List<BurndownSnapshot> burndownSnapshots = new ArrayList<BurndownSnapshot>(snapshots);
	// DefaultXYDataset data = createSprintBurndownChartDataset(burndownSnapshots, firstDay, lastDay);
	// double tick = 1.0;
	// double max = getMaximum(data);
	//
	// while (max / tick > 25) {
	// tick *= 2;
	// if (max / tick <= 25) break;
	// tick *= 2.5;
	// if (max / tick <= 25) break;
	// tick *= 2;
	// }
	//
	// JFreeChart chart = createSprintBurndownChart(data, "Date", "Work", firstDay, lastDay, 10, 30, max *
	// 1.1, tick,
	// freeDays);
	// try {
	// ChartUtilities.writeScaledChartAsPNG(out, chart, width, height, 1, 1);
	// } catch (IOException e) {
	// throw new RuntimeException(e);
	// }
	// }

	void writeSprintBurndownChart(OutputStream out, List<? extends BurndownSnapshot> snapshots, Sprint sprint,
			WeekdaySelector freeDays, int width, int height) {

		Date firstDay = sprint.getBegin();
		Date lastDay = sprint.getEnd();
		LOG.debug("Creating burndown chart:", snapshots.size(), "snapshots from", firstDay, "to", lastDay, "(" + width
				+ "x" + height + " px)");

		int dayCount = firstDay.getPeriodTo(lastDay).toDays();
		int dateMarkTickUnit = 1;
		float widthPerDay = (float) width / (float) dayCount * dateMarkTickUnit;
		while (widthPerDay < 20) {
			dateMarkTickUnit++;
			widthPerDay = (float) width / (float) dayCount * dateMarkTickUnit;
		}

		List<BurndownSnapshot> burndownSnapshots = new ArrayList<BurndownSnapshot>(snapshots);
		DefaultXYDataset data = createSprintBurndownChartDataset(burndownSnapshots, sprint, freeDays);

		int maxData = Math.max((int) getMaximum(data), sprint.getMaxWorkingHoursForTeam());
		JFreeChart chart = createXYLineChart(sprint, dateMarkTickUnit, widthPerDay, data, maxData, height);
		createPic(out, width, height, chart);
	}

	private DefaultXYDataset createSprintBurndownChartDataset(final List<BurndownSnapshot> snapshots,
			final Sprint sprint, final WeekdaySelector freeDays) {

		ChartDataFactory factory = new ChartDataFactory();
		factory.createDataset(snapshots, sprint, freeDays);
		return factory.getDataset();
	}

	private class ChartDataFactory {

		List<Double> mainDates = new ArrayList<Double>();
		List<Double> mainValues = new ArrayList<Double>();

		List<Double> extrapolationDates = new ArrayList<Double>();
		List<Double> extrapolationValues = new ArrayList<Double>();

		List<Double> idealDates = new ArrayList<Double>();
		List<Double> idealValues = new ArrayList<Double>();

		List<BurndownSnapshot> snapshots;
		WeekdaySelector freeDays;

		Date date;
		long millisBegin;
		long millisEnd;
		boolean freeDay;
		BurndownSnapshot snapshot;
		boolean workStarted;
		boolean workFinished;

		int totalBurned;
		int totalBefore;
		int totalAfter;
		int burned;
		int jump;
		double totalRemaining;
		int workDays;
		double burnPerDay;
		double idealRemaining;
		double idealBurnPerDay;
		int totalWorkDays = 0;
		boolean extrapolationFinished;

		DefaultXYDataset dataset;

		public void createDataset(final List<BurndownSnapshot> snapshots, final Sprint sprint,
				final WeekdaySelector freeDays) {

			Date firstDay = sprint.getBegin();
			Date lastDay = sprint.getEnd();

			this.snapshots = snapshots;
			this.freeDays = freeDays;

			totalWorkDays = sprint.getWorkDaysInSprint();

			setDate(firstDay);
			while (true) {
				if (!workFinished) {
					burned = snapshot.getBurnedWorkTotal() - totalBurned;
					totalBurned = snapshot.getBurnedWorkTotal();
					totalAfter = snapshot.getRemainingWork();
					jump = totalAfter - totalBefore + burned;
				}

				// System.out.println(date + " totalBefore=" + totalBefore + " totalAfter=" + totalAfter +
				// " jump=" + jump
				// + " burned=" + burned + " -> " + snapshot);
				if (workFinished) {
					processSuffix();
				} else if (workStarted) {
					processCenter();
				} else {
					processPrefix();
				}

				if (date.equals(lastDay)) break;

				setDate(date.nextDay());
				totalBefore = totalAfter;
			}

			dataset = new DefaultXYDataset();
			dataset.addSeries("Main", toArray(mainDates, mainValues));
			dataset.addSeries("Extrapolation", toArray(extrapolationDates, extrapolationValues));
			dataset.addSeries("Ideal", toArray(idealDates, idealValues));
		}

		private void setDate(Date newDate) {
			date = newDate;
			millisBegin = date.toMillis();
			millisEnd = date.nextDay().toMillis();
			freeDay = freeDays.isFree(date.getWeekday().getDayOfWeek());
			if (!workFinished) snapshot = getSnapshot();
		}

		private void processPrefix() {
			if (totalAfter > 0 || totalBurned > 0) {
				workStarted = true;
				idealRemaining = totalAfter + burned;
				idealDates.add((double) millisBegin);
				idealValues.add(idealRemaining);

				if (totalWorkDays > 0) {
					idealBurnPerDay = (double) jump / (double) totalWorkDays;
					// System.out.println("*** jump:" + jump + " totalWorkDays:" + totalWorkDays +
					// " idealBurnPerDay:"
					// + idealBurnPerDay);
				}

				processCenter();
				return;
			}
			totalWorkDays--;
		}

		private void processCenter() {
			mainDates.add((double) millisBegin);
			mainValues.add((double) totalBefore);
			if (jump != 0) {
				mainDates.add((double) millisBegin);
				mainValues.add((double) totalBefore + jump);
			}
			mainDates.add((double) millisEnd);
			mainValues.add((double) totalAfter);

			if (!freeDay) {
				workDays++;
				idealRemaining -= idealBurnPerDay;
			}

			idealDates.add((double) millisEnd);
			idealValues.add(idealRemaining);
		}

		private void processSuffix() {
			if (!freeDay) {
				totalRemaining -= burnPerDay;
				idealRemaining -= idealBurnPerDay;
			}
			if (!extrapolationFinished) {
				extrapolationDates.add((double) millisEnd);
				extrapolationValues.add(totalRemaining);
			}
			idealDates.add((double) millisEnd);
			idealValues.add(idealRemaining);

			if (totalRemaining <= 0) extrapolationFinished = true;
		}

		private BurndownSnapshot getSnapshot() {
			for (BurndownSnapshot snapshot : snapshots) {
				if (snapshot.getDate().equals(date)) return snapshot;
			}
			workFinished = true;
			totalRemaining = totalAfter;
			burnPerDay = (double) totalBurned / (double) workDays;
			// System.out.println("***** totalBurned:" + totalBurned + " workDays:" + workDays +
			// " burnPerDay:"
			// + burnPerDay);
			extrapolationDates.add((double) millisBegin);
			extrapolationValues.add(totalRemaining);
			return null;
		}

		public DefaultXYDataset getDataset() {
			return dataset;
		}

	}

}
