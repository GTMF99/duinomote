package com.gtmf.duinomote;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;
import java.util.List;

@Dao
public interface RemoteDao {
    @Insert
    void insert(Remote remote);

    @Update
    void update(Remote remote);

    @Delete
    void delete(Remote remote);

   	@Query("SELECT * FROM remote_table ORDER BY id ASC")
   	LiveData<List<Remote>> getAllRemotes();
}