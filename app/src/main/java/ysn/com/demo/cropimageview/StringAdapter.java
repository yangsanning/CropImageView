package ysn.com.demo.cropimageview;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

/**
 * @Author yangsanning
 * @ClassName StringAdapter
 * @Description 一句话概括作用
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class StringAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public StringAdapter() {
        super(R.layout.item_string);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.string_item_text, item);
    }
}
