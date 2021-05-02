package com.example.foodliapp.Screens.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodliapp.Database.Database;
import com.example.foodliapp.R;
import com.example.foodliapp.ViewHolder.FavoritesAdapter;
import com.google.firebase.database.FirebaseDatabase;

public class FavoriteFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager manager;



    FavoritesAdapter adapter;
    RelativeLayout rootLayout;
    FirebaseDatabase database;

    // Favorites
    Database localDB;


    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorite, container, false);
        rootLayout = root.findViewById(R.id.root_layout);

        recyclerView = root.findViewById(R.id.recycler_fav);
        recyclerView.setHasFixedSize(true);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),R.anim.from_left);
        recyclerView.setLayoutAnimation(controller);

//        //Swipe to delete
//        ItemTouchHelper.SimpleCallback itemTouchHelper = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT, this);
//        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView);

        return root;

    }

}