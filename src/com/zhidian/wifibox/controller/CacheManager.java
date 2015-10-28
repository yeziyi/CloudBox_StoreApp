package com.zhidian.wifibox.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;

import com.zhidian.wifibox.data.CacheDataBean;

public class CacheManager {

	private PackageManager packageManager;
	private OnActionListener onActionListener = null;
	private boolean isScanning = false;
	private boolean isCleaning = false;

	public interface OnActionListener {
		public void onScanStarted(int appsCount);

		public void onScanProgressUpdated(int current, int max);

		public void onScanCompleted(List<CacheDataBean> apps);

		public void onCleanStarted();

		public void onCleanCompleted(long cacheSize);
	}

	private class TaskScan extends
			AsyncTask<Void, Integer, List<CacheDataBean>> {

		private CountDownLatch countDownLatch;
		private List<ApplicationInfo> packages;
		private int appCount = 1;

		@Override
		protected void onPreExecute() {
			packages = packageManager
					.getInstalledApplications(PackageManager.GET_META_DATA);

			countDownLatch = new CountDownLatch(packages.size());

			if (onActionListener != null)
				onActionListener.onScanStarted(packages.size());
		}

		@Override
		protected List<CacheDataBean> doInBackground(Void... params) {
			final List<CacheDataBean> apps = new ArrayList<CacheDataBean>();

			for (ApplicationInfo pkg : packages) {
				invokeMethod("getPackageSizeInfo", pkg.packageName,
						new IPackageStatsObserver.Stub() {

							@Override
							public void onGetStatsCompleted(
									PackageStats pStats, boolean succeeded)
									throws RemoteException {
								publishProgress(appCount++);

								if (succeeded) {
									try {
										if (pStats.cacheSize > 0) {
											CacheDataBean bean = new CacheDataBean();
											ApplicationInfo info = packageManager
													.getApplicationInfo(
															pStats.packageName,
															PackageManager.GET_META_DATA);
											bean.mName = packageManager
													.getApplicationLabel(info)
													.toString();
											bean.mInfo = info;
											bean.mCache = pStats.cacheSize;
											apps.add(bean);
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

								countDownLatch.countDown();
							}
						});
			}

			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
			}

			return apps;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (onActionListener != null)
				onActionListener.onScanProgressUpdated(values[0],
						packages.size());
		}

		@Override
		protected void onPostExecute(List<CacheDataBean> result) {
			if (onActionListener != null)
				onActionListener.onScanCompleted(result);

			isScanning = false;
		}
	}

	private class TaskClean extends AsyncTask<Long, Void, Long> {

		private CountDownLatch countDownLatch = new CountDownLatch(1);

		@Override
		protected void onPreExecute() {
			if (onActionListener != null)
				onActionListener.onCleanStarted();
		}

		@Override
		protected Long doInBackground(Long... params) {
			StatFs stat = new StatFs(Environment.getDataDirectory()
					.getAbsolutePath());

			invokeMethod(
					"freeStorageAndNotify",
					(2 * params[0])
							+ ((long) stat.getFreeBlocks() * (long) stat
									.getBlockSize()),
					new IPackageDataObserver.Stub() {
						@Override
						public void onRemoveCompleted(String packageName,
								boolean succeeded) throws RemoteException {
							countDownLatch.countDown();
						}
					});

			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
			}

			return params[0];
		}

		@Override
		protected void onPostExecute(Long result) {
			if (onActionListener != null)
				onActionListener.onCleanCompleted(result);

			isCleaning = false;
		}
	}

	public CacheManager(PackageManager packageManager) {
		this.packageManager = packageManager;
	}

	private Method getMethod(String methodName) {
		for (Method method : packageManager.getClass().getMethods()) {
			if (method.getName().equals(methodName))
				return method;
		}

		return null;
	}

	private void invokeMethod(String method, Object... args) {
		try {
			getMethod(method).invoke(packageManager, args);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void scanCache() {
		isScanning = true;

		new TaskScan().execute();
	}

	public void cleanCache(long cacheSize) {
		isCleaning = true;

		new TaskClean().execute(cacheSize);
	}

	public void setOnActionListener(OnActionListener listener) {
		onActionListener = listener;
	}

	public boolean isScanning() {
		return isScanning;
	}

	public boolean isCleaning() {
		return isCleaning;
	}
}
