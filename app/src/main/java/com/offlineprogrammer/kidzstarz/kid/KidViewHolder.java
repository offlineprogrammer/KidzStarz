package com.offlineprogrammer.kidzstarz.kid;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.offlineprogrammer.kidzstarz.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class KidViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "KidViewHolder";
    private TextView kidNameTextView;
    private ImageView kidMonsterImageView;
    private ImageButton happy_button;
    private ImageButton sad_button;
    OnKidListener onKidListener;
    private Context mContext;
    public KidViewHolder(@NonNull View itemView, OnKidListener onKidListener) {
        super(itemView);
        mContext = itemView.getContext();
        kidNameTextView = itemView.findViewById(R.id.kid_name);
        kidMonsterImageView = itemView.findViewById(R.id.kid_monster_name);
        happy_button = itemView.findViewById(R.id.happy_button);
        sad_button = itemView.findViewById(R.id.sad_button);
        this.onKidListener = onKidListener;
       // itemView.setOnClickListener(this);

        happy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onKidListener.showAddHappyStarzDialog(getAdapterPosition());
            }
        });

        sad_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onKidListener.showAddSadStarzDialog(getAdapterPosition());
            }
        });

    }



    public void bindData(final Kid viewModel) {
        kidNameTextView.setText(viewModel.getKidName());
        kidMonsterImageView.setImageResource( mContext.getResources().getIdentifier(viewModel.getMonsterImageResourceName() , "drawable" ,
                mContext.getPackageName()) );
    }

    @Override
    public void onClick(View v) {
        onKidListener.onKidClick(getAdapterPosition());
        onKidListener.showAddHappyStarzDialog(getAdapterPosition());
        onKidListener.showAddSadStarzDialog(getAdapterPosition());


    }
}

