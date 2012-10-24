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
package scrum.server.sprint;

import ilarkesto.webapp.RequestWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import scrum.client.project.ProjectOverviewWidget;
import scrum.server.ScrumWebApplication;
import scrum.server.WebSession;
import scrum.server.common.AKunagiServlet;
import scrum.server.common.Chart;
import scrum.server.common.UserBurndownChart;

public class SprintBurndownChartServlet extends AKunagiServlet {

	@Override
	protected void onRequest(RequestWrapper<WebSession> req) throws IOException {

		String sprintId = req.get("sprintId");
		String widthParam = req.get("width");
		if (widthParam == null) widthParam = String.valueOf(ProjectOverviewWidget.CHART_WIDTH);
		String heightParam = req.get("height");
		if (heightParam == null) heightParam = String.valueOf(ProjectOverviewWidget.CHART_HEIGHT);
		String chartName = req.get("chart");
		String userNameParam = req.get("userName");
		String userName = (userNameParam == null || userNameParam.isEmpty() || "null".equals(userNameParam)) ? null
				: userNameParam;

		req.preventCaching();
		req.setContentType("image/png");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int width = Integer.parseInt(widthParam);
		int height = Integer.parseInt(heightParam);

		Chart chart = getChart(chartName);
		if ("userBurndownChart".equals(chartName)) {
			((UserBurndownChart) chart).writeChart(out, sprintId, width, height, userName);
		} else {
			chart.writeChart(out, sprintId, width, height);
		}

		req.write(out.toByteArray());
	}

	private Chart getChart(String chartName) {

		ScrumWebApplication scrumWebApp = ScrumWebApplication.get();
		Chart chart = scrumWebApp.getBurndownChart();

		try {
			if (chartName != null) {
				String methodName = "get" + chartName.substring(0, 1).toUpperCase().concat(chartName.substring(1));
				Method method = scrumWebApp.getClass().getMethod(methodName, new Class[] {});
				chart = (Chart) method.invoke(scrumWebApp, new Object[] {});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		chart.setSprintDao(scrumWebApp.getSprintDao());
		return chart;

	}
}
