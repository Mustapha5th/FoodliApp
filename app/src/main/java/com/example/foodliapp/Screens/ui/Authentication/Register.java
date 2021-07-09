package com.example.foodliapp.Screens.ui.Authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class Register extends AppCompatActivity {

    MaterialEditText edtName, edtPhone, edtPassword, edtSecureCode;
    Button btnRegister;
    TextView txtRegister, txtSignIn;
    RelativeLayout rootLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        rootLayout = findViewById(R.id.root_layout);
        txtSignIn = findViewById(R.id.txtSignIn);
        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(Register.this, Login.class);
                startActivity(login);
                finish();
            }
        });
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtSecureCode = findViewById(R.id.edtsecureCode);

        btnRegister = findViewById(R.id.btnRegister);

        // init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){
                    final ProgressDialog mDialog = new ProgressDialog(Register.this);
                    mDialog.setMessage("Please Wait...");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (edtPhone.getText().toString().isEmpty() || edtName.getText().toString().isEmpty() || edtPassword.getText().toString().isEmpty()){
                                mDialog.dismiss();
                                Toast.makeText(Register.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                            } else
                                // Check if user already exists
                                if (snapshot.child(edtPhone.getText().toString()).exists()){
                                    mDialog.dismiss();
                                    Toast.makeText(Register.this, "Phone Number Already Exists", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    mDialog.dismiss();
                                    User user = new User(edtName.getText().toString(), edtPassword.getText().toString(), edtSecureCode.getText().toString());
                                    user.setBalance(0.0);
                                    table_user.child(edtPhone.getText().toString()).setValue(user);
                                    Toast.makeText(Register.this, "User Successfully registered", Toast.LENGTH_SHORT).show();
                                    Intent home = new Intent(Register.this, Home.class);
                                    Common.currentUser = user;
                                    startActivity(home);
                                    finish();
                                }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Snackbar snackbar = Snackbar.make(rootLayout,"Please check your internet connection", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });


    }
}