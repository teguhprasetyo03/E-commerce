package com.yudha.ecommerce.Buyers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yudha.ecommerce.Admin.AdminHomeActivity;
import com.yudha.ecommerce.Model.Users;
import com.yudha.ecommerce.Prevalent.Prevalent;
import com.rey.material.widget.CheckBox;
import com.yudha.ecommerce.R;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    EditText InputPhoneNumber,InputPassword;
    Button LoginButton;
    ProgressDialog loadingBar;
    TextView AdminLink,NotAdminLink,ForgotPasswordLink;

    String parentDBName="Users";
    CheckBox chkBoxRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginButton=findViewById(R.id.login_btn);
        InputPhoneNumber=findViewById(R.id.login_phone_number_input);
        InputPassword=findViewById(R.id.login_password_input);
        chkBoxRememberMe=findViewById(R.id.remeber_me_chk);
        AdminLink=findViewById(R.id.admin_panel_link);
        NotAdminLink=findViewById(R.id.not_admin_panel_link);
        ForgotPasswordLink=findViewById(R.id.forgot_password_link);
        loadingBar= new ProgressDialog(this);

        Paper.init(this);


        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
            }
        });

        ForgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check","login");
                startActivity(intent);
            }
        });

        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginButton.setText("Login Admin");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                parentDBName="Admins";
                chkBoxRememberMe.setVisibility(View.INVISIBLE);
                ForgotPasswordLink.setVisibility(View.INVISIBLE);
            }
        });

        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginButton.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminLink.setVisibility(View.INVISIBLE);
                parentDBName="Users";
            }
        });
    }

    private void LoginUser() {
        String password= InputPassword.getText().toString();
        String phone= InputPhoneNumber.getText().toString();

        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Write Your Password...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Please Write Your Phone...", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait, While we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            AllowAccessToAccount(phone,password);
        }
    }

    private void AllowAccessToAccount(final String phone, final String password) {

        if (chkBoxRememberMe.isChecked()){
            Paper.book().write(Prevalent.UserPhoneKey,phone);
            Paper.book().write(Prevalent.UserPasswordKey,password);

        }


        final DatabaseReference RootRef;
        RootRef= FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(parentDBName).child(phone).exists())
                {
                    Users userData= dataSnapshot.child(parentDBName).child(phone).getValue(Users.class);

                    if (userData.getPhone().equals(phone))
                    {
                        if (userData.getPassword().equals(password))
                        {
                            if (parentDBName.equals("Admins"))
                            {
                                Toast.makeText(LoginActivity.this, "Welcome Admin, You are logged in Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent=new Intent(LoginActivity.this, AdminHomeActivity.class);
                                startActivity(intent);
                            }
                            else if (parentDBName.equals("Users")){
                                Toast.makeText(LoginActivity.this, "logged in Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent=new Intent(LoginActivity.this, HomeActivity.class);
                                Prevalent.currentOnlineUser=userData;
                                startActivity(intent);
                            }
                        }
                        else {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Password user login is incorrect.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                        Toast.makeText(LoginActivity.this, "Account with this "+phone+ " number do not exist", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "You need to create a new Account.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
