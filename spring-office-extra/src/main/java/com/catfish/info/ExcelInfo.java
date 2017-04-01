package com.catfish.info;

import java.io.Serializable;

/**
 * Created by A on 2017/3/31.
 */
public class ExcelInfo implements Serializable {

    private String head;
    private String replaceParams;
    private String fileName;

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getReplaceParams() {
        return replaceParams;
    }

    public void setReplaceParams(String replaceParams) {
        this.replaceParams = replaceParams;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
