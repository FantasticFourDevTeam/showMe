package com.example.FundigoApp.Customer.Social;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Benjamin on 01/05/2016.
 */
@ParseClassName("ForumPost")
public class ForumPost extends ParseObject
{
    public String getPost() {
        return getString ("Post");
    }

    public void setPost(String text) {
        put ("Post", text);
    }

}
