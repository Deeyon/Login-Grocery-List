package com.example.grocery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class GroceryActivity extends AppCompatActivity {

    ListView g_list;
    ArrayList <String> items;
    ArrayAdapter<String> adapter;
    Button add;
    EditText addtext;

    FirebaseFirestore fStore;
    StorageReference storageReference;
    FirebaseAuth fAuth;
    DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grocery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //inside on create

        g_list = findViewById(R.id.grocery_list);
        add = findViewById(R.id.add_item);
        addtext=findViewById(R.id.add_item_text);

        fStore = FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        fAuth=FirebaseAuth.getInstance();


//        items = new ArrayList<>();

        documentReference = fStore.collection("g_list").document(fAuth.getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    items = (ArrayList <String>) documentSnapshot.get("items");
                }
                else{
                    items = new ArrayList<>();
                }
                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,items);
                g_list.setAdapter(adapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("retrieval failure","Failed to retrieve from firebase");
                Toast.makeText(GroceryActivity.this,"Failed to retrieve from firebase",Toast.LENGTH_SHORT).show();
                Intent gotomain = new Intent(GroceryActivity.this,MainActivity.class);
                startActivity(gotomain);
            }
        });




        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = addtext.getText().toString();
                if (!text.isEmpty()){
                    additem(text);
                    Toast.makeText(GroceryActivity.this,"Added "+text,Toast.LENGTH_SHORT).show();
                    uploadarrtofirebase(items);
                }
                else{
                    Toast.makeText(GroceryActivity.this, "Cannot add empty item.", Toast.LENGTH_SHORT).show();
                }

                addtext.setText("");
            }
        });

        g_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(GroceryActivity.this,"Removed " + items.get(i),Toast.LENGTH_SHORT).show();
                removeitem(i);
                uploadarrtofirebase(items);
                return false;
            }
        });






        //outside oncreate
    }

    private void uploadarrtofirebase(ArrayList <String> arr){
        // Set the array directly as the document content
        documentReference.set(Collections.singletonMap("items",arr)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("upload","arr successfully uploaded.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("failure","failure to upload.");
            }
        });

    }
//        documentReference.set(arr);


    private void removeitem(int i) {
        items.remove(i);
        g_list.setAdapter(adapter);
    }

    private void additem(String text) {
        items.add(text);
        g_list.setAdapter(adapter);
    }
}