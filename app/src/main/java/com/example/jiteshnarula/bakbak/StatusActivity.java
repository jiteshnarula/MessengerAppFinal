package com.example.jiteshnarula.bakbak;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {


    private Toolbar statusPageToolbar;
    private FirebaseUser currentUser;
    private Button statusButton;
    private TextInputLayout statusTextView;
    private DatabaseReference database;

    //Progress Dialog
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        statusPageToolbar = (Toolbar) findViewById(R.id.statusPageToolbar);
        statusTextView = (TextInputLayout) findViewById(R.id.statusTextLayout);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        //Getting Status Value from Account Settings Activity and set it to the status textView
        String statusPreviousValue = getIntent().getStringExtra("statusText");
        statusTextView.getEditText().setText(statusPreviousValue);

        database = FirebaseDatabase.getInstance().getReference().child("Users").child(UID);

        statusButton = (Button) findViewById(R.id.statusButton);

        setSupportActionBar(statusPageToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //status loading progressdialog
                progressDialog = new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait while we save");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();


                String status = statusTextView.getEditText().getText().toString();
                database.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                        }else{
                            Toast.makeText(StatusActivity.this,"There is some errors in saving changes.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });



    }
}
