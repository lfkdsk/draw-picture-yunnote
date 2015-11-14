package com.lfk.drawapictiure.Info;

/**
 * Created by liufengkai on 15/9/15.
 */
public class CodeInfo {
    private String codeName;
    private String codeContent;
    private String codeRoot;

    public CodeInfo(String codeName, String codeContent, String codeRoot) {
        this.codeName = codeName;
        this.codeContent = codeContent;
        this.codeRoot = codeRoot;
    }

    public String getCodeName() {
        return codeName;
    }

    public String getCodeContent() {
        return codeContent;
    }

    public String getCodeRoot() {
        return codeRoot;
    }
}
