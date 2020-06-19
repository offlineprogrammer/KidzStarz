package com.offlineprogrammer.kidzstarz.kid;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineprogrammer.kidzstarz.R;

public class KidViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView kidNameTextView;
    private ImageView kidMonsterImageView;
    OnKidListener onKidListener;
    private Context mContext;
    public KidViewHolder(@NonNull View itemView, OnKidListener onKidListener) {
        super(itemView);
        mContext = itemView.getContext();
        kidNameTextView = itemView.findViewById(R.id.kid_name);
        kidMonsterImageView = itemView.findViewById(R.id.kid_monster_name);
        this.onKidListener = onKidListener;
        itemView.setOnClickListener(this);
    }

    public void bindData(final Kid viewModel) {
        kidNameTextView.setText(viewModel.getKidName());
        kidMonsterImageView.setImageResource( mContext.getResources().getIdentifier(viewModel.getMonsterImageResourceName() , "drawable" ,
                mContext.getPackageName()) );
    }

    @Override
    public void onClick(View v) {
        onKidListener.onKidClick(getAdapterPosition());

    }
}

