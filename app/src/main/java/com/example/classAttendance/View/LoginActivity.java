package com.example.classAttendance.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.classAttendance.Activity.MainActivity;
import com.example.classAttendance.R;
import com.example.classAttendance.Activity.SignUpActivity;
import com.example.classAttendance.Activity.StudentAttendance;
import com.example.classAttendance.Activity.TeacherActivity;
import com.example.classAttendance.Activity.TeacherSingUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText loginEmail;
    TextInputEditText loginPassword;
    TextView registerHere;
    Button btnLogin;
    ProgressBar progressBar;
    SwitchCompat switchCompat;

    FirebaseAuth mAuth;
    FirebaseFirestore fireStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        switchCompat = findViewById(R.id.switchCheck);
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPass);
        registerHere = findViewById(R.id.txtRegisterHere);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.loginProgress);


        switchCompat.setOnClickListener(view -> {
            if (switchCompat.isChecked()) {
                switchCompat.setText("Teacher");
            } else {
                switchCompat.setText("Student");
            }
        });


        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> {
            mAuth.fetchSignInMethodsForEmail(loginEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            boolean check = !task.getResult().getSignInMethods().isEmpty();

                            if (!check) {
                                Toast.makeText(getApplicationContext(), "Email not Registered", Toast.LENGTH_SHORT).show();
                            } else {
                                loginUser();
                            }
                        }
                    });

        });
        registerHere.setOnClickListener(view -> {
            if (switchCompat.isChecked()) {
                startActivity(new Intent(LoginActivity.this, TeacherSingUpActivity.class));
                finish();
            } else {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });
    }

    private void loginUser() {
        String email = loginEmail.getText().toString();
        String password = loginPassword.getText().toString();
        if (TextUtils.isEmpty(email)) {
            loginEmail.setError("Email cannot be empty");
            loginEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            loginPassword.setError("Password cannot be empty");
            loginPassword.requestFocus();
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid email address",Toast.LENGTH_SHORT).show();

        } else{
            progressBar.setVisibility(View.VISIBLE);
            if (switchCompat.isChecked()) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        finish();
                        if (task.isSuccessful()) {
                            //checkUser();
                            Toast.makeText(LoginActivity.this, "Teacher Login is successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                });


            } else {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        finish();
                        if (task.isSuccessful()) {

                            Toast.makeText(LoginActivity.this, "Student Login is successfully", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                            //checkUser();

                        } else {
                            Toast.makeText(LoginActivity.this, "Student Login Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                            finish();
                            Log.d("Main", task.getException().getMessage());
                        }
                    }
                });


            }

        }
    }

    private void checkUser() {
        //FirebaseUser user = mAuth.getCurrentUser();
        String userEmail = loginEmail.getText().toString();

        DocumentReference df = fireStore.collection("Users").document(userEmail);
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("isStudent") != null) {
                    startActivity(new Intent(LoginActivity.this, StudentAttendance.class));
                    finish();
                } else if (documentSnapshot.getString("isTeacher") != null) {
                    startActivity(new Intent(LoginActivity.this, TeacherActivity.class));
                    finish();
                } else {
                    if (switchCompat.isChecked()) {
                        Toast.makeText(getApplicationContext(), "You are not a Teacher", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "You are not a Student", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}