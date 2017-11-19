package com.example.jiteshnarula.bakbak;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsRecyclerView;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private View friendView;
    private DatabaseReference friendDatabaseReference;
    private DatabaseReference mUsersDatbase;

    TextView simpleStatusTextView;


    public FriendsFragment() {
         // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        friendView = inflater.inflate(R.layout.fragment_friends,container,false);
        friendsRecyclerView = (RecyclerView) friendView.findViewById(R.id.friendsRecyclerView);
        firebaseAuth  =FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();



        friendDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friends")
                .child(currentUserId);
        friendDatabaseReference.keepSynced(true);

        mUsersDatbase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatbase.keepSynced(true);
        friendsRecyclerView.setHasFixedSize(true);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return friendView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
            Friends.class,
            R.layout.allusers_layout,
            FriendsViewHolder.class,
            friendDatabaseReference
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends model, int position) {

                final String listUserId = getRef(position).getKey();
                mUsersDatbase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild("online")) {
                            String userOnline =   dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);

                        }
                        friendsViewHolder.setStatus(userStatus);
                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setUserImage(userThumb,getContext());


                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which == 0 ){
                                            Intent profileIntent = new Intent(getActivity(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",listUserId);
                                            startActivity(profileIntent);
                                        }
                                        if(which == 1){
                                            Intent chatIntent = new Intent(getActivity(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",listUserId);
                                            chatIntent.putExtra("userName",userName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        friendsRecyclerView.setAdapter(friendsRecyclerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

View mView;

        public FriendsViewHolder(View itemView) {

            super(itemView);

            mView = itemView;
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.userName);
            userNameView.setText(name);
        }
        public void setStatus(String status){
            TextView userStatus = (TextView) mView.findViewById(R.id.userStatus);
            userStatus.setText(status);
        }

        public void setUserImage(String thumb_image,Context context){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.imageView_user);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.defaultmaleimage).into(userImageView);
        }

        public void setUserOnline(String onlineStatus){
            ImageView  onlineImageView = (ImageView) mView.findViewById(R.id.onlineImageView);
            if(onlineStatus.equals("true")){
                onlineImageView.setVisibility(View.VISIBLE);
            }else{
                onlineImageView.setVisibility(View.INVISIBLE);

            }
        }
    }
}
