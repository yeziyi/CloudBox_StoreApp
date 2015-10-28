package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;

/**
 * 应用分类列表控制器
 * 
 * @author xiedezhi
 * 
 */
public class CategoriesController extends TACommand {

	/**
	 * 加载下一页命令
	 */
	public static final String NEXT_PAGE = "CATEGORIESCONTROLLER_NEXT_PAGE";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(NEXT_PAGE)) {
			String[] obj = (String[]) request.getData();
			final String idUrl = obj[0];
			final String dataUrl = obj[1];
			CDataDownloader.getData(dataUrl, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
					// 加载成功
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.CATEGORIES_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = 1;
					DataParser.parseCategoryList(
							TAApplication.getApplication(), bean, content);
					CategoriesController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onStart() {
				}

				@Override
				public void onFailure(Throwable error) {
					// 加载失败
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.CATEGORIES_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = 1;
					bean.mStatuscode = 2;
					bean.mMessage = error.getMessage();
					CategoriesController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onFinish() {
				}
			});
		}
	}

}
