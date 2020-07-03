package com.offlineprogrammer.kidzstarz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.offlineprogrammer.kidzstarz.kid.Kid;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ClaimStarzActivity extends AppCompatActivity {

    private static final String TAG = "ClaimStarzActivity";
    private static final int CAMERA_REQUEST = 2222;


    FirebaseHelper firebaseHelper;
    Kid selectedKid;

    private ImageView kidMonsterImageView;
    private TextView sad_starz;
    private TextView happy_starz;
    private String imagePath;
    private ImageView claimed_starz_ImageView;
    private TextView camera_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_starz);
        firebaseHelper = new FirebaseHelper(getApplicationContext());

        kidMonsterImageView = findViewById(R.id.kid_monster_name);
        sad_starz = findViewById(R.id.sad_starz);
        happy_starz = findViewById(R.id.happy_starz);
        claimed_starz_ImageView = findViewById(R.id.claimed_starz_ImageView);
        camera_button = findViewById(R.id.camera_button);

        if (getIntent().getExtras() != null) {
            selectedKid = getIntent().getExtras().getParcelable("selected_kid");
            kidMonsterImageView.setImageResource(getApplicationContext().getResources().getIdentifier(selectedKid.getMonsterImageResourceName(), "drawable",
                    getApplicationContext().getPackageName()));

            setTitle(selectedKid.getKidName());
            happy_starz.setText(String.format("+ %d", selectedKid.getHappyStarz()));
            sad_starz.setText(String.format("- %d", selectedKid.getSadStarz()));

        }

        configCameraButton();

    }

    private void configCameraButton() {

        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.create(ClaimStarzActivity.this).returnMode(ReturnMode.ALL)
                        .folderMode(true).includeVideo(false).limit(1).theme(R.style.AppTheme_NoActionBar).single().start();
            }
        });
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        EasyPermissions.onRequestPermissionsResult(i, strArr, iArr, this);
    }

    public void onPermissionsGranted(int i, @NonNull List<String> list) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        //  ((ClaimStarzActivity) this.context).isManuallyPaused(true);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    public void onPermissionsDenied(int i, @NonNull List<String> list) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, list)) {
            new AppSettingsDialog.Builder(this).setTitle("Permissions Required").setPositiveButton("Settings").setNegativeButton("Cancel").setRequestCode(5).build().show();
        }
    }

    public void onCropFinish(Intent intent) {
        Uri output = UCrop.getOutput(intent);
        this.imagePath = output.getPath();
        GlideApp.with(this).load(output.getPath()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).centerCrop().into(this.claimed_starz_ImageView);
        this.camera_button.setVisibility(View.GONE);
        // this.editButton.setVisibility(0);
        this.claimed_starz_ImageView.setVisibility(View.VISIBLE);
        // this.image = BitmapFactory.decodeFile(this.imagePath);
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (ImagePicker.shouldHandle(i, i2, intent)) {
            Image firstImageOrNull = ImagePicker.getFirstImageOrNull(intent);
            if (firstImageOrNull != null) {
                UCrop.of(Uri.fromFile(new File(firstImageOrNull.getPath())), Uri.fromFile(new File(getCacheDir(), "cropped"))).withAspectRatio(1.0f, 1.0f).start(this);

                //   Uri destinationUri = Uri.fromFile(new File(myContext.getCacheDir(), "IMG_" + System.currentTimeMillis()));
                //   UCrop.of(sourceUri, destinationUri)
                //           .withMaxResultSize(1080, 768) // any resolution you want
                //           .start(mContext, YourFragment/YourActivity.this);

            }
        }

        if (i == UCrop.REQUEST_CROP) {
            onCropFinish(intent);
        }
    }

}