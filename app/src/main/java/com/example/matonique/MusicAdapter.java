package com.example.matonique;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matonique.model.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    public interface OnMusicClickListener {
        void onMusicClick(Music music);
    }

    private List<Music> musicList;
    private OnMusicClickListener listener;

    public MusicAdapter(List<Music> musicList, OnMusicClickListener listener) {
        // NullPointerException si la liste est init à null
        this.musicList = musicList != null ? musicList : new ArrayList<>();
        this.listener = listener;
    }


    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Music music = musicList.get(position);

        holder.txtTitle.setText(music.getTitle());
        holder.txtArtist.setText(music.getArtist());

        // Utiliser getCoverOrLoad() pour recharger la cover si elle est null (cas du Parcelable)
        Bitmap cover = music.getCoverOrLoad();
        holder.imgCover.setImageBitmap(
                cover != null ? cover :
                        BitmapFactory.decodeResource(
                                holder.itemView.getResources(),
                                R.drawable.music_placeholder
                        )
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMusicClick(music);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCover;
        TextView txtTitle, txtArtist;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.img_cover);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtArtist = itemView.findViewById(R.id.txt_artist);
        }
    }
}
