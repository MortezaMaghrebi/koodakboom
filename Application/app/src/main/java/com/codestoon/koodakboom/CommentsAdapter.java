package com.codestoon.koodakboom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<CommentModel> comments;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm · dd/MM", Locale.getDefault());

    public CommentsAdapter(List<CommentModel> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = comments.get(position);
        holder.tvUsername.setText(comment.getUsername());
        holder.tvComment.setText(comment.getComment());
        holder.tvTime.setText(dateFormat.format(comment.getTimestamp()));

        // حرف اول نام کاربری برای آواتار
        String firstChar = comment.getUsername().length() > 0 ?
                comment.getUsername().substring(0, 1).toUpperCase() : "U";
        holder.tvAvatar.setText(firstChar);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvComment, tvTime, tvAvatar;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvCommentUsername);
            tvComment = itemView.findViewById(R.id.tvCommentText);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            tvAvatar = itemView.findViewById(R.id.tvCommentAvatar);
        }
    }
}