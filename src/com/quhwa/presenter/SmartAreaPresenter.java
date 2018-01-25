package com.quhwa.presenter;

import android.os.Handler;

import com.quhwa.bean.Advertisement;
import com.quhwa.model.autoplaypicture.AutoPlayPictureModelImpl;
import com.quhwa.model.autoplaypicture.IAutoPlayPictureModel;
import com.quhwa.view.ISmartAreaView;
import java.util.List;

public class SmartAreaPresenter {
	private ISmartAreaView iSmartAreaView;

	public SmartAreaPresenter(ISmartAreaView iSmartAreaView) {
		super();
		this.iSmartAreaView = iSmartAreaView;
	}
	
	private IAutoPlayPictureModel autoPlayPictureModelImpl = new AutoPlayPictureModelImpl();
	/**
	 * 轮播图自动播放
	 * @param handler
	 */
	public void play(Handler handler){
		if(iSmartAreaView != null && autoPlayPictureModelImpl != null){
			handler.sendEmptyMessageDelayed(0, 6000);
		}
	}
	/**
	 * 加载轮播图
	 */
	public void load(){
		if(iSmartAreaView != null && autoPlayPictureModelImpl != null){
			autoPlayPictureModelImpl.loadPictureData(new IAutoPlayPictureModel.PictureDataOnLoadListener() {
				@Override
				public void onCompelete(List<Advertisement> advertisements) {
					iSmartAreaView.loadPicture(advertisements);
				}
			});
		}
	}

}
