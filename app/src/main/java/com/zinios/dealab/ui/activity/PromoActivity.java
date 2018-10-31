package com.zinios.dealab.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.thefinestartist.finestwebview.FinestWebView;
import com.zinios.dealab.DealabApplication;
import com.zinios.dealab.R;
import com.zinios.dealab.api.APIHelper;
import com.zinios.dealab.api.request.helper.impl.PromoRequestHelperImpl;
import com.zinios.dealab.api.response.Ancestor;
import com.zinios.dealab.api.response.Error;
import com.zinios.dealab.api.response.PromoResponse;
import com.zinios.dealab.model.PromoList;
import com.zinios.dealab.model.Promotions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PromoActivity extends BaseActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

	@BindView(R.id.title)
	TextView txtTitle;
	@BindView(R.id.txt_end_day)
	TextView txtEnd;
	@BindView(R.id.txt_note)
	TextView txtNote;
	@BindView(R.id.txt_start_day)
	TextView txtStart;
	@BindView(R.id.slider)
	SliderLayout mDemoSlider;

	private int branchID;
	private Map<String, Promotions> promoMap;
	private PromoList promoList;
	private Promotions promotions;

	private static String formatDay(Date date) {
		return new SimpleDateFormat("dd MMM, yyyy", java.util.Locale.getDefault()).format(date);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_promo);
		ButterKnife.bind(this);

		initializeViews();
		setListeners();
		loadData();
	}

	@Override
	void initializeViews() {
		super.initializeViews();

	}

	@Override
	protected void onStop() {
		mDemoSlider.stopAutoCycle();
		super.onStop();
	}

	@OnClick(R.id.back)
	void back() {
		onBackPressed();
	}

	private void loadData() {
		Bundle extras = getIntent().getExtras();
		branchID = extras.getInt("bid");
		txtTitle.setText(extras.getString("branch"));

		new PromoRequestHelperImpl().getBranchPromos(branchID, new APIHelper.PostManResponseListener() {
			@Override
			public void onResponse(Ancestor ancestor) {
				if (ancestor instanceof PromoResponse) {
					promoList = ((PromoResponse) ancestor).getData();
					List<Promotions> promotions = promoList.getPromotions();
					setupPromos(promotions);
				}
			}

			@Override
			public void onError(Error error) {
				DealabApplication.getInstance().showError(error);
			}
		});
	}

	private void setupPromos(List<Promotions> promotions) {
		Map<String, String> urlMaps = new HashMap<>();
		promoMap = new HashMap<>();
		for (Promotions promotion : promotions) {
			urlMaps.put(promotion.getDescription(), promotion.getImageUrl());
			promoMap.put(promotion.getDescription(), promotion);
		}

		for (String name : urlMaps.keySet()) {
			TextSliderView textSliderView = new TextSliderView(this);
			// initialize a SliderLayout
			textSliderView
					.description(name)
					.image(urlMaps.get(name))
					.setScaleType(BaseSliderView.ScaleType.Fit)
					.setOnSliderClickListener(this);

			//add your extra information
			textSliderView.bundle(new Bundle());
			textSliderView.getBundle()
					.putSerializable("extra", promoMap.get(name));

			mDemoSlider.addSlider(textSliderView);
		}
		mDemoSlider.setPresetTransformer(SliderLayout.Transformer.ZoomOutSlide);
		mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
		mDemoSlider.setCustomAnimation(new DescriptionAnimation());
		mDemoSlider.setDuration(5000);
		mDemoSlider.addOnPageChangeListener(this);

	}

	@Override
	public void onSliderClick(BaseSliderView slider) {
		Promotions extra = (Promotions) slider.getBundle().getSerializable("extra");
		showURL(extra.getWebUrl());
	}

	private void showURL(String url) {
		if (url == null) return;
		new FinestWebView.Builder(this)
				.statusBarColorRes(R.color.colorPrimaryDark)
				.toolbarColorRes(R.color.colorPrimary)
				.swipeRefreshColorRes(R.color.colorAccent)
				.titleColorRes(R.color.transparent_white_percent_95)
				.urlColorRes(R.color.colorLightGrayEEE)
				.iconDefaultColorRes(R.color.colorWhite)
				.show(url);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		int i = 0;
		for (String key : promoMap.keySet()) {
			if (position == i)
				promotions = promoMap.get(key);
			i++;
		}
		if (promotions != null) {
			setPromoData(promotions);
		}
	}

	private void setPromoData(Promotions promoData) {
		txtEnd.setText(promoData.getEndDate() != null ? formatDay(promoData.getEndDate()) : "");
		txtStart.setText(promoData.getEndDate() != null ? formatDay(promoData.getStartDate()) : "");
		txtNote.setText(!TextUtils.isEmpty(promoData.getNote()) ? promoData.getNote() : "-");
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	public void share(View view) {
		if (promoList != null && promotions != null)
			ShareCompat.IntentBuilder.from(this)
					.setType("text/plain")
					.setChooserTitle("Share promotion")
					.setText(promotions.getWebUrl())
					.startChooser();
	}

	public void navigate(View view) {
		if (promoList != null) {
			String url = "https://www.google.com/maps/dir/?api=1&destination=" + promoList.getBranch().getLat() + "," + promoList.getBranch().getLng() + "&travelmode=driving";
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
	}
}
