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
package scrum.server.css;

import ilarkesto.core.logging.Log;
import ilarkesto.io.DynamicClassLoader;
import ilarkesto.ui.web.CssRenderer;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scrum.server.ScrumWebApplication;
import scrum.server.WebSession;
import scrum.server.common.AHttpServlet;

public class CssServlet extends AHttpServlet {

	private static final Log LOG = Log.get(CssServlet.class);
	private static final long serialVersionUID = 1;

	private transient final ScreenCssBuilder screenCssBuilder = new ScreenCssBuilder();

	@Override
	protected void onRequest(HttpServletRequest req, HttpServletResponse resp, WebSession session) throws IOException {
		resp.setContentType("text/css");
		CssRenderer css = new CssRenderer(resp.getWriter());
		CssBuilder builder = getCssBuilder();
		builder.buildCss(css);
		css.flush();
		// LOG.debug(builder);
	}

	private CssBuilder getCssBuilder() {
		if (ScrumWebApplication.get().isDevelopmentMode()) {
			ClassLoader loader = new DynamicClassLoader(getClass().getClassLoader(), ScreenCssBuilder.class.getName());
			Class<? extends CssBuilder> type;
			try {
				type = (Class<? extends CssBuilder>) loader.loadClass(ScreenCssBuilder.class.getName());
				return type.newInstance();
			} catch (Throwable ex) {
				LOG.fatal(ex);
				throw new RuntimeException(ex);
			}
		} else {
			return screenCssBuilder;
		}
	}

	@Override
	protected void onInit(ServletConfig config) {
		ScrumWebApplication.get(config);
	}

}
