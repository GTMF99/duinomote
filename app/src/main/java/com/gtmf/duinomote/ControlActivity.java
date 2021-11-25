package com.gtmf.duinomote;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.ArrayList;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButton;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.google.android.material.appbar.MaterialToolbar;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import java.lang.ref.WeakReference;
import java.util.Set;
import android.content.ComponentName;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.content.Context;
import android.content.Intent;

public class ControlActivity extends AppCompatActivity {

	private List<IRButton> mButtonList;
    private KeypadViewModel mKeypadViewModel;
	private FrameLayout mKeypad;
	private int mRemoteId = 0;
	private String mRemoteName = "";
	private UsbService usbService;
    private int latestButtonId = 0;
    private String mSignalToSend = "";
    private MyHandler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mRemoteId = extras.getInt("remoteId");
			mRemoteName = extras.getString("remoteName");
		}

        mButtonList = new ArrayList<IRButton>();

        mHandler = new MyHandler(this);

		mKeypad = findViewById(R.id.keypad);

		mKeypadViewModel = new ViewModelProvider(this).get(KeypadViewModel.class);
        mKeypadViewModel.getAllButtonsFromRemote(mRemoteId).observe(this, new Observer<List<IRButton>>() {
            @Override
            public void onChanged(@Nullable List<IRButton> buttons) {
                if (!buttons.isEmpty()) {
                    latestButtonId = buttons.get(buttons.size() - 1).getButtonId();
                    updateButtonsOnLayout(buttons);
                }
            }
        });

        MaterialToolbar mToolbar = findViewById(R.id.control_topappbar);
        mToolbar.setSubtitle(mRemoteName);
        mToolbar.setNavigationOnClickListener(backButton);

	}

	private void updateButtonsOnLayout(List<IRButton> buttons) {
		clearKeypad();
		for (IRButton button : buttons) {
            mButtonList.add(button);
			createButton(button);
		}
		mKeypad.invalidate();
	}

	private void clearKeypad() {
		mKeypad.removeAllViews();
	}

	private void createButton(IRButton button) {
		MaterialButton buttonView = new MaterialButton(ControlActivity.this);
		FrameLayout.LayoutParams buttonLayout = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonLayout.leftMargin = button.getPosX();
		buttonLayout.topMargin = button.getPosY();
		buttonView.setLayoutParams(buttonLayout);
		buttonView.setOnClickListener(buttonClickListener);
		buttonView.setId(button.getButtonId());
		mKeypad.addView(buttonView);
	}

	private View.OnClickListener buttonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
            for (IRButton button : mButtonList) {
                if (v.getId() == button.getButtonId()) {
                    usbService.write("send|".getBytes());
                    mSignalToSend = button.getButtonSignal();
                    usbService.write(mSignalToSend.getBytes());
                }
            }
		}
	};

	private View.OnClickListener backButton = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CoordinatorLayout root_view = (CoordinatorLayout) findViewById(R.id.control_root_view);
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Snackbar.make(root_view, "USB Ready", Snackbar.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Snackbar.make(root_view, "USB Permission not granted", Snackbar.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Snackbar.make(root_view, "No USB connected", Snackbar.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Snackbar.make(root_view, "USB disconnected", Snackbar.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Snackbar.make(root_view, "USB device not supported", Snackbar.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<ControlActivity> mActivity;

        public MyHandler(ControlActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:

                    break;
                case UsbService.CTS_CHANGE:
                    
                    break;
                case UsbService.DSR_CHANGE:
                    
                    break;
            }
        }
    }

}
