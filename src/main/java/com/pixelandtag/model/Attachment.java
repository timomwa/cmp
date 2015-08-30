package com.pixelandtag.model;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
public class Attachment extends ModelBase {
    @ManyToOne
    private MessageEmail message;
    private String fileName;
    private long size;
    private String contentType;

    public MessageEmail getMessage() {
        return message;
    }
    public void setMessage(MessageEmail message) {
        this.message = message;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}