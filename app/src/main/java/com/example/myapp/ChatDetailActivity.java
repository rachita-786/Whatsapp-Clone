package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.myapp.Adapters.ChatAdapter;
import com.example.myapp.Models.MessageModel;
import com.example.myapp.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        final String senderId = auth.getUid();
        String recieverId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.user).into(binding.profileImage);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this , MainActivity.class);
                startActivity(intent);
            }
        });
    final ArrayList<MessageModel> messageModels =new ArrayList();
    final ChatAdapter chatAdapter = new ChatAdapter(messageModels , this  );
    binding.chatRecylearView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this );
        binding.chatRecylearView.setLayoutManager(layoutManager);

        final String senderRoom =senderId +recieverId;
        final String receiverRoom =recieverId+senderId;

        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        messageModels.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren())
                        {
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            messageModels.add(model);

                        }
                        chatAdapter.notifyDataSetChanged();  //to start recycler view on runtime (sent msgs can be seen )
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }

                });


                binding.send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        String message = binding.etMessage.getText().toString();
                        final MessageModel model=new MessageModel(senderId,message);
                        model.setTimestamp(new Date().getTime());
                        binding.etMessage.setText("");
                        database.getReference().child("chats")
                                .child(senderRoom)
                                .push()
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .push()
                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });

                            }
                        });

                    }
                });

    }
}