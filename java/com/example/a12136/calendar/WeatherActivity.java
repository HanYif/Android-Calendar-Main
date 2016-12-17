package com.example.a12136.calendar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Otherwise蔡岳 on 2016/12/17.
 */

public class WeatherActivity extends AppCompatActivity {
    private static final String url = "http://ws.webxml.com.cn/WebServices/WeatherWS.asmx/getWeather";
    private static final int UPDATE_CONTENT = 0;
    RecyclerView recyclerView;
    WeatherAdapter adapter;
    ArrayList<Weather> weather_list = new ArrayList<Weather>();
    TextView city, time, average_t, field_t, humidity, air, wind, ray, cold, clothes, car, exercise;
    String cityname;
    final List<Map<String, Object>> data = new ArrayList<>();
    final String[] para_value = new String[5];


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        FindViewById();
        Bundle bundle = getIntent().getExtras();
        cityname = bundle.getString("city");
        city.setText(cityname);

        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            Toast.makeText(WeatherActivity.this, "当前没有可用网络！", Toast.LENGTH_SHORT).show();
        }
        else {
            sendRequestWithHttpURLConnection();
        }
    }

    private  void createSimpleAdapter() {
        String[] para = new String[] {"紫外指数", "感冒指数", "穿衣指数", "洗车指数", "运动指数"};
        for (int i = 0; i < 5; i++) {
            Map<String, Object> temp = new LinkedHashMap<>();
            temp.put("para", para[i]);
            temp.put("value", para_value[i]);
            data.add(temp);
        }
        ListView listView = (ListView)findViewById(R.id.para_list);
        final SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.weather_para_item,
                            new String[] {"para", "value"}, new int[] {R.id.para, R.id.para_value});
        listView.setAdapter(simpleAdapter);
    }

    private void FindViewById() {
        city = (TextView)findViewById(R.id.city);
        time = (TextView)findViewById(R.id.time);
        average_t = (TextView)findViewById(R.id.average_temperature);
        field_t = (TextView)findViewById(R.id.field_temperature);
        humidity = (TextView)findViewById(R.id.humidity);
        air = (TextView)findViewById(R.id.air);
        wind = (TextView)findViewById(R.id.wind);
        //ray = (TextView)findViewById(R.id.ray);
        //cold = (TextView)findViewById(R.id.cold);
        //clothes = (TextView)findViewById(R.id.clothes);
        //car = (TextView)findViewById(R.id.car);
        //exercise = (TextView)findViewById(R.id.execise);

        recyclerView = (RecyclerView)findViewById(R.id.weather_horizontal);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                HttpURLConnection connection = null;
                try {
                    //Log.i("key", "Begin the connection");
                    connection = (HttpURLConnection)((new URL(url.toString()).openConnection()));
                    connection.setRequestMethod("POST");
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    String request = cityname;
                    request = URLEncoder.encode(request, "utf-8");
                    outputStream.writeBytes("theCityCode=" + request + "&theUserID=65d8870adcb84d5586ac0a43b0169abd");

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Message message = new Message();
                    message.what = UPDATE_CONTENT;
                    message.obj = parseXMLWithPull(response.toString());
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
            }
        }).start();
    }

    private ArrayList<String> parseXMLWithPull(String xml) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("string".equals(parser.getName())) {
                            String str = parser.nextText();
                            list.add(str);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                eventType=parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    };


    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE_CONTENT:
                    ArrayList<String> list = (ArrayList<String>)message.obj;
                    LinearLayout l1 = (LinearLayout)findViewById(R.id.l1);
                    LinearLayout l2 = (LinearLayout)findViewById(R.id.l2);
                    l1.setVisibility(View.VISIBLE);
                    l2.setVisibility(View.VISIBLE);
                    city.setText(list.get(1));
                    time.setText(list.get(3).substring(11) + " 更新");
                    field_t.setText(list.get(8));
                    handleawh(list.get(4));
                    handleair(list.get(5));
                    handleothers(list.get(6));
                    handlerecycleview(list);
                default:
                    break;
            }
        }
    };

    void handleawh(String str) {
        int a = str.indexOf('：');
        str = str.substring(a+1);
        a = str.indexOf('：');
        int b = str.indexOf('；');
        average_t.setText(str.substring(a + 1, b));
        str = str.substring(b+1);
        a = str.indexOf('：');
        b = str.indexOf('；');
        wind.setText(str.substring(a + 1, b));
        str = str.substring(b+1);
        humidity.setText(str);
    }

    void handleair(String str) {
        int a = str.indexOf('。');
        air.setText(str.substring(a+1, str.length() - 1));
    }

    void handleothers(String str) {
        int a = str.indexOf('：');
        int b = str.indexOf('。');
        //ray.setText(str.substring(a+1, b));
        para_value[0] = str.substring(a+1, b);
        str = str.substring(b+1);
        a = str.indexOf('：');
        b = str.indexOf('。');
        //cold.setText(str.substring(a+1, b));
        para_value[1] = str.substring(a+1, b);
        str = str.substring(b+1);
        a = str.indexOf('：');
        b = str.indexOf('。');
        //clothes.setText(str.substring(a+1, b));
        para_value[2] = str.substring(a+1, b);
        str = str.substring(b+1);
        a = str.indexOf('：');
        b = str.indexOf('。');
        //car.setText(str.substring(a+1, b));
        para_value[3] = str.substring(a+1, b);
        str = str.substring(b+1);
        a = str.indexOf('：');
        b = str.indexOf('。');
        //exercise.setText(str.substring(a+1, b));
        para_value[4] = str.substring(a+1, b);
        createSimpleAdapter();
    }

    void handlerecycleview(ArrayList<String> list) {
        for (int i = 7; i < 29; i +=5) {
            int a = list.get(i).indexOf(' ');
            weather_list.add(new Weather(list.get(i).substring(0, a), list.get(i).substring(a+1), list.get(i+1)));
        }
        if (weather_list.size() == 1)
            Toast.makeText(WeatherActivity.this, "rnmlgb", Toast.LENGTH_SHORT).show();
        adapter = new WeatherAdapter(WeatherActivity.this, weather_list);
        recyclerView.setAdapter(adapter);
    }
}
