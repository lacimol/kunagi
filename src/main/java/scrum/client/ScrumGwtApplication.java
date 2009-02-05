package scrum.client;

import ilarkesto.gwt.client.DataTransferObject;
import scrum.client.admin.User;
import scrum.client.dnd.ScrumDragController;
import scrum.client.project.Project;
import scrum.client.workspace.WorkspaceWidget;

import com.google.gwt.user.client.ui.RootPanel;

public class ScrumGwtApplication extends GScrumGwtApplication {

	private Dao dao = new Dao();

	/**
	 * Current, logged in user.
	 * 
	 * TODO potential security problem. User could change this variable.
	 */
	private User user;

	/**
	 * Currently selected project.
	 */
	private Project project;

	private ScrumDragController dragController;

	/**
	 * Application entry point.
	 */
	public void onModuleLoad() {
		callPing();
		RootPanel.get("workspace").add(new WorkspaceWidget());
	}

	public User getUser() {
		return user;
	}

	@Override
	protected void handleDataFromServer(DataTransferObject data) {
		super.handleDataFromServer(data);

		if (data.entityIdBase != null) {
			entityIdBase = data.entityIdBase;
			System.out.println("entityIdBase: " + entityIdBase);
		}

		if (data.isUserSet()) {
			user = getDao().getUser(data.getUserId());
		}
	}

	@Override
	protected void handleCommunicationError(Throwable ex) {
		ex.printStackTrace();
		WorkspaceWidget.lock("Error: " + ex.getMessage());
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

	@Override
	public Dao getDao() {
		return dao;
	}

	public static ScrumGwtApplication get() {
		return (ScrumGwtApplication) singleton;
	}

	public ScrumDragController getDragController() {
		if (dragController == null) {
			dragController = new ScrumDragController(RootPanel.get(), false);
		}
		return dragController;
	}
}
