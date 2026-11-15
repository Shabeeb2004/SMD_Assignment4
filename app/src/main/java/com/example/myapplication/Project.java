package com.example.myapplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Project implements Serializable {
    private final int id;
    private final long createdAt;
    private String tag, name, description;
    private int commentCount; // dummy value
    private final List<Task> tasks = new ArrayList<>();
    private final List<AttachmentLink> attachments = new ArrayList<>();

    public Project(int id, String tag, String name, String description) {
        this.id = id; this.tag = tag; this.name = name; this.description = description;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public long getCreatedAt() { return createdAt; }
    public String getTag() { return tag; }
    public void setTag(String t) { tag = t; }
    public String getName() { return name; }
    public void setName(String n) { name = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int c) { commentCount = c; }
    public List<Task> getTasks() { return tasks; }
    public List<AttachmentLink> getAttachments() { return attachments; }

    public int countByStatus(String status) {
        int c = 0;
        for (Task t : tasks) if (t.getStatus().equals(status)) c++;
        return c;
    }

    public int getDoneCount() { return countByStatus(Task.DONE); }

    public int getOverdueCount(int today) {
        int c = 0;
        for (Task t : tasks) if (t.isOverdue(today)) c++;
        return c;
    }

    public int getPercent() {
        return tasks.isEmpty() ? 0 : getDoneCount() * 100 / tasks.size();
    }

    public void removeTaskById(int id) {
        for (Iterator<Task> it = tasks.iterator(); it.hasNext(); ) if (it.next().getId() == id) { it.remove(); return; }
    }

    public void removeAttachmentById(int id) {
        for (Iterator<AttachmentLink> it = attachments.iterator(); it.hasNext(); ) if (it.next().getId() == id) { it.remove(); return; }
    }
}