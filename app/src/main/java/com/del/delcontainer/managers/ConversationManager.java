package com.del.delcontainer.managers;


import android.util.Log;

import com.del.delcontainer.utils.Constants;

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
 * TODO: make a fragment with a separate chat view
 */
public class ConversationManager {
    private WebSocketClient mWebSocketClient;
    final String uniqueID = UUID.randomUUID().toString();
    final JSONObject messagePayload = new JSONObject();
    private BotResponseActionListener botResponseActionListener;
    private BotResponseMessageListener botResponseMessageListener;
    private static ConversationManager conversationManager = new ConversationManager();
    private ConversationManager() {
    }

    public static ConversationManager getInstance() {
        return conversationManager;
    }

    public void setBotResponseListener(BotResponseMessageListener botResponseMessageListener,
                                       BotResponseActionListener botResponseActionListener) {
        this.botResponseMessageListener = botResponseMessageListener;
        this.botResponseActionListener = botResponseActionListener;
    }

    public void sendUserMessage(String userMsgString, String type){
        try {
            messagePayload.put("type", type);
            messagePayload.put("User", uniqueID);
            messagePayload.put("text", userMsgString);
            messagePayload.put("channel", "socket");
            messagePayload.put("user_profile", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWebSocketClient.send(messagePayload.toString());
    }

    public void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://" + Constants.DEL_SERVICE_IP + ":" + Constants.DEL_PORT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                sendUserMessage("Hi from android application", Constants.INITIAL_MESSAGE);
            }

            @Override
            public void onMessage(final String s) {

                final String message = s;
                try {
                    JSONObject response = new JSONObject(message);
                    String response_text = response.getString("text");
                    Log.d("Obtained response", response.toString());
                    //TODO: fix hardcoded appIds and actions with the chatbot
                    if(response_text.equalsIgnoreCase("You asked for steps count")) {
                        botResponseMessageListener.onBotResponseMessage("Opening Steps Application");
                        botResponseActionListener.onBotResponseAction(
                                "5f34de427352790e809ca870",
                                "Steps",
                                "steps",
                                Constants.APP_OPEN);
                    }
                    else {
                        botResponseMessageListener.onBotResponseMessage(response_text);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
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
