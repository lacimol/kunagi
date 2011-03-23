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

package scrum.client.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import scrum.client.img.Img;

import com.google.gwt.user.client.ui.Image;

public class ThemeSelector {

	private ThemesContainer model;

	public ThemeSelector(ThemesContainer model) {
		this.model = model;
	}

	public Image getStatusImage() {
		Image statusImage = null;
		// initial
		if (isDev()) {
			statusImage = Img.bundle.dev().createImage();
			statusImage.setTitle("Feature.");
		} else if (isBug()) {
			statusImage = Img.bundle.bug().createImage();
			statusImage.setTitle("Bug.");
		} else if (isTest()) {
			statusImage = Img.bundle.test().createImage();
			statusImage.setTitle("Test.");
		} else if (isRelease()) {
			statusImage = Img.bundle.release().createImage();
			statusImage.setTitle("Release.");
		} else if (isSupport()) {
			statusImage = Img.bundle.support().createImage();
			statusImage.setTitle("Support.");
		} else if (isRefactor()) {
			statusImage = Img.bundle.refactor().createImage();
			statusImage.setTitle("Refactor.");
		} else if (isDoc()) {
			statusImage = Img.bundle.doc().createImage();
			statusImage.setTitle("Documentation.");
		} else if (isMeeting()) {
			statusImage = Img.bundle.meeting().createImage();
			statusImage.setTitle("Meeting.");
		}

		return statusImage;
	}

	public boolean isThemesContains(String word) {
		boolean result = false;
		if (word != null
				&& (model.getThemes().contains(word) || model.getThemes().contains(word.toLowerCase()) || model
						.getThemes().contains(word.toUpperCase()))) {
			result = true;
		}
		return result;
	}

	/**
	 * Is a new feature?
	 * 
	 * @return
	 */
	public boolean isDev() {
		return isThemesContains("Dev") || isThemesContains("Feature");
	}

	public boolean isBug() {
		return isThemesContains("Bug") || isThemesContains("Error");
	}

	public boolean isTest() {
		return isThemesContains("Test");
	}

	/**
	 * Is a release work (build, copy, install)?
	 * 
	 * @return
	 */
	public boolean isRelease() {
		return isThemesContains("Release") || isThemesContains("Install");
	}

	/**
	 * Is a support work (phone, e-mail)?
	 * 
	 * @return
	 */
	public boolean isSupport() {
		return isThemesContains("Support") || isThemesContains("Helpdesk");
	}

	public boolean isRefactor() {
		return isThemesContains("Refactor");
	}

	public boolean isDoc() {
		return isThemesContains("Doc") || isThemesContains("Documentation");
	}

	public boolean isMeeting() {
		return isThemesContains("Meeting");
	}

	protected List<String> getBaseThemes() {
		Set<String> themesSet = new HashSet<String>(model.getAvailableThemes());
		themesSet.add("Feature");
		themesSet.add("Bug");
		themesSet.add("Test");
		themesSet.add("Refactor");
		themesSet.add("Release");
		themesSet.add("Support");
		themesSet.add("Meeting");
		themesSet.add("Documentation");
		return new ArrayList<String>(themesSet);
	}
}
