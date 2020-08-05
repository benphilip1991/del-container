package com.del.delcontainer.ui.chatbot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.CustomViewHolder> {

    List<ChatType> chatTypes;
    Context context;

    class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public CustomViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.chat_content);
        }
    }

    public ChatAdapter(List<ChatType> chatTypes, Context context) {
        this.chatTypes = chatTypes;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if(chatTypes.get(position).user()){
            return R.layout.sent_layout;
        }
        return R.layout.receive_layout;
    }

    @Override
    public int getItemCount() {
        return  chatTypes.size();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        holder.textView.setText(chatTypes.get(position).getText());
    }
}
