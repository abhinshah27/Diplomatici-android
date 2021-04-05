/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;

import io.realm.Realm;
import rs.highlande.app.diplomatici.base.DiplomaticiApp;
import rs.highlande.app.diplomatici.connection.HLServerCalls;
import rs.highlande.app.diplomatici.connection.HLServerCallsChat;
import rs.highlande.app.diplomatici.connection.OnServerMessageReceivedListenerWithIdOperation;
import rs.highlande.app.diplomatici.connection.ServerMessageReceiver;
import rs.highlande.app.diplomatici.models.HLUser;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.LogUtils;
import rs.highlande.app.diplomatici.utilities.RealmUtils;
import rs.highlande.app.diplomatici.utilities.Utils;

/**
 * {@link Service} subclass whose duty is to subscribe client to Real-Time communication with socket.
 *
 * @author mbaldrighi on 11/01/2017.
 */
public class SubscribeToSocketServiceChat extends Service implements OnServerMessageReceivedListenerWithIdOperation {

    public static final String LOG_TAG = SubscribeToSocketServiceChat.class.getCanonicalName();

    private ServerMessageReceiver receiver;

    private String userId = null;
    private String idOperation = null;


    public static void startService(Context context) {
        LogUtils.d(LOG_TAG, "SUBSCRIPTION to socket CHAT: startService()");
        try {
            context.startService(new Intent(context, SubscribeToSocketServiceChat.class));
        } catch (IllegalStateException e) {
            LogUtils.e(LOG_TAG, "Cannot start background service: " + e.getMessage(), e);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (receiver == null)
            receiver = new ServerMessageReceiver();
        receiver.setListener(this);
//		registerReceiver(receiver, new IntentFilter(Constants.BROADCAST_SOCKET_SUBSCRIPTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(Constants.BROADCAST_SOCKET_SUBSCRIPTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (receiver == null)
            receiver = new ServerMessageReceiver();
        receiver.setListener(this);
//		registerReceiver(receiver, new IntentFilter(Constants.BROADCAST_SOCKET_SUBSCRIPTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(Constants.BROADCAST_SOCKET_SUBSCRIPTION));

        Realm realm = null;
        try {
            realm = RealmUtils.getCheckedRealm();
            HLUser user = new HLUser().readUser(realm);
            if (user != null && user.isValid() && !user.isActingAsInterest()) {
                userId = user.getId();
                callSubscription(userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        } finally {
            RealmUtils.closeRealm(realm);
        }

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        try {
//			unregisterReceiver(receiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            LogUtils.d(LOG_TAG, e.getMessage());
        }

        super.onDestroy();
    }

    private void callSubscription(@NonNull String id) {
        Object[] results = HLServerCalls.subscribeToSocket(this, id, true);

        if (results.length == 3)
            idOperation = (String) results[2];

        LogUtils.d(LOG_TAG, "SUBSCRIPTION to socket CHAT - idOperation: " + idOperation);

        if (!((boolean) results[0])) {
            DiplomaticiApp.subscribedToSocketChat = false;
            stopSelf();
        }
    }

    private void callSetUserOnline(@NonNull String id) {
        Object[] results = null;
        try {
            results = HLServerCallsChat.setUserOnline(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (results != null && !((boolean) results[0])) {
            stopSelf();
        }
    }

    //region == Receiver Callback ==


    @Override
    public void handleSuccessResponse(String operationUUID, int operationId, JSONArray responseObject) {
        switch (operationId) {
            case Constants.SERVER_OP_SOCKET_SUBSCR:
                if (Objects.equals(idOperation, operationUUID)) {
                    DiplomaticiApp.subscribedToSocketChat = true;

                    LogUtils.d(LOG_TAG, "SUBSCRIPTION to socket CHAT SUCCESS");

                    if (Utils.isStringValid(userId)) callSetUserOnline(userId);
                }
                break;

            case Constants.SERVER_OP_CHAT_SET_USER_ONLINE:
                LogUtils.d(LOG_TAG, "CHAT SET USER ONLINE SUCCESS");
                stopSelf();
                break;
        }
    }

    @Override
    public void handleSuccessResponse(int operationId, JSONArray responseObject) {
//        switch (operationId) {
//            case Constants.SERVER_OP_SOCKET_SUBSCR:
//                DiplomaticiApp.subscribedToSocketChat = true;
//
//                LogUtils.i(LOG_TAG, "DiplomaticiApp.subscribedToSocketChat = " + DiplomaticiApp.subscribedToSocketChat);
//                LogUtils.i(LOG_TAG, "DiplomaticiApp.subscribedToSocket = " + DiplomaticiApp.subscribedToSocket);
//
//                LogUtils.d(LOG_TAG, "SUBSCRIPTION to socket CHAT SUCCESS");
//
//                if (Utils.isStringValid(userId)) callSetUserOnline(userId);
//                break;
//
//            case Constants.SERVER_OP_CHAT_SET_USER_ONLINE:
//                LogUtils.d(LOG_TAG, "CHAT SET USER ONLINE SUCCESS");
//                stopSelf();
//                break;
//        }
    }

    @Override
    public void handleErrorResponse(int operationId, int errorCode) {

        DiplomaticiApp.subscribedToSocketChat = false;

        switch (operationId) {
            case Constants.SERVER_OP_SOCKET_SUBSCR:
                LogUtils.e(LOG_TAG, "SUBSCRIPTION to socket CHAT FAILED");
                stopSelf();
                break;

            case Constants.SERVER_OP_CHAT_SET_USER_ONLINE:
                LogUtils.e(LOG_TAG, "CHAT SET USER ONLINE FAILED");
                stopSelf();
                break;
        }
    }

    //endregion

}
