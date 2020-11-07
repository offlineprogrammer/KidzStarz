package com.offlineprogrammer.kidzstarz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.google.android.material.textfield.TextInputLayout;
import com.offlineprogrammer.kidzstarz.kid.Kid;
import com.offlineprogrammer.kidzstarz.starz.Starz;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Date;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClaimFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClaimFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private static final String ARG_PARAM1 = "selectedKid";


    private Kid m_selectedKid;
    private Context context;
    private FirebaseHelper firebaseHelper;


    ProgressDialog progressDialog;
    private ImageView claimed_starz_ImageView;
    private ImageView claimed_starz_edit_ImageView;
    private TextView camera_button;
    private Button save_claim_starz;
    private TextInputLayout claimedstarz_desc_text_input;
    private TextInputLayout claimedstarz_count_text_input;
    private TextView warnText;
    private Uri imagePath;
    private Bitmap image;
    private static final int CAMERA_REQUEST = 1888;
    private static final String TAG = "ClaimFragment";



    public ClaimFragment() {
        // Required empty public constructor
    }


    public static ClaimFragment newInstance() {
        return new ClaimFragment();
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
            m_selectedKid = getArguments().getParcelable(ARG_PARAM1);
        }
    }

    public void setData(Kid selectedKid) {
        this.m_selectedKid = selectedKid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_claim, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        firebaseHelper = new FirebaseHelper(getActivity().getApplicationContext());
        initViews(view);
    }

    private void initViews(View view) {


        claimed_starz_ImageView = view.findViewById(R.id.claimed_starz_ImageView);
        claimed_starz_edit_ImageView = view.findViewById(R.id.claimed_starz_edit_ImageView);
        camera_button = view.findViewById(R.id.camera_button);
        save_claim_starz = view.findViewById(R.id.save_claim_starz);
        claimedstarz_desc_text_input = view.findViewById(R.id.claimedstarz_desc_text_input);
        claimedstarz_count_text_input = view.findViewById(R.id.claimedstarz_count_text_input);
        warnText = view.findViewById(R.id.warn_text);

        configButtons();
    }

    private void configButtons() {

        camera_button.setOnClickListener(view -> pickImage());

        claimed_starz_edit_ImageView.setOnClickListener(view -> pickImage());

        save_claim_starz.setOnClickListener(view -> uploadImage());
    }

    private void pickImage() {
        ImagePicker.create(this).returnMode(ReturnMode.ALL)
                .folderMode(true).includeVideo(false).limit(1).theme(R.style.AppTheme_NoActionBar).single().start();
    }

    public void onCropFinish(Intent intent) {
        this.imagePath = UCrop.getOutput(intent);
        GlideApp.with(this).load(this.imagePath.getPath()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).centerCrop().into(this.claimed_starz_ImageView);
        this.camera_button.setVisibility(View.GONE);
        // this.editButton.setVisibility(0);
        this.claimed_starz_ImageView.setVisibility(View.VISIBLE);
        this.claimed_starz_edit_ImageView.setVisibility(View.VISIBLE);
        this.image = BitmapFactory.decodeFile(UCrop.getOutput(intent).getPath());
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (ImagePicker.shouldHandle(i, i2, intent)) {
            Image firstImageOrNull = ImagePicker.getFirstImageOrNull(intent);
            if (firstImageOrNull != null) {
                UCrop.of(Uri.fromFile(new File(firstImageOrNull.getPath())), Uri.fromFile(new File(this.context.getCacheDir(), "claimed"+ " " + System.currentTimeMillis() + ".jpg"))).withAspectRatio(1.0f, 1.0f).start((DetailsActivity)this.context);
            }
        }

        if (i == UCrop.REQUEST_CROP) {
            onCropFinish(intent);
        }
    }


    private void uploadImage() {
        if (this.imagePath != null) {
            // Code for showing progressDialog while uploading
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            firebaseHelper.uploadImage(m_selectedKid, this.imagePath).observeOn(Schedulers.io())
                    //.observeOn(Schedulers.m)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<Uri>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Timber.d("onSubscribe");
                            //    disposable = d;
                        }

                        @Override
                        public void onNext(Uri downloadUri) {
                            Timber.d("onNext: %s", downloadUri.toString());
                            getActivity().runOnUiThread(() -> {
                                firebaseHelper.logEvent("image_uploaded");
                                savedClaimedStarz(downloadUri.toString());
                                progressDialog.dismiss();
                            });
                        }
                        @Override
                        public void onError(Throwable e) {
                            Timber.e("onError: %s", e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Timber.d("onComplete");
                        }
                    });
        } else {
            savedClaimedStarz(null);
        }
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
        int intValue = Integer.parseInt(trim2);
        if (intValue > m_selectedKid.getTotalStarz()) {
            this.warnText.setVisibility(View.VISIBLE);
            this.warnText.setText(String.format("%s %s", getString(R.string.maximum_redeem_point), Math.max(m_selectedKid.getTotalStarz(), 0)));
            return;
        }


        Starz claimedStarz = new Starz(m_selectedKid.getKidUUID(),
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
                        Timber.d("onSubscribe");
                        //    disposable = d;
                    }

                    @Override
                    public void onNext(Starz createdStarz) {
                        Timber.d("onNext: %s", createdStarz.getCount());

                        getActivity().runOnUiThread(() -> {
                            firebaseHelper.logEvent("starz_claimed");
                            //dismissProgressBar();
                            firebaseHelper.updateSelectedKidStarz(createdStarz, m_selectedKid)
                                    .subscribe(() -> {
                                        Timber.i("updateKidzCollection: completed");

                                       validate(createdStarz);


                                        //   dismissProgressBar();
                                    }, throwable -> {
                                        // handle error
                                    });

                        });







                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("onError: %s", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("onComplete");
                    }
                });
    }

    private void validate(Starz createdStarz) {
        ((DetailsActivity) this.context).gotoSharePage(this.imagePath, createdStarz.getDesc());
    }




    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, CAMERA_REQUEST);

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).setTitle("Permissions Required").setPositiveButton("Settings").setNegativeButton("Cancel").setRequestCode(5).build().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        EasyPermissions.onRequestPermissionsResult(i, strArr, iArr, this);
    }
}