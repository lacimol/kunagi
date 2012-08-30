/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package scrum.server.admin;

import ilarkesto.core.base.Str;
import ilarkesto.core.time.Tm;
import ilarkesto.ui.web.HtmlRenderer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scrum.server.WebSession;
import scrum.server.common.AHttpServlet;

public class ShutdownServlet extends AHttpServlet {

	private boolean shutdownInitiated;

	@Override
	protected void onRequest(HttpServletRequest req, HttpServletResponse resp, WebSession session) throws IOException {
		tokenLogin(req, resp, session);

		User user = session.getUser();
		if (user == null) {
			redirectToLogin(req, resp, session);
			return;
		}

		if (!user.isAdmin()) {
			resp.sendError(403);
			return;
		}

		if (!shutdownInitiated) {
			shutdownInitiated = true;
			String sDelay = req.getParameter("delay");
			long delayInMillis = 0;
			if (!Str.isBlank(sDelay)) delayInMillis = Long.parseLong(sDelay) * Tm.MINUTE;
			webApplication.shutdown(delayInMillis);
		}

		HtmlRenderer html = createDefaultHtmlWithHeader(resp, "Shutdown initiated", 3, "admin.html");
		html.startBODY();
		html.H1("Shutdown initiated");
		html.endBODY();
		html.endHTML();
		html.flush();
	}
}
