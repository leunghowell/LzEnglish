package com.jiubai.lzenglish.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.badoo.mobile.util.WeakHandler;
import com.danikula.videocache.HttpProxyCacheServer;
import com.jiubai.lzenglish.App;
import com.jiubai.lzenglish.R;
import com.jiubai.lzenglish.adapter.PlayVideoAdapter;
import com.jiubai.lzenglish.adapter.PlayVideoRecommendAdapter;
import com.jiubai.lzenglish.adapter.PopupDownloadVideoAdapter;
import com.jiubai.lzenglish.bean.DetailedSeason;
import com.jiubai.lzenglish.bean.OpeningClosingImage;
import com.jiubai.lzenglish.bean.PrefetchVideo;
import com.jiubai.lzenglish.bean.Video;
import com.jiubai.lzenglish.bean.WatchHistory;
import com.jiubai.lzenglish.common.StatusBarUtil;
import com.jiubai.lzenglish.common.UtilBox;
import com.jiubai.lzenglish.config.Config;
import com.jiubai.lzenglish.config.Constants;
import com.jiubai.lzenglish.manager.DownloadManager;
import com.jiubai.lzenglish.manager.WatchHistoryManager;
import com.jiubai.lzenglish.net.DownloadUtil;
import com.jiubai.lzenglish.presenter.GetCartoonInfoPresenterImpl;
import com.jiubai.lzenglish.ui.iview.IGetCartoonInfoView;
import com.jiubai.lzenglish.widget.CustomViewPager;
import com.jiubai.lzenglish.widget.JCVideoPlayerStandard;
import com.jiubai.lzenglish.widget.recorder.AudioPlayer;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCUtils;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerManager;
import me.shaohui.bottomdialog.BottomDialog;

import static android.view.View.FOCUS_UP;

public class PlayVideoActivity extends BaseActivity implements IGetCartoonInfoView, PlayVideoAdapter.OnStateChangeListener {

    @Bind(R.id.videoplayer)
    JCVideoPlayerStandard mJVideoPlayer;

    @Bind(R.id.recyclerView_video)
    RecyclerView mVideoRecyclerView;

    @Bind(R.id.recyclerView_recommend)
    RecyclerView mRecommendRecyclerView;

    @Bind(R.id.layout_abstract_content)
    LinearLayout mAbstractContentLayout;

    @Bind(R.id.textView_abstract_content)
    TextView mAbstractContentTextView;

    @Bind(R.id.imageView_abstract)
    ImageView mAbstractImageView;

    @Bind(R.id.textView_ep)
    TextView mEPTextView;

    @Bind(R.id.imageView_ep)
    ImageView mEPImageView;

    @Bind(R.id.textView_title)
    TextView mTitleTextView;

    @Bind(R.id.textView_keywords)
    TextView mKeywordsTextView;

    @Bind(R.id.view_cover)
    View mCoverView;

    @Bind(R.id.imageView_lock)
    ImageView mLockImageView;

    @Bind(R.id.textView_abstract_text)
    TextView mAbstractTextTextView;

    @Bind(R.id.framelayout_bottom)
    FrameLayout mBottomLayout;

    @Bind(R.id.imageView_back)
    ImageView mBackImageView;

    @Bind(R.id.scrollView)
    NestedScrollView mScrollView;

    @Bind(R.id.layout_image)
    RelativeLayout mImageLayout;

    @Bind(R.id.viewPager)
    CustomViewPager mViewPager;

    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;

    @Bind(R.id.imageView_play)
    ImageView mPlayImageView;

    @Bind(R.id.button_shadowing)
    Button mShadowingButton;

    private ArrayList<String> list = new ArrayList<>();
    private PlayVideoAdapter mVideoAdapter;
    private PlayVideoRecommendAdapter mRecommendAdapter;

    private DownloadManager mDownloadManager;

    private WeakHandler handler;
    private WeakHandler popupHandler;
    private WeakHandler fullScreenHandler;

    private DetailedSeason mDetailedSeason;
    private ArrayList<Video> videoList;

    private WeakHandler progressHandler;
    private int which = 0;

    private int videoId;
    private int currentVideoIndex = 0;

    private boolean fromQRScan = false;

    private boolean notPlaying = true;

    private boolean itemClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        StatusBarUtil.StatusBarDarkMode(this, Config.DeviceType);

        ButterKnife.bind(this);

        mDownloadManager = DownloadManager.getInstance();

        videoId = getIntent().getIntExtra("videoId", 0);
        fromQRScan = getIntent().getBooleanExtra("fromQRScan", false);

