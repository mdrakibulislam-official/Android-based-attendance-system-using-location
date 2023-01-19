package com.example.classAttendance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends FirebaseRecyclerAdapter<StudentModel, SearchAdapter.ViewHolder> {
    private List<StudentModel> list;
    private Context context;
    private ViewClickListener listener;

    public SearchAdapter(@NonNull FirebaseRecyclerOptions<StudentModel> options, List<StudentModel> list, Context context, ViewClickListener listener) {
        super(options);
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull StudentModel model) {
        holder.text_name.setText(model.getName());
        holder.text_examRoll.setText(model.getExamRoll());
        holder.check_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.checkImageClick(view, position);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item_list, parent, false);
        return new ViewHolder(view);
    }

    public interface ViewClickListener {
        void onClick(View v, int position);
        void onLongClick(View v, int position);
        void checkImageClick(View v, int position);
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView text_name, text_examRoll;
        private CircleImageView check_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text_name = itemView.findViewById(R.id.studentTextView);
            text_examRoll = (TextView) itemView.findViewById(R.id.txt_id);
            check_image = (CircleImageView) itemView.findViewById(R.id.attendanceCheck);
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
