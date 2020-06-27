package com.offlineprogrammer.kidzstarz.kid;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineprogrammer.kidzstarz.R;

public class KidViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "KidViewHolder";
    private TextView kidNameTextView;
    private ImageView kidMonsterImageView;
    private ImageButton happy_button;
    private ImageButton sad_button;
    private TextView sad_starz;
    private TextView happy_starz;
    private TextView total_starz_text;
    private ImageButton moreinfo_button;

    OnKidListener onKidListener;
    private Context mContext;

    public KidViewHolder(@NonNull View itemView, OnKidListener onKidListener) {
        super(itemView);
        mContext = itemView.getContext();
        // kidNameTextView = itemView.findViewById(R.id.kid_name);
        kidMonsterImageView = itemView.findViewById(R.id.kid_monster_name);
        happy_button = itemView.findViewById(R.id.happy_button);
        sad_button = itemView.findViewById(R.id.sad_button);
        moreinfo_button = itemView.findViewById(R.id.moreinfo_button);

        sad_starz = itemView.findViewById(R.id.sad_starz);
        happy_starz = itemView.findViewById(R.id.happy_starz);
        total_starz_text = itemView.findViewById(R.id.total_starz_text);

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

        moreinfo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onKidListener.showMoreInfo(getAdapterPosition());
            }
        });

    }



    public void bindData(final Kid viewModel) {
        happy_starz.setText(String.format("+ %d", viewModel.getHappyStarz()));
        sad_starz.setText(String.format("- %d", viewModel.getSadStarz()));
        total_starz_text.setText(String.format("%d", viewModel.getTotalStarz()));


        kidMonsterImageView.setImageResource(mContext.getResources().getIdentifier(viewModel.getMonsterImageResourceName(), "drawable",
                mContext.getPackageName()));
    }

    @Override
    public void onClick(View v) {
        onKidListener.onKidClick(getAdapterPosition());
        onKidListener.showAddHappyStarzDialog(getAdapterPosition());
        onKidListener.showAddSadStarzDialog(getAdapterPosition());


    }
}

