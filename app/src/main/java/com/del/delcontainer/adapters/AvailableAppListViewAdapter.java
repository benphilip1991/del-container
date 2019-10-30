package com.del.delcontainer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

public class AvailableAppListViewAdapter extends RecyclerView.Adapter<AvailableAppListViewAdapter.ViewHolder> {

    private static final String TAG = "AvailableAppListViewAda";

    private HashMap<String, HashMap<String, String>> availableApps;
    private ArrayList<String> appList = new ArrayList<>();
    private Context context;

    public AvailableAppListViewAdapter(Context context, HashMap<String, HashMap<String, String>> availableApps) {

        this.availableApps = availableApps;
        this.context = context;

        initAvailableAppList();
    }

    /**
     * Extract app names from the map
     */
    private void initAvailableAppList() {

        for(HashMap.Entry<String, HashMap<String, String>> entry : availableApps.entrySet()) {
            appList.add(entry.getKey());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.availableservice_listitem, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    /**
     * Add names, descriptions and images in the cards
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int imageRes;

        holder.availableAppLabel.setText(appList.get(position));
        holder.availableAppDescription.setText(availableApps
                .get(appList.get(position)).get(Constants.APP_DESCRIPTION));

        imageRes = Integer.parseInt(availableApps
                .get(appList.get(position)).get(Constants.APP_IMAGE));
        holder.availableAppImage.setBackgroundResource(imageRes);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView availableAppImage;
        TextView availableAppLabel;
        TextView availableAppDescription;
        Button getAvailableAppButton;

        public ViewHolder(View itemView) {
            super(itemView);

            this.availableAppImage = itemView.findViewById(R.id.availableAppImage);
            this.availableAppLabel = itemView.findViewById(R.id.availableAppLabel);
            this.availableAppDescription = itemView.findViewById(R.id.availableAppDescription);
            this.getAvailableAppButton = itemView.findViewById(R.id.getAvailableAppButton);

            getAvailableAppButton.setOnClickListener(this);
        }

        // Change to add something more useful
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: getting app : " + appList.get(getAdapterPosition()));
            Toast.makeText(context, "Getting app : "
                    + appList.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
        }
    }
}
