package com.example.hongbeomahn.sfdev;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;



public class MainActivity extends AppCompatActivity {


    public static final String BROKER = "tcp://smartfleet.sktelecom.com:1883";


    //# Means subscribe to everything
    public static final String TOPIC = "#";

    //Optional
    public static final String USERNAME2 = "kbr89123451234554321";


    public MqttAndroidClient CLIENT;
    public MqttConnectOptions MQTT_CONNECTION_OPTIONS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MqttSetup(this);
        MqttConnect();

        CLIENT.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            //background notification
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("topic:" + topic, "message:" + message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    void MqttSetup(Context context) {


        CLIENT = new MqttAndroidClient(getBaseContext(), BROKER, MqttClient.generateClientId());
        MQTT_CONNECTION_OPTIONS = new MqttConnectOptions();
        MQTT_CONNECTION_OPTIONS.setAutomaticReconnect(false);
        MQTT_CONNECTION_OPTIONS.setCleanSession(true);
        MQTT_CONNECTION_OPTIONS.setConnectionTimeout(15);
        MQTT_CONNECTION_OPTIONS.setKeepAliveInterval(60);
        MQTT_CONNECTION_OPTIONS.setUserName(USERNAME2);
        //MQTT_CONNECTION_OPTIONS.setPassword(PASSWORD.toCharArray());


        /**
         * SSL broker requires a certificate to authenticate their connection
         * Certificate can be found in resources folder /res/raw/
         */
        if (BROKER.contains("ssl")) {
            SocketFactory.SocketFactoryOptions socketFactoryOptions = new SocketFactory.SocketFactoryOptions();
            try {
                socketFactoryOptions.withCaInputStream(context.getResources().openRawResource(R.raw.rsa_root_ca));
                MQTT_CONNECTION_OPTIONS.setSocketFactory(new SocketFactory(socketFactoryOptions));
            } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | KeyManagementException | UnrecoverableKeyException e) {
                e.printStackTrace();
                Log.d("mqtt", e.toString());

            }
        }
    }

    void MqttConnect() {
        try {

            final IMqttToken token = CLIENT.connect(MQTT_CONNECTION_OPTIONS);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("mqtt:", "connected, token:" + asyncActionToken.toString());
                    subscribe(TOPIC, (byte) 1);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("mqtt:", "not connected : " + asyncActionToken.toString());
                    Log.d("mqtt", exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void subscribe(String topic, byte qos) {

        try {
            IMqttToken subToken = CLIENT.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("mqtt:", "subscribed" + asyncActionToken.toString());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {

                    Log.d("mqtt:", "subscribing error");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void publish(String topic, String msg) {

        //0 is the Qos
        MQTT_CONNECTION_OPTIONS.setWill(topic, msg.getBytes(), 0, false);
        try {
            IMqttToken token = CLIENT.connect(MQTT_CONNECTION_OPTIONS);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("mqtt:", "send done" + asyncActionToken.toString());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("mqtt:", "publish error" + asyncActionToken.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void unsubscribe(String topic) {

        try {
            IMqttToken unsubToken = CLIENT.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.d("mqtt:", "unsubcribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {


                    Log.d("mqtt:", "couldnt unregister");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void disconnect() {
        try {
            IMqttToken disconToken = CLIENT.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("mqtt:", "disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {


                    Log.d("mqtt:", "couldnt disconnect");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}