package com.example.arduino.utilities;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.arduino.menu.MenuActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class HttpHelper {
    private String view;
    public HttpHelper() {
    }

    public void HttpRequestForLooby(String req, String url){
        OkHttpClient client = new OkHttpClient();
        String newReq = url+"?token="+req;
        Request request = new Request.Builder().url(newReq).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
            }
        });
    }

    /**
     * NOT-WORKING -- NEED TO fIX
     * @param collection
     * @param doucment
     * @param field
     * @return
     */

    public String geFieldDocumentFromFirestore(String collection, final String doucment, final String field){
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = fstore.collection(collection).document(doucment);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                if(documentSnapshot.getString(field) == null){
                    Log.v("HTTP-CLASS","NULL-PROBLEM!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                }
                else {
                    Log.v("HTTP-CLASS","NO-PROBLEM!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    view = documentSnapshot.getString("waitTojoin");
                }
            }
        });
        if (view == null){
            Log.v("HTTP-CLASS","BIG-PROBLEM!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        return view;
    }
}
