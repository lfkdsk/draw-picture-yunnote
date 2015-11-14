package com.lfk.drawapictiure.Info;

/**
 * Created by liufengkai on 15/9/13.
 */
public class MenuInfo {
    private String paint_name;
    private String paint_time;
    private String paint_content;
    private String paint_root;
    private String paint_img_root;

    public MenuInfo(String paint_name, String paint_time,
                    String paint_content, String paint_root,
                    String paint_img_root) {
        this.paint_name = paint_name;
        this.paint_time = paint_time;
        this.paint_content = paint_content;
        this.paint_root = paint_root;
        this.paint_img_root = paint_img_root;
    }

    public String getPaint_content() {
        return paint_content;
    }

    public String getPaint_name() {
        return paint_name;
    }

    public String getPaint_time() {
        return paint_time;
    }

    public String getPaint_root() {
        return paint_root;
    }

    public String getPaint_img_root() {
        return paint_img_root;
    }
}
