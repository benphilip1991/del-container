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
import com.del.delcontainer.utils.apiUtils.pojo.LinkedApplicationDetails;

import java.util.ArrayList;
import java.util.HashMap;

public class InstalledAppListViewAdapter extends RecyclerView.Adapter<InstalledAppListViewAdapter.ViewHolder> {

    private static final String TAG = "InstalledAppListViewAda";

    private ArrayList<LinkedApplicationDetails> linkedAppDetails;
    private AppClickListener appClickListener;
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
                                       AppClickListener appClickListener) {

        this.context = context;
        this.appClickListener = appClickListener;
        this.linkedAppDetails = linkedAppDetails;
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

        holder.appLabel.setText(linkedAppDetails.get(position).getApplicationName());
        try {
            holder.appImage.setBackgroundResource(R.drawable.default_app_icon);
        } catch (NullPointerException e) {
            Log.e(TAG, "onBindViewHolder: Exception ", e);
        }
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
}
