package com.neeri.wbis.tools;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.neeri.wbis.R;
import java.util.List;

public class ImageAdaptor extends BaseAdapter {

    private List<Uri> imageUris;
    private Context context;
    private LayoutInflater inflater;

    public ImageAdaptor(List<Uri> imageUris, Context context) {
        this.imageUris = imageUris;
        this.context = context;
    }

    @Override
    public int getCount() {
        return imageUris.size();
    }

    @Override
    public Object getItem(int position) {
        return imageUris.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.album_row_item, null);
        }

        ImageView imageView = convertView.findViewById(R.id.row_item_image_view);
        if (imageView != null) {
            Glide.with(context)
                    .load(imageUris.get(position))
                    .centerCrop()
                    .into(imageView);
        }

        return convertView;
    }
}
