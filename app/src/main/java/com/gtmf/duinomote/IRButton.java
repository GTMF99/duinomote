package com.gtmf.duinomote;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;
import androidx.room.ForeignKey;

@Entity(
	tableName = "button_table",
	foreignKeys = @ForeignKey(
		entity = Remote.class,
		parentColumns = "id",
		childColumns = "remoteButtonId",
		onDelete = ForeignKey.CASCADE
	)
)
public class IRButton {
	@PrimaryKey(autoGenerate = true)
	@NonNull
	@ColumnInfo(name = "buttonId")
	int buttonId;

	@NonNull
	@ColumnInfo(name = "remoteButtonId")
	int remoteButtonId;

	@NonNull
	@ColumnInfo(name = "signal")
	String signal;

	@NonNull
	@ColumnInfo(name = "posX")
	int posX;

	@NonNull
	@ColumnInfo(name = "posY")
	int posY;

	public IRButton(String signal, int posX, int posY) {
		this.signal = signal;
		this.posX = posX;
		this.posY = posY;
	}

	public void setButtonId(int buttonId) {
		this.buttonId = buttonId;
	}

	public void setRemoteButtonId(int remoteButtonId) {
		this.remoteButtonId = remoteButtonId;
	}

	public String getButtonSignal() {
		return this.signal;
	}

	public int getButtonId() {
		return this.buttonId;
	}

	public int getRemoteId() {
		return this.remoteButtonId;
	}

	public int getPosX() {
		return this.posX;
	}

	public int getPosY() {
		return this.posY;
	}

	public void setPosX(int position) {
		this.posX = position;
	}

	public void setPosY(int position) {
		this.posY = position;
	}
}