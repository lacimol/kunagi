package scrum.client.project;

import ilarkesto.gwt.client.Date;
import scrum.client.collaboration.EmoticonsWidget;
import scrum.client.common.ABlockWidget;
import scrum.client.common.AScrumAction;
import scrum.client.common.BlockHeaderWidget;
import scrum.client.common.BlockWidgetFactory;
import scrum.client.common.ThemeSelector;
import scrum.client.dnd.TrashSupport;
import scrum.client.img.Img;
import scrum.client.journal.ActivateChangeHistoryAction;
import scrum.client.sprint.Sprint;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class RequirementBlock extends ABlockWidget<Requirement> implements TrashSupport {

	private SimplePanel statusIcon;
	private SprintSwitchIndicatorWidget sprintBorderIndicator;

	@Override
	protected void onInitializationHeader(BlockHeaderWidget header) {
		Requirement requirement = getObject();
		statusIcon = header.addIconWrapper();
		header.addText(requirement.getLabelModel());
		header.addText(requirement.getThemesAsStringModel(), true, false);
		header.addText(requirement.getEstimatedWorkWithUnitModel(), true);

		header.appendCell(new EmoticonsWidget(requirement), null, true);
		header.appendCell(new EstimationBarWidget(requirement), "150px", true);

		header.addMenuAction(new AddRequirementToCurrentSprintAction(requirement));
		header.addMenuAction(new RemoveRequirementFromSprintAction(requirement));
		header.addMenuAction(new SetRequirementDirtyAction(requirement));
		header.addMenuAction(new SetRequirementCleanAction(requirement));
		header.addMenuAction(new StartRequirementEstimationVotingAction(requirement));
		header.addMenuAction(new ActivateChangeHistoryAction(requirement));
		header.addMenuAction(new DeleteRequirementAction(requirement));
	}

	@Override
	protected void onUpdateHeader(BlockHeaderWidget header) {
		Requirement requirement = getObject();
		header.setDragHandle(requirement.getReference());
		Image statusImage = new ThemeSelector(requirement).getStatusImage();
		if (requirement.isWorkEstimationVotingActive()) {
			statusImage = Img.bundle.reqPoker().createImage();
			statusImage.setTitle("Estimation game \"Planning Poker\" active.");
		} else if (requirement.isInCurrentSprint()) {
			statusImage = Img.bundle.reqInSprint().createImage();
			statusImage.setTitle("In current sprint.");
		} else if (requirement.isClosed()) {
			statusImage = Img.bundle.reqClosed().createImage();
			statusImage.setTitle("Closed.");
		} else if (!requirement.isEstimatedWorkValid()) {
			statusImage = Img.bundle.reqDirty().createImage();
			statusImage.setTitle("Needs estimation.");
		}
		statusIcon.setWidget(statusImage);

		boolean sprintBorder = false;
		Requirement previous = getList().getPrevious(requirement);
		if (previous != null) {
			EstimationBar previousEstimationBar = previous.getEstimationBar();
			sprintBorder = !requirement.getEstimationBar().isCompetedOnSameSprint(previousEstimationBar);
		}

		if (previous != null && sprintBorder) {
			if (sprintBorderIndicator == null) {
				sprintBorderIndicator = new SprintSwitchIndicatorWidget();
				Sprint sprint = getCurrentProject().getCurrentSprint();
				int sprints = previous.getEstimationBar().getEndSprintOffset() + 2;
				sprintBorderIndicator.updateLabel(sprints,
					sprint.getLength().multiplyBy(sprints).subtract(sprint.getBegin().getPeriodTo(Date.today()).abs()));
				getPreHeaderPanel().add(sprintBorderIndicator);
				requirement.updateLocalModificationTime();
			}
		} else {
			if (sprintBorderIndicator != null) {
				getPreHeaderPanel().remove(sprintBorderIndicator);
				sprintBorderIndicator = null;
				requirement.updateLocalModificationTime();
			}
		}
	}

	@Override
	protected Widget onExtendedInitialization() {
		return new RequirementWidget(getObject(), true, true, false, true, true, true, false);
	}

	@Override
	public AScrumAction getTrashAction() {
		return new DeleteRequirementAction(getObject());
	}

	public static final BlockWidgetFactory<Requirement> FACTORY = new BlockWidgetFactory<Requirement>() {

		@Override
		public RequirementBlock createBlock() {
			return new RequirementBlock();
		}
	};

}
