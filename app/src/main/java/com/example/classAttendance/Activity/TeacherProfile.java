package com.example.classAttendance.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.classAttendance.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class TeacherProfile extends AppCompatActivity {
    TextView teacherProfileName, teacherProfileDate, teacherProfileEmail, totalCourse;
    CircleImageView imageTeacher, teacherCameraButton;
    ProgressDialog progressDialog;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    FirebaseUser user;
    Uri imageUri;
    final static int REQUEST_CODE=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);
        this.setTitle("Teacher Profile");

        progressDialog = new ProgressDialog(TeacherProfile.this);
        teacherProfileName = findViewById(R.id.teacherProfileName);
        teacherProfileDate = findViewById(R.id.teacherProfileDate);
        teacherProfileEmail = findViewById(R.id.teacherProfileEmail);
        totalCourse = findViewById(R.id.totalCourse);
        imageTeacher = findViewById(R.id.imageTeacher);
        teacherCameraButton = findViewById(R.id.teacherCameraButton);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        String userEmail = user.getEmail();
        reference = FirebaseDatabase.getInstance().getReference();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(calendar.getTime());

        teacherProfileDate.setText("  Date                     : "+date);
        teacherProfileEmail.setText("  Email                   : "+userEmail);
        progressDialog.setTitle("Loading....");
        reference.child("Teacher").orderByChild("email").equalTo(userEmail).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String name = (String) dataSnapshot.child("name").getValue();
                    teacherProfileName.setText(name);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        reference.child("Courses").orderByChild("userEmail").equalTo(userEmail).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numberOfCourse = 0;
                for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                    numberOfCourse++;
                }
                progressDialog.dismiss();
                totalCourse.setText("  Total Course      : "+numberOfCourse);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("image",MODE_PRIVATE);
        String path = sharedPreferences.getString("imgUri","");

        if (!path.equals("")){
            Uri imgUri = Uri.parse(path);
            Picasso.with(TeacherProfile.this).load(imgUri).into(imageTeacher);
        }

        teacherCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        imageUri = getImageUri(this,bitmap);

        SharedPreferences preferences = getSharedPreferences("image",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("imgUri", String.valueOf(imageUri));
        editor.apply();

        if(requestCode==REQUEST_CODE && resultCode==RESULT_OK && data!=null && imageUri!=null){

            Log.d("URI", String.valueOf(imageUri));
            Picasso.with(TeacherProfile.this).load(imageUri).into(imageTeacher);
        }



    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap,"Photo",null);
        return Uri.parse(path);
    }
}