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
package scrum.client.admin;

import ilarkesto.core.time.Date;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;

public class SystemConfig extends GSystemConfig {

	public SystemConfig(Map data) {
		super(data);
	}

	public List<Date> getHolidayDates() {

		List<Date> dates = new ArrayList<Date>();
		if (getHolidays() != null) {
			String[] holidays = getHolidays().split(";");
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
			DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy.MM.dd");

			for (String holiday : holidays) {
				dates.add(new Date(dtf.parse(holiday)));
			}
		}
		return dates;

	}

}