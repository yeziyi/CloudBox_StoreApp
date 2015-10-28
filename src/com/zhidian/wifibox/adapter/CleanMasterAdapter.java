package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import android.graphics.Bitmap;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.CleanMasterActivity;
import com.zhidian.wifibox.data.CleanMasterDataBean;
import com.zhidian.wifibox.data.CleanMasterDataBean.APKBean;
import com.zhidian.wifibox.data.CleanMasterDataBean.CacheBean;
import com.zhidian.wifibox.data.CleanMasterDataBean.RAMBean;
import com.zhidian.wifibox.data.CleanMasterDataBean.TrashBean;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.Setting;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 手机清理适配器
 * 
 * @author xiedezhi
 * 
 */
public class CleanMasterAdapter extends BaseExpandableListAdapter {
	private List<CacheBean> mCList = new ArrayList<CacheBean>();
	private List<APKBean> mAList = new ArrayList<APKBean>();
	private List<RAMBean> mRList = new ArrayList<RAMBean>();
	private List<TrashCombination> mTList = new ArrayList<TrashCombination>();

	private Handler mHandler = null;

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());
	/**
	 * 全选点击
	 */
	private OnClickListener mTitleSelectClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int groupPosition = (Integer) v.getTag();
			boolean bs = false;
			if (groupPosition == 0) {
				for (CacheBean cache : mCList) {
					if (cache.isSelect) {
						bs = true;
						break;
					}
				}
				if (bs) {
					for (CacheBean cache : mCList) {
						cache.isSelect = false;
					}
				} else {
					for (CacheBean cache : mCList) {
						cache.isSelect = true;
					}
				}
			} else if (groupPosition == 1) {
				for (APKBean apk : mAList) {
					if (apk.isSelect) {
						bs = true;
						break;
					}
				}
				if (bs) {
					for (APKBean apk : mAList) {
						apk.isSelect = false;
					}
				} else {
					for (APKBean apk : mAList) {
						apk.isSelect = true;
					}
				}
			} else if (groupPosition == 2) {
				for (RAMBean ram : mRList) {
					if (ram.isSelect) {
						bs = true;
						break;
					}
				}
				if (bs) {
					for (RAMBean ram : mRList) {
						ram.isSelect = false;
					}
				} else {
					for (RAMBean ram : mRList) {
						ram.isSelect = true;
					}
				}
			} else if (groupPosition == 3) {
				for (TrashCombination trash : mTList) {
					if (trash.isSelect) {
						bs = true;
						break;
					}
				}
				if (bs) {
					for (TrashCombination trash : mTList) {
						trash.isSelect = false;
					}
				} else {
					for (TrashCombination trash : mTList) {
						trash.isSelect = true;
					}
				}
				for (TrashCombination trash : mTList) {
					for (TrashBean tb : trash.list) {
						tb.isSelect = trash.isSelect;
					}
				}
			}
			notifyDataSetChanged();
			mHandler.sendEmptyMessage(CleanMasterActivity.MSG_UPDATE_SELECT);
		}
	};

	/**
	 * 单选点击
	 */
	private OnClickListener mItemSelectClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag instanceof CacheBean) {
				CacheBean cache = (CacheBean) tag;
				cache.isSelect = !cache.isSelect;
			} else if (tag instanceof APKBean) {
				APKBean apk = (APKBean) tag;
				apk.isSelect = !apk.isSelect;
			} else if (tag instanceof RAMBean) {
				RAMBean ram = (RAMBean) tag;
				ram.isSelect = !ram.isSelect;
				Setting setting = new Setting(TAApplication.getApplication());
				String json = setting.getString(Setting.PROTECT_APP);
				JSONArray array = null;
				try {
					array = new JSONArray(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (array == null) {
					array = new JSONArray();
				}
				if (!ram.isSelect) {
					// 加入内存白名单
					Set<String> set = arrayToSet(array);
					set.add(ram.pkgName);
					array = setToArray(set);
					setting.putString(Setting.PROTECT_APP, array.toString());
				} else {
					// 移出内存白名单
					Set<String> set = arrayToSet(array);
					set.remove(ram.pkgName);
					array = setToArray(set);
					setting.putString(Setting.PROTECT_APP, array.toString());
				}
			} else if (tag instanceof TrashCombination) {
				TrashCombination trash = (TrashCombination) tag;
				trash.isSelect = !trash.isSelect;
				for (TrashBean tb : trash.list) {
					tb.isSelect = trash.isSelect;
				}
			}
			notifyDataSetChanged();
			mHandler.sendEmptyMessage(CleanMasterActivity.MSG_UPDATE_SELECT);
		}
	};

	/**
	 * json数组转成set
	 */
	private Set<String> arrayToSet(JSONArray array) {
		Set<String> set = new HashSet<String>();
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				try {
					set.add(array.getString(i));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return set;
	}

	/**
	 * set转成json数组
	 */
	private JSONArray setToArray(Set<String> set) {
		JSONArray array = new JSONArray();
		for (String str : set) {
			array.put(str);
		}
		return array;
	}

	public CleanMasterAdapter(Handler handler) {
		mHandler = handler;
	}

	@Override
	public int getGroupCount() {
		return 4;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition == 0) {
			return mCList.size();
		}
		if (groupPosition == 1) {
			return mAList.size();
		}
		if (groupPosition == 2) {
			return mRList.size();
		}
		if (groupPosition == 3) {
			return mTList.size();
		}
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.cleanmaster_title, null);
		}
		ImageView select = (ImageView) convertView.findViewById(R.id.select);
		select.setTag(groupPosition);
		select.setOnClickListener(mTitleSelectClickListener);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView info1 = (TextView) convertView.findViewById(R.id.info1);
		TextView info2 = (TextView) convertView.findViewById(R.id.info2);
		TextView info3 = (TextView) convertView.findViewById(R.id.info3);
		ImageView arrow = (ImageView) convertView.findViewById(R.id.arrow);
		long size = 0;
		int selectCount = 0;
		boolean bs = false;
		if (groupPosition == 0) {
			title.setText("缓存清理");
			for (CacheBean cache : mCList) {
				if (cache.isSelect) {
					bs = true;
					size += cache.cache;
					selectCount++;
				}
			}
		} else if (groupPosition == 1) {
			title.setText("安装包清理");
			for (APKBean apk : mAList) {
				if (apk.isSelect) {
					bs = true;
					size += apk.size;
					selectCount++;
				}
			}
		} else if (groupPosition == 2) {
			title.setText("内存加速");
			for (RAMBean ram : mRList) {
				if (ram.isSelect) {
					bs = true;
					size += ram.ram;
					selectCount++;
				}
			}
		} else if (groupPosition == 3) {
			title.setText("残留文件");
			for (TrashCombination trash : mTList) {
				if (trash.isSelect) {
					bs = true;
					size += trash.size;
					selectCount++;
				}
			}
		}
		info1.setText("(" + selectCount + "项共");
		info2.setText(Formatter.formatFileSize(TAApplication.getApplication(),
				size));
		info3.setText(")");
		if (isExpanded) {
			arrow.setImageResource(R.drawable.arrow_up);
			convertView.findViewById(R.id.gap).setVisibility(View.GONE);
		} else {
			convertView.findViewById(R.id.gap).setVisibility(View.VISIBLE);
			arrow.setImageResource(R.drawable.arrow_down);
		}
		if (bs) {
			select.setImageResource(R.drawable.cleanmaster_select);
		} else {
			select.setImageResource(R.drawable.cleanmaster_noselect);
		}
		if (getChildrenCount(groupPosition) == 0) {
			arrow.setImageResource(R.drawable.arrow_down);
			info1.setText("干干净净");
			info2.setText("");
			info3.setText("");
			convertView.findViewById(R.id.gap).setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.cleanmaster_item, null);
		}
		View gap1 = convertView.findViewById(R.id.gap1);
		if (childPosition == 0) {
			gap1.setVisibility(View.GONE);
		} else {
			gap1.setVisibility(View.VISIBLE);
		}
		ImageView select = (ImageView) convertView.findViewById(R.id.select);
		select.setOnClickListener(mItemSelectClickListener);
		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		icon.setVisibility(View.VISIBLE);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView info1 = (TextView) convertView.findViewById(R.id.info1);
		TextView info2 = (TextView) convertView.findViewById(R.id.info2);
		View gap2 = convertView.findViewById(R.id.gap2);
		View gap3 = convertView.findViewById(R.id.gap3);
		if (isLastChild) {
			gap2.setVisibility(View.VISIBLE);
			gap3.setVisibility(View.VISIBLE);
		} else {
			gap2.setVisibility(View.GONE);
			gap3.setVisibility(View.GONE);
		}
		if (groupPosition == 0) {
			CacheBean cache = mCList.get(childPosition);
			select.setTag(cache);
			if (cache.isSelect) {
				select.setImageResource(R.drawable.cleanmaster_select);
			} else {
				select.setImageResource(R.drawable.cleanmaster_noselect);
			}
			title.setText(cache.name);
			icon.setTag(cache.pkgName);
			Bitmap bm = AsyncImageManager.getInstance().loadIcon(cache.pkgName,
					true, false, new AsyncImageLoadedCallBack() {

						@Override
						public void imageLoaded(Bitmap imageBitmap,
								String imgUrl) {
							if (imageBitmap != null
									&& imgUrl.equals(icon.getTag())) {
								icon.setImageBitmap(imageBitmap);
							}
						}
					});
			if (bm == null) {
				icon.setImageBitmap(DrawUtil.sDefaultIcon);
			} else {
				icon.setImageBitmap(bm);
			}
			info1.setText("可清理空间");
			info2.setText(cache.cache_str);
		} else if (groupPosition == 1) {
			APKBean apk = mAList.get(childPosition);
			select.setTag(apk);
			if (apk.isSelect) {
				select.setImageResource(R.drawable.cleanmaster_select);
			} else {
				select.setImageResource(R.drawable.cleanmaster_noselect);
			}
			title.setText(apk.name);
			if (apk.damage) {
				icon.setImageBitmap(DrawUtil.sDefaultIcon);
				info1.setText("已损坏");
				info2.setText(" ("
						+ Formatter.formatFileSize(
								TAApplication.getApplication(), apk.size) + ")");
			} else {
				icon.setTag(apk.pkgName);
				Bitmap bm = AsyncImageManager.getInstance().loadIcon(
						apk.pkgName, apk.path, true, true,
						new AsyncImageLoadedCallBack() {

							@Override
							public void imageLoaded(Bitmap imageBitmap,
									String imgUrl) {
								if (imageBitmap == null) {
									return;
								}
								if (icon.getTag().equals(imgUrl)) {
									icon.setImageBitmap(imageBitmap);
								}
							}
						});
				if (bm != null) {
					icon.setImageBitmap(bm);
				} else {
					// 默认
					icon.setImageBitmap(DrawUtil.sDefaultIcon);
				}
				if (AppUtils.isAppExist(TAApplication.getApplication(),
						apk.pkgName)) {
					if (AppUtils.getVersionCode(TAApplication.getApplication(),
							apk.pkgName) >= apk.versionCode) {
						info1.setText("已安装");
					} else if (AppUtils.getVersionCode(
							TAApplication.getApplication(), apk.pkgName) < apk.versionCode) {
						info1.setText("可升级");
					}
				} else {
					info1.setText("未安装");
				}
				info2.setText(" ("
						+ Formatter.formatFileSize(
								TAApplication.getApplication(), apk.size) + ")");
			}
		} else if (groupPosition == 2) {
			RAMBean ram = mRList.get(childPosition);
			select.setTag(ram);
			if (ram.isSelect) {
				select.setImageResource(R.drawable.cleanmaster_select);
			} else {
				select.setImageResource(R.drawable.cleanmaster_noselect);
			}
			title.setText(ram.name);
			icon.setTag(ram.pkgName);
			Bitmap bm = AsyncImageManager.getInstance().loadIcon(ram.pkgName,
					true, false, new AsyncImageLoadedCallBack() {

						@Override
						public void imageLoaded(Bitmap imageBitmap,
								String imgUrl) {
							if (imageBitmap != null
									&& imgUrl.equals(icon.getTag())) {
								icon.setImageBitmap(imageBitmap);
							}
						}
					});
			if (bm == null) {
				icon.setImageBitmap(DrawUtil.sDefaultIcon);
			} else {
				icon.setImageBitmap(bm);
			}
			info1.setText("可释放内存");
			info2.setText(ram.ram_str);
		} else if (groupPosition == 3) {
			icon.setVisibility(View.GONE);
			TrashCombination trash = mTList.get(childPosition);
			select.setTag(trash);
			if (trash.isSelect) {
				select.setImageResource(R.drawable.cleanmaster_select);
			} else {
				select.setImageResource(R.drawable.cleanmaster_noselect);
			}
			title.setText(trash.name);
			info1.setText("可清理空间");
			info2.setText(trash.size_str);
		}
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	public void update(CleanMasterDataBean bean) {
		if (bean == null) {
			return;
		}
		mCList.clear();
		mRList.clear();
		mAList.clear();
		mTList.clear();
		if (bean.cacheList != null) {
			mCList.addAll(bean.cacheList);
		}
		if (bean.ramList != null) {
			mRList.addAll(bean.ramList);
		}
		if (bean.apkList1 != null) {
			mAList.addAll(bean.apkList1);
		}
		if (bean.apkList2 != null) {
			mAList.addAll(bean.apkList2);
		}
		if (bean.apkList3 != null) {
			mAList.addAll(bean.apkList3);
		}
		// 处理数据
		Collections.sort(mCList);
		Collections.sort(mRList);
		Collections.sort(mAList);
		// 缩略图
		List<TrashBean> tList1 = new ArrayList<TrashBean>();
		// 空文件夹
		List<TrashBean> tList2 = new ArrayList<TrashBean>();
		// 临时文件
		List<TrashBean> tList3 = new ArrayList<TrashBean>();
		// 日志文件
		List<TrashBean> tList4 = new ArrayList<TrashBean>();
		for (int i = 0; i < 3; i++) {
			List<TrashBean> trList = null;
			if (i == 0) {
				trList = bean.trashList1;
			} else if (i == 1) {
				trList = bean.trashList2;
			} else if (i == 2) {
				trList = bean.trashList3;
			}
			if (trList != null) {
				for (TrashBean tb : trList) {
					if (tb.type == 1) {
						tList1.add(tb);
					} else if (tb.type == 2) {
						tList2.add(tb);
					} else if (tb.type == 3) {
						tList3.add(tb);
					} else if (tb.type == 4) {
						tList4.add(tb);
					}
				}
			}
		}
		if (tList1.size() > 0) {
			// 缩略图
			TrashCombination tcb = new TrashCombination();
			tcb.isSelect = true;
			tcb.name = "缩略图缓存文件(" + tList1.size() + "个)";
			long size = 0;
			for (TrashBean tb : tList1) {
				size += tb.size;
				tb.isSelect = true;
			}
			tcb.size = size;
			tcb.size_str = Formatter.formatFileSize(
					TAApplication.getApplication(), tcb.size);
			tcb.list = tList1;
			mTList.add(tcb);
		}
		if (tList2.size() > 0) {
			// 空文件夹
			TrashCombination tcb = new TrashCombination();
			tcb.isSelect = true;
			tcb.name = "空文件夹(" + tList2.size() + "个)";
			long size = 0;
			for (TrashBean tb : tList2) {
				size += tb.size;
				tb.isSelect = true;
			}
			tcb.size = size;
			tcb.size_str = Formatter.formatFileSize(
					TAApplication.getApplication(), tcb.size);
			tcb.list = tList2;
			mTList.add(tcb);
		}
		if (tList3.size() > 0) {
			// 临时文件
			TrashCombination tcb = new TrashCombination();
			tcb.isSelect = true;
			tcb.name = "临时文件(" + tList3.size() + "个)";
			long size = 0;
			for (TrashBean tb : tList3) {
				size += tb.size;
				tb.isSelect = true;
			}
			tcb.size = size;
			tcb.size_str = Formatter.formatFileSize(
					TAApplication.getApplication(), tcb.size);
			tcb.list = tList3;
			mTList.add(tcb);
		}
		if (tList4.size() > 0) {
			// 日志文件
			TrashCombination tcb = new TrashCombination();
			tcb.isSelect = true;
			tcb.name = "日志文件(" + tList4.size() + "个)";
			long size = 0;
			for (TrashBean tb : tList4) {
				size += tb.size;
				tb.isSelect = true;
			}
			tcb.size = size;
			tcb.size_str = Formatter.formatFileSize(
					TAApplication.getApplication(), tcb.size);
			tcb.list = tList4;
			mTList.add(tcb);
		}
		notifyDataSetChanged();
	}

	/**
	 * 残留垃圾
	 */
	public static class TrashCombination {
		public boolean isSelect;
		public String name;
		public long size;
		public String size_str;
		public List<TrashBean> list = new ArrayList<TrashBean>();
	}

}
