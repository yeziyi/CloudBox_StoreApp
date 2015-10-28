package com.zhidian.wifibox.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.CleanMasterAdapter;
import com.zhidian.wifibox.controller.CleanMasterController;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.data.CleanMasterDataBean;
import com.zhidian.wifibox.data.CleanMasterDataBean.APKBean;
import com.zhidian.wifibox.data.CleanMasterDataBean.CacheBean;
import com.zhidian.wifibox.data.CleanMasterDataBean.RAMBean;
import com.zhidian.wifibox.data.CleanMasterDataBean.TrashBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.view.RotationView;
import com.zhidian.wifibox.view.ScanView;

/**
 * 手机清理
 * 
 * @author xiedezhi
 * 
 */
public class CleanMasterActivity extends Activity {
	/**
	 * 停止扫描
	 */
	private volatile boolean stop = false;
	/**
	 * 缓存扫描完成
	 */
	public static final int MSG_CACHE = 11001;
	/**
	 * 安装包扫描完成
	 */
	public static final int MSG_APK = 12002;
	/**
	 * 内存扫描完成
	 */
	public static final int MSG_RAM = 13003;
	/**
	 * 残留扫描完成
	 */
	public static final int MSG_TRASH = 14004;

	private long mSelectCache = 0;
	private long mSelectAPK = 0;
	private long mSelectRAM = 0;
	private long mSelectTrash = 0;
	private long mTotalCache = 0;
	private long mTotalAPK = 0;
	private long mTotalRAM = 0;
	private long mTotalTrash = 0;
	private long mCleanCache = 0;
	private long mCleanAPK = 0;
	private long mCleanRAM = 0;
	private long mCleanTrash = 0;

	private ExpandableListView mListView;
	private CleanMasterAdapter mAdapter;
	private RelativeLayout mScanFrame;
	private ScanView mScanView;
	private TextView mScaning;
	private TextView mCacheSize;
	private TextView mRAMSize;
	private TextView mAPKSize;
	private TextView mTrashSize;
	private TextView mScanResult;
	private RotationView mTotalRotation;
	private RotationView mCacheRotation;
	private RotationView mAPKRotation;
	private RotationView mRAMRotation;
	private RotationView mTrashRotation;
	private CleanMasterDataBean mBean = new CleanMasterDataBean();
	private LinearLayout mScanInfo;
	private RelativeLayout mCancleFrame;
	private RelativeLayout mCleanFrame;
	private Button mCancleButton;
	private Button mCleanButton;
	private FrameLayout mCleaningFrame;
	private TextView mCleaning;
	private LinearLayout mCleanResultFrame;
	private LinearLayout mResultFrame1;
	private LinearLayout mResultFrame2;
	private LinearLayout mResultFrame3;
	private LinearLayout mResultFrame4;

