package com.quhwa.model.loadpagers;

import android.support.v4.app.Fragment;
import com.quhwa.fragment.LifeCircleFragment;
import com.quhwa.fragment.MyFragment;
import com.quhwa.fragment.SmartAreaFragment;
import com.quhwa.fragment.SmartFamilyFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * 加载Fragment页面数据
 *
 * @author lxz
 * @date 2017年3月17日
 */
public class PagerModelImpl implements IPagerModel{
	@Override
	public void loadPagersData(PagerOnLoadListener pagerOnLoadListener) {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(new SmartAreaFragment());
		fragments.add(new LifeCircleFragment());
		fragments.add(new SmartFamilyFragment());
		fragments.add(new MyFragment());
		pagerOnLoadListener.onComplete(fragments);
	}

}
