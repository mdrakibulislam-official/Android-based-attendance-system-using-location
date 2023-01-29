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

import com.example.classAttendance.Model.Teacher;
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

public class TeacherSingUpActivity extends AppCompatActivity {
    TextInputEditText regName;
    TextInputEditText regEmail;
    TextInputEditText regPassword;

    TextView loginHere;
    Button btnRegister;
    ProgressBar progressBar;

    FirebaseFirestore fireStore;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    Teacher teacher;
    String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_sing_up);
        getSupportActionBar().hide();

        regName = findViewById(R.id.regTeacherName);
        regEmail = findViewById(R.id.regTeacherEmail);
        regPassword = findViewById(R.id.regTeacherPass);
        loginHere = findViewById(R.id.txtTeacherLoginHere);
        btnRegister = findViewById(R.id.btnRegisterT);
        progressBar = findViewById(R.id.signUpProgress);

        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        btnRegister.setOnClickListener(view -> {
            createUser();
        });
        loginHere.setOnClickListener(view -> {
            startActivity(new Intent(TeacherSingUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void createUser() {
        String name = regName.getText().toString();
        String email = regEmail.getText().toString().trim();
        String password = regPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            regName.setError("Email cannot be empty");
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
                        map.put("isTeacher", "0");
                        df.set(map);
                        saveData();
                        Toast.makeText(TeacherSingUpActivity.this, "user registered successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(TeacherSingUpActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(TeacherSingUpActivity.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                private void saveData() {
                    FirebaseUser user = mAuth.getCurrentUser();


                    //  reference = FirebaseDatabase.getInstance().getReference("Teacher");
                    teacher = new Teacher(name, email, password);
                    teacherId = user.getUid();
                    reference.child("Teacher").child(teacherId).setValue(teacher);

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