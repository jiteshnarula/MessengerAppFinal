package com.example.jiteshnarula.bakbak;

import android.content.Context;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar chatPageToolbar;
    private DatabaseReference rootReference;
    TextView nameTextView;
    TextView seenTextView;
CircleImageView customImage;
private FirebaseAuth mAuth;
private String mCurrentUserId;
private EditText chatEditText;
private ImageButton addImageButton;
private ImageButton sendImageButton;
private RecyclerView mMessagesList;


private final List<Messages>  messagesList = new ArrayList<>();
private LinearLayoutManager mLinearLayout;
private MessageAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatPageToolbar = (Toolbar) findViewById(R.id.chatPageToolbar);
        setSupportActionBar(chatPageToolbar);
        ActionBar actionBar = getSupportActionBar();
       actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        rootReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("userName");

        getSupportActionBar().setTitle(userName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_custom_bar_layout,null);
        actionBar.setCustomView(view);

        //custom toolbar items

        nameTextView = (TextView) findViewById(R.id.nameTextView);
        seenTextView = (TextView) findViewById(R.id.seenTextView);
        customImage = (CircleImageView) findViewById(R.id.customImage);

        addImageButton = (ImageButton) findViewById(R.id.addImageButton);
        sendImageButton = (ImageButton) findViewById(R.id.sendImageButton);
        chatEditText = (EditText) findViewById(R.id.sendEditText);

        mAdapter = new MessageAdapter(messagesList);

//here is the recycler view

        mMessagesList = (RecyclerView) findViewById(R.id.messagesList);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
        loadMessages();

        nameTextView.setText(userName);

        rootReference.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image =dataSnapshot.child("image").getValue().toString();

                if(online.equals("true")){
                    seenTextView.setText("Online");
                }else{
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime  = Long.parseLong(online);
                    String lastSeenTime =  getTimeAgo.getTimeAgo(lastTime,getApplicationContext());

                seenTextView.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this,"error in retriving time",Toast.LENGTH_LONG);
            }
        }) ;

        rootReference.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId+"/" + mChatUser,chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser+"/" + mCurrentUserId,chatAddMap);

                    rootReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("Chat_log",databaseError.getMessage().toString());
                            }

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    private void loadMessages() {
rootReference.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Messages message = dataSnapshot.getValue(Messages.class);
        messagesList.add(message);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});

    }

    private void sendMessage() {
    String message = chatEditText.getText().toString();
    if(!TextUtils.isEmpty(message)){
        String current_user_ref = "messages/" + mCurrentUserId +"/"+mChatUser;
        String chat_user_ref = "messages/"+ mChatUser+"/"+mCurrentUserId;

        DatabaseReference user_message_push = rootReference.child("messages").child(mCurrentUserId).child(mChatUser).push();

String push_id = user_message_push.getKey();
        Map messageMap =  new HashMap();
        messageMap.put("message",message);
        messageMap.put( "seen",false);
        messageMap.put( "type","text");
        messageMap.put( "time",ServerValue.TIMESTAMP);
        messageMap.put( "from",mCurrentUserId);



        Map messageUserMap =  new HashMap();
        messageUserMap.put(current_user_ref +"/"+push_id,messageMap);
        messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

        chatEditText.setText("");

        rootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError != null){
                    Log.d("Chat_log",databaseError.getMessage().toString());
                }

            }
        });

    }
    }
}
