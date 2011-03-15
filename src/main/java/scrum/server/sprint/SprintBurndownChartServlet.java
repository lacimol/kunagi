package scrum.server.sprint;

import ilarkesto.webapp.Servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scrum.client.project.ProjectOverviewWidget;
import scrum.server.ScrumWebApplication;
import scrum.server.WebSession;
import scrum.server.common.AHttpServlet;

public class SprintBurndownChartServlet extends AHttpServlet {

	@Override
	protected void onRequest(HttpServletRequest req, HttpServletResponse resp, WebSession session) throws IOException {
		String sprintId = req.getParameter("sprintId");
		String widthParam = req.getParameter("width");
		if (widthParam == null) widthParam = String.valueOf(ProjectOverviewWidget.CHART_WIDTH);
		String heightParam = req.getParameter("height");
		if (heightParam == null) heightParam = String.valueOf(ProjectOverviewWidget.CHART_HEIGHT);
		boolean isWorkChart = "workChart".equals(req.getParameter("chart"));
		boolean isEfficiencyChart = "efficiencyChart".equals(req.getParameter("chart"));
		boolean isAccomplishChart = "accomplishChart".equals(req.getParameter("chart"));
		boolean isVelocityChart = "velocityChart".equals(req.getParameter("chart"));
		boolean isSprintWorkChart = "sprintWorkChart".equals(req.getParameter("chart"));
		boolean isSprintRangeChart = "sprintRangeChart".equals(req.getParameter("chart"));
		boolean isCurrentSprintRangeChart = "currentSprintRangeChart".equals(req.getParameter("chart"));
		boolean isTaskRangeChart = "taskRangeChart".equals(req.getParameter("chart"));
		String userNameParam = req.getParameter("userName");
		String userName = (userNameParam == null || userNameParam.isEmpty() || "null".equals(userNameParam)) ? null
				: userNameParam;

		Servlet.preventCaching(resp);
		resp.setContentType("image/png");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int width = Integer.parseInt(widthParam);
		int height = Integer.parseInt(heightParam);
		if (isWorkChart) {
			// team or user burned hours
			ScrumWebApplication.get().getUserBurndownChart().writeChart(out, sprintId, width, height, userName);
		} else if (isEfficiencyChart) {
			// initial / burned hours at closed tasks
			ScrumWebApplication.get().getEfficiencyChart().writeChart(out, sprintId, width, height);
		} else if (isVelocityChart) {
			// velocity history
			ScrumWebApplication.get().getVelocityChart().writeChart(out, sprintId, width, height);
		} else if (isSprintWorkChart) {
			// sprint work history
			ScrumWebApplication.get().getSprintWorkChart().writeChart(out, sprintId, width, height);
		} else if (isSprintRangeChart) {
			// sprint range history
			ScrumWebApplication.get().getSprintRangeChart().writeChart(out, sprintId, width, height);
		} else if (isCurrentSprintRangeChart) {
			// current sprint range history
			ScrumWebApplication.get().getCurrentSprintRangeChart().writeChart(out, sprintId, width, height);
		} else if (isTaskRangeChart) {
			// task range history
			ScrumWebApplication.get().getTaskRangeChart().writeChart(out, sprintId, width, height);
		} else if (isAccomplishChart) {
			// burned hours per user
			ScrumWebApplication.get().getAccomplishChart().writeChart(out, sprintId, width, height);
		} else {
			// sprint burndown
			ScrumWebApplication.get().getBurndownChart().writeChart(out, sprintId, width, height);
		}

		resp.getOutputStream().write(out.toByteArray());
	}
}
