package stkmakers.pdfpasswordaddRemove.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;

import stkmakers.pdfpasswordaddRemove.HelperFunctions;
import stkmakers.pdfpasswordaddRemove.StatusViewPagerAdapter;
import stkmakers.pdfpasswordaddRemove.arch.SharedViewModel;
import stkmakers.pdfpasswordaddRemove.databinding.FragmentFileListBinding;

public class FileListFragment extends Fragment {
    FragmentFileListBinding binding;
    SharedViewModel sharedViewModel;
    public FileListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFileListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayoutAndViewPagerSetting();
    }

    @SuppressLint("ResourceType")
    private void tabLayoutAndViewPagerSetting() {
        StatusViewPagerAdapter mStatusViewPagerAdapter = new StatusViewPagerAdapter(requireActivity());
        binding.mainPageViewPager.setAdapter(mStatusViewPagerAdapter);
        binding.mainPageViewPager.setPageTransformer(new HelperFunctions.ZoomOutPageTransformer());
        new TabLayoutMediator(binding.mainPageTabLayout, binding.mainPageViewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: {
                            tab.setText("Locked Files");
                            break;
                        }
                        case 1: {
                            tab.setText("UnLocked Files");
                            break;
                        }
                    }
                }).attach();
    }


    @Override
    public void onResume() {
        super.onResume();
        sharedViewModel.refreshLockedFileList();
        sharedViewModel.refreshUnLockedFileList();
    }
}