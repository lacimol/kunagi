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
import scrum.server.common.Chart;
import scrum.server.common.UserBurndownChart;

public class SprintBurndownChartServlet extends AHttpServlet {

	@Override
	protected void onRequest(HttpServletRequest req, HttpServletResponse resp, WebSession session) throws IOException {

		String sprintId = req.getParameter("sprintId");
		String widthParam = req.getParameter("width");
		if (widthParam == null) widthParam = String.valueOf(ProjectOverviewWidget.CHART_WIDTH);
		String heightParam = req.getParameter("height");
		if (heightParam == null) heightParam = String.valueOf(ProjectOverviewWidget.CHART_HEIGHT);
		String chartName = req.getParameter("chart");
		String userNameParam = req.getParameter("userName");
		String userName = (userNameParam == null || userNameParam.isEmpty() || "null".equals(userNameParam)) ? null
				: userNameParam;

		Servlet.preventCaching(resp);
		resp.setContentType("image/png");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int width = Integer.parseInt(widthParam);
		int height = Integer.parseInt(heightParam);

		Chart chart = getChart(chartName);
		if ("workChart".equals(chartName)) {
			((UserBurndownChart) chart).writeChart(out, sprintId, width, height, userName);
		} else {
			chart.writeChart(out, sprintId, width, height);
		}

		resp.getOutputStream().write(out.toByteArray());
	}

	protected Chart getChart(String chartName) {

		Chart chart = null;
		ScrumWebApplication scrumWebApp = ScrumWebApplication.get();
		boolean isWorkChart = "workChart".equals(chartName);
		boolean isEfficiencyChart = "efficiencyChart".equals(chartName);
		boolean isAccomplishChart = "accomplishChart".equals(chartName);
		boolean isVelocityChart = "velocityChart".equals(chartName);
		boolean isSprintWorkChart = "sprintWorkChart".equals(chartName);
		boolean isSprintRangeChart = "sprintRangeChart".equals(chartName);
		boolean isCurrentSprintRangeChart = "currentSprintRangeChart".equals(chartName);
		boolean isTaskRangeChart = "taskRangeChart".equals(chartName);
		boolean isStoryThemeChart = "storyThemeChart".equals(chartName);

		if (isWorkChart) {
			// team or user burned hours
			chart = scrumWebApp.getUserBurndownChart();
		} else if (isEfficiencyChart) {
			// initial / burned hours at closed tasks
			chart = scrumWebApp.getEfficiencyChart();
		} else if (isVelocityChart) {
			// velocity history
			chart = scrumWebApp.getVelocityChart();
		} else if (isSprintWorkChart) {
			// sprint work history
			chart = scrumWebApp.getSprintWorkChart();
		} else if (isSprintRangeChart) {
			// sprint range history
			chart = scrumWebApp.getSprintRangeChart();
		} else if (isCurrentSprintRangeChart) {
			// current sprint range history
			chart = scrumWebApp.getCurrentSprintRangeChart();
		} else if (isTaskRangeChart) {
			// task range history
			chart = scrumWebApp.getTaskRangeChart();
		} else if (isAccomplishChart) {
			// burned hours per user
			chart = scrumWebApp.getAccomplishChart();
		} else if (isStoryThemeChart) {
			// story theme pie chart
			chart = scrumWebApp.getStoryThemeChart();
		} else {
			// sprint burndown
			chart = scrumWebApp.getBurndownChart();
		}
		return chart;

	}
}
