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

import ilarkesto.auth.OpenId;
import ilarkesto.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.integration.gravatar.Gravatar;
import ilarkesto.integration.gravatar.Profile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import scrum.server.ScrumWebApplication;
import scrum.server.common.Chart;

public class UserDao extends GUserDao {

	private static Log log = Log.get(UserDao.class);

	public static List<String> userNames = new ArrayList<String>();
	static {
		userNames.add("Doc");
		userNames.add("Dopey");
		userNames.add("Grumpy");
		userNames.add("Sleepy");
		userNames.add("Sneezy");
		userNames.add("Bashful");
		userNames.add("Happy");
	}

	@Override
	public User postUser(String name, String password) {
		return postUser(null, name, password);
	}

	public User postUser(String email, String name, String password) {
		User user = newEntityInstance();
		user.setEmail(email);
		user.setName(name);
		user.setPassword(password);
		user.tryUpdateByGravatar();
		saveEntity(user);
		log.info("User created:", user);
		return user;
	}

	public User postUserWithDefaultPassword(String name) {
		return postUser(name, getDefaultPassword());
	}

	public User postUserWithOpenId(String openId, String nickname, String fullname, String email) {
		String name = null;

		if (nickname != null) {
			if (getUserByName(nickname) == null) name = nickname;
		}

		if (name == null && email != null) {
			Profile profile = Gravatar.loadProfile(email);
			if (profile != null) name = profile.getPreferredUsername();
		}

		if (name == null && email != null) {
			name = Str.cutTo(email, "@");
		}

		if (name == null) {
			name = OpenId.cutUsername(openId);
			name = Str.removePrefix(name, "AItOaw");
			if (name.length() > 10) name = name.substring(0, 9);
		}

		String namePrefix = name;
		int suffix = 1;
		while (getUserByName(name) != null) {
			suffix++;
			name = namePrefix + suffix;
		}

		User user = newEntityInstance();
		user.setName(name);
		user.setFullName(fullname);
		user.setOpenId(openId);
		if (!Str.isBlank(email)) {
			user.setEmail(email);
			user.setEmailVerified(true);
		}
		user.setPassword(Str.generatePassword());
		user.tryUpdateByGravatar();
		saveEntity(user);
		log.info("User created:", user);
		return user;
	}

	@Override
	public User newEntityInstance() {
		User user = super.newEntityInstance();
		user.setPassword(getDefaultPassword());
		user.setRegistrationDateAndTime(DateAndTime.now());
		return user;
	}

	private String getDefaultPassword() {
		return ScrumWebApplication.get().getSystemConfig().getDefaultUserPassword();
	}

	// --- test data ---

	public User getTestUser(String name) {
		User user = getUserByName(name);
		if (user == null) user = postUserWithDefaultPassword(name);
		return user;
	}

	public List<User> getExampleUsers() {
		List<User> users = new ArrayList<User>();
		User user = null;
		Iterator<String> colors = Chart.userColors.keySet().iterator();
		colors.next();
		int userNr = 7;
		for (int x = 0; x < userNr; x++) {
			String name = userNames.get(x);
			user = getUserByName(name);
			if (user == null) user = postUserWithDefaultPassword(name);
			user.setEmail(name + "@kunagi.org");
			user.setEmailVerified(true);
			if (colors.hasNext()) {
				user.setColor(colors.next());
			}
			users.add(user);
		}
		return users;
	}

}
