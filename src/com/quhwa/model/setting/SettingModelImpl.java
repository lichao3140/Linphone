package com.quhwa.model.setting;

import android.os.Handler;
import android.os.Message;
import com.quhwa.MyApplication;
import com.quhwa.bean.BoundResult;
import com.quhwa.bean.ReturnResult;
import com.quhwa.db.Table;
import com.quhwa.netmanager.OkhttpManager;
import com.quhwa.netmanager.RequestParamsValues;
import com.quhwa.utils.Constants;
import com.quhwa.utils.MSG;
import com.quhwa.utils.MyLog;
import com.quhwa.utils.MySharedPreferenceManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lxz on 2017/10/16 0016.
 */

public class SettingModelImpl implements ISettingModel {
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG.ALTER_DISTURB_STATUS_SUCCESS:
                    ReturnResult result = (ReturnResult) msg.obj;
                    disturbStatusListener.loadDisturbStatusOnComplete(result);
                    break;
                case MSG.SERVER_EXCEPTION:
                    disturbStatusListener.onCompleteFail();
                break;
                case MSG.NO_NET_MSG:
                    disturbStatusListener.onNoNet();
                break;
            }
        }
    };
    private DisturbStatusListener disturbStatusListener;
    private String Tag = SettingModelImpl.class.getSimpleName();


    @Override
    public void setDisturbStatus(DisturbStatusListener disturbStatusListener, BoundResult.Device device,String deviceStatus) {
        this.disturbStatusListener = disturbStatusListener;
        RequestParamsValues requestParams = new RequestParamsValues();
        HashMap<String, String> map = new HashMap<String, String>();
        int userId = MySharedPreferenceManager.queryInt(MyApplication.instance, Table.TAB_USER,Table.TAB_USER_USERID_KEY);
        map.put("userId", userId+"");
        map.put("deviceId", device.getDeviceId()+"");
        map.put("status", deviceStatus);
        MyLog.print(Tag,"deviceStatus:"+deviceStatus,MyLog.PRINT_RED);
        HashMap<String, String> addRequestParams = requestParams
                .addRequestParams(map);
        OkhttpManager okhttpManager = new OkhttpManager();
        List<Class> clsList = new ArrayList<Class>();
        clsList.add(ReturnResult.class);
        okhttpManager.getData(Constants.DISTURBED_URL, addRequestParams,
                clsList, handler, MSG.ALTER_DISTURB_STATUS_SUCCESS,
                MSG.SERVER_EXCEPTION,MSG.NO_NET_MSG,0);
    }
}
