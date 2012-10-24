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
package scrum.server.admin;

import ilarkesto.base.Str;
import ilarkesto.core.time.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import scrum.server.KunagiRootConfig;
import scrum.server.ScrumWebApplication;

public class SystemConfig extends GSystemConfig {

	// --- dependencies ---

	private static transient ScrumWebApplication webApplication;

	public static void setWebApplication(ScrumWebApplication webApplication) {
		SystemConfig.webApplication = webApplication;
	}

	// --- ---

	public String getInstanceNameWithApplicationLabel() {
		if (!isInstanceNameSet()) return webApplication.getApplicationLabel();
		return webApplication.getApplicationLabel() + " @ " + getInstanceName();
	}

	@Override
	public boolean isVisibleFor(User user) {
		return true;
	}

	@Override
	public boolean isEditableBy(User user) {
		if (user == null) return false;
		return user.isAdmin();
	}

	@Override
	protected String prepareUrl(String url) {
		if (url != null && !url.endsWith("/")) url += "/";
		return super.prepareUrl(url);
	}

	@Override
	public void ensureIntegrity() {
		super.ensureIntegrity();
		if (!isDefaultUserPasswordSet()) setDefaultUserPassword(config.getInitialPassword());
		if (!isMaxFileSizeSet()) setMaxFileSize(20);
		if (!isSubscriptionKeySeedSet()) setSubscriptionKeySeed(Str.generatePassword(32));
		if (!isSmtpServerSet()) updateSmtp();
		if (getWorkingHoursPerDay() == null) setWorkingHoursPerDay(7);
	}

	private void updateSmtp() {
		setSmtpServer(config.getSmtpServer());
		if (!Str.isBlank(config.getSmtpUser())) setSmtpUser(config.getSmtpUser());
		if (!Str.isBlank(config.getSmtpPassword())) setSmtpPassword(config.getSmtpPassword());
	}

	public boolean isOpenIdDomainAllowed(String openId) {
		if (!isOpenIdDomainsSet()) return true;
		String[] domains = Str.tokenize(getOpenIdDomains(), ",; ");
		for (String domain : domains) {
			if (openId.contains(domain)) return true;
		}
		return false;
	}

	public List<Date> getHolidayDates() {

		List<Date> dates = new ArrayList<Date>();
		if (getHolidays() != null) {
			String[] holidays = getHolidays().split(";");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

			for (String holiday : holidays) {
				try {
					dates.add(new Date(sdf.parse(holiday)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return dates;

	}

	// --- dependencies ---

	private transient static KunagiRootConfig config;

	public static void setConfig(KunagiRootConfig config) {
		SystemConfig.config = config;
	}

	// --- ---
}