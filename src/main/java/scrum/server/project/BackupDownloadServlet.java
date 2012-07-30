/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package scrum.server.project;

import ilarkesto.base.PermissionDeniedException;
import ilarkesto.webapp.Servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scrum.server.ScrumWebApplication;
import scrum.server.WebSession;
import scrum.server.common.AHttpServlet;

public class BackupDownloadServlet extends AHttpServlet {

	@Override
	protected void onRequest(HttpServletRequest req, HttpServletResponse resp, WebSession session) throws IOException {
		ScrumWebApplication webapp = ScrumWebApplication.get();
		String projectId = req.getParameter("projectId");
		if (projectId == null) throw new RuntimeException("projectId == null");

		Project project = webapp.getProjectDao().getById(projectId);
		if (!project.containsAdmin(session.getUser())) throw new PermissionDeniedException();

		ProjectZipper zipper = new ProjectZipper(new File(webapp.getApplicationDataDir()), project);

		Servlet.setFilename("scrum-project-backup.zip", resp);
		zipper.createZip(resp.getOutputStream());
	}

}
