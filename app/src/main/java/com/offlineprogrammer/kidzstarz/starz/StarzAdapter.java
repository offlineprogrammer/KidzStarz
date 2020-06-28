package com.offlineprogrammer.kidzstarz.starz;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineprogrammer.kidzstarz.R;

import java.util.ArrayList;

public class StarzAdapter extends RecyclerView.Adapter {
    private static final String TAG = "StarzAdapter";
    private ArrayList<Starz> models = new ArrayList<>();
    private OnStarzListener mOnStarzListener;

    public StarzAdapter(@NonNull final ArrayList<Starz> viewModels, OnStarzListener onStarzListener) {
        this.models.addAll(viewModels);
        this.mOnStarzListener = onStarzListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new StarzViewHolder(view, mOnStarzListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((StarzViewHolder) holder).bindData(models.get(position));

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public ArrayList<Starz> getAllItems() {
        return models;
    }

    public void updateData(ArrayList<Starz> viewModels) {
        models.clear();
        models.addAll(viewModels);
        notifyDataSetChanged();

    }

    public void delete(int position) {
        models.remove(position);
        notifyItemRemoved(position);
    }

    public void add(Starz item, int position) {
        models.add(position, item);
        Log.i(TAG, "add: " + item.toString());
        notifyItemInserted(position);
        //notifyDataSetChanged();
        //notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.starz_itemview;
    }
}