package com.bambuserplayer;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.bambuser.broadcaster.BroadcastPlayer;
import com.bambuser.broadcaster.PlayerState;
import com.bambuser.broadcaster.SurfaceViewWithAutoAR;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;

/**
 * Copyright Bambuser AB 2018
 */

public class BambuserPlayerView extends RelativeLayout {

    BambuserPlayerViewViewManager manager;
    RelativeLayout mWrapperLayout;
    SurfaceViewWithAutoAR mVideoSurfaceView;
    BroadcastPlayer mBroadcastPlayer;
    boolean hasAddedLayout = false;
    String _resourceUri;
    String _applicationId;
    String _videoScaleMode = "aspectFit";
    BroadcastPlayer.AcceptType _requiredBroadcastState = BroadcastPlayer.AcceptType.ANY;
    boolean _timeShiftMode = false;
    float _volume = 0.5f;
    int _duration = -1;
    BroadcastPlayer.LatencyMode _latencyMode = BroadcastPlayer.LatencyMode.LOW;
    final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    public BambuserPlayerView(ThemedReactContext context, BambuserPlayerViewViewManager manager) {
        super(context);
        this.manager = manager;
        init(null, 0);
    }

    public BambuserPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mWrapperLayout = new RelativeLayout(getContext());
        mWrapperLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mWrapperLayout.setBackgroundColor(Color.parseColor("#000000"));
        mVideoSurfaceView = new SurfaceViewWithAutoAR(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mWrapperLayout.addView(mVideoSurfaceView, layoutParams);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWrapperLayout.layout(0, 0, right, bottom);
        if (!hasAddedLayout) {
            this.hasAddedLayout = true;
            addView(mWrapperLayout, 0);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        WritableMap event = new WritableNativeMap();
        sendEvent(event, "onReady");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    boolean playerExists() {
        return (mBroadcastPlayer != null);
    }

    void play() {
        if (playerExists()) {
            if (mBroadcastPlayer.getState() != PlayerState.CLOSED) {
                mBroadcastPlayer.start();
                return;
            }
        }
        loadAndPlay();
    }

    void pause() {
        if (playerExists()) {
            if (mBroadcastPlayer.isPlaying()) {
                mBroadcastPlayer.pause();
            }
        }
    }

    void stop() {
        if (playerExists()) {
            mBroadcastPlayer.close();
            mBroadcastPlayerObserver.onStateChange(PlayerState.CLOSED);
        }
    }

    void seekTo(int position) {
        int positionInMs = position * 1000;
        if (positionInMs < 0) positionInMs = 0;
        if (playerExists()) {
            if (mBroadcastPlayer.getState() != PlayerState.CLOSED) {
                mBroadcastPlayer.seekTo(positionInMs);
                sendOnProgressUpdate();
            }
        }
    }

    void setApplicationId(String applicationId) {
        if (applicationId != null) {
            _applicationId = applicationId;
        }
    }

    void setResourceUri(String resourceUri) {
        if (resourceUri != null) {
            _resourceUri = resourceUri;
        }
    }

    void setVideoScaleMode(String videoScaleMode) {
        if (videoScaleMode != null) {
            _videoScaleMode = videoScaleMode;
        }
    }

    void setRequiredBroadcastState(BroadcastPlayer.AcceptType requiredBroadcastState) {
        if (requiredBroadcastState != null) {
            _requiredBroadcastState = requiredBroadcastState;
        }
    }

    void setLatencyMode(BroadcastPlayer.LatencyMode latencyMode) {
        if (latencyMode != null) {
            _latencyMode = latencyMode;
        }
    }

    void setTimeShiftMode(boolean timeShiftMode) {
        _timeShiftMode = timeShiftMode;
    }

    void setVolume(float volume) {
        _volume = volume;
        if (playerExists()) {
            mBroadcastPlayer.setAudioVolume(_volume);
        }
    }

    void loadAndPlay() {
        if (playerExists()) {
            mBroadcastPlayer.close();
        }
        switch (_videoScaleMode) {
            case "aspectFit":
                mVideoSurfaceView.setCropToParent(false);
                break;
            case "aspectFill":
                mVideoSurfaceView.setCropToParent(true);
                break;
            case "scaleToFill":
                mVideoSurfaceView.setCropToParent(false);
                break;
        }
        _duration = -1;
        mBroadcastPlayer = null;
        mBroadcastPlayer = new BroadcastPlayer(getContext(), _resourceUri, _applicationId, mBroadcastPlayerObserver);
        mBroadcastPlayer.setViewerCountObserver(mBroadcastViewerCountObserver);
        mBroadcastPlayer.setTimeshiftMode(_timeShiftMode);
        mBroadcastPlayer.setAcceptType(_requiredBroadcastState);
        mBroadcastPlayer.setLatencyMode(_latencyMode);
        mBroadcastPlayer.setSurfaceView(mVideoSurfaceView);
        mBroadcastPlayer.setAudioVolume(_volume);
        mBroadcastPlayer.load();
    }

    void sendOnProgressUpdate() {
        if (playerExists()) {
            WritableMap event = new WritableNativeMap();
            boolean live = mBroadcastPlayer.isTypeLive();
            event.putString("live", String.valueOf(live));
            int position = mBroadcastPlayer.getCurrentPosition();
            event.putString("currentPosition", String.valueOf(position / 1000));
            int duration = mBroadcastPlayer.getDuration();
            if (duration > -1) {
                event.putString("duration", String.valueOf(duration / 1000));
            }
            sendEvent(event, "onProgressUpdate");
        }
    }

    private final Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            sendOnProgressUpdate();
            mMainThreadHandler.postDelayed(mProgressRunnable, 1000);
        }
    };

