/*
 * Copyright (c) 2017. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.notifications;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rs.highlande.app.diplomatici.R;
import rs.highlande.app.diplomatici.base.BasicInteractionListener;
import rs.highlande.app.diplomatici.base.DiplomaticiApp;
import rs.highlande.app.diplomatici.base.HLActivity;
import rs.highlande.app.diplomatici.connection.HLRequestTracker;
import rs.highlande.app.diplomatici.connection.HLServerCalls;
import rs.highlande.app.diplomatici.connection.OnMissingConnectionListener;
import rs.highlande.app.diplomatici.connection.OnServerMessageReceivedListenerWithIdOperation;
import rs.highlande.app.diplomatici.connection.ServerMessageReceiver;
import rs.highlande.app.diplomatici.features.profile.ProfileActivity;
import rs.highlande.app.diplomatici.features.profile.ProfileHelper;
import rs.highlande.app.diplomatici.features.profile.SinglePostActivity;
import rs.highlande.app.diplomatici.features.userDocumentation.LuissDocumentationActivity1List;
import rs.highlande.app.diplomatici.models.HLNotifications;
import rs.highlande.app.diplomatici.models.Notification;
import rs.highlande.app.diplomatici.models.enums.ActionTypeEnum;
import rs.highlande.app.diplomatici.models.enums.NotificationNatureEnum;
import rs.highlande.app.diplomatici.models.enums.NotificationTypeEnum;
import rs.highlande.app.diplomatici.models.enums.RequestsStatusEnum;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.LogUtils;
import rs.highlande.app.diplomatici.utilities.Utils;
import rs.highlande.app.diplomatici.utilities.helpers.LoadMoreResponseHandlerTask;
import rs.highlande.app.diplomatici.utilities.listeners.LoadMoreScrollListener;

/**
 * @author mbaldrighi on 12/13/2017.
 */
