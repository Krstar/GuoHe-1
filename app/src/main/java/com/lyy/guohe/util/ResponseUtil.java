package com.lyy.guohe.util;

import com.lyy.guohe.gson.Weather;
import com.lyy.guohe.model.Res;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lyy on 2017/10/11.
 */

public class ResponseUtil {

    //处理服务器传来的天气的数据
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //处理传来的信息
    public static Res handleResponse(String response) {
        if (HttpUtil.isGoodJson(response)) {
            try {
                JSONObject object = new JSONObject(response);
                String code = object.getString("code");
                String msg = object.getString("msg");
                String info = object.getString("info");
                return new Res(Integer.parseInt(code), msg, info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
