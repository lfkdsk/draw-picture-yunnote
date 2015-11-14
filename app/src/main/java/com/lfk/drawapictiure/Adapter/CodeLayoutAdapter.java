package com.lfk.drawapictiure.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lfk.drawapictiure.Info.CodeInfo;
import com.lfk.drawapictiure.InterFace.CodeItemClickListener;
import com.lfk.drawapictiure.InterFace.ItemLongClickListener;
import com.lfk.drawapictiure.R;

import java.util.ArrayList;

/**
 * Created by liufengkai on 15/9/15.
 */
public class CodeLayoutAdapter extends RecyclerView.Adapter<CodeLayoutAdapter.CodeViewHolder> {
    private LayoutInflater inflater;
    private Context context;
    private ArrayList<CodeInfo> userList;
    private CodeItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;

    public CodeLayoutAdapter(ArrayList<CodeInfo> userList, Context context) {
        this.userList = userList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setItemClickListener(CodeItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    @Override
    public CodeLayoutAdapter.CodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View wrapper = inflater.inflate(R.layout.code_item,parent,false);
        return new CodeViewHolder(wrapper,
                (TextView)wrapper.findViewById(R.id.code_name),
                (TextView)wrapper.findViewById(R.id.code_content),
                (TextView)wrapper.findViewById(R.id.code_root));
    }

    @Override
    public void onBindViewHolder(CodeLayoutAdapter.CodeViewHolder holder, int position) {
//        ViewGroup.LayoutParams lp = holder.codeContent.getLayoutParams();
//        lp.height = (int) (100 + Math.random() * 300);
        CodeInfo codeInfo = userList.get(position);
        holder.codeName.setText(codeInfo.getCodeName());
        holder.codeContent.setText(codeInfo.getCodeContent());
        holder.codeRoot.setText(codeInfo.getCodeRoot());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
    public void remove(int position){
        userList.remove(position);
        notifyItemRemoved(position);
    }

    public void add(CodeInfo info){
        userList.add(info);
        notifyItemInserted(userList.size());
    }
    public class CodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        private TextView codeName;
        private TextView codeContent;
        private TextView codeRoot;

        public CodeViewHolder(View itemView, TextView codeName,
                              TextView codeContent, TextView codeRoot) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.codeName = codeName;
            this.codeContent = codeContent;
            this.codeRoot = codeRoot;
        }

        @Override
        public void onClick(View view) {
            CodeInfo codeInfo = userList.get(getAdapterPosition());
            itemClickListener.onItemClick(codeInfo.getCodeName(),codeInfo.getCodeContent(),codeInfo.getCodeRoot());
            codeInfo = null;
        }

        @Override
        public boolean onLongClick(View view) {
            CodeInfo codeInfo = userList.get(getAdapterPosition());
            itemLongClickListener.onLongItemClick(codeInfo.getCodeName(),getAdapterPosition());
            codeInfo = null;
            return false;
        }
    }
}
