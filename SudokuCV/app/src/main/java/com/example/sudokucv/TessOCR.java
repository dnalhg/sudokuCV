package com.example.sudokucv;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TessOCR {
    private final TessBaseAPI mTess;

    public TessOCR(Context context) {
        mTess = new TessBaseAPI();
        String filePath = context.getFilesDir() + "/tesseract/";
        setupOCR(context, filePath);
        Log.i("OCR", Boolean.toString(new File(filePath).exists()));
        Log.i("OCR", Boolean.toString(new File(filePath + "/tessdata/eng.traineddata").exists()));
        mTess.init(filePath, "eng");

        mTess.setVariable("tessedit_char_whitelist", "123456789");
    }

    public String getOCRResult(Bitmap bitmap) {
        mTess.setImage(bitmap);
        return mTess.getUTF8Text();
    }

    public void onDestroy() {
        if (mTess != null) mTess.end();
    }

    private void setupOCR(Context mContext, String filePath){

        File folder = new File(filePath+"/tessdata/");
        if (!folder.exists()) {
            folder.mkdirs();
            Log.i("OCR", "File made");
        }

        File saving = new File(folder, "eng.traineddata");
        try {
            saving.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream stream = null;
        try {
            stream = mContext.getAssets().open("eng.traineddata", AssetManager.ACCESS_STREAMING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (stream != null){
            copyInputStreamToFile(stream, saving);
        }
    }

    private void copyInputStreamToFile(InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
