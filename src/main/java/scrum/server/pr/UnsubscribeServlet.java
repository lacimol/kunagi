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
package scrum.server.pr;

import ilarkesto.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.io.IO;
import ilarkesto.persistence.AEntity;
import ilarkesto.persistence.DaoService;
import ilarkesto.persistence.TransactionService;
import ilarkesto.webapp.Servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scrum.server.ScrumWebApplication;
import scrum.server.WebSession;
import scrum.server.common.AHttpServlet;
import scrum.server.common.KunagiUtl;

public class UnsubscribeServlet extends AHttpServlet {

	private static final long serialVersionUID = 1;

	private static Log log = Log.get(UnsubscribeServlet.class);

	private transient SubscriptionService subscriptionService;
	private transient DaoService daoService;
	private transient TransactionService transactionService;

	private transient ScrumWebApplication app;

	@Override
	protected void onRequest(HttpServletRequest req, HttpServletResponse resp, WebSession session) throws IOException {
		req.setCharacterEncoding(IO.UTF_8);

		String subjectId = req.getParameter("subject");
		String email = Str.cutRight(req.getParameter("email"), 64);
		if (Str.isBlank(email)) email = null;
		String key = req.getParameter("key");

		log.info("Unsubscription from the internet");
		log.info("    subject: " + subjectId);
		log.info("    email: " + email);
		log.info("    key: " + key);
		log.info("  Request-Data:");
		log.info(Servlet.toString(req, "        "));

		if (email == null) {
			sendFailureResponse(req, resp, "email required");
			return;
		}

		String message;
		try {
			AEntity subject = unsubscribe(email, subjectId, key);
			message = subject == null ? "Succesfully unsubscribed from all entities"
					: "Succesfully unsubscribed from <strong>" + KunagiUtl.createExternalRelativeHtmlAnchor(subject)
							+ "</strong>";
		} catch (Throwable ex) {
			log.error("Unsubscription failed.", "\n" + Servlet.toString(req, "  "), ex);
			sendFailureResponse(req, resp, Str.getRootCauseMessage(ex));
			return;
		}

		sendResponse(req, resp, message);
	}

	private void sendFailureResponse(HttpServletRequest req, HttpServletResponse resp, String message)
			throws IOException {
		sendResponse(req, resp, "<h2>Failure</h2><p>Unsubscription failed: <strong>" + message
				+ "</strong></p><p>We are sorry, please try again later.</p>");
	}

	private void sendResponse(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
		String returnUrl = req.getParameter("returnUrl");
		if (returnUrl == null) returnUrl = "http://kunagi.org/message.html?#{message}";
		returnUrl = returnUrl.replace("{message}", Str.encodeUrlParameter(message));

		resp.sendRedirect(returnUrl);
	}

	private AEntity unsubscribe(String email, String subjectId, String key) throws InvalidKeyException {
		AEntity subject = subjectId == null ? null : daoService.getById(subjectId);
		subscriptionService.unsubscribe(email, subject, key);
		transactionService.commit();
		return subject;
	}

	@Override
	protected void onInit(ServletConfig config) {
		app = ScrumWebApplication.get(config);
		subscriptionService = app.getSubscriptionService();
		daoService = app.getDaoService();
		transactionService = app.getTransactionService();
	}

}