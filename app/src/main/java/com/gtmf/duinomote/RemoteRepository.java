package com.gtmf.duinomote;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;
import java.util.List;

public class RemoteRepository {
	RemoteDao mRemoteDao;
	LiveData<List<Remote>> allRemotes;

	public RemoteRepository (Application app) {
		AppDatabase database = AppDatabase.getInstance(app);
		mRemoteDao = database.remoteDao();
		allRemotes = mRemoteDao.getAllRemotes();
	}

	public void insert(Remote remote) {
        new InsertRemoteAsyncTask(mRemoteDao).execute(remote);
	}

	public void update(Remote remote) {
		new UpdateRemoteAsyncTask(mRemoteDao).execute(remote);
	}

	public void delete(Remote remote) {
		new DeleteRemoteAsyncTask(mRemoteDao).execute(remote);
	}

	public LiveData<List<Remote>> getAllRemotes() {
		return allRemotes;
	}

    private static class InsertRemoteAsyncTask extends AsyncTask<Remote, Void, Void> {
        private RemoteDao remoteDao;

        private InsertRemoteAsyncTask(RemoteDao remoteDao) {
            this.remoteDao = remoteDao;
        }

        @Override
        protected Void doInBackground(Remote... remotes) {
            remoteDao.insert(remotes[0]);
            return null;
        }
    }

    private static class UpdateRemoteAsyncTask extends AsyncTask<Remote, Void, Void> {
        private RemoteDao remoteDao;

        private UpdateRemoteAsyncTask(RemoteDao remoteDao) {
            this.remoteDao = remoteDao;
        }

        @Override
        protected Void doInBackground(Remote... remotes) {
            remoteDao.update(remotes[0]);
            return null;
        }
    }

    private static class DeleteRemoteAsyncTask extends AsyncTask<Remote, Void, Void> {
        private RemoteDao remoteDao;

        private DeleteRemoteAsyncTask(RemoteDao remoteDao) {
            this.remoteDao = remoteDao;
        }

        @Override
        protected Void doInBackground(Remote... remotes) {
            remoteDao.delete(remotes[0]);
            return null;
        }
    }

}