        initView();
    }

    private void initView() {
        UtilBox.showLoading(this, false);

        mCoverView.setVisibility(View.VISIBLE);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        mVideoRecyclerView.setLayoutManager(gridLayoutManager);
        mVideoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecommendRecyclerView.setLayoutManager(linearLayoutManager);
        mRecommendRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecommendAdapter = new PlayVideoRecommendAdapter(this, list);
        mRecommendRecyclerView.setAdapter(mRecommendAdapter);

        mJVideoPlayer.setUp("", JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBackImageView.getLayoutParams();
        params.setMargins(0, Config.StatusbarHeight, 0, 0);
        mBackImageView.setLayoutParams(params);

        new GetCartoonInfoPresenterImpl(this).getVideoList(videoId);
    }

    @Override
    public void onGetVideoListResult(boolean result, String info, Object extras) {
        if (result) {
            mDetailedSeason = (DetailedSeason) extras;

            videoList = (ArrayList<Video>) mDetailedSeason.getVideoList();

            for (int i = 0; i < videoList.size(); i++) {
                if (videoList.get(i).getId() == videoId) {
                    currentVideoIndex = i;
                    break;
                }
            }

            mTitleTextView.setText(videoList.get(currentVideoIndex).getName());
            mKeywordsTextView.setText(mDetailedSeason.getSeoKeywords());
            mKeywordsTextView.setVisibility(View.GONE);
            mAbstractContentTextView.setText(videoList.get(currentVideoIndex).getNote());

            setupPlayer(currentVideoIndex);

            mVideoAdapter = new PlayVideoAdapter(this, (ArrayList<Video>) mDetailedSeason.getVideoList());
            mVideoAdapter.setListener(this);
            mVideoAdapter.setCurrentVideo(currentVideoIndex);
            mVideoRecyclerView.setAdapter(mVideoAdapter);

            mEPTextView.setText("第1集/共" + mDetailedSeason.getVideoList().size() + "集");

            if (TextUtils.isEmpty(mAbstractContentTextView.getText())) {
                mAbstractTextTextView.setVisibility(View.GONE);
                mAbstractImageView.setVisibility(View.GONE);
            }

            if (videoList.get(currentVideoIndex).isAllowReview()) {
                mLockImageView.setVisibility(View.GONE);
            } else {
                mLockImageView.setVisibility(View.VISIBLE);
            }

            if (videoList.get(currentVideoIndex).isHasReview()) {
                mBottomLayout.setVisibility(View.VISIBLE);
            } else {
                mBottomLayout.setVisibility(View.GONE);
            }

            mScrollView.fullScroll(FOCUS_UP);

            handler = new WeakHandler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    Log.i("text", "working");

                    PlayVideoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (JCMediaManager.instance().mediaPlayer.isPlaying()) {
                                    Log.i("text", JCMediaManager.instance().mediaPlayer.getCurrentPosition() / 1000
                                            + " -- " + JCMediaManager.instance().mediaPlayer.getDuration() / 1000);

                                    Video video = videoList.get(currentVideoIndex);

                                    WatchHistory watchHistory = new WatchHistory(
                                            video.getId(),
                                            video.getName(),
                                            JCMediaManager.instance().mediaPlayer.getDuration(),
                                            JCMediaManager.instance().mediaPlayer.getCurrentPosition(),
                                            -99,
                                            new Date().getTime(),
                                            video.getHeadImg(),
                                            false
                                    );

                                    if (currentVideoIndex + 1 < videoList.size()
                                            && videoList.get(currentVideoIndex + 1).isAllowWatch()) {
                                        watchHistory.setNextVideoId(videoList.get(currentVideoIndex + 1).getId());
                                    }

                                    WatchHistoryManager.getInstance().saveHistory(watchHistory);

                                    if (JCMediaManager.instance().mediaPlayer.getCurrentPosition() * 1.0
                                            / JCMediaManager.instance().mediaPlayer.getDuration() >= 0.8) {
                                        new GetCartoonInfoPresenterImpl(null).finishedWatched(
                                                videoList.get(currentVideoIndex).getId());
                                    } else {
                                        new GetCartoonInfoPresenterImpl(null).saveWatchHistory(
                                                videoList.get(currentVideoIndex).getId(),
                                                JCMediaManager.instance().mediaPlayer.getCurrentPosition());
                                    }
                                }
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                                JCMediaManager.instance().mediaPlayer = new MediaPlayer();
                            }
                        }
                    });

                    handler.sendEmptyMessageDelayed(0, 5000);

                    return false;
                }
            });

            handler.sendEmptyMessage(0);

            mImageLayout.setVisibility(View.GONE);
            mPlayImageView.setVisibility(View.GONE);

            mDownloadManager.setListener(new DownloadManager.OnProgressChangedListener() {
                @Override
                public void onChanged(int index) {

                }

                @Override
                public void onCompletion(int id) {
                    if (videoList.get(currentVideoIndex).getId() == id) {
                        if (notPlaying) {
                            setupPlayer(currentVideoIndex);
                        }
                    }
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCoverView.setVisibility(View.GONE);

                    UtilBox.dismissLoading(false);

                    if (fromQRScan) {
                        fromQRScan = false;

                        mJVideoPlayer.startButton.performClick();
                    }
                }
            }, 500);
        } else {
            UtilBox.dismissLoading(false);

            UtilBox.alert(this, info,
                    "重试", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new GetCartoonInfoPresenterImpl(PlayVideoActivity.this).getVideoList(videoId);
                        }
                    },
                    "返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UtilBox.returnActivity(PlayVideoActivity.this);
                        }
                    });
        }
    }

    private void initViewPager(final ArrayList<OpeningClosingImage> images) {
        mImageLayout.setVisibility(View.GONE);
        mPlayImageView.setVisibility(View.GONE);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mImageLayout.getLayoutParams();
        layoutParams.height = mJVideoPlayer.getHeight();
        mImageLayout.setLayoutParams(layoutParams);

        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return images.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                if (object != null) {
                    int count = container.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View childView = container.getChildAt(i);
                        if (childView == object) {
                            container.removeView(childView);
                            break;
                        }
                    }
                }
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {

                View rootView = LayoutInflater.from(PlayVideoActivity.this).inflate(R.layout.item_opening_closing, null);

                ImageView imageView = rootView.findViewById(R.id.imageView);

                ImageLoader.getInstance().displayImage(Config.ResourceUrl + images.get(position).getUrl(), imageView);

                container.addView(rootView);

                return rootView;
            }
        };

        int totalSecond = 0;
        final ArrayList<Integer> seconds = new ArrayList<>();
        for (OpeningClosingImage image : images) {
            totalSecond += image.getInterval();
            seconds.add(totalSecond);
        }
        mProgressBar.setMax(totalSecond);

        mViewPager.setOffscreenPageLimit(3);

        mViewPager.setAdapter(pagerAdapter);

        progressHandler = new WeakHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == 0) {
                    if (mProgressBar.getProgress() < mProgressBar.getMax()) {
                        if (seconds.contains(mProgressBar.getProgress())) {
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        }

                        mProgressBar.setProgress(mProgressBar.getProgress() + 1);

                        Message newMessage = new Message();
                        newMessage.what = 0;
                        newMessage.arg1 = message.arg1;
                        progressHandler.sendMessageDelayed(newMessage, 1000);
                    } else {
                        toggleOpeningClosingImage(0, false);
                        toggleOpeningClosingImage(1, false);

                        // 如果是播完片头，就点击一下开始播放正片
                        if (message.arg1 == 0) {
                            mJVideoPlayer.startButton.performClick();
                        }
                    }
                }
                return false;
            }
        });
    }

    private void toggleOpeningClosingImage(int which, boolean on) {
        this.which = which;

        if (on) {
            String voiceUrl = "";
            if (which == 0) {
                voiceUrl = Config.ResourceUrl + videoList.get(currentVideoIndex).getOpeningVoice();
            } else if (which == 1) {
                voiceUrl = Config.ResourceUrl + videoList.get(currentVideoIndex).getClosingVoice();
            }

            if (voiceUrl.contains("http")) {
                HttpProxyCacheServer proxy = App.getProxy(this);
                String proxyUrl = proxy.getProxyUrl(voiceUrl);

                AudioPlayer.getInstance().playUrl(proxyUrl, -99);
            } else {
                AudioPlayer.getInstance().playUrl(voiceUrl, -99);
            }

            mImageLayout.setVisibility(View.VISIBLE);

            if (progressHandler != null) {
                Message message = new Message();
                message.what = 0;
                message.arg1 = which;
                progressHandler.sendMessage(message);
            }
        } else {
            if (AudioPlayer.getInstance().mediaPlayer != null
                    && AudioPlayer.getInstance().mediaPlayer.isPlaying()) {
                AudioPlayer.getInstance().currentId = -99;
                AudioPlayer.getInstance().pause();
                AudioPlayer.getInstance().stop();
            }

            mImageLayout.setVisibility(View.GONE);

            if (progressHandler != null) {
                progressHandler.removeMessages(0);
            }

            mProgressBar.setProgress(0);
            if (mViewPager.getAdapter() != null) {
                mViewPager.setCurrentItem(0, false);
            }
        }
    }

    @OnClick({R.id.layout_image, R.id.imageView_play})
    public void onImageLayoutClick(View view) {
        // 暂停播放
        if (mPlayImageView.getVisibility() == View.GONE) {

            mPlayImageView.setVisibility(View.VISIBLE);

            if (AudioPlayer.getInstance().mediaPlayer != null
                    && AudioPlayer.getInstance().mediaPlayer.isPlaying()) {
                AudioPlayer.getInstance().currentId = -99;
                AudioPlayer.getInstance().pause();
            }

            progressHandler.removeMessages(0);
        }
        // 继续播放
        else {
            mPlayImageView.setVisibility(View.GONE);

            if (AudioPlayer.getInstance().mediaPlayer != null
                    && !AudioPlayer.getInstance().mediaPlayer.isPlaying()) {
                AudioPlayer.getInstance().currentId = -99;
                AudioPlayer.getInstance().play();
            }

            Message message = new Message();
            message.what = 0;
            message.arg1 = which;
            // todo 这个延迟时间只能随意设定一下了，取均值500
            progressHandler.sendMessageDelayed(message, 500);
        }
    }

    @Override
    public void onItemClick(int position) {
        if (currentVideoIndex != position) {
            currentVideoIndex = position;

            mEPTextView.setText("第" + (position + 1) + "集/共" + mDetailedSeason.getVideoList().size() + "集");

            setupPlayer(position);

            mTitleTextView.setText(videoList.get(position).getName());
            mKeywordsTextView.setText(mDetailedSeason.getSeoKeywords());
            mKeywordsTextView.setVisibility(View.GONE);
            mAbstractContentTextView.setText(videoList.get(position).getNote());

            if (videoList.get(position).isHasReview()) {
                mBottomLayout.setVisibility(View.VISIBLE);
            } else {
                mBottomLayout.setVisibility(View.GONE);
            }

            if (videoList.get(position).isAllowReview()) {
                mLockImageView.setVisibility(View.GONE);
            } else {
                mLockImageView.setVisibility(View.VISIBLE);
            }

            toggleOpeningClosingImage(0, false);
            toggleOpeningClosingImage(1, false);
        } else {
            mJVideoPlayer.startButton.performClick();
        }
    }

    private void setupPlayer(final int position) {
        notPlaying = true;

        final int index = mDownloadManager.getPrefetchVideoByVideoId(videoList.get(position).getId());

        if (index != -1
                && mDownloadManager.getPrefetchVideos().get(index).getVideoStatus()
                == PrefetchVideo.VideoStatus.Downloaded) {
            Log.i("prefetch", DownloadUtil.getFileName(mDownloadManager.getPrefetchVideos().get(index).getVideoId() + ".mp4"));

            setupOpeningAndClosing(DownloadUtil.getFileName(mDownloadManager.getPrefetchVideos().get(index).getVideoId() + ".mp4"));
        } else {
            HttpProxyCacheServer proxy = App.getProxy(this);
            String proxyUrl = proxy.getProxyUrl(Config.ResourceUrl + videoList.get(position).getVideo());

            setupOpeningAndClosing(proxyUrl);
        }
        mJVideoPlayer.thumbImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mJVideoPlayer.fullscreenButton.setVisibility(View.VISIBLE);

        ImageLoader.getInstance().displayImage(Config.ResourceUrl + videoList.get(position).getHeadImg(), mJVideoPlayer.thumbImageView);
    }

    private void setupOpeningAndClosing(final String url) {
        final HttpProxyCacheServer proxy = App.getProxy(this);

        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mJVideoPlayer.fullscreenButton.getLayoutParams();
        final LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(UtilBox.dip2px(this, 16), 1);

        mJVideoPlayer.fullscreenButton.setLayoutParams(emptyParams);

        final View.OnClickListener outListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(Constants.TAG, "myOnClick");

                notPlaying = false;

                fullScreenHandler.sendEmptyMessageDelayed(1, 2000);

                mJVideoPlayer.onClick(mJVideoPlayer.startButton);

                if (mVideoAdapter.playingVideo == -1) {
                    mVideoAdapter.playingVideo = currentVideoIndex;
                    mVideoAdapter.notifyDataSetChanged();
                } else {
                    mVideoAdapter.playingVideo = -1;
                    mVideoAdapter.notifyDataSetChanged();
                }
            }
        };

        final View.OnClickListener firstListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(Constants.TAG, "myOnClick");

                notPlaying = false;

                fullScreenHandler.sendEmptyMessageDelayed(1, 2000);

                JCVideoPlayerManager.getFirstFloor().onClick(JCVideoPlayerManager.getFirstFloor().startButton);

                if (mVideoAdapter.playingVideo == -1) {
                    mVideoAdapter.playingVideo = currentVideoIndex;
                    mVideoAdapter.notifyDataSetChanged();
                } else {
                    mVideoAdapter.playingVideo = -1;
                    mVideoAdapter.notifyDataSetChanged();
                }
            }
        };

        final View.OnClickListener secondListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(Constants.TAG, "myOnClick");

                notPlaying = false;

                fullScreenHandler.sendEmptyMessageDelayed(1, 2000);

                JCVideoPlayerManager.getSecondFloor().onClick(JCVideoPlayerManager.getSecondFloor().startButton);

                if (mVideoAdapter.playingVideo == -1) {
                    mVideoAdapter.playingVideo = currentVideoIndex;
                    mVideoAdapter.notifyDataSetChanged();
                } else {
                    mVideoAdapter.playingVideo = -1;
                    mVideoAdapter.notifyDataSetChanged();
                }
            }
        };

        mJVideoPlayer.startButton.setOnClickListener(outListener);

        fullScreenHandler = new WeakHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == 1) {
                    try {
                        if (JCMediaManager.instance().mediaPlayer.getCurrentPosition() != 0
                                && JCMediaManager.instance().mediaPlayer.getCurrentPosition() / 1000.0 >= 1.5
                                && JCMediaManager.instance().mediaPlayer.getCurrentPosition() / 1000.0 <= 3.5) {
                            mJVideoPlayer.fullscreenButton.performClick();

                            JCUtils.getAppCompActivity(PlayVideoActivity.this).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                            JCVideoPlayerManager.getFirstFloor().startButton.setOnClickListener(firstListener);

                            JCVideoPlayerManager.getSecondFloor().startButton.setOnClickListener(secondListener);

                            fullScreenHandler.removeMessages(1);
                        } else {
                            fullScreenHandler.sendEmptyMessageDelayed(1, 1000);
                        }
                    } catch (IllegalStateException exception) {
                        exception.printStackTrace();

                        fullScreenHandler.sendEmptyMessageDelayed(1, 1000);
                    }
                }

                return false;
            }
        });

        final Video currentVideo = videoList.get(currentVideoIndex);

        // todo 以下有3*3=9种情况，用了最糟糕的写法
        // 无片头片尾
        if (TextUtils.isEmpty(currentVideo.getOpeningVideo())
                && TextUtils.isEmpty(currentVideo.getOpeningVoice())
                && TextUtils.isEmpty(currentVideo.getClosingVideo())
                && TextUtils.isEmpty(currentVideo.getClosingVoice())) {

            mJVideoPlayer.setUp(url, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

            mJVideoPlayer.seekToInAdvance = 1;

            mJVideoPlayer.fullscreenButton.setLayoutParams(params);

            //mJVideoPlayer.startButton.performClick();

            // 播放完毕，初始化播放器
            mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        setupPlayer(currentVideoIndex);

                        showShadowingDialog();

                        mVideoAdapter.playingVideo = -1;
                        mVideoAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStartTrackingTouch(seekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStopTrackingTouch(seekBar);
                }
            });
        }
        // 无片头+视频片尾
        else if (TextUtils.isEmpty(currentVideo.getOpeningVideo())
                && TextUtils.isEmpty(currentVideo.getOpeningVoice())
                && !TextUtils.isEmpty(currentVideo.getClosingVideo())) {
            mJVideoPlayer.setUp(url, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

            mJVideoPlayer.seekToInAdvance = 1;

            mJVideoPlayer.fullscreenButton.setLayoutParams(params);

            //mJVideoPlayer.startButton.performClick();

            mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        if (mJVideoPlayer.currentScreen == JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN) {
                            mJVideoPlayer.fullscreenButton.performClick();
                        }

                        mJVideoPlayer.setUp(proxy.getProxyUrl(Config.ResourceUrl + currentVideo.getClosingVideo()),
                                JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

                        mJVideoPlayer.startButton.performClick();

                        mJVideoPlayer.fullscreenButton.setLayoutParams(emptyParams);

                        // 播放完毕，初始化播放器
                        mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (progress == 100) {
                                    setupPlayer(currentVideoIndex);

                                    showShadowingDialog();
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStartTrackingTouch(seekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStopTrackingTouch(seekBar);
                }
            });
        }
        // 无片头+图片片尾
        else if (TextUtils.isEmpty(currentVideo.getOpeningVideo())
                && TextUtils.isEmpty(currentVideo.getOpeningVoice())
                && TextUtils.isEmpty(currentVideo.getClosingVideo())
                && !TextUtils.isEmpty(currentVideo.getClosingVoice())) {
            mJVideoPlayer.setUp(url, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

            mJVideoPlayer.seekToInAdvance = 1;

            //mJVideoPlayer.startButton.performClick();

            mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        if (mJVideoPlayer.currentScreen == JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN) {
                            mJVideoPlayer.fullscreenButton.performClick();
                        }

                        initViewPager(videoList.get(currentVideoIndex).getClosingImages());

                        toggleOpeningClosingImage(1, true);

                        setupPlayer(currentVideoIndex);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStartTrackingTouch(seekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStopTrackingTouch(seekBar);
                }
            });
        }
        // 视频片头+无片尾
        else if (!TextUtils.isEmpty(currentVideo.getOpeningVideo())
                && TextUtils.isEmpty(currentVideo.getClosingVideo())
                && TextUtils.isEmpty(currentVideo.getClosingVoice())) {
            mJVideoPlayer.setUp(proxy.getProxyUrl(Config.ResourceUrl + currentVideo.getOpeningVideo()),
                    JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

            mJVideoPlayer.fullscreenButton.setLayoutParams(emptyParams);

            mJVideoPlayer.startButton.performClick();

            mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        mJVideoPlayer.setUp(url, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

                        mJVideoPlayer.seekToInAdvance = 1;

                        mJVideoPlayer.fullscreenButton.setLayoutParams(params);

                        mJVideoPlayer.startButton.performClick();

                        // 播放完毕，初始化播放器
                        mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (progress == 100) {
                                    setupPlayer(currentVideoIndex);

                                    showShadowingDialog();
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                                mJVideoPlayer.onStartTrackingTouch(seekBar);
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                mJVideoPlayer.onStopTrackingTouch(seekBar);
                            }
                        });
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        // 视频片头+视频片尾
        else if (!TextUtils.isEmpty(currentVideo.getOpeningVideo())
                && !TextUtils.isEmpty(currentVideo.getClosingVideo())) {
            mJVideoPlayer.setUp(proxy.getProxyUrl(Config.ResourceUrl + currentVideo.getOpeningVideo()),
                    JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

            mJVideoPlayer.fullscreenButton.setLayoutParams(emptyParams);

            mJVideoPlayer.startButton.performClick();

            mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        mJVideoPlayer.setUp(url, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

                        mJVideoPlayer.seekToInAdvance = 1;

                        mJVideoPlayer.fullscreenButton.setLayoutParams(params);

                        mJVideoPlayer.startButton.performClick();

                        mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (progress == 100) {
                                    if (mJVideoPlayer.currentScreen == JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN) {
                                        mJVideoPlayer.fullscreenButton.performClick();
                                    }

                                    mJVideoPlayer.setUp(proxy.getProxyUrl(Config.ResourceUrl + currentVideo.getClosingVideo()),
                                            JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

                                    mJVideoPlayer.fullscreenButton.setLayoutParams(emptyParams);

                                    mJVideoPlayer.startButton.performClick();

                                    // 播放完毕，初始化播放器
                                    mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                        @Override
                                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                            if (progress == 100) {
                                                setupPlayer(currentVideoIndex);

                                                showShadowingDialog();
                                            }
                                        }

                                        @Override
                                        public void onStartTrackingTouch(SeekBar seekBar) {

                                        }

                                        @Override
                                        public void onStopTrackingTouch(SeekBar seekBar) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                                mJVideoPlayer.onStartTrackingTouch(seekBar);
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                mJVideoPlayer.onStopTrackingTouch(seekBar);
                            }
                        });
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        // 视频片头+图片片尾
        else if (!TextUtils.isEmpty(currentVideo.getOpeningVideo())
                && TextUtils.isEmpty(currentVideo.getClosingVideo())
                && !TextUtils.isEmpty(currentVideo.getClosingVoice())) {
            mJVideoPlayer.setUp(proxy.getProxyUrl(Config.ResourceUrl + currentVideo.getOpeningVideo()),
                    JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

            mJVideoPlayer.fullscreenButton.setLayoutParams(emptyParams);

            mJVideoPlayer.startButton.performClick();

            mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        mJVideoPlayer.setUp(url, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

                        mJVideoPlayer.seekToInAdvance = 1;

                        mJVideoPlayer.fullscreenButton.setLayoutParams(params);

                        mJVideoPlayer.startButton.performClick();

                        mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (progress == 100) {
                                    if (mJVideoPlayer.currentScreen == JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN) {
                                        mJVideoPlayer.fullscreenButton.performClick();
                                    }

                                    initViewPager(videoList.get(currentVideoIndex).getClosingImages());

                                    toggleOpeningClosingImage(1, true);

                                    setupPlayer(currentVideoIndex);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                                mJVideoPlayer.onStartTrackingTouch(seekBar);
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                mJVideoPlayer.onStopTrackingTouch(seekBar);
                            }
                        });
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        // 图片片头+无片尾
        else if (TextUtils.isEmpty(currentVideo.getOpeningVideo())
                && !TextUtils.isEmpty(currentVideo.getOpeningVoice())
                && TextUtils.isEmpty(currentVideo.getClosingVideo())
                && TextUtils.isEmpty(currentVideo.getClosingVoice())) {
            mJVideoPlayer.setUp(url, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

            mJVideoPlayer.seekToInAdvance = 1;

            mJVideoPlayer.fullscreenButton.setLayoutParams(params);

            initViewPager(videoList.get(currentVideoIndex).getOpeningImages());

            toggleOpeningClosingImage(0, true);

            // 播放完毕，初始化播放器
            mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        setupPlayer(currentVideoIndex);

                        showShadowingDialog();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStartTrackingTouch(seekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStopTrackingTouch(seekBar);
                }
            });
        }
        // 图片片头+视频片尾
        else if (TextUtils.isEmpty(currentVideo.getOpeningVideo())
                && !TextUtils.isEmpty(currentVideo.getOpeningVoice())
                && !TextUtils.isEmpty(currentVideo.getClosingVideo())) {
            mJVideoPlayer.setUp(url, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

            mJVideoPlayer.seekToInAdvance = 1;

            mJVideoPlayer.fullscreenButton.setLayoutParams(params);

            initViewPager(videoList.get(currentVideoIndex).getOpeningImages());

            toggleOpeningClosingImage(0, true);

            mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        if (mJVideoPlayer.currentScreen == JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN) {
                            mJVideoPlayer.fullscreenButton.performClick();
                        }

                        mJVideoPlayer.setUp(proxy.getProxyUrl(Config.ResourceUrl + currentVideo.getClosingVideo()),
                                JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

                        mJVideoPlayer.fullscreenButton.setLayoutParams(emptyParams);

                        mJVideoPlayer.startButton.performClick();

                        // 播放完毕，初始化播放器
                        mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (progress == 100) {
                                    setupPlayer(currentVideoIndex);

                                    showShadowingDialog();
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStartTrackingTouch(seekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStopTrackingTouch(seekBar);
                }
            });
        }
        // 图片片头+图片片尾
        else if (TextUtils.isEmpty(currentVideo.getOpeningVideo())
                && !TextUtils.isEmpty(currentVideo.getOpeningVoice())
                && TextUtils.isEmpty(currentVideo.getClosingVideo())
                && !TextUtils.isEmpty(currentVideo.getClosingVoice())) {
            mJVideoPlayer.setUp(url, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

            mJVideoPlayer.seekToInAdvance = 1;

            mJVideoPlayer.fullscreenButton.setLayoutParams(params);

            initViewPager(videoList.get(currentVideoIndex).getOpeningImages());

            toggleOpeningClosingImage(0, true);

            mJVideoPlayer.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 100) {
                        if (mJVideoPlayer.currentScreen == JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN) {
                            mJVideoPlayer.fullscreenButton.performClick();
                        }

                        initViewPager(videoList.get(currentVideoIndex).getClosingImages());

                        toggleOpeningClosingImage(1, true);

                        setupPlayer(currentVideoIndex);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStartTrackingTouch(seekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mJVideoPlayer.onStopTrackingTouch(seekBar);
                }
            });
        }
        // 其他情况(出错)
        else {
            Toast.makeText(PlayVideoActivity.this, "视频信息出错", Toast.LENGTH_SHORT).show();
        }
    }

    private void showShadowingDialog() {
        if (videoList.get(currentVideoIndex).isHasReview()
                && mDetailedSeason.isAllowShadowing()) {
            UtilBox.colorfulAlert(PlayVideoActivity.this, "您已看完", "去跟读",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mShadowingButton.performClick();

                            UtilBox.colorfulDoubleAlertDialog.dismiss();
                        }
                    }, "取消");
        }
    }

    @OnClick({R.id.layout_abstract})
    public void setAbstractVisibility(View view) {
        if (TextUtils.isEmpty(mAbstractContentTextView.getText())) {
            mAbstractTextTextView.setVisibility(View.GONE);
            mAbstractImageView.setVisibility(View.GONE);
        } else {
            if (mAbstractContentTextView.getVisibility() == View.VISIBLE) {
                mAbstractContentTextView.setVisibility(View.GONE);
                mAbstractContentLayout.setVisibility(View.GONE);
                mAbstractImageView.setImageResource(R.drawable.to_right);
            } else {
                mAbstractContentTextView.setVisibility(View.VISIBLE);
                mAbstractContentLayout.setVisibility(View.VISIBLE);
                mAbstractImageView.setImageResource(R.drawable.to_down);
            }
        }
    }

    //@OnClick({R.id.layout_ep})
    public void setEPHeight(View view) {
        if (mVideoRecyclerView.isNestedScrollingEnabled()) {
            UtilBox.setViewParams(mVideoRecyclerView, mVideoRecyclerView.getWidth(), UtilBox.dip2px(this, 68));
            mEPImageView.setImageResource(R.drawable.to_right);
            mVideoRecyclerView.setNestedScrollingEnabled(false);
        } else {
            ViewGroup.LayoutParams layoutParams = mVideoRecyclerView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mVideoRecyclerView.setLayoutParams(layoutParams);
            mEPImageView.setImageResource(R.drawable.to_down);
            mVideoRecyclerView.setNestedScrollingEnabled(true);
        }
    }

    @OnClick({R.id.layout_download})
    public void showDownloadPopup(View view) {
        final BottomDialog dialog = BottomDialog.create(getSupportFragmentManager());

        dialog.setLayoutRes(R.layout.popup_download_video)
                .setDimAmount(0.5f)
                .setHeight(UtilBox.getHeightPixels(this) - UtilBox.dip2px(this, 180 + 40))
                .setViewListener(new BottomDialog.ViewListener() {
                    @Override
                    public void bindView(View v) {
                        RecyclerView recyclerView = v.findViewById(R.id.recyclerView_popup);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(PlayVideoActivity.this, 5);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());

                        final Button checkDownloadButton = v.findViewById(R.id.button_check_downloaded);

                        final PopupDownloadVideoAdapter adapter = new PopupDownloadVideoAdapter(
                                PlayVideoActivity.this, (ArrayList<Video>) mDetailedSeason.getVideoList());
                        adapter.setListener(new PopupDownloadVideoAdapter.OnStateChangeListener() {
                            @Override
                            public void onItemClick(int position) {
                                checkDownloadButton.setText("查看缓存视频(" + mDownloadManager.getPrefetchVideos().size() + ")");
                                Toast.makeText(PlayVideoActivity.this, "正在缓存...", Toast.LENGTH_SHORT).show();
                            }
                        });
                        recyclerView.setAdapter(adapter);

                        popupHandler = new WeakHandler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message msg) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (adapter != null) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });

                                popupHandler.sendEmptyMessageDelayed(0, 1000);
                                return false;
                            }
                        });

                        popupHandler.sendEmptyMessage(0);

                        v.findViewById(R.id.imageView_close).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        if (mDownloadManager.getPrefetchVideos().size() == 0) {
                            checkDownloadButton.setText("查看缓存视频");
                        } else {
                            checkDownloadButton.setText("查看缓存视频(" + mDownloadManager.getPrefetchVideos().size() + ")");
                        }

                        checkDownloadButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                Intent intent = new Intent(PlayVideoActivity.this, DownloadActivity.class);
                                UtilBox.startActivity(PlayVideoActivity.this, intent, false);
                            }
                        });

                        final Button downloadAllButton = v.findViewById(R.id.button_download_all);

                        boolean hasDownloadedAll = true;

                        for (int i = 0; i < mDetailedSeason.getVideoList().size(); i++) {
                            if (DownloadManager.getInstance().getPrefetchVideoByVideoId(mDetailedSeason.getVideoList().get(i).getId()) == -1
                                    && mDetailedSeason.getVideoList().get(i).isAllowWatch()) {
                                hasDownloadedAll = false;
                                break;
                            }
                        }

                        if (hasDownloadedAll) {
                            downloadAllButton.setTextColor(Color.parseColor("#999999"));
                            downloadAllButton.setEnabled(false);
                            downloadAllButton.setClickable(false);
                        } else {
                            downloadAllButton.setTextColor(Color.parseColor("#484848"));
                            downloadAllButton.setEnabled(true);
                            downloadAllButton.setClickable(true);

                            downloadAllButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    UtilBox.alert(PlayVideoActivity.this,
                                            "确定要下载所有未缓存的视频？",
                                            "下载", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    for (int j = 0; j < mDetailedSeason.getVideoList().size(); j++) {
                                                        Video video = mDetailedSeason.getVideoList().get(j);

                                                        if (DownloadManager.getInstance().getPrefetchVideoByVideoId(video.getId()) == -1
                                                                && video.isAllowWatch()) {
                                                            DownloadManager.getInstance().downloadVideo(video.getId(), video.getName(),
                                                                    Config.ResourceUrl + video.getVideo(),
                                                                    Config.ResourceUrl + video.getHeadImg());
                                                        }
                                                    }

                                                    downloadAllButton.setTextColor(Color.parseColor("#484848"));
                                                    downloadAllButton.setEnabled(false);
                                                    downloadAllButton.setClickable(false);

                                                    adapter.notifyDataSetChanged();

                                                    checkDownloadButton.setText("查看缓存视频(" + mDownloadManager.getPrefetchVideos().size() + ")");
                                                }
                                            },
                                            "取消", null);
                                }
                            });
                        }
                    }
                })
                .show();
    }

    private boolean listening = false;

    @OnClick({R.id.button_shadowing, R.id.imageView_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_back:
                onBackPressed();
                break;

            case R.id.button_shadowing:
                if (mDetailedSeason.isAllowShadowing()) {
                    Intent intent = new Intent(this, ShadowingActivity.class);

                    intent.putExtra("videoUrl", videoList.get(currentVideoIndex).getVideo());
                    intent.putExtra("videoId", videoList.get(currentVideoIndex).getId());
                    intent.putExtra("videoImage", Config.ResourceUrl + videoList.get(currentVideoIndex).getHeadImg());

                    startActivityForResult(intent, 99);
                    overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
                } else {
                    UtilBox.purchaseAlert(this, "您还未购买跟读功能", videoList.get(0).getIdCartoon());
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 99:
                if (resultCode == RESULT_OK) {
                    UtilBox.showLoading(this, false);

                    new GetCartoonInfoPresenterImpl(this).getVideoList(videoId);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }

        UtilBox.returnActivity(this);
    }

    @Override
    public void onResume() {
        if (TextUtils.isEmpty(Config.ThirdSession)) {
            finish();
        }

        super.onResume();
        if (handler != null) {
            handler.sendEmptyMessage(0);
        }
        if (popupHandler != null) {
            popupHandler.sendEmptyMessage(0);
        }
    }

    @Override
    protected void onStop() {
        if (fullScreenHandler != null) {
            fullScreenHandler.removeMessages(1);
        }

        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
        if (handler != null) {
            handler.removeMessages(0);
        }
        if (popupHandler != null) {
            popupHandler.removeMessages(0);
        }
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler.removeMessages(0);
        }
        if (popupHandler != null) {
            popupHandler.removeMessages(0);
        }
        if (fullScreenHandler != null) {
            fullScreenHandler.removeMessages(1);
        }
        super.onDestroy();

        toggleOpeningClosingImage(0, false);
        toggleOpeningClosingImage(1, false);
    }

    @Override
    public void onGetCartoonListResult(boolean result, String info, Object extras) {

    }

    @Override
    public void onGetCartoonSeasonListResult(boolean result, String info, Object extras) {

    }
}
