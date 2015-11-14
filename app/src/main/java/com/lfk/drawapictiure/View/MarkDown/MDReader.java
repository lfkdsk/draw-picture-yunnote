
package com.lfk.drawapictiure.View.MarkDown;


import android.text.SpannableStringBuilder;
import android.util.Log;

import com.lfk.drawapictiure.View.MarkDown.parser.BoldParser;
import com.lfk.drawapictiure.View.MarkDown.parser.CenterParser;
import com.lfk.drawapictiure.View.MarkDown.parser.HeaderParser;
import com.lfk.drawapictiure.View.MarkDown.parser.ItalicParser;
import com.lfk.drawapictiure.View.MarkDown.parser.OrderListParser;
import com.lfk.drawapictiure.View.MarkDown.parser.QuoteParser;
import com.lfk.drawapictiure.View.MarkDown.parser.UnOrderListParser;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class MDReader {

    private final String mContent;
    private List<Markdown.MDLine> mMDLines = new ArrayList<>();
    private static List<Markdown.MDParser> mMDParsers = new ArrayList<>();

    static {
        mMDParsers.add(new HeaderParser());
        mMDParsers.add(new QuoteParser());
        mMDParsers.add(new OrderListParser());
        mMDParsers.add(new UnOrderListParser());
        mMDParsers.add(new BoldParser());
        mMDParsers.add(new CenterParser());
        mMDParsers.add(new ItalicParser());
    }

    public MDReader(String content) {
        mContent = content;
        if (mContent == null || "".equals(content)) {
            return;
        }
        String[] lines = content.split("\n");
        for (String line : lines) {
            mMDLines.add(parseLine(line));
        }
    }

    public String getTitle() {
        if (mContent == null || "".equals(mContent)) {
            return "";
        }
        int end = mContent.indexOf("\n");
        return mContent.substring(0, end == -1 ? mContent.length() : end);
    }

    public String getContent() {
        return mContent;
    }

    public String getRawContent() {
        StringBuilder builder = new StringBuilder();
        for (Markdown.MDLine line : mMDLines) {
            builder.append(line.getRawContent());
            builder.append("\n");
        }
        return builder.toString();
    }

    public SpannableStringBuilder getFormattedContent() {
        return new MDFormatter(mMDLines).getFormattedContent();
    }

    private Markdown.MDLine parseLine(String lineContent) {

        Markdown.MDLine mdline = new Markdown.MDLine(lineContent);
        if ("".equals(lineContent)) {
            return mdline;
        }

        String pContent = lineContent;

        //Parse the start format        
        for (Markdown.MDParser parser : mMDParsers) {
            Markdown.MDWord word = parser.parseLineFmt(pContent);
            if (word.mFormat != Markdown.MD_FMT_TEXT) {
                mdline.mFormat = word.mFormat;
                pContent = lineContent.substring(word.mLength);
                break;
            }
        }

        Logger.d("content" + pContent);

        // Parse the word format
        StringBuilder mNoFmtContent = new StringBuilder();
        while (pContent.length() != 0) {
            boolean isFmtFound = false;
            // Check format start with pContent
            for (Markdown.MDParser parser : mMDParsers) {
                Markdown.MDWord word = parser.parseWordFmt(pContent);
                if (word.mLength > 0) {
                    isFmtFound = true;
                    // Add no format string first
                    int noFmtContentLen = mNoFmtContent.length();
                    if (noFmtContentLen != 0) {
                        mdline.mMDWords.add(new Markdown.MDWord(mNoFmtContent.toString(), noFmtContentLen, Markdown.MD_FMT_TEXT));
                        mNoFmtContent = new StringBuilder();
                    }
                    mdline.mMDWords.add(word);
                    pContent = pContent.substring(word.mLength);
                    break;
                }
            }

            Logger.d("content2" + pContent);

            // If no format found, move to next position
            if (!isFmtFound) {
                mNoFmtContent.append(pContent.charAt(0));
                pContent = pContent.substring(1);
                if (pContent.length() == 0) {
                    mdline.mMDWords.add(new Markdown.MDWord(mNoFmtContent.toString(), mNoFmtContent.length(), Markdown.MD_FMT_TEXT));
                    break;
                }
            }

            Logger.d("content3" + pContent);

        }
        return mdline;
    }

    protected void display() {
        StringBuilder builder = new StringBuilder();
        builder.append("Markdown Parse: \n" + mContent + "\n\n");
        for (Markdown.MDLine line : mMDLines) {
            builder.append("Line format: " + line.mFormat + "\n");
            for (Markdown.MDWord word : line.mMDWords) {
                builder.append("Word: " + word.mRawContent + ", " + word.mFormat + "\n");
            }
        }
        Log.d("JNote", builder.toString());
    }
}
