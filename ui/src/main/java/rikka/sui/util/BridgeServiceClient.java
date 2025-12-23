/*
 * This file is part of Sui.
 *
 * Sui is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sui is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Sui.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2021 Sui Contributors
 */

package rikka.sui.util;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;

import androidx.annotation.Nullable;

import java.util.List;

import moe.shizuku.server.IShizukuService;
import rikka.parcelablelist.ParcelableListSlice;
import rikka.sui.model.AppInfo;

public class BridgeServiceClient {

    private static final int BINDER_TRANSACTION_getApplications = 10001;
    private static final int BINDER_TRANSACTION_REQUEST_PINNED_SHORTCUT_FROM_UI = 10005;
    private static final int BINDER_TRANSACTION_BATCH_UPDATE_UNCONFIGURED = 10006;
    private static IBinder binder;
    private static IShizukuService service;

    private static final int BRIDGE_TRANSACTION_CODE = ('_' << 24) | ('S' << 16) | ('U' << 8) | 'I';
    private static final String BRIDGE_SERVICE_DESCRIPTOR = "android.app.IActivityManager";
    private static final String BRIDGE_SERVICE_NAME = "activity";
    private static final int BRIDGE_ACTION_GET_BINDER = 2;

    private static final IBinder.DeathRecipient DEATH_RECIPIENT = () -> {
        binder = null;
        service = null;
    };

    private static IBinder requestBinderFromBridge() {
        final String TAG = "SuiBridgeDebug";

        android.util.Log.d(TAG, "Attempting to request binder from bridge...");

        IBinder binder = ServiceManager.getService(BRIDGE_SERVICE_NAME);
        if (binder == null) {
            android.util.Log.e(TAG, "CRITICAL FAILURE: ServiceManager.getService(\"activity\") returned null!");
            return null;
        }
        android.util.Log.d(TAG, "'activity' service binder obtained. Preparing custom transact...");

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(BRIDGE_SERVICE_DESCRIPTOR);
            data.writeInt(BRIDGE_ACTION_GET_BINDER);

            android.util.Log.d(TAG, "Executing binder.transact with custom code...");
            binder.transact(BRIDGE_TRANSACTION_CODE, data, reply, 0);
            android.util.Log.d(TAG, "Transact call has returned. Reading reply...");

            reply.readException();
            android.util.Log.d(TAG, "readException() completed without throwing an exception.");

            IBinder received = reply.readStrongBinder();
            if (received != null) {
                android.util.Log.i(TAG, "SUCCESS! Received a non-null binder from the bridge.");
                return received;
            } else {
                android.util.Log.w(TAG, "FAILURE: Transact was successful, but the returned binder is NULL. The bridge likely rejected the request.");
            }
        } catch (Throwable e) {
            android.util.Log.e(TAG, "FATAL FAILURE: An exception was thrown during the transact/reply process.", e);
        } finally {
            data.recycle();
            reply.recycle();
        }
        android.util.Log.e(TAG, "requestBinderFromBridge is returning NULL.");
        return null;
    }

    protected static void setBinder(@Nullable IBinder binder) {
        if (BridgeServiceClient.binder == binder) return;

        if (BridgeServiceClient.binder != null) {
            BridgeServiceClient.binder.unlinkToDeath(DEATH_RECIPIENT, 0);
        }

        if (binder == null) {
            BridgeServiceClient.binder = null;
            BridgeServiceClient.service = null;
        } else {
            BridgeServiceClient.binder = binder;
            BridgeServiceClient.service = IShizukuService.Stub.asInterface(binder);

            try {
                BridgeServiceClient.binder.linkToDeath(DEATH_RECIPIENT, 0);
            } catch (Throwable ignored) {
            }
        }
    }

    public static IShizukuService getService() {
        if (service == null) {
            setBinder(requestBinderFromBridge());
        }
        return service;
    }

    public static List<AppInfo> getApplications(int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<AppInfo> result;
        try {
            data.writeInterfaceToken("moe.shizuku.server.IShizukuService");
            data.writeInt(userId);
            try {
                getService().asBinder().transact(BINDER_TRANSACTION_getApplications, data, reply, 0);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            reply.readException();
            if ((0 != reply.readInt())) {
                //noinspection unchecked
                result = ParcelableListSlice.CREATOR.createFromParcel(reply).getList();
            } else {
                result = null;
            }
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    public static void requestPinnedShortcut() throws RemoteException {
        IShizukuService service = getService();
        if (service == null) {
            throw new RemoteException("Sui service is not available.");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("moe.shizuku.server.IShizukuService");
            service.asBinder().transact(BINDER_TRANSACTION_REQUEST_PINNED_SHORTCUT_FROM_UI, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
    public static void batchUpdateUnconfigured(int targetMode) throws RemoteException {
        IShizukuService service = getService();
        if (service == null) {
            throw new RemoteException("Sui service is not available.");
        }

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(rikka.shizuku.ShizukuApiConstants.BINDER_DESCRIPTOR);
            data.writeInt(targetMode);

            service.asBinder().transact(BINDER_TRANSACTION_BATCH_UPDATE_UNCONFIGURED, data, reply, 0);

            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}