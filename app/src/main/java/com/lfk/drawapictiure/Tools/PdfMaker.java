package com.lfk.drawapictiure.Tools;

import android.content.Context;
import android.graphics.Bitmap;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by liufengkai on 15/11/14.
 */
public class PdfMaker {
    public static void makeIt(Context context, String filename, Bitmap bitmap) throws DocumentException, MalformedURLException, IOException {
        File pdf = new File(filename);
        Document document = new Document();
        PdfWriter.getInstance(document,
                new FileOutputStream(pdf));
        document.open();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG /* FileType */,
                100 /* Ratio */, stream);
        Image img = Image.getInstance(stream.toByteArray());
        img.setAbsolutePosition(0, 0);
        document.add(img);

    }
}