    void startTimer() {
        stopTimer();
        mMainThreadHandler.postDelayed(mProgressRunnable, 1000);
    }

    void stopTimer() {
        mMainThreadHandler.removeCallbacks(mProgressRunnable);
    }

    BroadcastPlayer.Observer mBroadcastPlayerObserver = new BroadcastPlayer.Observer() {
        @Override
        public void onStateChange(PlayerState playerState) {
            WritableMap event = new WritableNativeMap();
            switch (playerState) {
                case CONSTRUCTION:
                    break;
                case LOADING:
                    sendEvent(event, "onLoading");
                    break;
                case BUFFERING:
                    break;
                case PLAYING:
                    sendEvent(event, "onPlaying");
                    startTimer();
                    break;
                case PAUSED:
                    sendEvent(event, "onPaused");
                    stopTimer();
                    break;
                case COMPLETED:
                    sendEvent(event, "onPlaybackComplete");
                    stopTimer();
                    break;
                case ERROR:
                    sendEvent(event, "onPlaybackError");
                    stopTimer();
                    break;
                case CLOSED:
                    sendEvent(event, "onStopped");
                    stopTimer();
                    break;
            }
        }

        @Override
        public void onBroadcastLoaded(boolean live, int width, int height) {
            requestLayout();
        }
    };

    BroadcastPlayer.ViewerCountObserver mBroadcastViewerCountObserver = new BroadcastPlayer.ViewerCountObserver() {
        @Override
        public void onCurrentViewersUpdated(long l) {
            WritableMap event = new WritableNativeMap();
            event.putString("viewers", String.valueOf(l));
            sendEvent(event, "onCurrentViewerCountUpdate");
        }

        @Override
        public void onTotalViewersUpdated(long l) {
            WritableMap event = new WritableNativeMap();
            event.putString("viewers", String.valueOf(l));
            sendEvent(event, "onTotalViewerCountUpdate");
        }
    };

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    private void sendEvent(WritableMap event, String name) {
        if (manager != null) {
            manager.pushEvent((ThemedReactContext) getContext(), this, name, event);
        }
    }

}
