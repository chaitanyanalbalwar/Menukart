package in.menukart.menukart.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

@Dao
public interface MenuKartDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert();

}
