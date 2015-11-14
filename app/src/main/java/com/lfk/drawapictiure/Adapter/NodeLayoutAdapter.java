package com.lfk.drawapictiure.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lfk.drawapictiure.Info.NodeInfo;
import com.lfk.drawapictiure.InterFace.ItemLongClickListener;
import com.lfk.drawapictiure.InterFace.NodeItemClickListener;
import com.lfk.drawapictiure.R;

import java.util.ArrayList;

/**
 * Created by liufengkai on 15/9/15.
 */
public class NodeLayoutAdapter extends RecyclerView.Adapter<NodeLayoutAdapter.NodeViewHolder> {
    private LayoutInflater inflater;
    private ArrayList<NodeInfo> userList;
    private Context context;
    private NodeItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;

    public NodeLayoutAdapter(ArrayList<NodeInfo> userList, Context context) {
        this.userList = userList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    public void setItemClickListener(NodeItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public NodeLayoutAdapter.NodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View wrapper = inflater.inflate(R.layout.node_item,parent,false);
        return new NodeViewHolder(wrapper,
                (TextView)wrapper.findViewById(R.id.node_time),
                (TextView)wrapper.findViewById(R.id.node_root),
                (TextView)wrapper.findViewById(R.id.node_content),
                (TextView)wrapper.findViewById(R.id.node_min));
    }

    @Override
    public void onBindViewHolder(NodeLayoutAdapter.NodeViewHolder holder, int position) {
//        ViewGroup.LayoutParams lp = holder.nodeContent.getLayoutParams();
//        lp.height = (int) (100 + Math.random() * 300);
        NodeInfo nodeInfo = userList.get(position);
        holder.nodeTime.setText(nodeInfo.getNodeTime());
        holder.nodeContent.setText(nodeInfo.getNodeContent());
        holder.nodeName.setText(nodeInfo.getNodeName());
        holder.nodeTimeMin.setText(nodeInfo.getNodeTimeMin());
    }

    public void remove(int position){
        userList.remove(position);
        notifyItemRemoved(position);
    }

    public void add(NodeInfo info){
        userList.add(info);
        notifyItemInserted(userList.size());
    }
    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class NodeViewHolder extends RecyclerView.ViewHolder implements
            View.OnLongClickListener,View.OnClickListener{
        private TextView nodeTime;
        private TextView nodeName;
        private TextView nodeContent;
        private TextView nodeTimeMin;

        public NodeViewHolder(View itemView, TextView nodeTime, TextView nodeName,
                              TextView nodeContent, TextView nodeTimeMin) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.nodeTime = nodeTime;
            this.nodeName = nodeName;
            this.nodeContent = nodeContent;
            this.nodeTimeMin = nodeTimeMin;
        }

        @Override
        public void onClick(View view) {
            NodeInfo nodeInfo = userList.get(getAdapterPosition());
            itemClickListener.onItemClick(nodeInfo.getNodeName(),nodeInfo.getNodeTime(),
                    nodeInfo.getNodeTimeMin(),nodeInfo.getNodeContent());
            nodeInfo = null;
        }

        @Override
        public boolean onLongClick(View view) {
            NodeInfo nodeInfo = userList.get(getAdapterPosition());
            itemLongClickListener.onLongItemClick(nodeInfo.getNodeName(),getAdapterPosition());
            nodeInfo = null;
            return false;
        }
    }
}
