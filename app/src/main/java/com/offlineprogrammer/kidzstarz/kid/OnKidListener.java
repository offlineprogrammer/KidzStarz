package com.offlineprogrammer.kidzstarz.kid;

public interface OnKidListener {

    void onKidClick(int position);

    void showAddHappyStarzDialog(int position);

    void showAddSadStarzDialog(int adapterPosition);
}
