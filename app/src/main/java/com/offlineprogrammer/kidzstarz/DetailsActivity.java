package com.offlineprogrammer.kidzstarz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.Task;
import com.offlineprogrammer.kidzstarz.kid.Kid;
import com.offlineprogrammer.kidzstarz.starz.OnStarzListener;
import com.offlineprogrammer.kidzstarz.starz.Starz;
import com.offlineprogrammer.kidzstarz.starz.StarzAdapter;
import com.offlineprogrammer.kidzstarz.starz.StarzGridItemDecoration;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DetailsActivity extends AppCompatActivity implements OnStarzListener {
    private static final String TAG = "DetailsActivity";

    FirebaseHelper firebaseHelper;
    Kid selectedKid;
    private ImageView kidMonsterImageView;
    private TextView sad_starz;
    private TextView happy_starz;

    private StarzAdapter starzAdapter;
    private RecyclerView starzRecyclerView;
    private ArrayList<Starz> starzArrayList = new ArrayList<>();
    private Disposable disposable;
    private com.google.android.gms.ads.AdView adView;
    private Fragment currentFragment;
    ProgressDialog progressBar;

    ReviewInfo reviewInfo;
    ReviewManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        firebaseHelper = new FirebaseHelper(getApplicationContext());
        setupProgressBar();

        kidMonsterImageView = findViewById(R.id.kid_monster_name);
        sad_starz = findViewById(R.id.sad_starz);
        happy_starz = findViewById(R.id.happy_starz);


        if (getIntent().getExtras() != null) {
            selectedKid = getIntent().getExtras().getParcelable("selected_kid");
            kidMonsterImageView.setImageResource(getApplicationContext().getResources().getIdentifier(selectedKid.getMonsterImageResourceName(), "drawable",
                    getApplicationContext().getPackageName()));

            setTitle(selectedKid.getKidName());
            happy_starz.setText(String.format("+ %d", selectedKid.getHappyStarz()));
            sad_starz.setText(String.format("- %d", selectedKid.getSadStarz()));
            getkidStarz();
        }

        setupRecyclerView();
        configClaimStarzButton();

        setupAds();

    }


    private void Review() {
        manager = ReviewManagerFactory.create(this);
        manager.requestReviewFlow().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewInfo = task.getResult();
                manager.launchReviewFlow(DetailsActivity.this, reviewInfo).addOnFailureListener(e -> firebaseHelper.logEvent("rating_failed")).addOnCompleteListener(task1 -> firebaseHelper.logEvent("rating_completed"));
            }

        }).addOnFailureListener(e -> firebaseHelper.logEvent("rating_request_failed"));
    }


    private void setupProgressBar() {
        dismissProgressBar();
        progressBar = new ProgressDialog(this);
        progressBar.setMessage("Loading data ...");
        progressBar.show();
    }

    private void dismissProgressBar() {
        dismissWithCheck(progressBar);
    }

    public void dismissWithCheck(ProgressDialog dialog) {
        if (dialog != null) {
            if (dialog.isShowing()) {

                //get the Context object that was used to great the dialog
                Context context = ((ContextWrapper) dialog.getContext()).getBaseContext();

                // if the Context used here was an activity AND it hasn't been finished or destroyed
                // then dismiss it
                if (context instanceof Activity) {

                    // Api >=17
                    if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                        dismissWithTryCatch(dialog);
                    }
                } else
                    // if the Context used wasn't an Activity, then dismiss it too
                    dismissWithTryCatch(dialog);
            }
            dialog = null;
        }
    }

    public void dismissWithTryCatch(ProgressDialog dialog) {
        try {
            dialog.dismiss();
        } catch (final Exception e) {
            // Do nothing.
        } finally {
            dialog = null;
        }
    }

    private void setupAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });
        adView = findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
