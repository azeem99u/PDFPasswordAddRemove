package stkmakers.pdfpasswordaddRemove.ui.fragments;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.itextpdf.text.pdf.PdfReader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import stkmakers.pdfpasswordaddRemove.EncryptionFunctions;
import stkmakers.pdfpasswordaddRemove.FileAdapter;
import stkmakers.pdfpasswordaddRemove.HelperFunctions;
import stkmakers.pdfpasswordaddRemove.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    FileAdapter fileAdapter;
    ActivityResultLauncher<Intent> resultLauncher;
    Executor mExecutor = Executors.newSingleThreadExecutor();
    String path;
    Uri uri;
    public HomeFragment() {}
    @SuppressLint("UseRequireInsteadOfGet")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater);
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            if (data != null) {
                binding.chooseFileBtn.setVisibility(View.GONE);
                binding.materialCardViewFile.setVisibility(View.VISIBLE);
                String s = HelperFunctions.convertContentTypeIntoFile(requireContext(), data.getData());
                path = HomeFragment.copyFileAndGetPath(requireContext(), data.getData(), s);
                File file = new File(path);
                binding.fileName.setText(file.getName());
                binding.fileDate.setText(HelperFunctions.getFormattedDate(file.lastModified()));
                binding.fileSize.setText(HelperFunctions.humanReadableByteCountSI(file.length()));


                boolean isValidPdf = false;
                try {
                    InputStream tempStream = new FileInputStream(file);
                    PdfReader reader = new PdfReader(tempStream);
                    isValidPdf = reader.isOpenedWithFullPermissions();
                } catch (Exception e) {
                    isValidPdf = false;
                }

                if (isValidPdf){

                    Toast.makeText(requireActivity(), "no pass", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(requireActivity(), "their is pass", Toast.LENGTH_SHORT).show();
                }

            }else {
                binding.chooseFileBtn.setVisibility(View.VISIBLE);
                binding.materialCardViewFile.setVisibility(View.GONE);
            }
        });
        return binding.getRoot();
    }


    private static String copyFileAndGetPath(Context context, Uri realUri, String id) {
        final String selection = "_id=?";
        final String[] selectionArgs = new String[]{id};
        String path = null;
        Cursor cursor = null;
        try {
            final String[] projection = {"_display_name"};
            cursor = context.getContentResolver().query(realUri, projection, selection, selectionArgs,
                    null);
            cursor.moveToFirst();
            final String fileName = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"));
            File file = new File(context.getCacheDir(), fileName);
            saveAnswerFileFromUri(realUri, file, context);
            path = file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return path;
    }


    static void saveAnswerFileFromUri(Uri uri, File destFile, Context context) {
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fileDescriptor = pfd.getFileDescriptor();
                InputStream fileInputStream = new FileInputStream(fileDescriptor);
                OutputStream fileOutputStream = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int length;
                while ((length = fileInputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, length);
                }
                fileOutputStream.flush();
                fileInputStream.close();
                fileOutputStream.close();
                pfd.close();
            }
        }catch (Exception e){
            e.getStackTrace();
        }

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.chooseFileBtn.setOnClickListener(v -> selectPDF());
        binding.lockPdfBtn.setOnClickListener(v -> {
            EditText editText = binding.etPasswordLock.getEditText();
            if (editText != null && !editText.getText().toString().isEmpty() && !path.isEmpty()){
                String password = editText.getText().toString();
                EncryptionFunctions.addPasswordItextpdf(requireActivity(),path,password);
                editText.setText("");
                path = null;
                binding.materialCardViewFile.setVisibility(View.GONE);
                binding.chooseFileBtn.setVisibility(View.VISIBLE);

            }
        });




        binding.unlockPdfBtn.setOnClickListener(v -> {
            EditText editText = binding.etPasswordUnlock.getEditText();
            if (editText != null && !editText.getText().toString().isEmpty() && !path.isEmpty()){
                String password = editText.getText().toString();
                EncryptionFunctions.removePasswordItextpdf(requireActivity(),path,password);
                Objects.requireNonNull(binding.etPasswordUnlock.getEditText()).setText("");
                Objects.requireNonNull(binding.etPasswordLock.getEditText()).setText("");
                path = null;
                binding.materialCardViewFile.setVisibility(View.GONE);
                binding.chooseFileBtn.setVisibility(View.VISIBLE);
            }
        });

        binding.removeIconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.materialCardViewFile.setVisibility(View.GONE);
                binding.chooseFileBtn.setVisibility(View.VISIBLE);
                path = null;
                Objects.requireNonNull(binding.etPasswordUnlock.getEditText()).setText("");
                Objects.requireNonNull(binding.etPasswordLock.getEditText()).setText("");

            }
        });
    }


    private void selectPDF() {
        String folderPath = Environment.getExternalStorageDirectory() + "/";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Uri myUri = Uri.parse(folderPath);
        intent.setDataAndType(myUri, "application/pdf");
        Intent.createChooser(intent, "Select a file");
        resultLauncher.launch(intent);
    }

    private void showPermissionDialog() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", requireActivity().getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2000);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2000);
            }
        } else
            ActivityCompat.requestPermissions(requireActivity(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 333);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!HelperFunctions.checkPermission(requireContext())){
            showPermissionDialog();
        }

        if (path !=null){
            binding.materialCardViewFile.setVisibility(View.VISIBLE);
            binding.chooseFileBtn.setVisibility(View.GONE);
            File file = new File(path);
            binding.fileName.setText(file.getName());
            binding.fileDate.setText(HelperFunctions.getFormattedDate(file.lastModified()));
            binding.fileSize.setText(HelperFunctions.humanReadableByteCountSI(file.length()));

        }else {
            binding.chooseFileBtn.setVisibility(View.VISIBLE);
            binding.materialCardViewFile.setVisibility(View.GONE);
        }

    }

}