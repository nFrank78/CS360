package CS360.nicksinventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventoryApp.db";
    private static DatabaseHelper inventoryDatabase;
    private Context context;

    // get Singleton or create new
    public static DatabaseHelper getInstance(Context context) {
        Log.i(LOG, "Get instance of database");
        if (inventoryDatabase == null) {
            inventoryDatabase = new DatabaseHelper(context);
        }
        return inventoryDatabase;
    }

    // make private singleton
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // users table
    private static final class UsersTable {
        private static final String TABLE = "users";
        private static final String COL_ID = "_id";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
    }

    // inventory table
    private static final class InventoryTable {
        private static final String TABLE = "inventory";
        private static final String COL_ID = "_id";
        private static final String COL_NAME = "name";
        private static final String COL_QUANTITY = "quantity";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Log that the database is being created
        Log.i(LOG, "Create database");

        // Create InventoryTable
        db.execSQL("CREATE TABLE " + InventoryTable.TABLE + " (" +
                InventoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryTable.COL_NAME + " TEXT, " +
                InventoryTable.COL_QUANTITY + " INTEGER)");

        // Create UsersTable
        db.execSQL("CREATE TABLE " + UsersTable.TABLE + " (" +
                UsersTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UsersTable.COL_USERNAME + " TEXT, " +
                UsersTable.COL_PASSWORD + " TEXT)");
    }


    /**
     * Upgrades the database to a new version by dropping the existing tables and creating new ones.
     * @param db the database to be upgraded
     * @param oldVersion the current version of the database
     * @param newVersion the new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the existing tables
        db.execSQL("DROP TABLE IF EXISTS " + UsersTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + InventoryTable.TABLE);

        // Recreate the tables with the new schema
        onCreate(db);
    }

    /**
     * Retrieves all inventory items from the database and returns them as a list.
     * @return a list of InventoryItem objects
     */
    public List<InventoryItem> getItems() {
        List<InventoryItem> items = new ArrayList<InventoryItem>();
        SQLiteDatabase db = getReadableDatabase();

        // Select all rows from the inventory table
        String sql = "SELECT * FROM " + InventoryTable.TABLE;
        Cursor cursor = db.rawQuery(sql, new String[]{});

        // Iterate over the cursor results and create an InventoryItem object for each row
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int quantity = cursor.getInt(2);
                items.add(new InventoryItem(id, name, quantity));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return items;
    }


    // Create user checking if it already exists and not allowing duplicate
    public boolean createUser(String username, String password) {
        // Guard against registering an existing user
        if (usernameTaken(username)) {
            return false;
        }

        // Fetch an instance of database to write to
        SQLiteDatabase db = this.getWritableDatabase();

        // Set the username and password values to insert into their columns
        ContentValues values = new ContentValues();
        values.put(UsersTable.COL_USERNAME, username);
        values.put(UsersTable.COL_PASSWORD, password);

        // Insert row
        long userId = db.insert(UsersTable.TABLE, null, values);

        // Check if there was a user ID returned to indicate success.
        return userId != -1;
    }

    /**
     * Validates a user's credentials by checking if the given username and password are present in the database.
     * @param username the username to check
     * @param password the password to check
     * @return true if the user is valid, false otherwise
     */
    public boolean validateUser(String username, String password) {
        // Get a writable database instance
        SQLiteDatabase db = this.getWritableDatabase();

        // Define the SQL query to select a user with the given username and password
        String sql = "SELECT * FROM " + UsersTable.TABLE + " WHERE username = ? AND password = ?";

        // Execute the query with the given parameters
        Cursor cursor = db.rawQuery(sql, new String[]{username, password});

        // Check if any rows were returned by the query (i.e. the user is valid)
        return cursor.getCount() > 0;
    }

    /**
     * Check if a given username already exists in the database.
     * @param username The username to check for existence in the database.
     * @return true if the username already exists in the database, false otherwise.
     */
    public boolean usernameTaken(String username) {
        // Get a writable database instance.
        SQLiteDatabase db = this.getWritableDatabase();

        // Construct the SQL query to select all rows from the UsersTable where the username matches the given username.
        String sql = "SELECT * FROM " + UsersTable.TABLE + " WHERE username = ?";

        // Execute the SQL query with the given username as a parameter.
        Cursor cursor = db.rawQuery(sql, new String[]{username});

        // If the resulting cursor has any rows, the username is already taken, so return true. Otherwise, return false.
        return cursor.getCount() > 0;
    }


    // add item to database
    public boolean addItem(String name, int quantity) {
        // Get database instance
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if an item with the same name already exists in the database
        Cursor cursor = db.query(InventoryTable.TABLE, new String[]{InventoryTable.COL_NAME},
                InventoryTable.COL_NAME + " = ?", new String[]{name}, null, null, null);

        if (cursor.getCount() > 0) {
            // Item with the same name already exists, don't add it
            return false;
        }

        // Set the values based on columns
        ContentValues values = new ContentValues();
        values.put(InventoryTable.COL_NAME, name);
        values.put(InventoryTable.COL_QUANTITY, quantity);

        // Insert row
        long itemId = db.insert(InventoryTable.TABLE, null, values);

        return itemId != -1;
    }

    // update item in database
    public boolean updateItem(InventoryItem item, Context context) {
        // Get database instance
        SQLiteDatabase db = this.getWritableDatabase();

        // Set values based on columns
        ContentValues values = new ContentValues();
        values.put(InventoryTable.COL_NAME, item.getName());
        values.put(InventoryTable.COL_QUANTITY, item.getQuantity());

        // Update the item and verify successful update
        int rowsUpdated = db.update(InventoryTable.TABLE, values, InventoryTable.COL_ID + " = ?",
                new String[]{String.valueOf(item.getId())});

        if (rowsUpdated > 0 && item.getQuantity() <= 1) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean notifications = sharedPrefs.getBoolean(SettingsActivity.NOTIFICATIONS_PREFERENCE, false);
            if (notifications) {
                Toast.makeText(context, "Low inventory for " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        }

        return rowsUpdated > 0;
    }

    // delete item from database
    public boolean deleteItem(InventoryItem item) {
        // Get database instance
        SQLiteDatabase db = getWritableDatabase();

        // Delete the item by ID
        int rowsDeleted = db.delete(InventoryTable.TABLE, InventoryTable.COL_ID + " = ?",
                new String[]{String.valueOf(item.getId())});

        // Check that it was deleted
        return rowsDeleted > 0;
    }
}
