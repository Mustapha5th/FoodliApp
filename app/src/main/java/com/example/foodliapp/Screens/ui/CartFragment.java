package com.example.foodliapp.Screens.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodliapp.Common.Common;
import com.example.foodliapp.Common.Config;
import com.example.foodliapp.Database.Database;
import com.example.foodliapp.Helper.RecyclerItemTouchHelper;
import com.example.foodliapp.Interface.RecyclerItemTouchHelperListener;
import com.example.foodliapp.Model.MyResponse;
import com.example.foodliapp.Model.Notification;
import com.example.foodliapp.Model.Order;
import com.example.foodliapp.Model.Request;
import com.example.foodliapp.Model.Sender;
import com.example.foodliapp.Model.Token;
import com.example.foodliapp.Model.User;
import com.example.foodliapp.R;
import com.example.foodliapp.Remote.APIService;
import com.example.foodliapp.ViewHolder.CartAdapter;
import com.example.foodliapp.ViewHolder.CartViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class CartFragment extends Fragment implements RecyclerItemTouchHelperListener {
    // PayPal payment
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) // using sandbox
            .clientId(Config.PAY_CLIENT_ID);
    private final int PAYPAL_REQUEST_CODE = 500;
    public TextView txtTotal;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager manager;
    FirebaseDatabase database;
    DatabaseReference requests;
    Button btnPlaceOrder;
    ImageButton btnDelete;
    APIService mService;
    SwipeRefreshLayout swipeRefreshLayout;
    String address, comment;
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    RelativeLayout rootLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
         View root = inflater.inflate(R.layout.fragment_cart, container, false);


         // init Paypal
        Intent intent = new Intent(getContext(), PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        getActivity().startService(intent);
         // Init Service
        mService = Common.getFCMService();
        // Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        rootLayout = root.findViewById(R.id.rootLayout);

         // Init
        recyclerView = root.findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelper = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView);


        txtTotal = root.findViewById(R.id.txtTotal);
        btnPlaceOrder = root.findViewById(R.id.btnPlaceOrder);

        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();

        return root;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null){
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);

                // create new request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        address,
                        txtTotal.getText().toString(),
                        "0",
                        comment,
                        "Paypal",
                        jsonObject.getJSONObject("response").getString("state"),
                        cart
                );

                // Submit to Firebase
                // Using System.currentMilli to key
                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);
                //Delete cart
                new Database(requireContext()).cleanCart(Common.currentUser.getPhone());
                sendNotificationOrder(order_number);


                        Toast.makeText(getContext(), "Thank You, Order is Placed", Toast.LENGTH_SHORT).show();
                        getActivity().recreate();

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        }else if (resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(getContext(), "Payment cancelled", Toast.LENGTH_SHORT).show();

        }else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
            Toast.makeText(getContext(), "Invalid payment", Toast.LENGTH_SHORT).show();

        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme);
        alertDialog.setTitle("One More Step!");
        alertDialog.setMessage("Enter your address: ");

      LayoutInflater inflater = this.getLayoutInflater();
      View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);
        MaterialEditText edtAddress = order_address_comment.findViewById(R.id.edtAddress);
        MaterialEditText edtComment = order_address_comment.findViewById(R.id.edtComment);

        RadioButton rdiCOD = order_address_comment.findViewById(R.id.rdiCOD);
        RadioButton rdiPaypal = order_address_comment.findViewById(R.id.rdiPaypal);
        RadioButton rdiFoodliBalance = order_address_comment.findViewById(R.id.rdiFoodliBalance);

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_baseline_shopping_cart);
        alertDialog.setPositiveButton(Html.fromHtml("<font color= '#DE8405'>Yes</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // show Paypal payment
                address = edtAddress.getText().toString();
                comment = edtComment.getText().toString();
                if (address.isEmpty() ){
                    Toast.makeText(getContext(), "Please enter your delivery address", Toast.LENGTH_LONG).show();
                }else {
                    if (!rdiCOD.isChecked() && !rdiPaypal.isChecked() && !rdiFoodliBalance.isChecked()) {
                        Toast.makeText(getContext(), "Please select payment option", Toast.LENGTH_LONG).show();
                    }
                    else if (rdiPaypal.isChecked()) {
                        String formatAmount = txtTotal.getText().toString()
                                .replace("₦", "")
                                .replace(",", "");

                        PayPalPayment payment = new PayPalPayment(new BigDecimal(formatAmount), "USD",
                                "Foodli App Order",
                                PayPalPayment.PAYMENT_INTENT_SALE);
                        Intent intent = new Intent(getActivity().getApplicationContext(), PaymentActivity.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
                        startActivityForResult(intent, PAYPAL_REQUEST_CODE);

                    }
                    else if (rdiCOD.isChecked()) {
                        String formatAmount = txtTotal.getText().toString()
                                .replace("₦", "")
                                .replace(",", "");
                        double amount = 0;
                        // get total price
                        amount = Double.parseDouble(formatAmount);
                        // create new request
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                address,
                                formatAmount,
                                Common.currentUser.getName(),
                                "0",
                                "Unpaid",
                                "Foodli Balance",
                                comment,
                                cart
        );
                        // Submit to Firebase
        // Using System.currentMilli to key
                        String order_number = String.valueOf(System.currentTimeMillis());
                        requests.child(order_number)
                                .setValue(request);
                        //Delete cart
                        new Database(requireContext()).cleanCart(Common.currentUser.getPhone());
                        sendNotificationOrder(order_number);
                        Toast.makeText(getContext(), "Thank You, Order is Placed", Toast.LENGTH_SHORT).show();
                        requireActivity().recreate();

    }
                    else if (rdiFoodliBalance.isChecked()){
                        String formatAmount = txtTotal.getText().toString()
                                .replace("₦", "")
                                .replace(",", "");
                        double amount = 0;
                        // get total price
                         amount = Double.parseDouble(formatAmount);
                        // Check user balance
                        if (Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount){
                            // create new request
                            Request request = new Request(
                                    Common.currentUser.getPhone(),
                                    address,
                                    formatAmount,
                                    Common.currentUser.getName(),
                                    "0",
                                    "Paid",
                                    "Foodli Balance",
                                    comment,
                                    cart

                            );
                            // Submit to Firebase
                            // Using System.currentMilli to key
                            String order_number = String.valueOf(System.currentTimeMillis());
                            requests.child(order_number)
                                    .setValue(request);
                            //Delete cart
                            new Database(requireContext()).cleanCart(Common.currentUser.getPhone());
                            // update balance
                            double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
                            Map<String ,Object> update_balance = new HashMap<>();
                            update_balance.put("balance", balance);
                            FirebaseDatabase.getInstance()
                                    .getReference("User")
                                    .child(Common.currentUser.getPhone())
                                    .updateChildren(update_balance)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                // refresh user
                                                FirebaseDatabase.getInstance()
                                                        .getReference("User")
                                                        .child(Common.currentUser.getPhone())
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                Common.currentUser = snapshot.getValue(User.class);
                                                                sendNotificationOrder(order_number);
                                                                Toast.makeText(getContext(), "Thank You, Order is Placed", Toast.LENGTH_SHORT).show();

                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                            }
                                        }
                                    });



                        }
                                else {
                    Toast.makeText(getContext(), "You have insufficient balance", Toast.LENGTH_LONG).show();
                }
    }

}

            }
        });


        alertDialog.setNegativeButton(Html.fromHtml("<font color= '#DE8405'>No</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void sendNotificationOrder(String order_number) {
        DatabaseReference tokens =  FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true); // get all node with token true

        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot:snapshot.getChildren()){
                    Token serverToken = postSnapshot.getValue(Token.class);
                    Notification notification = new Notification("FoodLi", "You have a new order"+order_number);
                    Sender sender = new Sender(serverToken.getToken(),notification);
                    mService.sendNotification(sender)
                    .enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success == 1) {
                                    Toast.makeText(getContext(), "Thank You, Order is Placed", Toast.LENGTH_SHORT).show();


                                } else {
                                    Toast.makeText(getContext(), "Failed to place order!", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                            Log.e("ERROR", t.getMessage());
                        }
                    });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadListFood() {
        cart = new Database(requireContext()).getCart(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        int total = 0;
        // Calculate total price
        for (Order order:cart)
            total +=(Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
            Locale locale = new Locale("en","NG");
             NumberFormat format = NumberFormat.getCurrencyInstance(locale);

        txtTotal.setText(format.format(total));
       // swipeRefreshLayout.setRefreshing(false);

    }
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder){
            String name =  ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();
            Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            int deleteIndex = viewHolder.getAdapterPosition();
            adapter.removeItem(deleteIndex);
            new Database(getContext()).deleteFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());
            //
            // update total
            int total = 0;
            // Calculate total price
            List<Order> orders = new Database(getContext()).getCart(Common.currentUser.getPhone());
            for (Order item:orders)
                total +=(Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en","NG");
            NumberFormat format = NumberFormat.getCurrencyInstance(locale);

            txtTotal.setText(format.format(total));

            Snackbar snackbar = Snackbar.make(rootLayout, name +" removed from cart", Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(R.id.nav_view);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoredItem(deleteItem,deleteIndex);
                    new Database(getContext()).addToCart(deleteItem);
                    // update total
                    int total = 0;
                    // Calculate total price
                    List<Order> orders = new Database(getContext()).getCart(Common.currentUser.getPhone());
                    for (Order item:orders)
                        total +=(Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en","NG");
                    NumberFormat format = NumberFormat.getCurrencyInstance(locale);

                    txtTotal.setText(format.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}