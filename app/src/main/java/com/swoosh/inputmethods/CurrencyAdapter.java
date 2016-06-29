package com.swoosh.inputmethods;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.HashMap;

public class CurrencyAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mInflator;
    private int[] mNotes;
    private float intY = 0;
    private float endY = 0;
    private final static float SWIPE_UP_THRESHOLD = -25;
    private final static String LOGKEY = "SWOOSH_INPUT";

    public CurrencyAdapter(Context context, int[] notes) {
        mContext = context;
        mNotes = notes;
        mInflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mNotes.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mInflator.inflate(R.layout.currency_view, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.currency);
        imageView.setImageBitmap(decodeSampledBitmapFromResource(container.getResources(),
                mNotes[position], 100, 100));

        container.addView(itemView);
        itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        intY = event.getY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        endY = event.getY();
                        float delta = endY - intY;
                        Log.d(LOGKEY, "Start: " + intY + ", End: " + endY);
                        Intent msg = new Intent("cash_drag_event");
                        msg.putExtra("position", position);
                        if (delta < SWIPE_UP_THRESHOLD) {
                            msg.putExtra("amount", 1);
                        }
                        if (delta > Math.abs(SWIPE_UP_THRESHOLD)) {
                            msg.putExtra("amount", -1);
                        }
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(msg);
                        return true;
                }
                return false;
            }
        });

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
