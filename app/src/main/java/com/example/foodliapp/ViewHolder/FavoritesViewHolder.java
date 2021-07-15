package com.example.foodliapp.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodliapp.Interface.ItemClickListener;
import com.example.foodliapp.R;

public class FavoritesViewHolder extends RecyclerView.ViewHolder //implements View.OnClickListener
{

    public TextView food_name, food_price;
    public ImageView food_image, addToCartIcon;
    public RelativeLayout view_background;
    public LinearLayout view_foreground;
    private ItemClickListener itemClickListener;

    public FavoritesViewHolder(View itemView) {
        super(itemView);

        food_name = itemView.findViewById(R.id.food_name);
        food_price = itemView.findViewById(R.id.food_price);
        food_image = itemView.findViewById(R.id.food_image);

        addToCartIcon = itemView.findViewById(R.id.addToCartIcon);

        view_background = itemView.findViewById(R.id.view_background);
        view_foreground = itemView.findViewById(R.id.view_foreground);


       // itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

//    @Override
//    public void onClick(View v) {
//        itemClickListener.onClick(v, getAdapterPosition(), false);
//
//    }
}
