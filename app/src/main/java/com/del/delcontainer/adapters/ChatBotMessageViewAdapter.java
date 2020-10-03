package com.del.delcontainer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.utils.chatBotUtils.ChatMessage;

import java.util.List;

public class ChatBotMessageViewAdapter extends RecyclerView.Adapter<ChatBotMessageViewAdapter.CustomViewHolder> {
    private static final String TAG = "ChatBotMessageViewAdapter";
    List<ChatMessage> chatMessages;
    Context context;

    class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public CustomViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.chat_content);
        }
    }

    public ChatBotMessageViewAdapter(List<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).user()){
            return R.layout.sent_layout;
        }
        return R.layout.receive_layout;
    }

    @Override
    public int getItemCount() {
        return  chatMessages.size();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        holder.textView.setText(chatMessages.get(position).getText());
    }
}
