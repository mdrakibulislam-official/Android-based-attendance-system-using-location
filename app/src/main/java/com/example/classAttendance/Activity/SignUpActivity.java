package com.example.classAttendance.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.classAttendance.Model.Student;
import com.example.classAttendance.R;
import com.example.classAttendance.View.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    TextInputEditText regName;
    TextInputEditText regExamRoll;
    TextInputEditText regEmail;
    TextInputEditText regPassword;

    TextView loginHere;
    Button btnRegister;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore fireStore;
    DatabaseReference reference;
    Student student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        regName = findViewById(R.id.regName);
        regExamRoll = findViewById(R.id.regExamRoll);
        regEmail = findViewById(R.id.regEmail);
        regPassword = findViewById(R.id.regPass);
        loginHere = findViewById(R.id.txtLoginHere);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.signUpProgress);
        fireStore = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(view -> {
            createUser();
        });
        loginHere.setOnClickListener(view -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void createUser() {
        String name = regName.getText().toString();
        String examRoll = regExamRoll.getText().toString().trim();
        String email = regEmail.getText().toString().trim();
        String password = regPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            regName.setError("Name cannot be empty");
            regName.requestFocus();
        } else if (TextUtils.isEmpty(examRoll)) {
            regExamRoll.setError("Exam Roll cannot be empty");
            regName.requestFocus();
        } else if (TextUtils.isEmpty(email)) {
            regEmail.setError("Email cannot be empty");
            regEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            regPassword.setError("Password cannot be empty");
            regPassword.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid email address",Toast.LENGTH_SHORT).show();

        } else {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    finish();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        DocumentReference df = fireStore.collection("Users").document(user.getEmail());
                        Map<String, Object> map = new HashMap<>();
                        map.put("isStudent", "1");
                        df.set(map);

                        saveData();
                        Toast.makeText(SignUpActivity.this, "user registered successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                private void saveData() {
                    reference = FirebaseDatabase.getInstance().getReference("Student");

                    student = new Student(name, examRoll, email, password);

                    reference.child(examRoll).setValue(student);

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}