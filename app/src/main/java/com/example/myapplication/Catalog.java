package com.example.myapplication;

import android.content.Context;
import java.util.Arrays;
import java.util.List;

public class Catalog {
    public static final List<CatalogItem> ITEMS = Arrays.asList(
            new CatalogItem(1, "home_mockup.png", CatalogItem.IMAGE, "cat_photo_1", null, "2.4 MB"),
            new CatalogItem(2, "team_photo.jpg", CatalogItem.IMAGE, "cat_photo_2", null, "1.1 MB"),
            new CatalogItem(3, "requirements.txt", CatalogItem.TEXT, "text_requirements",
                    "TaskFlow Requirements\n\n1. Users can create projects with a tag and description.\n"
                            + "2. Each project has tasks with status, priority and due date.\n"
                            + "3. Tasks can be searched, filtered and sorted.\n"
                            + "4. Attachments are linked from a local catalog.", "450 KB"),
            new CatalogItem(4, "wireframe.png", CatalogItem.IMAGE, "cat_photo_3", null, "1.8 MB"),
            new CatalogItem(5, "meeting_notes.txt", CatalogItem.TEXT, "text_meeting_notes",
                    "Sprint meeting notes\n\n- Finish the tab navigation\n- Review the task filters\n- Prepare the demo", "12 KB"),
            new CatalogItem(6, "release_checklist.txt", CatalogItem.TEXT, "text_release_checklist",
                    "Release checklist\n\n[ ] Test on a small screen\n[ ] Test rotation\n[ ] Update version number", "8 KB")
    );

    public static CatalogItem find(int id) {
        for (CatalogItem c : ITEMS) if (c.id == id) return c;
        return null;
    }

    public static int drawableId(Context ctx, String name) {
        int res = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
        return res != 0 ? res : android.R.drawable.ic_menu_gallery;
    }
}