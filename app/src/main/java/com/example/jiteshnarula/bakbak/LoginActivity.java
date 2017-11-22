package com.example.jiteshnarula.bakbak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by jiteshnarula on 11-11-2017.
 */

public class LoginActivity extends AppCompatActivity {
    TextInputLayout logEmail;
    TextInputLayout logPassword;
    Button loginButton;
    private Toolbar loginPageToolBar;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);

        mAuth = FirebaseAuth.getInstance();

        logEmail = (TextInputLayout) findViewById(R.id.logEmail);
        logPassword = (TextInputLayout) findViewById(R.id.logPassword);
        loginButton = (Button) findViewById(R.id.loginButton);
            loginPageToolBar = (Toolbar) findViewById(R.id.loginPageToolBar);

            progressDialog = new ProgressDialog(this);


            userDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

            setSupportActionBar(loginPageToolBar);
            getSupportActionBar().setTitle("Login");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String enteredEmail = logEmail.getEditText().getText().toString();
                    String enteredPassword = logPassword.getEditText().getText().toString();

                    if(!TextUtils.isEmpty(enteredEmail) && !TextUtils.isEmpty(enteredPassword)){
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setTitle("Logging In");
                        progressDialog.setMessage("Please wait while we are checking your credentials");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        loginUser(enteredEmail,enteredPassword);
                    }
                }
            });




    }

    private void loginUser(String enteredEmail, String enteredPassword) {
        mAuth.signInWithEmailAndPassword(enteredEmail,enteredPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();

                    String current_user_id = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    userDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                Intent intent  = new Intent(LoginActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                                finish();
                            }else{
Toast.makeText(LoginActivity.this,"Token ID can't saved",Toast.LENGTH_SHORT);
                            }
                        }
                    });

                }else{
                    progressDialog.hide();
                    Toast.makeText(LoginActivity.this,"Please Check your credentials and try agian!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
