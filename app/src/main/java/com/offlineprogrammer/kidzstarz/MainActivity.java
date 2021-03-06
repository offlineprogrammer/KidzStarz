package com.offlineprogrammer.kidzstarz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.Task;
import com.offlineprogrammer.kidzstarz.kid.Kid;
import com.offlineprogrammer.kidzstarz.kid.KidAdapter;
import com.offlineprogrammer.kidzstarz.kid.OnKidListener;
import com.offlineprogrammer.kidzstarz.starz.Starz;
import com.offlineprogrammer.kidzstarz.user.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements OnKidListener {
    private static final String TAG = "MainActivity";
    FirebaseHelper firebaseHelper;
    User m_User;
    ProgressDialog progressBar;
    private Disposable disposable;
    private KidAdapter kidAdapter;
    private ArrayList<Kid> kidzList = new ArrayList<>();
    ViewPager2 view_pager;
    private LinearLayout container;
    private int recentPosition;
    private com.google.android.gms.ads.AdView adView;

    ReviewInfo reviewInfo;
    ReviewManager manager;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseHelper = new FirebaseHelper(getApplicationContext());
        // setupProgressBar();
        configActionButton();
        setupViewPager();
        container = findViewById(R.id.indicator_container);
        populateIndicator();
        setIndicator(0);
        setupAds();
    }

    private void setupAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });
        adView = findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


    private void Review() {
        manager = ReviewManagerFactory.create(this);
        manager.requestReviewFlow().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewInfo = task.getResult();
                manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> firebaseHelper.logEvent("rating_failed")).addOnCompleteListener(task1 -> firebaseHelper.logEvent("rating_completed"));
            }

        }).addOnFailureListener(e -> firebaseHelper.logEvent("rating_request_failed"));
    }



    public void onActivityResult(int i, int i2, @Nullable Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 500) {

            updateViewPager();
            int kidzSize = firebaseHelper.kidzStarz.getUser().getKidz().size() - 1;
            int i4 = this.recentPosition;
            if (kidzSize >= i4) {
                this.view_pager.setCurrentItem(i4);
            }
            Review();
        }
    }

    private void populateIndicator() {
        this.container.removeAllViewsInLayout();
        for (int i = 0; i < firebaseHelper.kidzStarz.getUser().getKidz().size(); i++) {
            LayoutParams layoutParams = new LayoutParams(-2, -2);
            layoutParams.setMargins(5, 0, 5, 0);
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageDrawable(getDrawable(R.drawable.scroll_indicator));
            this.container.addView(imageView);
        }
    }

    private void setIndicator(int i) {
        int childCount = this.container.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            this.container.getChildAt(i2).setSelected(i2 == i);
        }
    }

    public void setTitle(int i) {
        this.setTitle(firebaseHelper.kidzStarz.getUser().getKidz().get(i).getKidName());
    }


    private void updateViewPager() {
        kidAdapter = null;
        kidAdapter = new KidAdapter(firebaseHelper.kidzStarz.getUser().getKidz(), this);
        view_pager = findViewById(R.id.view_pager);
        view_pager.setAdapter(kidAdapter);
        populateIndicator();
        setIndicator(0);

        dismissProgressBar();

    }


    private void setupViewPager() {
        kidAdapter = new KidAdapter(firebaseHelper.kidzStarz.getUser().getKidz(), this);
        view_pager = findViewById(R.id.view_pager);
        view_pager.setAdapter(kidAdapter);
        view_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {


            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Timber.d("onPageSelected: position %s", position);
                setIndicator(position);
                recentPosition = position;
                setTitle(position);


                Timber.e(String.valueOf(position));
            }


        });
    }

    private void configActionButton() {
        Button add_kid = findViewById(R.id.add_kid);
        add_kid.setOnClickListener(view -> {
            //throw new RuntimeException("Test Crash"); // Force a crash
            showAddKidDialog(MainActivity.this);
        });
    }


    private void showAddKidDialog(Context c) {

        final AlertDialog builder = new AlertDialog.Builder(c).create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_add_kid, null);
        final TextInputLayout kidNameText = dialogView.findViewById(R.id.kidname_text_input);
        kidNameText.requestFocus();
        Button okBtn= dialogView.findViewById(R.id.kidname_save_button);
        Button cancelBtn = dialogView.findViewById(R.id.kidname_cancel_button);
        okBtn.setOnClickListener(v -> {
            String kidName = String.valueOf(kidNameText.getEditText().getText());
            if (!isKidNameValid(kidName)) {
                kidNameText.setError(getString(R.string.kid_error_name));
            } else {
                kidNameText.setError(null);
                Date currentTime = Calendar.getInstance().getTime();
                int monsterImage = pickMonster();
                String monsterImageResourceName = getResources().getResourceEntryName(monsterImage);

                Kid newKid = new Kid(kidName,
                        monsterImageResourceName,
                        currentTime);
                setupProgressBar();
                saveKid(newKid);

                //  mFirebaseAnalytics.logEvent("kid_created", null);
                builder.dismiss();
            }


        });

        kidNameText.setOnKeyListener((view, i, keyEvent) -> {
            String kidName = String.valueOf(kidNameText.getEditText().getText());
            if (isKidNameValid(kidName)) {
                kidNameText.setError(null); //Clear the error
            }
            return false;
        });
        cancelBtn.setOnClickListener(v -> builder.dismiss());
        builder.setView(dialogView);
        builder.show();
        builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void updateViewPager(Kid kid) {
        Timber.i("onClick UserFireStore : %s", kid.getKidName());

        kidAdapter.add(kid, 0);
        setIndicator(firebaseHelper.kidzStarz.getUser().getKidz().size()-1);
        setIndicator(firebaseHelper.kidzStarz.getUser().getKidz().size()-1);
        dismissProgressBar();
    }

    private void saveKid(Kid newKid) {

        firebaseHelper.saveKid(newKid).observeOn(Schedulers.io())
                //.observeOn(Schedulers.m)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Kid>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Timber.d("onSubscribe");
                        disposable = d;
                    }

                    @Override
                    public void onNext(Kid kid) {
                        Timber.d("onNext: %s", kid.getKidName());
                        runOnUiThread(() -> {
                            firebaseHelper.logEvent("kid_created");
                            //updateViewPager(kid);
                            updateViewPager();
                            firebaseHelper.updateKidzCollection(kid)
                                    .subscribe(() -> {
                                        Timber.i("updateKidzCollection: completed");
                                        updateViewPager();
                                        Review();
                                        // handle completion
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


    private boolean isKidNameValid(String kidName) {
        return kidName != null && kidName.length() >= 2;
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

    private int pickMonster() {
        final TypedArray imgs;
        imgs = getResources().obtainTypedArray(R.array.kidzMonsters);
        final Random rand = new Random();
        final int rndInt = rand.nextInt(imgs.length());
        return imgs.getResourceId(rndInt, 0);
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
        dismissWithCheck(progressBar);
        super.onDestroy();
    }


    @Override
    public void onKidClick(int position) {

    }

    @Override
    public void showAddHappyStarzDialog(int position) {
        Timber.i("showAddHappyStarzDialog: clicked %s", position);
        showAddHappyStarzDialog(MainActivity.this, position);
    }

    @Override
    public void showAddSadStarzDialog(int position) {
        Timber.i("showAddSadStarzDialog: clicked %s", position);
        showAddSadStarzDialog(MainActivity.this, position);

    }

    @Override
    public void deleteKid(int position) {
        Timber.i("deleteKid: Clicked %s", position);
        Kid selectedKid = firebaseHelper.kidzStarz.getUser().getKidz().get(position);
        final AlertDialog builder = new AlertDialog.Builder(MainActivity.this).create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_delete_kid, null);
        Button okBtn = dialogView.findViewById(R.id.deletekid_confirm_button);
        Button cancelBtn = dialogView.findViewById(R.id.deletekid_cancel_button);

        okBtn.setOnClickListener(v -> {
            builder.dismiss();
            deleteKid(selectedKid);
            // mFirebaseAnalytics.logEvent("kid_deleted", null);
        });
        cancelBtn.setOnClickListener(v -> builder.dismiss());
        builder.setView(dialogView);
        builder.show();
    }

    private void deleteKid(Kid theSelectedKid) {
        firebaseHelper.deleteKid(theSelectedKid)
                .subscribe(() -> {
                    Timber.i("updateRewardImage: completed");
                    firebaseHelper.logEvent("kid_deleted");
                    firebaseHelper.deleteKidStarzCollection(theSelectedKid)
                            .subscribe(this::updateViewPager, throwable -> {

                            });

                }, throwable -> {
                    // handle error
                });

    }

    @Override
    public void showMoreInfo(int position) {
        Timber.i("showMoreInfo: Clicked %s", position);
        Kid selectedKid = firebaseHelper.kidzStarz.getUser().getKidz().get(position);
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("selected_kid", selectedKid);
        this.recentPosition = this.view_pager.getCurrentItem();
        startActivityForResult(intent, 500);
        // startActivity(intent);
    }


    private void showAddHappyStarzDialog(Context c, int position) {
        final AlertDialog builder = new AlertDialog.Builder(c).create();
        LayoutInflater inflater = LayoutInflater.from(c);
        View dialogView = inflater.inflate(R.layout.alert_dialog_add_happystar, null);
        final TextInputLayout HappyStarDescText = dialogView.findViewById(R.id.happystar_desc_text_input);
        final TextInputLayout HappyStarCountText = dialogView.findViewById(R.id.happystar_count_text_input);
        HappyStarDescText.requestFocus();
        Button okBtn = dialogView.findViewById(R.id.happystar_save_button);
        Button cancelBtn = dialogView.findViewById(R.id.happystar_cancel_button);
        okBtn.setOnClickListener(v -> {
            String happyStarDesc = String.valueOf(HappyStarDescText.getEditText().getText());
            String happyStarCount = String.valueOf(HappyStarCountText.getEditText().getText());
            if (!isDescValid(happyStarDesc)) {
                HappyStarDescText.setError(c.getString(R.string.star_desc_error));
            } else if (!isCountValid(happyStarCount, firebaseHelper.kidzStarz.getUser().getKidz().get(position), Constants.SAD)) {
                HappyStarCountText.setError(c.getString(R.string.star_count_error));
            } else {
                HappyStarDescText.setError(null);
                Date currentTime = Calendar.getInstance().getTime();
                Kid selectedKid = firebaseHelper.kidzStarz.getUser().getKidz().get(position);
                Starz happyStarz = new Starz(selectedKid.getKidUUID(), happyStarDesc, Integer.valueOf(happyStarCount.trim()), Constants.HAPPY);
                setupProgressBar();
                saveStarz(happyStarz, position);

                builder.dismiss();
            }


        });

        cancelBtn.setOnClickListener(v -> {
            builder.dismiss();

            // btnAdd1 has been clicked

        });
        builder.setView(dialogView);
        builder.show();
        builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void saveStarz(Starz starz, int position) {

        firebaseHelper.saveStarz(starz).observeOn(Schedulers.io())
                //.observeOn(Schedulers.m)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Starz>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Timber.d("onSubscribe");
                        disposable = d;
                    }

                    @Override
                    public void onNext(Starz createdStarz) {
                        Timber.d("onNext: %s", createdStarz.getCount());
                        firebaseHelper.logEvent("starz_created_" + createdStarz.getType());
                        //dismissProgressBar();
                        updateKidStarz(createdStarz, position);


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

    private void updateKidStarz(Starz createdStarz, int position) {
        firebaseHelper.updateKidStarz(createdStarz, position)
                .subscribe(() -> {
                    Timber.i("updateKidzCollection: completed");
                    updateViewPager();
                    dismissProgressBar();
                    Review();
                }, throwable -> {
                    // handle error
                });
    }

    private void showAddSadStarzDialog(Context c, int position) {
        final AlertDialog builder = new AlertDialog.Builder(c).create();
        LayoutInflater inflater = LayoutInflater.from(c);
        View dialogView = inflater.inflate(R.layout.alert_dialog_add_sadstar, null);
        final TextInputLayout sadStarDescText = dialogView.findViewById(R.id.sadstar_desc_text_input);
        final TextInputLayout sadStarCountText = dialogView.findViewById(R.id.sadstar_count_text_input);
        sadStarDescText.requestFocus();
        Button okBtn = dialogView.findViewById(R.id.sadstar_save_button);
        Button cancelBtn = dialogView.findViewById(R.id.sadstar_cancel_button);
        okBtn.setOnClickListener(v -> {
            String sadStarDesc = String.valueOf(sadStarDescText.getEditText().getText());
            String sadStarCount = String.valueOf(sadStarCountText.getEditText().getText());
            if (!isDescValid(sadStarDesc)) {
                sadStarDescText.setError(c.getString(R.string.star_desc_error));
            } else if (!isCountValid(sadStarCount, firebaseHelper.kidzStarz.getUser().getKidz().get(position), Constants.SAD)) {
                sadStarCountText.setError(c.getString(R.string.star_count_error));
            } else {
                sadStarDescText.setError(null);
                Date currentTime = Calendar.getInstance().getTime();
                Kid selectedKid = firebaseHelper.kidzStarz.getUser().getKidz().get(position);
                Starz sadStarz = new Starz(selectedKid.getKidUUID(), sadStarDesc, Integer.valueOf(sadStarCount.trim()), Constants.SAD);
                setupProgressBar();
                saveStarz(sadStarz, position);

                builder.dismiss();
            }


        });

        cancelBtn.setOnClickListener(v -> {
            builder.dismiss();

            // btnAdd1 has been clicked

        });
        builder.setView(dialogView);
        builder.show();
        builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private boolean isDescValid(String starDesc) {
        return starDesc != null && starDesc.length() >= 2;
    }

    private boolean isCountValid(String starzCount, Kid kid, String starzType) {
        if (starzCount == null) {
            return false;
        }
        try {
            Integer iCount = Integer.parseInt(starzCount);
        } catch (NumberFormatException nfe) {
            return false;
        }


        return true;


    }
}