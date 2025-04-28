package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    EditText mEmail, mPhone, mName, mPassword, mRePassword;
    Button mSignupBtn;
    TextView mLoginRedirect;
    FirebaseAuth fAuth;
    Toast Progress;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mName = findViewById(R.id.signup_name);
        mPhone = findViewById(R.id.signup_phone);
        mEmail = findViewById(R.id.signup_email);
        mPassword = findViewById(R.id.signup_password);
        mRePassword = findViewById(R.id.signup_repassword);
        mSignupBtn = findViewById(R.id.signup_btn);
        mLoginRedirect = findViewById(R.id.logingRedirect);
        fAuth = FirebaseAuth.getInstance();
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mRePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

       /*if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        }*/
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String phone = mPhone.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String rePassword = mRePassword.getText().toString().trim();

                if (name.isEmpty()) {
                    mName.setError("Name is required.");
                    return;
                }
                if (email.isEmpty()) {
                    mEmail.setError("Email is required.");
                    return;
                }
                if (phone.isEmpty()) {
                    mPhone.setError("Phone is required.");
                    return;
                }
                if (phone.length() != 10) {
                    mPhone.setError("Phone Must be 10 Characters.");
                    return;
                }
                if (password.isEmpty()) {
                    mEmail.setError("Password is required.");
                    return;
                }
                if (!password.equals(rePassword)) {
                    mRePassword.setError("Passwords do not match.");
                    return;
                }
                if (password.length() < 6) {
                    mPassword.setError("Password Must be More Than 6 Characters.");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "User Created.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            String userId = fAuth.getCurrentUser().getUid();
                            Map<String,Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("phone", phone);
                            user.put("email", email);
                            db.collection("users").document(userId).set(user).addOnSuccessListener(aVoid -> {
                                startActivity(new Intent(SignupActivity.this,MainActivity.class));
                                finish();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(SignupActivity.this,"Faild to create user profile." + e.getMessage(),Toast.LENGTH_SHORT).show();
                            });


                        } else {
                            Toast.makeText(SignupActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });



            }
        });


    }

    public void setLoginRedirect(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
