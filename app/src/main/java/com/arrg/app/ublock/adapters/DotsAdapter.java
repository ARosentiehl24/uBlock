package com.arrg.app.ublock.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arrg.app.ublock.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/*
 * Created by albert on 4/02/2016.
 */
public class DotsAdapter extends RecyclerView.Adapter<DotsAdapter.ViewHolder> {

    private Context activity;
    private ArrayList<ImageView> dots;
    private Integer dotColor;

    public DotsAdapter(Context activity, ArrayList<ImageView> dots, Integer dotColor) {
        this.activity = activity;
        this.dots = dots;
        this.dotColor = dotColor;
    }

    @Override
    public DotsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        View dots = inflater.inflate(R.layout.dots_list_row, parent, false);

        return new ViewHolder(dots);
    }

    @Override
    public void onBindViewHolder(DotsAdapter.ViewHolder holder, int position) {
        holder.imageView.getDrawable().setTint(dotColor);
    }

    @Override
    public int getItemCount() {
        return dots.size();
    }

    public void addDot(int position) {
        dots.add(new ImageView(activity));
        notifyDataSetChanged();
        notifyItemInserted(position);
    }

    public void removeDot(int position) {
        dots.remove(position);
        notifyDataSetChanged();
        notifyItemRemoved(position);
    }

    public void removeAllItems(){
        int size = getItemCount();

        if (size != 0) {
            for (int i = 0; i < getItemCount(); i++) {
                dots.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.dot)
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}