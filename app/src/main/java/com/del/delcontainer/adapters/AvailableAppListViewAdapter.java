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
import com.del.delcontainer.utils.apiUtils.pojo.ApplicationDetails;

import java.util.ArrayList;
import java.util.HashMap;

public class AvailableAppListViewAdapter extends RecyclerView.Adapter<AvailableAppListViewAdapter.ViewHolder> {

    private static final String TAG = "AvailableAppListViewAda";

    private ArrayList<ApplicationDetails> availableAppsList; // from del-api
    private GetAppClickListener getAppClickListener;
    private Context context;

    /**
     * Initialize the available apps list adapter
     * @param context
     * @param availableApps
     * @param getAppClickListener
     */
    public AvailableAppListViewAdapter(Context context,
                                       ArrayList<ApplicationDetails> availableApps,
                                       GetAppClickListener getAppClickListener) {
        this.context = context;
        this.availableAppsList = availableApps;
        this.getAppClickListener = getAppClickListener;
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
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int imageRes;

        holder.availableAppLabel.setText(availableAppsList.get(position).getApplicationName());
        holder.availableAppDescription.setText(
                availableAppsList.get(position).getApplicationDescription());
        imageRes = R.drawable.default_app_icon;
        holder.availableAppImage.setBackgroundResource(imageRes);
    }

    @Override
    public int getItemCount() {
        return availableAppsList.size();
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
            Log.d(TAG, "onClick: getting app : " + availableAppsList.get(getAdapterPosition())
                    .getApplicationName());
            getAppClickListener.onGetAppClick(getAdapterPosition());
        }
    }

    /**
     * Interface providing app linking functionality
     */
    public interface GetAppClickListener {
        void onGetAppClick(int position);
    }
}
