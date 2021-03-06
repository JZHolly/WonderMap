package jason.wondermap.fragment;

import jason.wondermap.R;
import jason.wondermap.adapter.AIContentAdapter;
import jason.wondermap.bean.Blog;
import jason.wondermap.bean.User;
import jason.wondermap.config.WMapConstants;
import jason.wondermap.dao.DatabaseUtil;
import jason.wondermap.manager.AccountUserManager;
import jason.wondermap.manager.FootblogManager;
import jason.wondermap.utils.L;
import jason.wondermap.utils.T;
import jason.wondermap.utils.WModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class QiangContentFragment extends RealFragment {

	public static final String TAG = "QiangContentFragment";
	private View contentView;
	private int currentIndex;
	private int pageNum;
	private String lastItemTime;// 当前列表结尾的条目的创建时间，

	private ArrayList<Blog> mListItems;
	private PullToRefreshListView mPullRefreshListView;
	private AIContentAdapter mAdapter;
	private ListView actualListView;

	private ProgressBar progressbar;
	private boolean pullFromUser;

	public enum RefreshType {
		REFRESH, LOAD_MORE
	}

	private RefreshType mRefreshType = RefreshType.LOAD_MORE;

	public static Fragment newInstance(int index) {
		Fragment fragment = new QiangContentFragment();
		Bundle args = new Bundle();
		args.putInt("page", index);
		fragment.setArguments(args);
		return fragment;
	}

	private String getCurrentTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String times = formatter.format(new Date(System.currentTimeMillis()));
		return times;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentIndex = getArguments().getInt("page");
		pageNum = 0;
		lastItemTime = getCurrentTime();
		L.i(TAG, "curent time:" + lastItemTime);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		contentView = inflater.inflate(R.layout.fragment_qiangcontent, null);
		mPullRefreshListView = (PullToRefreshListView) contentView
				.findViewById(R.id.pull_refresh_list);
		progressbar = (ProgressBar) contentView.findViewById(R.id.progressBar);
		mPullRefreshListView.setMode(Mode.BOTH);
		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(getActivity(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);
						mPullRefreshListView.setMode(Mode.BOTH);
						pullFromUser = true;
						mRefreshType = RefreshType.REFRESH;
						pageNum = 0;
						lastItemTime = getCurrentTime();
						fetchData();
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						mRefreshType = RefreshType.LOAD_MORE;
						fetchData();
					}
				});
		mPullRefreshListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {

					}
				});

		actualListView = mPullRefreshListView.getRefreshableView();
		mListItems = new ArrayList<Blog>();
		mAdapter = new AIContentAdapter(mContext, mListItems, false);
		actualListView.setAdapter(mAdapter);
		ViewGroup emptyView = (ViewGroup) BaseFragment.getInflater().inflate(
				R.layout.view_footblog_empty, null);
		TextView t = (TextView) emptyView.findViewById(R.id.tv_empty_tips);
		t.setText(R.string.empty_tips_footblog);
		actualListView.setEmptyView(emptyView);
		if (mListItems.size() == 0) {
			fetchData();
		}
		mPullRefreshListView.setState(State.RELEASE_TO_REFRESH, true);
		actualListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				FootblogManager.getInstance().setCurrentBlog(
						mListItems.get(position - 1));
				BaseFragment.getWMFragmentManager().showFragment(
						WMFragmentManager.TYPE_FOOTBLOG_COMMENT);
			}
		});
		return contentView;
	}

	Handler handler = new Handler() {
	};

	/**
	 * 获取足迹数据，本来是直接获取的，4月29日，放到线程里面，暂时没发现问题中，之后要优化足迹页加载流畅度
	 */
	public void fetchData() {
		new Thread(new Runnable() {
			public void run() {
				BaseFragment.getMainActivity().getHandler()
						.post(new Runnable() {
							public void run() {
								setState(LOADING);
							}
						});
				BmobQuery<Blog> query = new BmobQuery<Blog>();
				query.order("-createdAt");
				// query.setCachePolicy(CachePolicy.NETWORK_ONLY);
				query.setLimit(WMapConstants.NUMBERS_PER_PAGE);
				// TODO 只看好友和自己，好友数量足够多，可能会有问题，超过1024k请求限制
				// Map<String, BmobChatUser> friendsMap = new HashMap<String,
				// BmobChatUser>(
				// AccountUserManager.getInstance().getContactList());
				// friendsMap.put(AccountUserManager.getInstance().getCurrentUserid(),
				// AccountUserManager.getInstance().getCurrentUser());
				// Set<String> friends = friendsMap.keySet();
				// query.addWhereContainedIn("author", friends);
				// 复杂查询http://docs.bmob.cn/android/developdoc/index.html?menukey=develop_doc&key=develop_android#index_%E5%E2pn
				BmobQuery<User> innerFriendsQuery = new BmobQuery<User>();
				innerFriendsQuery.addWhereRelatedTo("contacts",
						new BmobPointer(AccountUserManager.getInstance()
								.getCurrentUser()));
				BmobQuery<User> innerMyselfQuery = new BmobQuery<User>();
				innerMyselfQuery.addWhereEqualTo("objectId", AccountUserManager
						.getInstance().getCurrentUserid());
				List<BmobQuery<User>> queries = new ArrayList<BmobQuery<User>>();
				queries.add(innerFriendsQuery);
				queries.add(innerMyselfQuery);
				BmobQuery<User> innerQuery = new BmobQuery<User>();
				innerQuery.or(queries);
				// innerQuery.
				query.addWhereMatchesQuery("author", "_User", innerQuery);
				BmobDate date = new BmobDate(new Date(System
						.currentTimeMillis()));
				query.addWhereLessThan("createdAt", date);
				L.i(TAG, "SIZE:" + WMapConstants.NUMBERS_PER_PAGE * pageNum);
				query.setSkip(WMapConstants.NUMBERS_PER_PAGE * (pageNum++));
				L.i(TAG, "SIZE:" + WMapConstants.NUMBERS_PER_PAGE * pageNum);
				query.include("author");
				query.findObjects(getActivity(), new FindListener<Blog>() {

					@Override
					public void onSuccess(List<Blog> list) {
						L.d(WModel.PublishBlog, "find success." + list.size());
						if (list.size() != 0
								&& list.get(list.size() - 1) != null) {
							if (mRefreshType == RefreshType.REFRESH) {
								mListItems.clear();
							}
							if (list.size() < WMapConstants.NUMBERS_PER_PAGE) {
								L.i(TAG, "已加载完所有数据~");
							}
							if (AccountUserManager.getInstance()
									.getCurrentUser() != null) {
								list = DatabaseUtil.getInstance(mContext)
										.setFav(list);
							}
							mListItems.addAll(list);
							BaseFragment.getMainActivity().getHandler()
									.postDelayed(new Runnable() {
										public void run() {
											mAdapter.notifyDataSetChanged();
											mPullRefreshListView
													.onRefreshComplete();
											setState(LOADING_COMPLETED);
										}
									}, 100);
						} else {
							T.showShort(getActivity(), "暂无更多数据~");
							pageNum--;
							BaseFragment.getMainActivity().getHandler()
									.post(new Runnable() {
										public void run() {
											mPullRefreshListView
													.onRefreshComplete();
											setState(LOADING_COMPLETED);
										}
									});
						}
					}

					@Override
					public void onError(int arg0, String arg1) {
						L.d(WModel.PublishBlog, arg0 + "find failed." + arg1);
						pageNum--;
						BaseFragment.getMainActivity().getHandler()
								.post(new Runnable() {
									public void run() {
										mPullRefreshListView
												.onRefreshComplete();
										setState(LOADING_FAILED);
									}
								});
					}
				});
			}
		}).start();
	}

	private static final int LOADING = 1;
	private static final int LOADING_COMPLETED = 2;
	private static final int LOADING_FAILED = 3;
	private static final int NORMAL = 4;

	public void setState(int state) {
		switch (state) {
		case LOADING:
			if (mListItems.size() == 0) {
				mPullRefreshListView.setVisibility(View.GONE);
				progressbar.setVisibility(View.VISIBLE);
			}

			break;
		case LOADING_COMPLETED:
			progressbar.setVisibility(View.GONE);
			mPullRefreshListView.setVisibility(View.VISIBLE);
			mPullRefreshListView.setMode(Mode.BOTH);

			break;
		case LOADING_FAILED:
			if (mListItems.size() == 0) {
				mPullRefreshListView.setVisibility(View.VISIBLE);
				mPullRefreshListView.setMode(Mode.PULL_FROM_START);
				T.showLong(mContext, "请检查网络连接");
			}
			progressbar.setVisibility(View.GONE);
			break;
		case NORMAL:

			break;
		default:
			break;
		}
	}

}
