package scrum.client.project;

import ilarkesto.core.time.DateAndTime;
import scrum.client.common.TooltipBuilder;

public class CheckProjectActivityAction extends GCheckProjectActivityAction {

	private DateAndTime lastCheck;

	public CheckProjectActivityAction(scrum.client.project.Project project) {
		super(project);
	}

	@Override
	protected void updateTooltip(TooltipBuilder tb) {
		tb.setText("Check project and team members activity. Send mail to them about burned hours if needed."
				+ (lastCheck == null ? "" : " Last check: " + lastCheck));
		if (!project.isScrumTeamMember(getCurrentUser())) tb.addRemark(TooltipBuilder.NOT_SCRUMTEAM);
	}

	@Override
	public boolean isExecutable() {
		if (project.getTeamMembers().isEmpty()) return false;
		return true;
	}

	@Override
	public boolean isPermitted() {
		if (!project.isScrumMaster(getCurrentUser())) return false;
		return true;
	}

	@Override
	public String getLabel() {
		return "Check project activity";
	}

	@Override
	protected void onExecute() {
		lastCheck = new DateAndTime();
		new CheckProjectActivityServiceCall().execute();
	}

}