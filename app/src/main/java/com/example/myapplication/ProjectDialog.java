package com.example.myapplication;

import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;

public class ProjectDialog {
    public interface OnSave { void onSave(String name, String tag, String desc, CatalogItem attachment); }

    public static void show(Context ctx, boolean edit, String name, String tag, String desc, OnSave cb) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_project, null);
        EditText etName = v.findViewById(R.id.etProjectName);
        EditText etTag = v.findViewById(R.id.etProjectTag);
        EditText etDesc = v.findViewById(R.id.etProjectDescription);
        View layoutAtt = v.findViewById(R.id.layoutAttachment);
        TextView tvAtt = v.findViewById(R.id.tvAttachmentName);
        Button btnPick = v.findViewById(R.id.btnPickAttachment);

        etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
        etTag.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        etDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});

        final CatalogItem[] picked = {null};
        if (edit) {
            etName.setText(name); etTag.setText(tag); etDesc.setText(desc);
            layoutAtt.setVisibility(View.GONE);
        } else {
            btnPick.setOnClickListener(x -> CatalogDialog.show(ctx, new ArrayList<>(), item -> {
                picked[0] = item;
                tvAtt.setText(item.name);
            }));
        }

        AlertDialog d = new AlertDialog.Builder(ctx)
                .setTitle(edit ? "Edit Project" : "New Project")
                .setView(v)
                .setPositiveButton(edit ? "Save" : "Add Project", null)
                .setNegativeButton("Cancel", null)
                .create();
        d.show();

        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x -> {
            String n = etName.getText().toString().trim();
            String t = etTag.getText().toString().trim();
            String ds = etDesc.getText().toString().trim();
            boolean ok = true;
            if (n.length() < 1 || n.length() > 60) { etName.setError("Name must be 1-60 characters"); ok = false; }
            if (t.length() < 1 || t.length() > 20) { etTag.setError("Tag must be 1-20 characters"); ok = false; }
            if (ds.length() < 1 || ds.length() > 500) { etDesc.setError("Description must be 1-500 characters"); ok = false; }
            if (!ok) return;
            d.dismiss();
            cb.onSave(n, t, ds, picked[0]);
        });
    }
}