package com.quhwa.model.register;

import com.quhwa.bean.Result;
import com.quhwa.bean.UserInfo;

/**
 * 注册model层接口
 *
 * @author lxz
 * @date 2017年3月23日
 */
public interface IRegisterModel {
	void loadRegisterData(RegisterOnLoadListener registerOnLoadListener, UserInfo userInfo);
	interface RegisterOnLoadListener{
		void onComplete(Result result);//可获取到数据
		void onCompleteFail();//获取不到数据
	}
}
