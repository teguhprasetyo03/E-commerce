package com.yudha.ecommerce.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yudha.ecommerce.Interface.ItemClickListener;
import com.yudha.ecommerce.R;

public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtProductName,txtProductDescription,txtProductPrice,txtProductStatus;
    public ImageView imageView;
    ItemClickListener listener;

    public ItemViewHolder(@NonNull View itemView)
    {
        super(itemView);

        imageView=itemView.findViewById(R.id.seller_product_image);
        txtProductName=itemView.findViewById(R.id.seller_product_name);
        txtProductDescription=itemView.findViewById(R.id.seller_product_description);
        txtProductPrice=itemView.findViewById(R.id.seller_product_price);
        txtProductStatus=itemView.findViewById(R.id.seller_product_state);
    }

    public void setItemClickListener(ItemClickListener listener){
        this.listener=listener;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view,getAdapterPosition(),false);
    }
}
