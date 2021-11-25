package com.gtmf.duinomote;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class RemoteAdapter extends RecyclerView.Adapter<RemoteAdapter.RemoteHolder> {
    private List<Remote> remotes = new ArrayList<>();
    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;

    @NonNull
    @Override
    public RemoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.remotelist_item, parent, false);
        return new RemoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RemoteHolder holder, int position) {
        Remote currentRemote = remotes.get(position);
        holder.text.setText(currentRemote.getName());
        holder.image.setImageResource(R.drawable.ic_tv_48dp);
    }

    @Override
    public int getItemCount() {
        return remotes.size();
    }

    public Remote getRemoteAt(int position) {
        return remotes.get(position);
    }

    public void setRemotes(List<Remote> remotes) {
        this.remotes = remotes;
        notifyDataSetChanged();
    }

    class RemoteHolder extends RecyclerView.ViewHolder {
        ImageView image;
        MaterialTextView text;

        public RemoteHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.remotelist_item_image);
            text = itemView.findViewById(R.id.remotelist_item_text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (mClickListener != null && position != RecyclerView.NO_POSITION) {
                        mClickListener.onItemClick(remotes.get(position));
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (mLongClickListener != null && position != RecyclerView.NO_POSITION) {
                        mLongClickListener.onItemLongClick(remotes.get(position));
                    }
                    return true;
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Remote remote);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(Remote remote);
    }

    public boolean setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mLongClickListener = listener;
        return true;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mClickListener = listener;
    }
}