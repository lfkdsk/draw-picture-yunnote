
package com.lfk.drawapictiure.View.MarkDown.parser;


import com.lfk.drawapictiure.View.MarkDown.Markdown;

public class UnOrderListParser extends Markdown.MDParser {

    private static final char KEY = '-';

    @Override
    public Markdown.MDWord parseLineFmt(String content) {
        if (content.charAt(0) != KEY) {
            return Markdown.MDWord.NULL;
        }
        return new Markdown.MDWord("", 1, Markdown.MD_FMT_UNORDER_LIST);
    }

    @Override
    public Markdown.MDWord parseWordFmt(String content) {
        return Markdown.MDWord.NULL;
    }

}
