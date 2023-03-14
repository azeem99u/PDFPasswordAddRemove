package stkmakers.pdfpasswordaddRemove;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import java.util.Objects;

import stkmakers.pdfpasswordaddRemove.databinding.FragmentCreateAttendanceSheetBinding;


public class CreateAttBottomSheet extends BottomSheetDialogFragment {

    public CreateAttBottomSheet() {}

    public static final String BOTTOM_SHEET_PICKER_TAG = "CreateAttendanceSheet";
    private FragmentCreateAttendanceSheetBinding binding;
    TextWatcher mTextWatcher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateAttendanceSheetBinding.inflate(inflater);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        Objects.requireNonNull(dialog).getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        return binding.getRoot();
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.cancelBottomSheetBtn.setOnClickListener(view13 -> {
            dismiss();
        });
        binding.closeBottomSheetBtn.setOnClickListener(view15 -> {
            dismiss();
        });

        binding.continueBtn.setOnClickListener(view14 -> {

        });
    }










    @NonNull
    private TextWatcher getWatcher(MaterialCardView cardView, String prefKey) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString().trim();
                if (s.isEmpty()){
                    cardView.setStrokeColor(ContextCompat.getColor(requireContext(),R.color.red));
                }else {
                    cardView.setStrokeColor(ContextCompat.getColor(requireContext(),R.color.card_color));
                }
            }
        };
    }
}