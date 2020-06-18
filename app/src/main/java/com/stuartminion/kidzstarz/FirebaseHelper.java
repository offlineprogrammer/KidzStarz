package com.stuartminion.kidzstarz;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.stuartminion.kidzstarz.kid.Kid;
import com.stuartminion.kidzstarz.user.User;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class FirebaseHelper {
    User m_User;
    FirebaseFirestore m_db;
    FirebaseAuth firebaseAuth;
    FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "FirebaseHelper";
    Context mContext;

    public FirebaseHelper(Context c){
        m_db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mContext = c;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
    }

    Observable<User> saveUser() {
        return Observable.create((ObservableEmitter<User> emitter) -> {
            Date currentTime = Calendar.getInstance().getTime();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();

            m_User = new User(currentUser.getUid(),currentUser.getEmail(), currentTime);

            Map<String, Object> user = m_User.toMap();

            m_db.collection("users").document(currentUser.getUid())
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            emitter.onNext(m_User);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                            emitter.onError(e);
                        }
                    });
        });
    }





    public void logEvent(String event_name) {
        mFirebaseAnalytics.logEvent(event_name, null);
    }
}
