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

import ilarkesto.core.time.Date;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;

import scrum.server.ScrumWebApplication;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.Task;
import scrum.server.task.TaskDaySnapshot;

public class UserBurndownChart extends Chart {

	private static Integer teamMembersCount;

	private String userName;

	public void writeChart(OutputStream out, Sprint sprint, int width, int height, String userName) {
		this.userName = userName;
		writeChart(out, sprint, width, height);
	}

	public void writeChart(OutputStream out, String sprintId, int width, int height, String userName) {
		Sprint sprint = sprintDao.getById(sprintId);
		if (sprint == null) throw new IllegalArgumentException("Sprint " + sprintId + " does not exist.");
		this.userName = userName;
		writeChart(out, sprint, width, height);
	}

	@Override
	public void writeChart(OutputStream out, Sprint sprint, int width, int height) {

		Date firstDay = sprint.getBegin();
		Date lastDay = sprint.getEnd();
		teamMembersCount = sprint.getProject().getTeamMembersCount();

		Map<String, Integer> userBurnedHours = getUserBurnedHours(sprint, firstDay, lastDay);

		LOG.debug("Creating burndown chart for", userName == null ? "team" : userName, ", size:",
			userBurnedHours.size(), "userBurnedHours from", firstDay, "to", lastDay, "(" + width + "x" + height
					+ " px)");

		int dayCount = firstDay.getPeriodTo(lastDay).toDays();
		int dateMarkTickUnit = 1;
		float widthPerDay = (float) width / (float) dayCount * dateMarkTickUnit;
		while (widthPerDay < 20) {
			dateMarkTickUnit++;
			widthPerDay = (float) width / (float) dayCount * dateMarkTickUnit;
		}

		JFreeChart chart = createSprintBurndownChart(userBurnedHours, sprint, dateMarkTickUnit, widthPerDay, height);
		createPic(out, width, height, chart);
	}

	private Map<String, Integer> getUserBurnedHours(Sprint sprint, Date firstDay, Date lastDay) {
		List<Task> sprintTasks = new LinkedList<Task>(sprint.getProject().getTasks());
		Map<String, Integer> userBurnedHours = new HashMap<String, Integer>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		while (firstDay.isBeforeOrSame(lastDay)) {
			userBurnedHours.put(dateFormat.format(firstDay.toJavaDate()), 0);
			firstDay = firstDay.nextDay();
		}

		String day;
		int burnedWork;
		Integer burnedThatDay;
		for (Task task : sprintTasks) {
			if (userName == null || task.isOwnersTask(userName)) {
				int previousSnapshotBurn = 0;
				for (TaskDaySnapshot snapshot : task.getTaskDaySnapshots(sprint)) {
					day = dateFormat.format(snapshot.getDate().toJavaDate());
					burnedWork = snapshot.getBurnedWork();
					if (previousSnapshotBurn != 0 && burnedWork > 0) {
						burnedWork -= previousSnapshotBurn;
					}
					burnedThatDay = userBurnedHours.get(day);
					userBurnedHours.put(day, burnedThatDay == null ? burnedWork : burnedWork + burnedThatDay);
					previousSnapshotBurn = snapshot.getBurnedWork();
				}
			}
		}

		Comparator<String> comparer = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		};
		Map<String, Integer> sorted = new TreeMap<String, Integer>(comparer);
		sorted.putAll(userBurnedHours);

		return sorted;
	}

	private JFreeChart createSprintBurndownChart(Map<String, Integer> userBurnedHours, Sprint sprint,
			int dateMarkTickUnit, float widthPerDay, int height) {

		Date firstDay = sprint.getBegin();
		Date lastDay = sprint.getEnd();

		DefaultXYDataset data = null;
		double max = 0;
		data = createWorkDataset(sprint, userBurnedHours, firstDay, lastDay);
		max = Math.max(max, UserBurndownChart.getMaximum(data));

		return createXYLineChart(sprint, dateMarkTickUnit, widthPerDay, data, max, height);
	}

	public DefaultXYDataset createWorkDataset(Sprint sprint, final Map<String, Integer> userBurnedHours,
			final Date firstDay, final Date lastDay) {

		List<Double> mainDates = new ArrayList<Double>();
		List<Double> mainValues = new ArrayList<Double>();

		List<Double> idealDates = new ArrayList<Double>();
		List<Double> idealValues = new ArrayList<Double>();

		List<Double> avgDates = new ArrayList<Double>();
		List<Double> avgValues = new ArrayList<Double>();

		DefaultXYDataset dataset;

		long millisBegin;
		long millisEnd;

		double average = 0;
		double all = 0;
		double workDays = 0;

		int workingHoursPerDay = ScrumWebApplication.get().getSystemConfig().getWorkingHoursPerDay();
		Double idealWorkingHours = (double) (userName != null ? workingHoursPerDay : workingHoursPerDay
				* teamMembersCount);

		for (Entry<String, Integer> entry : userBurnedHours.entrySet()) {
			// System.out.println(entry.getKey() + ": " + entry.getValue());
			Date date = new Date(entry.getKey());
			millisBegin = date.toMillis();
			millisEnd = date.nextDay().toMillis();

			mainDates.add((double) millisBegin);
			mainValues.add(entry.getValue().doubleValue());
			mainDates.add((double) millisEnd);
			mainValues.add(entry.getValue().doubleValue());
			all += entry.getValue().doubleValue();

			if (!sprint.getProject().isFreeDay(date)) {
				idealDates.add((double) millisBegin);
				idealValues.add(idealWorkingHours);
				idealDates.add((double) millisEnd);
				idealValues.add(idealWorkingHours);
				if (entry.getValue() > 0) {
					workDays++;
				}
			} else {
				idealDates.add((double) millisBegin);
				idealValues.add(0.0);
				idealDates.add((double) millisEnd);
				idealValues.add(0.0);
			}
		}

		// average
		average = all / workDays;
		for (Entry<String, Integer> entry : userBurnedHours.entrySet()) {
			Date date = new Date(entry.getKey());
			millisBegin = date.toMillis();
			millisEnd = date.nextDay().toMillis();
			if (!sprint.getProject().isFreeDay(date)) {
				avgDates.add((double) millisBegin);
				avgValues.add(average);
				avgDates.add((double) millisEnd);
				avgValues.add(average);
			} else {
				avgDates.add((double) millisBegin);
				avgValues.add(0.0);
				avgDates.add((double) millisEnd);
				avgValues.add(0.0);
			}
		}

		dataset = new DefaultXYDataset();
		dataset.addSeries("Main", toArray(mainDates, mainValues));
		dataset.addSeries("Ideal", toArray(idealDates, idealValues));
		dataset.addSeries("Average", toArray(avgDates, avgValues));
		return dataset;
	}

}
