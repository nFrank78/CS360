package CS360.nicksinventoryapp;

import java.io.Serializable;

public class InventoryItem implements Serializable {
    private int itemId;
    private String itemName;
    private int itemQuantity;
    private int userId;


    // default constructor
    public InventoryItem() {
    }

    // Constructor with full parameters
    public InventoryItem(int id, String name, int quantity, int userID) {
        itemId = id;
        itemName = name;
        itemQuantity = quantity;
        userId = userID;
    }

    public InventoryItem(int id, String name, int quantity) {
        itemId = id;
        itemName = name;
        itemQuantity = quantity;
    }

    // increment quantity
    public void incQuantity() {
        this.itemQuantity++;
    }
    // decrement quantity but does not allow negative
    public void decQuantity() {
        this.itemQuantity = Math.max(0, this.itemQuantity - 1);
    }


    // getters
    public int getId() {
        return itemId;
    }
    public String getName() {
        return itemName;
    }
    public int getQuantity() {
        return itemQuantity;
    }
    public long getUserId(){ return userId;}


    // setters
    public void setId(int id) {
        this.itemId = id;
    }
    public void setName(String name) {
        this.itemName = name;
    }
    public void setUserId(int userId){ this.userId = userId;}
    // Prevent a negative quantity in its setter
    public void setQuantity(int quantity) {
        this.itemQuantity = Math.max(0, quantity);
    }
}
