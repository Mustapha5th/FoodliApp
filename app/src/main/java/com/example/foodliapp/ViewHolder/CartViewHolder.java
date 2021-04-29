package com.example.foodliapp.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.foodliapp.Interface.ItemClickListener;
import com.example.foodliapp.R;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView txt_cart_name, txt_price;
    public ImageView img_cart_item_count;
    public ElegantNumberButton btn_quantity;
    public ImageView cart_image;

    public RelativeLayout view_background;
    public LinearLayout  view_foreground;

    private ItemClickListener itemClickListener;

    public CartViewHolder( View itemView) {
        super(itemView);
        txt_cart_name =  itemView.findViewById(R.id.cart_item_name);
        txt_price = itemView.findViewById(R.id.cart_item_price);
        img_cart_item_count = itemView.findViewById(R.id.img_cart_item_count);
        // btn_quantity = itemView.findViewById(R.id.btn_quantity);
        cart_image = itemView.findViewById(R.id.cart_image);

        view_background = itemView.findViewById(R.id.view_background);
        view_foreground = itemView.findViewById(R.id.view_foreground);

      //  itemView.setOnCreateContextMenuListener(this);


    }

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    @Override
    public void onClick(View v) {

    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//
//        menu.setHeaderTitle("Select action");
//        menu.add(0,0,getAdapterPosition(), Common.DELETE);
//
//    }
}
