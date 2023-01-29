package com.example.classAttendance.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.classAttendance.Model.AttendanceStatus;
import com.example.classAttendance.Model.LocationTeacher;
import com.example.classAttendance.Model.Present;
import com.example.classAttendance.Model.StudentModel;
import com.example.classAttendance.R;
import com.example.classAttendance.ViewModel.SearchAdapter;
import com.example.classAttendance.ViewModel.StudentAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentActivity extends AppCompatActivity {

    private static final int PERMISSION_STORAGE_CODE = 1000;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    private RecyclerView recyclerView, filterData;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    StudentAdapter adapterModel;

    BottomNavigationView bottomNavigationView;


    public List<StudentModel> list;
    List<StudentModel>listFilter;
    private StudentAdapter.ViewClickListener listener;
    private CircleImageView takeAttendance;
    ProgressDialog progressDialog;
    CountDownTimer countDownTimer;

    RecyclerView getRecyclerView;
    SearchAdapter searchAdapter;
    SearchAdapter.ViewClickListener clickListener;

    DatabaseReference reference, ref;
    FirebaseAuth mAuth;
    FirebaseUser user;
    Context context;

    String lat, lon;
    String userID;
    String stu_key;
    String courseCode, c;
    String newDate = "";

    int year, month, day;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        setOnClickListener();
        recyclerView = findViewById(R.id.StudentRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //takeAttendance = findViewById(R.id.take_attendance);

        bottomNavigationView = findViewById(R.id.student_bottom_navigation);


        BottomMenu();

        getRecyclerView = findViewById(R.id.StudentRecyclerView);
        getRecyclerView.setHasFixedSize(true);
        getRecyclerView.setLayoutManager(new LinearLayoutManager(this));
       /* FirebaseRecyclerOptions<StudentModel> options =
                new FirebaseRecyclerOptions.Builder<StudentModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("Student"), StudentModel.class)
                        .build();*/
        //searchAdapter = new SearchAdapter(options, list, getApplicationContext(), clickListener);
        //getRecyclerView.setAdapter(searchAdapter);
        filterData = findViewById(R.id.StudentRecyclerView);


        list = new ArrayList<StudentModel>();
        listFilter = new ArrayList<StudentModel>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        Bundle bundle = getIntent().getExtras();
        c = bundle.getString("Course");
        courseCode = bundle.getString("key");
        Log.d("MMM", courseCode);
        this.setTitle(c);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        userID = user.getUid();
        reference = FirebaseDatabase.getInstance().getReference();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        showList();

        /*takeAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setTitle("Location tracing...");
                progressDialog.setCancelable(true);
                progressDialog.show();
                getCurrentLocation();

            }
        });*/

    }

    private void BottomMenu() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.filter:
                        showFilter();
                        break;
                    case R.id.attendance:
                        progressDialog.setTitle("Location tracing...");
                        progressDialog.setCancelable(true);
                        progressDialog.show();
                        getCurrentLocation();
                        break;
                    case R.id.mark:
                        showMarkAllDialog();

                        break;
                }
                return true;
            }
        });
    }



    private void showFilter() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(StudentActivity.this);
        View view = getLayoutInflater().inflate(R.layout.filter_data, null);

        Button button_allStudent = view.findViewById(R.id.all_student);
        Button button_present = view.findViewById(R.id.all_Present);
        Button button_absent = view.findViewById(R.id.all_Absent);

        alert.setView(view);
        final AlertDialog alertDialog = alert.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);

        button_allStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                showList();
                Toast.makeText(getApplicationContext(), "All student is here", Toast.LENGTH_SHORT).show();
            }
        });

        button_present.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newDate.equals("")) {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    newDate = dateFormat.format(calendar.getTime());
                }
                progressDialog.setTitle("Loading...");
                progressDialog.show();
                Log.d("MM", newDate);
                reference.child("Courses").child(courseCode).child("Attendance").child(newDate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.getChildrenCount() < 1) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "No present", Toast.LENGTH_SHORT).show();
                        } else {
                            listFilter.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String examRoll = dataSnapshot.getKey();

                                Log.d("MM", examRoll);

                                reference.child("Student").orderByChild("examRoll").equalTo(examRoll).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        alertDialog.dismiss();
                                        progressDialog.dismiss();
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            String name = (String) dataSnapshot.child("name").getValue();
                                            String examRoll = (String) dataSnapshot.child("examRoll").getValue();
                                            String email = (String) dataSnapshot.child("email").getValue();
                                            String password = (String) dataSnapshot.child("password").getValue();
                                            StudentModel model = new StudentModel(name, examRoll, email, password);
                                            listFilter.add(model);


                                        }


                                        adapterModel = new StudentAdapter(listFilter, getApplicationContext(), listener);
                                        filterData.setAdapter(adapterModel);


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        button_absent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newDate.equals("")) {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    newDate = dateFormat.format(calendar.getTime());
                }
                progressDialog.setTitle("Loading...");
                progressDialog.show();


                reference.child("Courses").child(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listFilter.clear();
                        for (DataSnapshot dataSnapshot : snapshot.child("Enroll").getChildren()) {
                            String examRoll = dataSnapshot.getKey();

                            int k = 0;
                            for (DataSnapshot dataSnapshot1 : snapshot.child("Attendance").child(newDate).getChildren()) {
                                String present = dataSnapshot1.getKey();
                                Log.d("MM", present);
                                if (examRoll.equals(present)) {
                                    k++;
                                }

                            }

                            if (k == 0) {
                                reference.child("Student").orderByChild("examRoll").equalTo(examRoll).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        alertDialog.dismiss();
                                        progressDialog.dismiss();
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            String name = (String) dataSnapshot.child("name").getValue();
                                            String examRoll = (String) dataSnapshot.child("examRoll").getValue();
                                            String email = (String) dataSnapshot.child("email").getValue();
                                            String password = (String) dataSnapshot.child("password").getValue();
                                            StudentModel model = new StudentModel(name, examRoll, email, password);

                                            listFilter.add(model);

                                        }

                                        adapterModel = new StudentAdapter(listFilter, getApplicationContext(), listener);
                                        filterData.setAdapter(adapterModel);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
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




    private void showList() {
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        Log.d("MMM", courseCode);
        reference.child("Courses").child(courseCode).child("Enroll").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getChildrenCount() < 1) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "No Student is enrolled", Toast.LENGTH_SHORT).show();
                } else {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Log.d("MMM", dataSnapshot.getValue().toString());
                        String examRoll = (String) dataSnapshot.child("examRoll").getValue();

                        Log.d("MMM", examRoll);

                        reference.child("Student").orderByChild("examRoll").equalTo(examRoll).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                progressDialog.dismiss();
                                Log.d("Check1", "examRoll");
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String name = (String) dataSnapshot.child("name").getValue();
                                    String examRoll = (String) dataSnapshot.child("examRoll").getValue();
                                    String email = (String) dataSnapshot.child("email").getValue();
                                    String password = (String) dataSnapshot.child("password").getValue();
                                    StudentModel model = new StudentModel(name, examRoll, email, password);
                                    list.add(model);


                                }
                                //list.remove(list.get(list.size() - 1));
                                //list.remove(list.size() - 1);

                                adapter = new StudentAdapter(list, getApplicationContext(), listener);
                                recyclerView.setAdapter(adapter);

                                adapterModel = new StudentAdapter(list, getApplicationContext(), listener);
                                filterData.setAdapter(adapterModel);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }

                //adapter = new StudentAdapter(list, getApplicationContext(), listener);
                //recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }




    private void getCurrentLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(StudentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(StudentActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(StudentActivity.this)
                                            .removeLocationUpdates(this);
                                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        lat = String.valueOf(latitude);
                                        lon = String.valueOf(longitude);
                                        if (lat != null && progressDialog.isShowing()) {
                                            LocationTeacher teacher = new LocationTeacher(lat, lon);
                                            ref = FirebaseDatabase.getInstance().getReference("Courses");
                                            ref.child(courseCode).child("Location").setValue(teacher);
                                            if (progressDialog.isShowing()) {
                                                showAlert();
                                            }

                                            progressDialog.dismiss();
                                        } else {
                                            progressDialog.dismiss();
                                        }

                                    }
                                }
                            }, Looper.getMainLooper());
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(StudentActivity.this, "Press again after turn on", Toast.LENGTH_SHORT).show();
                    turnOnGPS();
                }
            } else {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {

            progressDialog.dismiss();
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
                    Toast.makeText(StudentActivity.this, "GPS is already turned on", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {

                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(StudentActivity.this, 2);
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
        ;
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




    private void showAlert() {


        final AlertDialog.Builder alert = new AlertDialog.Builder(StudentActivity.this);
        View view = getLayoutInflater().inflate(R.layout.attendance_dialog, null);
        TextView textView1 = view.findViewById(R.id.counter);
        TextView text_distanceLimit = view.findViewById(R.id.distanceLimit);
        EditText textView = view.findViewById(R.id.inputCounter);
        EditText inputLimit = view.findViewById(R.id.inputDistance);
        TextView session = view.findViewById(R.id.sessionDuration);
        Button start = view.findViewById(R.id.startAttendanceSession);
        alert.setView(view);
        final AlertDialog alertDialog = alert.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String limit = inputLimit.getText().toString();

                String count = textView.getText().toString();

                int time = 0;
                if (!count.equals("")) {
                    time = Integer.parseInt(count) * 60;
                } else {
                    time = 180;
                }

                int lastLimit = 0;
                if (!limit.equals("")) {
                    lastLimit = Integer.parseInt(limit);
                } else {
                    lastLimit = 5;
                }

                if (time > 2400 || time < 0) {
                    Toast.makeText(getApplicationContext(), "Allow maximum time of 40 min", Toast.LENGTH_SHORT).show();
                } else if (lastLimit < 1) {
                    Toast.makeText(getApplicationContext(), "Allow minimum distance of 1 miter", Toast.LENGTH_SHORT).show();

                } else {


                    AttendanceStatus attendanceStatus = new AttendanceStatus("true", String.valueOf(lastLimit));

                    reference = FirebaseDatabase.getInstance().getReference("Courses");
                    reference.child(courseCode).child("AttendanceStatus").setValue(attendanceStatus);

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = dateFormat.format(calendar.getTime());

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses");
                    ref.child(courseCode).child("Attendance").setValue(date);


                    inputLimit.setVisibility(View.INVISIBLE);
                    textView.setVisibility(View.INVISIBLE);
                    textView1.setVisibility(View.VISIBLE);
                    start.setText("End Session");
                    Log.d("TIME", String.valueOf(time));
                    countDownTimer = new CountDownTimer(time * 1000, 1000) {
                        @Override
                        public void onTick(long l) {
                            textView1.setText(" " + l / 1000);

                        }

                        @Override
                        public void onFinish() {
                            attendanceStatus.setStatus("false");
                            reference.child(courseCode).child("AttendanceStatus").setValue(attendanceStatus);
                            text_distanceLimit.setVisibility(View.INVISIBLE);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses").child(courseCode).child("Attendance").child(date);
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int total = 0;
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        total++;
                                    }
                                    session.setText("Attendance Count:  ");
                                    textView1.setText("" + total);
                                    start.setText("Back");

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            start.setOnClickListener(view1 -> {
                                alertDialog.cancel();
                            });
                        }
                    };
                    countDownTimer.start();

                    start.setOnClickListener(view1 -> {
                        attendanceStatus.setStatus("false");
                        reference.child(courseCode).child("AttendanceStatus").setValue(attendanceStatus);
                        text_distanceLimit.setVisibility(View.INVISIBLE);

                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Courses").child(courseCode).child("Attendance").child(date);
                        ref1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int total = 0;
                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                    total++;
                                }
                                session.setText("Attendance Count:  ");
                                textView1.setText("" + total);
                                start.setText("Back");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        countDownTimer.cancel();
                        start.setOnClickListener(view2 -> {
                            alertDialog.cancel();

                        });
                    });
                }


            }
        });

        alertDialog.show();
    }




    private void setOnClickListener() {
        listener = new StudentAdapter.ViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                click(v, position);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onLongClick(View v, int position) {
                longClick(v, position);
            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date = dateFormat.format(calendar.getTime());

            Bundle bundle = getIntent().getExtras();
            String Course_key = bundle.getString("key");


            @Override
            public void checkImageClick(View v, int position) {
                String rollNumber = list.get(position).getExamRoll();
                String date1;
                if (newDate.equals("")) {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    date1 = dateFormat.format(calendar.getTime());
                } else {
                    date1 = newDate;
                }

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses").child(courseCode).child("Attendance").child(date1).child(rollNumber);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.getChildrenCount() != 0) {

                            CircleImageView imageView = v.findViewById(R.id.attendanceCheck);
                            imageView.setImageResource(R.drawable.red_check);

                            DatabaseReference Temp2 = FirebaseDatabase.getInstance().getReference("Courses").child(courseCode).child("Attendance").child(date1).child(rollNumber);
                            Temp2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Temp2.removeValue();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        } else {

                            CircleImageView imageView = v.findViewById(R.id.attendanceCheck);
                            imageView.setImageResource(R.drawable.green_check);
                            Present present = new Present("1");

                            DatabaseReference Temp2 = FirebaseDatabase.getInstance().getReference("Courses");
                            Temp2.child(courseCode).child("Attendance").child(date1).child(rollNumber).setValue(present);

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };

        clickListener = new SearchAdapter.ViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                click(v, position);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onLongClick(View v, int position) {
                longClick(v, position);
            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date = dateFormat.format(calendar.getTime());

            Bundle bundle = getIntent().getExtras();
            String Course_key = bundle.getString("key");


            @Override
            public void checkImageClick(View v, int position) {
                imageClick(v, position);
            }
        };
    }



    private void imageClick(View v, int position) {



    }




    private void click(View v, int position) {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        String studentName = list.get(position).getName();
        String examRoll = list.get(position).getExamRoll();
        String code = courseCode;
        intent.putExtra("Student", studentName);
        intent.putExtra("examRoll", examRoll);
        intent.putExtra("courseCode", code);
        startActivity(intent);
    }




    @RequiresApi(api = Build.VERSION_CODES.M)
    private void longClick(View v, int position) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.setGravity(Gravity.RIGHT);
        popup.inflate(R.menu.student_option_menu);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.deleteStudent:
                        deletedItem(position);
                        break;
                }
                return true;
            }


            private void deletedItem(int position) {

                String key = list.get(position).getExamRoll();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses");
                ref.child(courseCode).child("Enroll").child(key).removeValue();
                list.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, list.size());
            }
        });
        popup.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_menu, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //processSearch(s);
                return false;
            }


            @Override
            public boolean onQueryTextChange(String s) {

                adapterModel.getFilter().filter(s);
                // processSearch(s);
                return false;
            }

        });
        return super.onCreateOptionsMenu(menu);
    }




    private void processSearch(String s) {
        FirebaseRecyclerOptions<StudentModel> options =
                new FirebaseRecyclerOptions.Builder<StudentModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("Student").orderByChild("name").startAt(s).endAt(s + "\uf8ff"), StudentModel.class)
                        .build();
        searchAdapter = new SearchAdapter(options, list, getApplicationContext(), clickListener);
        searchAdapter.startListening();
        getRecyclerView.setAdapter(searchAdapter);
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.calender:
                getCalender(item);
                break;
            case R.id.search:

                break;
            case R.id.sort:
                sortStudentList();
                break;
            case R.id.markAll:
                showMarkAllDialog();
                break;
            case R.id.exportCSV:
                exportData();
                break;
        }
        return true;
    }




    private void getCalender(MenuItem item) {

        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(StudentActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, i);
                c.set(Calendar.MONTH, i1);
                c.set(Calendar.DAY_OF_MONTH, i2);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                newDate = dateFormat.format(c.getTime());

                Calendar calendar1 = Calendar.getInstance();
                String currentDate = dateFormat.format(calendar1.getTime());
                if (newDate.equals(currentDate)) {
                    item.setIcon(R.drawable.calender);
                } else {
                    item.setIcon(R.drawable.calender_red);
                }


                showList();
                SharedPreferences preferences = getSharedPreferences("newDate", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("date", newDate);
                editor.apply();

                Log.d("date", newDate);
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void sortStudentList() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(StudentActivity.this);
        View view = getLayoutInflater().inflate(R.layout.sort_student, null);

        Button button_Name = view.findViewById(R.id.sortByName);
        Button button_ExamRoll = view.findViewById(R.id.sortByExamRoll);

        alert.setView(view);
        final AlertDialog alertDialog = alert.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);

        button_Name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Collections.sort(list, StudentModel.sortByName);
                adapterModel.notifyDataSetChanged();
            }
        });

        button_ExamRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Collections.sort(list, StudentModel.sortByRoll);
                adapterModel.notifyDataSetChanged();
            }
        });

        alertDialog.show();
    }

    private void exportData() {
        progressDialog.setTitle("Downloading....");
        progressDialog.show();
        List<String> listRoll = new ArrayList<String>();
        List<String> date = new ArrayList<String>();
        date.clear();


        reference.child("Courses").child(courseCode).child("Enroll").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String r;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    r = dataSnapshot.getRef().getKey().toString();

                    listRoll.add(r);

                }
                reference.child("Roll").setValue(listRoll);
                Log.d("Response", String.valueOf(listRoll));
                StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbzlPTufVSgdPTEc1HSahkRYlsq43Tf6wt3jMFOIcTm0O8LTq9bQeCJzzNZ8n9norZ9ymQ/exec",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {


                                if (!response.equals("Success")) {
                                    progressDialog.dismiss();
                                    Toast.makeText(StudentActivity.this, "Attendance is not collected", Toast.LENGTH_LONG).show();
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                            requestPermissions(permissions, PERMISSION_STORAGE_CODE);
                                        } else {
                                            startDownload();
                                            Log.d("Response", response);
                                            Toast.makeText(StudentActivity.this, response, Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        startDownload();
                                        Log.d("Response", response);
                                        Toast.makeText(StudentActivity.this, response, Toast.LENGTH_LONG).show();
                                    }

                                }


                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> parmas = new HashMap<>();

                        //here we pass params
                        parmas.put("action", "addItem");
                        parmas.put("courseCode", courseCode);


                        return parmas;
                    }
                };

                int socketTimeOut = 50000;

                RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(retryPolicy);

                RequestQueue queue = Volley.newRequestQueue(StudentActivity.this);

                queue.add(stringRequest);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.child("Courses").child(courseCode).child("Attendance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String r;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    r = dataSnapshot.getRef().getKey().toString();

                    date.add(r);

                }
                reference.child("AttendanceDate").setValue(date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDownload();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT);
                }
            }

        }
    }

    private void startDownload() {
        String url = "https://docs.google.com/spreadsheets/d/1KFYnuOE-1mb3JgJFhmWmXOUX58dD9N0qGsHx8YJpuJY/export?format=xlsx";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle("Download");
        request.setDescription("Downloading file....");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Class Attendance(" + c + ").xlsx");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        progressDialog.dismiss();
    }


    private void showMarkAllDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(StudentActivity.this);
        View view = getLayoutInflater().inflate(R.layout.mark_all, null);

        Button button_MarkAll = view.findViewById(R.id.allPresent);
        Button button_Holiday = view.findViewById(R.id.holiday);

        alert.setView(view);
        final AlertDialog alertDialog = alert.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);

        button_MarkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (newDate.equals("")) {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    newDate = dateFormat.format(calendar.getTime());
                }
                progressDialog.show();
                for (StudentModel model : list) {
                    String roll = model.getExamRoll();
                    Present present = new Present("1");
                    Log.d("ATT", courseCode + " " + newDate + " " + roll);
                    reference = FirebaseDatabase.getInstance().getReference("Courses");
                    reference.child(courseCode).child("Attendance").child(newDate).child(roll).setValue(present);

                }
                Toast.makeText(getApplicationContext(), "Attendance taken done", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                progressDialog.dismiss();
            }
        });

        button_Holiday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Today is Holiday", Toast.LENGTH_SHORT).show();

                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences preferences = getSharedPreferences("newDate", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("date", "");
        editor.apply();
        super.onDestroy();

    }
}