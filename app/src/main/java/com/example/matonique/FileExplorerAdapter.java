package com.example.matonique;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matonique.model.FileItem;

import java.util.ArrayList;
import java.util.List;

// Adapter pour afficher les fichiers et dossiers dans un RecyclerView
public class FileExplorerAdapter extends RecyclerView.Adapter<FileExplorerAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(FileItem item);
    }

    private List<FileItem> items;
    private OnItemClickListener listener;

    public FileExplorerAdapter(List<FileItem> items, OnItemClickListener listener) {
        this.items = items != null ? items : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_explorer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem item = items.get(position);
        holder.txtName.setText(item.getName());
        holder.imgIcon.setImageResource(
                item.isDirectory() ? R.drawable.folder_icon : R.drawable.music_file_icon
        );
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_icon);
            txtName = itemView.findViewById(R.id.txt_name);
        }
    }
}
