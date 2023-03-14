package stkmakers.pdfpasswordaddRemove;

import static stkmakers.pdfpasswordaddRemove.HelperFunctions.getFileFor_locked;
import static stkmakers.pdfpasswordaddRemove.HelperFunctions.getFileFor_unlocked;

import android.app.Activity;
import android.content.Context;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EncryptionFunctions {


    public static File decrFile(String filePath, String fileName) throws IOException {
        File desc = getFileFor_unlocked(fileName);
        try (FileInputStream oldFile = new FileInputStream(filePath)) {
            try (FileOutputStream newFile = new FileOutputStream(desc)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = oldFile.read(buf)) > 0) {
                    newFile.write(buf, 0, len);
                }
                newFile.flush();
                oldFile.close();
                return desc;
            }
        }
    }

    public static void addPasswordItextpdf(Context context,String path,String password) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                File desc = encFile(path,new File(path).getName());
                PdfReader reader = new PdfReader(path);
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(desc));
                stamper.setEncryption(password.getBytes(), password.getBytes(), PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY, PdfWriter.ENCRYPTION_AES_128);
                stamper.flush();
                stamper.close();
                reader.close();
                HelperFunctions.showSnackbar((Activity) context,"Password Added.");
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        });
    }


    public static void removePasswordItextpdf(Context context, String path, String password) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                File file = decrFile(path,new File(path).getName());
                PdfReader reader = new PdfReader(path, password.getBytes());
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(file));
                stamper.flush();
                stamper.close();
                reader.close();
                HelperFunctions.showSnackbar((Activity) context,"Password Removed.");

            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        });
    }


    public static File encFile(String path, String fileName) throws IOException {
        File file = getFileFor_locked(fileName);
        try (FileInputStream oldFile = new FileInputStream(path)) {
            try (FileOutputStream newFile = new FileOutputStream(file)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = oldFile.read(buf)) > 0) {
                    newFile.write(buf, 0, len);
                }
                newFile.flush();
                oldFile.close();
                return file;
            }
        }
    }



}
