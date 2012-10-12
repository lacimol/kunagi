// ----------> GENERATED FILE - DON'T TOUCH! <----------

// generator: ilarkesto.mda.legacy.generator.GwtEntityGenerator










package scrum.client.task;

import java.util.*;
import ilarkesto.core.logging.Log;
import scrum.client.common.*;
import ilarkesto.gwt.client.*;

public abstract class GTaskDaySnapshot
            extends scrum.client.common.AScrumGwtEntity {

    protected scrum.client.Dao getDao() {
        return scrum.client.Dao.get();
    }

    public GTaskDaySnapshot() {
    }

    public GTaskDaySnapshot(Map data) {
        super(data);
        updateProperties(data);
    }

    public static final String ENTITY_TYPE = "taskDaySnapshot";

    @Override
    public final String getEntityType() {
        return ENTITY_TYPE;
    }

    // --- task ---

    private String taskId;

    public final scrum.client.sprint.Task getTask() {
        if (taskId == null) return null;
        return getDao().getTask(this.taskId);
    }

    public final boolean isTaskSet() {
        return taskId != null;
    }

    public final TaskDaySnapshot setTask(scrum.client.sprint.Task task) {
        String id = task == null ? null : task.getId();
        if (equals(this.taskId, id)) return (TaskDaySnapshot) this;
        this.taskId = id;
        propertyChanged("taskId", this.taskId);
        return (TaskDaySnapshot)this;
    }

    public final boolean isTask(scrum.client.sprint.Task task) {
        return equals(this.taskId, task);
    }

    // --- date ---

    private ilarkesto.core.time.Date date ;

    public final ilarkesto.core.time.Date getDate() {
        return this.date ;
    }

    public final TaskDaySnapshot setDate(ilarkesto.core.time.Date date) {
        if (isDate(date)) return (TaskDaySnapshot)this;
        this.date = date ;
        propertyChanged("date", this.date);
        return (TaskDaySnapshot)this;
    }

    public final boolean isDate(ilarkesto.core.time.Date date) {
        return equals(this.date, date);
    }

    private transient DateModel dateModel;

    public DateModel getDateModel() {
        if (dateModel == null) dateModel = createDateModel();
        return dateModel;
    }

    protected DateModel createDateModel() { return new DateModel(); }

    protected class DateModel extends ilarkesto.gwt.client.editor.ADateEditorModel {

        @Override
        public String getId() {
            return "TaskDaySnapshot_date";
        }

        @Override
        public ilarkesto.core.time.Date getValue() {
            return getDate();
        }

        @Override
        public void setValue(ilarkesto.core.time.Date value) {
            setDate(value);
        }

        @Override
        protected void onChangeValue(ilarkesto.core.time.Date oldValue, ilarkesto.core.time.Date newValue) {
            super.onChangeValue(oldValue, newValue);
            addUndo(this, oldValue);
        }

    }

    // --- remainingWork ---

    private int remainingWork ;

    public final int getRemainingWork() {
        return this.remainingWork ;
    }

    public final TaskDaySnapshot setRemainingWork(int remainingWork) {
        if (isRemainingWork(remainingWork)) return (TaskDaySnapshot)this;
        this.remainingWork = remainingWork ;
        propertyChanged("remainingWork", this.remainingWork);
        return (TaskDaySnapshot)this;
    }

    public final boolean isRemainingWork(int remainingWork) {
        return equals(this.remainingWork, remainingWork);
    }

    private transient RemainingWorkModel remainingWorkModel;

    public RemainingWorkModel getRemainingWorkModel() {
        if (remainingWorkModel == null) remainingWorkModel = createRemainingWorkModel();
        return remainingWorkModel;
    }

    protected RemainingWorkModel createRemainingWorkModel() { return new RemainingWorkModel(); }

    protected class RemainingWorkModel extends ilarkesto.gwt.client.editor.AIntegerEditorModel {

        @Override
        public String getId() {
            return "TaskDaySnapshot_remainingWork";
        }

        @Override
        public java.lang.Integer getValue() {
            return getRemainingWork();
        }

        @Override
        public void setValue(java.lang.Integer value) {
            setRemainingWork(value);
        }

            @Override
            public void increment() {
                setRemainingWork(getRemainingWork() + 1);
            }

            @Override
            public void decrement() {
                setRemainingWork(getRemainingWork() - 1);
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

    public final TaskDaySnapshot setBurnedWork(int burnedWork) {
        if (isBurnedWork(burnedWork)) return (TaskDaySnapshot)this;
        this.burnedWork = burnedWork ;
        propertyChanged("burnedWork", this.burnedWork);
        return (TaskDaySnapshot)this;
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
            return "TaskDaySnapshot_burnedWork";
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

    // --- owner ---

    private String ownerId;

    public final scrum.client.admin.User getOwner() {
        if (ownerId == null) return null;
        return getDao().getUser(this.ownerId);
    }

    public final boolean isOwnerSet() {
        return ownerId != null;
    }

    public final TaskDaySnapshot setOwner(scrum.client.admin.User owner) {
        String id = owner == null ? null : owner.getId();
        if (equals(this.ownerId, id)) return (TaskDaySnapshot) this;
        this.ownerId = id;
        propertyChanged("ownerId", this.ownerId);
        return (TaskDaySnapshot)this;
    }

    public final boolean isOwner(scrum.client.admin.User owner) {
        return equals(this.ownerId, owner);
    }

    // --- update properties by map ---

    public void updateProperties(Map props) {
        taskId = (String) props.get("taskId");
        String dateAsString = (String) props.get("date");
        date  =  dateAsString == null ? null : new ilarkesto.core.time.Date(dateAsString);
        remainingWork  = (Integer) props.get("remainingWork");
        burnedWork  = (Integer) props.get("burnedWork");
        ownerId = (String) props.get("ownerId");
        updateLocalModificationTime();
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

}