/**
 * Copyright 2016 Summers Pittman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.secondsun.pollingdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

public class PollingDemo extends AppCompatActivity {

    private FirebaseJobDispatcher dispatcher;

    private EditText zipCode;

    private TextView temperature;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    temperature.setText(""+intent.getIntExtra("temp", -999));
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pollin_demo);
        this.dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));

        zipCode = (EditText) findViewById(R.id.zipCode);

        temperature = (TextView) findViewById(R.id.temperature);

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatcher.cancelAll();
            }
        });

        findViewById(R.id.schedule_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString("zip", zipCode.getText().toString());
                Job myJob = dispatcher.newJobBuilder()
                        .setService(WeatherService.class) // the JobService that will be called
                        .setTag("weather")        // uniquely identifies the job
                        .setReplaceCurrent(true)
                        .setRecurring(true)
                        .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                        .setTrigger(Trigger.executionWindow(0, 10))
                        .setConstraints(Constraint.ON_ANY_NETWORK)
                        .setExtras(extras)
                        .build();

                dispatcher.mustSchedule(myJob);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(WeatherService.TEMP_CHANGE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
