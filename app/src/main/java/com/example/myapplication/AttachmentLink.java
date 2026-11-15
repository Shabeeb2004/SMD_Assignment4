package com.example.myapplication;

import java.io.Serializable;

public class AttachmentLink implements Serializable {
    private final int id, catalogId;
    private final String resourceRef;
    private final long linkedAt;

    public AttachmentLink(int id, int catalogId, String resourceRef) {
        this.id = id;
        this.catalogId = catalogId;
        this.resourceRef = resourceRef;
        this.linkedAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public int getCatalogId() { return catalogId; }
    public String getResourceRef() { return resourceRef; }
    public long getLinkedAt() { return linkedAt; }
}