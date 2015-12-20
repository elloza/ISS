package com.lozasolutions.iss.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lozasolutions.iss.R;
import com.lozasolutions.iss.activities.DetailPassActivity;
import com.lozasolutions.iss.models.ISSPass;
import com.lozasolutions.iss.utils.controls.Constants;
import com.lozasolutions.iss.utils.controls.Utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Created by Alberto on 17/11/2015.
 */
public class PassesAdapter extends RecyclerView.Adapter<PassesAdapter.PassesViewHolder> {

    private List<ISSPass> items = Collections.emptyList();
    protected Context context;

    public PassesAdapter(List<ISSPass> items, Context context) {
        this.items = items;
        this.context = context;

    }

    @Override
    public PassesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_station_pass, parent, false);
        PassesViewHolder viewHolder = new PassesViewHolder(v);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(PassesViewHolder holder, int position) {

        //Get pass
        ISSPass issPass = items.get(position);
        Long millis = issPass.getRisetime()*1000L;

        Timestamp stamp = new Timestamp(millis);
        Date date = new Date(stamp.getTime());

        Long minutes = issPass.getDuration()/60L;
        Long seconds = issPass.getDuration()%60;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setTimeZone(TimeZone.getDefault());

        String dayOfWeek = Utils.capitalize(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        String hourFormatted  = format.format(date);



        holder.txtDuration.setText(String.format(context.getResources().getConfiguration().locale, context.getString(R.string.row_next_pass_duration),minutes,seconds));
        holder.txtTime.setText(String.format(context.getResources().getConfiguration().locale, context.getString(R.string.row_next_pass_time),dayOfWeek,dayOfMonth,month,hourFormatted));
        holder.setClickListener(viewListener);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    //Listener
    private PassesViewHolder.ClickListener viewListener = new PassesViewHolder.ClickListener() {
        @Override
        public void onClick(View v, int pos, boolean isLongClick) {

            if (!isLongClick) {

                ISSPass pass = items.get(pos);
                Intent myIntent = new Intent(context, DetailPassActivity.class);
                myIntent.putExtra(Constants.ISSPASS, pass);
                v.getContext().startActivity(myIntent);

            }
        }
    };


    static class PassesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView txtDuration,txtTime;
        private ClickListener clickListener;

        public PassesViewHolder(View itemView) {
            super(itemView);
            txtDuration = (TextView) itemView.findViewById(R.id.txtDuration);
            txtTime = (TextView) itemView.findViewById(R.id.txtTime);
            itemView.setOnClickListener(this);
        }

        /* Interface for handling clicks - both normal and long ones. */
        public interface ClickListener {

            /**
             * Called when the view is clicked.
             *
             * @param v           view that is clicked
             * @param position    of the clicked item
             * @param isLongClick true if long click, false otherwise
             */
            public void onClick(View v, int position, boolean isLongClick);

        }

        /* Setter for listener. */
        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View v) {

            // If not long clicked, pass last variable as false.
            clickListener.onClick(v, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View v) {

            // If long clicked, passed last variable as true.
            clickListener.onClick(v, getAdapterPosition(), true);
            return true;
        }


    }

}
