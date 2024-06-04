package com.saaweel.healthcheckai.tasks;

import android.os.AsyncTask;

import com.saaweel.healthcheckai.fragments.ChatFragment;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class SendChatMessageTask extends AsyncTask<ChatMessage, Void, ChatMessage> {
    private ChatFragment chatFragment;

    public SendChatMessageTask(ChatFragment chatFragment) {
        this.chatFragment = chatFragment;
    }

    @Override
    protected ChatMessage doInBackground(ChatMessage... messages) {
        ChatMessage userMessage = messages[0];

        List<ChatMessage> sendMessages = new ArrayList<>(this.chatFragment.messageList);
        sendMessages.add(0, this.chatFragment.getInitialPropmpt());
        sendMessages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(sendMessages)
                .maxTokens(256)
                .build();

        try {
            ChatMessage responseMessage = this.chatFragment.service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
            return responseMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ChatMessage responseMessage) {
        if (responseMessage != null) {
            this.chatFragment.addMessage(responseMessage);
        }
    }
}