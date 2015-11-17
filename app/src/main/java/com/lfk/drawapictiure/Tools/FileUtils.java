package com.lfk.drawapictiure.Tools;

import java.io.File;

/**
 * Created by liufengkai on 15/11/17.
 */
public class FileUtils {
    /**
     * 获取文件类型
     *
     * @param file
     * @return
     */
    public static String getType(File file) {
        if (file.isDirectory())
            return null;

        int dotIndex = file.getName().lastIndexOf(".");
        if (dotIndex < 0)
            return "";

        return file.getName().substring(dotIndex).toLowerCase();
    }

}
