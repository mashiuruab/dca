package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model;

/**
 * Created by mashiur on 9/27/16.
 */
public class DCABinaryImage {
    private String base64EncodeString;
    private String mimeType;
    private String fileName;

    public String getBase64EncodeString() {
        return base64EncodeString;
    }

    public void setBase64EncodeString(String base64EncodeString) {
        this.base64EncodeString = base64EncodeString;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
