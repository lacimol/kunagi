package scrum.client.sprint;

import ilarkesto.gwt.client.AIntegerViewEditWidget;
import ilarkesto.gwt.client.ARichtextViewEditWidget;
import ilarkesto.gwt.client.ATextViewEditWidget;
import ilarkesto.gwt.client.AWidget;
import ilarkesto.gwt.client.ToolbarWidget;
import scrum.client.ScrumGwtApplication;
import scrum.client.common.AExtensibleBlockWidget;
import scrum.client.common.FieldsWidget;
import scrum.client.img.Img;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TaskWidget extends AExtensibleBlockWidget {

	private Task task;
	private AWidget autoUpdateWidget; // widget to update, when fields edited

	private Label summary;
	private FieldsWidget fields;

	public TaskWidget(Task task, AWidget autoUpdateWidget) {
		this.task = task;
		this.autoUpdateWidget = autoUpdateWidget;
	}

	@Override
	protected void onCollapsedInitialization() {
		summary = new Label();
	}

	@Override
	protected void onCollapsedUpdate() {
		setBlockTitle(task.getLabel());
		setIcon(task.isDone() ? Img.bundle.taskDoneIcon32() : Img.bundle.taskIcon32());
		summary.setText(task.getSummary());
		setContent(summary);
		setToolbar(null);
	}

	@Override
	protected void onExtendedInitialization() {
		fields = new FieldsWidget();
		fields.setAutoUpdateWidget(autoUpdateWidget);

		fields.add("Description", new ATextViewEditWidget() {

			@Override
			protected void onViewerUpdate() {
				setViewerText(task.getLabel());
			}

			@Override
			protected void onEditorUpdate() {
				setViewerText(task.getLabel());
			}

			@Override
			protected void onEditorSubmit() {
				task.setLabel(getEditorText());
			}

		});

		fields.add("Burned Work", new AIntegerViewEditWidget() {

			@Override
			protected void onViewerUpdate() {
				setViewerValue(task.getBurnedWork(), "hours");
			}

			@Override
			protected void onEditorUpdate() {
				setEditorValue(task.getBurnedWork());
			}

			@Override
			protected void onEditorSubmit() {
				Integer value = getEditorValue(0);
				if (value == null) value = 0;
				int previous = task.getBurnedWork();
				int diff = value - previous;
				task.setBurnedWork(value);
				task.adjustRemainingWork(diff);
			}

			@Override
			protected void onMinusClicked() {
				task.decrementBurnedWork();
				task.adjustRemainingWork(-1);
			}

			@Override
			protected void onPlusClicked() {
				task.incrementBurnedWork();
				task.adjustRemainingWork(1);
			}
		});

		fields.add("Remaining Work", new AIntegerViewEditWidget() {

			@Override
			protected void onViewerUpdate() {
				setViewerValue(task.getRemainingWork(), "hours");
			}

			@Override
			protected void onEditorUpdate() {
				setEditorValue(task.getRemainingWork());
			}

			@Override
			protected void onEditorSubmit() {
				task.setRemainingWork(getEditorValue(1));
			}

			@Override
			protected void onMinusClicked() {
				task.decrementRemainingWork();
			}

			@Override
			protected void onPlusClicked() {
				task.incrementRemainingWork();
			}

		});

		fields.add("Note", new ARichtextViewEditWidget() {

			@Override
			protected void onViewerUpdate() {
				setViewerText(task.getNotice());
			}

			@Override
			protected void onEditorUpdate() {
				setEditorText(task.getNotice());
			}

			@Override
			protected void onEditorSubmit() {
				task.setNotice(getEditorText());
			}

		});

		fields.add("Owner", new Label(task.getOwner() == null ? "No owner specified." : task.getOwner().getName()));

	}

	@Override
	protected void onExtendedUpdate() {
		setBlockTitle(task.getLabel());
		setIcon(task.isDone() ? Img.bundle.taskDoneIcon32() : Img.bundle.taskIcon32());
		fields.update();
		setContent(fields);
		setToolbar(createToolbar());
	}

	public Task getTask() {
		return task;
	}

	@Override
	protected DropController createDropController() {
		return null;
	}

	protected Widget createToolbar() {

		ToolbarWidget toolbar = new ToolbarWidget();
		if (!task.isDone()) {
			toolbar.addButton("Own").addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					task.setOwner(ScrumGwtApplication.get().getUser());
					autoUpdateWidget.update();
				}

			});
		}

		toolbar.addButton(Img.bundle.delete16().createImage(), "Delete").addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				delete();
				autoUpdateWidget.update();
			}

		});

		if (!task.isDone()) {
			toolbar.addButton(Img.bundle.done16().createImage(), "Done").addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					task.setDone();
					autoUpdateWidget.update();
				}

			});
		}
		return toolbar;
	}

	@Override
	protected AbstractImagePrototype getIcon16() {
		if (task.isDone()) return Img.bundle.taskDoneIcon16();
		return Img.bundle.taskIcon16();
	}

	@Override
	public void delete() {
		task.getRequirement().deleteTask(task);
		autoUpdateWidget.update();
	}
}
