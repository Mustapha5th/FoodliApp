package com.example.foodliapp.Screens.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.foodliapp.Common.Common;
import com.example.foodliapp.Database.Database;
import com.example.foodliapp.Model.Food;
import com.example.foodliapp.Model.Order;
import com.example.foodliapp.Model.Rating;
import com.example.foodliapp.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {
    TextView food_name, food_price, food_description;
    ImageView img_food;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton fbtnCart, fbtnRating;
    ElegantNumberButton numberButton;

    RatingBar ratingBar;

    String foodId="";
    FirebaseDatabase database;
    DatabaseReference foods, ratingTbl;
    Food currentFood;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        // Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");

        ratingTbl = database.getReference("Rating");

        //Init view
        numberButton = findViewById(R.id.number_button);


        fbtnRating = findViewById(R.id.fbtnRating);
        ratingBar = findViewById(R.id.ratingBar);

        fbtnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        fbtnCart = findViewById(R.id.fbtnCart);
        fbtnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                                foodId,
                                currentFood.getName(),
                                numberButton.getNumber(),
                                currentFood.getPrice(),
                                currentFood.getDiscount(),
                                currentFood.getImage()

                        )
                );

                Toast.makeText(FoodDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        food_description = findViewById(R.id.food_description);
        food_price = findViewById(R.id.food_price);
        food_name = findViewById(R.id.food_name);
        img_food = findViewById(R.id.img_food);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        // Get Food id from intent
        if (getFragmentManager() != null)
            foodId = getIntent().getStringExtra("CategoryId");
        if (!foodId.isEmpty()){
            if (Common.isConnectedToInternet(getBaseContext())){
            getDetailFood(foodId);
            getRatingFood(foodId);
            } else {

                // TO DO: convert to snack bar ...
                Toast.makeText(FoodDetail.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }

    private void getRatingFood(String foodId) {
        Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count =0, sum = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot:snapshot.getChildren()){
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum += Integer.parseInt(item.getRatingValue());
                    count++;
                }
                if (count!=0){
                    float average = sum/count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good", "Quite Ok", "Very Good", "Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorPrimaryDark)
                .setCommentTextColor(R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();
    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentFood = snapshot.getValue(Food.class);

                //Set Image
                Picasso.get().load(currentFood.getImage()).into(img_food);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled( DatabaseError error) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {
        // Get Rating and upload to firebase

        Rating rating = new Rating(Common.currentUser.getPhone(),foodId,String.valueOf(value), comments);

        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(Common.currentUser.getPhone()).exists()){
                    // remove old value
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    // Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);


                }else {
                    // Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);

                }
                Toast.makeText(FoodDetail.this, "Thank you for rating us!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}