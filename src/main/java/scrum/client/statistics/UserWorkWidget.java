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

package scrum.client.statistics;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class UserWorkWidget extends ChartWidget {

	public static final int CHART_WIDTH = 800;
	public static final int CHART_HEIGHT = 100;

	private Image sprintChart;
	private String userName;

	public UserWorkWidget() {}

	public UserWorkWidget(String userName) {
		this.userName = userName;
	}

	@Override
	protected Widget onInitialization() {
		sprintChart = new Image(getChartUrl(200));
		return sprintChart;
	}

	@Override
	protected void onUpdate() {
		int width = getChartWidth();
		sprintChart.setWidth(width + "px");
		sprintChart.setUrl(getChartUrl(width) + "&timestamp=" + System.currentTimeMillis());
	}

	private String getChartUrl(int width) {
		return getCurrentSprint().getUserChartUrl(width, CHART_HEIGHT, userName);
	}

}
