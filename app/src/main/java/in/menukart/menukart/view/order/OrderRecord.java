package in.menukart.menukart.view.order;

import java.util.ArrayList;
import java.util.List;

import in.menukart.menukart.entities.order.RestaurantMenu;

public class OrderRecord {

    public static List<RestaurantMenu> restaurantMenus;

    public static List<RestaurantMenu> getOrderList(){
        if(restaurantMenus == null){
            restaurantMenus = new ArrayList<>();
        }
        return restaurantMenus;
    }
}
