package com.quhwa.model.loadpagers;

import android.support.v4.app.Fragment;

import java.util.List;
/**
 * Fragment页面数据接口
 *
 * @author lxz
 * @date 2017年3月17日
 */
public interface IPagerModel {
	/**
	 * 加载页面数据
	 */
	void loadPagersData(PagerOnLoadListener pagerOnLoadListener);
	/**
	 * 页面数据加载监听器
	 * @author lxz
	 *
	 */
	interface PagerOnLoadListener{
		void onComplete(List<Fragment> fragments);
	}
}
