// ----------> GENERATED FILE - DON'T TOUCH! <----------

// generator: ilarkesto.mda.legacy.generator.GwtEntityGenerator










package scrum.client.sprint;

import java.util.*;
import ilarkesto.core.logging.Log;
import scrum.client.common.*;
import ilarkesto.gwt.client.*;

public abstract class GTeamMemberSnapshot
            extends scrum.client.common.AScrumGwtEntity {

    protected scrum.client.Dao getDao() {
        return scrum.client.Dao.get();
    }

    public GTeamMemberSnapshot() {
    }

    public GTeamMemberSnapshot(Map data) {
        super(data);
        updateProperties(data);
    }

    public static final String ENTITY_TYPE = "teamMemberSnapshot";

    @Override
    public final String getEntityType() {
        return ENTITY_TYPE;
    }

    // --- sprint ---

    private String sprintId;

    public final scrum.client.sprint.Sprint getSprint() {
        if (sprintId == null) return null;
        return getDao().getSprint(this.sprintId);
    }

    public final boolean isSprintSet() {
        return sprintId != null;
    }

    public final TeamMemberSnapshot setSprint(scrum.client.sprint.Sprint sprint) {
        String id = sprint == null ? null : sprint.getId();
        if (equals(this.sprintId, id)) return (TeamMemberSnapshot) this;
        this.sprintId = id;
        propertyChanged("sprintId", this.sprintId);
        return (TeamMemberSnapshot)this;
    }

    public final boolean isSprint(scrum.client.sprint.Sprint sprint) {
        return equals(this.sprintId, sprint);
    }

    // --- teamMember ---

    private String teamMemberId;

    public final scrum.client.admin.User getTeamMember() {
        if (teamMemberId == null) return null;
        return getDao().getUser(this.teamMemberId);
    }

    public final boolean isTeamMemberSet() {
        return teamMemberId != null;
    }

    public final TeamMemberSnapshot setTeamMember(scrum.client.admin.User teamMember) {
        String id = teamMember == null ? null : teamMember.getId();
        if (equals(this.teamMemberId, id)) return (TeamMemberSnapshot) this;
        this.teamMemberId = id;
        propertyChanged("teamMemberId", this.teamMemberId);
        return (TeamMemberSnapshot)this;
    }

    public final boolean isTeamMember(scrum.client.admin.User teamMember) {
        return equals(this.teamMemberId, teamMember);
    }

    // --- initialWork ---

    private int initialWork ;

    public final int getInitialWork() {
        return this.initialWork ;
    }

    public final TeamMemberSnapshot setInitialWork(int initialWork) {
        if (isInitialWork(initialWork)) return (TeamMemberSnapshot)this;
        this.initialWork = initialWork ;
        propertyChanged("initialWork", this.initialWork);
        return (TeamMemberSnapshot)this;
    }

    public final boolean isInitialWork(int initialWork) {
        return equals(this.initialWork, initialWork);
    }

    private transient InitialWorkModel initialWorkModel;

    public InitialWorkModel getInitialWorkModel() {
        if (initialWorkModel == null) initialWorkModel = createInitialWorkModel();
        return initialWorkModel;
    }

    protected InitialWorkModel createInitialWorkModel() { return new InitialWorkModel(); }

    protected class InitialWorkModel extends ilarkesto.gwt.client.editor.AIntegerEditorModel {

        @Override
        public String getId() {
            return "TeamMemberSnapshot_initialWork";
        }

        @Override
        public java.lang.Integer getValue() {
            return getInitialWork();
        }

        @Override
        public void setValue(java.lang.Integer value) {
            setInitialWork(value);
        }

            @Override
            public void increment() {
                setInitialWork(getInitialWork() + 1);
            }

            @Override
            public void decrement() {
                setInitialWork(getInitialWork() - 1);
            }

        @Override
        protected void onChangeValue(java.lang.Integer oldValue, java.lang.Integer newValue) {
            super.onChangeValue(oldValue, newValue);
            addUndo(this, oldValue);
        }

    }

    // --- burnedWork ---

    private int burnedWork ;

    public final int getBurnedWork() {
        return this.burnedWork ;
    }

    public final TeamMemberSnapshot setBurnedWork(int burnedWork) {
        if (isBurnedWork(burnedWork)) return (TeamMemberSnapshot)this;
        this.burnedWork = burnedWork ;
        propertyChanged("burnedWork", this.burnedWork);
        return (TeamMemberSnapshot)this;
    }

    public final boolean isBurnedWork(int burnedWork) {
        return equals(this.burnedWork, burnedWork);
    }

    private transient BurnedWorkModel burnedWorkModel;

    public BurnedWorkModel getBurnedWorkModel() {
        if (burnedWorkModel == null) burnedWorkModel = createBurnedWorkModel();
        return burnedWorkModel;
    }

    protected BurnedWorkModel createBurnedWorkModel() { return new BurnedWorkModel(); }

    protected class BurnedWorkModel extends ilarkesto.gwt.client.editor.AIntegerEditorModel {

        @Override
        public String getId() {
            return "TeamMemberSnapshot_burnedWork";
        }

        @Override
        public java.lang.Integer getValue() {
            return getBurnedWork();
        }

        @Override
        public void setValue(java.lang.Integer value) {
            setBurnedWork(value);
        }

            @Override
            public void increment() {
                setBurnedWork(getBurnedWork() + 1);
            }

            @Override
            public void decrement() {
                setBurnedWork(getBurnedWork() - 1);
            }

        @Override
        protected void onChangeValue(java.lang.Integer oldValue, java.lang.Integer newValue) {
            super.onChangeValue(oldValue, newValue);
            addUndo(this, oldValue);
        }

    }

    // --- efficiency ---

    private java.lang.Float efficiency ;

    public final java.lang.Float getEfficiency() {
        return this.efficiency ;
    }

    public final TeamMemberSnapshot setEfficiency(java.lang.Float efficiency) {
        if (isEfficiency(efficiency)) return (TeamMemberSnapshot)this;
        this.efficiency = efficiency ;
        propertyChanged("efficiency", this.efficiency);
        return (TeamMemberSnapshot)this;
    }

    public final boolean isEfficiency(java.lang.Float efficiency) {
        return equals(this.efficiency, efficiency);
    }

    private transient EfficiencyModel efficiencyModel;

    public EfficiencyModel getEfficiencyModel() {
        if (efficiencyModel == null) efficiencyModel = createEfficiencyModel();
        return efficiencyModel;
    }

    protected EfficiencyModel createEfficiencyModel() { return new EfficiencyModel(); }

    protected class EfficiencyModel extends ilarkesto.gwt.client.editor.AFloatEditorModel {

        @Override
        public String getId() {
            return "TeamMemberSnapshot_efficiency";
        }

        @Override
        public java.lang.Float getValue() {
            return getEfficiency();
        }

        @Override
        public void setValue(java.lang.Float value) {
            setEfficiency(value);
        }

        @Override
        protected void onChangeValue(java.lang.Float oldValue, java.lang.Float newValue) {
            super.onChangeValue(oldValue, newValue);
            addUndo(this, oldValue);
        }

    }

    // --- update properties by map ---

    public void updateProperties(Map props) {
        sprintId = (String) props.get("sprintId");
        teamMemberId = (String) props.get("teamMemberId");
        initialWork  = (Integer) props.get("initialWork");
        burnedWork  = (Integer) props.get("burnedWork");
        efficiency  = (java.lang.Float) props.get("efficiency");
        updateLocalModificationTime();
    }

    @Override
    public void storeProperties(Map properties) {
        super.storeProperties(properties);
        properties.put("sprintId", this.sprintId);
        properties.put("teamMemberId", this.teamMemberId);
        properties.put("initialWork", this.initialWork);
        properties.put("burnedWork", this.burnedWork);
        properties.put("efficiency", this.efficiency);
    }

    public final java.util.List<scrum.client.sprint.SprintReport> getSprintReports() {
        return getDao().getSprintReportsByTeamMemberStatistic((TeamMemberSnapshot)this);
    }

}