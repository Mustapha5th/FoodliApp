package com.example.foodliapp.Screens.ui.Authentication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodliapp.Common.Common;
import com.example.foodliapp.Model.User;
import com.example.foodliapp.R;
import com.example.foodliapp.Screens.Home;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.paperdb.Paper;

public class Login extends AppCompatActivity {
    MaterialEditText edtPhone, edtPassword;
    TextView txtLogin, txtForgotPassword, txtSignUp;
    Button btnLogin;
    CheckBox checkBoxRemember;
    FirebaseDatabase database;
    DatabaseReference table_user;
    RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        rootLayout = findViewById(R.id.root_layout);

//        txtLogin = findViewById(R.id.txtLogin);
//        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Caroline.otf");
//        txtLogin.setTypeface(face);

        txtSignUp = findViewById(R.id.txtSignUp);
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(Login.this, Register.class);
                startActivity(register);
                finish();
            }
        });
        txtForgotPassword = findViewById(R.id.txtForgetPassword);
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPassword();
            }
        });
        checkBoxRemember = findViewById(R.id.chkRemember);
        // Init Paper
        Paper.init(this);
        btnLogin = findViewById(R.id.btnLogin);

        // init Firebase
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");
        if (!Common.isConnectedToInternet(getBaseContext())) {
            Snackbar snackbar = Snackbar.make(rootLayout,"Please check your internet connection", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    // save user & password
                    if (checkBoxRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY, edtPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY, edtPassword.getText().toString());
                    }
                    final ProgressDialog mDialog = new ProgressDialog(Login.this);
                    mDialog.setMessage("Please Wait...");
                    mDialog.show();

                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //checking if user exists
                            if (snapshot.child(edtPhone.getText().toString()).exists()) {

                                //Get User information
                                mDialog.dismiss();
                                User user = snapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                user.setPhone(edtPhone.getText().toString());//get phone number
                                if (user.getPassword().equals(edtPassword.getText().toString())) {
                                    Intent home = new Intent(Login.this, Home.class);
                                    Common.currentUser = user;
                                    startActivity(home);
                                    finish();
                                    table_user.removeEventListener(this);
                                } else {
                                    Toast.makeText(Login.this, "Wrong Password or Username...", Toast.LENGTH_SHORT).show();

                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(Login.this, "User does not exist", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else {
                    Snackbar snackbar = Snackbar.make(rootLayout,"Please check your internet connection", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

        });


    }

    private void showForgotPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forget Password");
        builder.setMessage("Enter your secure code");

        LayoutInflater inflater = this.getLayoutInflater();
        View forget_view = inflater.inflate(R.layout.forgot_password_layout, null);

        builder.setView(forget_view);
        builder.setIcon(R.drawable.ic_baseline_security);
        MaterialEditText  edtPhone = forget_view.findViewById(R.id.edtPhone);
        MaterialEditText  edtSecureCode = forget_view.findViewById(R.id.edtsecureCode);

        builder.setPositiveButton(Html.fromHtml("<font color= '#DE8405'>Yes</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // Check if user exist
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.child(edtPhone.getText().toString())
                                .getValue(User.class);

                        if (user.getSecureCode().equals(edtSecureCode.getText().toString())){
                            Toast.makeText(Login.this, "Your Password is: "+ user.getPassword(), Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(Login.this, "Wrong secure code!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        builder.setNegativeButton(Html.fromHtml("<font color= '#DE8405'>No</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // show dialog
        builder.show();
    }

}