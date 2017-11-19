package com.example.jiteshnarula.bakbak;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mainpageToolBar;
    private ViewPager viewPager;
    private SectionPagerAdapter sectionPagerAdapter;
    private TabLayout tabLayout;
    private Toolbar tabsToolBar;
    private DatabaseReference mUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        Toast.makeText(MainActivity.this,"this is Main Activity",Toast.LENGTH_LONG).show();

        //Adding Fragments in Main Activity
        viewPager = (ViewPager) findViewById(R.id.container);
        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionPagerAdapter);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        //Adding ToolBars for both Appbar and Fragments tabs...





         mainpageToolBar = (Toolbar) findViewById(R.id.mainpageToolBar);
       setSupportActionBar(mainpageToolBar);
        getSupportActionBar().setTitle("Bak Bak");

    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

            if(currentUser == null){
                sendToStart();
        }else{
                mUserRef.child("online").setValue("true");
            }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
             mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
         }

    private void sendToStart() {
        Intent intent0  = new Intent(MainActivity.this,Email_Verification.class);
        startActivity(intent0);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.logout_menu){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
            if(item.getItemId() == R.id.account_settings_menu){
            Intent intent1 = new Intent(MainActivity.this,AccountSettingsActivity.class);
            startActivity(intent1);
            }

            if(item.getItemId() == R.id.all_users_menu){
                Intent intent2 = new Intent(MainActivity.this,UsersActivity.class);
                startActivity(intent2);
            }

        return true;

    }
}
