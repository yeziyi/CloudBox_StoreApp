package com.zhidian.wifibox.db.dao;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.db.table.AppDownloadTable;

/**
 * app下载量统计，数据库操作类
 * 
 * @author zhaoyl
 * 
 */
public class AppDownloadDao {
	private DBOpenHelper dbOpenHelper;

	public AppDownloadDao(Context context) {
		dbOpenHelper = DBOpenHelper.getInstance(context);
	}

	private SQLiteDatabase getDb(boolean writeable) {
		synchronized (DBOpenHelper.sObj) {
			if (writeable) {
				return dbOpenHelper.getWritableDatabase();
			} else {
				return dbOpenHelper.getReadableDatabase();
			}
		}
	}

	/***************************
	 * 新增app下载量统计日志记录begin
	 ***************************/
	public void saveAppDownloadInfo(AppDownloadCount bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				db.insert(AppDownloadTable.TABLE_NAME, null,
						downloadToContentValues(bean));
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (db != null) {
					try {
						db.endTransaction();
						db.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	private ContentValues downloadToContentValues(AppDownloadCount bean) {
		final ContentValues cv = new ContentValues();
		cv.put(AppDownloadTable.FIELD_BOXNUM, bean.boxNum);
		cv.put(AppDownloadTable.FIELD_UUID, bean.uuId);
		cv.put(AppDownloadTable.FIELD_DOWNLOADSOURCE, bean.downloadSource);
		cv.put(AppDownloadTable.FIELD_APPID, bean.appId);
		cv.put(AppDownloadTable.FIELD_PACKAGENAME, bean.packageName);
		cv.put(AppDownloadTable.FIELD_VERSION, bean.version);
		cv.put(AppDownloadTable.FIELD_DOWNLOADMODEL, bean.downloadModel);
		cv.put(AppDownloadTable.FIELD_NETWORKWAY, bean.networkWay);
		cv.put(AppDownloadTable.FIELD_DOWNLOADTIME, bean.downloadTime);
		return cv;
	}

	/************************
	 * 获取所有App下载量统计日志记录
	 ************************/
	public List<AppDownloadCount> getSpkData() {
		List<AppDownloadCount> result = new ArrayList<AppDownloadCount>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (DBOpenHelper.sObj) {
			try {
				db = getDb(false);
				cursor = db.rawQuery("select * from "
						+ AppDownloadTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					AppDownloadCount bean = new AppDownloadCount();
					bean.boxNum = cursor.getString(1);
					bean.uuId = cursor.getString(2);
					bean.downloadSource = cursor.getString(3);
					bean.appId = cursor.getString(4);
					bean.packageName = cursor.getString(5);
					bean.downloadModel = cursor.getString(6);
					bean.version = cursor.getString(7);
					bean.networkWay = cursor.getString(8);
					bean.downloadTime = cursor.getString(9);
					result.add(bean);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (cursor != null) {
						cursor.close();
					}
					if (db != null) {
						db.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/************************
	 * 删除所有App下载量统计日志记录
	 ************************/
	public void deleteData() {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.execSQL("delete from " + AppDownloadTable.TABLE_NAME
						+ " where 1=1", new Object[] {});
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (db != null) {
					try {
						db.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/************************
	 * 删除一条App下载量统计日志记录
	 ************************/
	public void deleteData(String packageName) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.execSQL("delete from " + AppDownloadTable.TABLE_NAME
						+ " where packageName=?", new String[] { packageName });
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (db != null) {
					try {
						db.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
