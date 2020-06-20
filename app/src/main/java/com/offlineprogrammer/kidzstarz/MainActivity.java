package com.offlineprogrammer.kidzstarz;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
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

import com.google.android.material.textfield.TextInputLayout;
import com.offlineprogrammer.kidzstarz.kid.Kid;
import com.offlineprogrammer.kidzstarz.kid.KidAdapter;
import com.offlineprogrammer.kidzstarz.kid.OnKidListener;
import com.offlineprogrammer.kidzstarz.user.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


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
            if (i2 == i) {
                this.container.getChildAt(i2).setSelected(true);
            } else {
                this.container.getChildAt(i2).setSelected(false);
            }
        }
    }




    private void setupViewPager() {
        kidAdapter = new KidAdapter(firebaseHelper.kidzStarz.getUser().getKidz(),this);
        view_pager = findViewById(R.id.view_pager);
        view_pager.setAdapter(kidAdapter);
        view_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {


            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setIndicator(position);

                Log.e("Selected_Page", String.valueOf(position));
            }


        });



    }

    private void configActionButton() {
        Button add_kid = findViewById(R.id.add_kid);
        add_kid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddKidDialog(MainActivity.this);
            }
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
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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


            }
        });

        kidNameText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                String kidName = String.valueOf(kidNameText.getEditText().getText());
                if (isKidNameValid(kidName)) {
                    kidNameText.setError(null); //Clear the error
                }
                return false;
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                builder.dismiss();
            }
        });
        builder.setView(dialogView);
        builder.show();
        builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void updateViewPager(Kid kid) {
        Log.i(TAG, "onClick UserFireStore : " + kid.getKidName());

        kidAdapter.add(kid, 0);
        dismissProgressBar();
    }

    private void saveKid(Kid newKid) {

        firebaseHelper.saveKid(newKid).observeOn(Schedulers.io())
                //.observeOn(Schedulers.m)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Kid>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe");
                        disposable = d;
                    }

                    @Override
                    public void onNext(Kid kid) {
                        Log.d(TAG, "onNext: " + kid.getKidName());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                firebaseHelper.logEvent("kid_created");
                                updateViewPager(kid);
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
        } catch (final IllegalArgumentException e) {
            // Do nothing.
        } catch (final Exception e) {
            // Do nothing.
        } finally {
            dialog = null;
        }
    }

    private int pickMonster(){
        final TypedArray imgs;
        imgs = getResources().obtainTypedArray(R.array.kidzMonsters);
        final Random rand = new Random();
        final int rndInt = rand.nextInt(imgs.length());
        return imgs.getResourceId(rndInt, 0);
    }


    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
       /* if (adView != null) {
            adView.destroy();
        }*/
        dismissWithCheck(progressBar);
        super.onDestroy();
    }


    @Override
    public void onKidClick(int position) {

    }
}