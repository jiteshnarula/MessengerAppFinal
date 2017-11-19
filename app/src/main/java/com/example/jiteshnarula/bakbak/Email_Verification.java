package com.example.jiteshnarula.bakbak;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by jiteshnarula on 10-11-2017.
 */

public class Email_Verification extends AppCompatActivity {

    private Button newAccount;
    private Button existingAccountButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    setContentView(R.layout.email_verification_layout);
    Toast.makeText(Email_Verification.this,"This is email activity",Toast.LENGTH_LONG).show();
newAccount = (Button) findViewById(R.id.newAccountButton);
        existingAccountButton = (Button) findViewById(R.id.existingAccountButton);

        newAccount.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent1 =  new Intent(Email_Verification.this,RegisterActivity.class);
        startActivity(intent1);
    }
});
        existingAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2  = new Intent(Email_Verification.this,LoginActivity.class);
                startActivity(intent2);
            }
        });



    }
}
