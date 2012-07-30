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

import ilarkesto.core.logging.Log;

import java.util.ArrayList;
import java.util.List;

public class SystemConfigDao extends GSystemConfigDao {

	private static Log log = Log.get(SystemConfigDao.class);

	public SystemConfig getSystemConfig() {
		List<SystemConfig> all = new ArrayList<SystemConfig>(getEntities());

		if (all.isEmpty()) throw new RuntimeException("SystemConfig entity does not exist");

		while (all.size() > 1)
			throw new RuntimeException("Multiple SystemConfig entities exist");

		return all.get(0);
	}

	@Override
	public void ensureIntegrity() {
		super.ensureIntegrity();
		if (getEntities().isEmpty()) {
			log.warn("SystemConfig entity does not exist. Creating new one.");
			SystemConfig config = newEntityInstance();
			saveEntity(config);
		}
		getSystemConfig();
	}

}