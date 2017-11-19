package com.example.jiteshnarula.bakbak;

import android.graphics.Color;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jiteshnarula on 17-11-2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;

    }
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout,parent,false);
    return new MessageViewHolder(view);
    }
    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public CircleImageView profileImage;

        public MessageViewHolder(View view){
            super(view);

            messageText =(TextView)view.findViewById(R.id.messageTextLayout);
            profileImage = (CircleImageView)view.findViewById(R.id.messageimageView);
            mAuth = FirebaseAuth.getInstance();


        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        String current_user_id = mAuth.getCurrentUser().getUid();
        Messages c  = mMessageList.get(position);
        String  from_user = c.getFrom();
        if( from_user.equals(current_user_id))
        {
            holder.messageText.setBackgroundColor(Color.WHITE);
            holder.messageText.setTextColor(Color.BLACK);
        }else
        {
holder.messageText.setBackgroundResource(R.drawable.message_text_background);
holder.messageText.setTextColor(Color.WHITE);
        }
        holder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
