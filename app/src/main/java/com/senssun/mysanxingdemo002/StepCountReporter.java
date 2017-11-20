/**
 * Copyright (C) 2014 Samsung Electronics Co., Ltd. All rights reserved.
 * <p>
 * Mobile Communication Division,
 * Digital Media & Communications Business, Samsung Electronics Co., Ltd.
 * <p>
 * This software and its documentation are confidential and proprietary
 * information of Samsung Electronics Co., Ltd.  No part of the software and
 * documents may be copied, reproduced, transmitted, translated, or reduced to
 * any electronic medium or machine-readable form without the prior written
 * consent of Samsung Electronics.
 * <p>
 * Samsung Electronics makes no representations with respect to the contents,
 * and assumes no responsibility for any errors that might appear in the
 * software and documents. This publication and the contents hereof are subject
 * to change without notice.
 */

package com.senssun.mysanxingdemo002;

import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthDevice;
import com.samsung.android.sdk.healthdata.HealthDeviceManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder.ResultListener;

import java.util.Calendar;
import java.util.TimeZone;

import static com.senssun.mysanxingdemo002.MainActivity.APP_TAG;

public class StepCountReporter {
    private final HealthDataStore mStore;
    private StepCountObserver mStepCountObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;
    private static final String TAG = "StepCountReporter";

    public StepCountReporter(HealthDataStore store) {
        mStore = store;
    }

    public void start(StepCountObserver listener) {
        mStepCountObserver = listener;
        // Register an observer to listen changes of step count and get today
        // step count
        HealthDataObserver.addObserver(mStore,
                HealthConstants.StepCount.HEALTH_DATA_TYPE, mObserver);
       readTodayStepCount();


        writeStepCount();
    }

    // Read the today's step count on demand
    private void readTodayStepCount() {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        // Set time range from start time of today to the current time
        long startTime = getStartTimeOfToday();
        long endTime = startTime + ONE_DAY_IN_MILLIS;

        ReadRequest request = new ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setProperties(new String[]{HealthConstants.StepCount.COUNT})
                .setLocalTimeRange(HealthConstants.StepCount.START_TIME,
                        HealthConstants.StepCount.TIME_OFFSET, startTime,
                        endTime).build();

        try {
            resolver.read(request).setResultListener(mListener);
        } catch (Exception e) {
            Log.e(APP_TAG, "Getting step count fails.", e);
        }
    }

    public void writeStepCount() {
//        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

//        HealthDataResolver.InsertRequest request = new HealthDataResolver.InsertRequest.Builder()
//                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE).build();

        // Set time range from start time of today to the current time
        Calendar calendar =Calendar.getInstance();

        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");



        long startTime = getStartTimeOfToday();
        long endTime = startTime + ONE_DAY_IN_MILLIS;
//		ReadRequest request = new ReadRequest.Builder()
//				.setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
//				.setProperties(new String[] { HealthConstants.StepCount.COUNT })
//				.setLocalTimeRange(HealthConstants.StepCount.START_TIME,
//						HealthConstants.StepCount.TIME_OFFSET, startTime,
//						endTime).build();

        HealthDevice myDevice = new HealthDeviceManager(mStore).getLocalDevice();

        HealthData healthData = new HealthData();
        healthData.setSourceDevice(myDevice.getUuid());

        healthData.putInt(HealthConstants.StepCount.COUNT, 1256);
        healthData.putLong(HealthConstants.StepCount.END_TIME,calendar.getTimeInMillis());
        healthData.putLong(HealthConstants.StepCount.START_TIME,calendar.getTimeInMillis()-5300);
        healthData.putInt(HealthConstants.StepCount.TIME_OFFSET, timeZone.getOffset(System.currentTimeMillis()));
       // healthData.putInt(HealthConstants.StepCount.TIME_OFFSET, 8);

        Log.i(TAG, "writeStepCount: 时区"+timeZone.getOffset(System.currentTimeMillis()));
        Log.i(TAG, "writeStepCount: UUID"+myDevice.getUuid().toString());
      //  request.addHealthData(healthData);

//		HealthDataResolver.InsertRequest request1 =new HealthDataResolver.InsertRequest() {
//			@Override
//			public void addHealthData(HealthData healthData) {
//				healthData.setSourceDevice("59:9c79:78:62:73");
//				healthData.putInt(HealthConstants.StepCount.COUNT,1256);
//			}
//
//			@Override
//			public void addHealthData(List<HealthData> list) {
//
//			}
//		};

        HealthDataResolver.InsertRequest insRequest =
                new HealthDataResolver.InsertRequest.Builder()
                        .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                        .build();

        // Add health data
        insRequest.addHealthData(healthData);

        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        try {
            resolver.insert(insRequest);
        } catch (Exception e) {
            Log.d(APP_TAG, "resolver.insert() fails.");
        }

//        resolver.insert(request).setResultListener(new ResultListener<HealthResultHolder.BaseResult>() {
//
//            @Override
//            public void onResult(HealthResultHolder.BaseResult baseResult) {
//                Log.i("11111111111111111", baseResult.getStatus()+"11111111111=" + baseResult.getCount());
//            }
//        });
//		resolver.insert(request1).setResultListener(new ResultListener<HealthResultHolder.BaseResult>() {
//			@Override
//			public void onResult(HealthResultHolder.BaseResult baseResult) {
//				Log.i("11111111111111111","222222222222222222="+baseResult.toString());
//			}
//		});

    }

    private long getStartTimeOfToday() {
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTimeInMillis();
    }


    ResultListener<ReadResult> mListener = new ResultListener<ReadResult>() {

        @Override
        public void onResult(ReadResult result) {
            // TODO Auto-generated method stub
            int count = 0;

            try {
                for (HealthData data : result) {
                    count += data.getInt(HealthConstants.StepCount.COUNT);
                }
            } finally {
                result.close();
            }

            if (mStepCountObserver != null) {
                mStepCountObserver.onChanged(count);
            }
        }
    };

    // private final HealthResultHolder.ResultListener<ReadResult> mListener =
    // result -> {
    // int count = 0;
    //
    // try {
    // for (HealthData data : result) {
    // count += data.getInt(HealthConstants.StepCount.COUNT);
    // }
    // } finally {
    // result.close();
    // }
    //
    // if (mStepCountObserver != null) {
    // mStepCountObserver.onChanged(count);
    // }
    // };

    private final HealthDataObserver mObserver = new HealthDataObserver(null) {

        // Update the step count when a change event is received
        @Override
        public void onChange(String dataTypeName) {
            Log.d(APP_TAG,
                    "Observer receives a data changed event");
            readTodayStepCount();
        }
    };

    public interface StepCountObserver {
        void onChanged(int count);
    }
}
