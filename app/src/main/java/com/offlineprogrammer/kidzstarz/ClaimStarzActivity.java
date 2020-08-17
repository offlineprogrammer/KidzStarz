package com.offlineprogrammer.kidzstarz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.google.android.material.textfield.TextInputLayout;
import com.offlineprogrammer.kidzstarz.kid.Kid;
import com.offlineprogrammer.kidzstarz.starz.Starz;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
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
    ProgressDialog progressDialog;
    private ImageView claimed_starz_ImageView;
    private ImageView claimed_starz_edit_ImageView;
    private TextView camera_button;
    private Button save_claim_starz;
    private TextInputLayout claimedstarz_desc_text_input;
    private TextInputLayout claimedstarz_count_text_input;
    private TextView warnText;
    private Uri imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_starz);
        firebaseHelper = new FirebaseHelper(getApplicationContext());

        kidMonsterImageView = findViewById(R.id.kid_monster_name);
        sad_starz = findViewById(R.id.sad_starz);
        happy_starz = findViewById(R.id.happy_starz);
        claimed_starz_ImageView = findViewById(R.id.claimed_starz_ImageView);
        claimed_starz_edit_ImageView = findViewById(R.id.claimed_starz_edit_ImageView);
        camera_button = findViewById(R.id.camera_button);
        save_claim_starz = findViewById(R.id.save_claim_starz);
        claimedstarz_desc_text_input = findViewById(R.id.claimedstarz_desc_text_input);
        claimedstarz_count_text_input = findViewById(R.id.claimedstarz_count_text_input);
        warnText = findViewById(R.id.warn_text);

        if (getIntent().getExtras() != null) {
            selectedKid = getIntent().getExtras().getParcelable("selected_kid");
            kidMonsterImageView.setImageResource(getApplicationContext().getResources().getIdentifier(selectedKid.getMonsterImageResourceName(), "drawable",
                    getApplicationContext().getPackageName()));

            setTitle(selectedKid.getKidName());
            happy_starz.setText(String.format("+ %d", selectedKid.getHappyStarz()));
            sad_starz.setText(String.format("- %d", selectedKid.getSadStarz()));

        }

        configButtons();

    }

    private void configButtons() {

        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.create(ClaimStarzActivity.this).returnMode(ReturnMode.ALL)
                        .folderMode(true).includeVideo(false).limit(1).theme(R.style.AppTheme_NoActionBar).single().start();
            }
        });

        claimed_starz_edit_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.create(ClaimStarzActivity.this).returnMode(ReturnMode.ALL)
                        .folderMode(true).includeVideo(false).limit(1).theme(R.style.AppTheme_NoActionBar).single().start();
            }
        });

        save_claim_starz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadImage();

            }
        });
    }

    private void savedClaimedStarz(String s) {
        String trim = this.claimedstarz_desc_text_input.getEditText().getText().toString().trim();
        String trim2 = this.claimedstarz_count_text_input.getEditText().getText().toString().trim();
        if (trim.isEmpty() || trim2.isEmpty()) {
            this.warnText.setText(R.string.some_fields_are_empty);
            this.warnText.setVisibility(View.VISIBLE);
            return;
        }
        this.warnText.setVisibility(View.INVISIBLE);
        int intValue = Integer.valueOf(trim2).intValue();
        if (intValue > selectedKid.getTotalStarz()) {
            this.warnText.setVisibility(View.VISIBLE);
            this.warnText.setText(String.format("%s %s", getString(R.string.maximum_redeem_point), selectedKid.getTotalStarz() < 0 ? 0 : selectedKid.getTotalStarz()));
            return;
        }


        Starz claimedStarz = new Starz(selectedKid.getKidUUID(),
                trim,
                intValue,
                Constants.CLAIMED);

        claimedStarz.setFirestoreImageUri(s);


        saveClaimedStarz(claimedStarz);


    }

    private void saveClaimedStarz(Starz claimedStarz) {
        firebaseHelper.saveStarz(claimedStarz).observeOn(Schedulers.io())
                //.observeOn(Schedulers.m)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Starz>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe");
                        //    disposable = d;
                    }

                    @Override
                    public void onNext(Starz createdStarz) {
                        Log.d(TAG, "onNext: " + createdStarz.getCount());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                firebaseHelper.logEvent("starz_claimed");
                                //dismissProgressBar();
                                firebaseHelper.updateSelectedKidStarz(createdStarz, selectedKid)
                                        .subscribe(() -> {
                                            Log.i(TAG, "updateKidzCollection: completed");
                                            Intent returnIntent = new Intent();
                                            returnIntent.putExtra("result", "result");
                                            setResult(Activity.RESULT_OK, returnIntent);
                                            finish();
                                            //   dismissProgressBar();
                                        }, throwable -> {
                                            // handle error
                                        });

                            }
                        });


                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete");
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
        this.imagePath = UCrop.getOutput(intent);
        GlideApp.with(this).load(this.imagePath.getPath()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).centerCrop().into(this.claimed_starz_ImageView);
        this.camera_button.setVisibility(View.GONE);
        // this.editButton.setVisibility(0);
        this.claimed_starz_ImageView.setVisibility(View.VISIBLE);
        this.claimed_starz_edit_ImageView.setVisibility(View.VISIBLE);
        // this.image = BitmapFactory.decodeFile(this.imagePath);
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (ImagePicker.shouldHandle(i, i2, intent)) {
            Image firstImageOrNull = ImagePicker.getFirstImageOrNull(intent);
            if (firstImageOrNull != null) {
                //UCrop.of(Uri.fromFile(new File(firstImageOrNull.getPath())), Uri.fromFile(new File(getCacheDir(), "cropped"))).start(this);
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

    private void uploadImage() {
        if (this.imagePath != null) {
            // Code for showing progressDialog while uploading
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            firebaseHelper.uploadImage(selectedKid, this.imagePath).observeOn(Schedulers.io())
                    //.observeOn(Schedulers.m)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<Uri>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Log.d(TAG, "onSubscribe");
                            //    disposable = d;
                        }

                        @Override
                        public void onNext(Uri downloadUri) {
                            Log.d(TAG, "onNext: " + downloadUri.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    firebaseHelper.logEvent("image_uploaded");
                                    savedClaimedStarz(downloadUri.toString());
                                    progressDialog.dismiss();
                                }
                            });


                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: " + e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete");
                        }
                    });
        } else {
            savedClaimedStarz(null);

        }

    }

}