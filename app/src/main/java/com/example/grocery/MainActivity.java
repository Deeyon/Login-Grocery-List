package com.example.grocery;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.KeyStore;

public class MainActivity extends AppCompatActivity {
    ImageView ProfileImage;
    StorageReference storageReference;
    FirebaseAuth fAuth;
    Button Gbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        TextView Name = findViewById(R.id.name);
        TextView Email = findViewById(R.id.email);
        TextView Pno = findViewById(R.id.pno);
        View logout = findViewById(R.id.log_out);
        View RESETPWD = findViewById(R.id.reset_pwd);
        ProfileImage = findViewById((R.id.profile));
        storageReference= FirebaseStorage.getInstance().getReference();
        Gbtn = findViewById(R.id.grocery_btn);


        String UserID = fAuth.getUid();
        FirebaseUser fuser = fAuth.getCurrentUser();


        StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(ProfileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Profile failed to upload. Error - " + e, Toast.LENGTH_LONG).show();
            }
        });

        if (!fuser.isEmailVerified()){
            TextView NotVerified = findViewById(R.id.not_veri);
            NotVerified.setVisibility(View.VISIBLE);

            Button NotVerifiedBtn = findViewById(R.id.veri_btn);
            NotVerifiedBtn.setVisibility(View.VISIBLE);

            NotVerifiedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //send email verification link

                    fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(MainActivity.this, "Email link sent", Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,"Error - Email verification link not sent. Exception - " + e.getMessage(),Toast.LENGTH_LONG).show();
                            Log.d("email verification link", "email verification link not sent - " + e.getMessage());
                        }
                    });
                }
            });

        }

        DocumentReference documentReference = fStore.collection("users").document(UserID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value!=null){
                    Name.setText("Name: " + value.getString("FName"));
                    Email.setText("Email: " + value.getString("FEmail"));
                    Pno.setText("Phone no.: " + value.getString("FPno"));
                }


            }
        });






        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FirebaseAuth fAuth = FirebaseAuth.getInstance();
                FirebaseAuth.getInstance().signOut();
                Log.d("signout","signout done");
                startActivity(new Intent(getApplicationContext(), login.class));
                finish();

            }
        });


        //reset password
        RESETPWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText resetpwd = new EditText(view.getContext());
                resetpwd.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                // the above line makes the password entered in the form of asterisks
                // type_class_text makes it a normal text input and it is combined using '|'
                // with TYPE_TEXT_VARIATION_PASSWORD which is for passwords.
                //This combination gives an asterisk password format, it has to be used together.
                final AlertDialog.Builder pwd_dialog = new AlertDialog.Builder(view.getContext());
                pwd_dialog.setTitle("Reset Password?");
                pwd_dialog.setMessage("Enter your new password: ");
                pwd_dialog.setView(resetpwd);

                pwd_dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mail = resetpwd.getText().toString();
                        fuser.updatePassword(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(MainActivity.this,"Password reset successful.",Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,"Error. Exception - "+e.getMessage(),Toast.LENGTH_LONG).show();
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

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent OpenGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(OpenGalleryIntent,1000);

            }
        });

        Gbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotogrocery = new Intent(MainActivity.this,GroceryActivity.class);
                startActivity(gotogrocery);

            }
        });







    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode==1000){
            if (resultCode== Activity.RESULT_OK){
                Uri ImageURI = data.getData();
                ProfileImage.setImageURI(ImageURI);
                uploadImageToFirebase(ImageURI);
            }
        }

    }

    private void uploadImageToFirebase(Uri ImageURI) {
        StorageReference fileRef=storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(ImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(ProfileImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Error in picasso - " + e, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Error - " + e, Toast.LENGTH_LONG).show();
                Log.d("upload","Error - "+ e);
            }
        });
    }


}