package com.del.delcontainer.managers;


import android.util.Log;

import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.apiUtils.pojo.LinkedApplicationDetails;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Experimental feature for now.
 * Handle chatbot conversation and manage user interaction
 * with data and contained apps.
 * Interface for managing user queries and the health domain model
 *
 */
public class ConversationManager {

    private static final String TAG = "ConversationManager";

    private WebSocketClient mWebSocketClient;
    final String uniqueID = UUID.randomUUID().toString();
    final JSONObject messagePayload = new JSONObject();
    private BotResponseActionListener botResponseActionListener;
    private BotResponseMessageListener botResponseMessageListener;
    private static ConversationManager conversationManager = new ConversationManager();

    private boolean sessionActive = false;

    private ConversationManager() {
    }

    public static ConversationManager getInstance() {
        return conversationManager;
    }

    public boolean isConversationSessionActive() {
        return sessionActive;
    }

    public void setConversationSessionStatus(boolean isActive) {
        sessionActive = isActive;
    }

    public void setBotResponseListener(BotResponseMessageListener botResponseMessageListener,
                                       BotResponseActionListener botResponseActionListener) {
        this.botResponseMessageListener = botResponseMessageListener;
        this.botResponseActionListener = botResponseActionListener;
    }

    public void sendUserMessage(String userMsgString, String type) {
        try {
            messagePayload.put(Constants.BOTKIT_TYPE, type);
            messagePayload.put(Constants.BOTKIT_USER, uniqueID);
            messagePayload.put(Constants.BOTKIT_TEXT, userMsgString);
            messagePayload.put(Constants.BOTKIT_CHANNEL, Constants.BOTKIT_SOCKET);
            messagePayload.put(Constants.BOTKIT_USER_PROFILE, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWebSocketClient.send(messagePayload.toString());
    }

    public void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(Constants.WS_PREFIX + Constants.DEL_SERVICE_IP + ":" + Constants.DEL_PORT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i(TAG, "Opened");
            }

            @Override
            public void onMessage(final String s) {

                final String message = s;
                try {
                    JSONObject response = new JSONObject(message);
                    Log.d(TAG, response.toString());
                    String botResponseText = response.getString(Constants.BOTKIT_TEXT);

                    // botResponse is a JSON object with fields - text, action and params.
                    // Display text to the user and use params if action is present
                    botResponseMessageListener.onBotResponseMessage(botResponseText);

                    if (response.has(Constants.BOT_ACTION) &&
                            response.getString(Constants.BOT_ACTION).equals(Constants.BOT_ACTION_HEALTH)) {
                        JSONObject botParams = (JSONObject) response.get(Constants.BOT_ENTITY_PARAMS);

                        // Params will have an entity_health_metric like the below sample - use it to get the metric type
                        // "entity_health_metric" : {"stringValue":"weight","kind":"stringValue"}
                        JSONObject botParamsEntity = (JSONObject) botParams.get(Constants.BOT_ENTITY);
                        String healthMetricType = botParamsEntity.getString(botParamsEntity.getString(Constants.BOT_ENTITY_KIND));

                        Log.d(TAG, "onMessage: botParamsEntity     : " + botParamsEntity.toString());
                        Log.d(TAG, "onMessage: botParamsMetricType : " + healthMetricType);

                        // Check and get applications that can handle the healthMetricType - weight, hr etc.
                        LinkedApplicationDetails app = DelAppManager.getInstance().getQueryResponseApp(healthMetricType);
                        if (null != app) {
                            botResponseMessageListener.onBotResponseMessage(Constants.BOT_LAUNCHING_APP + app.getApplicationName());
                            botResponseActionListener.onBotResponseAction(app.getApplicationId(),
                                    app.getApplicationName(),
                                    app.getApplicationUrl(),
                                    Constants.APP_OPEN);
                        } else {
                            botResponseMessageListener.onBotResponseMessage(Constants.BOT_ERROR_NO_APP_FOUND);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {

                Log.i(TAG, "Closed connection. " + s);
                sessionActive = false;
            }

            @Override
            public void onError(Exception e) {
                Log.i(TAG, "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    /**
     * Interface for returning messages to the caller
     */
    public interface BotResponseMessageListener {
        void onBotResponseMessage(String message);
    }

    /**
     * Interface for passing application control functions
     */
    public interface BotResponseActionListener {
        void onBotResponseAction(String appId, String appName, String packageName, int action);
    }

}
