package stkmakers.pdfpasswordaddRemove.ui.fragments;

import static stkmakers.pdfpasswordaddRemove.HelperFunctions.ADS_VIEW;
import static stkmakers.pdfpasswordaddRemove.HelperFunctions.ITEMS_VIEW;
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

import com.google.android.gms.ads.nativead.NativeAd;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stkmakers.pdfpasswordaddRemove.HelperFunctions;
import stkmakers.pdfpasswordaddRemove.R;
import stkmakers.pdfpasswordaddRemove.arch.FileModel;
import stkmakers.pdfpasswordaddRemove.databinding.NativeAdLayoutBinding;
import stkmakers.pdfpasswordaddRemove.databinding.RecyclerItemBinding;
import stkmakers.pdfpasswordaddRemove.native_ad_temp.NativeTemplateStyle;
import stkmakers.pdfpasswordaddRemove.native_ad_temp.TemplateView;
public class LockedFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final Activity activity;
    private List<Object> mFiles = new ArrayList<>();
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LockedFragmentAdapter(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case ADS_VIEW: {
                NativeAdLayoutBinding binding = NativeAdLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new AdsViewHolder(binding);
            }
            case ITEMS_VIEW:
            default:
                RecyclerItemBinding binding = RecyclerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new MyViewHolder(binding);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ADS_VIEW:
                AdsViewHolder adsViewHolder = (AdsViewHolder) holder;
                if (mFiles.get(position) instanceof NativeAd) {
                    NativeAd nativeAd = (NativeAd) mFiles.get(position);
                    populateNativeAdView(nativeAd, adsViewHolder);
                }
                break;
            case ITEMS_VIEW:
            default:
                if (mFiles.get(position) instanceof FileModel) {
                    MyViewHolder myViewHolder = (MyViewHolder) holder;
                    FileModel fileModel = (FileModel) mFiles.get(position);
                    onBindMyViewHolder(myViewHolder, position, fileModel);
                }
        }
    }

    private void onBindMyViewHolder(MyViewHolder holder, int position, FileModel fileModel) {

        try {

            File selectedFile = fileModel.getFile();
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
                        mFiles.remove(position);
                        notifyItemRemoved(position);
                    }
                });
                share.setOnClickListener(view -> {
                    changeStatusPopUp.dismiss();
                    sharePdf(context, selectedFile);
                });
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFiles(List<Object> mFiles) {
        this.mFiles = mFiles;
        notifyDataSetChanged();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileDate, fileSize;
        ImageView fileImage, moreIcon;

        MyViewHolder(@NonNull RecyclerItemBinding binding) {
            super(binding.getRoot());
            fileName = itemView.findViewById(R.id.fileName);
            fileImage = itemView.findViewById(R.id.icon_view);
            moreIcon = itemView.findViewById(R.id.moreIcon);
            fileDate = itemView.findViewById(R.id.fileDate);
            fileSize = itemView.findViewById(R.id.fileSize);
        }

    }

    static class AdsViewHolder extends RecyclerView.ViewHolder {
        public TemplateView myTemplate;



        public AdsViewHolder(@NonNull NativeAdLayoutBinding binding) {
            super(binding.getRoot());
            myTemplate = binding.myTemplate;
        }
    }

    private void populateNativeAdView(NativeAd nativeAd, AdsViewHolder viewHolder) {
        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().build();
        TemplateView template = viewHolder.myTemplate;
        template.setStyles(styles);
        template.setNativeAd(nativeAd);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        Object file = mFiles.get(position);
        if (file instanceof NativeAd) {
            return ADS_VIEW;
        }
        return ITEMS_VIEW;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
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

    @SuppressLint("NotifyDataSetChanged")
    private void renameFile(MyViewHolder holder, File selectedFile) {
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
                    mFiles.set(holder.getAdapterPosition(),file);;
                    notifyItemChanged(holder.getAdapterPosition());
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

}
