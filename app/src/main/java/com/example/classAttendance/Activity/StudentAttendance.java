package com.example.classAttendance.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classAttendance.Model.StudentCourseList;
import com.example.classAttendance.R;
import com.example.classAttendance.View.LoginActivity;
import com.example.classAttendance.ViewModel.CourseAdapter;
import com.example.classAttendance.ViewModel.StudentCourseAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentAttendance extends AppCompatActivity {


    ProgressDialog progressDialog;
    TextView welcomeText;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private CourseAdapter.RecyclerViewClickListener listener;
    public List<StudentCourseList> list;

    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference spinnerRef, reference;
    ArrayList<String> spinnerList;
    ArrayAdapter<String> arrayAdapter;


    String userEmail;
    String teacherName;
    String examRoll;
    String course, courseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);
        this.setTitle("StudentActivity");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(StudentAttendance.this, LoginActivity.class));
            finish();
        } else {
            progressDialog = new ProgressDialog(StudentAttendance.this);
            progressDialog.setTitle("Loading...");
            setOnClickListener();
            welcomeText = findViewById(R.id.welcome);
            recyclerView = findViewById(R.id.enrollRecyclerView);
            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));


            list = new ArrayList<StudentCourseList>();
            spinnerRef = FirebaseDatabase.getInstance().getReference();
            //   reference = FirebaseDatabase.getInstance().getReference();
            spinnerList = new ArrayList<>();


            arrayAdapter = new ArrayAdapter<String>(StudentAttendance.this, android.R.layout.simple_spinner_dropdown_item, spinnerList);


            showEnrollList();
        }

    }

    private void showEnrollList() {
        progressDialog.show();
        try {
            String userStudentEmail = user.getEmail();
            spinnerRef.child("Student").orderByChild("email").equalTo(userStudentEmail)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot item : snapshot.getChildren()) {
                                examRoll = item.child("examRoll").getValue().toString();


                                spinnerRef.child("Courses").orderByValue().addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        list.clear();

                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            String CourseId = dataSnapshot.child("courseCode").getValue().toString();
                                            Log.d("TAG", CourseId);

                                            spinnerRef.child("Courses").child(CourseId).child("Enroll").orderByChild("examRoll").equalTo(examRoll)
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                                            if (snapshot1.getChildrenCount()==0){
                                                                welcomeText.setVisibility(View.VISIBLE);
                                                                welcomeText.setText("Enroll Courses by using \nmenu, and assign course title");
                                                            }
                                                            progressDialog.dismiss();
                                                            for (DataSnapshot dataSnapshot1 : snapshot1.getChildren()) {

                                                                String coureName = (String) dataSnapshot1.child("course").getValue();

                                                                String teacherName = (String) dataSnapshot1.child("teacherName").getValue();
                                                                String roll = (String) dataSnapshot1.child("examRoll").getValue();
                                                                String code = (String) dataSnapshot1.child("courseCode").getValue();
                                                                Log.d("TAG", coureName + roll + teacherName);
                                                                StudentCourseList courseList = new StudentCourseList(coureName, teacherName, roll, code);
                                                                list.add(courseList);
                                                            }
                                                            if (list.size() != 0) {
                                                                welcomeText.setVisibility(View.INVISIBLE);
                                                            }
                                                            adapter = new StudentCourseAdapter(list, getApplicationContext(), listener);
                                                            recyclerView.setAdapter(adapter);

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "OnCancelled is Called", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

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

                StudentCourseList courseList;
                Intent intent = new Intent(getApplicationContext(), Attendance.class);
                String courseName = list.get(position).getCourse();
                String teacher = list.get(position).getTeacherName();
                String code = list.get(position).getCourseCode();
                intent.putExtra("Course", courseName);
                intent.putExtra("Teacher", teacher);
                intent.putExtra("courseCode", code);
                intent.putExtra("examRoll", examRoll);
                startActivity(intent);

            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_student_menu, menu);
        MenuCompat.setGroupDividerEnabled(menu,true);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enrollCourse:
                showSpinnerDialog();
                break;
            case R.id.logoutStudent:
                SharedPreferences sharedPreferences = getSharedPreferences("timing",MODE_PRIVATE);
                String value = sharedPreferences.getString("time","");

                if (value.equals("")){
                    Toast.makeText(StudentAttendance.this, "SignOut is Successfully", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                    finish();
                    //Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    //startActivity(intent);
                } else{
                    long oldDate = Long.parseLong(value);

                    Date currentDate = new Date();

                    long diff = currentDate.getTime() - oldDate;
                    long seconds = diff / 1000;
                    long minutes = seconds / 60;


                    if (minutes>40){
                        Toast.makeText(StudentAttendance.this, "SignOut is Successfully", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        finish();
                        //Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        //startActivity(intent);
                    }else {
                        Toast.makeText(StudentAttendance.this,"Please try after "+(40-minutes)+" minutes", Toast.LENGTH_SHORT).show();
                    }
                }


                break;
        }
        return true;
    }

    private void showSpinnerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StudentAttendance.this);
        View view = getLayoutInflater().inflate(R.layout.spinner_dialog, null);
        builder.setTitle("Course Enrollment");
        Spinner spinner = view.findViewById(R.id.spinner);

        spinnerRef.child("Courses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                spinnerList.clear();
                spinnerList.add("Choose a course: ");
                for (DataSnapshot item : snapshot.getChildren()) {
                    spinnerList.add(item.child("courseName").getValue().toString());
                }

                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        spinner.setAdapter(arrayAdapter);


        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (spinner.getSelectedItem().toString().equalsIgnoreCase("Choose a course: ")) {
                    Toast.makeText(getApplicationContext(), spinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                } else {
                    course = spinner.getSelectedItem().toString();

                    progressDialog.setTitle("Uploading....");
                    progressDialog.show();
                    spinnerRef.child("Courses").orderByChild("courseName").equalTo(course).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot item : snapshot.getChildren()) {
                                userEmail = item.child("userEmail").getValue().toString();
                                courseCode = item.child("courseCode").getValue().toString();


                                spinnerRef.child("Teacher").orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot item : snapshot.getChildren()) {
                                            teacherName = item.child("name").getValue().toString();
                                        }


                                        String userStudentEmail = user.getEmail();
                                        spinnerRef.child("Student").orderByChild("email").equalTo(userStudentEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                for (DataSnapshot item : snapshot.getChildren()) {
                                                    examRoll = item.child("examRoll").getValue().toString();
                                                }

                                                if (!course.isEmpty() && !examRoll.isEmpty()) {
                                                    StudentCourseList courseList = new StudentCourseList(course, teacherName, examRoll, courseCode);
                                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses");
                                                    ref.child(courseCode).child("Enroll").child(examRoll).setValue(courseList);

                                                    progressDialog.dismiss();


                                                    //adapter = new StudentCourseAdapter(list, getApplicationContext(), listener);
                                                   // recyclerView.setAdapter(adapter);

                                                }


                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                    Toast.makeText(getApplicationContext(), "Course enrollment Successfully", Toast.LENGTH_SHORT).show();
                }

            }


        });

        builder.setView(view);
        AlertDialog dialog = builder.show();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this)
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