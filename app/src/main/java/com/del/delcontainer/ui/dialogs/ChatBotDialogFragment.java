package com.del.delcontainer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.adapters.ChatBotMessageViewAdapter;
import com.del.delcontainer.managers.ConversationManager;
import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.chatBotUtils.ChatMessage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ChatBotDialogFragment extends DialogFragment {
    FloatingActionButton chatButton;
    EditText inputText;
    Button sendButton;
    Button closeButton;
    RecyclerView recyclerView;
    ChatBotMessageViewAdapter chatAdapter;
    List<ChatMessage> chatMessageList;
    ConversationManager conversationManager;
    static int TIME_OUT = 3000;
    Handler mHandler = new Handler();

    public ChatBotDialogFragment() {
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatBotMessageViewAdapter(chatMessageList, getContext());
        conversationManager = ConversationManager.getInstance();
        conversationManager.connectWebSocket();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static ChatBotDialogFragment newInstance() {
        return new ChatBotDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.chat_dialog, null);
        chatButton = v.findViewById(R.id.chat_button);
        inputText = v.findViewById(R.id.chat_input_text);
        sendButton = (Button) v.findViewById(R.id.send_button);
        closeButton = (Button) v.findViewById(R.id.close_button);
        recyclerView = v.findViewById(R.id.chat_recycler_view);

        // Initiate a new chat session only on a new app launch
        if (!conversationManager.isConversationSessionActive()) {
            conversationManager.sendUserMessage("", Constants.INITIAL_MESSAGE);
            conversationManager.setConversationSessionStatus(true);
        }

        /**
         * Configure recycler view adapter for chat bot messages
         */
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(chatAdapter);

        /**
         * Set the view and create the dialog
         */
        builder.setView(v);
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        /**
         * Set the callback function when a response message is received
         * from the conversation manager
         * First function is for the message listener
         * Second function is the message action listener
         */
        conversationManager.setBotResponseListener((message) -> getActivity().runOnUiThread(() -> {
                    showMessage(message, false);
                    inputText.setText("");
                }),
                /**
                 * Set the callback function when a application action is received
                 * from the conversation manager
                 */
                (appId, appName, packageName, action) -> mHandler.postDelayed(() -> {
                    //Open the application
                    DelAppManager delAppManager = DelAppManager.getInstance();
                    delAppManager.launchApp(appId, appName, packageName);
                    dialog.dismiss();
                }, TIME_OUT));

        /**
         * Set the listener function when focus is taken of the input bar
         */
        inputText.setOnFocusChangeListener((v1, hasFocus) -> {
            if (hasFocus)
                inputText.setHint("");
            else
                inputText.setHint("Ask Something");
        });

        /**
         * Set a listener function to send messages to the conversation manager
         * when the action button is clicked in the editor
         */
        inputText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEND) {
                String inputTextValue = inputText.getText().toString();
                conversationManager.sendUserMessage(inputTextValue, Constants.USER_MESSAGE);
                showMessage(inputTextValue, true);
                inputText.setText("");
            }
            return false;
        });
        /**
         * Set a click listener to close the chat bot dialog
         */
        closeButton.setOnClickListener((View) -> {
            // Might need to close connection to del-api
            dialog.dismiss();
        });
        /**
         * Set a click listener to send the message type in the input
         */
        sendButton.setOnClickListener((View) -> {
            String inputTextValue = inputText.getText().toString();
            conversationManager.sendUserMessage(inputTextValue, Constants.USER_MESSAGE);
            showMessage(inputTextValue, true);
            inputText.setText("");
            if (v != null) {
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        return dialog;
    }

    /**
     * Function to display a message on the chat window
     */
    void showMessage(String messageString, boolean user) {
        if (!"".equals(messageString)) {
            ChatMessage responseMessage = new ChatMessage(messageString, user);
            chatMessageList.add(responseMessage);
            chatAdapter.notifyDataSetChanged();
            if (!isLastVisible())
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    /**
     * Function to check if the last message in the chat is visible
     */
    boolean isLastVisible() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int pos = layoutManager.findLastCompletelyVisibleItemPosition();
        int numItems = recyclerView.getAdapter().getItemCount();
        return (pos >= numItems);
    }
}
