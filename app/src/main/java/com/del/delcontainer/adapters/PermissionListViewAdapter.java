package com.del.delcontainer.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;

import java.util.List;

public class PermissionListViewAdapter extends RecyclerView.Adapter<PermissionListViewAdapter.ViewHolder>{
    List<String> permissionsList;
    Context context;
    View view;
    ViewHolder viewHolder;
    TextView textView;

    public PermissionListViewAdapter(Context context, List<String> permissionsList){

        this.permissionsList = permissionsList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView textView;

        public ViewHolder(View v){

            super(v);

            textView = (TextView)v.findViewById(R.id.description);
        }
    }

    @Override
    public PermissionListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        view = LayoutInflater.from(context).inflate(R.layout.permission_description_item,parent,false);

        viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.textView.setText(permissionsList.get(position));
    }

    @Override
    public int getItemCount(){

        return permissionsList.size();
    }
}