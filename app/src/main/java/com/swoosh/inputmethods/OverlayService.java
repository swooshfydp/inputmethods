package com.swoosh.inputmethods;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private final static String LOGKEY = "SWOOSH_INPUT";
	private WindowManager windowManager;
	private ImageView overlayIcon;
    private LinearLayout overlayView;
    private GridLayout keypadView;
    private TextView mOutput;
	private WindowManager.LayoutParams params;
    private boolean visibleKeypad = false;

	@Override
	public void onCreate() {
		super.onCreate();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayView = new LinearLayout(this);

		params= new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;

        windowManager.addView(overlayView, params);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.overlay, overlayView);

        overlayIcon = (ImageView) overlayView.findViewById(R.id.overlay_icon);
        keypadView = (GridLayout) overlayView.findViewById(R.id.keyPadView);
        mOutput = (TextView) keypadView.findViewById(R.id.numeric_text_output);
        for(View v : keypadView.getTouchables()) {
            if(v instanceof Button) {
                Button btn = (Button) v;
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button b = (Button) view;
                        setOutput(b.getText().toString());
                    }
                });
            }
        }

        //this code is for dragging the chat head
		overlayIcon.setOnTouchListener(new View.OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
                    if(!visibleKeypad) {
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                    }
					return true;
				case MotionEvent.ACTION_UP:
                    if(!visibleKeypad && Math.abs(params.x - initialX) <= 10
                            && Math.abs(params.y - initialY) <= 10) {
                        keypadView.setVisibility(View.VISIBLE);
                        visibleKeypad = true;
                        windowManager.updateViewLayout(overlayView, params);
                    } else if(visibleKeypad) {
                        keypadView.setVisibility(View.GONE);
                        visibleKeypad = false;
                        windowManager.updateViewLayout(overlayView, params);
                    }
					return true;
				case MotionEvent.ACTION_MOVE:
                    if(!visibleKeypad) {
                        params.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayView, params);
                        return true;
                    }
				}
				return false;
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (overlayView != null) {
            visibleKeypad = false;
            windowManager.removeView(overlayView);
        }
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

    private void setOutput(String value) {
        String output = mOutput.getText().toString();
        if (value.equals("C")) {
            mOutput.setText("");
        }
        if (value.equals("B")) {
            if(output.length() > 0)
                mOutput.setText(output.substring(0, output.length()-1));
        }
        else if(output.length() < 12 && isNumeric(value)) {
            mOutput.setText(output.concat(value));
        }
    }

    private void inputText(Context c, String data, AccessibilityNodeInfo source) {
        Log.d(LOGKEY, "Old Clipboard saved");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            source.refresh();
            source.performAction(AccessibilityNodeInfo.ACTION_CUT);
            String oldClip = "";
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            try {
                oldClip = clipboard.getPrimaryClip().getItemAt(0).coerceToText(c).toString();
            } catch(Exception e) {}
            ClipData clip = ClipData.newPlainText("inputmethod", data);
            clipboard.setPrimaryClip(clip);
            source.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            clip = ClipData.newPlainText("inputmethod", oldClip);
            clipboard.setPrimaryClip(clip);
        }
    }

    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }
}