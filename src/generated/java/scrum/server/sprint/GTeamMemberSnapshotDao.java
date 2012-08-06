// ----------> GENERATED FILE - DON'T TOUCH! <----------

// generator: ilarkesto.mda.legacy.generator.DaoGenerator










package scrum.server.sprint;

import java.util.*;
import ilarkesto.persistence.*;
import ilarkesto.core.logging.Log;
import ilarkesto.base.*;
import ilarkesto.base.time.*;
import ilarkesto.auth.*;
import ilarkesto.fp.*;

public abstract class GTeamMemberSnapshotDao
            extends ilarkesto.persistence.ADao<TeamMemberSnapshot> {

    public final String getEntityName() {
        return TeamMemberSnapshot.TYPE;
    }

    public final Class getEntityClass() {
        return TeamMemberSnapshot.class;
    }

    public Set<TeamMemberSnapshot> getEntitiesVisibleForUser(final scrum.server.admin.User user) {
        return getEntities(new Predicate<TeamMemberSnapshot>() {
            public boolean test(TeamMemberSnapshot e) {
                return Auth.isVisible(e, user);
            }
        });
    }

    // --- clear caches ---
    public void clearCaches() {
        teamMemberSnapshotsBySprintCache.clear();
        sprintsCache = null;
        teamMemberSnapshotsByTeamMemberCache.clear();
        teamMembersCache = null;
        teamMemberSnapshotsByInitialWorkCache.clear();
        initialWorksCache = null;
        teamMemberSnapshotsByBurnedWorkCache.clear();
        burnedWorksCache = null;
        teamMemberSnapshotsByEfficiencyCache.clear();
        efficiencysCache = null;
    }

    @Override
    public void entityDeleted(EntityEvent event) {
        super.entityDeleted(event);
        if (event.getEntity() instanceof TeamMemberSnapshot) {
            clearCaches();
        }
    }

    @Override
    public void entitySaved(EntityEvent event) {
        super.entitySaved(event);
        if (event.getEntity() instanceof TeamMemberSnapshot) {
            clearCaches();
        }
    }

    // -----------------------------------------------------------
    // - sprint
    // -----------------------------------------------------------

    private final Cache<scrum.server.sprint.Sprint,Set<TeamMemberSnapshot>> teamMemberSnapshotsBySprintCache = new Cache<scrum.server.sprint.Sprint,Set<TeamMemberSnapshot>>(
            new Cache.Factory<scrum.server.sprint.Sprint,Set<TeamMemberSnapshot>>() {
                public Set<TeamMemberSnapshot> create(scrum.server.sprint.Sprint sprint) {
                    return getEntities(new IsSprint(sprint));
                }
            });

    public final Set<TeamMemberSnapshot> getTeamMemberSnapshotsBySprint(scrum.server.sprint.Sprint sprint) {
        return new HashSet<TeamMemberSnapshot>(teamMemberSnapshotsBySprintCache.get(sprint));
    }
    private Set<scrum.server.sprint.Sprint> sprintsCache;

    public final Set<scrum.server.sprint.Sprint> getSprints() {
        if (sprintsCache == null) {
            sprintsCache = new HashSet<scrum.server.sprint.Sprint>();
            for (TeamMemberSnapshot e : getEntities()) {
                if (e.isSprintSet()) sprintsCache.add(e.getSprint());
            }
        }
        return sprintsCache;
    }

    private static class IsSprint implements Predicate<TeamMemberSnapshot> {

        private scrum.server.sprint.Sprint value;

        public IsSprint(scrum.server.sprint.Sprint value) {
            this.value = value;
        }

        public boolean test(TeamMemberSnapshot e) {
            return e.isSprint(value);
        }

    }

    // -----------------------------------------------------------
    // - teamMember
    // -----------------------------------------------------------

    private final Cache<scrum.server.admin.User,Set<TeamMemberSnapshot>> teamMemberSnapshotsByTeamMemberCache = new Cache<scrum.server.admin.User,Set<TeamMemberSnapshot>>(
            new Cache.Factory<scrum.server.admin.User,Set<TeamMemberSnapshot>>() {
                public Set<TeamMemberSnapshot> create(scrum.server.admin.User teamMember) {
                    return getEntities(new IsTeamMember(teamMember));
                }
            });

    public final Set<TeamMemberSnapshot> getTeamMemberSnapshotsByTeamMember(scrum.server.admin.User teamMember) {
        return new HashSet<TeamMemberSnapshot>(teamMemberSnapshotsByTeamMemberCache.get(teamMember));
    }
    private Set<scrum.server.admin.User> teamMembersCache;

    public final Set<scrum.server.admin.User> getTeamMembers() {
        if (teamMembersCache == null) {
            teamMembersCache = new HashSet<scrum.server.admin.User>();
            for (TeamMemberSnapshot e : getEntities()) {
                if (e.isTeamMemberSet()) teamMembersCache.add(e.getTeamMember());
            }
        }
        return teamMembersCache;
    }

    private static class IsTeamMember implements Predicate<TeamMemberSnapshot> {

        private scrum.server.admin.User value;

        public IsTeamMember(scrum.server.admin.User value) {
            this.value = value;
        }

        public boolean test(TeamMemberSnapshot e) {
            return e.isTeamMember(value);
        }

    }

    // -----------------------------------------------------------
    // - initialWork
    // -----------------------------------------------------------

    private final Cache<Integer,Set<TeamMemberSnapshot>> teamMemberSnapshotsByInitialWorkCache = new Cache<Integer,Set<TeamMemberSnapshot>>(
            new Cache.Factory<Integer,Set<TeamMemberSnapshot>>() {
                public Set<TeamMemberSnapshot> create(Integer initialWork) {
                    return getEntities(new IsInitialWork(initialWork));
                }
            });

    public final Set<TeamMemberSnapshot> getTeamMemberSnapshotsByInitialWork(int initialWork) {
        return new HashSet<TeamMemberSnapshot>(teamMemberSnapshotsByInitialWorkCache.get(initialWork));
    }
    private Set<Integer> initialWorksCache;

    public final Set<Integer> getInitialWorks() {
        if (initialWorksCache == null) {
            initialWorksCache = new HashSet<Integer>();
            for (TeamMemberSnapshot e : getEntities()) {
                initialWorksCache.add(e.getInitialWork());
            }
        }
        return initialWorksCache;
    }

    private static class IsInitialWork implements Predicate<TeamMemberSnapshot> {

        private int value;

        public IsInitialWork(int value) {
            this.value = value;
        }

        public boolean test(TeamMemberSnapshot e) {
            return e.isInitialWork(value);
        }

    }

    // -----------------------------------------------------------
    // - burnedWork
    // -----------------------------------------------------------

    private final Cache<Integer,Set<TeamMemberSnapshot>> teamMemberSnapshotsByBurnedWorkCache = new Cache<Integer,Set<TeamMemberSnapshot>>(
            new Cache.Factory<Integer,Set<TeamMemberSnapshot>>() {
                public Set<TeamMemberSnapshot> create(Integer burnedWork) {
                    return getEntities(new IsBurnedWork(burnedWork));
                }
            });

    public final Set<TeamMemberSnapshot> getTeamMemberSnapshotsByBurnedWork(int burnedWork) {
        return new HashSet<TeamMemberSnapshot>(teamMemberSnapshotsByBurnedWorkCache.get(burnedWork));
    }
    private Set<Integer> burnedWorksCache;

    public final Set<Integer> getBurnedWorks() {
        if (burnedWorksCache == null) {
            burnedWorksCache = new HashSet<Integer>();
            for (TeamMemberSnapshot e : getEntities()) {
                burnedWorksCache.add(e.getBurnedWork());
            }
        }
        return burnedWorksCache;
    }

    private static class IsBurnedWork implements Predicate<TeamMemberSnapshot> {

        private int value;

        public IsBurnedWork(int value) {
            this.value = value;
        }

        public boolean test(TeamMemberSnapshot e) {
            return e.isBurnedWork(value);
        }

    }

    // -----------------------------------------------------------
    // - efficiency
    // -----------------------------------------------------------

    private final Cache<java.lang.Float,Set<TeamMemberSnapshot>> teamMemberSnapshotsByEfficiencyCache = new Cache<java.lang.Float,Set<TeamMemberSnapshot>>(
            new Cache.Factory<java.lang.Float,Set<TeamMemberSnapshot>>() {
                public Set<TeamMemberSnapshot> create(java.lang.Float efficiency) {
                    return getEntities(new IsEfficiency(efficiency));
                }
            });

    public final Set<TeamMemberSnapshot> getTeamMemberSnapshotsByEfficiency(java.lang.Float efficiency) {
        return new HashSet<TeamMemberSnapshot>(teamMemberSnapshotsByEfficiencyCache.get(efficiency));
    }
    private Set<java.lang.Float> efficiencysCache;

    public final Set<java.lang.Float> getEfficiencys() {
        if (efficiencysCache == null) {
            efficiencysCache = new HashSet<java.lang.Float>();
            for (TeamMemberSnapshot e : getEntities()) {
                if (e.isEfficiencySet()) efficiencysCache.add(e.getEfficiency());
            }
        }
        return efficiencysCache;
    }

    private static class IsEfficiency implements Predicate<TeamMemberSnapshot> {

        private java.lang.Float value;

        public IsEfficiency(java.lang.Float value) {
            this.value = value;
        }

        public boolean test(TeamMemberSnapshot e) {
            return e.isEfficiency(value);
        }

    }

    // --- valueObject classes ---
    @Override
    protected Set<Class> getValueObjectClasses() {
        Set<Class> ret = new HashSet<Class>(super.getValueObjectClasses());
        return ret;
    }

    @Override
    public Map<String, Class> getAliases() {
        Map<String, Class> aliases = new HashMap<String, Class>(super.getAliases());
        return aliases;
    }

    // --- dependencies ---

    scrum.server.sprint.SprintDao sprintDao;

    public void setSprintDao(scrum.server.sprint.SprintDao sprintDao) {
        this.sprintDao = sprintDao;
    }

}