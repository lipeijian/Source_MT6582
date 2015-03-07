/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.nfc.handover;

import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.util.ArrayList;
import java.util.Arrays;

public class BluetoothOppHandover implements Handler.Callback {
    static final String TAG = "BluetoothOppHandover";
    static final boolean DBG = true;

    static final int STATE_INIT = 0;
    static final int STATE_TURNING_ON = 1;
    static final int STATE_WAITING = 2; // Need to wait for remote side turning on BT
    static final int STATE_COMPLETE = 3;

    static final int MSG_START_SEND = 0;

    static final int REMOTE_BT_ENABLE_DELAY_MS = 5000;

    static final String ACTION_HANDOVER_SEND =
            "android.btopp.intent.action.HANDOVER_SEND";

    static final String ACTION_HANDOVER_SEND_MULTIPLE =
            "android.btopp.intent.action.HANDOVER_SEND_MULTIPLE";

    final Context mContext;
    final BluetoothDevice mDevice;

    final Uri[] mUris;
    final boolean mRemoteActivating;
    final Handler mHandler;
    final Long mCreateTime;

    int mState;

    public BluetoothOppHandover(Context context, BluetoothDevice device, Uri[] uris,
            boolean remoteActivating) {
        mContext = context;
        mDevice = device;
        mUris = uris;
        mRemoteActivating = remoteActivating;
        mCreateTime = SystemClock.elapsedRealtime();

        mHandler = new Handler(context.getMainLooper(),this);
        mState = STATE_INIT;
    }

    public static String getMimeTypeForUri(Context context, Uri uri)  {
        if (uri.getScheme() == null) return null;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            return cr.getType(uri);
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.getPath()).toLowerCase();
            if (extension != null) {
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            } else {
                return null;
            }
        } else {
            Log.d(TAG, "Could not determine mime type for Uri " + uri);
            return null;
        }
    }

    /**
     * Main entry point. This method is usually called after construction,
     * to begin the BT sequence. Must be called on Main thread.
     */
    public void start() {
        if (mRemoteActivating) {
            Long timeElapsed = SystemClock.elapsedRealtime() - mCreateTime;
            if (timeElapsed < REMOTE_BT_ENABLE_DELAY_MS) {
                mHandler.sendEmptyMessageDelayed(MSG_START_SEND,
                        REMOTE_BT_ENABLE_DELAY_MS - timeElapsed);
            } else {
                // Already waited long enough for BT to come up
                // - start send.
                sendIntent();
            }
        } else {
            // Remote BT enabled already, start send immediately
            sendIntent();
        }
    }

    void complete() {
        mState = STATE_COMPLETE;
    }

    void sendIntent() {
        Intent intent = new Intent();
        intent.setPackage("com.android.bluetooth");
        String mimeType = getMimeTypeForUri(mContext, mUris[0]);
        intent.setType(mimeType);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDevice);
        if (mUris.length == 1) {
            intent.setAction(ACTION_HANDOVER_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, mUris[0]);
        } else {
            ArrayList<Uri> uris = new ArrayList<Uri>(Arrays.asList(mUris));
            intent.setAction(ACTION_HANDOVER_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        if (DBG) Log.d(TAG, "Handing off outging transfer to BT");
        mContext.sendBroadcast(intent);

        complete();
    }


    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG_START_SEND) {
            sendIntent();
            return true;
        }
        return false;
    }
}
