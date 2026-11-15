package com.example.myapplication;

import java.io.Serializable;

public class Task implements Serializable {
    public static final String TODO = "To do", IN_PROGRESS = "In progress", DONE = "Done";
    public static final String HIGH = "High", NORMAL = "Normal", LOW = "Low";
    public static final String[] STATUSES = {TODO, IN_PROGRESS, DONE};
    public static final String[] PRIORITIES = {HIGH, NORMAL, LOW};

    private final int id;
    private String description, status, priority;
    private int dueDate; // yyyyMMdd, 0 = no due date
    private final long createdAt;

    public Task(int id, String description, String status, String priority, int dueDate) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }
    public String getStatus() { return status; }
    public void setStatus(String s) { status = s; }
    public String getPriority() { return priority; }
    public void setPriority(String p) { priority = p; }
    public int getDueDate() { return dueDate; }
    public void setDueDate(int d) { dueDate = d; }
    public long getCreatedAt() { return createdAt; }

    public boolean isDone() { return DONE.equals(status); }
    public void setDone(boolean done) { status = done ? DONE : TODO; }
    public boolean isOverdue(int today) { return dueDate != 0 && dueDate < today && !isDone(); }

    public static int indexOf(String[] arr, String v) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(v)) return i;
        return 0;
    }
}