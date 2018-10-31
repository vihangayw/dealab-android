package com.zinios.dealab.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zinios.dealab.R;
import com.zinios.dealab.model.MapLocation;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PromoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<MapLocation> items;
	private OnComponentClickListener listener;
	private Context context;

	public PromoAdapter(Context context, List<MapLocation> items, OnComponentClickListener listener) {
		if (items == null) items = new ArrayList<>();
		this.items = items;
		this.context = context;
		this.listener = listener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
				R.layout.list_item_promo, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		bindViewHolder((ViewHolder) holder, position);
	}

	private void bindViewHolder(ViewHolder holder, int position) {
		MapLocation location = this.items.get(position);
		if (location != null) {
			holder.txtCompany.setText(!TextUtils.isEmpty(location.getCompany())
					? location.getCompany() : "-");
			holder.txtBranch.setText(!TextUtils.isEmpty(location.getBranch())
					? location.getBranch() : "-");
			holder.txtCount.setText(String.valueOf(location.getDealCount()).concat(" ")
					.concat(location.getDealCount() > 1 ? "Promotions" : "Promotion"));
		}
	}

	public List<MapLocation> getList() {
		return items;
	}

	public void setList(List<MapLocation> items) {
		if (items == null) items = new ArrayList<>();
		this.items = items;
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		return items.size();
	}


	public interface OnComponentClickListener {
		void onComponentClick(View itemView, int position);
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.company)
		TextView txtCompany;
		@BindView(R.id.branch)
		TextView txtBranch;
		@BindView(R.id.count)
		TextView txtCount;
		@BindView(R.id.item_layout)
		View itemLayout;

		ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);

			itemLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener != null) {
						listener.onComponentClick(v, getLayoutPosition());
					}
				}
			});
		}
	}

}