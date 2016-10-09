package com.qianft.m.qian.adapter;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianft.m.qian.R;
import com.qianft.m.qian.bean.ShareObject;

public class SocialShareAdapter extends RecyclerView.Adapter<SocialShareAdapter.MyViewHolder>{

	private List<ShareObject> dataList;
	private Context context;
	private OnItemClickLitener mOnItemClickLitener;
	public SocialShareAdapter(Context context, ArrayList<ShareObject> datas){
		if (datas == null) {
			datas = new ArrayList<ShareObject>();
		}
		this.dataList = datas;
		this.context = context;
	}
	 public interface OnItemClickLitener{
	        void onItemClick(View view, int position);
	 }
	 public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener){
	        this.mOnItemClickLitener = mOnItemClickLitener;
	}
	@Override
	public int getItemCount() {
		return dataList.size();
	}

	@Override
	public void onBindViewHolder(final MyViewHolder viewHolder, int position) {
		viewHolder.mImageView.setImageResource(dataList.get(position).getImageId());
		viewHolder.mTextView.setText(dataList.get(position).getName());
		
		// 如果设置了回调，则设置点击事件
				if (mOnItemClickLitener != null)
				{
					viewHolder.itemView.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							int pos = viewHolder.getLayoutPosition();
							mOnItemClickLitener.onItemClick(viewHolder.itemView, pos);
						}
					});
				}
	  }

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(context).
				inflate(R.layout.umeng_social_share_item, arg0, false));
		return viewHolder;
	}
	
	class MyViewHolder extends  ViewHolder {

		ImageView mImageView;
		TextView mTextView;
		public MyViewHolder(View view) {
			super(view);
			
			mImageView = (ImageView) view.findViewById(R.id.umeng_social_share_image);
			mTextView = (TextView) view.findViewById(R.id.umeng_social_share_pltform_name);
		}
		
	}
	
}
