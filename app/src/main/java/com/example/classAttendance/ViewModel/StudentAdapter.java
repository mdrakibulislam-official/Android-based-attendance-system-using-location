package com.example.classAttendance.ViewModel;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.classAttendance.Model.StudentModel;
import com.example.classAttendance.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> implements Filterable {
    private List<StudentModel> list;
    private Context context;
    private ViewClickListener listener;
    List<StudentModel> backup;


    public StudentAdapter(List<StudentModel> list, Context context, ViewClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
        backup = new ArrayList<>(list);
    }


    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_item_list, parent, false);
        return new StudentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StudentAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        StudentModel studentModel = list.get(position);
        String name = list.get(position).getName();
        String examRoll = list.get(position).getExamRoll();

        holder.text_name.setText(name);
        holder.text_examRoll.setText(studentModel.getExamRoll());
        holder.check_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.checkImageClick(view, position);
            }
        });
        String date ;

        SharedPreferences preferences = context.getSharedPreferences("newDate", MODE_PRIVATE);
        String path = preferences.getString("date", "");

        if (path.equals("")){
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = dateFormat.format(calendar.getTime());
            SharedPreferences sharedPreferences = context.getSharedPreferences("Code", MODE_PRIVATE);
            String courseCode = sharedPreferences.getString("courseCode", "");


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Courses");
            reference.child(courseCode).child("Attendance").child(date).child(examRoll).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull @NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        if (snapshot.getChildrenCount() !=0){
                            holder.check_image.setImageResource(R.drawable.green_check);
                        }
                        // if (dataSnapshot.getValue().toString().equals("1")) {
                        //   holder.check_image.setImageResource(R.drawable.green_check);
                        //  }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else{
            date = path;
            SharedPreferences sharedPreferences = context.getSharedPreferences("Code", MODE_PRIVATE);
            String courseCode = sharedPreferences.getString("courseCode", "");


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Courses");
            reference.child(courseCode).child("Attendance").child(date).child(examRoll).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull @NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        if (snapshot.getChildrenCount() !=0){
                            holder.check_image.setImageResource(R.drawable.green_check);
                        }
                        // if (dataSnapshot.getValue().toString().equals("1")) {
                        //   holder.check_image.setImageResource(R.drawable.green_check);
                        //  }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }




    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<StudentModel> filteredData = new ArrayList<>();
            if (charSequence.toString().isEmpty()) {
                filteredData.addAll(backup);
            } else {
                for (StudentModel model : backup) {
                    if (model.getName().toString().toLowerCase().contains(charSequence.toString().toLowerCase())
                            || model.getExamRoll().contains(charSequence)) {
                        filteredData.add(model);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredData;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            list.clear();
            list.addAll((ArrayList<StudentModel>)filterResults.values);
            notifyDataSetChanged();
        }
    };


    public interface ViewClickListener {
        void onClick(View v, int position);

        void onLongClick(View v, int position);

        void checkImageClick(View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView text_name, text_examRoll;
        private de.hdodenhof.circleimageview.CircleImageView check_image;

        public ViewHolder(View itemView) {
            super(itemView);
            text_name = itemView.findViewById(R.id.studentTextView);
            text_examRoll = itemView.findViewById(R.id.txt_id);
            check_image = itemView.findViewById(R.id.attendanceCheck);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }


        @Override
        public void onClick(View view) {

            listener.onClick(view, getAdapterPosition());
        }


        @Override
        public boolean onLongClick(View view) {
            listener.onLongClick(view, getAdapterPosition());

            return true;
        }

    }
}
