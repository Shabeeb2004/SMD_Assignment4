package com.example.myapplication;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;

public class CatalogDialog {
    public interface OnPicked { void onPicked(CatalogItem item); }

    public static void show(Context ctx, List<Integer> excludeIds, OnPicked cb) {
        final List<CatalogItem> avail = new ArrayList<>();
        for (CatalogItem c : Catalog.ITEMS) if (!excludeIds.contains(c.id)) avail.add(c);

        if (avail.isEmpty()) {
            new AlertDialog.Builder(ctx).setTitle("Attachment catalog")
                    .setMessage("All catalog items are already linked.")
                    .setPositiveButton("OK", null).show();
            return;
        }

        String[] labels = new String[avail.size()];
        for (int i = 0; i < labels.length; i++) labels[i] = avail.get(i).name + "  (" + avail.get(i).type + ")";

        final int[] sel = {-1};
        AlertDialog d = new AlertDialog.Builder(ctx)
                .setTitle("Select attachment")
                .setSingleChoiceItems(labels, -1, (dlg, which) -> {
                    sel[0] = which;
                    ((AlertDialog) dlg).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                })
                .setPositiveButton("Add", (dlg, w) -> { if (sel[0] >= 0) cb.onPicked(avail.get(sel[0])); })
                .setNegativeButton("Cancel", null)
                .create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));
        d.show();
    }
}