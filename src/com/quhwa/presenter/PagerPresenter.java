package com.quhwa.presenter;

import android.support.v4.app.Fragment;
import com.quhwa.model.loadpagers.IPagerModel;
import com.quhwa.model.loadpagers.IPagerModel.PagerOnLoadListener;
import com.quhwa.model.loadpagers.PagerModelImpl;
import com.quhwa.view.IPagerView;
import java.util.List;

/**
 * Fragment表示层：处理view层显示页面和model层业务处理类
 *
 * @author lxz
 * @date 2017年3月17日
 */
public class PagerPresenter {
	//view层的引用
	IPagerView pagerView;
	public PagerPresenter(IPagerView pagerView){
		this.pagerView = pagerView;
	}
	//model层的引用
	IPagerModel pagerModel = new PagerModelImpl();
	/**
	 * 加载各页面数据
	 */
	public void loadFragments(){
		if(pagerModel != null){
			pagerModel.loadPagersData(new PagerOnLoadListener() {
				@Override
				public void onComplete(List<Fragment> fragments) {
					if(pagerView != null){
						pagerView.load(fragments);//加载页面
					}
				}
			});
		}
	}
}
