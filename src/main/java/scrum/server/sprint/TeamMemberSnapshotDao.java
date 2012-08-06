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

package scrum.server.sprint;

import ilarkesto.fp.Predicate;
import scrum.server.admin.User;

public class TeamMemberSnapshotDao extends GTeamMemberSnapshotDao {

	public TeamMemberSnapshot getSnapshot(final Sprint sprint, final User user, boolean autoCreate) {

		TeamMemberSnapshot snapshot = getEntity(new Predicate<TeamMemberSnapshot>() {

			@Override
			public boolean test(TeamMemberSnapshot e) {
				return e.isSprint(sprint) && e.isTeamMember(user);
			}
		});

		if (autoCreate && snapshot == null) {
			snapshot = newEntityInstance();
			snapshot.setSprint(sprint);
			snapshot.setTeamMember(user);
			String name = user.getName();
			UserEfficiency userEfficiency = sprint.getUserEfficiency(name);
			snapshot.setInitialWork(userEfficiency.getInitialBurnableHours());
			snapshot.setBurnedWork(userEfficiency.getAllBurnedHours());
			snapshot.setEfficiency(userEfficiency.getEfficiency());
			saveEntity(snapshot);
		}

		return snapshot;
	}

}