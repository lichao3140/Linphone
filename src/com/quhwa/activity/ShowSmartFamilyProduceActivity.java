package com.quhwa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.quhwa.bean.SmartProduce;
import com.zhy.autolayout.AutoRelativeLayout;
import org.linphone.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowSmartFamilyProduceActivity extends BaseActivity implements View.OnTouchListener {

    @BindView(R.id.iv_smart_produce)
    ImageView ivSmartProduce;
    @BindView(R.id.tv_smart_produce_name)
    TextView tvSmartProduceName;
    @BindView(R.id.activity_show_smart_family_produce)
    AutoRelativeLayout activityShowSmartFamilyProduce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_smart_family_produce);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        SmartProduce produce = (SmartProduce) bundle.get("smartProduce");
        setData(produce);
    }

    private void setData(SmartProduce produce) {
        ivSmartProduce.setBackgroundResource(produce.getSpRes());
        tvSmartProduceName.setText(produce.getSpName());
        activityShowSmartFamilyProduce.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ShowSmartFamilyProduceActivity.this.finish();
        return false;
    }
}
