package com.offlineprogrammer.kidzstarz.kid;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineprogrammer.kidzstarz.R;

import java.util.ArrayList;

public class KidAdapter extends RecyclerView.Adapter {
    private ArrayList<Kid> models = new ArrayList<>();
    private OnKidListener mOnKidListener;
    private static final String TAG = "KidAdapter";

    public KidAdapter(@NonNull final ArrayList<Kid> viewModels, OnKidListener onKidListener) {
        this.models.addAll(viewModels);
        this.mOnKidListener=onKidListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new KidViewHolder(view, mOnKidListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((KidViewHolder) holder).bindData(models.get(position));

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public ArrayList<Kid> getAllItems() {
        return models;
    }

    public void updateData(ArrayList<Kid> viewModels){
        models.clear();
        models.addAll(viewModels);
        notifyDataSetChanged();

    }

    public void delete(int position) {
        models.remove(position);
        notifyItemRemoved(position);
    }

    public void add(Kid item, int position){
        models.add(position, item);
        Log.i(TAG, "add: " + item.toString());
        notifyItemInserted(position);
        //notifyDataSetChanged();
        //notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.kid_itemview;
    }
}

