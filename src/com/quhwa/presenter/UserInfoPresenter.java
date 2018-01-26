package com.quhwa.presenter;

import android.os.Handler;

import com.quhwa.bean.ReturnResult;
import com.quhwa.model.userinfo.IUserInfoModel;
import com.quhwa.model.userinfo.UserInfoModelImpl;
import com.quhwa.utils.Code;
import com.quhwa.utils.CommonUtil;
import com.quhwa.utils.MSG;
import com.quhwa.utils.MyLog;
import com.quhwa.utils.TimerUtils;
import com.quhwa.view.IUserInfoView;

public class UserInfoPresenter {
	private IUserInfoView iUserInfoView;
	private String Tag = "UserInfoPresenter";
	private IUserInfoModel userInfoModel = new UserInfoModelImpl();
	public UserInfoPresenter(IUserInfoView iUserInfoView) {
		super();
		this.iUserInfoView = iUserInfoView;
	}
	/**
	 * 检测sip注册状态
	 */
	public void checkSipRegisterStatus(final Handler handler){
		TimerUtils.sendSipMsgTimer(null, handler, MSG.MSG_REGISTER_SIP_SERVER,3000,true);
	}
	/**
	 * 开始检测
	 */
	public void startCheck() {
		MyLog.print(Tag, "检测sip注册状态", MyLog.PRINT_RED);
		//iUserInfoView.showSipRegisterStatus(PJSipService.instance.getSipRegisterStatus());
	}
	/**
	 * 停止检测
	 */
	public void stopCheck(){
		TimerUtils.stopToSend();
	}

	public void deleteToken(){
		if(userInfoModel != null && iUserInfoView != null){
			userInfoModel.deleteToken(new IUserInfoModel.OnDeleteTokenlistener() {
				@Override
				public void onCompelete(ReturnResult result) {
					if(result.getCode() == Code.RETURN_SUCCESS){
						MyLog.print(Tag,"删除token成功,开始删除本地相关数据,退出登陆",MyLog.PRINT_RED);
						iUserInfoView.deleteLocalData();
					}else{
						MyLog.print(Tag,"删除token失败,开始删除本地相关数据,退出登陆code："+result.getCode(),MyLog.PRINT_RED);
						iUserInfoView.deleteLocalData();
					}
				}

				@Override
				public void onCompleteFail() {
					MyLog.print(Tag,"服务器繁忙，删除token失败,开始删除本地相关数据,退出登陆",MyLog.PRINT_RED);
					iUserInfoView.deleteLocalData();
				}

				@Override
				public void onNoNet() {
					MyLog.print(Tag,"没有网络,删除token成失败,开始删除本地相关数据,退出登陆",MyLog.PRINT_RED);
					iUserInfoView.deleteLocalData();
				}
			}, CommonUtil.getToken());
		}

	}
}
