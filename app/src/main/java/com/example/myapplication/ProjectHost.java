package com.example.myapplication;

public interface ProjectHost {
    Project getProject();
    void onProjectChanged();                              // refresh header, tab titles and all fragments
    void selectTab(int index);                            // 0 Overview, 1 Tasks, 2 Files, 3 Discussion
    void openTasks(String status, String dateFilter);     // null = leave that filter unchanged
    void showTaskDialog(Task existingOrNull);
    void showEditProjectDialog();
}