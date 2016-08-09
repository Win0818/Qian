package com.qianft.m.qian.callback;

public interface DownloadListener {

        public void downloading(int progress);
        public void downloaded();
        public void downloadFailure();
        
}
