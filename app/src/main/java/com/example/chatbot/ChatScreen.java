package com.example.chatbot;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;

public class ChatScreen extends AppCompatActivity {

    RecyclerView chatRecyclerView;
    EditText messageInput;
    Button sendButton;
    List<ChatMessage> chatMessages = new ArrayList<>();
    ChatAdapter chatAdapter;
    SharedPreferences sharedPreferences;
    String Username;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        Username = sharedPreferences.getString("LoggedInUser", "User");
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setAdapter(chatAdapter);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        ChatMessage welcomeMessage = new ChatMessage("Welcome, " + Username + "!", false);
        chatMessages.add(welcomeMessage);
        chatAdapter.notifyDataSetChanged();






        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                ChatMessage newMessage = new ChatMessage(message, true);

                chatAdapter.addMessage(newMessage);

                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() -1);
                messageInput.setText("");
                sendMessageToServer(message);
            }
        });
    }
    public void sendMessageToServer(String userMessage) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:5000/chat");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("userMessage=" + URLEncoder.encode(userMessage, "UTF-8"));
                writer.flush();
                writer.close();
                os.close();

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                conn.disconnect();

                String serverResponse = response.toString();
                Log.d("ChatScreen", "Server Response: " + serverResponse);
                runOnUiThread(() -> chatAdapter.addMessage(new ChatMessage(serverResponse, false)));
            } catch (Exception e) {
                Log.e("ChatScreen", "Network Error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

}