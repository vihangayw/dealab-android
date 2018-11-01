package com.zinios.dealab.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.zinios.dealab.R;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FilterActivity extends BaseActivity {

	@BindView(R.id.imgpp)
	ImageView imageView;
	@BindView(R.id.img2)
	ImageView img2;
	@BindView(R.id.img3)
	ImageView img3;
	@BindView(R.id.img4)
	ImageView img4;
	@BindView(R.id.img5)
	ImageView img5;
	@BindView(R.id.img6)
	ImageView img6;
	@BindView(R.id.calendarView)
	MaterialCalendarView materialCalendarView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);
		ButterKnife.bind(this);
		materialCalendarView.setTopbarVisible(false);
		Date date = new Date();
		materialCalendarView.state().edit()
				.setMinimumDate(date)
				.commit();
		materialCalendarView.setSelectedDate(date);
		Glide.with(this)
				.load("https://www.techprevue.com/wp-content/uploads/2016/05/online-apparel-business.jpg")
				.crossFade()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(imageView);

		Glide.with(this)
				.load("https://pas-wordpress-media.s3.amazonaws.com/wp-content/uploads/2014/02/8-Tips-For-Getting-into-Retail-Channels.jpg")
				.crossFade()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(img2);

		Glide.with(this)
				.load("https://media.gettyimages.com/photos/man-woman-working-out-on-crosscycles-at-gym-picture-id559688773").crossFade()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(img3);

		Glide.with(this)
				.load("https://image.shutterstock.com/z/stock-photo-human-resources-pool-customer-care-care-for-employees-labor-union-life-insurance-employment-423521482.jpg").crossFade()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(img4);

		Glide.with(this)
				.load("http://lecrans.com/wp-content/uploads/2016/03/restaurant-slideshow-06.jpg").crossFade()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(img5);
		Glide.with(this)
				.load("https://ababankmarketing.com/wp-content/uploads/2016/09/Kroll_Tiny-Bank-tile-image.jpg").crossFade()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(img6);

	}

	public void back(View view) {
		onBackPressed();
	}
}
