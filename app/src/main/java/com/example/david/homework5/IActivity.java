package com.example.david.homework5;

/**
 * Created by David on 12/8/2015.
 */
public interface IActivity {

    void connected();
    void error(String errorMesssage);
    void messageReceived(String message);
    void statusChanged(String status);
}
