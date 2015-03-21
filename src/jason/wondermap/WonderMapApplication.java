package jason.wondermap;

import jason.wondermap.dao.MessageDB;
import jason.wondermap.dao.UserDB;
import jason.wondermap.server.BaiduPush;
import jason.wondermap.utils.L;
import jason.wondermap.utils.SharePreferenceUtil;
import android.app.Notification;
import android.app.NotificationManager;

import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaApplication;
import com.baidu.mapapi.SDKInitializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 应用上下文
 * 
 * @author liuzhenhui
 * 
 */
public class WonderMapApplication extends FrontiaApplication {

	/**
	 * API_KEY 应用标识，终端上的绑定和服务端推送消息时都要用到
	 */
	public final static String API_KEY = "784FwY2gl6Wh4tIyWg639AGl";
	/**
	 * SECRET_KEY 应用私钥，服务端推送消息时用到。
	 */
	public final static String SECRIT_KEY = "F28MrehVdbAj4oXiBCCIoFSPth92i1m4";
	/**
	 * sharedPreference存储名称
	 */
	public static final String SP_FILE_NAME = "push_msg_sp";
	private static WonderMapApplication mApplication;

	/**
	 * 模拟服务器推送
	 */
	private BaiduPush mBaiduPushServer;

	/**
	 * 本地存储工具
	 */
	private SharePreferenceUtil mSpUtil;

	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private Gson mGson;

	/**
	 * 用户信息增删改查
	 */
	private UserDB userDB;
	/**
	 * 消息增删改查
	 */
	private MessageDB messageDB;

	@Override
	public void onCreate() {
		super.onCreate();
		mApplication = this;
		initData();
	}

	// ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝内部实现＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
	private void initData() {
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		SDKInitializer.initialize(this);
		boolean isInit = Frontia.init(this, API_KEY);
		if (!isInit) {
			L.e("Frontia初始化失败");
		}
		mBaiduPushServer = new BaiduPush(BaiduPush.HTTP_METHOD_POST,
				SECRIT_KEY, API_KEY);
		// 不转换没有 @Expose 注解的字段
		mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		mSpUtil = new SharePreferenceUtil(this, SP_FILE_NAME);
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		userDB = new UserDB(this);
		messageDB = new MessageDB(this);
	}

	// ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝对外接口＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
	/**
	 * 获取应用的全局上下文
	 * 
	 * @return
	 */
	public synchronized static WonderMapApplication getInstance() {
		return mApplication;
	}

	/**
	 * 获取百度推送服务器
	 * 
	 * @return
	 */
	public synchronized BaiduPush getBaiduPush() {
		if (mBaiduPushServer == null)
			mBaiduPushServer = new BaiduPush(BaiduPush.HTTP_METHOD_POST,
					SECRIT_KEY, API_KEY);
		return mBaiduPushServer;
	}

	/**
	 * 获取json转换工具
	 * 
	 * @return
	 */
	public synchronized Gson getGson() {
		if (mGson == null)
			// 不转换没有 @Expose 注解的字段
			mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
					.create();
		return mGson;
	}

	/**
	 * 获取通知管理工具
	 * 
	 * @return
	 */
	public NotificationManager getNotificationManager() {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}

	/**
	 * 获取本地存储帮助类
	 * 
	 * @return
	 */
	public synchronized SharePreferenceUtil getSpUtil() {
		if (mSpUtil == null)
			mSpUtil = new SharePreferenceUtil(this, SP_FILE_NAME);
		return mSpUtil;
	}

	/**
	 * 获取消息数据存储帮助
	 * 
	 * @return
	 */
	public MessageDB getMessageDB() {
		return messageDB;
	}

	/**
	 * 获取用户数据存储帮助
	 * 
	 * @return
	 */
	public UserDB getUserDB() {
		return userDB;
	}

}