public class NotificationAndRequestHelper implements Serializable,
		OnMissingConnectionListener, OnServerMessageReceivedListenerWithIdOperation,
		NotificationsAdapter.OnNotificationItemClickListener, LoadMoreResponseHandlerTask.OnDataLoadedListener,
		View.OnClickListener {

	public static final String LOG_TAG = NotificationAndRequestHelper.class.getCanonicalName();

	private HLActivity activity;

	private NotificationNatureEnum type = NotificationNatureEnum.NOTIFICATION;

	private View tabNotifs, tabRequests;
	private RecyclerView notificationsRv, requestsRv;
	private TextView noNotifs;

	private List<Notification> notificationsList = new ArrayList<>();
	private List<Notification> requestsList = new ArrayList<>();
	private NotificationsAdapter notifsAdapter, requestsAdapter;
	private Integer notificationsPosition = null, requestsPosition = null;

	private OnNotificationHelpListener mListener;

	private ServerMessageReceiver messageReceiver;

	private int newItemsCount;
	private boolean fromLoadMore;

	private SwipeRefreshLayout srl;


	public NotificationAndRequestHelper(HLActivity activity, OnNotificationHelpListener listener) {
		this.activity = activity;
		this.mListener = listener;

		messageReceiver = new ServerMessageReceiver();
		messageReceiver.setListener(this);
//		activity.registerReceiver(messageReceiver, new IntentFilter(Constants.BROADCAST_SERVER_RESPONSE));
		LocalBroadcastManager.getInstance(activity).registerReceiver(messageReceiver, new IntentFilter(Constants.BROADCAST_SERVER_RESPONSE));

		notifsAdapter = new NotificationsAdapter(notificationsList);
		notifsAdapter.setListener(this);
		notifsAdapter.setHasStableIds(true);

		requestsAdapter = new NotificationsAdapter(requestsList);
		requestsAdapter.setListener(this);
		requestsAdapter.setHasStableIds(true);
	}

	public void configureNotificationLayout(View view) {
		srl = Utils.getGenericSwipeLayout(view, () -> {
			if (srl != null)
				Utils.setRefreshingForSwipeLayout(srl, true);

			updateNotifications(true);
		});

		tabNotifs = view.findViewById(R.id.tab_notification);
		tabNotifs.setOnClickListener(this);
		tabRequests = view.findViewById(R.id.tab_requests);
		tabRequests.setOnClickListener(this);

		noNotifs = view.findViewById(R.id.no_notifications);
		notificationsRv = view.findViewById(R.id.rv_notifications);
		notificationsRv.addOnScrollListener(new LoadMoreScrollListener() {
			@Override
			public void onLoadMore() {

				notificationsPosition = null;

				HLNotifications.getInstance().callForNotifications(activity,
						NotificationAndRequestHelper.this, activity.getUser().getId(), fromLoadMore = true);
			}
		});
		notificationsRv.setItemAnimator(null);

		requestsRv = view.findViewById(R.id.rv_requests);
		requestsRv.addOnScrollListener(new LoadMoreScrollListener() {
			@Override
			public void onLoadMore() {

				requestsPosition = null;

				HLNotifications.getInstance().callForNotifRequests(activity,
						NotificationAndRequestHelper.this, activity.getUser().getId(), fromLoadMore = true);
			}
		});
		requestsRv.setItemAnimator(null);

		tabNotifs.performClick();
	}

	public void onResume() {
		if (messageReceiver == null)
			messageReceiver = new ServerMessageReceiver();
		messageReceiver.setListener(this);

//		activity.registerReceiver(messageReceiver, new IntentFilter(Constants.BROADCAST_SERVER_RESPONSE));
		LocalBroadcastManager.getInstance(activity).registerReceiver(messageReceiver, new IntentFilter(Constants.BROADCAST_SERVER_RESPONSE));

		notificationsRv.getRecycledViewPool().clear();
		notificationsRv.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.VERTICAL, false));
		notificationsRv.setAdapter(notifsAdapter);

		requestsRv.getRecycledViewPool().clear();
		requestsRv.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.VERTICAL, false));
		requestsRv.setAdapter(requestsAdapter);

		updateNotifications(true);
		setData(false);
	}

	public void onPause() {
		if (notificationsRv != null && notificationsRv.getLayoutManager() != null)
			notificationsPosition = ((LinearLayoutManager) notificationsRv.getLayoutManager()).findFirstVisibleItemPosition();
		if (requestsRv != null && requestsRv.getLayoutManager() != null)
			requestsPosition = ((LinearLayoutManager) requestsRv.getLayoutManager()).findFirstVisibleItemPosition();

		try {
//			activity.unregisterReceiver(messageReceiver);
			LocalBroadcastManager.getInstance(activity).unregisterReceiver(messageReceiver);
		} catch (IllegalArgumentException e) {
			LogUtils.e(LOG_TAG, e.getMessage(), e);
		}
	}

	public void setData(boolean background) {
		HLNotifications notifications = HLNotifications.getInstance();
		if (type == NotificationNatureEnum.NOTIFICATION) {
			if (notificationsList == null) {
				notificationsList = new ArrayList<>();
				notifsAdapter = new NotificationsAdapter(notificationsList);
			}
			notificationsList.clear();

			notificationsList.addAll(notifications.getSortedNotifications());
			if (notifsAdapter != null) {
				if (!background) {
					notificationsRv.getRecycledViewPool().clear();
					notifsAdapter.notifyDataSetChanged();
				}

				// INFO: 2/26/19    if background on, rv updates are already called from LoadMoreResponseHandlerTask
//				else if (notificationsRv != null) {
//					notificationsRv.post(() -> {
//						notificationsRv.getRecycledViewPool().clear();
//						notifsAdapter.notifyDataSetChanged();
//					});
//				}
			}
		}
		else if (type == NotificationNatureEnum.REQUEST) {
			if (requestsList == null) {
				requestsList = new ArrayList<>();
				requestsAdapter = new NotificationsAdapter(requestsList);
			}
			requestsList.clear();

			requestsList.addAll(notifications.getSortedRequests());
			if (requestsAdapter != null) {
				if (!background) {
					requestsRv.getRecycledViewPool().clear();
					requestsAdapter.notifyDataSetChanged();
				}

				// INFO: 2/26/19    if background on, rv updates are already called from LoadMoreResponseHandlerTask
//				else if (requestsRv != null) {
//					requestsRv.post(() -> {
//						requestsRv.getRecycledViewPool().clear();
//						requestsAdapter.notifyDataSetChanged();
//					});
//				}
			}
		}

		final boolean empty = (type == NotificationNatureEnum.NOTIFICATION) ?
				notificationsList.isEmpty() : requestsList.isEmpty();

		if (background) {
			activity.runOnUiThread(() -> {
				if (!DiplomaticiApp.profileFragmentVisible)
					handleDotVisibility(null);
				else
					handleDotVisibility(false);
				handleListVisibility(type, empty);
				handleTabSelection(type);
			});
		} else {
			if (!DiplomaticiApp.profileFragmentVisible)
				handleDotVisibility(null);
			else
				handleDotVisibility(false);
			handleListVisibility(type, empty);
			handleTabSelection(type);

			if (notificationsPosition != null && notificationsPosition > -1 && notificationsRv.getLayoutManager() != null)
				notificationsRv.getLayoutManager().scrollToPosition(notificationsPosition);
			if (requestsPosition != null && requestsPosition > -1 && requestsRv.getLayoutManager() != null)
				requestsRv.getLayoutManager().scrollToPosition(requestsPosition);
		}
	}

	public void handleDotVisibility(Boolean show) {
		if (mListener.getBottomBarNotificationDot() != null) {
			if (show == null)
				show = HLNotifications.getInstance().getUnreadCount(true) > 0;
			mListener.getBottomBarNotificationDot().setVisibility(
					show && activity.getUser().isValid() && !DiplomaticiApp.profileFragmentVisible ?
							View.VISIBLE : View.GONE
			);
		}
	}


	public static void handleDotVisibility(View dot, boolean validUser) {
		if (dot != null) {
			dot.setVisibility((HLNotifications.getInstance().getUnreadCount(true) > 0  && !DiplomaticiApp.profileFragmentVisible && validUser) ? View.VISIBLE : View.GONE);
		}
	}

	private void handleListVisibility(NotificationNatureEnum type, boolean empty) {
		if (notificationsRv != null && requestsRv != null && noNotifs != null) {
			noNotifs.setText(type == NotificationNatureEnum.NOTIFICATION ? R.string.no_notifications : R.string.no_requests);
			notificationsRv.setVisibility((type == NotificationNatureEnum.REQUEST || empty) ? View.GONE : View.VISIBLE);
			requestsRv.setVisibility((type == NotificationNatureEnum.NOTIFICATION || empty) ? View.GONE : View.VISIBLE);
			noNotifs.setVisibility(empty ? View.VISIBLE : View.GONE);

//			handleDotVisibility(mListener.getBottomBarNotificationDot(), false);
		}
	}

	private void handleTabSelection(NotificationNatureEnum type) {
		if (tabNotifs != null && tabRequests != null) {
			tabNotifs.setSelected(type == NotificationNatureEnum.NOTIFICATION);
			tabRequests.setSelected(type == NotificationNatureEnum.REQUEST);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.tab_notification:
				if (type == NotificationNatureEnum.REQUEST) {
					type = NotificationNatureEnum.NOTIFICATION;
					setData(false);
				}
				break;
			case R.id.tab_requests:
				if (type == NotificationNatureEnum.NOTIFICATION) {
					type = NotificationNatureEnum.REQUEST;
					setData(false);
				}
				break;
		}
	}

	@Override
	public void handleSuccessResponse(String operationUUID, int operationId, JSONArray responseObject) {

		Utils.setRefreshingForSwipeLayout(srl, false);

		switch (operationId) {
			case Constants.SERVER_OP_GET_NOTIFICATIONS:

				int unread = responseObject.optJSONObject(0).optInt("toRead");

				if (mListener.getBottomBarNotificationDot() != null)
					mListener.getBottomBarNotificationDot().setVisibility(unread > 0 ? View.VISIBLE : View.GONE);

				newItemsCount = responseObject.optJSONObject(0).optJSONArray("notifications").length();

				HLNotifications notifs = HLNotifications.getInstance();
				boolean isRequestCall = notifs.isAllRequests(operationUUID);

				new LoadMoreResponseHandlerTask(this,
						isRequestCall ? LoadMoreResponseHandlerTask.Type.REQUESTS :
								LoadMoreResponseHandlerTask.Type.NOTIFICATIONS,
						null, null).execute(responseObject);

				HLNotifications.getInstance().removeRequestUUID(operationUUID);

				if (!fromLoadMore) {
					notificationsPosition = null;
					requestsPosition = null;
				}


//				boolean isRequestCall = false;
//				try {
//					int unread = responseObject.optJSONObject(0).optInt("toRead");
//
//					if (mListener.getBottomBarNotificationDot() != null)
//						mListener.getBottomBarNotificationDot().setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
//
//					newItemsCount = responseObject.optJSONObject(0).optJSONArray("notifications").length();
//
//					HLNotifications notifs = HLNotifications.getInstance();
//					isRequestCall = notifs.isAllRequests(operationUUID);
//					if (isRequestCall)
//						notifs.setRequests(responseObject);
//					else
//						notifs.setNotifications(responseObject);
//
//				} catch (JSONException e) {
//					LogUtils.e(LOG_TAG, e.getMessage(), e);
//				}
//
//				if (fromLoadMore) {
//					new LoadMoreResponseHandlerTask(this,
//							isRequestCall ? LoadMoreResponseHandlerTask.Type.REQUESTS :
//									LoadMoreResponseHandlerTask.Type.NOTIFICATIONS,
//							null, null).execute(responseObject);
//
//					HLNotifications.getInstance().removeRequestUUID(operationUUID);
//
////					fromLoadMore = false;
//				}
//				else {
//					setData(false);
//					notificationsPosition = null;
//					requestsPosition = null;
//				}
				break;
		}
	}

	@Override
	public void handleSuccessResponse(int operationId, JSONArray responseObject) {

		Utils.setRefreshingForSwipeLayout(srl, false);

		if (selNotification == null) {
			LogUtils.e(LOG_TAG, "Wrong Helper instance");
			return;
		}

		switch (operationId) {
			case Constants.SERVER_OP_DO_ACTION:
				RequestsStatusEnum status = RequestsStatusEnum.AUTHORIZED;
				if (actionType == ActionTypeEnum.DENY)
					status = RequestsStatusEnum.DECLINED;
				selNotification.setStatus(status);

				setNotificationReadPlusOperations(true);
				break;

			case Constants.SERVER_OP_SET_NOTIFICATION_READ:
				setNotificationReadPlusOperations(false);
				break;

			case Constants.SERVER_OP_AUTHORIZE_TO_CIRCLE:
			case Constants.SERVER_OP_AUTHORIZE_LEGACY_CONTACT:
				HLServerCalls.doActionOnNotification(activity, selNotification.getId(), actionType, this);
				break;

			case Constants.SERVER_OP_GET_NOTIFICATION_COUNT:
				handleDotVisibility(true);
				break;
		}
	}

	@Override
	public void handleErrorResponse(int operationId, int errorCode) {

		Utils.setRefreshingForSwipeLayout(srl, false);
		notificationsPosition = null;
		requestsPosition = null;

		String s;
		switch (operationId) {
			case Constants.SERVER_OP_GET_NOTIFICATIONS:
				s = "Get notifications FAILED with code: ";
				activity.showAlert(R.string.error_generic_list);
				LogUtils.e(LOG_TAG, s + errorCode);
				break;
			case Constants.SERVER_OP_DO_ACTION:
				s = "Do action FAILED with code: ";
				activity.showGenericError();
				selButton.setSelected(false);
				LogUtils.e(LOG_TAG, s + errorCode);
				break;

			case Constants.SERVER_OP_SET_NOTIFICATION_READ:
				s = "Set notification read FAILED with code: ";
				if (Utils.isStringValid(selNotification.getPostId())) {
					goToSinglePost(selNotification.getPostId());
				}
				LogUtils.e(LOG_TAG, s + errorCode);
				break;
		}
	}

	@Override
	public void onMissingConnection(int operationId) {
		Utils.setRefreshingForSwipeLayout(srl, false);
		notificationsPosition = null;
		requestsPosition = null;
	}


	public interface OnNotificationHelpListener {
		View getBottomBarNotificationDot();
		String getUserId();
	}

	@Override
	public void onNotificationItemClick(@NonNull Notification notification) {
		selNotification = notification;

		if (!notification.isRead()) {
			setNotificationAsRead(notification);
		}
		else {
			if ((notification.isRequest() && notification.getType() != NotificationTypeEnum.TAG) ||
					notification.getType() == NotificationTypeEnum.ADD_TO_CIRCLE_AUTHORIZED) {
				String userId = notification.getUserId();

				if (activity instanceof ProfileActivity)
					((ProfileActivity) activity).showProfileCardFragment(ProfileHelper.ProfileType.NOT_FRIEND, userId);
			}

//			else if (Utils.isStringValid(selNotification.getDocUrl())) {
//				goToUserDocumentation(selNotification.getDocUrl());
//			}

			else if (Utils.isStringValid(selNotification.getPostId())) {
				goToSinglePost(selNotification.getPostId());
			}
			else getActivityListener().showGenericError();
		}
	}

	private void setNotificationAsRead(Notification notification) {
		selNotification = notification;

		Object[] result = null;
		try {
			result = HLServerCalls.setNotificationAsRead(activity.getUser().getId(), notification.getId());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		HLRequestTracker.getInstance((DiplomaticiApp) activity.getApplication()).handleCallResult(this, activity, result);
	}

	public void updateNotifications(boolean force) {
		if (mListener != null && Utils.isStringValid(mListener.getUserId()) && (force || (DiplomaticiApp.profileFragmentVisible/* && mListener.getProfileType() == ProfileHelper.ProfileType.ME*/))) {
			HLNotifications.getInstance().callForNotifications(activity, this, mListener.getUserId(), false);
//			HLNotifications.getInstance().callForNotifRequests(activity, this, mListener.getUserId(), false);
		}
	}

	private void goToSinglePost(@NonNull String postId) {
		Intent intent = new Intent(activity, SinglePostActivity.class);
		intent.putExtra(Constants.EXTRA_PARAM_1, postId);
		activity.startActivityForResult(intent, Constants.RESULT_SINGLE_POST);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation);
	}

	private void goToUserDocumentation(@NonNull String docUrl) {
		Intent intent = new Intent(activity, LuissDocumentationActivity1List.class);
		intent.putExtra(Constants.EXTRA_PARAM_1, docUrl);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation);
	}

	private void setNotificationReadPlusOperations(boolean fromActionOnRequest) {
		if (selNotification != null) {
			selNotification.setRead(true);
			HLNotifications.getInstance().updateUnreadCount();

			LogUtils.d(LOG_TAG, "Unread count >>> with int: " + HLNotifications.getInstance().getUnreadCount(true) + ", no int: " + HLNotifications.getInstance().getUnreadCount(false));

			notificationsRv.getRecycledViewPool().clear();
			notifsAdapter.notifyDataSetChanged();
			if (selNotification.isRequest()) {
				requestsRv.getRecycledViewPool().clear();
				requestsAdapter.notifyDataSetChanged();
			}

			int unread = HLNotifications.getInstance().getUnreadCount(false);
			if (mListener.getBottomBarNotificationDot() != null) {
				mListener.getBottomBarNotificationDot()
						.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
			}

			if (!fromActionOnRequest) {
				onNotificationItemClick(selNotification);

//				new Handler().postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						if (Utils.isStringValid(selNotification.getPostId()))
//							goToSinglePost(selNotification.getPostId());
//					}
//				}, 500);
			}
		}
	}

	private ActionTypeEnum actionType;
	private Notification selNotification;
	private View selButton;
	@Override
	public void allowDenyRequest(@NonNull Notification notification, @NonNull ActionTypeEnum type,
	                             @NonNull View view) {
		actionType = type;
		selNotification = notification;
		selButton = view;

		if (notification.getType() == NotificationTypeEnum.ADD_TO_CIRCLE ||
				notification.getType() == NotificationTypeEnum.LEGACY_CONTACT ||
				notification.getType() == NotificationTypeEnum.FAMILY_RELATIONSHIP ||
				notification.getType() == NotificationTypeEnum.TAG) {
			Object[] result = null;
			try {
				result = HLServerCalls.authorizeFromNotification(
						activity.getUser(),
						notification,
						ActionTypeEnum.convertEnumToBoolean(type)
				);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			HLRequestTracker.getInstance(((DiplomaticiApp) activity.getApplication())).handleCallResult(this, activity, result);
			return;
		}

		HLServerCalls.doActionOnNotification(activity, notification.getId(), type, this);
	}



	/* LOAD MORE INTERFACE */
	@Override
	public BasicInteractionListener getActivityListener() {
		if (activity instanceof BasicInteractionListener)
			return (BasicInteractionListener) activity;
		return  null;
	}

	@Override
	public RecyclerView.Adapter getAdapter() {
		if (type == NotificationNatureEnum.REQUEST)
			return requestsAdapter;

		return notifsAdapter;
	}

	@Override
	public void setData(Realm realm) {
		setData(true);
	}

	@Override
	public void setData(JSONArray array) {}

	@Override
	public boolean isFromLoadMore() {
		return fromLoadMore;
	}

	@Override
	public void resetFromLoadMore() {
		fromLoadMore = false;
	}


	@Override
	public int getLastPageId() {
		if (type == NotificationNatureEnum.NOTIFICATION)
			return HLNotifications.getInstance().getLastPageId();
		else if (type == NotificationNatureEnum.REQUEST)
			return HLNotifications.getInstance().getLastPageIdRequests();

		return 0;
	}

	@Override
	public int getNewItemsCount() {
		return newItemsCount;
	}


	//region == Getters and setters ==

	//endregion

}
