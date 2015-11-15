package com.lfk.drawapictiure.Tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.WindowManager;

import com.lfk.drawapictiure.Info.UserInfo;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by liufengkai on 15/11/14.
 */
public class PdfMaker {
    public static void makeIt(Context context, String filename, Bitmap bitmap) throws DocumentException, MalformedURLException, IOException {
        File pdf = new File(filename);
        int screenHeight = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getHeight();
        Document document = new Document(new Rectangle(bitmap.getWidth(),
                screenHeight));
        PdfWriter.getInstance(document,
                new FileOutputStream(pdf));
        document.open();
        document.addAuthor(UserInfo.UserName);
        List<PicUtils.ImagePiece> list = PicUtils.spilt(context, bitmap);

        for (int i = 0; i < list.size(); i++) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            list.get(i).bitmap.compress(Bitmap.CompressFormat.PNG /* FileType */,
                    100 /* Ratio */, stream);
            Image img = Image.getInstance(stream.toByteArray());
            if (i > 0)
                document.newPage();
            document.add(img);
        }
        document.close();
    }

    public static void makeIt(String filename, String rawContent) throws FileNotFoundException, DocumentException {
        File pdf = new File(filename);
        Document document = new Document();
        PdfWriter.getInstance(document,
                new FileOutputStream(pdf));
        document.open();
        document.add(new Paragraph(rawContent));
        document.close();
    }
}
