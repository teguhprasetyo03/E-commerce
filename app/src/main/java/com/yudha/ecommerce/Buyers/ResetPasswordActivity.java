package com.yudha.ecommerce.Buyers;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yudha.ecommerce.Prevalent.Prevalent;
import com.yudha.ecommerce.R;

import java.util.HashMap;

public class ResetPasswordActivity extends AppCompatActivity {

    String check="";
    TextView pageTitle,titleQuestion;
    EditText phoneNumber,question1,question2;
    Button verifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        check=getIntent().getStringExtra("check");

        pageTitle=findViewById(R.id.page_title);
        titleQuestion=findViewById(R.id.title_question);
        phoneNumber=findViewById(R.id.find_phone_number);
        question1=findViewById(R.id.question_1);
        question2=findViewById(R.id.question_2);
        verifyBtn=findViewById(R.id.verify_btn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        phoneNumber.setVisibility(View.GONE);


        if (check.equals("settings"))
        {
            pageTitle.setText("Set Questions");
            titleQuestion.setText("Please Answer for the following Security Questions?");
            verifyBtn.setText("Set");

            displayPreviousAnswer();

            verifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAnswers();

                }
            });
        }
        else if(check.equals("login"))
        {
            phoneNumber.setVisibility(View.VISIBLE);

            verifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    verifyUser();
                }
            });
        }
    }

    private void setAnswers()
    {
        String answare1=question1.getText().toString().toLowerCase();
        String answare2=question2.getText().toString().toLowerCase();

        if (question1.equals("") && question2.equals(""))
        {
            Toast.makeText(ResetPasswordActivity.this, "Please Answer both questions", Toast.LENGTH_SHORT).show();
        }
        else
        {
            DatabaseReference ref= FirebaseDatabase.getInstance()
                    .getReference().child("Users")
                    .child(Prevalent.currentOnlineUser.getPhone());

            HashMap<String,Object> userdataMap= new HashMap<>();
            userdataMap.put("answer1",answare1);
            userdataMap.put("answer2",answare2);

            ref.child("Security Questions").updateChildren(userdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ResetPasswordActivity.this, "You have  set security questions successfully.", Toast.LENGTH_SHORT).show();

                        Intent intent=new Intent(ResetPasswordActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    private void displayPreviousAnswer()
    {
        DatabaseReference ref= FirebaseDatabase.getInstance()
                .getReference().child("Users")
                .child(Prevalent.currentOnlineUser.getPhone());

        ref.child("Security Questions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String ans1= dataSnapshot.child("answer1").getValue().toString();
                    String ans2= dataSnapshot.child("answer2").getValue().toString();

                    question1.setText(ans1);
                    question2.setText(ans2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected void verifyUser()
    {
        final String phone=phoneNumber.getText().toString();
        final String answare1=question1.getText().toString().toLowerCase();
        final String answare2=question2.getText().toString().toLowerCase();

        if (!phone.equals("")&& !answare1.equals("")&& !answare2.equals(""))
        {
            final DatabaseReference ref= FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Users")
                    .child(phone);

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        String mPhone =dataSnapshot.child("phone").getValue().toString();

                        if (dataSnapshot.hasChild("Security Questions"))
                        {
                            String ans1= dataSnapshot.child("Security Questions").child("answer1").getValue().toString();
                            String ans2= dataSnapshot.child("Security Questions").child("answer2").getValue().toString();

                            if (!ans1.equals(answare1))
                            {
                                Toast.makeText(ResetPasswordActivity.this, "your 1st answer is wrong.", Toast.LENGTH_SHORT).show();
                            }
                            else if (!ans2.equals(answare2))
                            {
                                Toast.makeText(ResetPasswordActivity.this, "your 2st answer is wrong.", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                AlertDialog.Builder builder=new AlertDialog.Builder(ResetPasswordActivity.this);
                                builder.setTitle("New Password");

                                final EditText newPassword = new EditText(ResetPasswordActivity.this);
                                newPassword.setHint("Write New Password Here...");
                                builder.setView(newPassword);

                                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!newPassword.getText().toString().equals(""))
                                        {
                                            ref.child("password").setValue(newPassword.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                            {
                                                                Toast.makeText(ResetPasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();

                                                                Intent intent=new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder.show();
                            }
                        }
                        else
                        {
                            Toast.makeText(ResetPasswordActivity.this, "You have not set  the security questions.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(ResetPasswordActivity.this, "This phone number not exists.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else
        {
            Toast.makeText(this, "Please complete the form", Toast.LENGTH_SHORT).show();
        }
    }
}
