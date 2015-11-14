package com.lfk.drawapictiure.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lfk.drawapictiure.Info.MenuInfo;
import com.lfk.drawapictiure.InterFace.ItemLongClickListener;
import com.lfk.drawapictiure.InterFace.PaintItemClickListener;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.ImageLoader;
import com.lfk.drawapictiure.Tools.PicTools;

import java.util.ArrayList;

/**
 * Created by liufengkai on 15/9/14.
 */
public class MainLayoutAdapter extends RecyclerView.Adapter<MainLayoutAdapter.MainViewHolder> {
    private LayoutInflater inflater;
    private ArrayList<MenuInfo> userList;
    private Context context;
    private PaintItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;
    private ImageLoader mImageLoader;

    public MainLayoutAdapter(ArrayList<MenuInfo> userList, Context context) {
        this.userList = userList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        mImageLoader = ImageLoader.getInstance(3, ImageLoader.Type.LIFO);

    }

    public void setItemClickListener(PaintItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    @Override
    public MainLayoutAdapter.MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View wrapper = inflater.inflate(R.layout.draw_item, parent, false);
        return new MainViewHolder(
                wrapper,
                (TextView) wrapper.findViewById(R.id.paint_name),
                (TextView) wrapper.findViewById(R.id.paint_time),
                (TextView) wrapper.findViewById(R.id.paint_root),
                (ImageView) wrapper.findViewById(R.id.paint_img));
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        MenuInfo menuInfo = userList.get(position);
//        ThumbnailUtils.extractThumbnail(
//                BitmapFactory.decodeFile(menuInfo.getPaint_img_root()),
//                dip2px(100), dip2px(100),ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
        holder.paint_name.setText(menuInfo.getPaint_name());
        holder.paint_time.setText(menuInfo.getPaint_time());
        holder.paint_root.setText(menuInfo.getPaint_root());
        holder.paint_img.setImageBitmap(PicTools.stringToBitmap(menuInfo.getPaint_img_root()));
//        mImageLoader.loadImage(menuInfo.getPaint_name(),
//                PicTools.stringToBitmap(menuInfo.getPaint_img_root()),
//                holder.paint_img);
    }


    public int dip2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView paint_name;
        private TextView paint_time;
        private TextView paint_root;
        private ImageView paint_img;

        public MainViewHolder(View itemView, TextView paint_name,
                              TextView paint_time, TextView paint_root,
                              ImageView paint_img) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.paint_name = paint_name;
            this.paint_time = paint_time;
            this.paint_root = paint_root;
            this.paint_img = paint_img;
        }

        @Override
        public void onClick(View view) {
            MenuInfo menuInfo = userList.get(getAdapterPosition());
            itemClickListener.onItemClick(menuInfo.getPaint_name(), menuInfo.getPaint_content(), menuInfo.getPaint_root());
            menuInfo = null;
        }

        @Override
        public boolean onLongClick(View view) {
            MenuInfo menuInfo = userList.get(getAdapterPosition());
            itemLongClickListener.onLongItemClick(menuInfo.getPaint_name(), getAdapterPosition());
            menuInfo = null;
            return false;
        }
    }

    public void remove(int position) {
        userList.remove(position);
        notifyItemRemoved(position);
    }

    public void add(MenuInfo info) {
        userList.add(info);
        notifyItemInserted(userList.size());
    }

}
