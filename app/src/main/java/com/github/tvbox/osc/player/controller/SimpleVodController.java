package com.github.tvbox.osc.player.controller;

import static xyz.doikki.videoplayer.util.PlayerUtils.stringForTime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.subtitle.widget.SimpleSubtitleView;
import com.github.tvbox.osc.ui.adapter.ParseAdapter;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.PlayerHelper;
import com.github.tvbox.osc.util.ScreenUtils;
import com.github.tvbox.osc.util.SubtitleHelper;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

public class SimpleVodController extends BaseController {

    public SimpleVodController(@NonNull @NotNull Context context) {
        super(context);
        mHandlerCallback = new HandlerCallback() {
            @Override
            public void callback(Message msg) {
                switch (msg.what) {
                    case 1000: { // seek 刷新
                        // CC:TODO 隐藏暂停 icon

                        mProgressRoot.setVisibility(VISIBLE);
                        break;
                    }
                    case 1001: { // seek 关闭
                        mProgressRoot.setVisibility(GONE);
                        break;
                    }
                    case 1002: { // 显示底部菜单
                        mBottomRoot2.setVisibility(VISIBLE);
                        mTopRoot1.setVisibility(VISIBLE);
                        //mBottomRoot2.setVisibility(GONE);

                        // mBottomRoot.setVisibility(VISIBLE);
                        // mTopRoot1.setVisibility(VISIBLE);
                        // mTopRoot2.setVisibility(VISIBLE);

//                        mPlayTitle.setVisibility(VISIBLE);
                        //mNextBtn.requestFocus();
                        //backBtn.setVisibility(ScreenUtils.isTv(context) ? INVISIBLE : VISIBLE);
//                        showLockView();
                        break;
                    }
                    case 1003: { // 隐藏底部菜单
                        mBottomRoot.setVisibility(GONE);
                        // mTopRoot1.setVisibility(GONE);
                        // mTopRoot2.setVisibility(GONE);
                        backBtn.setVisibility(INVISIBLE);

                        mBottomRoot2.setVisibility(VISIBLE);
                        break;
                    }
                    case 1004: { // 设置速度 
                        if (isInPlaybackState()) {
                            try {
                                float speed = (float) mPlayerConfig.getDouble("sp");
                                mControlWrapper.setSpeed(speed);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else
                            mHandler.sendEmptyMessageDelayed(1004, 100);
                        break;
                    }
                }
            }
        };
    }

//    SeekBar mSeekBar;
    SeekBar mSeekBar2;
//    TextView mCurrentTime;
    TextView mCurrentTime2;
//    TextView mTotalTime;
    TextView mTotalTime2;
    boolean mIsDragging;
    private LinearLayout mProgressRoot;

    private FrameLayout mPauseICON;
    TextView mProgressText;
    ImageView mProgressIcon;
    ImageView mLockView;
    LinearLayout mBottomRoot;
    LinearLayout mBottomRoot2;
    LinearLayout mTopRoot1;
    LinearLayout mTopRoot2;
    LinearLayout mParseRoot;
    TvRecyclerView mGridView;
//    TextView mPlayTitle;
    TextView mPlayTitle1;
    TextView mPlayLoadNetSpeedRightTop;
    TextView mNextBtn;
    TextView mPreBtn;
    TextView mPlayerScaleBtn;
    public TextView mPlayerSpeedBtn;
    TextView mPlayerBtn;
    TextView mPlayerIJKBtn;
    TextView mPlayerRetry;
    TextView mPlayrefresh;
    public TextView mPlayerTimeStartEndText;
    public TextView mPlayerTimeStartBtn;
    public TextView mPlayerTimeSkipBtn;
    public TextView mPlayerTimeResetBtn;
    TextView timeClock;
    TextView mPlayLoadNetSpeed;
    TextView mVideoSize;
    public SimpleSubtitleView mSubtitleView;
    TextView mZimuBtn;
    TextView mAudioTrackBtn;
    public TextView mLandscapePortraitBtn;
    private View backBtn;//返回键
    private boolean isClickBackBtn;
   
    LockRunnable lockRunnable = new LockRunnable();
    private boolean isLock = false;
    Handler myHandle;
    Runnable myRunnable;
    int myHandleSeconds = 10000;//闲置多少毫秒秒关闭底栏  默认6秒

    int videoPlayState = 0;
    private TextView video_name_large;


    private Runnable clockTick = new Runnable() {
        private int tick=0;

        @Override
        public void run() {
            tick++;

            SimpleDateFormat timeFormat;
            Date date = new Date();
            if (tick%2==0) {
                timeFormat = new SimpleDateFormat("HH:mm:ss");
            } else {
                timeFormat = new SimpleDateFormat("HH mm ss");

                String width = Integer.toString(mControlWrapper.getVideoSize()[0]);
                String height = Integer.toString(mControlWrapper.getVideoSize()[1]);
                mVideoSize.setText("[ " + width + " X " + height +" ]");

                String speed = PlayerHelper.getDisplaySpeed(mControlWrapper.getTcpSpeed());
                mPlayLoadNetSpeedRightTop.setText(speed);
            }

            timeClock.setText(timeFormat.format(date));

            mHandler.postDelayed(this, 1000);
        }
    };


    @Override
    protected void initView() {
        super.initView();
//        mCurrentTime = findViewById(R.id.curr_time);
        //XXTODO
        mCurrentTime2 = findViewById(R.id.curr_time2);
        mTotalTime2 = findViewById(R.id.total_time2);
        mSeekBar2 = findViewById(R.id.seekBar2);
        mBottomRoot2 = findViewById(R.id.bottom_container2);

//        mTotalTime = findViewById(R.id.total_time);
//        mPlayTitle = findViewById(R.id.tv_info_name);
        mPlayTitle1 = findViewById(R.id.tv_info_name1);
        video_name_large=findViewById(R.id.video_name_large);

        mPlayLoadNetSpeedRightTop = findViewById(R.id.tv_play_load_net_speed_right_top);
//        mSeekBar = findViewById(R.id.seekBar);

        mProgressRoot = findViewById(R.id.tv_progress_container);
        mPauseICON=findViewById(R.id.tv_pause_container);
        mProgressIcon = findViewById(R.id.tv_progress_icon);
        mProgressText = findViewById(R.id.tv_progress_text);

        mBottomRoot = findViewById(R.id.bottom_container);
        mTopRoot1 = findViewById(R.id.tv_top_l_container);
        mTopRoot2 = findViewById(R.id.tv_top_r_container);
        mParseRoot = findViewById(R.id.parse_root);
        mGridView = findViewById(R.id.mGridView);
        mPlayerRetry = findViewById(R.id.play_retry);
        mPlayrefresh = findViewById(R.id.play_refresh);
        mNextBtn = findViewById(R.id.play_next);
        mPreBtn = findViewById(R.id.play_pre);
        mPlayerScaleBtn = findViewById(R.id.play_scale);
        mPlayerSpeedBtn = findViewById(R.id.play_speed);
        mPlayerBtn = findViewById(R.id.play_player);
        mPlayerIJKBtn = findViewById(R.id.play_ijk);
        mPlayerTimeStartEndText = findViewById(R.id.play_time_start_end_text);
        mPlayerTimeStartBtn = findViewById(R.id.play_time_start);
        mPlayerTimeSkipBtn = findViewById(R.id.play_time_end);
        mPlayerTimeResetBtn = findViewById(R.id.play_time_reset);
        timeClock = findViewById(R.id.tv_sys_time);
        mPlayLoadNetSpeed = findViewById(R.id.tv_play_load_net_speed);
        mVideoSize = findViewById(R.id.tv_videosize);
        mSubtitleView = findViewById(R.id.subtitle_view);
        mZimuBtn = findViewById(R.id.zimu_select);
        mAudioTrackBtn = findViewById(R.id.audio_track_select);
        mLandscapePortraitBtn = findViewById(R.id.landscape_portrait);
        backBtn = findViewById(R.id.tv_back);
        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof Activity) {
                    isClickBackBtn = true;
                    ((Activity) getContext()).onBackPressed();
                }
            }
        });
        mLockView = findViewById(R.id.tv_lock);
        mLockView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isLock = !isLock;
                mLockView.setImageResource(isLock ? R.drawable.icon_lock : R.drawable.icon_unlock);
                if (isLock) {
                    Message obtain = Message.obtain();
                    obtain.what = 1003;//隐藏底部菜单
                    mHandler.sendMessage(obtain);
                }
