package com.example.classAttendance.ViewModel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classAttendance.Model.CourseList;
import com.example.classAttendance.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private List<CourseList> list;
    private Context context;
    private RecyclerViewClickListener listener;
    DatabaseReference reference;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    public CourseAdapter(List<CourseList> list, Context context, RecyclerViewClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public CourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_items_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CourseList courseList = list.get(position);
        String course = courseList.getCourseName();
       // if (course.length() >28){
          //  holder.courseText.setText(course.substring(0,28).concat("..."));
       // }else {
            holder.courseText.setText(course);
      //  }


        holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, holder.buttonViewOption);
                popup.inflate(R.menu.course_option_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit:
                                showCourseDialog();
                                break;
                            case R.id.copy:
                                showCopyDialog();
                                break;
                            case R.id.delete:
                                deletedItem(position);
                                break;
                        }
                        return true;
                    }

                    private void showCopyDialog() {
                        Dialog dialog = new Dialog(view.getContext());

                        final AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                        View view = dialog.getLayoutInflater().inflate(R.layout.custom_dialog, null);

                        final EditText txt_courseName = (EditText) view.findViewById(R.id.txt_input);
                        final EditText txt_courseCode = (EditText) view.findViewById(R.id.txt_CourseCode);

                        String inputText = list.get(position).getCourseName();
                        txt_courseName.setText(inputText + "-1");
                        Button button_create = view.findViewById(R.id.btn_create);

                        alert.setView(view);
                        final AlertDialog alertDialog = alert.create();
                        alertDialog.setCanceledOnTouchOutside(true);

                        button_create.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                loadRecyclerViewItem();

                                alertDialog.dismiss();
                            }

                            private void loadRecyclerViewItem() {
                                String input = txt_courseName.getText().toString();
                                String code = txt_courseCode.getText().toString();
                                if (TextUtils.isEmpty(input)) {
                                    Toast.makeText(context.getApplicationContext(),"Course name cannot be empty",Toast.LENGTH_SHORT).show();
                                    txt_courseName.setError("Course name cannot be empty");
                                    txt_courseName.requestFocus();
                                } else if (TextUtils.isEmpty(code)){
                                    Toast.makeText(context.getApplicationContext(),"Course code cannot be empty",Toast.LENGTH_SHORT).show();
                                    txt_courseCode.setError("Course code cannot be empty");
                                    txt_courseCode.requestFocus();
                                } else {

                                    HashMap map = new HashMap();
                                    map.put("courseName", input);

                                    String userID = user.getEmail();
                                    reference = FirebaseDatabase.getInstance().getReference("Courses");

                                    CourseList courseList = new CourseList(input, code, userID);

                                    reference.child(code).setValue(courseList);
                                    list.add(courseList);
                                }
                            }
                        });

                        alertDialog.show();
                    }

                    private void showCourseDialog() {
                        Dialog dialog = new Dialog(view.getContext());

                        final AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                        View view = dialog.getLayoutInflater().inflate(R.layout.custom_dialog, null);

                        final EditText txt_courseName = (EditText) view.findViewById(R.id.txt_input);
                        final EditText txt_courseCode = (EditText) view.findViewById(R.id.txt_CourseCode);
                        txt_courseCode.setVisibility(View.INVISIBLE);
                        Button button_create = view.findViewById(R.id.btn_create);

                        alert.setView(view);
                        final AlertDialog alertDialog = alert.create();
                        alertDialog.setCanceledOnTouchOutside(false);

                        button_create.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                loadRecyclerViewItem();

                                alertDialog.dismiss();
                            }

                            private void loadRecyclerViewItem() {
                                String input = txt_courseName.getText().toString();
                                String code = txt_courseCode.getText().toString();
                                if (TextUtils.isEmpty(input)) {
                                    Toast.makeText(context.getApplicationContext(),"Course name cannot be empty",Toast.LENGTH_SHORT).show();
                                    txt_courseName.setError("Course name cannot be empty");
                                    txt_courseName.requestFocus();
                                } else{
                                    HashMap map = new HashMap();
                                    map.put("courseName", input);


                                    //user = mAuth.getCurrentUser();
                                    String userID = user.getEmail();
                                    reference = FirebaseDatabase.getInstance().getReference("Courses");
                                    String courseCode = list.get(position).getCourseCode();
                                    CourseList courseList = new CourseList(input, courseCode, userID);
                                    reference.child(courseCode).updateChildren(map);
                                    list.set(position, courseList);
                                    notifyItemChanged(position); 
                                }

                            }
                        });

                        alertDialog.show();

                    }


                    private void deletedItem(int position) {
                        //user = mAuth.getCurrentUser();
                        String userID = user.getUid();
                        reference = FirebaseDatabase.getInstance().getReference("Courses");
                        String key = list.get(position).getCourseCode();
                        reference.child(key).removeValue();
                        //list.remove(position);

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

    public interface RecyclerViewClickListener extends StudentCourseAdapter.RecyclerViewClickListener {
        void onClick(View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView courseText;
        public TextView buttonViewOption;
        public CardView cardView;


        public ViewHolder(View itemView) {
            super(itemView);

            courseText = itemView.findViewById(R.id.CourseTextView);
            buttonViewOption = itemView.findViewById(R.id.textViewOptions);
            cardView = itemView.findViewById(R.id.ItemCardView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }
    }
}