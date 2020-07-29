package in.menukart.menukart.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import in.menukart.menukart.entities.order.RestaurantMenu;

@Dao
public interface MenuKartDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<RestaurantMenu> restaurantMenus);

    @Query("DELETE FROM current_orders")
    void deleteAll();

    @Query("SELECT * from current_orders")
    List<RestaurantMenu> getAll();

    @Query("UPDATE current_orders SET  quantity=:quantity,isAddedToCart=:isAdded WHERE restaurant_id=:restaurantId AND menu_id =:menuId ")
    void updateItem(String restaurantId, String menuId, int quantity, boolean isAdded);

    @Query("SELECT * from current_orders WHERE quantity != 0")
    List<RestaurantMenu> getAllAddedItems();

    @Query("DELETE FROM current_orders WHERE restaurant_id =:restaurantId AND menu_id =:menuId")
    void delete(String restaurantId, String menuId);

    @Query("SELECT * from current_orders WHERE restaurant_id=:restaurantId")
    List<RestaurantMenu> getAllByRestaurantId(String restaurantId);

    @Query("DELETE FROM current_orders WHERE restaurant_id=:restaurantId")
    void deleteAllByRestaurantId(String restaurantId);
}
