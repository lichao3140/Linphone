package com.quhwa.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.quhwa.MyApplication;
import com.quhwa.bean.Result;
import com.quhwa.bean.ReturnResult;
import com.quhwa.bean.UserInfo;
import com.quhwa.db.Table;
import com.quhwa.model.login.ILoginModel;
import com.quhwa.model.login.ILoginModel.LoginOnLoadListener;
import com.quhwa.model.login.ILoginModel.RecoverOnloadListener;
import com.quhwa.model.login.LoginModelImpl;
import com.quhwa.utils.Code;
import com.quhwa.utils.CommonUtil;
import com.quhwa.utils.Constants;
import com.quhwa.utils.MyLog;
import com.quhwa.utils.MySharedPreferenceManager;
import com.quhwa.view.ILoginView;

import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.R;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.mediastream.Log;

/**
 * 登陆表示层
 *
 * @author lxz
 * @date 2017年3月23日
 */
public class LoginPresenter {
	private ILoginView iLoginView;
	private UserInfo userInfo;
	private String Tag = "LoginPresenter";
	private LinphonePreferences mPrefs;

	public LoginPresenter(ILoginView iLoginView) {
		this.iLoginView = iLoginView;
	}
	public LoginPresenter(ILoginView iLoginView,UserInfo userInfo){
		this.iLoginView = iLoginView;
		this.userInfo = userInfo;
	}
	ILoginModel loginModel = new LoginModelImpl();
	private Context context;
	/**
	 * 登陆
	 */
	public void login(Context context){
		this.context = context;
		if(loginModel != null && iLoginView != null) {
			if (TextUtils.isEmpty(userInfo.getUsername()) || TextUtils.isEmpty(userInfo.getPassword())) {
				iLoginView.showToastInputIsNull();
			} else {
				String token = CommonUtil.getJpushToken();
				if(token != null){
					sendTokenToServer(token);
				}else {
					MyLog.print(Tag,"token为null，开始登陆",MyLog.PRINT_RED);
					loginToServer();
				}
			}
		}

	}

	/**
	 * 上传token
	 * @param token
     */
	private void sendTokenToServer(String token) {
		if(loginModel != null && iLoginView != null){
			iLoginView.loadDialog();
			loginModel.sendToken(new ILoginModel.SendTokenOnLoadListener() {
				@Override
				public void onComplete(ReturnResult result) {
					if(result.getCode() == Code.RETURN_SUCCESS){
						MyLog.print(Tag,"上传token成功，开始登陆",MyLog.PRINT_RED);
						loginToServer();
					}else{
//						iLoginView.showToastLoginFail();
//						iLoginView.dismissDialog();
						MyLog.print(Tag,"上传token失败，开始登陆",MyLog.PRINT_RED);
						loginToServer();
					}
				}

				@Override
				public void onCompleteFail() {
					iLoginView.showToastLoginFail();
					iLoginView.dismissDialog();
				}

				@Override
				public void onNoNet() {

				}
			},token);
		}
	}

	/**
	 * 登陆到服务器
	 */
	private void loginToServer() {
		loginModel.loadLoginData(new LoginOnLoadListener() {

			@Override
			public void onCompleteFail() {
				iLoginView.showToastLoginFail();
				iLoginView.dismissDialog();
			}

			@Override
			public void onNoNet() {

			}

			@Override
			public void onComplete(Result result) {
				if(result.getCode() == Code.RETURN_SUCCESS){
					UserInfo userInfoFrom = result.getUserInfo();
					iLoginView.showToastLoginSuccess(userInfoFrom);
//							long count = DatabaseManager.INSTANCE.queryCount(UserInfo.class);
					userInfoFrom.setPassword(userInfo.getPassword());
//							Status.LOGIN_STATUS = true;
//							MySharedPreferenceManager.saveBoolean(MyApplication.instance, Table.TAB_LOGIN_STATUS, Table.TAB_LOGIN_STATUS, Status.LOGIN_STATUS);
					MySharedPreferenceManager.saveString(MyApplication.instance, Table.TAB_USER, Table.TAB_USER_NAME_KEY, userInfoFrom.getUsername());
					MySharedPreferenceManager.saveString(MyApplication.instance, Table.TAB_USER, Table.TAB_USER_PASSWORD_KEY, userInfoFrom.getPassword());
					MySharedPreferenceManager.saveString(MyApplication.instance, Table.TAB_USER, Table.TAB_USER_PASSWORD_SIP_ID, userInfoFrom.getSipid());
					MySharedPreferenceManager.saveString(MyApplication.instance, Table.TAB_USER, Table.TAB_USER_PASSWORD_SIP_PASSWORD, userInfoFrom.getSipPasswd());
					MySharedPreferenceManager.saveString(MyApplication.instance, Table.TAB_USER, Table.TAB_USER_SESSION_KEY, userInfoFrom.getSessionKey());
					//保存userId
					MySharedPreferenceManager.saveInt(MyApplication.instance, Table.TAB_USER,
							Table.TAB_USER_USERID_KEY,userInfoFrom.getId());

					//sip注册
					//PJSipService.registerToSipServer(userInfoFrom.getSipid(), userInfoFrom.getSipPasswd());
//					AssistantActivity.instance().genericLogIn(userInfoFrom.getSipid(), userInfoFrom.getSipPasswd(), "", Constants.SERVER_IP, LinphoneAddress.TransportType.LinphoneTransportUdp);
					logIn(userInfoFrom.getSipid(), userInfoFrom.getSipPasswd(), "", Constants.SERVER_IP, LinphoneAddress.TransportType.LinphoneTransportUdp);
					MyLog.print(Tag, "登陆成功后注册sip账号", MyLog.PRINT_RED);
				}
				if(result.getCode() == Code.RETURN_PASSWD_ERROR){
					iLoginView.showToastPasswordError();
				}
				if(result.getCode() == Code.RETURN_USERNAME_NOT_EXIST){
					iLoginView.showToastUsernameIsNotExist();
				}
				iLoginView.dismissDialog();

			}
		}, userInfo);
	}

