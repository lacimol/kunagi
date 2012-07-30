// ----------> GENERATED FILE - DON'T TOUCH! <----------

// generator: ilarkesto.mda.legacy.generator.DaoGenerator










package scrum.server.task;

import java.util.*;
import ilarkesto.persistence.*;
import ilarkesto.core.logging.Log;
import ilarkesto.base.*;
import ilarkesto.base.time.*;
import ilarkesto.auth.*;
import ilarkesto.fp.*;

public abstract class GTaskDaySnapshotDao
            extends ilarkesto.persistence.ADao<TaskDaySnapshot> {

    public final String getEntityName() {
        return TaskDaySnapshot.TYPE;
    }

    public final Class getEntityClass() {
        return TaskDaySnapshot.class;
    }

    public Set<TaskDaySnapshot> getEntitiesVisibleForUser(final scrum.server.admin.User user) {
        return getEntities(new Predicate<TaskDaySnapshot>() {
            public boolean test(TaskDaySnapshot e) {
                return Auth.isVisible(e, user);
            }
        });
    }

    // --- clear caches ---
    public void clearCaches() {
        taskDaySnapshotsByTaskCache.clear();
        tasksCache = null;
        taskDaySnapshotsByDateCache.clear();
        datesCache = null;
        taskDaySnapshotsByRemainingWorkCache.clear();
        remainingWorksCache = null;
        taskDaySnapshotsByBurnedWorkCache.clear();
        burnedWorksCache = null;
        taskDaySnapshotsByOwnerCache.clear();
        ownersCache = null;
    }

    @Override
    public void entityDeleted(EntityEvent event) {
        super.entityDeleted(event);
        if (event.getEntity() instanceof TaskDaySnapshot) {
            clearCaches();
        }
    }

    @Override
    public void entitySaved(EntityEvent event) {
        super.entitySaved(event);
        if (event.getEntity() instanceof TaskDaySnapshot) {
            clearCaches();
        }
    }

    // -----------------------------------------------------------
    // - task
    // -----------------------------------------------------------

    private final Cache<scrum.server.sprint.Task,Set<TaskDaySnapshot>> taskDaySnapshotsByTaskCache = new Cache<scrum.server.sprint.Task,Set<TaskDaySnapshot>>(
            new Cache.Factory<scrum.server.sprint.Task,Set<TaskDaySnapshot>>() {
                public Set<TaskDaySnapshot> create(scrum.server.sprint.Task task) {
                    return getEntities(new IsTask(task));
                }
            });

    public final Set<TaskDaySnapshot> getTaskDaySnapshotsByTask(scrum.server.sprint.Task task) {
        return new HashSet<TaskDaySnapshot>(taskDaySnapshotsByTaskCache.get(task));
    }
    private Set<scrum.server.sprint.Task> tasksCache;

    public final Set<scrum.server.sprint.Task> getTasks() {
        if (tasksCache == null) {
            tasksCache = new HashSet<scrum.server.sprint.Task>();
            for (TaskDaySnapshot e : getEntities()) {
                if (e.isTaskSet()) tasksCache.add(e.getTask());
            }
        }
        return tasksCache;
    }

    private static class IsTask implements Predicate<TaskDaySnapshot> {

        private scrum.server.sprint.Task value;

        public IsTask(scrum.server.sprint.Task value) {
            this.value = value;
        }

        public boolean test(TaskDaySnapshot e) {
            return e.isTask(value);
        }

    }

    // -----------------------------------------------------------
    // - date
    // -----------------------------------------------------------

    private final Cache<ilarkesto.base.time.Date,Set<TaskDaySnapshot>> taskDaySnapshotsByDateCache = new Cache<ilarkesto.base.time.Date,Set<TaskDaySnapshot>>(
            new Cache.Factory<ilarkesto.base.time.Date,Set<TaskDaySnapshot>>() {
                public Set<TaskDaySnapshot> create(ilarkesto.base.time.Date date) {
                    return getEntities(new IsDate(date));
                }
            });

    public final Set<TaskDaySnapshot> getTaskDaySnapshotsByDate(ilarkesto.base.time.Date date) {
        return new HashSet<TaskDaySnapshot>(taskDaySnapshotsByDateCache.get(date));
    }
    private Set<ilarkesto.base.time.Date> datesCache;

    public final Set<ilarkesto.base.time.Date> getDates() {
        if (datesCache == null) {
            datesCache = new HashSet<ilarkesto.base.time.Date>();
            for (TaskDaySnapshot e : getEntities()) {
                if (e.isDateSet()) datesCache.add(e.getDate());
            }
        }
        return datesCache;
    }

    private static class IsDate implements Predicate<TaskDaySnapshot> {

        private ilarkesto.base.time.Date value;

        public IsDate(ilarkesto.base.time.Date value) {
            this.value = value;
        }

        public boolean test(TaskDaySnapshot e) {
            return e.isDate(value);
        }

    }

    // -----------------------------------------------------------
    // - remainingWork
    // -----------------------------------------------------------

    private final Cache<Integer,Set<TaskDaySnapshot>> taskDaySnapshotsByRemainingWorkCache = new Cache<Integer,Set<TaskDaySnapshot>>(
            new Cache.Factory<Integer,Set<TaskDaySnapshot>>() {
                public Set<TaskDaySnapshot> create(Integer remainingWork) {
                    return getEntities(new IsRemainingWork(remainingWork));
                }
            });

    public final Set<TaskDaySnapshot> getTaskDaySnapshotsByRemainingWork(int remainingWork) {
        return new HashSet<TaskDaySnapshot>(taskDaySnapshotsByRemainingWorkCache.get(remainingWork));
    }
    private Set<Integer> remainingWorksCache;

    public final Set<Integer> getRemainingWorks() {
        if (remainingWorksCache == null) {
            remainingWorksCache = new HashSet<Integer>();
            for (TaskDaySnapshot e : getEntities()) {
                remainingWorksCache.add(e.getRemainingWork());
            }
        }
        return remainingWorksCache;
    }

    private static class IsRemainingWork implements Predicate<TaskDaySnapshot> {

        private int value;

        public IsRemainingWork(int value) {
            this.value = value;
        }

        public boolean test(TaskDaySnapshot e) {
            return e.isRemainingWork(value);
        }

    }

    // -----------------------------------------------------------
    // - burnedWork
    // -----------------------------------------------------------

    private final Cache<Integer,Set<TaskDaySnapshot>> taskDaySnapshotsByBurnedWorkCache = new Cache<Integer,Set<TaskDaySnapshot>>(
            new Cache.Factory<Integer,Set<TaskDaySnapshot>>() {
                public Set<TaskDaySnapshot> create(Integer burnedWork) {
                    return getEntities(new IsBurnedWork(burnedWork));
                }
            });

    public final Set<TaskDaySnapshot> getTaskDaySnapshotsByBurnedWork(int burnedWork) {
        return new HashSet<TaskDaySnapshot>(taskDaySnapshotsByBurnedWorkCache.get(burnedWork));
    }
    private Set<Integer> burnedWorksCache;

    public final Set<Integer> getBurnedWorks() {
        if (burnedWorksCache == null) {
            burnedWorksCache = new HashSet<Integer>();
            for (TaskDaySnapshot e : getEntities()) {
                burnedWorksCache.add(e.getBurnedWork());
            }
        }
        return burnedWorksCache;
    }

    private static class IsBurnedWork implements Predicate<TaskDaySnapshot> {

        private int value;

        public IsBurnedWork(int value) {
            this.value = value;
        }

        public boolean test(TaskDaySnapshot e) {
            return e.isBurnedWork(value);
        }

    }

    // -----------------------------------------------------------
    // - owner
    // -----------------------------------------------------------

    private final Cache<scrum.server.admin.User,Set<TaskDaySnapshot>> taskDaySnapshotsByOwnerCache = new Cache<scrum.server.admin.User,Set<TaskDaySnapshot>>(
            new Cache.Factory<scrum.server.admin.User,Set<TaskDaySnapshot>>() {
                public Set<TaskDaySnapshot> create(scrum.server.admin.User owner) {
                    return getEntities(new IsOwner(owner));
                }
            });

    public final Set<TaskDaySnapshot> getTaskDaySnapshotsByOwner(scrum.server.admin.User owner) {
        return new HashSet<TaskDaySnapshot>(taskDaySnapshotsByOwnerCache.get(owner));
    }
    private Set<scrum.server.admin.User> ownersCache;

    public final Set<scrum.server.admin.User> getOwners() {
        if (ownersCache == null) {
            ownersCache = new HashSet<scrum.server.admin.User>();
            for (TaskDaySnapshot e : getEntities()) {
                if (e.isOwnerSet()) ownersCache.add(e.getOwner());
            }
        }
        return ownersCache;
    }

    private static class IsOwner implements Predicate<TaskDaySnapshot> {

        private scrum.server.admin.User value;

        public IsOwner(scrum.server.admin.User value) {
            this.value = value;
        }

        public boolean test(TaskDaySnapshot e) {
            return e.isOwner(value);
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

    scrum.server.sprint.TaskDao taskDao;

    public void setTaskDao(scrum.server.sprint.TaskDao taskDao) {
        this.taskDao = taskDao;
    }

}