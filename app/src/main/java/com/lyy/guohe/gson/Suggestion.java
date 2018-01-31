package com.lyy.guohe.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lyy on 2017/10/11.
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("sport")
    public Sport sport;

    @SerializedName("air")
    public Air air;

    public class Air {
        @SerializedName("txt")
        public String info;
    }

    public class Comfort {

        @SerializedName("txt")
        public String info;

    }

    public class Sport {

        @SerializedName("txt")
        public String info;

    }
}
