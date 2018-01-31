package com.lyy.guohe.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lyy on 2017/10/11.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
