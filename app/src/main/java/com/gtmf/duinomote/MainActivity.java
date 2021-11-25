package com.gtmf.duinomote;
 
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import java.util.Arrays;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import java.lang.ref.WeakReference;
import java.util.Set;
 
public class MainActivity extends AppCompatActivity implements RemoteParamsDialog.RemoteParamsDialogListener {

	private RemoteListViewModel remoteListViewModel;
    private UsbService usbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


	    RecyclerView recyclerView = findViewById(R.id.main_recyclerview);
	    recyclerView.setLayoutManager(new LinearLayoutManager(this));
	    recyclerView.setHasFixedSize(true);

	    final RemoteAdapter adapter = new RemoteAdapter();
	    recyclerView.setAdapter(adapter);

        remoteListViewModel = new ViewModelProvider(this).get(RemoteListViewModel.class);
        remoteListViewModel.getAllRemotes().observe(this, new Observer<List<Remote>>() {
            @Override
            public void onChanged(@Nullable List<Remote> remotes) {
                adapter.setRemotes(remotes);
            }
        });

        adapter.setOnItemClickListener(new RemoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Remote remote) {
                Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                intent.putExtra("remoteId", remote.getId());
                intent.putExtra("remoteName", remote.getName());
                startActivity(intent);
            }
        });

        adapter.setOnItemLongClickListener(new RemoteAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(Remote remote) {
                Intent intent = new Intent(MainActivity.this, ControlEditorActivity.class);
                intent.putExtra("remoteId", remote.getId());
                intent.putExtra("remoteName", remote.getName());
                startActivity(intent);
         		return true;
            }
        });

		ExtendedFloatingActionButton createButton = findViewById(R.id.create_efab);

		createButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				RemoteParamsDialog mRemoteParamsDialog = new RemoteParamsDialog();
				mRemoteParamsDialog.show(getSupportFragmentManager(), "main dialog");
			} 
		});
	}

	@Override
	public void saveRemoteToList(RemoteParamsDialog dialog, String name, String category) {
		Remote mRemote = new Remote(name, category);
		if (dialog.getClickedButton()) {
			remoteListViewModel.insert(mRemote);
		}
	}

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
            CoordinatorLayout root_view = (CoordinatorLayout) findViewById(R.id.main_root_view);
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

}