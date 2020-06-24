package com.offlineprogrammer.kidzstarz;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.offlineprogrammer.kidzstarz.kid.Kid;
import com.offlineprogrammer.kidzstarz.starz.Starz;
import com.offlineprogrammer.kidzstarz.user.User;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class FirebaseHelper {
   // User m_User;
    FirebaseFirestore m_db;
    FirebaseAuth firebaseAuth;
    FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "FirebaseHelper";
    Context mContext;
    KidzStarz kidzStarz;

    public FirebaseHelper(Context c){
        m_db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mContext = c;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);

         kidzStarz = (KidzStarz) mContext;
    }

    Observable<User> saveUser() {
        return Observable.create((ObservableEmitter<User> emitter) -> {
            Date currentTime = Calendar.getInstance().getTime();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();

           kidzStarz.setUser(new User(currentUser.getUid(),currentUser.getEmail(), currentTime));

            Map<String, Object> user = kidzStarz.getUser().toMap();

            m_db.collection("users").document(currentUser.getUid())
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
//                            listenToUserDocument();
                            emitter.onNext(kidzStarz.getUser());
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




    Observable<User> getUserData() {
        return Observable.create((ObservableEmitter<User> emitter) -> {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        final DocumentReference docRef = m_db.collection("users").document(currentUser.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    kidzStarz.setUser(snapshot.toObject(User.class));
                    Log.d(TAG, "Current User: " + kidzStarz.getUser());
                    Log.d(TAG, "onEvent: User Kidz " +  kidzStarz.getUser().getKidz());
                    emitter.onNext(kidzStarz.getUser());

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
        });
    }


    public void logEvent(String event_name) {
        mFirebaseAnalytics.logEvent(event_name, null);
    }

    public Observable<Kid> saveKid(Kid newKid) {
        return Observable.create((ObservableEmitter<Kid> emitter) -> {
            kidzStarz.getUser().getKidz().add(newKid);
            DocumentReference newKidRef = m_db.collection("users").document(kidzStarz.getUser().getUserId());//.collection("kidz").document();
            newKidRef.update("kidz",kidzStarz.getUser().getKidz())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Add Kid", "DocumentSnapshot successfully written!");
                            emitter.onNext(newKid);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Add Kid", "Error writing document", e);
                            emitter.onError(e);
                        }
                    });

        });
    }

    public Completable updateKidzCollection(Kid newKid) {
        return Completable.create( emitter -> {
            Map<String, Object> kidValues = newKid.toMap();
            m_db.collection("users").document(kidzStarz.getUser().getUserId()).collection("kidzStarz").document(newKid.getKidUUID())
                    .set(kidValues)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
//                            listenToUserDocument();
                            emitter.onComplete();
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

    public Observable<Starz> saveStarz(Starz starz) {
        return Observable.create((ObservableEmitter<Starz> emitter) -> {
            DocumentReference newStarzRef = m_db.collection("users").document(kidzStarz.getUser().getUserId())
                    .collection("kidzStarz").document(starz.getKidUUID())
                    .collection("starz").document();
            newStarzRef.set(starz)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: DocumentSnapshot successfully written!");
                            emitter.onNext(starz);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Error writing document " + e.getMessage());
                            emitter.onError(e);
                        }
                    });

        });


    }
}
