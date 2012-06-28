package scrum.client.common;

import ilarkesto.core.base.Str;
import ilarkesto.gwt.client.AAction;
import ilarkesto.gwt.client.AMultiSelectionViewEditWidget;
import ilarkesto.gwt.client.HyperlinkWidget;
import ilarkesto.gwt.client.MultiSelectionWidget;

import java.util.Collections;
import java.util.List;

import scrum.client.ScrumGwt;

import com.google.gwt.user.client.ui.Widget;

public class ThemesWidget extends AMultiSelectionViewEditWidget<String> {

	private ThemesContainer model;

	private ThemeSelector themeSelector;

	public ThemesWidget(ThemesContainer model) {
		super();
		this.model = model;
		themeSelector = new ThemeSelector(model);
	}

	@Override
	protected void onViewerUpdate() {
		setViewerItems(model.getThemes(), ", ");
	}

	@Override
	protected void onEditorUpdate() {
		List<String> themes = themeSelector.getBaseThemes();
		Collections.sort(themes);
		setEditorItems(themes);
		setEditorSelectedItems(model.getThemes());
	}

	@Override
	protected void onEditorSubmit() {
		model.setThemes(getEditorSelectedItems());
	}

	@Override
	protected Widget getExtendedEditorContent() {
		return new HyperlinkWidget(new AddThemeAction()).update();
	}

	@Override
	public boolean isEditable() {
		return model.isThemesEditable();
	}

	class AddThemeAction extends AAction {

		@Override
		public String getLabel() {
			return "Add new Theme";
		}

		@Override
		protected void onExecute() {
			String theme = ScrumGwt.prompt("New Theme:", "");
			if (!Str.isBlank(theme)) {
				MultiSelectionWidget<String> editor = getEditor();

				List<String> items = editor.getItems();
				List<String> selected = editor.getSelected();

				items.add(theme);
				Collections.sort(items);
				selected.add(theme);

				editor.setItems(items);
				editor.setSelected(selected);
			}
		}
	}

}
