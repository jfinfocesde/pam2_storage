package com.example.configfb.data.dao;


import android.net.Uri;
import android.util.Log;


import androidx.annotation.NonNull;

import com.example.configfb.data.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO (Data Access Object) para la colección de usuarios en Firebase Firestore.
 * Esta clase proporciona métodos para interactuar con la colección "users", incluyendo:
 * - Insertar un nuevo usuario.
 * - Actualizar los datos de un usuario existente.
 * - Actualizar la imagen de un usuario existente.
 * - Obtener un usuario por su ID.
 * - Obtener todos los usuarios de la colección.
 * - Eliminar un usuario de la colección.
 */
public class UserDao {

    private static final String TAG = "UserDao";
    private static final String COLLECTION_NAME = "users";

    private final FirebaseFirestore db;

    /**
     * Constructor de la clase UserDao.
     *
     * @param db Instancia de FirebaseFirestore para acceder a la base de datos.
     */
    public UserDao(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Inserta un nuevo usuario en la colección "users".
     *
     * @param user      El objeto User a insertar.
     * @param listener Listener para notificar el resultado de la operación.
     */
    public void insert(User user, OnSuccessListener<String> listener) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("image", "");

        db.collection(COLLECTION_NAME)
                .add(userData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "onSuccess: " + documentReference.getId());
                    listener.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: ", e);
                    listener.onSuccess(null);
                });
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param id       El ID del usuario a actualizar.
     * @param user      El objeto User con los datos actualizados.
     * @param listener Listener para notificar el resultado de la operación.
     */
    public void update(String id, User user, OnSuccessListener<Boolean> listener) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());

        db.collection(COLLECTION_NAME)
                .document(id)
                .update(userData)
                .addOnSuccessListener(unused -> listener.onSuccess(true))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: ", e);
                    listener.onSuccess(false);
                });
    }

    /**
     * Actualiza la imagen de un usuario existente.
     *
     * @param id       El ID del usuario a actualizar.
     * @param uri   La nueva imagen del usuario.
     * @param listener Listener para notificar el resultado de la operación.
     */
    public void updateImage(String id, Uri uri, OnSuccessListener<Boolean> listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a unique filename for the upload
        String filename = "image_" + id + ".png";
        StorageReference imageRef = storageRef.child("images/" + filename);

        // Upload the image to Firebase Storage
        UploadTask uploadTask = imageRef.putFile(uri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the download URL for the uploaded image
            imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String downloadUrl = downloadUri.toString();
                // Update the user document in Firestore with the download URL
                updateImageUrl(id, downloadUrl, listener);
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Error getting download URL", exception);
                listener.onSuccess(false);
            });
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Upload failed: " + exception.getMessage());
            listener.onSuccess(false);
        });
    }

    /**
     * Updates the image URL of a user inFirestore.
     *
     * @param id The ID of the user to update.
     * @param downloadUrl The download URL of the new image.
     * @param listener A listener to be notified when the update operation is complete.
     *                 The listener's `onSuccess` method will be called with`true` if the update
     *                 was successful, or `false` if there was an error.
     */
    private void updateImageUrl(String id, String downloadUrl, OnSuccessListener<Boolean> listener) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("image", downloadUrl); // Store the download URL in the "image" field

        db.collection(COLLECTION_NAME)
                .document(id)
                .update(userData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Image URL successfully updated in Firestore");
                    listener.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating image URL in Firestore", e);
                    listener.onSuccess(false);
                });
    }


    /**
     * Obtiene un usuario por su ID.
     *
     * @param id       El ID del usuario a obtener.
     * @param listener Listener para notificar el resultado de la operación.
     */
    public void getById(String id, OnSuccessListener<User> listener) {
        db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            listener.onSuccess(user);
                        } else {
                            listener.onSuccess(null);
                        }
                    } else {
                        Log.e(TAG, "onComplete: ", task.getException());
                        listener.onSuccess(null);
                    }
                });
    }

    /**
     * Obtiene todos los usuarios de la colección.
     *
     * @param listener Listener para notificar el resultado de la operación.
     */


    public void getAll(OnSuccessListener<List<User>> listener) {
        db.collection(COLLECTION_NAME).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<User> userList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        User user = documentSnapshot.toObject(User.class);
                        userList.add(user);
                    }
                    listener.onSuccess(userList);
                } else {
                    listener.onSuccess(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ", e);
                listener.onSuccess(null);
            }
        });
    }



    /**
     * Elimina un usuario de la colección por su ID.
     *
     * @param id       El ID del usuario a eliminar.
     * @param listener Listener para notificar el resultado de la operación.
     */
    public void delete(String id, OnSuccessListener<Boolean> listener) {
        db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess(true))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: ", e);
                    listener.onSuccess(false);
                });
    }

}