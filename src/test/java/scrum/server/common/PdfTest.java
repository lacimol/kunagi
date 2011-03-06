package scrum.server.common;

import ilarkesto.base.time.Date;
import ilarkesto.core.logging.Log;
import ilarkesto.integration.itext.PdfBuilder;
import ilarkesto.testng.ATest;

import java.io.File;
import java.util.Arrays;

import org.testng.annotations.Test;

import scrum.TestUtil;
import scrum.server.calendar.CalendarPdfCreator;
import scrum.server.collaboration.Wikipage;
import scrum.server.collaboration.WikipagePdfCreator;
import scrum.server.impediments.ImpedimentListPdfCreator;
import scrum.server.project.ProductBacklogPdfCreator;
import scrum.server.project.Project;
import scrum.server.project.Requirement;
import scrum.server.risks.RiskListPdfCreator;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.SprintBacklogPdfCreator;
import scrum.server.sprint.SprintReportPdfCreator;

public class PdfTest extends ATest {

	@Test
	public void calendar() {
		Project project = TestUtil.createProject(TestUtil.getDuke());

		TestUtil.createSimpleEvent(project, 0);
		TestUtil.createSimpleEvent(project, 10);
		TestUtil.createSimpleEvent(project, 20);

		createPdf(new CalendarPdfCreator(project, Date.today(), Date.inDays(30)));
	}

	@Test
	public void riskList() {
		Project project = TestUtil.createProject();

		TestUtil.createRisk(project, 1);
		TestUtil.createRisk(project, 2);
		TestUtil.createRisk(project, 3);
		TestUtil.createRisk(project, 4);
		TestUtil.createRisk(project, 5);

		createPdf(new RiskListPdfCreator(project));
	}

	@Test
	public void impedimentList() {
		Project project = TestUtil.createProject();

		TestUtil.createImpediment(project, 1);
		TestUtil.createImpediment(project, 2);
		TestUtil.createImpediment(project, 3);
		TestUtil.createImpediment(project, 4);
		TestUtil.createImpediment(project, 5);

		createPdf(new ImpedimentListPdfCreator(project));
	}

	@Test
	public void sprintBacklog() {
		Project project = TestUtil.createProject();

		Sprint sprint = TestUtil.createSprint(project, Date.today());
		project.setCurrentSprint(sprint);

		Requirement req1 = TestUtil.createRequirement(project, 1);
		req1.setSprint(sprint);

		Requirement req2 = TestUtil.createRequirement(project, 2);
		req2.setSprint(sprint);

		createPdf(new SprintBacklogPdfCreator(project));
	}

	@Test
	public void productBacklog() {
		Project project = TestUtil.createProject();

		Requirement req1 = TestUtil.createRequirement(project, 1);
		Requirement req2 = TestUtil.createRequirement(project, 2);

		createPdf(new ProductBacklogPdfCreator(project));
	}

	@Test
	public void sprintReport() {
		Sprint sprint = new Sprint();
		sprint.setProject(TestUtil.createProject());
		sprint.setEnd(Date.today().beforeDays(3));
		sprint.setBegin(sprint.getEnd().beforeDays(30));
		sprint.setLabel("Productivity Boost Sprint ä ü ö ß Mirosław");
		sprint.setGoal("Boost productivity for users.");
		sprint.setPlanningNote("Planning was fun.");
		sprint.setReviewNote("PO accepted everything.");
		sprint.setRetrospectiveNote("Perfect sprint.");
		sprint.setVelocity(666f);
		sprint.setProductOwners(Arrays.asList(TestUtil.createUser("Cartman")));
		sprint.setScrumMasters(Arrays.asList(TestUtil.createUser("Homer")));
		sprint.setTeamMembers(Arrays.asList(TestUtil.createUser("Alfred"), TestUtil.createUser("Duke")));

		Requirement req1 = TestUtil.createRequirement(sprint, 1, 0.5f);
		TestUtil.createTask(req1, 1, 0).setBurnedWork(1);
		req1.setClosed(true);
		Requirement req2 = TestUtil.createRequirement(sprint, 1, 1f);
		Requirement req3 = TestUtil.createRequirement(sprint, 1, 5f);
		TestUtil.createTask(req3, 2, 1).setBurnedWork(1);
		TestUtil.createTask(req3, 3, 2).setBurnedWork(2);

		sprint.close();

		createPdf(new SprintReportPdfCreator(sprint));
	}

	@Test
	public void sprintReportWithStats() {
		Sprint sprint = new ChartTest().createTestProjectForCharts().getCurrentSprint();
		// sprint.close();
		createPdf(new SprintReportPdfCreator(sprint));
	}

	@Test
	public void wikipage() {
		Wikipage wikipage = new Wikipage();
		wikipage.setName("wikipage");
		wikipage.setText("= Test Wiki Page =\n\nTest wiki page.\nTest wiki page. Test wiki page. Test wiki page. Test wiki page. Test wiki page. Test wiki page. Test wiki page. Test wiki page. Test wiki page. ");
		createPdf(new WikipagePdfCreator(wikipage));
	}

	private void createPdf(APdfCreator creator) {
		PdfBuilder pdf = new PdfBuilder();
		creator.build(pdf);
		File file = new File(OUTPUT_DIR + "/" + creator.getFilename() + ".pdf");
		Log.DEBUG("Writing PDF:", file);
		pdf.write(file);
	}

}
