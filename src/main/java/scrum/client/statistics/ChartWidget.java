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

import scrum.client.common.AScrumWidget;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public abstract class ChartWidget extends AScrumWidget {

	public static final int TEAM_CHART_HEIGHT = 200;
	public static final int USER_CHART_HEIGHT = 120;

	public static final int DEFAULT_CHART_WIDTH = 200;
	public static final int MIN_CHART_WIDTH = 100;
	public static final int MENU_WIDTH = 280;

	private Image sprintChart;

	@Override
	protected Widget onInitialization() {
		sprintChart = new Image(getChartUrl(DEFAULT_CHART_WIDTH));
		return sprintChart;
	}

	@Override
	protected void onUpdate() {
		int width = getChartWidth();
		sprintChart.setWidth(width + "px");
		sprintChart.setUrl(getChartUrl(width) + "&timestamp=" + System.currentTimeMillis());
	}

	abstract String getChartUrl(int width);

	public int getChartWidth() {
		int width = Window.getClientWidth() - MENU_WIDTH;
		width = width / 2;
		if (width < MIN_CHART_WIDTH) width = MIN_CHART_WIDTH;
		return width;
	}
}
