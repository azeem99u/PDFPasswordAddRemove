package stkmakers.pdfpasswordaddRemove;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class HelperFunctions {


    public static final int ADS_VIEW = 0;
    public static final int ITEMS_VIEW = 1;



    public static String mainFolderName = "/PDF Locked_Unlock App";
    public static String lockedSubFolder = "/Locked PDF";
    public static String unLockedSubFolder = "/UnLocked PDF";
    public static File getFileFor_locked(String fileName) {
        File mainFile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mainFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + mainFolderName+lockedSubFolder);
        } else {
            mainFile = new File(Environment.getExternalStorageDirectory().toString() + mainFolderName+lockedSubFolder );
        }
        if (!mainFile.exists()) {
            mainFile.mkdirs();
        }
        if (mainFile.exists()) {
            mainFile =new File(mainFile.getAbsoluteFile(), fileName);
        }
        return mainFile;
    }

    public static File getFileFor_unlocked(String fileName) {
        File mainFile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mainFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + mainFolderName+unLockedSubFolder );
        } else {
            mainFile = new File(Environment.getExternalStorageDirectory().toString() + mainFolderName+unLockedSubFolder );
        }
        if (!mainFile.exists()) {
            mainFile.mkdirs();
        }
        if (mainFile.exists()) {
            mainFile =new File(mainFile.getAbsoluteFile(), fileName);
        }
        return mainFile;
    }


    public static File getLockedFilePath() {
        File mainFile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mainFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + mainFolderName+lockedSubFolder );
        } else {
            mainFile = new File(Environment.getExternalStorageDirectory().toString() + mainFolderName+lockedSubFolder );
        }

        if (!mainFile.exists()) {
            mainFile.mkdirs();
        }
        return mainFile;
    }


    public static File getUnLockedFilePath() {
        File mainFile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mainFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + mainFolderName+unLockedSubFolder );
        } else {
            mainFile = new File(Environment.getExternalStorageDirectory().toString() + mainFolderName+unLockedSubFolder );
        }

        if (!mainFile.exists()) {
            mainFile.mkdirs();
        }
        return mainFile;
    }

    public static String getFormattedDate(long l){
        Date lastModDate = new Date(l);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(lastModDate);
    }

    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static File renameLockedFile(String oldName, String newName, Activity activity){
        File dir = getLockedFilePath();
        if(dir.exists()){
            File from = new File(dir,oldName);
            File to = new File(dir,newName);

            if (to.exists()){
                Toast.makeText(activity, "File name already exists", Toast.LENGTH_SHORT).show();
            }else {
                if(from.exists()){
                    boolean b = from.renameTo(to);
                    if (b){
                        return to;
                    }else {
                        Toast.makeText(activity, "Unknown error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
     return null;
    }


    public static File renameUnLockedFile(String oldName, String newName, Activity activity){
        File dir = getUnLockedFilePath();
        if(dir.exists()){
            File from = new File(dir,oldName);
            File to = new File(dir,newName);

            if (to.exists()){
                Toast.makeText(activity, "File name already exists", Toast.LENGTH_SHORT).show();
            }else {
                if(from.exists()){
                    boolean b = from.renameTo(to);
                    if (b){
                        return to;
                    }else {
                        Toast.makeText(activity, "Unknown error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        return null;
    }

    public static class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well.
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1).
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }

    }


    public static boolean checkPermission(Context context) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    READ_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }


    public static String convertContentTypeIntoFile(Context context, Uri uri) {

        String path = null;
        Cursor cursor;
        String column = "_id";
        String[] strings = {column};
        try {
            cursor = context.getContentResolver().query(uri, strings, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                path = cursor.getString(index);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return path;
    }


    public static void showSnackbar(Activity context, String message) {
        Snackbar.make(Objects.requireNonNull(context).findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG).show();
    }


}
