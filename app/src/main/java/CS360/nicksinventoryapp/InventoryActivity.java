package CS360.nicksinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {
    private static final String TAG = "InventoryActivity";
    private List<InventoryItem> inventoryItems;
    DatabaseHelper DbH;
    RecyclerView inventoryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the activity transition animation
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        // Set the activity layout
        setContentView(R.layout.activity_inventory);

        // Initialize the database and fetch all inventory items
        DbH = DatabaseHelper.getInstance(getApplicationContext());
        inventoryItems = DbH.getItems();

        // Set the layout manager for the RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        // Get a reference to the RecyclerView
        inventoryView = findViewById(R.id.inventory_recycler_view);

        // Set the layout manager for the RecyclerView
        inventoryView.setLayoutManager(mLayoutManager);

        // Add dividers between RecyclerView items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                inventoryView.getContext(), ((LinearLayoutManager) mLayoutManager).getOrientation());
        inventoryView.addItemDecoration(dividerItemDecoration);

        // Create an adapter for the RecyclerView
        InventoryAdapter adapter = new InventoryAdapter(inventoryItems, this, DbH);

        // Register an observer to listen for changes in the adapter's data
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });

        // Set the adapter for the RecyclerView
        inventoryView.setAdapter(adapter);

        // Get a reference to the add item FAB
        FloatingActionButton addItemButton = findViewById(R.id.add_item_button);

        // Set an onClickListener on the add item FAB to open the new item activity
        addItemButton.setOnClickListener(v -> {
            Log.d(TAG, "New item view");
            Intent intent = new Intent(getApplicationContext(), ItemInfoActivity.class);
            startActivity(intent);
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Show the app bar menu
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    /**
     * Called when an options menu item is selected
     * @param item The selected menu item
     * @return true if the menu item was handled successfully, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            // Handle the toggle notifications menu option
            case R.id.action_toggle_notifications:
                // Switch to the notifications setting screen
                Log.d(TAG, "Navigating to Notifications Settings");
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            // Handle the logout menu option
            case R.id.action_logout:
                // Log the user out by returning to the login screen
                Log.d(TAG, "Logging out");
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                return true;
            // Handle any other menu options by calling the superclass implementation
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
