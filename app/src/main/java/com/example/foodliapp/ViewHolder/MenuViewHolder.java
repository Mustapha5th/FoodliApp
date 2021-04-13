package com.example.foodliapp.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodliapp.Interface.ItemClickListener;
import com.example.foodliapp.R;
public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtMenuName;
    public ImageView menuImage;

    private ItemClickListener itemClickListener;

    public MenuViewHolder( View itemView) {
        super(itemView);

        txtMenuName = itemView.findViewById(R.id.menu_name);
        menuImage = itemView.findViewById(R.id.menu_image);

       itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);

    }
}
