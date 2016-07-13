package com.example.FundigoApp.Customer.Social;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Benjamin on 01/05/2016.
 */
@ParseClassName("PostResponse")
public class ResponsePost extends ParseObject
{
    public String getResponsePost() {
        return getString ("Response");
    }
    public void setResponsePost(String text) {
        put ("Response", text);
    }
    public void setMyPost(String text) {
        put ("MyPost", text);
    }
    public void getMyPost (){
        getString ("MyPost");
    }
    public void setFatherPost(String text) {
        put ("FatherPost", text);
    }
    public void getFatherPost (){
        getString ("FatherPosr");
    }
}
