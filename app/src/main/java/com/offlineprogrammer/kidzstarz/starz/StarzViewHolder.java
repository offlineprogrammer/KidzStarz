package com.offlineprogrammer.kidzstarz.starz;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineprogrammer.kidzstarz.Constants;
import com.offlineprogrammer.kidzstarz.R;

public class StarzViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    OnStarzListener onStarzListener;
    private TextView starz_desc_TextView;
    private ImageView starz_type_ImageView;
    private Context mContext;

    public StarzViewHolder(@NonNull View itemView, OnStarzListener onStarzListener) {
        super(itemView);
        mContext = itemView.getContext();
        starz_desc_TextView = itemView.findViewById(R.id.starz_desc_TextView);
        starz_type_ImageView = itemView.findViewById(R.id.starz_type_ImageView);
        this.onStarzListener = onStarzListener;
        itemView.setOnClickListener(this);
    }

    public void bindData(final Starz viewModel) {
        starz_desc_TextView.setText(viewModel.getDesc());
        if (viewModel.getType().equals(Constants.HAPPY)) {
            starz_type_ImageView.setImageResource(R.drawable.happystar);
        }
        if (viewModel.getType().equals(Constants.SAD)) {
            starz_type_ImageView.setImageResource(R.drawable.sadstar);
        }

    }

    @Override
    public void onClick(View v) {
        onStarzListener.onStarzClick(getAdapterPosition());

    }
}


