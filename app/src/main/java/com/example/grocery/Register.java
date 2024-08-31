package com.example.grocery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Register extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        EditText name= findViewById(R.id.R_Name);
        EditText email= findViewById(R.id.R_Email);
        EditText pwd= findViewById(R.id.R_Pwd);
        EditText pno= findViewById(R.id.R_Pno);
        Button register_btn = findViewById(R.id.R_Btn);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        if (fAuth.getCurrentUser()!=null){
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        }


        register_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String str_email = email.getText().toString().trim();
                String str_pwd = pwd.getText().toString().trim();
                String str_name = name.getText().toString();
                String str_pno = pno.getText().toString().trim();


                if (TextUtils.isEmpty(str_email)){
                    email.setError("You cannot have an empty email.");
                    return;
                }

                if (TextUtils.isEmpty(str_pwd)){
                    pwd.setError("You cannot have an empty password.");
                    return;
                }

                if (str_pwd.length()<6){
                    pwd.setError("Your password must be at least 6 characters long.");
                    return;
                }

                TextView loading_text= findViewById(R.id.loading);
                loading_text.setText("Loading...");

                //register the user in firebase

                fAuth.createUserWithEmailAndPassword(str_email,str_pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            //send email verification link

                            FirebaseUser fuser=fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(Register.this, "Email link sent", Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Register.this,"Error - Email verification link not sent. Exception - " + e.getMessage(),Toast.LENGTH_LONG).show();
                                    Log.d("email verification link", "email verification link not sent - " + e.getMessage());
                                }
                            });





                            Toast.makeText(Register.this,"User successfully created.",Toast.LENGTH_LONG).show();
                            String UserID= fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference= fStore.collection("users").document(UserID);
                            HashMap<String,Object> user = new HashMap<>();
                            user.put("FName",str_name);
                            user.put("FEmail",str_email);
                            user.put("FPno",str_pno);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("store","user name, email, pno successfully stored!");
                                }
                            });
                            startActivity(new Intent(Register.this, MainActivity.class));
                            loading_text.setText("Done!");
                        }
                        else{
                            Toast.makeText(Register.this,"User creation failed - " + task.getException().toString(),Toast.LENGTH_LONG).show();
                            loading_text.setText("Failed!");
                        }
                    }
                });






            }
            
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });





    }





    public void gotolog(View v){
        Intent intent = new Intent(Register.this,login.class);
        startActivity(intent);

    }




    }

