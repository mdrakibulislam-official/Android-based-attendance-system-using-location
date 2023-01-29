package com.example.classAttendance.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.classAttendance.Model.Present;
import com.example.classAttendance.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Attendance extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    ProgressDialog progressDialog;

    TextView textCourse, textTeacher;
    Button AttendanceButton;
    String lat, lon;

    String courseCode, examRoll;
    String status;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        textCourse = findViewById(R.id.studentEnrollCourse);
        textTeacher = findViewById(R.id.studentEnrollCourseTeacher);
        AttendanceButton = findViewById(R.id.studentTakeAttendance);

        progressDialog = new ProgressDialog(Attendance.this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        reference = FirebaseDatabase.getInstance().getReference("Courses");

        Bundle bundle = getIntent().getExtras();
        String courseName = bundle.getString("Course");
        String teacherName = bundle.getString("Teacher");
        courseCode = bundle.getString("courseCode");
        examRoll = bundle.getString("examRoll");
        textCourse.setText("Course name   :  " + courseName +'\n'+ "Teacher name  :  " + teacherName);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Courses");
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses");
        progressDialog.setTitle("Loading...");
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
                        if (rol.equals(examRoll)){
                            attendance++;
                        }
                    }


                }
                Log.d("PPPP", String.valueOf(attendance));
                Log.d("PPPP", String.valueOf(totalClass));

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date1 = dateFormat.format(cal.getTime());

                double total = totalClass;
                double att= attendance;
                double percentage = Math.round((att/total) *100);
                Log.d("PPPP", String.valueOf(percentage));
                textTeacher.setText(":  "+ date1 + "\n" + ":  "+ examRoll + "\n" + ":  "+ totalClass + "\n" + ":  "+ attendance + "\n" + ":  "+ percentage + "%");
                //textTeacher.setText("Date              :  "+date1 + "\n" +"Exam Roll     :  "+ examRoll + "\n" +"Total Class  :  "+ totalClass + "\n" + "Attendance  :  " + attendance + "\n" +"Percentage  :  "+ percentage + "%");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        AttendanceButton.setOnClickListener(view -> {
            Date currentDate = new Date();


            SharedPreferences preferences = getSharedPreferences("timing",MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("time", String.valueOf(currentDate.getTime()));
            editor.apply();

            reference.child(courseCode).child("AttendanceStatus").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.getChildrenCount()<1){
                        Toast.makeText(getApplicationContext(), "Attendance session is OFF", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            status = dataSnapshot.getValue().toString();
                            Log.d("ST", status);
                        }
                        if (status.equals("true")) {
                            progressDialog.setTitle("Location tracing...");

                            /*Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String date = dateFormat.format(calendar.getTime());
                            Present present = new Present("1");
                            Log.d("ATT", courseCode + " " + date + " " + examRoll);
                            reference.child(courseCode).child("Attendance").child(date).child(examRoll).setValue(present);
                            Toast.makeText(getApplicationContext(), "Attendance taken done", Toast.LENGTH_SHORT).show();*/
                            getCurrentLocation();

                        } else {
                            Toast.makeText(getApplicationContext(), "Attendance session is OFF", Toast.LENGTH_SHORT).show();
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });


        });

    }

    private void takeAttendance() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses");

        ref.child(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("DDD", snapshot.getValue().toString());

                String latitude = (String) snapshot.child("Location").child("latitude").getValue();
                String longitude = (String) snapshot.child("Location").child("longitude").getValue();
                String range = (String) snapshot.child("AttendanceStatus").child("limit").getValue();
                Log.d("DDD", latitude+ " " + longitude+ " " + range);

                double latS = Double.parseDouble(lat);
                double longS = Double.parseDouble(lon);
                double latT = Double.parseDouble(latitude);
                double longT = Double.parseDouble(longitude);



                double longDiff = longT - longS;

                double distance = Math.sin(deg2rad(latT))
                        * Math.sin(deg2rad(latS))
                        + Math.cos(deg2rad(latT))
                        * Math.cos(deg2rad(latS))
                        * Math.cos(deg2rad(longDiff));
                distance = Math.acos(distance);
                distance = rad2deg(distance);
                distance = distance * 60 * 1.1515;
                distance = distance * 1.609344;

                Log.d("DDD", lat + " "+lon);
                Log.d("DDD", String.valueOf(distance));
                double limit = Double.parseDouble(range);
                if (distance <= limit) {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = dateFormat.format(calendar.getTime());
                    Present present = new Present("1");

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Courses").child(courseCode).child("Attendance").child(date);
                    databaseReference.child(examRoll).setValue(present);
                    Toast.makeText(getApplicationContext(), "Attendance taken done", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Out of class area", Toast.LENGTH_SHORT).show();
                }

            }

            private double rad2deg(double distance) {
                return (distance * 180.0 / Math.PI);
            }

            private double deg2rad(double latT) {
                return (latT * Math.PI / 180.0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCurrentLocation() {
        progressDialog.show();
        progressDialog.setCancelable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(Attendance.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(Attendance.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(Attendance.this)
                                            .removeLocationUpdates(this);
                                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        lat = String.valueOf(latitude);
                                        lon = String.valueOf(longitude);
                                        if (lat != null) {
                                            takeAttendance();
                                            progressDialog.dismiss();
                                        }

                                    }
                                }
                            }, Looper.getMainLooper());
                } else {
                    progressDialog.dismiss();
                    turnOnGPS();
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(Attendance.this, "GPS is already turned on", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(Attendance.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }
}