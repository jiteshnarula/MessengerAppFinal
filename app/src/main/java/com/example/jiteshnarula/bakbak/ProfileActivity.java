package com.example.jiteshnarula.bakbak;

import android.app.ProgressDialog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

public class ProfileActivity extends AppCompatActivity {

   private ImageView profileImageView;
   private TextView profileNameTextView;
   private TextView profileStatusTextView;
   private TextView profileFriendsTextView;
   private Button sendRequestButton;
   private Button declineButton;

   private DatabaseReference rootReference;


   private FirebaseUser currentUser;

private ProgressDialog progressDialog;
//current state for request
    private String currentState;



   //Database Reference
    private DatabaseReference databaseReference;

    //Database Reference for friend Request
    private DatabaseReference friendDatabaseReference;

    private DatabaseReference mfriendDatabase;

    private DatabaseReference mNotificationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        //current user reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        //friends reference
        friendDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mfriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notification");


        //current user reference using firebase Auth
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileImageView = (ImageView)findViewById(R.id.profileImageView);
        profileNameTextView = (TextView) findViewById(R.id.profileNameTextView);
        profileStatusTextView = (TextView) findViewById(R.id.profileStatusTextView);
        profileFriendsTextView = (TextView) findViewById(R.id.profileFriendsTextView);
        sendRequestButton = (Button) findViewById(R.id.requestButton);
        declineButton = (Button) findViewById(R.id.declineButton);
        rootReference  = FirebaseDatabase.getInstance().getReference();

        currentState = "not_friends";

        declineButton.setVisibility(View.INVISIBLE);
        declineButton.setEnabled(false);


progressDialog = new ProgressDialog(this);
progressDialog.setTitle("Loading User Data");
progressDialog.setMessage("Please wait while we load the userdata");
progressDialog.setCanceledOnTouchOutside(false);
progressDialog.show();




        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image  = dataSnapshot.child("image").getValue().toString();

                profileNameTextView.setText(displayName);
                profileStatusTextView.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.defaultmaleimage).into(profileImageView);

                //---------------------------------Friends List /Request Feature--------------------//
                friendDatabaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       if(dataSnapshot.hasChild(user_id)){
                           String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                           if(req_type.equals("received")){
                                currentState = "req_received";
                                sendRequestButton.setText("Accept Friend Request");
                               declineButton.setVisibility(View.VISIBLE);
                               declineButton.setEnabled(true);


                           }else if(req_type.equals("sent")){
                                currentState ="req_sent";
                                sendRequestButton.setText("Cancel Friend Request");

                                declineButton.setVisibility(View.INVISIBLE);
                                declineButton.setEnabled(false);

                           }
                           progressDialog.dismiss();
                       }else {
                           mfriendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {
                                   if(dataSnapshot.hasChild(user_id)){
                                       currentState ="friends";
                                       sendRequestButton.setText("Unfriend");
                                       declineButton.setVisibility(View.INVISIBLE);
                                       declineButton.setEnabled(false);
                                   }
                                   progressDialog.dismiss();
                               }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {
                                   progressDialog.dismiss();
                               }
                           });
                       }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

sendRequestButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {

        sendRequestButton.setEnabled(false);

        //-----------------------------------Not Friends State ----------------------------------//

        if(currentState == "not_friends"){

            DatabaseReference newNotifiactionRef = rootReference.child("notification").child(user_id).push();
            String newNotificationId  = newNotifiactionRef.getKey();

            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("from",currentUser.getUid());
            notificationData.put("type","request");


            Map requestMap = new HashMap<>();
            requestMap.put("Friend_req/"+currentUser.getUid() + "/" + user_id + "/request_type" , "sent");
            requestMap.put("Friend_req/"+user_id + "/" + currentUser.getUid() + "/request_type", "received");
            requestMap.put("notification/" + user_id + "/" + newNotificationId,notificationData);

            rootReference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    String error = databaseError.getMessage();
                    Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_LONG).show();

                }
                    sendRequestButton.setEnabled(true);
                    currentState ="req_sent";
                    sendRequestButton.setText("Cancel Friend Request");
                }
            });

        }
        //----------------------------------- Cancel Request State  ----------------------------------//

        if(currentState.equals("req_sent")){
            friendDatabaseReference.child(currentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        friendDatabaseReference.child(user_id).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    sendRequestButton.setEnabled(true);
                                    currentState ="not_friends";
                                    sendRequestButton.setText("Send Friend Request");
                                    declineButton.setVisibility(View.INVISIBLE);
                                    declineButton.setEnabled(false);
                                }
                            }
                        });
                    }
                }
            });
        }
        //------------------------------------------------Request Received State-------------------------------------------------//
        if(currentState.equals("req_received")){

            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

            Map friendMap = new HashMap();
            friendMap.put("Friends/"+currentUser.getUid()+"/"+user_id+"/date",currentDate);
            friendMap.put("Friends/"+user_id +"/"+currentUser.getUid()+"/date",currentDate);

            friendMap.put("Friend_req/" + currentUser.getUid()+"/"+user_id,null);
            friendMap.put("Friend_req/"+user_id+"/"+currentUser.getUid(),null);


rootReference.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

        if(databaseError == null){
            sendRequestButton.setEnabled(true);
            currentState ="friends";
            sendRequestButton.setText("Unfriend this Person");
            declineButton.setVisibility(View.INVISIBLE);
            declineButton.setEnabled(false);
        }else{
            String error = databaseError.getMessage();
            Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_LONG).show();

            }
        }
    });
}

//------------------------------Unfriend This person--------------------------------------//
        if(currentState.equals("friends")){
            Map unfriendMap = new HashMap();
            unfriendMap.put("Friends/" + currentUser.getUid() + "/" + user_id ,null);
             unfriendMap.put("Friends/"+user_id + "/" + currentUser.getUid(),null);

            rootReference.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError == null){

                        currentState ="not_friends";
                        sendRequestButton.setText("Send Friend Request");
                        declineButton.setVisibility(View.INVISIBLE);
                        declineButton.setEnabled(false);
                    }else{
                        String error = databaseError.getMessage();
                        Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_LONG).show();

                    }
                    sendRequestButton.setEnabled(true);
                }
            });
        }


    }
});


    }
}
