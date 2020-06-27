package com.offlineprogrammer.kidzstarz;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.offlineprogrammer.kidzstarz.kid.Kid;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "DetailsActivity";
    Kid selectedKid;
    private ImageView kidMonsterImageView;

    private TextView sad_starz;
    private TextView happy_starz;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
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

            //rewardimageView.setImageResource( getApplicationContext().getResources().getIdentifier(selectedPlan.getRewardImageResourceName() , "drawable" ,
            //        getApplicationContext().getPackageName()) );
            //getPlanItemz();
        }
    }
}