package com.example.foodliapp.Screens.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodliapp.Common.Common;
import com.example.foodliapp.Database.Database;
import com.example.foodliapp.Interface.ItemClickListener;
import com.example.foodliapp.Model.Favorites;
import com.example.foodliapp.Model.Food;
import com.example.foodliapp.Model.Order;
import com.example.foodliapp.R;
import com.example.foodliapp.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager manager;


    FirebaseDatabase database;
    DatabaseReference foodList;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    String categoryId = "";

    // Search Food Functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar searchFood;

    // Favorites
    Database localDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        // Local DB
        localDB = new Database(this);

        recyclerView = findViewById(R.id.recycler_search);
        recyclerView.setHasFixedSize(true);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        // Search Functionality
        searchFood = findViewById(R.id.searchBar);
        loadSuggest(); // function to load suggested food

        searchFood.setCardViewElevation(10);
        searchFood.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Change Suggestion List When user type their text,

                List<String> suggest = new ArrayList<>();
                for (String search:suggestList){ // Loop in SuggestList
                    if (search.toLowerCase().contains(searchFood.getText().toLowerCase()))
                        suggest.add(search);
                }
                searchFood.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchFood.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //  When Search bar is closed
                // Restore Init Suggest adapter
                if (!enabled)
                    recyclerView.setAdapter(adapter);

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                // When Search is confirmed
                // show result of search adapter
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        //load all food
        loadAllFood();

    }

    private void startSearch(CharSequence text) {
        Query searchQuery = foodList.orderByChild("name").equalTo(text.toString());
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchQuery,Food.class).build();
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, int position, @NonNull Food food) {
                foodViewHolder.food_name.setText(food.getName());
                Picasso.get().load(food.getImage()).into(foodViewHolder.food_image);

                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Start new Activity
                        Intent foodDetail = new Intent(SearchActivity.this, FoodDetail.class);
                        foodDetail.putExtra("categoryId", searchAdapter.getRef(position).getKey());// send food id to new activity
                        startActivity(foodDetail);
                    }
                });
            }


            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView  = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        searchAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(searchAdapter);// SET ADAPTER FOR RECYCLER VIEW
    }

    private void loadSuggest() {
        foodList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Food item = dataSnapshot.getValue(Food.class);
                    suggestList.add(item.getName()); // Add name off food to the suggested list
                }
                searchFood.setLastSuggestions(suggestList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllFood() {
        Query searchQuery = foodList;
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchQuery,Food.class).build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, int i, @NonNull Food food) {
                foodViewHolder.food_name.setText(food.getName());
                foodViewHolder.food_price.setText(String.format("₦ %s", food.getPrice()));
                Picasso.get().load(food.getImage()).into(foodViewHolder.food_image);

                foodViewHolder.addToCartIcon.setImageResource(R.drawable.ic_baseline_add_shopping_cart);

                foodViewHolder.addToCartIcon.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        boolean isExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(i).getKey(), Common.currentUser.getPhone());
                        if(!isExists){
                            new Database(getBaseContext()).addToCart(new Order(
                                            Common.currentUser.getPhone(),
                                            adapter.getRef(i).getKey(),
                                            food.getName(),
                                            "1",
                                            food.getPrice(),
                                            food.getDiscount(),
                                            food.getImage()
                                    )
                            );
                        }else {
                            new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(i).getKey());

                        }
                        Toast.makeText(getBaseContext(), "Added to Cart", Toast.LENGTH_SHORT).show();

                    }



                });

                //add favorites
                if (localDB.isFavorites(adapter.getRef(i).getKey(), Common.currentUser.getPhone())){
                    foodViewHolder.favIcon.setImageResource(R.drawable.ic_baseline_favorite);
                }
                foodViewHolder.favIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Favorites favorites = new Favorites();

                        favorites.setFoodId(adapter.getRef(i).getKey());
                        favorites.setFoodName(food.getName());
                        favorites.setFoodDescription(food.getDescription());
                        favorites.setFoodDiscount(food.getDiscount());
                        favorites.setFoodImage(food.getImage());
                        favorites.setFoodMenuId(food.getMenuId());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setFoodPrice(food.getPrice());

                        if (!localDB.isFavorites(adapter.getRef(i).getKey(), Common.currentUser.getPhone())){
                            localDB.addToFavorites(favorites);
                            foodViewHolder.favIcon.setImageResource(R.drawable.ic_baseline_favorite);
                            Toast.makeText(SearchActivity.this, ""+food.getName()+" was added to favorites", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            localDB.deleteFromFavorites(adapter.getRef(i).getKey(), Common.currentUser.getPhone());
                            foodViewHolder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_border);
                            Toast.makeText(SearchActivity.this, ""+food.getName()+" was removed from favorites", Toast.LENGTH_SHORT).show();

                        }
                    }
                });


                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Start new Activity
                        Intent foodDetail = new Intent(SearchActivity.this, FoodDetail.class);
                        foodDetail.putExtra("CategoryId", adapter.getRef(position).getKey());// send food id to new activity
                        startActivity(foodDetail);
                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View itemView  = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        // set Adapter
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
        if (searchAdapter != null)
            searchAdapter.stopListening();

    }
}