package com.zinios.dealab.util.par;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PARProgressBar extends ProgressBar {
	private TextView textView;
	private RelativeLayout mainLayout;

	public PARProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setIndeterminate(true);
		this.setVisibility(VISIBLE);
		this.textView = new TextView(context);
		this.textView.setText("");
		// this.textView.setTextAppearance(context, 16974257);
		this.textView.setTextColor(-1);
		LinearLayout linearLayout = new LinearLayout(context);
		LayoutParams lp = new LayoutParams(-2, -2);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		int pad = (int) TypedValue.applyDimension(1, 10.0F, this.getResources().getDisplayMetrics());
		linearLayout.setPadding(pad, pad, pad, pad);
		linearLayout.setBackgroundColor(-2013265920);
		linearLayout.setLayoutParams(lp);
		lp.gravity = 17;
		this.setLayoutParams(lp);
		linearLayout.addView(this.textView, lp);
		linearLayout.addView(this, lp);
		this.mainLayout = new RelativeLayout(context);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		this.mainLayout.setLayoutParams(params);
		this.mainLayout.addView(linearLayout);
	}

	public void setVisibility(int v) {
		super.setVisibility(v);
		if (this.mainLayout != null) {
			this.mainLayout.setVisibility(v);
		}

	}

	public void setText(String text) {
		this.getTextView().setText(text);
	}

	public void showWithText(String text) {
		this.setText(text);
		this.mainLayout.setVisibility(VISIBLE);
	}

	public void hide() {
		this.mainLayout.setVisibility(GONE);
	}

	public RelativeLayout getMainLayout() {
		return this.mainLayout;
	}

	public TextView getTextView() {
		return this.textView;
	}
}
