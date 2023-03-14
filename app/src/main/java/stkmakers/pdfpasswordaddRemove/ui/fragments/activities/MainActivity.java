package stkmakers.pdfpasswordaddRemove.ui.fragments.activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stkmakers.pdfpasswordaddRemove.R;
import stkmakers.pdfpasswordaddRemove.arch.SharedViewModel;
import stkmakers.pdfpasswordaddRemove.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    List<NativeAd> nativeAdList = new ArrayList<>();
    AdLoader mAdLoader;
    ConnectivityManager.NetworkCallback mNetworkCallback;
    boolean isNetworkNotAvailable;
    NetworkRequest mNetworkRequest;
    ConnectivityManager connMgr;
    private static final int NUMBER_OF_ADS = 4;
    AppBarConfiguration appBarConfiguration_for_drawer;
    NavController navController;
    private ActivityMainBinding binding;
    SharedViewModel sharedViewModel;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    public StartThread startThread = new StartThread();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        setUpNetworkChecking();
        toolBarSetUp();
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        adListeners();
        executorService.execute(startThread);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView_for_drawer = binding.navViewForDrawer;
        appBarConfiguration_for_drawer = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.fileListFragment)
                .setOpenableLayout(drawer)
                .build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navigationView_for_drawer, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration_for_drawer);
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
    }


    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration_for_drawer) || super.onSupportNavigateUp();
    }


    @SuppressLint("ResourceType")
    private void toolBarSetUp() {
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(true);

    }

    public class StartThread implements Runnable {

        private volatile boolean running = true;
        private volatile boolean paused = false;
        private final Object pauseLock = new Object();
        int count = 0;
        int count2 = 0;

        @Override
        public void run() {

            try {

                while (running) {
                    synchronized (pauseLock) {
                        if (!running) {
                            break;
                        }
                        if (paused) {
                            try {
                                pauseLock.wait();
                            } catch (InterruptedException ex) {
                                break;
                            }
                            if (!running) {
                                break;
                            }
                        }
                    }


                    SystemClock.sleep(1000);

                    count2++;

                    if (count >= 90 || nativeAdList.isEmpty() && !isNetworkNotAvailable) {
                        count = 0;
                        new Handler(Looper.getMainLooper()).post(MainActivity.this::loadNativeAds);
                    } else if (count % 35 == 0) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!nativeAdList.isEmpty() && !isNetworkNotAvailable) {
                                Collections.shuffle(nativeAdList);
                                sharedViewModel.nativeAdListMutableLiveData.setValue(nativeAdList);
                            } else if (!nativeAdList.isEmpty() && isNetworkNotAvailable) {
                                sharedViewModel.nativeAdListMutableLiveData.setValue(nativeAdList);
                            }
                        });
                    }
                    count++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void stop() {
            running = false;
            resume();
        }

        public void pause() {
            // you may want to throw an IllegalStateException if !running
            paused = true;
        }

        public void resume() {
            synchronized (pauseLock) {
                paused = false;
                try {
                    pauseLock.notifyAll(); // Unblocks thread
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadNativeAds() {
        if (mAdLoader != null && !mAdLoader.isLoading() && !isNetworkNotAvailable && sharedViewModel != null) {
            nativeAdList.clear();
            mAdLoader.loadAds(new AdRequest.Builder().build(), NUMBER_OF_ADS);
        }
        if (isNetworkNotAvailable) {
            if (!nativeAdList.isEmpty()) {
                sharedViewModel.nativeAdListMutableLiveData.setValue(nativeAdList);
            }
        }
    }

    private void adListeners() {

        sharedViewModel.nativeAdListMutableLiveData.setValue(nativeAdList);
        NativeAd.OnNativeAdLoadedListener onNativeAdLoadedListener = nativeAd -> {
            if (isDestroyed()) {
                nativeAd.destroy();
                return;
            }
            nativeAdList.add(nativeAd);
            if (!mAdLoader.isLoading()) {
                sharedViewModel.nativeAdListMutableLiveData.setValue(nativeAdList);
            }
        };

        AdListener adListener = new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if (isDestroyed()) {
                    return;
                }
                if (!mAdLoader.isLoading()) {
                    sharedViewModel.nativeAdListMutableLiveData.setValue(nativeAdList);
                }
            }
        };
        mAdLoader = new AdLoader.Builder(this, getString(R.string.admob_native_id)).forNativeAd(onNativeAdLoadedListener).withAdListener(adListener).build();
    }

    private void setUpNetworkChecking() {
        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                isNetworkNotAvailable = false;
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                isNetworkNotAvailable = true;
            }

        };
        mNetworkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();


    }


    @Override
    protected void onStart() {
        super.onStart();

        if (connMgr != null && mNetworkCallback != null && mNetworkRequest != null) {
            connMgr.requestNetwork(mNetworkRequest, mNetworkCallback);
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mNetworkCallback != null) {
            connMgr.unregisterNetworkCallback(mNetworkCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startThread.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        startThread.pause();
    }


    private void showPermissionDialog() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2000);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2000);
            }

        } else
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 333);
    }

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }


}