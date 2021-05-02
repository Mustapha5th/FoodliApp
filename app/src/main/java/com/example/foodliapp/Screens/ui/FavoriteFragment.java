package com.example.foodliapp.Screens.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodliapp.Common.Common;
import com.example.foodliapp.Database.Database;
import com.example.foodliapp.Helper.RecyclerItemTouchHelper;
import com.example.foodliapp.Interface.RecyclerItemTouchHelperListener;
import com.example.foodliapp.Model.Favorites;
import com.example.foodliapp.R;
import com.example.foodliapp.ViewHolder.FavoritesAdapter;
import com.example.foodliapp.ViewHolder.FavoritesViewHolder;
import com.google.android.material.snackbar.Snackbar;

public class FavoriteFragment extends Fragment implements RecyclerItemTouchHelperListener {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager manager;

    FavoritesAdapter adapter;
    RelativeLayout rootLayout;

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

//        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),R.anim.from_left);
//        recyclerView.setLayoutAnimation(controller);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelper = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView);

        loadFavorites();
        return root;
    }

    private void loadFavorites() {
        adapter = new FavoritesAdapter(getContext(),new Database(getContext()).getAllFavorites(Common.currentUser.getPhone()));
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoritesViewHolder){
            String name = ((FavoritesAdapter)recyclerView.getAdapter()).getItem(position).getFoodName();
            Favorites deleteItem = ((FavoritesAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            int deleteIndex = viewHolder.getAdapterPosition();
            adapter.removeItem(viewHolder.getAdapterPosition());
            new Database(getContext()).deleteFromFavorites(deleteItem.getFoodId(), Common.currentUser.getPhone());

            Snackbar snackbar = Snackbar.make(rootLayout, name +" removed from cart", Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(R.id.nav_view);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoredItem(deleteItem,deleteIndex);
                    new Database(getContext()).addToFavorites(deleteItem);

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

        }
    }
}