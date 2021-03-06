package jason.wondermap.fragment;

import com.xiaomi.mistatistic.sdk.MiStatInterface;

import jason.wondermap.adapter.AIContentAdapter;
import jason.wondermap.adapter.BaseContentAdapter;
import jason.wondermap.bean.Blog;
import jason.wondermap.manager.FootblogManager;
import android.view.View;
import android.widget.AdapterView;

public class FavouriteFragment extends CommonPullRefreshFragment {
	@Override
	protected void initTopBar() {
		initTopBarForLeft(contentView, "我的收藏");
	};

	@Override
	public BaseContentAdapter<Blog> getAdapter() {
		return new AIContentAdapter(mContext, mListItems);
	}

	@Override
	public void onListItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		FootblogManager.getInstance().setCurrentBlog(
				mListItems.get(position - 1));
		wmFragmentManager.showFragment(WMFragmentManager.TYPE_FOOTBLOG_COMMENT);
	}
	@Override
	public void onResume() {
		super.onResume();
		MiStatInterface.recordPageStart(getActivity(), "收藏页");
	}
	@Override
	public void onPause() {
		super.onPause();
		MiStatInterface.recordPageEnd();
	}
}
