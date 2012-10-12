// ----------> GENERATED FILE - DON'T TOUCH! <----------

// generator: ilarkesto.mda.legacy.generator.EntityGenerator










package scrum.server.task;

import java.util.*;
import ilarkesto.core.logging.Log;
import ilarkesto.persistence.ADatob;
import ilarkesto.persistence.AEntity;
import ilarkesto.persistence.AStructure;
import ilarkesto.auth.AUser;
import ilarkesto.persistence.EntityDoesNotExistException;
import ilarkesto.base.Str;

public abstract class GTaskDaySnapshot
            extends AEntity
            implements ilarkesto.auth.ViewProtected<scrum.server.admin.User>, java.lang.Comparable<TaskDaySnapshot> {

    // --- AEntity ---

    public final scrum.server.task.TaskDaySnapshotDao getDao() {
        return taskDaySnapshotDao;
    }

    protected void repairDeadDatob(ADatob datob) {
    }

    @Override
    public void storeProperties(Map properties) {
        super.storeProperties(properties);
        properties.put("taskId", this.taskId);
        properties.put("date", this.date == null ? null : this.date.toString());
        properties.put("remainingWork", this.remainingWork);
        properties.put("burnedWork", this.burnedWork);
        properties.put("ownerId", this.ownerId);
    }

    public int compareTo(TaskDaySnapshot other) {
        return toString().toLowerCase().compareTo(other.toString().toLowerCase());
    }

    private static final ilarkesto.core.logging.Log LOG = ilarkesto.core.logging.Log.get(GTaskDaySnapshot.class);

    public static final String TYPE = "taskDaySnapshot";

    // -----------------------------------------------------------
    // - task
    // -----------------------------------------------------------

    private String taskId;
    private transient scrum.server.sprint.Task taskCache;

    private void updateTaskCache() {
        taskCache = this.taskId == null ? null : (scrum.server.sprint.Task)taskDao.getById(this.taskId);
    }

    public final String getTaskId() {
        return this.taskId;
    }

    public final scrum.server.sprint.Task getTask() {
        if (taskCache == null) updateTaskCache();
        return taskCache;
    }

    public final void setTask(scrum.server.sprint.Task task) {
        task = prepareTask(task);
        if (isTask(task)) return;
        this.taskId = task == null ? null : task.getId();
        taskCache = task;
        updateLastModified();
        fireModified("task="+task);
    }

    protected scrum.server.sprint.Task prepareTask(scrum.server.sprint.Task task) {
        return task;
    }

    protected void repairDeadTaskReference(String entityId) {
        if (this.taskId == null || entityId.equals(this.taskId)) {
            repairMissingMaster();
        }
    }

    public final boolean isTaskSet() {
        return this.taskId != null;
    }

    public final boolean isTask(scrum.server.sprint.Task task) {
        if (this.taskId == null && task == null) return true;
        return task != null && task.getId().equals(this.taskId);
    }

    protected final void updateTask(Object value) {
        setTask(value == null ? null : (scrum.server.sprint.Task)taskDao.getById((String)value));
    }

    // -----------------------------------------------------------
    // - date
    // -----------------------------------------------------------

    private ilarkesto.core.time.Date date;

    public final ilarkesto.core.time.Date getDate() {
        return date;
    }

    public final void setDate(ilarkesto.core.time.Date date) {
        date = prepareDate(date);
        if (isDate(date)) return;
        this.date = date;
        updateLastModified();
        fireModified("date="+date);
    }

    protected ilarkesto.core.time.Date prepareDate(ilarkesto.core.time.Date date) {
        return date;
    }

    public final boolean isDateSet() {
        return this.date != null;
    }

    public final boolean isDate(ilarkesto.core.time.Date date) {
        if (this.date == null && date == null) return true;
        return this.date != null && this.date.equals(date);
    }

    protected final void updateDate(Object value) {
        value = value == null ? null : new ilarkesto.core.time.Date((String)value);
        setDate((ilarkesto.core.time.Date)value);
    }

    // -----------------------------------------------------------
    // - remainingWork
    // -----------------------------------------------------------

    private int remainingWork;

    public final int getRemainingWork() {
        return remainingWork;
    }

    public final void setRemainingWork(int remainingWork) {
        remainingWork = prepareRemainingWork(remainingWork);
        if (isRemainingWork(remainingWork)) return;
        this.remainingWork = remainingWork;
        updateLastModified();
        fireModified("remainingWork="+remainingWork);
    }

    protected int prepareRemainingWork(int remainingWork) {
        return remainingWork;
    }

    public final boolean isRemainingWork(int remainingWork) {
        return this.remainingWork == remainingWork;
    }

    protected final void updateRemainingWork(Object value) {
        setRemainingWork((Integer)value);
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
    // - owner
    // -----------------------------------------------------------

    private String ownerId;
    private transient scrum.server.admin.User ownerCache;

    private void updateOwnerCache() {
        ownerCache = this.ownerId == null ? null : (scrum.server.admin.User)userDao.getById(this.ownerId);
    }

    public final String getOwnerId() {
        return this.ownerId;
    }

    public final scrum.server.admin.User getOwner() {
        if (ownerCache == null) updateOwnerCache();
        return ownerCache;
    }

    public final void setOwner(scrum.server.admin.User owner) {
        owner = prepareOwner(owner);
        if (isOwner(owner)) return;
        this.ownerId = owner == null ? null : owner.getId();
        ownerCache = owner;
        updateLastModified();
        fireModified("owner="+owner);
    }

    protected scrum.server.admin.User prepareOwner(scrum.server.admin.User owner) {
        return owner;
    }

    protected void repairDeadOwnerReference(String entityId) {
        if (this.ownerId == null || entityId.equals(this.ownerId)) {
            setOwner(null);
        }
    }

    public final boolean isOwnerSet() {
        return this.ownerId != null;
    }

    public final boolean isOwner(scrum.server.admin.User owner) {
        if (this.ownerId == null && owner == null) return true;
        return owner != null && owner.getId().equals(this.ownerId);
    }

    protected final void updateOwner(Object value) {
        setOwner(value == null ? null : (scrum.server.admin.User)userDao.getById((String)value));
    }

    public void updateProperties(Map<?, ?> properties) {
        for (Map.Entry entry : properties.entrySet()) {
            String property = (String) entry.getKey();
            if (property.equals("id")) continue;
            Object value = entry.getValue();
            if (property.equals("taskId")) updateTask(value);
            if (property.equals("date")) updateDate(value);
            if (property.equals("remainingWork")) updateRemainingWork(value);
            if (property.equals("burnedWork")) updateBurnedWork(value);
            if (property.equals("ownerId")) updateOwner(value);
        }
    }

    protected void repairDeadReferences(String entityId) {
        super.repairDeadReferences(entityId);
        repairDeadTaskReference(entityId);
        repairDeadOwnerReference(entityId);
    }

    // --- ensure integrity ---

    public void ensureIntegrity() {
        super.ensureIntegrity();
        if (!isTaskSet()) {
            repairMissingMaster();
            return;
        }
        try {
            getTask();
        } catch (EntityDoesNotExistException ex) {
            LOG.info("Repairing dead task reference");
            repairDeadTaskReference(this.taskId);
        }
        try {
            getOwner();
        } catch (EntityDoesNotExistException ex) {
            LOG.info("Repairing dead owner reference");
            repairDeadOwnerReference(this.ownerId);
        }
    }


    // -----------------------------------------------------------
    // - dependencies
    // -----------------------------------------------------------

    static scrum.server.sprint.TaskDao taskDao;

    public static final void setTaskDao(scrum.server.sprint.TaskDao taskDao) {
        GTaskDaySnapshot.taskDao = taskDao;
    }

    static scrum.server.task.TaskDaySnapshotDao taskDaySnapshotDao;

    public static final void setTaskDaySnapshotDao(scrum.server.task.TaskDaySnapshotDao taskDaySnapshotDao) {
        GTaskDaySnapshot.taskDaySnapshotDao = taskDaySnapshotDao;
    }

}