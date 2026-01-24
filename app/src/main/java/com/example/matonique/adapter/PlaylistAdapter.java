package com.example.matonique.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matonique.R;
import com.example.matonique.model.Playlist;

import java.util.List;

// Adapter pour afficher les playlists dans un RecyclerView
// affiche le nom de la playlist et le nombre de musiques qu'elle contient
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final List<Playlist> playlists;
    private final OnPlaylistClickListener listener;

    // Interface pour gerer les clics sur une playlist
    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
        void onPlaylistLongClick(Playlist playlist); // pour suprimer une playlist
    }

    public PlaylistAdapter(List<Playlist> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);

        // afficher le nom de la playlist
        holder.txtName.setText(playlist.getName());

        // afficher le nombre de musiques dans la playlist
        int count = playlist.getMusicCount();
        String subtitle = count + (count > 1 ? " musiques" : " musique");
        holder.txtSubtitle.setText(subtitle);

        // icon de playlist (on reutilise l'icon de dossier)
        holder.imgIcon.setImageResource(R.drawable.folder_icon);

        // gerer le clic
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaylistClick(playlist);
            }
        });

        // gerer le long clic (pour suprimer)
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onPlaylistLongClick(playlist);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtName;
        TextView txtSubtitle;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_icon);
            txtName = itemView.findViewById(R.id.txt_name);
            txtSubtitle = itemView.findViewById(R.id.txt_subtitle);
        }
    }
}

