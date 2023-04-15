package CS360.nicksinventoryapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private static final String TAG = "InventoryAdapter";
    private List<InventoryItem> inventoryItems;
    private Context mContext;
    DatabaseHelper DbH;

    @Override
    public int getItemCount() {
        return inventoryItems.size();
    }

    // Inventory items child view
    class ViewHolder extends RecyclerView.ViewHolder {

        // view references
        private InventoryItem currentItem;
        private TextView itemNameView;
        private EditText itemQuantityView;
        // database instance
        DatabaseHelper DbH;
        ImageButton minusButton, plusButton, itemOptionsButton;

        // Creating ViewHolder constructor with itemView and DatabaseHelper as parameters
        public ViewHolder(View itemView, DatabaseHelper DatabaseHelper) {
            // Calling the super constructor with itemView
            super(itemView);
            // Setting the database helper instance
            DbH = DatabaseHelper;
            // Finding the views by id
            itemNameView = itemView.findViewById(R.id.item_name);
            itemQuantityView = itemView.findViewById(R.id.item_quantity);
            minusButton = itemView.findViewById(R.id.minus_button);
            plusButton = itemView.findViewById(R.id.plus_button);
            itemOptionsButton = itemView.findViewById(R.id.item_options_button);

            // Adding a click listener to the minus button
            minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Decreasing the quantity of the current item
                    currentItem.decQuantity();
                    // Updating the item in the database and storing the result in a variable
                    boolean updated = DbH.updateItem(currentItem, mContext.getApplicationContext());
                    // If the update was successful, set the text of the quantity view to the new value
                    if (updated) {
                        itemQuantityView.setText(String.valueOf(currentItem.getQuantity()));
                    }
                }
            });

            // create a new TextWatcher and add it to the itemQuantityView
            itemQuantityView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    // update the quantity of the currentItem with the new quantity entered by the user
                    currentItem.setQuantity(getItemQuantity());
                    // update the database with the new quantity of the currentItem
                    boolean updated = DbH.updateItem(currentItem, mContext.getApplicationContext());
                    // log whether the database update was successful or not
                    Log.d(TAG, "Item quantity updated: " + updated);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // When plus button is clicked, increase the quantity of the current item by 1
                    currentItem.incQuantity();
                    // Update the item's quantity in the database
                    boolean updated = DbH.updateItem(currentItem, mContext.getApplicationContext());
                    // If the update was successful, update the quantity text view with the new value
                    if (updated) {
                        itemQuantityView.setText(String.valueOf(currentItem.getQuantity()));
                    }
                }
            });
        }

        // convert quantity from text to integer
        private int getItemQuantity() {
            String rawValue = itemQuantityView.getText().toString().replaceAll("[^\\d.]", "").trim();
            int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);

            // Quantity cannot be less than 0
            return Math.max(quantity, 0);
        }

        // add view to controller
        public void addView(InventoryItem item) {
            currentItem = item;
            Log.d("ItemHolder", "Add item: " + currentItem.getName());
            itemNameView.setText(currentItem.getName());
            itemQuantityView.setText(String.valueOf(currentItem.getQuantity()));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create an instance of the child view
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item, parent, false);
        return new ViewHolder(view, DbH);
    }

    // Constructor that takes an inventory list, a context, and a database instance
    public InventoryAdapter(List<InventoryItem> items, Context context, DatabaseHelper DatabaseHelper) {
        inventoryItems = items;
        mContext = context;
        DbH = DatabaseHelper;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the current item from the list
        InventoryItem currentItem = inventoryItems.get(position);

        // Add the current item to the view
        holder.addView(currentItem);

        // Listen for item options button click
        holder.itemOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create popup menu for options button
                PopupMenu popup = new PopupMenu(mContext, holder.itemOptionsButton);
                popup.inflate(R.menu.item_options_menu);

                // Set click listener for menu items
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_edit:
                                // Move to item info screen to edit current item
                                Log.i(TAG, "edit item at position " + position);

                                // Create intent to launch item info activity
                                Intent intent = new Intent(mContext, ItemInfoActivity.class);
                                intent.putExtra(ItemInfoActivity.EXTRA_ITEM, currentItem);
                                mContext.startActivity(intent);

                                return true;
                            case R.id.menu_remove:
                                // Delete current item
                                Log.i(TAG, "remove item at position " + position);

                                // Create deletion confirmation dialog
                                new AlertDialog.Builder(mContext)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle(R.string.delete_confirmation_title)
                                        .setMessage(R.string.delete_confirmation)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Delete the item from the database
                                                boolean deleted = DbH.deleteItem(currentItem);
                                                if (deleted) {
                                                    // Remove the item from the list and notify of deletion
                                                    inventoryItems.remove(position);
                                                    notifyItemRemoved(position);
                                                    notifyDataSetChanged();
                                                } else {
                                                    // Display error message
                                                    Toast.makeText(mContext, R.string.delete_error, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .setNegativeButton("No", null)
                                        .show();

                                return true;
                            default:
                                return false;
                        }
                    }
                });

                // Display the popup menu
                popup.show();
            }
        });
    }

}
