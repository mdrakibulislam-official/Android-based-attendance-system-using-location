package com.example.classAttendance.ViewModel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classAttendance.Model.StudentCourseList;
import com.example.classAttendance.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StudentCourseAdapter extends RecyclerView.Adapter<StudentCourseAdapter.ViewHolder> {
    private List<StudentCourseList> list;
    private Context context;
    private StudentCourseAdapter.RecyclerViewClickListener listener;
    DatabaseReference reference;
    DatabaseReference ref;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    public StudentCourseAdapter(List<StudentCourseList> list, Context context, StudentCourseAdapter.RecyclerViewClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public StudentCourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_course_item_list, parent, false);
        return new StudentCourseAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StudentCourseAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        StudentCourseList courseList = list.get(position);
        holder.courseText.setText(courseList.getCourse());
        holder.teacherName.setText("Teacher: " + courseList.getTeacherName());

        holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, holder.buttonViewOption);
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

                        String userID = user.getUid();

                        String roll = list.get(position).getExamRoll();
                        String courseCode = list.get(position).getCourseCode();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses").child(courseCode).child("Enroll").child(roll);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ref.removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //list.remove(position);
                        notifyDataSetChanged();
                        //notifyItemRemoved(position);
                        //notifyItemRangeChanged(position, list.size());

                    }

                });
                popup.show();
            }

        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface RecyclerViewClickListener {
        void onClick(View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView courseText;
        public TextView buttonViewOption;
        public TextView teacherName;
        public CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);

            courseText = itemView.findViewById(R.id.CourseName);
            teacherName = itemView.findViewById(R.id.teacherName);
            buttonViewOption = itemView.findViewById(R.id.studentItemOption);
            cardView = itemView.findViewById(R.id.studentItemCardView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }
    }
}
