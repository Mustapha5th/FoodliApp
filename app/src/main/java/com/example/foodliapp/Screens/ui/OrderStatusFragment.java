package com.example.foodliapp.Screens.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodliapp.Common.Common;
import com.example.foodliapp.Model.Request;
import com.example.foodliapp.R;
import com.example.foodliapp.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class OrderStatusFragment extends Fragment {
    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager manager;
    TextView txtNotifer;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;
    FirebaseDatabase database;
    DatabaseReference requests;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
         View root = inflater.inflate(R.layout.fragment_order_status, container, false);
        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        recyclerView = root.findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);

        // swipe to refresh
        swipeRefreshLayout = root.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark,
                R.color.red,
                R.color.colorPrimaryDarkNight,
                R.color.green,
                R.color.blue
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadOrders(Common.currentUser.getPhone());
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadOrders(Common.currentUser.getPhone());
            }
        });

        return root;
    }

    private void loadOrders(String phone) {
        Query orderQuery =  requests.orderByChild("phone").equalTo(phone);
        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderQuery,Request.class).build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder orderViewHolder, int i, @NonNull Request request) {
                orderViewHolder.txtOrderId.setText(adapter.getRef(i).getKey());
                orderViewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(request.getStatus()));
                orderViewHolder.txtOrderAddress.setText(request.getAddress());
                orderViewHolder.txtOrderPhone.setText(request.getPhone());
              orderViewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      if (adapter.getItem(i).getStatus().equals("0")){
                          deleteOrder(adapter.getRef(i).getKey());
                      }else {
                          Toast.makeText(getContext(), "Sorry Order have already been processed you cannot delete the Order", Toast.LENGTH_SHORT).show();
                      }
                  }
              });
            }


            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView  = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_layout, parent,false);
                return new OrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void deleteOrder(String key) {
        requests.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), new StringBuilder("Order ").append(key).append(" has been deleted").toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}