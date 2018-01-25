package com.quhwa.presenter;

import com.quhwa.bean.Produce;
import com.quhwa.fragment.LifeCircleFragment;
import com.quhwa.model.produce.IProduceModel;
import com.quhwa.model.produce.ProduceModelImpl;
import java.util.List;

public class LifeCircelPresenter {
	private LifeCircleFragment lifeCircleView;

	public LifeCircelPresenter(LifeCircleFragment lifeCircleView) {
		super();
		this.lifeCircleView = lifeCircleView;
	}

	IProduceModel produceModel = new ProduceModelImpl();
	public void loadProduceList(){
		if(lifeCircleView != null && produceModel != null){
			produceModel.loadProduceData(new IProduceModel.ProduceDataOnLoadListener() {
				@Override
				public void onComplete(List<Produce> produces) {
					lifeCircleView.loadProduceList(produces);
					lifeCircleView.refreshCompelete();
				}
			});
		}
	}
	
}
