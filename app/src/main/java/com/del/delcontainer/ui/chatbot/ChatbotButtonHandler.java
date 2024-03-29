package com.del.delcontainer.ui.chatbot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.del.delcontainer.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Simple utility class to manage the chat bot button behaviour
 */
public class ChatbotButtonHandler {

    private static final String TAG = "ChatBotButtonHandler";
    private static ChatbotButtonHandler instance = null;
    private boolean isDrawerOpen = false;

    private ChatbotButtonHandler() {
    }

    public static synchronized ChatbotButtonHandler getInstance() {
        if(null == instance) {
            instance = new ChatbotButtonHandler();
        }
        return instance;
    }

    /**
     * Show/Hide chat bot button when using the available apps drawer.
     * The button is disabled when the drawer is open.
     *
     * @param activity activity instance used to fetch the chat button
     * @param showChatButton Boolean parameter to toggle the chat button
     */
    @SuppressLint("RestrictedApi")
    public void toggleChatButtonVisibility(Activity activity, boolean showChatButton) {
        FloatingActionButton chatButton = activity.findViewById(R.id.chat_button);

        if (showChatButton && !isDrawerOpen) {

            Log.d(TAG, "Showing chat button");
            chatButton.animate().alpha(1.0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    chatButton.setAlpha(0.0f);
                    chatButton.setVisibility(View.VISIBLE);
                }
            }).start();
        } else {

            Log.d(TAG, "Hiding chat button");
            chatButton.animate().alpha(0.0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    chatButton.setAlpha(1.0f);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    chatButton.setVisibility(View.INVISIBLE);
                }
            }).start();
        }
    }

    /**
     * Set app drawer state - if open, don't show chat button
     * @param isDrawerOpen set true if app drawer is open
     */
   public void setIsDrawerOpen(boolean isDrawerOpen) {
        this.isDrawerOpen = isDrawerOpen;
   }
}
