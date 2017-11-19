package com.example.jiteshnarula.bakbak;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettingsActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    private CircleImageView circleImageView;
     private TextView nameTextView;
    private TextView statusTextView;
    Button imageButton;
    Button statusButton;

    //progress Dialog
   private  ProgressDialog progressDialog;

    private static final int GALLERY_PICK=1;

    //Storage Firebase
    private StorageReference profilePictureStorage;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        Toast.makeText(AccountSettingsActivity.this,"Account Settings Activity",Toast.LENGTH_LONG).show();

        circleImageView = (CircleImageView) findViewById(R.id.circleImageView);
         nameTextView = (TextView) findViewById(R.id.nameTextView);
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        imageButton = (Button) findViewById(R.id.imageButton);
        statusButton =  (Button) findViewById(R.id.statusButton);
        profilePictureStorage = FirebaseStorage.getInstance().getReference();



        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUID = firebaseUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        databaseReference.keepSynced(true);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

             String name = dataSnapshot.child("name").getValue().toString();
             final String image = dataSnapshot.child("image").getValue().toString();
             String status = dataSnapshot.child("status").getValue().toString();
             String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                nameTextView.setText(name);
                statusTextView.setText(status);

                if(!image.equals("default")){

                    //Picasso.with(AccountSettingsActivity.this).load(image).placeholder(R.drawable.defaultmaleimage).into(circleImageView);

                    Picasso.with(AccountSettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.defaultmaleimage).into(circleImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(AccountSettingsActivity.this).load(image).placeholder(R.drawable.defaultmaleimage).into(circleImageView);
                        }
                    });




                }

               }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String statusText = statusTextView.getText().toString();
                Intent intent = new Intent(AccountSettingsActivity.this,StatusActivity.class);
                intent.putExtra("statusText",statusText);
                startActivity(intent);
            }
        });


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(AccountSettingsActivity.this);

//                Intent galleryIntent =  new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(galleryIntent,"Slect Image"),GALLERY_PICK);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog =  new ProgressDialog(AccountSettingsActivity.this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait until we upload and process your image.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
//                Toast.makeText(AccountSettingsActivity.this,resultUri.toString(),Toast.LENGTH_SHORT).show();


            final File thumb_filePath = new File(resultUri.getPath());



                //Accessing Firebase Directory inside storage directory

                String user_id = firebaseUser.getUid();



//compressing image using githubb library
                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream baos =  new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    final byte[] thumb_byte = baos.toByteArray();



                StorageReference filePath = profilePictureStorage.child("profile_images").child(user_id + ".jpg");
                final StorageReference thumbnail = profilePictureStorage.child("profile_images").child("thumbs").child(user_id + ".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                        final String download_url = task.getResult().getDownloadUrl().toString();

                        UploadTask uploadTask = thumbnail.putBytes(thumb_byte);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                if(thumb_task.isSuccessful()){

                                    Map update_hashMap =  new HashMap();
                                    update_hashMap.put("image",download_url);
                                    update_hashMap.put("thumb_image",thumb_downloadUrl);


                                    databaseReference.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {



                                            if(task.isSuccessful()){
                                                progressDialog.dismiss();
                                                Toast.makeText(AccountSettingsActivity.this,"Uplaoding done!",Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });

                                }else{
                                    Toast.makeText(AccountSettingsActivity.this,"Error Occured in thumnail",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }

                            }
                        });


                            //    Toast.makeText(AccountSettingsActivity.this,"everything is working fine",Toast.LENGTH_SHORT).show();
                        }else{

          Toast.makeText(AccountSettingsActivity.this,"Error Occured",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                    }
                    }
                });  } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }



}
