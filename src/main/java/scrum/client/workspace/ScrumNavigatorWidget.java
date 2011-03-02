package scrum.client.workspace;

import ilarkesto.core.base.Str;
import ilarkesto.core.menu.MenuItem;
import ilarkesto.core.menu.StaticMenuItem;
import ilarkesto.core.scope.Scope;
import ilarkesto.gwt.client.SwitchingNavigatorWidget;
import scrum.client.collaboration.WikiWidget;
import scrum.client.impediments.ImpedimentListWidget;
import scrum.client.pr.BlogWidget;
import scrum.client.project.ProductBacklogWidget;
import scrum.client.tasks.WhiteboardWidget;

import com.google.gwt.user.client.ui.Widget;

public class ScrumNavigatorWidget extends SwitchingNavigatorWidget {

	public ScrumNavigatorWidget() {
		super(Scope.get().getComponent(Ui.class).getWorkspace().getWorkarea());
	}

	@Override
	protected void showItem(Widget widget) {
		String page = Str.getSimpleName(widget.getClass());
		page = Str.removeSuffix(page, "Widget");
		Scope.get().getComponent(Navigator.class).setPage(page);
		super.showItem(widget);
	}

	@Override
	protected String getHref(MenuItem item) {
		if (item instanceof StaticMenuItem) {
			StaticMenuItem staticItem = (StaticMenuItem) item;
			Object payload = staticItem.getPayload();
			if ("sprint".equals(payload)) return Navigator.getPageHref(WhiteboardWidget.class);
			if ("product".equals(payload)) return Navigator.getPageHref(ProductBacklogWidget.class);
			if ("project".equals(payload)) return Navigator.getPageHref(ImpedimentListWidget.class);
			if ("collaboration".equals(payload)) return Navigator.getPageHref(WikiWidget.class);
			if ("administration".equals(payload)) return Navigator.getPageHref(BlogWidget.class);
			if (payload instanceof Widget) return Navigator.getPageHref(((Widget) payload).getClass());
		}
		return super.getHref(item);
	}
}
