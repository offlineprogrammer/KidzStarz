package com.offlineprogrammer.kidzstarz;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.offlineprogrammer.kidzstarz.kid.Kid;
import com.offlineprogrammer.kidzstarz.starz.Starz;
import com.offlineprogrammer.kidzstarz.user.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;
import timber.log.Timber;

public class FirebaseHelper {
   // User m_User;
    FirebaseFirestore m_db;
    FirebaseAuth firebaseAuth;
    FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "FirebaseHelper";
    Context mContext;
    KidzStarz kidzStarz;
    public static final String USERS_COLLECTION = "users";

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
            kidzStarz.getUser().setFcmInstanceId(FirebaseInstanceId.getInstance().getToken());


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

    Single<User> getUserData() {
        return Single.create(emitter -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            final DocumentReference docRef = m_db.collection(USERS_COLLECTION).document(currentUser.getUid());
            docRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Timber.tag(TAG).w(e, "Listen failed.");
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    Timber.d("V2.0 Current data: %s", snapshot.getData());
                    kidzStarz.setUser(snapshot.toObject(User.class));
                    emitter.onSuccess(kidzStarz.getUser());
                } else {
                    Timber.d("V2.0 Current data: null");
                    emitter.onError(new Exception("V2.0 o data found"));
                }
            });
        });
    }

    public void logEvent(String event_name) {
        mFirebaseAnalytics.logEvent(event_name, null);
    }

    public Observable<Kid> saveKid(Kid newKid) {
        return Observable.create((ObservableEmitter<Kid> emitter) -> {
            kidzStarz.getUser().getKidz().add(0, newKid);
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

    public Completable updateKidStarz(Starz createdStarz, int position) {
        return Completable.create(emitter -> {

            Kid selectedKid = kidzStarz.getUser().getKidz().get(position);

            int totalStarzCount = selectedKid.getTotalStarz();
            int newStarzCount = 0;
            if (createdStarz.getType().equals(Constants.HAPPY)) {
                totalStarzCount = totalStarzCount + createdStarz.getCount();
                newStarzCount = selectedKid.getHappyStarz() + createdStarz.getCount();
                selectedKid.setTotalStarz(totalStarzCount);
                selectedKid.setHappyStarz(newStarzCount);
            } else {
                totalStarzCount = totalStarzCount - createdStarz.getCount();
                newStarzCount = selectedKid.getSadStarz() + createdStarz.getCount();
                selectedKid.setTotalStarz(totalStarzCount);
                selectedKid.setSadStarz(newStarzCount);
            }

            kidzStarz.getUser().getKidz().set(position, selectedKid);

            DocumentReference newKidRef = m_db.collection("users").document(kidzStarz.getUser().getUserId());//.collection("kidz").document();
            newKidRef.update("kidz", kidzStarz.getUser().getKidz())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Add Kid", "DocumentSnapshot successfully written!");
                            emitter.onComplete();
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

    public Observable<ArrayList<Starz>> getkidStarz(Kid selectedKid) {

        ArrayList<Starz> starzsList = new ArrayList<>();
        return Observable.create((ObservableEmitter<ArrayList<Starz>> emitter) -> {


            DocumentReference selectedKidRef = m_db.collection("users").document(kidzStarz.getUser().getUserId())
                    .collection("kidzStarz").document(selectedKid.getKidUUID());
            selectedKidRef.collection("starz")
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.exists()) {
                                        Log.d("Got Task Data", document.getId() + " => " + document.getData());
                                        Starz starz = document.toObject(Starz.class);
                                        starzsList.add(starz);
                                    }
                                }
                                emitter.onNext(starzsList);
                            } else {
                                Log.d("Got Date", "Error getting documents: ", task.getException());
                                emitter.onError(task.getException());
                            }
                        }
                    });
        });
    }

    private Kid findKidByUUID(String sKidUUID) {

        return kidzStarz.getUser().getKidz().stream().filter(
                oKid -> sKidUUID.equals(oKid.getKidUUID())).findFirst().orElse(null);


    }

    public Completable updateSelectedKidStarz(Starz createdStarz, Kid sKid) {
        return Completable.create(emitter -> {
            int position = kidzStarz.getUser().getKidz().indexOf(findKidByUUID(sKid.getKidUUID()));
            Kid selectedKid = kidzStarz.getUser().getKidz().get(position);
            int totalStarzCount = selectedKid.getTotalStarz();
            int newStarzCount = 0;
            if (createdStarz.getType().equals(Constants.HAPPY)) {
                totalStarzCount = totalStarzCount + createdStarz.getCount();
                newStarzCount = selectedKid.getHappyStarz() + createdStarz.getCount();
                selectedKid.setTotalStarz(totalStarzCount);
                selectedKid.setHappyStarz(newStarzCount);
            } else {
                totalStarzCount = totalStarzCount - createdStarz.getCount();
                selectedKid.setTotalStarz(totalStarzCount);
                if (createdStarz.getType().equals(Constants.SAD)) {
                    newStarzCount = selectedKid.getSadStarz() + createdStarz.getCount();
                    selectedKid.setSadStarz(newStarzCount);
                }
                if (createdStarz.getType().equals(Constants.CLAIMED)) {
                    newStarzCount = selectedKid.getClaimedStarz() + createdStarz.getCount();
                    selectedKid.setClaimedStarz(newStarzCount);
                }
            }

            kidzStarz.getUser().getKidz().set(position, selectedKid);

            DocumentReference newKidRef = m_db.collection("users").document(kidzStarz.getUser().getUserId());//.collection("kidz").document();
            newKidRef.update("kidz", kidzStarz.getUser().getKidz())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Add Kid", "DocumentSnapshot successfully written!");
                            emitter.onComplete();
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

    public Observable<Uri> uploadImage(Kid selectedKid, Uri imagePath) {
        return Observable.create((ObservableEmitter<Uri> emitter) -> {

            FirebaseStorage storage;
            StorageReference storageReference;
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();

            final StorageReference ref
                    = storageReference
                    .child("images/" + selectedKid.getKidUUID() + "/"
                            + Calendar.getInstance().getTime().toString() + "/"
                            + UUID.randomUUID().toString());

            ref.putFile(imagePath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        //String downloadURL = downloadUri.toString();
                        emitter.onNext(downloadUri);

                    } else {
                        emitter.onError(task.getException());
                    }
                }
            });
        });

    }

    public Completable deleteKidStarzCollection(Kid selectedKid) {
        return Completable.create(emitter -> {
            DocumentReference selectedKidRef = m_db.collection("users").document(kidzStarz.getUser().getUserId())
                    .collection("kidzStarz").document(selectedKid.getKidUUID());
            selectedKidRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "DocumentSnapshot successfully deleted!");
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Error updating document", e);
                            emitter.onError(e);
                        }
                    });


        });
    }

    public Completable deleteKid(Kid selectedKid) {
        return Completable.create(emitter -> {
            kidzStarz.getUser().getKidz().remove(selectedKid);
            DocumentReference newKidRef = m_db.collection("users").document(kidzStarz.getUser().getUserId());//.collection("kidz").document();
            newKidRef.update("kidz", kidzStarz.getUser().getKidz())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "DocumentSnapshot successfully deleted!");

                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Error updating document", e);
                            emitter.onError(e);
                        }
                    });


        });
    }


    public Completable setUserFcmInstanceId() {
        return Completable.create(emitter -> {

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                emitter.onError(task.getException());
                            }
                            // Get new FCM registration token
                            String token = task.getResult();
                            kidzStarz.getUser().setFcmInstanceId(token);
                            DocumentReference newKidRef = m_db.collection(USERS_COLLECTION).document(kidzStarz.getUser().getUserId());//.collection("kidz").document();
                            newKidRef.update("fcmInstanceId", kidzStarz.getUser().getFcmInstanceId())
                                    .addOnSuccessListener(aVoid -> {
                                        Timber.d("fcmInstanceId successfully written!");
                                        emitter.onComplete();
                                    })
                                    .addOnFailureListener(e -> {
                                        Timber.tag("Add Kid").w(e, "Error writing document");
                                        emitter.onError(e);
                                    });

                        }
                    });




        });
    }



    public Completable updateUserFcmInstanceId(String token) {
        return Completable.create(emitter -> {
            kidzStarz.getUser().setFcmInstanceId(token);
            DocumentReference newKidRef = m_db.collection(USERS_COLLECTION).document(kidzStarz.getUser().getUserId());//.collection("kidz").document();
            newKidRef.update("fcmInstanceId", kidzStarz.getUser().getFcmInstanceId())
                    .addOnSuccessListener(aVoid -> {
                        Timber.d("fcmInstanceId successfully written!");
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        Timber.tag("Add Kid").w(e, "Error writing document");
                        emitter.onError(e);
                    });

        });
    }


}
