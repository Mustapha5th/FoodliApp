package com.example.foodliapp.ViewHolder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.foodliapp.Model.Order;
import com.example.foodliapp.R;
import com.example.foodliapp.Screens.ui.CartFragment;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

    private final CartFragment cartFragment;
    private List<Order> listData = new ArrayList<>();

    public CartAdapter(List<Order> listData, CartFragment cartFragment) {
        this.listData = listData;
        this.cartFragment = cartFragment;
    }


    @NotNull
    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cartFragment.requireContext());
        View itemView = inflater.inflate(R.layout.cart_layout, parent, false);
        return  new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(""+listData.get(position).getQuantity(), Color.RED);
        holder.img_cart_item_count.setImageDrawable(drawable);

        Picasso.get().load(listData.get(position).getImage())
                .centerCrop()
                .resize(70,70)
                .into(holder.cart_image);

//        holder.btn_quantity.setNumber(listData.get(position).getQuantity());
//        holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
//            @Override
//            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
//                Order order = listData.get(position);
//                order.setQuantity(String.valueOf(newValue));
//                new Database(cartFragment.requireContext()).updateCart(order);
//
//                // update total
//                int total = 0;
//                // Calculate total price
//                List<Order> orders = new Database(cartFragment.getContext()).getCart(Common.currentUser.getPhone());
//                for (Order item:orders)
//                    total +=(Integer.parseInt(order.getPrice())) * (Integer.parseInt(item.getQuantity()));
//                Locale locale = new Locale("en","NG");
//                NumberFormat format = NumberFormat.getCurrencyInstance(locale);
//
//                cartFragment.txtTotal.setText(format.format(total));
//
//            }
//        });
        Locale locale = new Locale("en","NG");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(position).getPrice())) * (Integer.parseInt(listData.get(position).getQuantity())) ;
        holder.txt_price.setText(format.format(price));
        holder.txt_cart_name.setText(listData.get(position).getProductName());


    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
    public Order getItem(int position){
        return listData.get(position);
    }

    public void removeItem(int position){
        listData.remove(position);
        notifyItemRemoved(position);
    }
    public void restoredItem(Order item,int position){
        listData.add(position,item);
        notifyItemInserted(position);
    }
}
