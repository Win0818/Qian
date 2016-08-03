package com.qianft.m.qian.view;

import com.qianft.m.qian.R;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class GlobalDetailDialog extends Dialog {
    private boolean mBlockKeycodeBack = false;

    public GlobalDetailDialog(Context context, int theme) {
        super(context, theme);
        mBlockKeycodeBack = false;
    }

    public GlobalDetailDialog(Context context) {
        super(context);
    }

    public void setBlockKeyCodeBack(boolean key) {
        mBlockKeycodeBack = key;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mBlockKeycodeBack && (keyCode == KeyEvent.KEYCODE_BACK)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {

        private Context context;
        private boolean mBlockKeyBack = false;
        private boolean mCancelable = true;
        private View mDialogContentView;
        private int mWidth =0;
        private int mHeight =0;
        private View mAnchorView = null;
        
        public Builder(Context context) {
            this.context = context;
            mBlockKeyBack = false;
        }

        public Builder setCancelable(boolean mCancelable) {
            this.mCancelable = mCancelable;
            return this;
        }

        public Builder setBlockKeyBack(boolean block) {
            this.mBlockKeyBack = block;
            return this;
        }


        /**
         * Create the custom dialog
         * 
         * @return
         */
        public GlobalDetailDialog create() {
            return create(R.style.detail_alert_dialog, mDialogContentView);
        }

        /**
         * Create the custom dialog
         * 
         * @return
         */
        public Builder setContentView(View dialogContentView) {
            this.mDialogContentView = dialogContentView;
            return this;
        }
        
        public Builder setViewHeight(int height) {
            this.mHeight = height;
            return this;
        }
        
        public Builder setViewWidth(int width) {
            this.mWidth = width;
            return this;
        }
        
        public Builder setAnchorView(View view) {
            this.mAnchorView = view;
            return this;
        }
        
        /**
         * Create the custom dialog
         * 
         * @param style 皮肤风格
         * @param conentView 布局文件
         * @return
         */
        public GlobalDetailDialog create(int style, View dialogContentView) {
            // instantiate the dialog with the custom Theme
        	if (dialogContentView == null) {
        		return null;
        	}
            final GlobalDetailDialog dialog = new GlobalDetailDialog(context, style);
            dialog.setContentView(dialogContentView);
            dialog.setCancelable(mCancelable);
            Window dialogWindow = dialog.getWindow();
            WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
            Resources resources = context.getResources();
            // 修改窗体宽高
            layoutParams.width = (int) (resources.getDimension(R.dimen.screen_width) * 0.7);
            layoutParams.height = (int) (resources.getDimension(R.dimen.screen_height) * 0.92);
            layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.gravity = Gravity.CENTER;
            dialogWindow.setAttributes(layoutParams); 
            if (mBlockKeyBack) {
                dialog.setBlockKeyCodeBack(mBlockKeyBack);
            }
            
            return dialog;
        }
    }
    
}
