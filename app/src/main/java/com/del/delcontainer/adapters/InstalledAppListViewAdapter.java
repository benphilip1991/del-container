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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.apiUtils.pojo.LinkedApplicationDetails;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class InstalledAppListViewAdapter extends RecyclerView.Adapter<InstalledAppListViewAdapter.ViewHolder> {

    private static final String TAG = "InstalledAppListViewAda";

    private ArrayList<LinkedApplicationDetails> linkedAppDetails;
    private AppClickListener appClickListener;
    private AppLongClickListener appLongClickListener;
    private Context context;

    /**
     * Constructor to initialize the adapter
     *
     * @param context
     * @param linkedAppDetails
     * @param appClickListener
     */
    public InstalledAppListViewAdapter(Context context,
                                       ArrayList<LinkedApplicationDetails> linkedAppDetails,
                                       AppClickListener appClickListener,
                                       AppLongClickListener appLongClickListener) {
        this.context = context;
        this.linkedAppDetails = linkedAppDetails;
        this.appClickListener = appClickListener;
        this.appLongClickListener = appLongClickListener;
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

        String imageUrl = Constants.HTTP_PREFIX + Constants.DEL_SERVICE_IP + ":"
                + Constants.DEL_SERVICE_PORT + "/" + linkedAppDetails.get(position)
                .getApplicationId() + "/icon";
        Picasso.with(this.context).load(imageUrl).into(holder.appImage);
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

            appImage = itemView.findViewById(R.id.appImage);
            appLabel = itemView.findViewById(R.id.appLabel);
            itemCard = itemView.findViewById(R.id.installedapp_carditem);
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
            Toast.makeText(context, "Launching " + linkedAppDetails
                            .get(getAdapterPosition()).getApplicationName(),
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

    /**
     * Interface to handle long click events on installed apps
     * The view is passed to identify where to place the menu
     */
    public interface AppLongClickListener {
        void onAppLongClick(int position, View view);
    }
}
