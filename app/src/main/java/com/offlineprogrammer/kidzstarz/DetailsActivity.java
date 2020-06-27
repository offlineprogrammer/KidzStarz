package com.offlineprogrammer.kidzstarz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.offlineprogrammer.kidzstarz.kid.Kid;

public class DetailsActivity extends AppCompatActivity {
    Kid selectedKid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (getIntent().getExtras() != null) {
            selectedKid = getIntent().getExtras().getParcelable("selected_kid");
            // planImageView.setImageResource( getApplicationContext().getResources().getIdentifier(selectedPlan.getPlanImageResourceName() , "drawable" ,
            //         getApplicationContext().getPackageName()) );

            setTitle(selectedKid.getKidName());
            //rewardimageView.setImageResource( getApplicationContext().getResources().getIdentifier(selectedPlan.getRewardImageResourceName() , "drawable" ,
            //        getApplicationContext().getPackageName()) );
            //getPlanItemz();
        }
    }
}