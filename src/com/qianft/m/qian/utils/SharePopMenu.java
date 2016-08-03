package com.qianft.m.qian.utils;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.qianft.m.qian.R;
import com.qianft.m.qian.bean.ShareObject;




/**
 *PopMenu
 */
public class SharePopMenu implements OnItemClickListener,OnClickListener {
	public interface OnItemClickListener {
		public void onItemClick(int index);
	}
	
	private ArrayList<ShareObject> itemList;
	private Context context;
	private PopupWindow popupWindow;
	private ListView listView;
	private OnItemClickListener listener;
	private LayoutInflater inflater;
	private LinearLayout shareToCrl;
	private LinearLayout shareToFrd;
	private Button mCancelBtn; 
	private shareBottomClickListener mListener;
	
	public SharePopMenu(Context context) {
		this.context = context;

		itemList = new ArrayList<ShareObject>(2);

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.popmenu2, null);

		//listView = (ListView) view.findViewById(R.id.listView);
		//listView.setAdapter(new PopAdapter());
		//listView.setOnItemClickListener(this);
		shareToCrl = (LinearLayout) view.findViewById(R.id.circle_ll);
		shareToFrd = (LinearLayout) view.findViewById(R.id.freind_ll);
		mCancelBtn = (Button) view.findViewById(R.id.dismiss_click);
		shareToCrl.setOnClickListener(this);
		shareToFrd.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
		popupWindow = new PopupWindow(view, 
				/*context.getResources().getDimensionPixelSize(R.dimen.popmenu_width)*/LayoutParams.MATCH_PARENT,  
				LayoutParams.WRAP_CONTENT);
		//按返回键时popwindow消失
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setAnimationStyle(R.style.AnimBottom);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (listener != null) {
			listener.onItemClick(position);
		}
		dismiss();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		 this.listener = listener;
	}

	public void addItems(ShareObject[] items) {
		for (ShareObject s : items)
			itemList.add(s);
	}

	public void addItem(ShareObject item) {
		itemList.add(item);
	}

	public void showAsDropDown(View parent) {
		//popupWindow.showAsDropDown(parent, 20,
		//		context.getResources().getDimensionPixelSize(R.dimen.popmenu_yoff));
		popupWindow.showAtLocation(parent, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.update();
	}

	// ���ز˵�
	public void dismiss() {
		popupWindow.dismiss();
	}
	
	public void setShareBottomClickListener(shareBottomClickListener shareListener) {
		
		this.mListener = shareListener;
		
	}
	
	public interface shareBottomClickListener {
		
		void shareCircle();
		void shareFreind();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.circle_ll:
			mListener.shareCircle();
			dismiss();
			break;
		case R.id.freind_ll:
			mListener.shareFreind();
			dismiss();
			break;
		case R.id.dismiss_click:
			dismiss();
			break;
		default:
		}
	}

	// ������
	private final class PopAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return itemList.size();
		}

		@Override
		public Object getItem(int position) {
			return itemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ShareObject shareObject = (ShareObject) getItem(position);
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.pomenu_item, null);
				holder = new ViewHolder();
				convertView.setTag(holder);
				holder.groupItem = (TextView) convertView.findViewById(R.id.textView);
				holder.mShareImage = (ImageView) convertView.findViewById(R.id.share_icon);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.groupItem.setText(itemList.get(position).getName());
			holder.mShareImage.setImageResource(itemList.get(position).getImageId());

			return convertView;
		}

		private final class ViewHolder {
			TextView groupItem;
			ImageView mShareImage;
		}
	}
	
	
}
