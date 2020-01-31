package com.yudha.ecommerce.Sellers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yudha.ecommerce.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class SellerAddNewProductActivity extends AppCompatActivity {

    String CategoryName,Description,Price,Pname,saveCurrentDate,saveCurrentTime;

    static final int GalleryPick=1;
    Uri ImagerUri;
    String productRandomKey,downloadImageUrl;
    StorageReference ProductImageRef;
    DatabaseReference ProductsReff,sellersRef;
    ProgressDialog loadingBar;

    Button AddNewProductButton;
    ImageView InputProductImage;
    EditText InputProductName,InputProductDescription,InputProductPrice;

    String sName,sAddress,sPhone,sEmail,sId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_add_new_product);

        CategoryName=getIntent().getExtras().get("category").toString();
        ProductImageRef= FirebaseStorage.getInstance().getReference().child("Products Images");
        ProductsReff=FirebaseDatabase.getInstance().getReference().child("Products");
        sellersRef=FirebaseDatabase.getInstance().getReference().child("Sellers");

        loadingBar= new ProgressDialog(this);

        AddNewProductButton=findViewById(R.id.add_new_product);
        InputProductImage=findViewById(R.id.select_product_image);
        InputProductName=findViewById(R.id.product_name);
        InputProductDescription=findViewById(R.id.product_description);
        InputProductPrice=findViewById(R.id.product_price);

        InputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        AddNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });

        sellersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            sName=dataSnapshot.child("name").getValue().toString();
                            sAddress=dataSnapshot.child("address").getValue().toString();
                            sPhone=dataSnapshot.child("phone").getValue().toString();
                            sId=dataSnapshot.child("sid").getValue().toString();
                            sEmail=dataSnapshot.child("email").getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void OpenGallery() {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            ImagerUri=data.getData();
            InputProductImage.setImageURI(ImagerUri);
        }
    }

    private void ValidateProductData() {
        Description=InputProductDescription.getText().toString();
        Price=InputProductPrice.getText().toString();
        Pname=InputProductName.getText().toString();

        if (ImagerUri == null){
            Toast.makeText(this, "Products image is mandatory...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Please write product description..", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Price)){
            Toast.makeText(this, "Please write product prices..", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Pname)){
            Toast.makeText(this, "Please write product name..", Toast.LENGTH_SHORT).show();
        }
        else {
            StoreProductInformation();
        }
    }



    private void StoreProductInformation() {

        loadingBar.setTitle("Add New Products");
        loadingBar.setMessage("Dear seller,Please wait, While we are adding the new product ");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, YYYY");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        productRandomKey= saveCurrentDate + saveCurrentTime;

        final StorageReference filepath=ProductImageRef.child(ImagerUri.getLastPathSegment()+productRandomKey+".jpg");
        final UploadTask uploadTask=filepath.putFile(ImagerUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message=e.toString();
                Toast.makeText(SellerAddNewProductActivity.this, "Eror: "+ message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(SellerAddNewProductActivity.this, "Image uploaded Successfully ", Toast.LENGTH_SHORT).show();

                Task<Uri>urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        downloadImageUrl=filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){

                            downloadImageUrl=task.getResult().toString();
                            Toast.makeText(SellerAddNewProductActivity.this, "got the Products image url Successfully...", Toast.LENGTH_SHORT).show();

                            SaveProductInfoToDatabase();
                        }

                    }
                });
            }
        });
    }

    private void SaveProductInfoToDatabase() {
        HashMap<String,Object>productMap=new HashMap<>();
        productMap.put("pid",productRandomKey);
        productMap.put("date",saveCurrentDate);
        productMap.put("time",saveCurrentTime);
        productMap.put("description",Description);
        productMap.put("image",downloadImageUrl);
        productMap.put("category",CategoryName);
        productMap.put("price",Price);
        productMap.put("pname",Pname);

        productMap.put("sellerName",sName );
        productMap.put("sellerAddress",sAddress);
        productMap.put("sellerPhone",sPhone );
        productMap.put("sellerEmail",sEmail);
        productMap.put("sid",sId);
        productMap.put("productState","Not Approved");


        ProductsReff.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            Intent intent=new Intent(SellerAddNewProductActivity.this, SellerHomeActivity.class);
                            startActivity(intent);

                            loadingBar.dismiss();
                            Toast.makeText(SellerAddNewProductActivity.this, "Products is added successfully..", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            loadingBar.dismiss();
                            String message=task.getException().toString();
                            Toast.makeText(SellerAddNewProductActivity.this, "Eror: "+ message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void sellerInformation()
    {

    }
}
