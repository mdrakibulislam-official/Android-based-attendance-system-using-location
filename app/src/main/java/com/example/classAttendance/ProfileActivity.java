package com.example.classAttendance;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    TextView stu_name, details_info;
    ProgressDialog progressDialog;
    CircleImageView imageButton;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.setTitle("Profile");

        stu_name = findViewById(R.id.name);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading....");
        details_info = findViewById(R.id.detailInfo);
        calendar = Calendar.getInstance();
        imageButton = findViewById(R.id.imageButton);
        imageButton.setVisibility(View.INVISIBLE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(calendar.getTime());

        Bundle bundle = getIntent().getExtras();
        String name = bundle.getString("Student");
        String id = bundle.getString("examRoll");
        String courseCode = bundle.getString("courseCode");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Courses");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses");
        progressDialog.show();


        reference.child(courseCode).child("Attendance").orderByValue().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                int totalClass = 0;
                int attendance=0;
                for (DataSnapshot dataSnapshot1: snapshot.getChildren()){
                    totalClass++;
                }

                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String date = dataSnapshot.getRef().getKey().toString();
                    for (DataSnapshot dataSnapshot2: snapshot.child(date).getChildren()){
                        String rol = dataSnapshot2.getKey();
                        if (rol.equals(id)){
                            attendance++;
                        }
                    }


                }
                Log.d("PPPP", String.valueOf(attendance));
                Log.d("PPPP", String.valueOf(totalClass));

                double total = totalClass;
                double att= attendance;
                double percentage = Math.round((att/total) *100);
                Log.d("PPPP", String.valueOf(percentage));
                details_info.setText(date + "\n" + id + "\n" + totalClass + "\n" + attendance + "\n" + percentage + "%");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        stu_name.setText(name);


    }
}