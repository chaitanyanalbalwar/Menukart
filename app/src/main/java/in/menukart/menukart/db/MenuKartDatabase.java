package in.menukart.menukart.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.menukart.menukart.entities.order.RestaurantMenu;

@Database(entities = {RestaurantMenu.class}, version = 1, exportSchema = false)
public abstract class MenuKartDatabase extends RoomDatabase {

    public static final String DB_NAME = "menukart_db";
    public static final String TABLE_NAME_ORDERS = "current_orders";
    private static MenuKartDatabase INSTANCE;

    public abstract MenuKartDao menuKartDao();

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(10);

    public static MenuKartDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MenuKartDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MenuKartDatabase.class, DB_NAME)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
