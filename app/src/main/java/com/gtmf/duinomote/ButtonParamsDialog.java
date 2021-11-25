package com.gtmf.duinomote;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import android.widget.AutoCompleteTextView;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import java.lang.ref.WeakReference;
import java.util.Set;
import android.content.ComponentName;
import android.content.Intent;

public class ButtonParamsDialog extends AppCompatDialogFragment {

    private static String incomingData = "";
    private TextInputEditText mSignalString;
    private MaterialButton mCaptureButton;
    private AutoCompleteTextView mColor;
    private AutoCompleteTextView mShape;
    private AutoCompleteTextView mIcon;
    private boolean mClickedButton = false;
	private ButtonParamsDialogListener mListener;

    private UsbService usbService;
    private MyHandler mHandler;

	public boolean getClickedButton() {
        return this.mClickedButton;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(getActivity());

        LayoutInflater mInflater = getActivity().getLayoutInflater();
        View mView = mInflater.inflate(R.layout.dialog_ctrledit_buttonparams, null);

        mHandler = new MyHandler(this);

        mColor = mView.findViewById(R.id.dialog_ctrledit_color);
        final String colorsArr[] = getResources().getStringArray(R.array.colors);
        mColor.setAdapter(new ArrayAdapter(mView.getContext(), R.layout.dropdown_item, colorsArr));

        mShape = mView.findViewById(R.id.dialog_ctrledit_shape);
        final String shapesArr[] = getResources().getStringArray(R.array.shapes);
        mShape.setAdapter(new ArrayAdapter(mView.getContext(), R.layout.dropdown_item, shapesArr));

        mIcon = mView.findViewById(R.id.dialog_ctrledit_icon);
        final String iconsArr[] = getResources().getStringArray(R.array.icons);
        mIcon.setAdapter(new ArrayAdapter(mView.getContext(), R.layout.dropdown_item, iconsArr));

        mSignalString = mView.findViewById(R.id.dialog_ctrledit_signal);
        
        mCaptureButton = mView.findViewById(R.id.capture_button);
        mCaptureButton.setOnClickListener(mButtonClick);

        mBuilder.setView(mView)
                .setTitle("Button Params")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mClickedButton = false;
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (incomingData != "") {
                            mSignalString.setText(incomingData);
                        }
                        mClickedButton = true;
                        String signal = mSignalString.getText().toString();
                        String color = mColor.getText().toString();
                        String shape = mShape.getText().toString();
                        String icon = mIcon.getText().toString();
                        mListener.saveButtonToKeypad(ButtonParamsDialog.this, signal, color, shape, icon);
                        dialogInterface.dismiss();
                    }
                });

        return mBuilder.create();
    }

    private View.OnClickListener mButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (usbService != null) { // if UsbService was correctly binded, Send data
                usbService.write("receive|".getBytes());
                v.setEnabled(false);
                ButtonParamsDialog.this.setCancelable(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setEnabled(true);
                        ButtonParamsDialog.this.setCancelable(true);
                    }
                }, 4000);
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (ButtonParamsDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ButtonParamsDialogListener");
        }
    }

    public interface ButtonParamsDialogListener {
        void saveButtonToKeypad(ButtonParamsDialog dialog, String signal, String color, String shape, String icon);
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    
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
        getActivity().unregisterReceiver(mUsbReceiver);
        getActivity().unbindService(usbConnection);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        getActivity().registerReceiver(mUsbReceiver, filter);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(getActivity(), service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            getActivity().startService(startService);
        }
        Intent bindingIntent = new Intent(getActivity(), service);
        getActivity().bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<ButtonParamsDialog> mDialog;

        public MyHandler(ButtonParamsDialog dialog) {
            mDialog = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    data = data.trim();
                    mDialog.get().mSignalString.setText(data);
                    break;
                case UsbService.CTS_CHANGE:
                    
                    break;
                case UsbService.DSR_CHANGE:
                    
                    break;
            }
        }
    }

}