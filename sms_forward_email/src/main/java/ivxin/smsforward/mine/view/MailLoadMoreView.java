package ivxin.smsforward.mine.view;

import com.chad.library.adapter.base.loadmore.LoadMoreView;

import ivxin.smsforward.mine.R;

public class MailLoadMoreView extends LoadMoreView {
    @Override
    public int getLayoutId() {
        return R.layout.layout_loadmore;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.ll_loading;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.tv_load_fail;
    }

    @Override
    protected int getLoadEndViewId() {
        return R.id.tv_load;
    }

}
