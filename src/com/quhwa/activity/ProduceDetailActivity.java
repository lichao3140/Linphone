package com.quhwa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.quhwa.bean.Produce;
import org.linphone.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 商品详情
 */
public class ProduceDetailActivity extends BaseActivity {

    @BindView(R.id.iv_produce)
    ImageView ivProduce;
    @BindView(R.id.tv_pro_detail)
    TextView tvProDetail;
    @BindView(R.id.tv_pro_price)
    TextView tvProPrice;
    @BindView(R.id.tv_shop)
    TextView tvShop;
    @BindView(R.id.tv_shop_phone)
    TextView tvShopPhone;
    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.tv_title_text)
    TextView tvTitleText;
    @BindView(R.id.activity_produce_detail)
    LinearLayout activityProduceDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produce_detail);
        ButterKnife.bind(this);
        tvTitleText.setText("商品详情");
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Produce produce = (Produce) bundle.get("produce");
        setData(produce);
    }

    private void setData(Produce produce) {
        ivProduce.setBackgroundResource(produce.getImageRes());
        tvProDetail.setText(produce.getProName());
        tvProPrice.setText(produce.getProPrice() + "元");
        tvShop.setText(produce.getShopName());
        tvShopPhone.setText(produce.getShopPhone());
    }

    @OnClick(R.id.back)
    public void onViewClicked() {
        finish();
    }
}
