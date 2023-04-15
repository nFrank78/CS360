package CS360.nicksinventoryapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

public class ItemInfoActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM = "CS360.nicksinventoryapp.item";
    private EditText itemName;
    private TextView itemQuantity;
    private DatabaseHelper currentDatabase;
    private InventoryItem currentItem;
    Button saveButton, deleteButton, minusButton, plusButton;
    private Context context;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_item_info);

        // Set the instance of the database
        currentDatabase = DatabaseHelper.getInstance(this);

        // Find the views in the layout
        itemName = findViewById(R.id.item_name_edittext);
        itemQuantity = findViewById(R.id.quantity_value_edittext);
        minusButton = findViewById(R.id.minus_button);
        plusButton = findViewById(R.id.plus_button);
        deleteButton = findViewById(R.id.delete_button);
        saveButton = findViewById(R.id.save_button);

        // start new items at 0
        int currentQuantity = 0;

        // If not a new item, get item info
        InventoryItem item = (InventoryItem) getIntent().getSerializableExtra(EXTRA_ITEM);
        if (item != null) {
            currentItem = item;
            itemName.setText(item.getName());
            currentQuantity = item.getQuantity();
        }

        // Set the item name, quantity and item info in the views
        itemQuantity.setText(Integer.toString(currentQuantity));

        // Set click listeners for minus and plus buttons to adjust quantity
        minusButton.setOnClickListener(v -> itemQuantity.setText(String.valueOf(Math.max(0, getItemQuantity() - 1))));

        plusButton.setOnClickListener(v -> itemQuantity.setText(String.valueOf(getItemQuantity() + 1)));
    }

    // get the currentItem's name
    private String getItemName() {
        Editable name = itemName.getText();
        return name != null ? name.toString().trim() : "";
    }

    // get the currentItem's quantity
    private int getItemQuantity() {
        String rawValue = itemQuantity.getText().toString().replaceAll("[^\\d.]", "").trim();
        int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);

        // Quantity cannot be less than 0
        return Math.max(quantity, 0);
    }

    // Delete item from database after clicking deleteButton
    public void deleteItem(View view) {
        // Wrap the delete action in a confirmation dialog only reacting to 'yes'
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete_confirmation_title).setMessage(R.string.delete_confirmation)
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the item from the database
                    boolean deleted = currentDatabase.deleteItem(currentItem);
                    finish();

                    // if deleted then return to previous screen
                    if (deleted) {
                        NavUtils.navigateUpFromSameTask(ItemInfoActivity.this);
                    }
                    // else show error
                    else {
                        Toast.makeText(ItemInfoActivity.this, R.string.delete_error, Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("No", null).show();
    }

    // Save item to database after clicking saveButton
    public void saveItem(View view) {
        boolean saved;

        // If editing then update the item in database
        if (currentItem != null) {
            currentItem.setName(getItemName());
            currentItem.setQuantity(getItemQuantity());
            saved = currentDatabase.updateItem(currentItem, context.getApplicationContext());
        }
        else {
            // Create a new item in the database
            saved = currentDatabase.addItem(getItemName(), getItemQuantity());
        }

        // If save is successful, go to previous screen
        if (saved) {
            NavUtils.navigateUpFromSameTask(this);
        }
        // else give error message
        else {
            Toast.makeText(ItemInfoActivity.this, R.string.save_error, Toast.LENGTH_SHORT).show();
        }
    }

    public ItemInfoActivity(Context context) {
        this.context = context;
    }
    public ItemInfoActivity() {
    }
}
