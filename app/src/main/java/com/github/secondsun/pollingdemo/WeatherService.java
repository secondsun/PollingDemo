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

import android.content.Intent;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherService extends JobService {
    public static final String TEMP_CHANGE = "TEMP_CHANGE";

    public WeatherService() {
    }

    @Override
    public boolean onStartJob(final JobParameters job) {

        String zip = job.getExtras().getString("zip", "30318");
        String serverKey = BuildConfig.SERVER_KEY;

        final String url = "http://api.openweathermap.org/data/2.5/weather?zip="+zip+",us&units=imperial&appid="+serverKey;

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                try {

                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = client.newCall(request).execute();

                    JSONObject responseJson = new JSONObject(response.body().string());
                    int temp = responseJson.getJSONObject("main").optInt("temp", -999);
                    if (temp == -999) System.err.print(responseJson.toString());
                    sendBroadcast(new Intent(TEMP_CHANGE).putExtra("temp",temp));

                    jobFinished(job, false);

                } catch (Exception e) {
                    e.printStackTrace();
                    sendBroadcast(new Intent(TEMP_CHANGE).putExtra("temp",-999));

                    jobFinished(job, true);
                }

                return null;
            }
        }.execute((Object[]) null);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
