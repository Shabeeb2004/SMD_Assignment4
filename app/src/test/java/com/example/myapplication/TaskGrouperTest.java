package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TaskGrouperTest {
    private static final int TODAY = 20260610;

    @Test
    public void groupsInFixedOrderAndSkipsEmptyGroups() {
        List<Task> tasks = Arrays.asList(
                new Task(1, "a", Task.DONE, Task.NORMAL, 0),
                new Task(2, "b", Task.TODO, Task.NORMAL, 20260101),   // past due, unfinished
                new Task(3, "c", Task.IN_PROGRESS, Task.HIGH, 0));

        List<TaskListItem> items = TaskGrouper.build(tasks, TODAY);

        // Overdue, In progress, Done. There is no To do group because it is empty.
        assertEquals(6, items.size());
        assertTrue(items.get(0).isHeader());
        assertEquals(TaskGrouper.OVERDUE, items.get(0).groupKey);
        assertEquals(1, items.get(0).count);
        assertEquals(2, items.get(1).taskId);
        assertEquals(TaskGrouper.IN_PROGRESS, items.get(2).groupKey);
        assertEquals(TaskGrouper.DONE, items.get(4).groupKey);
    }

    @Test
    public void completedTaskWithPastDueDateIsNotOverdue() {
        List<Task> tasks = Arrays.asList(new Task(1, "a", Task.DONE, Task.LOW, 20260101));
        List<TaskListItem> items = TaskGrouper.build(tasks, TODAY);
        assertEquals(2, items.size());
        assertEquals(TaskGrouper.DONE, items.get(0).groupKey);
    }
}