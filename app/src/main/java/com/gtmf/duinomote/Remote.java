package com.gtmf.duinomote;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

@Entity(tableName = "remote_table")
public class Remote {
	@PrimaryKey(autoGenerate = true)
	@NonNull
	@ColumnInfo(name = "id")
	int id;

	@ColumnInfo(name = "name")
	@NonNull
	String name;

	@ColumnInfo(name = "category")
	@NonNull
	String category;

	public Remote(String name, String category) {
		this.name = name;
		this.category = category;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}
}