	private void logIn(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport) {
		saveCreatedAccount(username, password, displayName, domain, transport);
	}

	public void saveCreatedAccount(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport) {
//		if (accountCreated)
//			return;
		mPrefs = LinphonePreferences.instance();
		if(username.startsWith("sip:")) {
			username = username.substring(4);
		}

		if (username.contains("@"))
			username = username.split("@")[0];

		if(domain.startsWith("sip:")) {
			domain = domain.substring(4);
		}

		String identity = "sip:" + username + "@" + domain;
		LinphoneAddress address = null;
		try {
			address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);//执行
		} catch (LinphoneCoreException e) {
			Log.e(e);
		}

		if(address != null && displayName != null && !displayName.equals("")){//执行
			address.setDisplayName(displayName);
		}

		boolean isMainAccountLinphoneDotOrg = domain.equals("sip.linphone.org");
		LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())//执行
				.setUsername(username)
				.setDomain(domain)
				.setDisplayName(displayName)
				.setPassword(password);

		if (isMainAccountLinphoneDotOrg) {
			if (context.getResources().getBoolean(R.bool.disable_all_security_features_for_markets)) {
				builder.setProxy(domain)
						.setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
			}
			else {
				builder.setProxy(domain)
						.setTransport(LinphoneAddress.TransportType.LinphoneTransportTls);
			}

			builder.setExpires("604800")
					.setAvpfEnabled(true)
					.setAvpfRRInterval(3)
					.setQualityReportingCollector("sip:voip-metrics@sip.linphone.org")
					.setQualityReportingEnabled(true)
					.setQualityReportingInterval(180)
					.setRealm("sip.linphone.org")
					.setNoDefault(false);


			mPrefs.setStunServer(context.getString(R.string.default_stun));
			mPrefs.setIceEnabled(true);
		} else {//执行
			String forcedProxy = "";
			if (!TextUtils.isEmpty(forcedProxy)) {
				builder.setProxy(forcedProxy)
						.setOutboundProxyEnabled(true)
						.setAvpfRRInterval(5);
			}

			if(transport != null) {//执行
				builder.setTransport(transport);
			}
		}

		if (context.getResources().getBoolean(R.bool.enable_push_id)) {//执行
			String regId = mPrefs.getPushNotificationRegistrationID();//regId=null
			String appId = context.getString(R.string.push_sender_id);
			if (regId != null && mPrefs.isPushNotificationEnabled()) {
				String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
				builder.setContactParameters(contactInfos);
			}
		}

		try {
			builder.saveNewAccount();//执行
//			if(!newAccount) {//执行
//				displayRegistrationInProgressDialog();
//			}
//			accountCreated = true;
		} catch (LinphoneCoreException e) {
			Log.e(e);
		}


	}

	/**
	 * 恢复控件显示状态
	 */
	public void recoverWidgetShowStatus(){
		if(loginModel != null && iLoginView != null){
			loginModel.recoverWidgetShowStatus(new RecoverOnloadListener() {
				@Override
				public void onComplete(UserInfo userInfo) {
					if(userInfo != null){
						iLoginView.recoverWidgetShowStatus(userInfo);
					}
				}
			});
		}
	}
}
