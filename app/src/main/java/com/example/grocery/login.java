package com.example.grocery;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {





        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        EditText email = findViewById(R.id.R_Email);
        EditText pwd = findViewById(R.id.R_Pwd);
        Button login_btn = findViewById(R.id.login_button);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        TextView forgotpwd = findViewById(R.id.forgot_password);
//        View forgot_pwd = findViewById(R.id.forgot_pwd);


        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(login.this, MainActivity.class));

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_email = email.getText().toString().trim();
                String str_pwd = pwd.getText().toString().trim();

                if (TextUtils.isEmpty(str_email)) {
                    email.setError("You cannot have an empty email.");
                    return;
                }

                if (TextUtils.isEmpty(str_pwd)) {
                    pwd.setError("You cannot have an empty password.");
                    return;
                }


                TextView loading_text = findViewById(R.id.loading);
                loading_text.setText("Loading...");

                //register the user in firebase

                fAuth.signInWithEmailAndPassword(email.getText().toString(), pwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loading_text.setText("");
                            Toast.makeText(login.this, "Logged in successfully.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(login.this, MainActivity.class));
                            email.setText("");
                            pwd.setText("");
                        } else {
                            loading_text.setText("");
                            Toast.makeText(login.this, "Error - " + task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }
                });




            }


        });

        forgotpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText resetmail = new EditText(view.getContext());
                final AlertDialog.Builder pwd_dialog = new AlertDialog.Builder(view.getContext());
                pwd_dialog.setTitle("Forgot Password?");
                pwd_dialog.setMessage("Enter your email reset link: ");
                pwd_dialog.setView(resetmail);

                pwd_dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mail = resetmail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(login.this,"Email link sent",Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(login.this,"Error. Exception - "+ e.toString(),Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                pwd_dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Nothing
                    }
                });
                pwd_dialog.create().show();
            }
        });

    }
    public void gotoreg(View v) {
        Intent intent = new Intent(login.this, Register.class);
        startActivity(intent);

    } // you probably gotta change to this onclicklistener
}