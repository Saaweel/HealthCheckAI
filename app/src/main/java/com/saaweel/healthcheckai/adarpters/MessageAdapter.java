package com.saaweel.healthcheckai.adarpters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saaweel.healthcheckai.R;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<Message> {
    List<ChatMessage> messageDataSet;
    private String myAvatarUrl;
    private String myUsername;
    public MessageAdapter(List<ChatMessage> messageDataSet, String myUsername, String myAvatarUrl) {
        this.messageDataSet = messageDataSet;
        this.myUsername = myUsername;
        this.myAvatarUrl = myAvatarUrl;
    }

    @Override
    public Message onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_message, viewGroup, false);
        return new Message(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Message message, int position) {
        ChatMessage data = messageDataSet.get(position);
        String username = "HealthCheckAI";
        String avatarUrl = "";

        if (data.getRole().equals(ChatMessageRole.USER.value())) {
            username = this.myUsername;
            avatarUrl = this.myAvatarUrl;
        }

        message.setData(username, avatarUrl, data.getContent());
    }

    @Override
    public int getItemCount() {
        return messageDataSet.size();
    }
}