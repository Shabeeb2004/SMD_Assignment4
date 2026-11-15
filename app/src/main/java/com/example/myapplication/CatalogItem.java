package com.example.myapplication;

public class CatalogItem {
    public static final String IMAGE = "Image", TEXT = "Text";
    public final int id;
    public final String name, type, resRef, content, sizeLabel;

    public CatalogItem(int id, String name, String type, String resRef, String content, String sizeLabel) {
        this.id = id; this.name = name; this.type = type; this.resRef = resRef;
        this.content = content; this.sizeLabel = sizeLabel;
    }

    public boolean isImage() { return IMAGE.equals(type); }
}