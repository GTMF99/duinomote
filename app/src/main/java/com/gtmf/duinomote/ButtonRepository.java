package com.gtmf.duinomote;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;
import java.util.List;

public class ButtonRepository {
	IRButtonDao mIRButtonDao;

	public ButtonRepository (Application app) {
		AppDatabase database = AppDatabase.getInstance(app);
		mIRButtonDao = database.irButtonDao();
	}

	public void insert(IRButton button) {
		new InsertButtonAsyncTask(mIRButtonDao).execute(button);
	}

	public void update(IRButton button) {
		new UpdateButtonAsyncTask(mIRButtonDao).execute(button);
	}

	public LiveData<List<IRButton>> getAllButtonsFromRemote(int remoteId) {
		return mIRButtonDao.getButtonsByRemote(remoteId);
	}

    private static class InsertButtonAsyncTask extends AsyncTask<IRButton, Void, Void> {
        private IRButtonDao irButtonDao;

        private InsertButtonAsyncTask(IRButtonDao irButtonDao) {
            this.irButtonDao = irButtonDao;
        }

        @Override
        protected Void doInBackground(IRButton... buttons) {
            irButtonDao.insert(buttons[0]);
            return null;
        }
    }

    private static class UpdateButtonAsyncTask extends AsyncTask<IRButton, Void, Void> {
        private IRButtonDao irButtonDao;

        private UpdateButtonAsyncTask(IRButtonDao irButtonDao) {
            this.irButtonDao = irButtonDao;
        }

        @Override
        protected Void doInBackground(IRButton... buttons) {
            irButtonDao.update(buttons[0]);
            return null;
        }
    }
	
}