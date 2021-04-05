/*
 * Copyright (c) 2017. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.fcm;

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
import rs.highlande.app.diplomatici.connection.HLRequestTracker;
import rs.highlande.app.diplomatici.connection.HLServerCalls;
import rs.highlande.app.diplomatici.connection.OnMissingConnectionListener;
import rs.highlande.app.diplomatici.connection.OnServerMessageReceivedListenerWithIdOperation;
import rs.highlande.app.diplomatici.connection.ServerMessageReceiver;
import rs.highlande.app.diplomatici.models.HLUser;
import rs.highlande.app.diplomatici.services.SubscribeToSocketService;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.LogUtils;
import rs.highlande.app.diplomatici.utilities.RealmUtils;

/**
 * {@link Service} subclass whose duty is to notify server that a provided notification has been opened.
 *
 * @author mbaldrighi on 9/25/2019.
 */
public class SendNotificationOpenService extends Service implements OnServerMessageReceivedListenerWithIdOperation,
        OnMissingConnectionListener {

    public static final String LOG_TAG = SubscribeToSocketService.class.getCanonicalName();

    private ServerMessageReceiver receiver;

    private String idOperation = null;


    public static void startService(Context context, String notifId) {
        LogUtils.d(LOG_TAG, "NOTIFICATION OPEN: startService()");
        try {
            context.startService(new Intent(context, SendNotificationOpenService.class) {{ putExtra(Constants.KEY_NOTIFICATION_ID, notifId); }});
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
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(Constants.BROADCAST_SERVER_RESPONSE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (receiver == null)
            receiver = new ServerMessageReceiver();
        receiver.setListener(this);
//		registerReceiver(receiver, new IntentFilter(Constants.BROADCAST_SOCKET_SUBSCRIPTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(Constants.BROADCAST_SERVER_RESPONSE));

        String notifId = null;
        if (intent != null && intent.hasExtra(Constants.KEY_NOTIFICATION_ID))
            notifId = intent.getStringExtra(Constants.KEY_NOTIFICATION_ID);

        Realm realm = null;
        try {
            realm = RealmUtils.getCheckedRealm();
            HLUser user = new HLUser().readUser(realm);
            if (user != null && user.isValid()) {
                String userId = user.getId();
                callServer(userId, notifId);
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
        LogUtils.d(LOG_TAG, "NOTIFICATION OPEN - STOPPING SERVICE");

        try {
//			unregisterReceiver(receiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            LogUtils.d(LOG_TAG, e.getMessage());
        }

        super.onDestroy();
    }

    private void callServer(@NonNull String id, String notifId) {
        Object[] results;
        try {
            results = HLServerCalls.setNotificationOpened(id, notifId);

            if (results.length == 3)
                idOperation = (String) results[2];

            LogUtils.d(LOG_TAG, "NOTIFICATION OPEN - idOperation: " + idOperation);

            HLRequestTracker.getInstance(((DiplomaticiApp) getApplication())).handleCallResult(this, this, results, false, false);
        } catch (JSONException e) {
            LogUtils.d(LOG_TAG, "NOTIFICATION OPEN: EXCEPTION");
            e.printStackTrace();
            stopSelf();
        }

    }

    //region == Receiver Callback ==


    @Override
    public void handleSuccessResponse(String operationUUID, int operationId, JSONArray responseObject) {
        switch (operationId) {
            case Constants.SERVER_OP_SET_NOTIF_OPENED:
                if (Objects.equals(idOperation, operationUUID)) {

                    LogUtils.d(LOG_TAG, "NOTIFICATION OPEN: SUCCESS");
                    stopSelf();
                }
                break;
        }
    }

    @Override
    public void handleSuccessResponse(int operationId, JSONArray responseObject) {}

    @Override
    public void handleErrorResponse(int operationId, int errorCode) {

        switch (operationId) {
            case Constants.SERVER_OP_SET_NOTIF_OPENED:
                LogUtils.e(LOG_TAG, "NOTIFICATION OPEN: FAILED");
                stopSelf();
                break;
        }
    }

    @Override
    public void onMissingConnection(int operationId) {
        LogUtils.e(LOG_TAG, "NOTIFICATION OPEN: NO CONNECTION");
    }

    //endregion

}
