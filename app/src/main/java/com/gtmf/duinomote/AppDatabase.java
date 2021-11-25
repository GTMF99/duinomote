package com.gtmf.duinomote;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;


@Database(entities = {Remote.class, IRButton.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RemoteDao remoteDao();
    public abstract IRButtonDao irButtonDao();
    private static AppDatabase INSTANCE;

	public static synchronized AppDatabase getInstance(Context context) {
	    if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "app_database")
            		.fallbackToDestructiveMigration()
                    .build();
	    }
	    return INSTANCE;
	}
}