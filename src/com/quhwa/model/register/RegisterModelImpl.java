package com.quhwa.model.register;

import android.os.Handler;
import android.os.Message;
import com.quhwa.bean.Result;
import com.quhwa.bean.UserInfo;
import com.quhwa.netmanager.OkhttpManager;
import com.quhwa.netmanager.RequestParamsValues;
import com.quhwa.utils.Constants;
import com.quhwa.utils.MSG;
import com.quhwa.utils.MyLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 注册model层，获取注册数据
 *
 * @author lxz
 * @date 2017年3月23日
 */
public class RegisterModelImpl implements IRegisterModel{
	private RegisterOnLoadListener registerOnLoadListener;
	private String Tag = "RegisterModelImpl";
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG.REGISTER_SUCCESS:
				Result result = (Result) msg.obj;
				registerOnLoadListener.onComplete(result);
				break;
			case MSG.SERVER_EXCEPTION:
				registerOnLoadListener.onCompleteFail();
				break;
			case MSG.NO_NET_MSG:

				break;
			}
		};
	};
	@Override
	public void loadRegisterData(RegisterOnLoadListener registerOnLoadListener,UserInfo userInfo) {
		this.registerOnLoadListener = registerOnLoadListener;
		MyLog.print(Tag , "---userInfo---"+userInfo.toString(), MyLog.PRINT_GREEN);
		RequestParamsValues requestParams = new RequestParamsValues();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", userInfo.getUsername());
		map.put("password", userInfo.getPassword());
		map.put("type", userInfo.getType());
		HashMap<String, String> addRequestParams = requestParams
				.addRequestParams(map);
		OkhttpManager okhttpManager = new OkhttpManager();
		List<Class> clsList = new ArrayList<Class>();
		clsList.add(UserInfo.class);
		clsList.add(Result.class);
		okhttpManager.getData(Constants.REGISTER_URL, addRequestParams,
				clsList, handler, MSG.REGISTER_SUCCESS,
				MSG.SERVER_EXCEPTION,MSG.NO_NET_MSG,0);
		
	}

	

}
