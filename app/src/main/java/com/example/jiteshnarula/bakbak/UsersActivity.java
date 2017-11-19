package com.example.jiteshnarula.bakbak;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar usersPageToolbar;
    private RecyclerView usersRecyclerView;
    private DatabaseReference allUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

         usersPageToolbar = (Toolbar) findViewById(R.id.usersPageToolbar);
         usersRecyclerView = (RecyclerView) findViewById(R.id.usersRecyclerView);

         //Databse Reference retriving all the values from database
        allUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


         setSupportActionBar(usersPageToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

          usersRecyclerView.setHasFixedSize(true);
          usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));




    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,R.layout.allusers_layout,UsersViewHolder.class,allUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setUserStatus(model.getStatus());
                viewHolder.setUserImage(model.getThumb_image(),getApplicationContext());

                final String user_id = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);

                    }
                });

            }
        };
        usersRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setName(String name){

            TextView userName  = (TextView)mView.findViewById(R.id.userName);
            userName.setText(name);
        }
        public void setUserStatus(String status){
            TextView usersStatusView = (TextView) mView.findViewById(R.id.userStatus);
            usersStatusView.setText(status);
        }

public void setUserImage(String thumb_image,Context context){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.imageView_user);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.defaultmaleimage).into(userImageView);
}
    }
}
