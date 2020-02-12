package com.del.delcontainer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;

import java.util.ArrayList;
import java.util.HashMap;

public class InstalledAppListViewAdapter  extends RecyclerView.Adapter<InstalledAppListViewAdapter.ViewHolder> {

    private static final String TAG = "InstalledAppListViewAda";

    private ArrayList<String> appList = new ArrayList<>();
    private HashMap<String, Integer> appDetails;
    private AppClickListener appClickListener;
    private Context context;

    /**
     * Constructor to initialize the Adapter.
     */
    public InstalledAppListViewAdapter(Context context, HashMap<String, Integer> appDetails,
                                       AppClickListener appClickListener) {

        this.appClickListener = appClickListener;
        this.appDetails = appDetails;
        this.context = context;

        initAppList();
    }

    /**
     * Extracts app names from the HashMap
     */
    private void initAppList() {

        for (HashMap.Entry<String, Integer> entry : appDetails.entrySet()) {
            appList.add(entry.getKey());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.installedservice_listitem, parent, false);

        ViewHolder viewHolder = new ViewHolder(view, appClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.appLabel.setText(appList.get(position));
        try {
            holder.appImage.setBackgroundResource(appDetails.get(appList.get(position)).intValue());
        } catch (NullPointerException e) {

            Log.e(TAG, "onBindViewHolder: Exception ", e);
        }
    }

    @Override
    public int getItemCount() {

        return appList.size();
    }

    /**
     * Create a method that will add new items to the list
     */
//    public void setApps(List<ApplicationObject> obj) {
//
//        this.appList = obj;
//        notifyDataSetChanged();
//    }


    /**
     * ViewHolder that represents each individual item in the list
     * In this instance, app cards (icon + name)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView appImage;
        TextView appLabel;
        AppClickListener appClickListener;

        public ViewHolder(View itemView, AppClickListener appClickListener) {
            super(itemView);

            appImage = itemView.findViewById(R.id.appImage);
            appLabel = itemView.findViewById(R.id.appLabel);
            this.appClickListener = appClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            Toast.makeText(context, "Launching " + appList.get(getAdapterPosition()),
                    Toast.LENGTH_SHORT).show();
            appClickListener.onAppClick(getAdapterPosition());
        }

    }

    /**
     * Interface that will pass on the position of the app
     * in the list to the implementation
     */
    public interface AppClickListener {
        void onAppClick(int position);
    }
}
