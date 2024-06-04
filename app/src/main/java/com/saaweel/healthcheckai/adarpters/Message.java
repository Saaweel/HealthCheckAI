package com.saaweel.healthcheckai.adarpters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.saaweel.healthcheckai.R;
import com.squareup.picasso.Picasso;

public class Message extends RecyclerView.ViewHolder {
    ImageView avatar;
    TextView username;
    TextView message;
    public Message(View view) {
        super(view);

        avatar = view.findViewById(R.id.avatar);
        username = view.findViewById(R.id.username);
        message = view.findViewById(R.id.message);
    }

    public void setData(String username, String avatarUrl, String message) {
        this.username.setText(username);

        if (!avatarUrl.isEmpty()) {
            Picasso.get().load(avatarUrl).into(this.avatar);
        } else {
            this.avatar.setImageResource(R.mipmap.ic_launcher_round);
        }

        this.message.setText(message);
    }
}
