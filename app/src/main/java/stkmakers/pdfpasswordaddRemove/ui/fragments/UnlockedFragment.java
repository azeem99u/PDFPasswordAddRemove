package stkmakers.pdfpasswordaddRemove.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.ads.nativead.NativeAd;

import stkmakers.pdfpasswordaddRemove.arch.SharedViewModel;
import stkmakers.pdfpasswordaddRemove.databinding.FragmentUnlockedBinding;


public class UnlockedFragment extends Fragment {
    FragmentUnlockedBinding binding;
    UnLockedFragmentAdapter unLockedFragmentAdapter;
    SharedViewModel sharedViewModel;
    public UnlockedFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUnlockedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unLockedFragmentAdapter = new UnLockedFragmentAdapter(requireContext(), requireActivity());
        binding.recycerViewFiles.hasFixedSize();
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(requireContext());
        binding.recycerViewFiles.setLayoutManager(gridLayoutManager);
        binding.recycerViewFiles.setAdapter(unLockedFragmentAdapter);
        sharedViewModel.unLockedFileListLiveData.observe(getViewLifecycleOwner(), files -> {
            if (!files.isEmpty()){
                if (files.size() == 1 && files.get(0) instanceof NativeAd){
                    unLockedFragmentAdapter.setFiles(files);
                    binding.customEmptyView.getRoot().setVisibility(View.VISIBLE);
                }else {
                    unLockedFragmentAdapter.setFiles(files);
                    binding.customEmptyView.getRoot().setVisibility(View.GONE);
                }
            }else {
                unLockedFragmentAdapter.setFiles(files);
                binding.customEmptyView.getRoot().setVisibility(View.VISIBLE);
            }
        });


    }
    @Override
    public void onResume() {
        super.onResume();
        sharedViewModel.refreshUnLockedFileList();
    }


}