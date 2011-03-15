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
import scrum.client.project.Project;
import scrum.client.sprint.Sprint;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class SprintWorkTextWidget extends AScrumWidget {

	private HTML html;

	@Override
	protected Widget onInitialization() {
		html = new HTML();
		return html;
	}

	@Override
	protected void onUpdate() {
		Project project = getCurrentProject();
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='SprintWorkTextWidget'>");

		int count = 0;
		for (Sprint sprint : project.getReverseFormerSprints()) {

			if (sprint.getVelocity() != null && sprint.getVelocity() > 0) {
				sb.append("<div class='SprintWorkTextWidget-sprint'>");
				sb.append("<span style='font-weight: bold;'>");
				sb.append(sprint.getLabel());
				sb.append("</span>");
				sb.append(" (" + sprint.getBegin() + " - " + sprint.getEnd() + ")");
				sb.append("<ul>");
				sb.append("<li>Releases: " + (sprint.getReleases().size() == 0 ? "" : sprint.getReleases()) + "</li>");
				sb.append("<li>Goal: " + (sprint.getGoal() == null ? "" : sprint.getGoal()) + "</li>");
				sb.append("<li>Velocity: " + sprint.getVelocity() + "</li>");
				sb.append("<li>Days: " + sprint.getLengthInDays() + "</li>");
				sb.append("</ul></div>");

				if (++count >= 10) break;
			}
		}
		// XXX current?
		sb.append("</div>");
		html.setHTML(sb.toString());

	}

}