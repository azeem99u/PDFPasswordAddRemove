package stkmakers.pdfpasswordaddRemove;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private final Context context;
    private final Activity activity;

    private ArrayList<File> files = new ArrayList<>();

    public FileAdapter(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false));
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File selectedFile = files.get(position);
        holder.fileName.setText(selectedFile.getName());
        holder.fileSize.setText(HelperFunctions.humanReadableByteCountSI(selectedFile.length()));
        Date lastModDate = new Date(selectedFile.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDateAndTime = sdf.format(lastModDate);
        holder.fileDate.setText(currentDateAndTime);
        holder.itemView.setOnClickListener(view -> renderPdf(context, selectedFile));
        holder.moreIcon.setOnClickListener(v -> {

            int[] location = new int[2];
            v.getLocationOnScreen(location);
            Point point = new Point();
            point.x = location[0];
            point.y = location[1];

            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams")
            View layout = layoutInflater.inflate(R.layout.pop_layout, null);

            PopupWindow changeStatusPopUp = new PopupWindow(activity);
            changeStatusPopUp.setContentView(layout);
            changeStatusPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
            changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            changeStatusPopUp.setFocusable(true);
            int OFFSET_X = -20;
            int OFFSET_Y = 70;
            changeStatusPopUp.setBackgroundDrawable(null);
            changeStatusPopUp.showAtLocation(layout, Gravity.NO_GRAVITY, point.x + OFFSET_X, point.y + OFFSET_Y);

            @SuppressLint("CutPasteId")
            TextView rename = layout.findViewById(R.id.renameBtn);
            @SuppressLint("CutPasteId")
            TextView delete = layout.findViewById(R.id.deleteBtn);
            @SuppressLint("CutPasteId")
            TextView share = layout.findViewById(R.id.shareBtn);

            rename.setOnClickListener(view -> {
                changeStatusPopUp.dismiss();
                renameFile(holder, selectedFile);
            });
            delete.setOnClickListener(view -> {
                changeStatusPopUp.dismiss();
                boolean deleted = selectedFile.delete();
                if (deleted) {
                    files.remove(position);
                    notifyItemRemoved(position);
                }
            });
            share.setOnClickListener(view -> {
                changeStatusPopUp.dismiss();
                sharePdf(context, selectedFile);
            });
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void renameFile(ViewHolder holder, File selectedFile) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setIcon(R.drawable.ic_baseline_drive_file_rename_outline_24);
        alert.setTitle("Rename File");
        final EditText input = new EditText(activity);
        input.setText(selectedFile.getName());
        alert.setView(input);
        alert.setPositiveButton("OK", (dialog, whichButton) -> {
            String srt1 = input.getEditableText().toString().trim();
            if (!srt1.isEmpty()) {
                File file;
                if (srt1.endsWith(".pdf")) {
                    file = HelperFunctions.renameLockedFile(selectedFile.getName(), srt1, activity);
                } else {
                    file = HelperFunctions.renameLockedFile(selectedFile.getName(), srt1 + ".pdf", activity);
                }
                if (file != null) {
                    files.set(holder.getAdapterPosition(),file);;
                    notifyDataSetChanged();
                }
            } else {
                Toast.makeText(activity, "Field is empty!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
        alert.setNegativeButton("CANCEL", (dialog, whichButton) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }


    @Override
    public int getItemCount() {
        return files.size();
    }

    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView fileName, fileDate, fileSize;
        ImageView fileImage, moreIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileImage = itemView.findViewById(R.id.icon_view);
            moreIcon = itemView.findViewById(R.id.moreIcon);
            fileDate = itemView.findViewById(R.id.fileDate);
            fileSize = itemView.findViewById(R.id.fileSize);
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void renderPdf(Context context, File filePath) {
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationInfo().packageName + ".provider", filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "application/pdf");
        PackageManager pm = context.getPackageManager();
        try {
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void sharePdf(Context context, File filePath) {
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationInfo().packageName + ".provider", filePath);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        PackageManager pm = context.getPackageManager();
        try {
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}