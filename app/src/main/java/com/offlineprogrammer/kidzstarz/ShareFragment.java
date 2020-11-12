package com.offlineprogrammer.kidzstarz;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.offlineprogrammer.kidzstarz.kid.Kid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShareFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "celebrate_image";
    private static final String ARG_PARAM2 = "celebrate_note";
    private static final String ARG_PARAM3 = "selectedKid";

    // TODO: Rename and change types of parameters
    private Bitmap m_celebrate_image;
    private String m_celebrate_note;
    private String m_celebrate_imageUrl;
    private Kid m_selectedKid;


    private Context context;
    private TextView date;
    private ImageView imageView;
    private TextView celebrate_note_text;
    private String shareImagePath;
    ImageView kidImageView;
    private FirebaseHelper firebaseHelper;

    private ProgressBar mLogInProgress;


    public ShareFragment() {
        // Required empty public constructor
    }


    public static ShareFragment newInstance() {

        return new ShareFragment();
    }

    public void onAttach(Context context2) {
        super.onAttach(context2);
        this.context = context2;
    }

    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            m_celebrate_image = getArguments().getParcelable(ARG_PARAM1);
            m_celebrate_note = getArguments().getString(ARG_PARAM2);
            m_selectedKid = getArguments().getParcelable(ARG_PARAM3);
        }
    }

    public void setData(String starImageUri, String str, Kid selectedKid) {
        this.m_celebrate_imageUrl = starImageUri;
        this.m_celebrate_note = str;
        this.m_selectedKid = selectedKid;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        firebaseHelper = new FirebaseHelper(getActivity().getApplicationContext());
        initViews(view);
        new Handler().postDelayed(() -> {

            Share(view);
           // showSharePopup(view);
        }, 2000);

    }

    public void Share(View view) {
        this.shareImagePath = captureScreen(view);
    }


    private void initViews(View view) {
        this.celebrate_note_text = view.findViewById(R.id.celebrate_note_text);
        this.imageView = view.findViewById(R.id.giftImage);
        this.date = view.findViewById(R.id.celebrate_share_created_at);
        if (this.m_celebrate_imageUrl == null) {

            imageView.setImageResource(R.drawable.celebrateimage);
        } else
        {
            GlideApp.with(context)
                    .load(Uri.parse(this.m_celebrate_imageUrl))
                    .placeholder(R.drawable.celebrateimage)
                    .into(imageView);
        }

        this.celebrate_note_text.setText(this.m_celebrate_note);
        this.date.setText(DateFormat.format("MMM dd, hh:mm a", new Date()));
        view.findViewById(R.id.share).setOnClickListener(this::showSharePopup);
        kidImageView = view.findViewById(R.id.kidMonsterImage);
        kidImageView.setImageResource(context.getResources().getIdentifier(m_selectedKid.getMonsterImageResourceName(), "drawable",
                context.getPackageName()));
    }

    public void skipShare(View view) {
        ((DetailsActivity) this.context).finish();
    }

    public void showSharePopup(View view) {
        if (this.shareImagePath != null) {
            firebaseHelper.logEvent("share_celebration");
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("*/*");
            ContentValues contentValues = new ContentValues();
            contentValues.put("_data", this.shareImagePath);
            Uri insert = this.context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            intent.putExtra("android.intent.extra.TEXT", "Download Kidz Starz – Reward Kids https://play.google.com/store/apps/details?id=com.offlineprogrammer.kidzstarz&referrer=utm_source%3Dappshare");
            intent.putExtra("android.intent.extra.STREAM", insert);
            startActivity(Intent.createChooser(intent, "Share Image"));
        }
      //  ((DetailsActivity) this.context).finish();
    }

    private String captureScreen(View view) {
        View findViewById = view.findViewById(R.id.gift_layout);
        findViewById.setDrawingCacheEnabled(true);
        Bitmap createBitmap = Bitmap.createBitmap(findViewById.getDrawingCache(), 0, 0, findViewById.getWidth(), findViewById.getHeight());
        findViewById.setDrawingCacheEnabled(false);
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), "/KidzStarz");
            file.mkdirs();
            String path = file.getPath();
            File file2 = new File(path, m_selectedKid.getKidName() + " " + System.currentTimeMillis() + ".jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            createBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            return file2.toString();
        } catch (IOException unused) {
            return null;
        }
    }


}