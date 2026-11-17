package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class TaskOrderer {

    /**
     * Writes the displayed order back into the project's task list.
     * Visible tasks keep occupying the same slots they had in the master list; only their
     * order changes. Tasks hidden by a filter do not move.
     */
    public static void applyDisplayOrder(List<Task> master, List<Task> displayed) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < master.size(); i++) {
            if (containsId(displayed, master.get(i).getId())) slots.add(i);
        }
        if (slots.size() != displayed.size()) return;   // inconsistent input: change nothing
        for (int k = 0; k < slots.size(); k++) master.set(slots.get(k), displayed.get(k));
    }

    private static boolean containsId(List<Task> list, int id) {
        for (Task t : list) if (t.getId() == id) return true;
        return false;
    }
}