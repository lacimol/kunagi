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

public class UserWorkWidget extends ChartWidget {

	private String userName;

	public UserWorkWidget() {}

	public UserWorkWidget(String userName) {
		this.userName = userName;
	}

	@Override
	String getChartUrl(int width) {
		int height = userName == null ? TEAM_CHART_HEIGHT : USER_CHART_HEIGHT;
		return getCurrentSprint().getUserChartUrl(width, height, "userBurndown", userName);
	}

}
