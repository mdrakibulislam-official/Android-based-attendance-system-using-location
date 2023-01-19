package com.example.classAttendance;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {
    TextView welcomeText;
    ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private CourseAdapter.RecyclerViewClickListener listener;
    public List<CourseList> list;
    public List<String> listKey;

    BottomNavigationView bottomNavigationView;

    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference reference;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(TeacherActivity.this, LoginActivity.class));
            finish();
        } else {
            this.setTitle("Class Attendance");
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);

            setOnClickListener();

            recyclerView = findViewById(R.id.CourseRecyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            welcomeText = findViewById(R.id.welcomeText);
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            BottomMenu();
            list = new ArrayList<CourseList>();
            listKey = new ArrayList<String>();

            userEmail = user.getEmail().toString();
            reference = FirebaseDatabase.getInstance().getReference("Courses");
            showEvent();

        }

    }



    private void showEvent() {
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        try {
            reference.orderByChild("userEmail").equalTo(userEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    progressDialog.dismiss();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CourseList courseList = dataSnapshot.getValue(CourseList.class);
                        list.add(courseList);
                    }
                    if (list.size() != 0) {
                        welcomeText.setVisibility(View.INVISIBLE);
                    }

                    adapter = new CourseAdapter(list, getApplicationContext(), listener);
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "OnCancelled is Called", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "On" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setOnClickListener() {
        listener = new CourseAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                CourseList courseList;
                Intent intent = new Intent(getApplicationContext(), StudentActivity.class);
                String courseName = list.get(position).getCourseName();
                String courseCode = list.get(position).getCourseCode();
                intent.putExtra("Course", courseName);
                intent.putExtra("key", courseCode);
                startActivity(intent);
                SharedPreferences preferences = getSharedPreferences("Code",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("courseCode", courseCode);
                editor.apply();

            }
        };


    }

    private void BottomMenu() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.teacherProfile:
                        showProfile();
                        break;
                    case R.id.createCourse:
                        addCourse();
                        break;
                    case R.id.logoutTeacher:
                        Toast.makeText(TeacherActivity.this, "SignOut is successful", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        finish();


                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teacher_menu, menu);
        MenuCompat.setGroupDividerEnabled(menu,true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.profile:
                showProfile();
                break;
            case R.id.addCourse:
                addCourse();
                break;
            case R.id.logOut:
                Toast.makeText(TeacherActivity.this, "SignOut is successful", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();

                break;
        }
        return true;
    }


    private void showProfile() {
        Intent intent = new Intent(TeacherActivity.this,TeacherProfile.class);
        startActivity(intent);
    }

    private void addCourse() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(TeacherActivity.this);
        View view = getLayoutInflater().inflate(R.layout.custom_dialog, null);

        final TextInputEditText txt_inputText = view.findViewById(R.id.txt_input);
        final TextInputEditText txt_courseCode = view.findViewById(R.id.txt_CourseCode);

        Button button_create = view.findViewById(R.id.btn_create);

        alert.setView(view);
        final AlertDialog alertDialog = alert.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);

        button_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                loadRecyclerViewItem();

                alertDialog.dismiss();
            }

            private void loadRecyclerViewItem() {
                user = mAuth.getCurrentUser();

                reference = FirebaseDatabase.getInstance().getReference("Courses");
                String courseName = txt_inputText.getText().toString();
                String courseCode = txt_courseCode.getText().toString();
                String userEmail = user.getEmail();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Courses");
                databaseReference.child(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount()!=0){
                            Toast.makeText(getApplicationContext(),"Course is exist",Toast.LENGTH_SHORT).show();
                        }else{
                            if (!courseName.isEmpty() && !courseCode.isEmpty()) {
                                progressDialog.setTitle("Uploading....");
                                progressDialog.show();

                                //String key = reference.push().getKey();
                                CourseList courseList = new CourseList(courseName, courseCode, userEmail);

                                reference.child(courseCode).setValue(courseList);
                                progressDialog.dismiss();

                                list.add(courseList);
                            }

                            adapter = new CourseAdapter(list, getApplicationContext(), listener);
                            recyclerView.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }
        });

        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user == null) {
            startActivity(new Intent(TeacherActivity.this, LoginActivity.class));
            finish();
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
