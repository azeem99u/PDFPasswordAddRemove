package stkmakers.pdfpasswordaddRemove.arch;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.nativead.NativeAd;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stkmakers.pdfpasswordaddRemove.HelperFunctions;


public class AppRepository {

    ExecutorService mExecutor = Executors.newFixedThreadPool(6);
    public static AppRepository outInstance;

    public static AppRepository getInstance(Context context) {
        return outInstance = new AppRepository(context);
    }

    private AppRepository(Context context) {

    }


    LiveData<List<Object>> getLockedFileList(List<NativeAd> nativeAdList) {
        MutableLiveData<List<Object>> listLiveData = new MutableLiveData<>();
        mExecutor.execute(() -> {
            ArrayList<Object> fileModels = new ArrayList<>();
            if (nativeAdList.isEmpty()){
                if (HelperFunctions.getLockedFilePath() != null) {
                    File[] files = HelperFunctions.getLockedFilePath().listFiles();
                    if (files != null && files.length != 0) {
                        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                        for (File file : files) {
                            if (file.getName().endsWith(".pdf")) {
                                fileModels.add(new FileModel(file));
                            }
                        }
                    }
                }
            }else {

                if (HelperFunctions.getLockedFilePath() != null) {
                    File[] files = HelperFunctions.getLockedFilePath().listFiles();
                    if (files != null && files.length != 0) {
                        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                        for (File file : files) {
                            if (file.getName().endsWith(".pdf")) {
                                fileModels.add(new FileModel(file));
                            }
                        }
                    }
                }
                int index = 0;
                for (NativeAd ad : nativeAdList) {
                    if (index <= fileModels.size()) {
                        if (!fileModels.contains(ad)) {
                            fileModels.add(index, ad);
                            index = index + 5;
                        }
                    }
                }
            }
            listLiveData.postValue(fileModels);
        });
        return listLiveData;
    }



    LiveData<List<Object>> getUnLockedFileList(List<NativeAd> nativeAdList) {
        MutableLiveData<List<Object>> listLiveData = new MutableLiveData<>();
        mExecutor.execute(() -> {
            ArrayList<Object> fileModels = new ArrayList<>();
            if (nativeAdList.isEmpty()){
                if (HelperFunctions.getUnLockedFilePath() != null) {
                    File[] files = HelperFunctions.getUnLockedFilePath().listFiles();
                    if (files != null && files.length != 0) {
                        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                        for (File file : files) {
                            if (file.getName().endsWith(".pdf")) {
                                fileModels.add(new FileModel(file));
                            }
                        }
                    }
                }
            }else {

                if (HelperFunctions.getUnLockedFilePath() != null) {
                    File[] files = HelperFunctions.getUnLockedFilePath().listFiles();
                    if (files != null && files.length != 0) {
                        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                        for (File file : files) {
                            if (file.getName().endsWith(".pdf")) {
                                fileModels.add(new FileModel(file));
                            }
                        }
                    }
                }
                int index = 0;
                for (NativeAd ad : nativeAdList) {
                    if (index <= fileModels.size()) {
                        if (!fileModels.contains(ad)) {
                            fileModels.add(index, ad);
                            index = index + 5;
                        }
                    }
                }
            }
            listLiveData.postValue(fileModels);
        });
        return listLiveData;
    }
}

