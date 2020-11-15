package org.netdex.hidfuzzer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LuaAssetAdapter extends RecyclerView.Adapter<LuaAssetAdapter.ViewHolder> {
    public static class LuaAsset {
        private final String name_;
        private final String path_;

        public LuaAsset(String name, String path) {
            this.name_ = name;
            this.path_ = path;
        }

        public String getName() {
            return name_;
        }

        public String getPath() {
            return path_;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public View layout;

        public ViewHolder(View view) {
            super(view);
            this.layout = view;
            textView = view.findViewById(R.id.textView);
        }
    }

    private final ArrayList<LuaAsset> assets_;

    public LuaAssetAdapter(ArrayList<LuaAsset> assets) {
        this.assets_ = assets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.activity_select_asset_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LuaAsset asset = assets_.get(position);
        holder.textView.setText(asset.getName());
        holder.layout.setOnClickListener(v -> onLuaAssetSelected(asset));
    }

    @Override
    public int getItemCount() {
        return assets_.size();
    }

    public void onLuaAssetSelected(LuaAsset asset) {
    }
}
