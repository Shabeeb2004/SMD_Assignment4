package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class TaskGrouper {
    public static final String OVERDUE = "Overdue", IN_PROGRESS = "In progress",
            TO_DO = "To do", DONE = "Done";

    /** Fixed group order. */
    public static final String[] ORDER = {OVERDUE, IN_PROGRESS, TO_DO, DONE};

    /** An unfinished task past its due date belongs to Overdue only, never to its status group too. */
    public static String groupOf(Task t, int today) {
        if (t.isOverdue(today)) return OVERDUE;
        if (Task.DONE.equals(t.getStatus())) return DONE;
        if (Task.IN_PROGRESS.equals(t.getStatus())) return IN_PROGRESS;
        return TO_DO;
    }

    /**
     * Groups the given tasks (already filtered and sorted) into header + task rows.
     * Empty groups produce no header. Tasks keep their incoming order inside each group.
     */
    public static List<TaskListItem> build(List<Task> tasks, int today) {
        List<List<Task>> buckets = new ArrayList<>();
        for (int i = 0; i < ORDER.length; i++) buckets.add(new ArrayList<>());

        for (Task t : tasks) buckets.get(indexOf(groupOf(t, today))).add(t);

        List<TaskListItem> out = new ArrayList<>();
        for (int i = 0; i < ORDER.length; i++) {
            List<Task> group = buckets.get(i);
            if (group.isEmpty()) continue;
            out.add(TaskListItem.header(ORDER[i], group.size()));
            for (Task t : group) out.add(TaskListItem.forTask(t, ORDER[i], today));
        }
        return out;
    }

    private static int indexOf(String key) {
        for (int i = 0; i < ORDER.length; i++) if (ORDER[i].equals(key)) return i;
        return ORDER.length - 1;
    }
}