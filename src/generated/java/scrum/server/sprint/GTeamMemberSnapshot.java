// ----------> GENERATED FILE - DON'T TOUCH! <----------

// generator: ilarkesto.mda.legacy.generator.EntityGenerator










package scrum.server.sprint;

import java.util.*;
import ilarkesto.core.logging.Log;
import ilarkesto.persistence.ADatob;
import ilarkesto.persistence.AEntity;
import ilarkesto.persistence.AStructure;
import ilarkesto.auth.AUser;
import ilarkesto.persistence.EntityDoesNotExistException;
import ilarkesto.base.Str;

public abstract class GTeamMemberSnapshot
            extends AEntity
            implements ilarkesto.auth.ViewProtected<scrum.server.admin.User>, java.lang.Comparable<TeamMemberSnapshot> {

    // --- AEntity ---

    public final scrum.server.sprint.TeamMemberSnapshotDao getDao() {
        return teamMemberSnapshotDao;
    }

    protected void repairDeadDatob(ADatob datob) {
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

    public int compareTo(TeamMemberSnapshot other) {
        return toString().toLowerCase().compareTo(other.toString().toLowerCase());
    }

    public final java.util.Set<scrum.server.sprint.SprintReport> getSprintReports() {
        return sprintReportDao.getSprintReportsByTeamMemberStatistic((TeamMemberSnapshot)this);
    }

    private static final ilarkesto.core.logging.Log LOG = ilarkesto.core.logging.Log.get(GTeamMemberSnapshot.class);

    public static final String TYPE = "teamMemberSnapshot";

    // -----------------------------------------------------------
    // - sprint
    // -----------------------------------------------------------

    private String sprintId;
    private transient scrum.server.sprint.Sprint sprintCache;

    private void updateSprintCache() {
        sprintCache = this.sprintId == null ? null : (scrum.server.sprint.Sprint)sprintDao.getById(this.sprintId);
    }

    public final String getSprintId() {
        return this.sprintId;
    }

    public final scrum.server.sprint.Sprint getSprint() {
        if (sprintCache == null) updateSprintCache();
        return sprintCache;
    }

    public final void setSprint(scrum.server.sprint.Sprint sprint) {
        sprint = prepareSprint(sprint);
        if (isSprint(sprint)) return;
        this.sprintId = sprint == null ? null : sprint.getId();
        sprintCache = sprint;
        updateLastModified();
        fireModified("sprint="+sprint);
    }

    protected scrum.server.sprint.Sprint prepareSprint(scrum.server.sprint.Sprint sprint) {
        return sprint;
    }

    protected void repairDeadSprintReference(String entityId) {
        if (this.sprintId == null || entityId.equals(this.sprintId)) {
            repairMissingMaster();
        }
    }

    public final boolean isSprintSet() {
        return this.sprintId != null;
    }

    public final boolean isSprint(scrum.server.sprint.Sprint sprint) {
        if (this.sprintId == null && sprint == null) return true;
        return sprint != null && sprint.getId().equals(this.sprintId);
    }

    protected final void updateSprint(Object value) {
        setSprint(value == null ? null : (scrum.server.sprint.Sprint)sprintDao.getById((String)value));
    }

    // -----------------------------------------------------------
    // - teamMember
    // -----------------------------------------------------------

    private String teamMemberId;
    private transient scrum.server.admin.User teamMemberCache;

    private void updateTeamMemberCache() {
        teamMemberCache = this.teamMemberId == null ? null : (scrum.server.admin.User)userDao.getById(this.teamMemberId);
    }

    public final String getTeamMemberId() {
        return this.teamMemberId;
    }

    public final scrum.server.admin.User getTeamMember() {
        if (teamMemberCache == null) updateTeamMemberCache();
        return teamMemberCache;
    }

    public final void setTeamMember(scrum.server.admin.User teamMember) {
        teamMember = prepareTeamMember(teamMember);
        if (isTeamMember(teamMember)) return;
        this.teamMemberId = teamMember == null ? null : teamMember.getId();
        teamMemberCache = teamMember;
        updateLastModified();
        fireModified("teamMember="+teamMember);
    }

    protected scrum.server.admin.User prepareTeamMember(scrum.server.admin.User teamMember) {
        return teamMember;
    }

    protected void repairDeadTeamMemberReference(String entityId) {
        if (this.teamMemberId == null || entityId.equals(this.teamMemberId)) {
            setTeamMember(null);
        }
    }

    public final boolean isTeamMemberSet() {
        return this.teamMemberId != null;
    }

    public final boolean isTeamMember(scrum.server.admin.User teamMember) {
        if (this.teamMemberId == null && teamMember == null) return true;
        return teamMember != null && teamMember.getId().equals(this.teamMemberId);
    }

    protected final void updateTeamMember(Object value) {
        setTeamMember(value == null ? null : (scrum.server.admin.User)userDao.getById((String)value));
    }

    // -----------------------------------------------------------
    // - initialWork
    // -----------------------------------------------------------

    private int initialWork;

    public final int getInitialWork() {
        return initialWork;
    }

    public final void setInitialWork(int initialWork) {
        initialWork = prepareInitialWork(initialWork);
        if (isInitialWork(initialWork)) return;
        this.initialWork = initialWork;
        updateLastModified();
        fireModified("initialWork="+initialWork);
    }

    protected int prepareInitialWork(int initialWork) {
        return initialWork;
    }

    public final boolean isInitialWork(int initialWork) {
        return this.initialWork == initialWork;
    }

    protected final void updateInitialWork(Object value) {
        setInitialWork((Integer)value);
    }

    // -----------------------------------------------------------
    // - burnedWork
    // -----------------------------------------------------------

    private int burnedWork;

    public final int getBurnedWork() {
        return burnedWork;
    }

    public final void setBurnedWork(int burnedWork) {
        burnedWork = prepareBurnedWork(burnedWork);
        if (isBurnedWork(burnedWork)) return;
        this.burnedWork = burnedWork;
        updateLastModified();
        fireModified("burnedWork="+burnedWork);
    }

    protected int prepareBurnedWork(int burnedWork) {
        return burnedWork;
    }

    public final boolean isBurnedWork(int burnedWork) {
        return this.burnedWork == burnedWork;
    }

    protected final void updateBurnedWork(Object value) {
        setBurnedWork((Integer)value);
    }

    // -----------------------------------------------------------
    // - efficiency
    // -----------------------------------------------------------

    private java.lang.Float efficiency;

    public final java.lang.Float getEfficiency() {
        return efficiency;
    }

    public final void setEfficiency(java.lang.Float efficiency) {
        efficiency = prepareEfficiency(efficiency);
        if (isEfficiency(efficiency)) return;
        this.efficiency = efficiency;
        updateLastModified();
        fireModified("efficiency="+efficiency);
    }

    protected java.lang.Float prepareEfficiency(java.lang.Float efficiency) {
        return efficiency;
    }

    public final boolean isEfficiencySet() {
        return this.efficiency != null;
    }

    public final boolean isEfficiency(java.lang.Float efficiency) {
        if (this.efficiency == null && efficiency == null) return true;
        return this.efficiency != null && this.efficiency.equals(efficiency);
    }

    protected final void updateEfficiency(Object value) {
        setEfficiency((java.lang.Float)value);
    }

    public void updateProperties(Map<?, ?> properties) {
        for (Map.Entry entry : properties.entrySet()) {
            String property = (String) entry.getKey();
            if (property.equals("id")) continue;
            Object value = entry.getValue();
            if (property.equals("sprintId")) updateSprint(value);
            if (property.equals("teamMemberId")) updateTeamMember(value);
            if (property.equals("initialWork")) updateInitialWork(value);
            if (property.equals("burnedWork")) updateBurnedWork(value);
            if (property.equals("efficiency")) updateEfficiency(value);
        }
    }

    protected void repairDeadReferences(String entityId) {
        super.repairDeadReferences(entityId);
        repairDeadSprintReference(entityId);
        repairDeadTeamMemberReference(entityId);
    }

    // --- ensure integrity ---

    public void ensureIntegrity() {
        super.ensureIntegrity();
        if (!isSprintSet()) {
            repairMissingMaster();
            return;
        }
        try {
            getSprint();
        } catch (EntityDoesNotExistException ex) {
            LOG.info("Repairing dead sprint reference");
            repairDeadSprintReference(this.sprintId);
        }
        try {
            getTeamMember();
        } catch (EntityDoesNotExistException ex) {
            LOG.info("Repairing dead teamMember reference");
            repairDeadTeamMemberReference(this.teamMemberId);
        }
    }


    // -----------------------------------------------------------
    // - dependencies
    // -----------------------------------------------------------

    static scrum.server.sprint.SprintDao sprintDao;

    public static final void setSprintDao(scrum.server.sprint.SprintDao sprintDao) {
        GTeamMemberSnapshot.sprintDao = sprintDao;
    }

    static scrum.server.sprint.TeamMemberSnapshotDao teamMemberSnapshotDao;

    public static final void setTeamMemberSnapshotDao(scrum.server.sprint.TeamMemberSnapshotDao teamMemberSnapshotDao) {
        GTeamMemberSnapshot.teamMemberSnapshotDao = teamMemberSnapshotDao;
    }

    static scrum.server.sprint.SprintReportDao sprintReportDao;

    public static final void setSprintReportDao(scrum.server.sprint.SprintReportDao sprintReportDao) {
        GTeamMemberSnapshot.sprintReportDao = sprintReportDao;
    }

}