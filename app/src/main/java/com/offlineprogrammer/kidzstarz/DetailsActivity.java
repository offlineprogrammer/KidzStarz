package com.offlineprogrammer.kidzstarz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.offlineprogrammer.kidzstarz.kid.Kid;
import com.offlineprogrammer.kidzstarz.starz.OnStarzListener;
import com.offlineprogrammer.kidzstarz.starz.Starz;
import com.offlineprogrammer.kidzstarz.starz.StarzAdapter;
import com.offlineprogrammer.kidzstarz.starz.StarzGridItemDecoration;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        firebaseHelper = new FirebaseHelper(getApplicationContext());

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

    private void setupAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
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
        Log.d(TAG, "onBackPressed Called");
        goBack();
    }

    private void goBack() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", "result");
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                break;
        }
        return true;
    }


    private void configClaimStarzButton() {
        Button claim_starz = findViewById(R.id.claim_starz);
        claim_starz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(DetailsActivity.this, ClaimStarzActivity.class);
                mIntent.putExtra("selected_kid", selectedKid);
                startActivityForResult(mIntent, 3);
            }
        });
    }

    private void getkidStarz() {
        firebaseHelper.getkidStarz(selectedKid).observeOn(Schedulers.io())
                //.observeOn(Schedulers.m)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<ArrayList<Starz>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe");
                        disposable = d;
                    }

                    @Override
                    public void onNext(ArrayList<Starz> starzs) {
                        Log.d(TAG, "onNext:  " + starzs.size());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateRecylerView(starzs);
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

    private void updateRecylerView(ArrayList<Starz> starzs) {
        starzAdapter.updateData(starzs);
        starzRecyclerView.scrollToPosition(0);

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

    }

    @Override
    public void onStarzClick(int position) {

    }
}