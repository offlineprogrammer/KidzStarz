package com.offlineprogrammer.kidzstarz.starz;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineprogrammer.kidzstarz.Constants;
import com.offlineprogrammer.kidzstarz.GlideApp;
import com.offlineprogrammer.kidzstarz.R;

public class StarzViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    OnStarzListener onStarzListener;
    private TextView starz_desc_TextView;
    private TextView starz_datecreated_TextView;
    private TextView starz_count_TextView;
    private ImageView starz_type_ImageView;
    private Context mContext;

    public StarzViewHolder(@NonNull View itemView, OnStarzListener onStarzListener) {
        super(itemView);
        mContext = itemView.getContext();
        starz_desc_TextView = itemView.findViewById(R.id.starz_desc_TextView);
        starz_type_ImageView = itemView.findViewById(R.id.starz_type_ImageView);
        starz_datecreated_TextView = itemView.findViewById(R.id.starz_datecreated_TextView);
        starz_count_TextView = itemView.findViewById(R.id.starz_count_TextView);
        this.onStarzListener = onStarzListener;
        itemView.setOnClickListener(this);
    }

    public void bindData(final Starz viewModel) {
        String strDateFormat = "MMM dd, hh:mm a";
        starz_desc_TextView.setText(viewModel.getDesc());

        starz_datecreated_TextView.setText(DateFormat.format(strDateFormat, viewModel.getCreatedDate()));//.setText(viewModel.getCreatedDate().toString());
        if (viewModel.getType().equals(Constants.HAPPY)) {
            starz_count_TextView.setText(String.format("+ %d", viewModel.getCount()));
            starz_type_ImageView.setImageResource(R.drawable.happystar);
        }
        if (viewModel.getType().equals(Constants.SAD)) {
            starz_count_TextView.setText(String.format("- %d", viewModel.getCount()));
            starz_type_ImageView.setImageResource(R.drawable.sadstar);
        }
        if (viewModel.getType().equals(Constants.CLAIMED)) {
            starz_count_TextView.setText(String.format("- %d", viewModel.getCount()));

            if (viewModel.getFirestoreImageUri() == null) {
                starz_type_ImageView.setImageResource(R.drawable.lovestar);
            } else {

                GlideApp.with(mContext)
                        .load(Uri.parse(viewModel.getFirestoreImageUri()))
                        .placeholder(R.drawable.lovestar)
                        .into(starz_type_ImageView);
            }

        }

    }

    @Override
    public void onClick(View v) {
        onStarzListener.onStarzClick(getAdapterPosition());

    }
}


