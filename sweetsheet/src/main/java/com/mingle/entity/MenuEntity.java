package com.mingle.entity;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;


/**
 * @author zzz40500
 * @version 1.0
 * @date 2015/8/5.
 * @github: https://github.com/zzz40500
 */
public class MenuEntity {

    public @DrawableRes int iconId;
    public CharSequence title;
    public Drawable icon;
    public String id;

    public MenuEntity() {
    }

    public MenuEntity(CharSequence title, int iconId, String id) {
        this.title = title;
        this.iconId = iconId;
        this.id = id;
    }
}
