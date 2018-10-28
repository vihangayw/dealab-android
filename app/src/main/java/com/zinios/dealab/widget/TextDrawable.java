package com.zinios.dealab.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.zinios.dealab.R;


public class TextDrawable extends Drawable {

	private final String text;
	private final Paint paint;
	private Context context;

	public TextDrawable(String text, Context context) {
		this.text = text;
		this.context = context;
		this.paint = new Paint();
		paint.setColor(ContextCompat.getColor(context, R.color.colorLightGray));
		paint.setTextSize(context.getResources().getDimension(R.dimen._12sdp));
		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "font/sf_ui_display_semibold.otf"));
		paint.setTextAlign(Paint.Align.RIGHT);
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		float density = context.getResources().getDisplayMetrics().density;
		if (density > 2.0) { //xxhdpi
			canvas.drawText(text, 0, 17, paint);
		} else if (density > 1.5) { //xhdpi
			canvas.drawText(text, 0, 14, paint);
		} else if (density > 1.0) { //hdpi
			canvas.drawText(text, 0, 12, paint);
		} else if (density > 0.75) { //mdpi
			canvas.drawText(text, 0, 9, paint);
		} else { //ldpi
			canvas.drawText(text, 0, 7, paint);
		}
	}

	@Override
	public void setAlpha(int alpha) {
		paint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		paint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

}