//                showLockView();
            }
        });
        View rootView = findViewById(R.id.rootView);
        rootView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isLock) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        showLockView();
                    }
                }
                return isLock;
            }
        });

        initSubtitleInfo();

        myHandle = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                hideBottom();
            }
        };

        timeClock.post(new Runnable() {
            @Override
            public void run() {
                mHandler.post(clockTick);
            }
        });

        mGridView.setLayoutManager(new V7LinearLayoutManager(getContext(), 0, false));
        ParseAdapter parseAdapter = new ParseAdapter();
        parseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ParseBean parseBean = parseAdapter.getItem(position);
                // 当前默认解析需要刷新
                int currentDefault = parseAdapter.getData().indexOf(ApiConfig.get().getDefaultParse());
                parseAdapter.notifyItemChanged(currentDefault);
                ApiConfig.get().setDefaultParse(parseBean);
                parseAdapter.notifyItemChanged(position);
                listener.changeParse(parseBean);
                hideBottom();
            }
        });
        mGridView.setAdapter(parseAdapter);
        parseAdapter.setNewData(ApiConfig.get().getParseBeanList());

        mParseRoot.setVisibility(VISIBLE);

        // seekBar2
        mSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                long duration = mControlWrapper.getDuration();
                long newPosition = (duration * progress) / seekBar.getMax();

                if (mCurrentTime2 != null)
                    mCurrentTime2.setText(stringForTime((int) newPosition));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsDragging = true;
                mControlWrapper.stopProgress();
                mControlWrapper.stopFadeOut();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myHandle.removeCallbacks(myRunnable);
                myHandle.postDelayed(myRunnable, myHandleSeconds);
                long duration = mControlWrapper.getDuration();
                long newPosition = (duration * seekBar.getProgress()) / seekBar.getMax();
                mControlWrapper.seekTo((int) newPosition);
                mIsDragging = false;
                mControlWrapper.startProgress();
                mControlWrapper.startFadeOut();
            }
        });

        mPlayerRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //listener.replay(true);
                hideBottom();
            }
        });
        mPlayrefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //listener.replay(false);
                hideBottom();
            }
        });
        mNextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //listener.playNext(false);
                hideBottom();
            }
        });
        mPreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // listener.playPre();
                hideBottom();
            }
        });
         mPlayerScaleBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 myHandle.removeCallbacks(myRunnable);
                 myHandle.postDelayed(myRunnable, myHandleSeconds);
                 try {
                     int scaleType = mPlayerConfig.getInt("sc");
                     scaleType++;
                     if (scaleType > 5)
                         scaleType = 0;
                     mPlayerConfig.put("sc", scaleType);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                     mControlWrapper.setScreenScaleType(scaleType);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         });
         mPlayerSpeedBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 myHandle.removeCallbacks(myRunnable);
                 myHandle.postDelayed(myRunnable, myHandleSeconds);
                 try {
                     float speed = (float) mPlayerConfig.getDouble("sp");
                     speed += 0.25f;
                     if (speed > 3)
                         speed = 0.5f;
                     mPlayerConfig.put("sp", speed);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                     speed_old = speed;
                     mControlWrapper.setSpeed(speed);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         });

         mPlayerSpeedBtn.setOnLongClickListener(new OnLongClickListener() {
             @Override
             public boolean onLongClick(View view) {
                 try {
                     mPlayerConfig.put("sp", 1.0f);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                     speed_old = 1.0f;
                     mControlWrapper.setSpeed(1.0f);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 return true;
             }
         });

         mPlayerBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 myHandle.removeCallbacks(myRunnable);
                 myHandle.postDelayed(myRunnable, myHandleSeconds);
                 try {
                     int playerType = mPlayerConfig.getInt("pl");
                     ArrayList<Integer> exsitPlayerTypes = PlayerHelper.getExistPlayerTypes();
                     int playerTypeIdx = 0;
                     int playerTypeSize = exsitPlayerTypes.size();
                     for(int i = 0; i<playerTypeSize; i++) {
                         if (playerType == exsitPlayerTypes.get(i)) {
                             if (i == playerTypeSize - 1) {
                                 playerTypeIdx = 0;
                             } else {
                                 playerTypeIdx = i + 1;
                             }
                         }
                     }
                     playerType = exsitPlayerTypes.get(playerTypeIdx);
                     mPlayerConfig.put("pl", playerType);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                     listener.replay(false);
                     hideBottom();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 mPlayerBtn.requestFocus();
                 mPlayerBtn.requestFocusFromTouch();
             }
         });

         mPlayerBtn.setOnLongClickListener(new OnLongClickListener() {
             @Override
             public boolean onLongClick(View view) {
                 myHandle.removeCallbacks(myRunnable);
                 myHandle.postDelayed(myRunnable, myHandleSeconds);
                 FastClickCheckUtil.check(view);
                 try {
                     int playerType = mPlayerConfig.getInt("pl");
                     int defaultPos = 0;
                     ArrayList<Integer> players = PlayerHelper.getExistPlayerTypes();
                     ArrayList<Integer> renders = new ArrayList<>();
                     for(int p = 0; p<players.size(); p++) {
                         renders.add(p);
                         if (players.get(p) == playerType) {
                             defaultPos = p;
                         }
                     }
                     SelectDialog<Integer> dialog = new SelectDialog<>(mActivity);
                     dialog.setTip("请选择播放器");
                     dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                         @Override
                         public void click(Integer value, int pos) {
                             try {
                                 dialog.cancel();
                                 int thisPlayType = players.get(pos);
                                 if (thisPlayType != playerType) {
                                     mPlayerConfig.put("pl", thisPlayType);
                                     updatePlayerCfgView();
                                     listener.updatePlayerCfg();
                                     listener.replay(false);
                                     hideBottom();
                                 }
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                             mPlayerBtn.requestFocus();
                             mPlayerBtn.requestFocusFromTouch();
                         }

                         @Override
                         public String getDisplay(Integer val) {
                             Integer playerType = players.get(val);
                             return PlayerHelper.getPlayerName(playerType);
                         }
                     }, new DiffUtil.ItemCallback<Integer>() {
                         @Override
                         public boolean areItemsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                             return oldItem.intValue() == newItem.intValue();
                         }

                         @Override
                         public boolean areContentsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                             return oldItem.intValue() == newItem.intValue();
                         }
                     }, renders, defaultPos);
                     dialog.show();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 return true;
             }
         });
         mPlayerIJKBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 myHandle.removeCallbacks(myRunnable);
                 myHandle.postDelayed(myRunnable, myHandleSeconds);
                 try {
                     String ijk = mPlayerConfig.getString("ijk");
                     List<IJKCode> codecs = ApiConfig.get().getIjkCodes();
                     for (int i = 0; i < codecs.size(); i++) {
                         if (ijk.equals(codecs.get(i).getName())) {
                             if (i >= codecs.size() - 1)
                                 ijk = codecs.get(0).getName();
                             else {
                                 ijk = codecs.get(i + 1).getName();
                             }
                             break;
                         }
                     }
                     mPlayerConfig.put("ijk", ijk);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                     listener.replay(false);
                     hideBottom();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 mPlayerIJKBtn.requestFocus();
                 mPlayerIJKBtn.requestFocusFromTouch();
             }
         });
 //        增加播放页面片头片尾时间重置
         mPlayerTimeResetBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 myHandle.removeCallbacks(myRunnable);
                 myHandle.postDelayed(myRunnable, myHandleSeconds);
                 try {
                     mPlayerConfig.put("et", 0);
                     mPlayerConfig.put("st", 0);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         });
         mPlayerTimeStartBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 myHandle.removeCallbacks(myRunnable);
                 myHandle.postDelayed(myRunnable, myHandleSeconds);
                 try {
                     int current = (int) mControlWrapper.getCurrentPosition();
                     int duration = (int) mControlWrapper.getDuration();
                     if (current > duration / 2) return;
                     mPlayerConfig.put("st",current/1000);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         });
         mPlayerTimeStartBtn.setOnLongClickListener(new OnLongClickListener() {
             @Override
             public boolean onLongClick(View view) {
                 try {
                     mPlayerConfig.put("st", 0);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 return true;
             }
         });
         mPlayerTimeSkipBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 myHandle.removeCallbacks(myRunnable);
                 myHandle.postDelayed(myRunnable, myHandleSeconds);
                 try {
                     int current = (int) mControlWrapper.getCurrentPosition();
                     int duration = (int) mControlWrapper.getDuration();
                     if (current < duration / 2) return;
                     mPlayerConfig.put("et", (duration - current)/1000);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         });
         mPlayerTimeSkipBtn.setOnLongClickListener(new OnLongClickListener() {
             @Override
             public boolean onLongClick(View view) {
                 try {
                     mPlayerConfig.put("et", 0);
                     updatePlayerCfgView();
                     listener.updatePlayerCfg();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 return true;
             }
         });
         mZimuBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 FastClickCheckUtil.check(view);
                 listener.selectSubtitle();
                 hideBottom();
             }
         });
         mZimuBtn.setOnLongClickListener(new OnLongClickListener() {
             @Override
             public boolean onLongClick(View view) {
                 mSubtitleView.setVisibility(View.GONE);
                 mSubtitleView.destroy();
                 mSubtitleView.clearSubtitleCache();
                 mSubtitleView.isInternal = false;
                 hideBottom();
                 Toast.makeText(getContext(), "字幕已关闭", Toast.LENGTH_SHORT).show();
                 return true;
             }
         });
         mAudioTrackBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 FastClickCheckUtil.check(view);
                 listener.selectAudioTrack();
                 hideBottom();
             }
         });
        mLandscapePortraitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                setLandscapePortrait();
                hideBottom();
            }
        });
        mNextBtn.setNextFocusLeftId(R.id.play_time_start);
    }

    private void hideLiveAboutBtn() {
        if (mControlWrapper != null && mControlWrapper.getDuration() == 0) {
            mPlayerSpeedBtn.setVisibility(GONE);
            mPlayerTimeStartEndText.setVisibility(GONE);
            mPlayerTimeStartBtn.setVisibility(GONE);
            mPlayerTimeSkipBtn.setVisibility(GONE);
            mPlayerTimeResetBtn.setVisibility(GONE);
            mNextBtn.setNextFocusLeftId(R.id.zimu_select);
        } else {
            mPlayerSpeedBtn.setVisibility(View.VISIBLE);
            mPlayerTimeStartEndText.setVisibility(View.VISIBLE);
            mPlayerTimeStartBtn.setVisibility(View.VISIBLE);
            mPlayerTimeSkipBtn.setVisibility(View.VISIBLE);
            mPlayerTimeResetBtn.setVisibility(View.VISIBLE);
            mNextBtn.setNextFocusLeftId(R.id.play_time_start);
        }
    }

    public void initLandscapePortraitBtnInfo() {
        if(mControlWrapper!=null && mActivity!=null){
            int width = mControlWrapper.getVideoSize()[0];
            int height = mControlWrapper.getVideoSize()[1];
            double screenSqrt = ScreenUtils.getSqrt(mActivity);
            if (screenSqrt < 10.0 && width < height) {
                mLandscapePortraitBtn.setVisibility(View.VISIBLE);
                mLandscapePortraitBtn.setText("竖屏");
            }
        }
    }

    void setLandscapePortrait() {
        int requestedOrientation = mActivity.getRequestedOrientation();
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            mLandscapePortraitBtn.setText("横屏");
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            mLandscapePortraitBtn.setText("竖屏");
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    void initSubtitleInfo() {
        int subtitleTextSize = SubtitleHelper.getTextSize(mActivity);
        mSubtitleView.setTextSize(subtitleTextSize);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.player_vod_control_view;
    }

    public void showParse(boolean userJxList) {
        mParseRoot.setVisibility(userJxList ? VISIBLE : GONE);
    }

    private JSONObject mPlayerConfig = null;

    public void setPlayerConfig(JSONObject playerCfg) {
        this.mPlayerConfig = playerCfg;
        updatePlayerCfgView();
    }

    void updatePlayerCfgView() {
        try {
            int playerType = mPlayerConfig.getInt("pl");
            mPlayerBtn.setText(PlayerHelper.getPlayerName(playerType));
            mPlayerScaleBtn.setText(PlayerHelper.getScaleName(mPlayerConfig.getInt("sc")));
            mPlayerIJKBtn.setText(mPlayerConfig.getString("ijk"));
            mPlayerIJKBtn.setVisibility(playerType == 1 ? VISIBLE : GONE);
            mPlayerScaleBtn.setText(PlayerHelper.getScaleName(mPlayerConfig.getInt("sc")));
            mPlayerSpeedBtn.setText("x" + mPlayerConfig.getDouble("sp"));
            mPlayerTimeStartBtn.setText(PlayerUtils.stringForTime(mPlayerConfig.getInt("st") * 1000));
            mPlayerTimeSkipBtn.setText(PlayerUtils.stringForTime(mPlayerConfig.getInt("et") * 1000));
            mAudioTrackBtn.setVisibility((playerType == 1) ? VISIBLE : GONE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTitle(String playTitleInfo) {
//        mPlayTitle.setText(playTitleInfo);
        mPlayTitle1.setText(playTitleInfo);
        video_name_large.setText(playTitleInfo);
    }

    public void resetSpeed() {
        skipEnd = true;
        mHandler.removeMessages(1004);
        mHandler.sendEmptyMessageDelayed(1004, 100);
    }

    public interface VodControlListener {
        void playNext(boolean rmProgress);

        void playPre();

        void prepared();

        void changeParse(ParseBean pb);

        void updatePlayerCfg();

        void replay(boolean replay);

        void errReplay();

        void selectSubtitle();

        void selectAudioTrack();
    }

    // TODO 错误处理. 切换源. retry 等操作.
    public void setListener(VodControlListener listener) {
        this.listener = listener;
    }

    private VodControlListener listener;

    private boolean skipEnd = true;

    @Override
    protected void setProgress(int duration, int position) {

        if (mIsDragging) {
            return;
        }
        super.setProgress(duration, position);
        if (skipEnd && position != 0 && duration != 0) {
            int et = 0;
            try {
                et = mPlayerConfig.getInt("et");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (et > 0 && position + (et * 1000) >= duration) {
                skipEnd = false;
                listener.playNext(true);
            }
        }
//        mCurrentTime.setText(PlayerUtils.stringForTime(position));
        mCurrentTime2.setText(PlayerUtils.stringForTime(position));
//        mTotalTime.setText(PlayerUtils.stringForTime(duration));
        mTotalTime2.setText(PlayerUtils.stringForTime(duration));
        if (duration > 0) {
//            mSeekBar.setEnabled(true);
            mSeekBar2.setEnabled(true);
            int pos = (int) (position * 1.0 / duration * mSeekBar2.getMax());
//            mSeekBar.setProgress(pos);
            mSeekBar2.setProgress(pos);
        } else {
//            mSeekBar.setEnabled(false);
            mSeekBar2.setEnabled(false);
        }
        int percent = mControlWrapper.getBufferedPercentage();
        if (percent >= 95) {
//            mSeekBar.setSecondaryProgress(mSeekBar.getMax());
            mSeekBar2.setSecondaryProgress(mSeekBar2.getMax());
        } else {
//            mSeekBar.setSecondaryProgress(percent * 10);
            mSeekBar2.setSecondaryProgress(percent * 10);
        }
    }

    private boolean simSlideStart = false;
    private int simSeekPosition = 0;
    private long simSlideOffset = 0;

    public void tvSlideStop() {
        if (!simSlideStart)
            return;
        mControlWrapper.seekTo(simSeekPosition);
        if (!mControlWrapper.isPlaying())
            mControlWrapper.start();
        simSlideStart = false;
        simSeekPosition = 0;
        simSlideOffset = 0;
    }

    public void tvSlideStart(int dir) {
        int duration = (int) mControlWrapper.getDuration();
        if (duration <= 0)
            return;
        if (!simSlideStart) {
            simSlideStart = true;
        }
        // 每次10秒
        simSlideOffset += (10000.0f * dir);
        int currentPosition = (int) mControlWrapper.getCurrentPosition();
        int position = (int) (simSlideOffset + currentPosition);
        if (position > duration-5) position = duration-5;
        if (position < 0) position = 0;
        updateSeekUI(currentPosition, position, duration);

        simSeekPosition = position;
    }

    @Override
    protected void updateSeekUI(int curr, int seekTo, int duration) {
        super.updateSeekUI(curr, seekTo, duration);
        if (seekTo > curr) {
            mProgressIcon.setImageResource(R.drawable.icon_pre);
        } else {
            mProgressIcon.setImageResource(R.drawable.icon_back);
        }
        //XXTODO
        mProgressText.setText(PlayerUtils.stringForTime(seekTo) + " / " + PlayerUtils.stringForTime(duration));
        mHandler.sendEmptyMessage(1000);
        mHandler.removeMessages(1001);
        mHandler.sendEmptyMessageDelayed(1001, 1000);

        int pos = (int) (seekTo * 1.0 / duration * mSeekBar2.getMax());
        mSeekBar2.setProgress(pos);

    }



    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        videoPlayState = playState;
        switch (playState) {
            case VideoView.STATE_IDLE:
                break;
            case VideoView.STATE_PLAYING:
                initLandscapePortraitBtnInfo();
                startProgress();
                mVideoSize.setVisibility(GONE);
                _on_pause(false);
                video_name_large.setVisibility(GONE);

                break;
            case VideoView.STATE_PAUSED:
                mVideoSize.setVisibility(VISIBLE);
                _on_pause(true);
                video_name_large.setVisibility(VISIBLE);

                break;
            case VideoView.STATE_ERROR:
                listener.errReplay();
                break;
            case VideoView.STATE_PREPARED:
                mPlayLoadNetSpeed.setVisibility(GONE);
                hideLiveAboutBtn();
                listener.prepared();
                break;
            case VideoView.STATE_BUFFERED:
                mPlayLoadNetSpeed.setVisibility(GONE);
                break;
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_BUFFERING:
                // 进度条
                mBottomRoot2.setVisibility(VISIBLE);
                // 时钟 下载速度
                mTopRoot2.setVisibility(VISIBLE);
                // 标题
                mTopRoot1.setVisibility(VISIBLE);
//                mPlayTitle.setVisibility(VISIBLE);
                if(mProgressRoot.getVisibility()==GONE)mPlayLoadNetSpeed.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                // playNext(reset=true)
                listener.playNext(true);
                break;
        }
    }

    boolean isBottomVisible() {
        return mBottomRoot.getVisibility() == VISIBLE;
    }

    void showBottom() {
        mHandler.removeMessages(1003);
        mHandler.sendEmptyMessage(1002);
    }

    void hideBottom() {
        mHandler.removeMessages(1002);
        mHandler.sendEmptyMessage(1003);
    }

    private Runnable hideProgressRoot=new Runnable(){
        @Override
        public void run() {
            mProgressRoot.setVisibility(GONE);
        }
    };
    private void _on_seek(){
//        mProgressRoot = findViewById(R.id.tv_progress_container);
//        mPauseICON=findViewById(R.id.tv_pause_container);
//        mProgressIcon = findViewById(R.id.tv_progress_icon);
//        mProgressText = findViewById(R.id.tv_progress_text);

        myHandle.removeCallbacks(hideProgressRoot);
        mPauseICON.setVisibility(GONE);
        mProgressIcon.setVisibility(VISIBLE);

        mProgressRoot.setVisibility(VISIBLE);
        if(!isSeeking) {
            myHandle.postDelayed(hideProgressRoot, 2000);
        }
    }

    private void _on_pause(boolean isPause){
        if(isSeeking){
            return;
        }

        myHandle.removeCallbacks(hideProgressRoot);
        if(!isPause) {
            myHandle.post(hideProgressRoot);
            return;
        }

        mProgressIcon.setVisibility(GONE);
        mPauseICON.setVisibility(VISIBLE);
        mProgressRoot.setVisibility(VISIBLE);
    }

    private boolean hasMenuClicked=false;
//    private long mLastMenuClicked=0;
    private Runnable nextAudioTrack=()->{
        if(hasMenuClicked){
            listener.selectAudioTrack();
        }
    };

    private boolean isSeeking=false;

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        myHandle.removeCallbacks(myRunnable);
        if (super.onKeyEvent(event)) {
            return true;
        }
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (isBottomVisible()) {
            mHandler.removeMessages(1002);
            mHandler.removeMessages(1003);
            myHandle.postDelayed(myRunnable, myHandleSeconds);
            return super.dispatchKeyEvent(event);
        }
        boolean isInPlayback = isInPlaybackState();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (isInPlayback) {
                    isSeeking=true;
                    _on_seek();
                    mControlWrapper.pause();//暂停播放
                    tvSlideStart(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1);
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (isInPlayback) {
                    togglePlay();
                    return true;
                }
//            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {  return true;// 闲置开启计时关闭透明底栏
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (!isBottomVisible()) {
                    showBottom();
                    myHandle.postDelayed(myRunnable, myHandleSeconds);
                    return true;
                }
            } else if(keyCode == KeyEvent.KEYCODE_MENU){
                mHandler.removeCallbacks(nextAudioTrack);
                // TODO 长按 2秒 切换 音轨.
                hasMenuClicked=true;
                mHandler.postDelayed(nextAudioTrack,2000);
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (isInPlayback) {
                    tvSlideStop();
                    // mControlWrapper.start();//开始播放
                    _on_seek();
                    isSeeking=false;
                    return true;
                }
            }
            else if (keyCode == KeyEvent.KEYCODE_MENU){
                hasMenuClicked=false;
                mHandler.removeCallbacks(nextAudioTrack);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    private boolean fromLongPress;
    private float speed_old = 1.0f;
    @Override
    public void onLongPress(MotionEvent e) {
        if (videoPlayState!=VideoView.STATE_PAUSED) {
            fromLongPress = true;
            try {
                speed_old = (float) mPlayerConfig.getDouble("sp");
                float speed = 3.0f;
                mPlayerConfig.put("sp", speed);
                updatePlayerCfgView();
                listener.updatePlayerCfg();
                mControlWrapper.setSpeed(speed);
            } catch (JSONException f) {
                f.printStackTrace();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            if (fromLongPress) {
                fromLongPress =false;
                try {
                    float speed = speed_old;
                    mPlayerConfig.put("sp", speed);
                    updatePlayerCfgView();
                    listener.updatePlayerCfg();
                    mControlWrapper.setSpeed(speed);
                } catch (JSONException f) {
                    f.printStackTrace();
                }
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        myHandle.removeCallbacks(myRunnable);
        if (!isBottomVisible()) {
            showBottom();
            // 闲置计时关闭
            myHandle.postDelayed(myRunnable, myHandleSeconds);
        } else {
            hideBottom();
        }
        return true;
    }
    
    private class LockRunnable implements Runnable {
        @Override
        public void run() {
            mLockView.setVisibility(INVISIBLE);
        }
    }
    
    @Override
    public boolean onBackPressed() {
        if (isClickBackBtn) {
            isClickBackBtn = false;
            if (isBottomVisible()) {
                hideBottom();
            }
            return false;
        }
        if (super.onBackPressed()) {
            return true;
        }
        if (isBottomVisible()) {
            hideBottom();
            return true;
        }
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(clockTick);
    }
}