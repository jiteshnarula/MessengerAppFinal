package com.example.jiteshnarula.bakbak;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

/**
 * Created by jiteshnarula on 10-11-2017.
 */

public class RegisterActivity extends AppCompatActivity {
    TextInputLayout regName,regEmail,regPassword;
     Button newAccountButton;
      FirebaseAuth mAuth;
      private DatabaseReference database;
      //for token
    private DatabaseReference userDatabase;

      private Toolbar registerPageToolBar;
      private Context context =  RegisterActivity.this;

      //Progress Bar
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity_layout);

         mAuth = FirebaseAuth.getInstance();

        regName = (TextInputLayout) findViewById(R.id.regName);
        regEmail = (TextInputLayout) findViewById(R.id.regEmail);
        regPassword  =  (TextInputLayout)findViewById(R.id.regPassword);
        newAccountButton = (Button) findViewById(R.id.newAccountButton);
        registerPageToolBar = (Toolbar) findViewById(R.id.registerpageToolBar);

        progressDialog = new ProgressDialog(this);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        setSupportActionBar(registerPageToolBar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toast.makeText(RegisterActivity.this,"This is Register activity",Toast.LENGTH_LONG).show();




        newAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String enteredName = regName.getEditText().getText().toString();
                String enteredEmail = regEmail.getEditText().getText().toString();
                String enteredPassword =regPassword.getEditText().getText().toString();

                if(!(TextUtils.isEmpty(enteredName) && !(TextUtils.isEmpty(enteredEmail)) && !(TextUtils.isEmpty(enteredPassword)))){
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setTitle("Please wait");
                    progressDialog.setMessage("While we are creating your account...");
                    progressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);

                    registerUser(enteredName,enteredEmail,enteredPassword);

                }
            }
        });
    }

    private void registerUser(final String enteredName, String enteredEmail, String enteredPassword) {
         mAuth.createUserWithEmailAndPassword(enteredEmail,enteredPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete(@NonNull Task<AuthResult> task) {

                 if(task.isSuccessful()){

                     final FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                     final String uid = current_user.getUid();

                     database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                     HashMap<String,String> userMap = new HashMap<>();
                     userMap.put("name",enteredName);
                     userMap.put("status","Namaste I'm ready to do some Bak-Bak");
                     userMap.put("image","default");
                     userMap.put("thumb_image","default");

                     database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {

                             if(task.isSuccessful()) {

                                 progressDialog.dismiss();

                                 String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                 userDatabase.child(uid).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {

                                            Intent intent1 = new Intent(RegisterActivity.this, MainActivity.class);
                                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                            startActivity(intent1);
                                            finish();
                                        }else{
                                            Toast.makeText(RegisterActivity.this,"Error in saveing device token id while registering user",Toast.LENGTH_SHORT).show();
                                        }
                                     }
                                 });
                             }
                         }
                     });
                  }else{
                     progressDialog.hide();
                     Toast.makeText(RegisterActivity.this,"Please check your Internet Connection and Try Again!",Toast.LENGTH_LONG).show();
                 }
             }
         });
    }
}
