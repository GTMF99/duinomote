package com.gtmf.duinomote;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import java.util.List;

public class RemoteListViewModel extends AndroidViewModel {
    private RemoteRepository repository;
    private LiveData<List<Remote>> allRemotes;

    public RemoteListViewModel(@NonNull Application application) {
        super(application);
        repository = new RemoteRepository(application);
        allRemotes = repository.getAllRemotes();
    }

    public void insert(Remote remote) {
        repository.insert(remote);
    }

    public void update(Remote remote) {
        repository.update(remote);
    }

    public void delete(Remote remote) {
        repository.delete(remote);
    }

    public LiveData<List<Remote>> getAllRemotes() {
        return allRemotes;
    }

}