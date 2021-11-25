package com.gtmf.duinomote;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;
import androidx.lifecycle.LiveData;
import java.util.List;

@Dao
public interface IRButtonDao {
	@Query("SELECT * FROM button_table WHERE remoteButtonId=:remoteId ORDER BY buttonId ASC")
	public LiveData<List<IRButton>> getButtonsByRemote(long remoteId);

    @Insert
    void insert(IRButton button);

    @Update
    void update(IRButton button);
}