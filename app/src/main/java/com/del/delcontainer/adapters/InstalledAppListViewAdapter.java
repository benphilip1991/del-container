package com.del.delcontainer.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.apiUtils.pojo.LinkedApplicationDetails;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class InstalledAppListViewAdapter extends RecyclerView.Adapter<InstalledAppListViewAdapter.ViewHolder> {

    private static final String TAG = "InstalledAppListViewAda";

    private final ArrayList<LinkedApplicationDetails> linkedAppDetails;
    private final AppClickListener appClickListener;
    private final AppLongClickListener appLongClickListener;

    /**
     * Constructor to initialize the adapter
     *
     * @param linkedAppDetails list of linked application
     * @param appClickListener implementation of click handler
     */
    public InstalledAppListViewAdapter(ArrayList<LinkedApplicationDetails> linkedAppDetails,
                                       AppClickListener appClickListener,
                                       AppLongClickListener appLongClickListener) {
        this.linkedAppDetails = linkedAppDetails;
        this.appClickListener = appClickListener;
        this.appLongClickListener = appLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.installedservice_listitem, parent, false);
        view.getLayoutParams().width = parent.getMeasuredWidth() / 3;
        return new ViewHolder(view, appClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String imageUrl = Constants.HTTP_PREFIX + Constants.DEL_SERVICE_IP + ":"
                + Constants.DEL_PORT + Constants.API_BASE_PATH + Constants.APP_RESOURCE_PATH
                + linkedAppDetails.get(position).getApplicationId()
                + "/" + linkedAppDetails.get(position).getApplicationUrl() +"/icon";

        Picasso.get().load(imageUrl).into(holder.appImage);
        holder.appLabel.setText(linkedAppDetails.get(position).getApplicationName());
    }

    @Override
    public int getItemCount() {
        return linkedAppDetails.size();
    }

    /**
     * ViewHolder that represents each individual item in the list
     * In this instance, app cards (icon + name)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView appImage;
        TextView appLabel;
        CardView itemCard;

        AppClickListener appClickListener;

        public ViewHolder(View itemView, AppClickListener appClickListener) {
            super(itemView);

            appImage = itemView.findViewById(R.id.app_image);
            appLabel = itemView.findViewById(R.id.app_label);
            itemCard = itemView.findViewById(R.id.installed_app_card_view);
            this.appClickListener = appClickListener;

            itemCard.setOnClickListener(this);
            itemCard.setOnLongClickListener((view) -> {
                appLongClickListener.onAppLongClick(getAdapterPosition(), view);
                return true; // the long press was consumed
            });
        }

        @Override
        public void onClick(View view) {

            Log.d(TAG, "onClick: Launching : " + linkedAppDetails.get(getAdapterPosition())
                    .getApplicationName());
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

    /**
     * Interface to handle long click events on installed apps
     * The view is passed to identify where to place the menu
     */
    public interface AppLongClickListener {
        void onAppLongClick(int position, View view);
    }
}