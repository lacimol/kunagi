package scrum.server.sprint;

import ilarkesto.base.Utl;
import ilarkesto.testng.ATest;

import org.testng.annotations.Test;

public class SprintHistoryHelperTest extends ATest {

	@Test
	public void parseLines() {
		assertEquals(SprintHistoryHelper.parseLines("a"), Utl.toList("a"));
		assertEquals(SprintHistoryHelper.parseLines("line1\nline2"), Utl.toList("line1", "line2"));
		assertEquals(SprintHistoryHelper.parseLines("line1\n\nline3"), Utl.toList("line1", "", "line3"));
		assertEquals(SprintHistoryHelper.parseLines(""), Utl.toList(""));
	}

}
