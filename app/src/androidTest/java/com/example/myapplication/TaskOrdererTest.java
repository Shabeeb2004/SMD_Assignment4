package com.example.myapplication;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TaskOrdererTest {

    private static Task task(int id) { return new Task(id, "t" + id, Task.TODO, Task.NORMAL, 0); }

    @Test
    public void visibleTasksTakeDisplayedOrderAndHiddenOnesStay() {
        Task a = task(1), b = task(2), c = task(3), d = task(4);
        List<Task> master = new ArrayList<>(Arrays.asList(a, b, c, d));

        // b is hidden by a filter; the visible tasks were reordered to c, a, d
        TaskOrderer.applyDisplayOrder(master, Arrays.asList(c, a, d));

        assertEquals(3, master.get(0).getId());
        assertEquals(2, master.get(1).getId());   // hidden task did not move
        assertEquals(1, master.get(2).getId());
        assertEquals(4, master.get(3).getId());
    }

    @Test
    public void unknownTaskInDisplayedListChangesNothing() {
        Task a = task(1), b = task(2);
        List<Task> master = new ArrayList<>(Arrays.asList(a, b));

        TaskOrderer.applyDisplayOrder(master, Arrays.asList(b, task(99)));

        assertEquals(1, master.get(0).getId());
        assertEquals(2, master.get(1).getId());
    }
}