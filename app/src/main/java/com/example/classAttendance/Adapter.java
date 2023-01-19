package com.example.classAttendance;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.annotations.NonNull;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable {
    private List<StudentModel> list;
    List<StudentModel> backup;
    private Context context;
    private ViewClickListener listener;


    public Adapter(List<StudentModel> list, Context context, ViewClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
        backup = new ArrayList<>(list);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        StudentModel studentModel = list.get(position);
        String examRoll = list.get(position).getExamRoll();
        holder.text_name.setText(studentModel.getName());
        holder.text_examRoll.setText(studentModel.getExamRoll());

        holder.check_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.checkImageClick(view, position);
            }
        });

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(calendar.getTime());

        SharedPreferences sharedPreferences = context.getSharedPreferences("Code",MODE_PRIVATE);
        String courseCode = sharedPreferences.getString("courseCode","");


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Courses");
        reference.child(courseCode).child("Attendance").child(date).child(examRoll).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.getValue().toString().equals("1")){
                        holder.check_image.setImageResource(R.drawable.green_check);
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }
    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<StudentModel> filteredData = new ArrayList<>();

            if (charSequence.toString().isEmpty()){
                filteredData.addAll(backup);
            } else {
                for (StudentModel model : backup) {
                    if (model.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
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
            list.addAll((List< StudentModel>) filterResults.values);
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
        private CircleImageView check_image;

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