	/**
	 * 列表选项更改后，更新一键清理的大小
	 */
	public static final int MSG_UPDATE_SELECT = 3601;

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_SELECT:
				long size = 0;
				for (CacheBean cache : mBean.cacheList) {
					if (cache.isSelect) {
						size += cache.cache;
					}
				}
				for (RAMBean ram : mBean.ramList) {
					if (ram.isSelect) {
						size += ram.ram;
					}
				}
				for (APKBean apk : mBean.apkList1) {
					if (apk.isSelect) {
						size += apk.size;
					}
				}
				for (APKBean apk : mBean.apkList2) {
					if (apk.isSelect) {
						size += apk.size;
					}
				}
				for (APKBean apk : mBean.apkList3) {
					if (apk.isSelect) {
						size += apk.size;
					}
				}
				for (TrashBean trash : mBean.trashList1) {
					if (trash.isSelect) {
						size += trash.size;
					}
				}
				for (TrashBean trash : mBean.trashList2) {
					if (trash.isSelect) {
						size += trash.size;
					}
				}
				for (TrashBean trash : mBean.trashList3) {
					if (trash.isSelect) {
						size += trash.size;
					}
				}
				mCleanButton.setText("一键清理 "
						+ Formatter.formatShortFileSize(
								getApplicationContext(), size));
				break;
			default:
				break;
			}
		};
	};
	/**
	 * 扫描结果回调
	 */
	private TAIResponseListener mListener = new TAIResponseListener() {

		@Override
		public void onSuccess(TAResponse response) {
			if (stop) {
				return;
			}
			stop = true;
			// 更新列表
			mScanFrame.removeView(mScanView);
			mScanView = null;
			mScaning.setText("扫描完成");
			mTotalRotation.stop();
			mScanInfo.setVisibility(View.GONE);
			findViewById(R.id.line).setVisibility(View.GONE);
			mCancleFrame.setVisibility(View.GONE);
			mCleanFrame.setVisibility(View.VISIBLE);
			mHandler.sendEmptyMessage(MSG_UPDATE_SELECT);
			mCleanButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 一键清理
					// TODO 先检查有没有选中的项
					mListView.setVisibility(View.GONE);
					findViewById(R.id.title_frame).setVisibility(View.GONE);
					mCleaningFrame.setVisibility(View.VISIBLE);
					mCleanResultFrame.setVisibility(View.VISIBLE);
					mCleanFrame.setVisibility(View.GONE);
					TextView text1 = (TextView) findViewById(R.id.total_size_1);
					text1.setText("成功清理");
					mScanResult.setText("0B");
					findViewById(R.id.total_size_3).setVisibility(View.GONE);
					// 开始清理
					TAApplication.getApplication().doCommand(
							TAApplication.getApplication().getString(
									R.string.cleanmastercontroller),
							new TARequest(CleanMasterController.CLEAN, mBean),
							new TAIResponseListener() {

								@Override
								public void onSuccess(TAResponse response) {
									mCleanFrame.setVisibility(View.VISIBLE);
									mCleanButton.setText("去看看有什么好玩的应用");
									mCleanButton
											.setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View v) {
													// 跳转到首页
													finish();
													TAApplication
															.sendHandler(
																	null,
																	IDiyFrameIds.NAVIGATIONBAR,
																	IDiyMsgIds.NAV_SWITCH_NAVIGATION,
																	-1,
																	TabController.NAVIGATIONFEATURE,
																	null);
													v.postDelayed(
															new Runnable() {

																@Override
																public void run() {
																	TAApplication
																			.sendHandler(
																					null,
																					IDiyFrameIds.ACTIONBAR,
																					IDiyMsgIds.JUMP_TITLE,
																					0,
																					null,
																					null);
																}
															}, 50);
												}
											});
									mCleaning.setVisibility(View.GONE);
									findViewById(R.id.clean_finish)
											.setVisibility(View.VISIBLE);
								}

								@Override
								public void onStart() {
								}

								@Override
								public void onRuning(TAResponse response) {
									Object obj = response.getData();
									if (obj == null) {
										return;
									}
									if (obj instanceof String) {
										mCleaning.setText("正在清理："
												+ obj.toString());
									} else if (obj instanceof Object[]) {
										Object[] array = (Object[]) obj;
										int type = (Integer) array[0];
										int count = (Integer) array[1];
										long size = (Long) array[2];
										if (type == MSG_CACHE) {
											TextView info1 = (TextView) mResultFrame1
													.findViewById(R.id.info1);
											TextView info2 = (TextView) mResultFrame1
													.findViewById(R.id.info2);
											info1.setText("(已清理" + count + "项共");
											info2.setText(Formatter
													.formatFileSize(
															getApplicationContext(),
															size));
											mCleanCache = size;
										} else if (type == MSG_APK) {
											TextView info1 = (TextView) mResultFrame2
													.findViewById(R.id.info1);
											TextView info2 = (TextView) mResultFrame2
													.findViewById(R.id.info2);
											info1.setText("(已清理" + count + "项共");
											info2.setText(Formatter
													.formatFileSize(
															getApplicationContext(),
															size));
											mCleanAPK = size;
										} else if (type == MSG_RAM) {
											TextView info1 = (TextView) mResultFrame3
													.findViewById(R.id.info1);
											TextView info2 = (TextView) mResultFrame3
													.findViewById(R.id.info2);
											info1.setText("(已清理" + count + "项共");
											info2.setText(Formatter
													.formatFileSize(
															getApplicationContext(),
															size));
											mCleanRAM = size;
										} else if (type == MSG_TRASH) {
											TextView info1 = (TextView) mResultFrame4
													.findViewById(R.id.info1);
											TextView info2 = (TextView) mResultFrame4
													.findViewById(R.id.info2);
											info1.setText("(已清理" + count + "项共");
											info2.setText(Formatter
													.formatFileSize(
															getApplicationContext(),
															size));
											mCleanTrash = size;
										}
										// 更新展示板
										mScanResult.setText(Formatter
												.formatFileSize(
														getApplicationContext(),
														mCleanCache + mCleanAPK
																+ mCleanRAM
																+ mCleanTrash));
									}
								}

								@Override
								public void onFinish() {
								}

								@Override
								public void onFailure(TAResponse response) {
								}
							}, true, false);
				}
			});
			mListView.setVisibility(View.VISIBLE);
			mListView.expandGroup(2);
			mAdapter.update(mBean);
			findViewById(R.id.title_frame).setVisibility(View.VISIBLE);
			TextView size = (TextView) findViewById(R.id.title_size);
			size.setText(Formatter.formatShortFileSize(
					TAApplication.getApplication(), mTotalAPK + mTotalCache
							+ mTotalRAM + mTotalTrash));
		}

		@Override
		public void onStart() {
		}

		@Override
		public void onRuning(TAResponse response) {
			if (stop) {
				return;
			}
			Object obj = response.getData();
			if (obj == null) {
				return;
			}
			if (obj instanceof Integer) {
				int msg = (Integer) obj;
				if (msg == MSG_CACHE) {
					mCacheRotation.stop();
					mCacheRotation.setBackgroundDrawable(null);
					mCacheRotation
							.setImageResource(R.drawable.scan_finish_icon);
					mCacheRotation.setPadding(0, 0, 0, 0);
				} else if (msg == MSG_RAM) {
					mRAMRotation.stop();
					mRAMRotation.setBackgroundDrawable(null);
					mRAMRotation.setImageResource(R.drawable.scan_finish_icon);
					mRAMRotation.setPadding(0, 0, 0, 0);
				} else if (msg == MSG_APK) {
					mAPKRotation.stop();
					mAPKRotation.setBackgroundDrawable(null);
					mAPKRotation.setImageResource(R.drawable.scan_finish_icon);
					mAPKRotation.setPadding(0, 0, 0, 0);
				} else if (msg == MSG_TRASH) {
					mTrashRotation.stop();
					mTrashRotation.setBackgroundDrawable(null);
					mTrashRotation
							.setImageResource(R.drawable.scan_finish_icon);
					mTrashRotation.setPadding(0, 0, 0, 0);
				}
			} else if (obj instanceof String) {
				mScaning.setText("正在扫描：" + obj.toString());
			} else if (obj instanceof List<?>) {
				List<Object> olist = (List<Object>) obj;
				if (olist.size() > 0) {
					Object item = olist.get(0);
					if (item instanceof CacheBean) {
						// 缓存
						List<CacheBean> clist = (List<CacheBean>) obj;
						mBean.cacheList = clist;
						long size = 0;
						long totalSize = 0;
						for (CacheBean cache : clist) {
							totalSize += cache.cache;
							if (cache.isSelect) {
								size += cache.cache;
							}
						}
						mTotalCache = totalSize;
						mSelectCache = size;
						updateScanResult();
						mCacheSize.setText("("
								+ Formatter.formatFileSize(
										TAApplication.getApplication(), size)
								+ ")");
					} else if (item instanceof RAMBean) {
						// 内存
						List<RAMBean> rlist = (List<RAMBean>) obj;
						mBean.ramList = rlist;
						long size = 0;
						long totalSize = 0;
						for (RAMBean ram : rlist) {
							totalSize += ram.ram;
							if (ram.isSelect) {
								size += ram.ram;
							}
						}
						mSelectRAM = size;
						mTotalRAM = totalSize;
						updateScanResult();
						mRAMSize.setText("("
								+ Formatter.formatFileSize(
										TAApplication.getApplication(), size)
								+ ")");
					}
				}
			} else if (obj instanceof Object[]) {
				Object[] oarray = (Object[]) obj;
				if (oarray.length == 2 && (oarray[0] instanceof Integer)
						&& (oarray[1] instanceof List<?>)) {
					int index = (Integer) oarray[0];
					List<Object> olist = (List<Object>) oarray[1];
					Object item = olist.get(0);
					if (item instanceof TrashBean) {
						// 残留
						List<TrashBean> tList = (List<TrashBean>) oarray[1];
						if (index == 1) {
							mBean.trashList1 = tList;
						} else if (index == 2) {
							mBean.trashList2 = tList;
						} else if (index == 3) {
							mBean.trashList3 = tList;
						}
						long size = 0;
						long totalSize = 0;
						for (TrashBean trash : mBean.trashList1) {
							totalSize += trash.size;
							if (trash.isSelect) {
								size += trash.size;
							}
						}
						for (TrashBean trash : mBean.trashList2) {
							totalSize += trash.size;
							if (trash.isSelect) {
								size += trash.size;
							}
						}
						for (TrashBean trash : mBean.trashList3) {
							totalSize += trash.size;
							if (trash.isSelect) {
								size += trash.size;
							}
						}
						mSelectTrash = size;
						mTotalTrash = totalSize;
						updateScanResult();
						mTrashSize.setText("("
								+ Formatter.formatFileSize(
										TAApplication.getApplication(), size)
								+ ")");
					} else if (item instanceof APKBean) {
						// APK
						List<APKBean> aList = (List<APKBean>) oarray[1];
						if (index == 1) {
							mBean.apkList1 = aList;
						} else if (index == 2) {
							mBean.apkList2 = aList;
						} else if (index == 3) {
							mBean.apkList3 = aList;
						}
						long size = 0;
						long totalSize = 0;
						for (APKBean apk : mBean.apkList1) {
							totalSize += apk.size;
							if (apk.isSelect) {
								size += apk.size;
							}
						}
						for (APKBean apk : mBean.apkList2) {
							totalSize += apk.size;
							if (apk.isSelect) {
								size += apk.size;
							}
						}
						for (APKBean apk : mBean.apkList3) {
							totalSize += apk.size;
							if (apk.isSelect) {
								size += apk.size;
							}
						}
						mSelectAPK = size;
						mTotalAPK = totalSize;
						updateScanResult();
						mAPKSize.setText("("
								+ Formatter.formatFileSize(
										TAApplication.getApplication(), size)
								+ ")");
					}
				}
			}
		}

		@Override
		public void onFinish() {
		}

		@Override
		public void onFailure(TAResponse response) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cleanmaster);

		findViewById(R.id.back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mListView = (ExpandableListView) findViewById(R.id.listview);
		mListView.setGroupIndicator(null);
		mAdapter = new CleanMasterAdapter(mHandler);
		mListView.setAdapter(mAdapter);
		mScanFrame = (RelativeLayout) findViewById(R.id.scanframe);
		mScanInfo = (LinearLayout) findViewById(R.id.scaninfo);
		mCancleFrame = (RelativeLayout) findViewById(R.id.cancle_frame);
		mCleanFrame = (RelativeLayout) findViewById(R.id.clean_frame);
		mCancleButton = (Button) findViewById(R.id.cancle);
		mCancleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 停止扫描
				mListener.onSuccess(null);
			}
		});
		mCleanButton = (Button) findViewById(R.id.clean);
		mScaning = (TextView) findViewById(R.id.title_total);
		mCacheSize = (TextView) findViewById(R.id.size_cache);
		mAPKSize = (TextView) findViewById(R.id.size_apk);
		mRAMSize = (TextView) findViewById(R.id.size_ram);
		mTrashSize = (TextView) findViewById(R.id.size_trash);
		mScanResult = (TextView) findViewById(R.id.total_size);
		mTotalRotation = (RotationView) findViewById(R.id.progress_total);
		mTotalRotation.rotate();
		mCacheRotation = (RotationView) findViewById(R.id.progress_cache);
		mCacheRotation.rotate();
		mAPKRotation = (RotationView) findViewById(R.id.progress_apk);
		mAPKRotation.rotate();
		mRAMRotation = (RotationView) findViewById(R.id.progress_ram);
		mRAMRotation.rotate();
		mTrashRotation = (RotationView) findViewById(R.id.progress_trash);
		mTrashRotation.rotate();
		mScanView = new ScanView(this);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mScanFrame.addView(mScanView, lp);
		mCleaningFrame = (FrameLayout) findViewById(R.id.cleaning_frame);
		mCleaning = (TextView) mCleaningFrame.findViewById(R.id.cleaning);
		mCleanResultFrame = (LinearLayout) findViewById(R.id.clean_result_frame);
		mResultFrame1 = (LinearLayout) findViewById(R.id.result_1);
		mResultFrame2 = (LinearLayout) findViewById(R.id.result_2);
		mResultFrame3 = (LinearLayout) findViewById(R.id.result_3);
		mResultFrame4 = (LinearLayout) findViewById(R.id.result_4);
		// 开始扫描
		TAApplication.getApplication().doCommand(
				TAApplication.getApplication().getString(
						R.string.cleanmastercontroller),
				new TARequest(CleanMasterController.SCAN, null), mListener,
				true, false);
		// 设置标签
		Setting setting = new Setting(this);
		int count = setting.getInt(Setting.CLEANMASTER_OPEN_COUNT);
		count = count + 1;
		setting.putInt(Setting.CLEANMASTER_OPEN_COUNT, count);
		if (count >= 3) {
			XGPushManager
					.deleteTag(TAApplication.getApplication(), "手机清理用户x10");
			XGPushManager.setTag(TAApplication.getApplication(), "手机清理用户x3");
		} else if (count >= 10) {
			XGPushManager.deleteTag(TAApplication.getApplication(), "手机清理用户x3");
			XGPushManager.setTag(TAApplication.getApplication(), "手机清理用户x10");
		}
	}

	/**
	 * 更新扫描结果
	 */
	private void updateScanResult() {
		mScanResult.setText(Formatter.formatShortFileSize(
				TAApplication.getApplication(), mSelectAPK + mSelectCache
						+ mSelectRAM + mSelectTrash));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 必须要调用这句
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 页面统计
		StatService.trackBeginPage(this, "手机清理");
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		if (click != null) {
			// TODO
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, "手机清理");
		XGPushManager.onActivityStoped(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
