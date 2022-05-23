package com.del.delcontainer.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RunningAppsListViewAdapter extends RecyclerView.Adapter<RunningAppsListViewAdapter.ViewHolder> {

    private static final String TAG = "RunningAppsListViewAdapter";

    private final HashMap<String, String> appNameMap;
    private ArrayList<String> appNameList;
    private final CloseRunningAppClickListener closeRunningAppClickListener;

    public RunningAppsListViewAdapter(HashMap<String, String> appNameMap,
                                      CloseRunningAppClickListener closeRunningAppClickListener) {
        this.appNameMap = appNameMap;
        this.closeRunningAppClickListener = closeRunningAppClickListener;

       createAppNameList();
    }

    public void createAppNameList() {
        appNameList = new ArrayList<>();
        for(Map.Entry<String, String> entry : appNameMap.entrySet()) {
            this.appNameList.add(entry.getValue());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.runningapps_listitem, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Bind app name to the list item
     * @param holder view holder
     * @param position running app index
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.runningAppName.setText(appNameList.get(position));
    }

    @Override
    public int getItemCount() {
        return appNameMap.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView runningAppName;
        ImageButton closeApp;

        public ViewHolder(View itemView) {
            super(itemView);

            this.runningAppName = itemView.findViewById(R.id.running_app_name);
            this.closeApp = itemView.findViewById(R.id.close_running_app_button);

            this.closeApp.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: Closing App");

            // Need to create a new set to prevent the app manager from
            // modifying the same set concurrently
            HashMap<String, String> runningApps = (HashMap) appNameMap.clone();
            String appName = appNameList.get(getAdapterPosition());

            for(Map.Entry<String, String> entry : runningApps.entrySet()) {
                if(entry.getValue().equals(appName)) {
                    closeRunningAppClickListener.onCloseRunningAppClick(entry.getKey());
                }
            }
        }
    }

    /**
     * Interface providing functionality to close running app
     */
    public interface CloseRunningAppClickListener {
        void onCloseRunningAppClick(String appId);
    }
}
