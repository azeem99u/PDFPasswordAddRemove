package stkmakers.pdfpasswordaddRemove.arch;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.ads.nativead.NativeAd;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SharedViewModel extends AndroidViewModel {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AppRepository mAppRepository;
    public MutableLiveData<List<NativeAd>> nativeAdListMutableLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> reloadLockedFileList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> reloadUnLockedFileList = new MutableLiveData<>();

    public SharedViewModel(@NonNull Application application) {
        super(application);
        mAppRepository = AppRepository.getInstance(application.getApplicationContext());
        refreshLockedFileList();
        refreshUnLockedFileList();
    }

    public LiveData<List<Object>> lockedFileListLiveData = Transformations.switchMap(reloadLockedFileList, new Function<Boolean, LiveData<List<Object>>>() {
        @Override
        public LiveData<List<Object>> apply(Boolean input) {
            return Transformations.switchMap(nativeAdListMutableLiveData, new Function<List<NativeAd>, LiveData<List<Object>>>() {
                @Override
                public LiveData<List<Object>> apply(List<NativeAd> nativeAdList) {
                    return mAppRepository.getLockedFileList(nativeAdList);
                }
            });
        }
    });


    public LiveData<List<Object>> unLockedFileListLiveData = Transformations.switchMap(reloadUnLockedFileList, new Function<Boolean, LiveData<List<Object>>>() {
        @Override
        public LiveData<List<Object>> apply(Boolean input) {
            return Transformations.switchMap(nativeAdListMutableLiveData, new Function<List<NativeAd>, LiveData<List<Object>>>() {
                @Override
                public LiveData<List<Object>> apply(List<NativeAd> nativeAdList) {
                    return mAppRepository.getUnLockedFileList(nativeAdList);
                }
            });
        }
    });


    public void refreshLockedFileList() {
        reloadLockedFileList.postValue(true);
    }

    public void refreshUnLockedFileList() {
        reloadUnLockedFileList.postValue(true);
    }


}