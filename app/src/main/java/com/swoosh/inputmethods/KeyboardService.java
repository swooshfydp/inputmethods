package com.swoosh.inputmethods;

import android.inputmethodservice.InputMethodService;
import android.view.DragEvent;
import android.view.View;

public class KeyboardService extends InputMethodService implements View.OnDragListener {

    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
        return false;
    }
}
