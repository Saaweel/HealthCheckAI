package com.saaweel.healthcheckai.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.saaweel.healthcheckai.R;
import com.saaweel.healthcheckai.activities.MainActivity;
import com.saaweel.healthcheckai.adarpters.MessageAdapter;
import com.saaweel.healthcheckai.tasks.SendChatMessageTask;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {
    private RecyclerView messages;
    private String myUsername;
    private String myAvatarUrl = "";
    private String gender;
    private String age;
    private String weight;
    private String height;
    private Boolean heart_problems;
    private Boolean diabetes;
    private List<String> allergies;
    public List<ChatMessage> messageList;
    FirebaseFirestore db;
    public OpenAiService service;

    public ChatFragment() {
        this.messageList = new ArrayList<>();

        FirebaseApp.initializeApp(this.getContext());

        this.db = FirebaseFirestore.getInstance();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        this.service = new OpenAiService("");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();

        activity.setTabs("app1");

        activity.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                activity.changeFragment(new MainFragment());

                activity.setTabs("home");
            }
        });

        this.myUsername = activity.getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("LOGIN_USERNAME", "");

        this.db.collection("messages").orderBy("timestamp").whereEqualTo("chat", this.myUsername).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                    Map<String, Object> message = task.getResult().getDocuments().get(i).getData();

                    if (message != null) {
                        HashMap<String, Object> messageData = (HashMap<String, Object>) message.get("message");

                        ChatMessage chatMessage = new ChatMessage(messageData.get("role").equals("user") ? ChatMessageRole.USER.value() : ChatMessageRole.ASSISTANT.value(), messageData.get("content").toString());

                        this.messageList.add(chatMessage);
                    }
                }

                if (messages != null && messages.getAdapter() != null) {
                    messages.getAdapter().notifyDataSetChanged();
                }

                this.scrollToBottom();
            }
        });
    }

    private void scrollToBottom() {
        if (messages != null && messages.getAdapter() != null && messages.getAdapter().getItemCount() > 0) {
            messages.post(() -> messages.smoothScrollToPosition(messages.getAdapter().getItemCount() - 1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messages = view.findViewById(R.id.messages);

        this.db.collection("users").whereEqualTo("username", this.myUsername).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);

                    String avatarUrl = document.getString("avatar");

                    if (avatarUrl != null) {
                        this.myAvatarUrl = avatarUrl;
                    }

                    String gender = document.getString("gender");
                    if (gender != null) {
                        this.gender = gender;
                    }

                    String age = document.getString("age");
                    if (age != null) {
                        this.age = age;
                    }

                    String weight = document.getString("weight");
                    if (weight != null) {
                        this.weight = weight;
                    }

                    String height = document.getString("height");
                    if (height != null) {
                        this.height = height;
                    }

                    Boolean heart_problems = document.getBoolean("heart_problems");
                    if (heart_problems != null) {
                        this.heart_problems = heart_problems;
                    }

                    Boolean diabetes = document.getBoolean("diabetes");
                    if (diabetes != null) {
                        this.diabetes = diabetes;
                    }

                    List<String> allergies = (List<String>) document.get("allergies");
                    if (allergies != null) {
                        this.allergies = allergies;
                    }
                }
            }

            messages.setAdapter(new MessageAdapter(this.messageList, this.myUsername, this.myAvatarUrl));

            this.scrollToBottom();
        });

        EditText messageInput = view.findViewById(R.id.messageInput);
        TextView sendMessage = view.findViewById(R.id.sendMessage);

        sendMessage.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString();
            if (!messageText.isEmpty()) {
                ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), messageText);

                addMessage(message);

                messageInput.setText("");

                List<ChatMessage> sendMessages = new ArrayList<>(this.messageList);

                sendMessages.add(0,  this.getInitialPropmpt());

                new SendChatMessageTask(this).execute(message);
            }
        });

        return view;
    }

    public void addMessage(ChatMessage message) {
        Map<String, Object> messageToInsert = new HashMap<>();

        messageToInsert.put("timestamp", System.currentTimeMillis());

        messageToInsert.put("chat", this.myUsername);

        messageToInsert.put("message", message);

        this.messageList.add(message);

        this.db.collection("messages").add(messageToInsert);

        messages.getAdapter().notifyDataSetChanged();

        this.scrollToBottom();
    }

    public ChatMessage getInitialPropmpt() {
        String message =
                "Eres un asistente de salud, y estás hablando " +
                "con un usuario que necesita ayuda con su salud. " +
                "Por favor, proporciona información útil y precisa " +
                "para ayudar al usuario. Puedes hacer preguntas " +
                "para obtener más información sobre el usuario. " +
                "Recuerda no salir NUNCA de tu rol de asistente de salud." +
                "Cualquier otro tema que no sea salud debes responder indicando " +
                "que eres un asistente de salud y no puedes hablar de eso.";

        message += "\nDatos del usuario: ";

        if (this.gender != null)
            message += "\n- Género: " + (this.gender.equals("male") ? "Hombre" : "Mujer");

        if (this.age != null)
            message += "\n- Edad: " + this.age;

        if (this.weight != null)
            message += "\n- Peso: " + this.weight;

        if (this.height != null)
            message += "\n- Altura: " + this.height;

        if (this.heart_problems != null)
            message += "\n- Problemas de corazón: " + (this.heart_problems ? "Sí" : "No");

        if (this.diabetes != null)
            message += "\n- Diabetes: " + (this.diabetes ? "Sí" : "No");

        if (this.allergies != null)
            message += "\n- Alergias: " + this.allergies;


        return new ChatMessage(ChatMessageRole.SYSTEM.value(), message);
    }
}