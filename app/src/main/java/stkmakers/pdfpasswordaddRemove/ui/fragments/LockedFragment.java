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
import stkmakers.pdfpasswordaddRemove.databinding.FragmentLockedBinding;

public class LockedFragment extends Fragment {

    FragmentLockedBinding binding;
    LockedFragmentAdapter lockedFragmentAdapter;

    SharedViewModel sharedViewModel;
    public LockedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLockedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lockedFragmentAdapter = new LockedFragmentAdapter(requireContext(), requireActivity());
        binding.recycerViewFiles.hasFixedSize();
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(requireContext());
        binding.recycerViewFiles.setLayoutManager(gridLayoutManager);
        binding.recycerViewFiles.setAdapter(lockedFragmentAdapter);
        sharedViewModel.lockedFileListLiveData.observe(getViewLifecycleOwner(), files -> {
            if (!files.isEmpty()){
                if (files.size() == 1 && files.get(0) instanceof NativeAd){
                    lockedFragmentAdapter.setFiles(files);
                    binding.customEmptyView.getRoot().setVisibility(View.VISIBLE);
                }else {
                    lockedFragmentAdapter.setFiles(files);
                    binding.customEmptyView.getRoot().setVisibility(View.GONE);
                }
            }else {
                lockedFragmentAdapter.setFiles(files);
                binding.customEmptyView.getRoot().setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedViewModel.refreshLockedFileList();
    }

}