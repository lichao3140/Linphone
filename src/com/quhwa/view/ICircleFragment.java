package com.quhwa.view;

import com.quhwa.bean.Produce;
import java.util.List;

public interface ICircleFragment {
	/**加载通话记录列表*/
	void loadProduceList(List<Produce> produces);
	
	void refreshCompelete();
}
