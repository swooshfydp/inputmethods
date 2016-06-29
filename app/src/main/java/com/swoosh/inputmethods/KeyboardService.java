package com.swoosh.inputmethods;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class KeyboardService extends InputMethodService {
    private TextView number_output;
    private ViewPager input_canvas;
    private ImageView output_canvas;
    private TextView noteCount;
    private CurrencyAdapter adapter;
    private HashMap<Integer, Integer> currencyMap = new HashMap<>();
    private final static String CURRENCY_STRING = "tz";
    private final static String LOGKEY = "SWOOSH_INPUT";
    private final int[] currencies = {500, 1000, 2000, 5000, 10000};

    @Override
    public View onCreateInputView() {
        int[] notes = {
            R.drawable.fivehund_bill,
            R.drawable.onethou_bill,
            R.drawable.twothou_bill,
            R.drawable.fivethou_bill,
            R.drawable.tenthou_bill
        };
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.keyboard_layout, null);
        number_output = (TextView) layout.findViewById(R.id.number_out);
        input_canvas = (ViewPager) layout.findViewById(R.id.in_canvas);
        output_canvas = (ImageView) layout.findViewById(R.id.out_canvas);
        noteCount = (TextView) layout.findViewById(R.id.currecny_count);
        Button btnClear = (Button) layout.findViewById(R.id.reset_button);
        buildCurrencyMap(currencies);
        adapter = new CurrencyAdapter(this, notes);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearButtonCallback();
            }
        });
        input_canvas.setAdapter(adapter);
        input_canvas.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                noteCount.setText("X" + String.valueOf(currencyMap.get(currencies[position])));
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int pos = intent.getExtras().getInt("position");
                int amount = intent.getExtras().getInt("amount");
                int value = currencyMap.get(currencies[pos]);
                currencyMap.put(currencies[pos], value + amount);
                noteCount.setText("X" + String.valueOf(currencyMap.get(currencies[pos])));
                outputResults();
            }
        }, new IntentFilter("cash_drag_event"));
        outputResults();
        return layout;
    }

    private void buildCurrencyMap(int[] currecies) {
        for(int currency : currecies) {
            currencyMap.put(currency, 0);
        }
    }

    private void outputResults() {
        InputConnection ic = getCurrentInputConnection();
        String output = "";
        int result = 0;
        Iterator it = currencyMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            result += ((int) pair.getKey() * (int) pair.getValue());
        }
        output = result + " " + CURRENCY_STRING;
        ic.deleteSurroundingText(1000000, 1000000);
        ic.commitText(String.valueOf(result), 0);
        number_output.setText(output);
    }

    private void clearButtonCallback() {
        for(int i = 0; i < adapter.getCount(); i++) {
            noteCount.setText("X0");
            currencyMap.put(currencies[i], 0);
        }
        outputResults();
    }
}
