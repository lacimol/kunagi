/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
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

import ilarkesto.base.PermissionDeniedException;
import ilarkesto.base.time.Date;
import ilarkesto.integration.itext.PdfBuilder;
import ilarkesto.webapp.Servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scrum.server.WebSession;
import scrum.server.calendar.CalendarPdfCreator;
import scrum.server.collaboration.Wikipage;
import scrum.server.collaboration.WikipagePdfCreator;
import scrum.server.impediments.ImpedimentListPdfCreator;
import scrum.server.issues.BugListPdfCreator;
import scrum.server.issues.IdeaListPdfCreator;
import scrum.server.project.ProductBacklogPdfCreator;
import scrum.server.project.QualityBacklogPdfCreator;
import scrum.server.release.ReleaseHistoryPdfCreator;
import scrum.server.release.ReleasePlanPdfCreator;
import scrum.server.risks.RiskListPdfCreator;
import scrum.server.sprint.Sprint;
import scrum.server.sprint.SprintBacklogPdfCreator;
import scrum.server.sprint.SprintReportPdfCreator;

public class PdfServlet extends AHttpServlet {

	private APdfCreator getPdfCreator(String pdfId, HttpServletRequest req, WebSession session) {
		if (pdfId.equals("sprintReport")) return createSprintReport(req, session);
		if (pdfId.equals("wikipage")) return createWikipage(req, session);
		if (pdfId.equals("productBacklog")) return createProductBacklog(req, session);
		if (pdfId.equals("sprintBacklog")) return createSprintBacklog(req, session);
		if (pdfId.equals("qualityBacklog")) return createQualityBacklog(req, session);
		if (pdfId.equals("impedimentList")) return createImpedimentList(req, session);
		if (pdfId.equals("riskList")) return createRiskList(req, session);
		if (pdfId.equals("calendar")) return createCalendar(req, session);
		if (pdfId.equals("bugList")) return createBugList(req, session);
		if (pdfId.equals("ideaList")) return createIdeaList(req, session);
		if (pdfId.equals("releasePlan")) return createReleasePlan(req, session);
		if (pdfId.equals("releaseHistory")) return createReleaseHistory(req, session);
		throw new RuntimeException("Unknown pdfId: " + pdfId);
	}

	private APdfCreator createCalendar(HttpServletRequest req, WebSession session) {
		Date from = new Date(req.getParameter("from"));
		Date to = new Date(req.getParameter("to"));
		return new CalendarPdfCreator(getProject(session, req), from, to);
	}

	private APdfCreator createReleasePlan(HttpServletRequest req, WebSession session) {
		return new ReleasePlanPdfCreator(getProject(session, req));
	}

	private APdfCreator createReleaseHistory(HttpServletRequest req, WebSession session) {
		return new ReleaseHistoryPdfCreator(getProject(session, req));
	}

	private APdfCreator createRiskList(HttpServletRequest req, WebSession session) {
		return new RiskListPdfCreator(getProject(session, req));
	}

	private APdfCreator createImpedimentList(HttpServletRequest req, WebSession session) {
		return new ImpedimentListPdfCreator(getProject(session, req));
	}

	private APdfCreator createSprintBacklog(HttpServletRequest req, WebSession session) {
		return new SprintBacklogPdfCreator(getProject(session, req));
	}

	private APdfCreator createQualityBacklog(HttpServletRequest req, WebSession session) {
		return new QualityBacklogPdfCreator(getProject(session, req));
	}

	private APdfCreator createProductBacklog(HttpServletRequest req, WebSession session) {
		return new ProductBacklogPdfCreator(getProject(session, req));
	}

	private APdfCreator createBugList(HttpServletRequest req, WebSession session) {
		return new BugListPdfCreator(getProject(session, req));
	}

	private APdfCreator createIdeaList(HttpServletRequest req, WebSession session) {
		return new IdeaListPdfCreator(getProject(session, req));
	}

	private APdfCreator createWikipage(HttpServletRequest req, WebSession session) {
		Wikipage wikipage = getEntityByParameter(req, Wikipage.class);
		if (!wikipage.isProject(getProject(session, req))) throw new PermissionDeniedException();
		return new WikipagePdfCreator(wikipage);
	}

	private APdfCreator createSprintReport(HttpServletRequest req, WebSession session) {
		Sprint sprint = getEntityByParameter(req, Sprint.class);
		if (!sprint.isProject(getProject(session, req))) throw new PermissionDeniedException();
		return new SprintReportPdfCreator(sprint);
	}

	@Override
	protected void onRequest(HttpServletRequest req, HttpServletResponse resp, WebSession session) throws IOException {
		String pdfId = req.getParameter("pdfId");
		if (pdfId == null) throw new RuntimeException("pdfId==null");
		APdfCreator creator = getPdfCreator(pdfId, req, session);

		resp.setContentType("application/pdf");
		Servlet.setFilename(creator.getFilename() + ".pdf", resp);

		PdfBuilder pdf = new PdfBuilder();
		creator.build(pdf);
		pdf.write(resp.getOutputStream());
	}

}
