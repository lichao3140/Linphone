package com.quhwa.model.autoplaypicture;

import com.quhwa.bean.Advertisement;
import java.util.List;

public interface IAutoPlayPictureModel {
	void loadPictureData(PictureDataOnLoadListener pictureDataOnLoadListener);
	interface PictureDataOnLoadListener{
		void onCompelete(List<Advertisement> advertisements);
	}
}
