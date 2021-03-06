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

import com.google.gwt.user.client.Window;

public class TwoThirdsChartWidget extends ChartWidget {

	@Override
	String getChartUrl(int width) {
		return "";
	}

	@Override
	public int getChartWidth() {
		int width = Window.getClientWidth() - 280;
		width -= width / 3;
		if (width < 100) width = 100;
		return width;
	}

}
