package com.example.foodliapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodliapp.Common.Common;
import com.example.foodliapp.Model.User;
import com.example.foodliapp.Screens.Home;
import com.example.foodliapp.Screens.ui.Authentication.Login;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
   private static final int SPLASH_SCREEN = 5000;
    TextView txtSlogan, txtAppName;
    ImageView iVStartScreen;
    Animation topAnim, bottomAnim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_main);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        iVStartScreen = findViewById(R.id.iVStartScreen);



        // font
        txtSlogan = findViewById(R.id.txtSlogan);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Caroline.otf");
        txtSlogan.setTypeface(face);
        txtAppName = findViewById(R.id.txtAppName);
        Typeface screen = Typeface.createFromAsset(getAssets(), "fonts/Caroline.otf");
        txtAppName.setTypeface(face);
        // set Animation
        iVStartScreen.setAnimation(topAnim);
        txtSlogan.setAnimation(bottomAnim);
        txtAppName.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN);
        // init paper
        Paper.init(this);


        // check remember
        String user = Paper.book().read(Common.USER_KEY);
        String password = Paper.book().read(Common.PWD_KEY);

        if (user != null && password != null){
            if (!user.isEmpty() && !password.isEmpty()){
                login(user,password);
            }
        }


    }

    private void login(String phone, String password) {

        // init Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        if (Common.isConnectedToInternet(getBaseContext())) {
            // save user & password
            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Please Wait...");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //checking if user exists
                    if (snapshot.child(phone).exists()) {

                        //Get User information
                        mDialog.dismiss();
                        User user = snapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);//get phone number
                        user.setBalance(String.valueOf(0.0));
                        if (user.getPassword().equals(password)) {
                            Intent home = new Intent(MainActivity.this, Home.class);

                            Common.currentUser = user;
                            startActivity(home);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Wrong Password or Username...", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {

            // TO DO: convert to snack bar ...
            Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}