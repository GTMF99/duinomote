package com.gtmf.duinomote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.DialogInterface;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButton;
import android.view.View;
import android.view.MenuItem;
import android.view.GestureDetector;
import android.view.DragEvent;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.os.Bundle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.ArrayList;

public class ControlEditorActivity extends AppCompatActivity implements ButtonParamsDialog.ButtonParamsDialogListener {

	private KeypadViewModel mKeypadViewModel;
	private FrameLayout mKeypad;
	private int latestButtonId = 0;
	private List<IRButton> mButtonList;
	private int mRemoteId = 0;
	private GestureDetector mGestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controleditor);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mRemoteId = extras.getInt("remoteId");
		}

		//Toast.makeText(ControlEditorActivity.this, String.valueOf(mRemoteId), Toast.LENGTH_LONG).show();

		mButtonList = new ArrayList<IRButton>();

		mGestureDetector = new GestureDetector(this, new SingleTapConfirm());

		mKeypad = findViewById(R.id.keypad_edit);
		FloatingActionButton createControlButton = findViewById(R.id.create_control);

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

        MaterialToolbar mToolbar = findViewById(R.id.controleditor_topappbar);
        mToolbar.setSubtitle("Edit control layout");
        mToolbar.setOnMenuItemClickListener(menuItemClickListener);
        mToolbar.setNavigationOnClickListener(backButton);

		createControlButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ButtonParamsDialog mButtonParamsDialog = new ButtonParamsDialog();
				mButtonParamsDialog.show(getSupportFragmentManager(), "ctrledit dialog");
			}
		});
	}

	private void updateButtonsOnLayout(List<IRButton> buttons) {
		clearKeypad();
		mButtonList.clear();
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
		MaterialButton buttonView = new MaterialButton(ControlEditorActivity.this);
		FrameLayout.LayoutParams buttonLayout = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonLayout.leftMargin = button.getPosX();
		buttonLayout.topMargin = button.getPosY();
		buttonView.setLayoutParams(buttonLayout);
		buttonView.setOnTouchListener(buttonTouchListener);
		buttonView.setId(button.getButtonId());
		mKeypad.addView(buttonView);
	}

	private void updateIRButtonPos(int viewId, FrameLayout.LayoutParams params) {
		IRButton buttonToUpdate = null;
		for (IRButton button : mButtonList) {
			if (button.getButtonId() == viewId) {
				//Toast.makeText(ControlEditorActivity.this, "button found!", Toast.LENGTH_LONG).show();
				buttonToUpdate = button;
			}
		}
		buttonToUpdate.setPosX(params.leftMargin);
		buttonToUpdate.setPosY(params.topMargin);
		if (buttonToUpdate == null) {
			//Toast.makeText(ControlEditorActivity.this, "failed", Toast.LENGTH_LONG).show();
		}
		else {
			mKeypadViewModel.update(buttonToUpdate);
		}
	}

	@Override
	public void saveButtonToKeypad(ButtonParamsDialog dialog, String signal, String color, String shape, String icon) {
		IRButton button = new IRButton(signal, 0, 0);
		button.setRemoteButtonId(mRemoteId);
		if (dialog.getClickedButton()) {
			mKeypadViewModel.insert(button);
		}
	}

	private View.OnTouchListener buttonTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			if (mGestureDetector.onTouchEvent(event)) {
				//Toast.makeText(ControlEditorActivity.this, "single tap", Toast.LENGTH_LONG).show();
				return true;
			}
			else {
				int _xDelta = 0;
				int _yDelta = 0;
				final int X = (int) event.getRawX();
				final int Y = (int) event.getRawY();
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
					_xDelta = X - lParams.leftMargin;
					_yDelta = Y - lParams.topMargin;
					break;
				case MotionEvent.ACTION_UP:
					updateIRButtonPos(view.getId(), (FrameLayout.LayoutParams) view.getLayoutParams());
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_MOVE:
					FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
					layoutParams.leftMargin = X - _xDelta;
					layoutParams.topMargin = Y - _yDelta;
					layoutParams.rightMargin = -250;
					layoutParams.bottomMargin = -250;
					view.setLayoutParams(layoutParams);
					break;
				}
				mKeypad.invalidate();
			}
			return false;
		}
	};

	private View.OnClickListener backButton = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	private Toolbar.OnMenuItemClickListener menuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.controleditor_save) {
            	if (!mButtonList.isEmpty()) {
            		for (IRButton button : mButtonList) {
            			mKeypadViewModel.insert(button);
            		}
            	}
            	else {
            		Snackbar.make(findViewById(R.id.editor_root_view), "You should add at least a button to save a remote layout", 0).show();
            	}
            }
            return true;
        }
    };

	private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			return true;
		}
	}

}
