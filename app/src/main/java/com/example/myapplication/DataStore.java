package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore instance;
    private final List<Project> projects = new ArrayList<>();
    private int projectSeq = 0, taskSeq = 0, attachmentSeq = 0;

    private DataStore() { seed(); }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public List<Project> getProjects() { return projects; }
    public int nextProjectId() { return ++projectSeq; }
    public int nextTaskId() { return ++taskSeq; }
    public int nextAttachmentId() { return ++attachmentSeq; }

    private void addTask(Project p, String desc, String status, String priority, Integer dueOffsetDays) {
        int due = dueOffsetDays == null ? 0 : DateUtil.offset(dueOffsetDays);
        p.getTasks().add(new Task(nextTaskId(), desc, status, priority, due));
    }

    public void syncCounters(Project p) {
        projectSeq = Math.max(projectSeq, p.getId());
        for (Task t : p.getTasks()) taskSeq = Math.max(taskSeq, t.getId());
        for (AttachmentLink a : p.getAttachments()) attachmentSeq = Math.max(attachmentSeq, a.getId());
    }

    private void addGeneric(Project p, String prefix, int total, int done) {
        for (int i = 1; i <= total; i++)
            addTask(p, prefix + " " + i, i <= done ? Task.DONE : Task.TODO, Task.NORMAL, null);
    }

    private void link(Project p, int catalogId) {
        CatalogItem c = Catalog.find(catalogId);
        p.getAttachments().add(new AttachmentLink(nextAttachmentId(), c.id, c.resRef));
    }

    private void seed() {
        // 1. Main sample project: 12 tasks (5 To do, 3 In progress, 4 Done), 2 overdue, 3 files, 3 comments
        Project p1 = new Project(nextProjectId(), "User Interface", "Service Page Website",
                "Make a page display about services for the company website in blue and red and provide some price information on each product card.");
        addTask(p1, "Create brand guideline for website design", Task.DONE, Task.NORMAL, -4);
        addTask(p1, "Draft hero copy and CTA text", Task.DONE, Task.LOW, -2);
        addTask(p1, "Set up design tokens", Task.DONE, Task.NORMAL, null);
        addTask(p1, "Collect competitor references", Task.DONE, Task.LOW, -6);
        addTask(p1, "Configure pricing table responsive grid", Task.IN_PROGRESS, Task.NORMAL, 2);
        addTask(p1, "Design product card layout", Task.IN_PROGRESS, Task.HIGH, -1);
        addTask(p1, "Review color palette with team", Task.IN_PROGRESS, Task.LOW, 4);
        addTask(p1, "Build header layout", Task.TODO, Task.HIGH, 0);
        addTask(p1, "Add form validation", Task.TODO, Task.HIGH, 0);
        addTask(p1, "Create low fidelity service page website", Task.TODO, Task.NORMAL, -3);
        addTask(p1, "Write footer links content", Task.TODO, Task.LOW, null);
        addTask(p1, "Prepare handoff notes", Task.TODO, Task.NORMAL, 7);
        link(p1, 1); link(p1, 2); link(p1, 3);
        p1.setCommentCount(3);
        projects.add(p1);

        Project p2 = new Project(nextProjectId(), "Backend", "Login API",
                "Build authentication endpoints and JWT token refresh handlers.");
        addGeneric(p2, "Implement endpoint", 10, 3);
        link(p2, 3); link(p2, 5);
        p2.setCommentCount(2);
        projects.add(p2);

        Project p3 = new Project(nextProjectId(), "Frontend", "Mobile Dashboard",
                "Responsive overview widgets and summary cards.");
        addGeneric(p3, "Build widget", 8, 2);
        link(p3, 4);
        p3.setCommentCount(1);
        projects.add(p3);

        Project p4 = new Project(nextProjectId(), "QA & Ops", "Release Checklist",
                "Pre-production verification and sign-off items.");
        addGeneric(p4, "Verify item", 4, 4);
        link(p4, 6);
        p4.setCommentCount(1);
        projects.add(p4);

        Project p5 = new Project(nextProjectId(), "Design", "Design Assets",
                "Vector icons and illustration library for landing pages.");
        addGeneric(p5, "Export asset", 3, 0);
        p5.setCommentCount(6);
        projects.add(p5);
    }
}