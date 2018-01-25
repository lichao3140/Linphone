package com.quhwa.view;

import com.quhwa.bean.Advertisement;
import java.util.List;

public interface ISmartAreaView {
	/***
	 * 加载轮播图
	 * @param advertisements
	 */
	void loadPicture(List<Advertisement> advertisements);
}