//        dismissWithCheck(progressBar);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        Timber.d("onBackPressed Called");
        goBack();
    }

    private void goBack() {


        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragment = this.currentFragment;
        if (fragment instanceof ClaimFragment) {
            this.setTitle(selectedKid.getKidName());
            Review();
          //  return;
        } else if (fragment instanceof ShareFragment) {


            setResult(0, new Intent());
            finish();
          //  return;
        }

        if (supportFragmentManager.getBackStackEntryCount() <= 0 ) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", "result");
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else {
            supportFragmentManager.popBackStack();
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBack();
        }
        return true;
    }


    public void onActivityResult(int i, int i2, @Nullable Intent intent) {
        super.onActivityResult(i, i2, intent);
        ClaimFragment claimFragment;

        if (i2 == -1 && i == 69) {
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            if (supportFragmentManager.getBackStackEntryCount() > 0) {
                String name = supportFragmentManager.getBackStackEntryAt(supportFragmentManager.getBackStackEntryCount() - 1).getName();
                if (Constants.CLAIM.equals(name) && (claimFragment = (ClaimFragment) supportFragmentManager.findFragmentByTag(name)) != null && claimFragment.isVisible()) {
                    claimFragment.onCropFinish(intent);
                }  //onCropFinish(intent);

            }  // onCropFinish(intent);


        }


        if (ImagePicker.shouldHandle(i, i2, intent)) {
            Image firstImageOrNull = ImagePicker.getFirstImageOrNull(intent);
            if (firstImageOrNull != null) {
                UCrop.of(Uri.fromFile(new File(firstImageOrNull.getPath())), Uri.fromFile(new File(getCacheDir(), "cropped"))).withAspectRatio(1.0f, 1.0f).start(this);
            }
        }


        Timber.i("onActivityResult: We r back");
        getkidStarz();

    }
    public void gotoSharePage(Uri starImageUri, Starz str, String claimedImagePath) {
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        this.setTitle(R.string.share);
        ShareFragment newInstance = ShareFragment.newInstance();
        newInstance.setData( ((starImageUri == null) ? null : starImageUri.toString())  , str.getDesc(), selectedKid);
        this.currentFragment = newInstance;
        beginTransaction.replace(R.id.container, newInstance, Constants.SHARE);
        beginTransaction.addToBackStack(Constants.SHARE);
        firebaseHelper.logEvent("show_share_page");
        beginTransaction.commit();
    }

    private void gotoClaimPage() {
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        this.setTitle(R.string.claim);
        ClaimFragment newInstance = ClaimFragment.newInstance();
        newInstance.setData(selectedKid);

        this.currentFragment = newInstance;
        beginTransaction.add(R.id.container, newInstance, Constants.CLAIM);
        beginTransaction.addToBackStack(Constants.CLAIM);
        firebaseHelper.logEvent("show_share_page");
        beginTransaction.commit();

    }


    private void configClaimStarzButton() {
        Button claim_starz = findViewById(R.id.claim_starz);
        claim_starz.setOnClickListener(view -> gotoClaimPage());
    }



    private void getkidStarz() {
        setupProgressBar();
        firebaseHelper.getkidStarz(selectedKid).observeOn(Schedulers.io())
                //.observeOn(Schedulers.m)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<ArrayList<Starz>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Timber.d("onSubscribe");
                        disposable = d;
                    }

                    @Override
                    public void onNext(ArrayList<Starz> starzs) {
                        Timber.d("onNext:  %s", starzs.size());
                        runOnUiThread(() -> updateRecylerView(starzs));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("onError: %s", e.getMessage());
                        dismissProgressBar();
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("onComplete");
                        dismissProgressBar();
                    }
                });

    }

    private void updateRecylerView(ArrayList<Starz> starzs) {
        starzAdapter.updateData(starzs);
        starzRecyclerView.scrollToPosition(0);
        dismissProgressBar();

    }


    private void setupRecyclerView() {
        starzAdapter = new StarzAdapter(starzArrayList, this);
        starzRecyclerView = findViewById(R.id.starz_recyclerview);
        starzRecyclerView.setHasFixedSize(true);
        starzRecyclerView.setAdapter(starzAdapter);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        starzRecyclerView.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false));
        starzRecyclerView.setItemAnimator(new DefaultItemAnimator());
        int largePadding = getResources().getDimensionPixelSize(R.dimen.ksz_starz_grid_spacing_small);
        int smallPadding = getResources().getDimensionPixelSize(R.dimen.ksz_starz_grid_spacing_small);
        starzRecyclerView.addItemDecoration(new StarzGridItemDecoration(largePadding, smallPadding));
      //  dismissProgressBar();

    }

    @Override
    public void onStarzClick(int position) {

    }
}