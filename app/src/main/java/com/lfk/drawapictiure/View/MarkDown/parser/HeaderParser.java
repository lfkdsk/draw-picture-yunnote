/*
 *  Copyright (C) 2015, Jhuster, All Rights Reserved
 *
 *  Author:  Jhuster(lujun.hust@gmail.com)
 *  
 *  https://github.com/Jhuster/JNote
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 */
package com.lfk.drawapictiure.View.MarkDown.parser;

import com.lfk.drawapictiure.View.MarkDown.Markdown;

public class HeaderParser extends Markdown.MDParser {

    public static final String HEADER = "# ";
    public static final String HEADER2 = "## ";
    public static final String HEADER3 = "### ";
    public static final String HEADER4 = "#### ";
    public static final String HEADER5 = "##### ";
    public static final String HEADER6 = "###### ";


    @Override
    public Markdown.MDWord parseLineFmt(String content) {
        if (content.startsWith(HEADER)) {
            return new Markdown.MDWord("", HEADER.length(), Markdown.MD_FMT_HEADER1);
        } else if (content.startsWith(HEADER2)) {
            return new Markdown.MDWord("", HEADER2.length(), Markdown.MD_FMT_HEADER2);
        } else if (content.startsWith(HEADER3)) {
            return new Markdown.MDWord("", HEADER3.length(), Markdown.MD_FMT_HEADER3);
        } else if (content.startsWith(HEADER4)) {
            return new Markdown.MDWord("", HEADER4.length(), Markdown.MD_FMT_HEADER4);
        } else if (content.startsWith(HEADER5)) {
            return new Markdown.MDWord("", HEADER5.length(), Markdown.MD_FMT_HEADER5);
        } else if (content.startsWith(HEADER6)) {
            return new Markdown.MDWord("", HEADER6.length(), Markdown.MD_FMT_HEADER6);
        }
        return Markdown.MDWord.NULL;
    }

    @Override
    public Markdown.MDWord parseWordFmt(String content) {
        return Markdown.MDWord.NULL;
    }
}
