package scrum.server.common;

import ilarkesto.base.Sys;
import ilarkesto.base.time.Date;
import ilarkesto.io.IO;
import ilarkesto.testng.ATest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import scrum.client.common.WeekdaySelector;
import scrum.server.sprint.SprintDaySnapshot;

public class BurndownChartTest extends ATest {

	@BeforeSuite
	public void init() {
		Sys.setHeadless(true);
	}

	@Test
	public void sprintBurndown() throws IOException {
		// WeekdaySelector freeDays = new WeekdaySelector(65);
		WeekdaySelector freeDays = new WeekdaySelector(1);

		List<BurndownSnapshot> shots = new ArrayList<BurndownSnapshot>();

		// shots.add(shot(new Date(2008, 7, 1), 0, 0));
		// shots.add(shot(new Date(2008, 7, 2), 0, 0));
		// shots.add(shot(new Date(2008, 7, 3), 0, 0));
		// shots.add(shot(new Date(2008, 7, 4), 5, 45));
		// shots.add(shot(new Date(2008, 7, 5), 10, 35));
		// shots.add(shot(new Date(2008, 7, 6), 0, 35));
		// shots.add(shot(new Date(2008, 7, 7), 18, 25));
		// shots.add(shot(new Date(2008, 7, 8), 10, 15));
		// shots.add(shot(new Date(2008, 7, 9), 2, 15));

		// shots.add(shot(new Date(2008, 7, 1), 0, 0));
		// shots.add(shot(new Date(2008, 7, 2), 0, 5));
		// shots.add(shot(new Date(2008, 7, 3), 1, 4));
		// shots.add(shot(new Date(2008, 7, 4), 1, 5));
		// shots.add(shot(new Date(2008, 7, 5), 2, 3));
		// shots.add(shot(new Date(2008, 7, 6), 1, 2));
		// shots.add(shot(new Date(2008, 7, 7), 2, 0));
		// shots.add(shot(new Date(2008, 7, 8), 0, 0));

		// shots.add(shot(new Date(2008, 7, 1), 0, 5));
		// shots.add(shot(new Date(2008, 7, 2), 2, 3));

		shots.add(shot(new Date(2008, 7, 1), 0, 5));
		shots.add(shot(new Date(2008, 7, 2), 2, 4));
		shots.add(shot(new Date(2008, 7, 3), 2, 4));
		shots.add(shot(new Date(2008, 7, 4), 2, 4));

		Date sprintBeginDate = shots.get(0).getDate();
		Date sprintEndDate = shots.get(shots.size() - 1).getDate().addDays(23);

		File file = new File(OUTPUT_DIR + "/sprintBurndownChart.png");
		IO.createDirectory(file.getParentFile());
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		new BurndownChart().writeSprintBurndownChart(out, shots, sprintBeginDate, sprintEndDate, freeDays, 1000, 500);
		out.close();
	}

	private static SprintDaySnapshot shot(Date d, int b, int r) {
		SprintDaySnapshot s = new SprintDaySnapshot();
		try {
			s.setDate(d);
		} catch (NullPointerException e) {}
		try {
			s.setBurnedWork(b);
		} catch (NullPointerException e) {}
		try {
			s.setRemainingWork(r);
		} catch (NullPointerException e) {}
		return s;
	}

}
