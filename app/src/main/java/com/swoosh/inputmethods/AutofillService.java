package com.swoosh.inputmethods;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AutofillService extends AccessibilityService {
    private final static String LOGKEY = "SWOOSH_INPUT";
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(LOGKEY, "Accessibility Event Happened");
        if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            AccessibilityNodeInfo source = event.getSource();
            Intent message = new Intent("swoosh_autofill");
            message.putExtra("sourceNode", source);
            Log.d(LOGKEY, "Send Message");
            LocalBroadcastManager.getInstance(this).sendBroadcast(message);
        }
    }

    @Override
    public void onInterrupt() {

    }
}
