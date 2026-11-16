package com.example.myapplication;

/** One row of the grouped task list: either a section header or a task. */
public class TaskListItem {
    public static final int TYPE_HEADER = 0, TYPE_TASK = 1;

    public final int type;
    public final String groupKey;          // Overdue / In progress / To do / Done
    public final int count;                // headers only: tasks in that group

    // Snapshot of what a task row shows. DiffUtil compares these copies, not the live Task,
    // because the live Task is edited in place and would always look "unchanged".
    public final int taskId;
    public final String description, status, priority;
    public final int dueDate;
    public final boolean overdue, done;

    public final Task task;                // the real object, used only for actions

    private TaskListItem(int type, String groupKey, int count, Task t, int today) {
        this.type = type;
        this.groupKey = groupKey;
        this.count = count;
        this.task = t;
        if (t != null) {
            taskId = t.getId();
            description = t.getDescription();
            status = t.getStatus();
            priority = t.getPriority();
            dueDate = t.getDueDate();
            overdue = t.isOverdue(today);
            done = t.isDone();
        } else {
            taskId = -1; description = ""; status = ""; priority = "";
            dueDate = 0; overdue = false; done = false;
        }
    }

    public static TaskListItem header(String groupKey, int count) {
        return new TaskListItem(TYPE_HEADER, groupKey, count, null, 0);
    }

    public static TaskListItem forTask(Task t, String groupKey, int today) {
        return new TaskListItem(TYPE_TASK, groupKey, 0, t, today);
    }

    public boolean isHeader() { return type == TYPE_HEADER; }

    public boolean sameItemAs(TaskListItem o) {
        if (type != o.type) return false;
        return isHeader() ? groupKey.equals(o.groupKey) : taskId == o.taskId;
    }

    public boolean sameContentAs(TaskListItem o) {
        if (isHeader()) return count == o.count;
        return description.equals(o.description) && status.equals(o.status)
                && priority.equals(o.priority) && dueDate == o.dueDate
                && overdue == o.overdue && done == o.done && groupKey.equals(o.groupKey);
    }
}