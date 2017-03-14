package com.optimalcities.gmailauthenticationapi;

import org.json.JSONObject;

/**
 * Created by obelix on 13/03/2017.
 */


public  class PersistUserMessage {
    public JSONObject userInfo;

    PersistUserMessage(JSONObject userInfo){
        this.userInfo = userInfo;
    }
}
