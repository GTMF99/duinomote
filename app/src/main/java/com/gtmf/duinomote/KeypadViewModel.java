package com.gtmf.duinomote;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import java.util.List;

public class KeypadViewModel extends AndroidViewModel {
	private ButtonRepository repository;

    public KeypadViewModel(@NonNull Application application) {
        super(application);
        repository = new ButtonRepository(application);
    }

    public void insert(IRButton button) {
        repository.insert(button);
    }

    public void update(IRButton button) {
        repository.update(button);
    }

   	public LiveData<List<IRButton>> getAllButtonsFromRemote(int remoteId) {
		return repository.getAllButtonsFromRemote(remoteId);
	}

}