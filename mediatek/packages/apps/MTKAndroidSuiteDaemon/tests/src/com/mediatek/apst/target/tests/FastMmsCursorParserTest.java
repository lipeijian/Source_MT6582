/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.target.tests;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.mediatek.apst.target.data.provider.message.MmsContent;
import com.mediatek.apst.target.data.proxy.IRawBlockConsumer;
import com.mediatek.apst.target.data.proxy.message.FastMmsCursorParser;
import com.mediatek.apst.target.data.proxy.message.MessageProxy;
import com.mediatek.apst.target.util.Global;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FastMmsCursorParserTest extends AndroidTestCase {

    private MessageProxy mMessageProxy;

    private FastMmsCursorParser mCursorParser;

    private ByteBuffer mBuffer = ByteBuffer.allocate(Global.DEFAULT_BUFFER_SIZE);

    IRawBlockConsumer mConsumer = new IRawBlockConsumer() {

        public void consume(byte[] block, int blockNo, int totalNo) {

        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMessageProxy = MessageProxy.getInstance(getContext());
        mBuffer.clear();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test01_onParseCursorToRaw() {
        ArrayList<Long> threadIdsToReturn = new ArrayList<Long>();
        mMessageProxy.importMms(MessageUtils.MMS_RAW, threadIdsToReturn);
        mMessageProxy.importMms(MessageUtils.getMmsRaw(), threadIdsToReturn);
        mMessageProxy.importMms(MessageUtils.MMS_RAW, threadIdsToReturn);
        mMessageProxy.importMms(MessageUtils.getMmsRaw(), threadIdsToReturn);

        mMessageProxy.importMms(MessageUtils.MMS_RAW, threadIdsToReturn);
        mMessageProxy.importMms(MessageUtils.getMmsRaw(), threadIdsToReturn);
        mMessageProxy.importMms(MessageUtils.MMS_RAW, threadIdsToReturn);
        mMessageProxy.importMms(MessageUtils.getMmsRaw(), threadIdsToReturn);
        Cursor cursor = mMessageProxy.getContentResolver().query(
                MmsContent.CONTENT_URI,
                new String[] { MmsContent.COLUMN_ID, MmsContent.COLUMN_THREAD_ID, MmsContent.COLUMN_DATE,
                        MmsContent.COLUMN_M_TYPE, MmsContent.COLUMN_READ, MmsContent.COLUMN_SUBJECT,
                        MmsContent.COLUMN_LOCKED, MmsContent.COLUMN_MSG_BOX, MmsContent.COLUMN_SIM_ID },
                MmsContent.COLUMN_THREAD_ID + ">0", null, MmsContent.COLUMN_DATE);
        assertTrue(cursor.moveToNext());
        mCursorParser = new FastMmsCursorParser(cursor, mConsumer, mBuffer, true, mMessageProxy);
        mCursorParser.onParseCursorToRaw(cursor, mBuffer);

        new FastMmsCursorParser(cursor, mConsumer, true, mMessageProxy);
        cursor.close();
    }
}
