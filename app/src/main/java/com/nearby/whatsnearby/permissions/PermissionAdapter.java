package com.nearby.whatsnearby.permissions;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nearby.whatsnearby.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rudhraksh.pahade on 1/31/2017.
 */

public class PermissionAdapter extends BaseAdapter {

    private Activity mActivity;
    private LayoutInflater mLayoutInflater;

    private List<PermHeaderDto> headerData = new ArrayList<>();
    private List<PermChildDto> childData = new ArrayList<>();

    public PermissionAdapter(Activity activity) {
        this.mActivity = activity;
        this.mLayoutInflater = LayoutInflater.from(mActivity);
        this.initPermissionDialogData();
    }

    private void initPermissionDialogData() {

        // Drawable resource ids
        int[] drawableImages = new int[]{
                R.drawable.ic_location_perm_24dp,
                R.drawable.ic_call_phone_perm_24dp,
                R.drawable.ic_media_perm_24dp,
                R.drawable.ic_internet_wifi_perm_24dp,

        };
        // Permission Name
        String[] permissionNames = new String[]{
                "Location",
                "Phone",
                "Photos/Media/Files",
                "Wi-Fi Connection Information",
        };
        // Permission Description
        String[] permissionDescription = new String[]{
                mActivity.getResources().getString(R.string.perm_location),
                mActivity.getResources().getString(R.string.perm_phone),
                mActivity.getResources().getString(R.string.perm_storage),
                mActivity.getResources().getString(R.string.perm_wifi_connection)
        };


        for (int i = 0; i < drawableImages.length; i++) {
            PermHeaderDto headerDto = new PermHeaderDto();
            PermChildDto childDto = new PermChildDto();

            headerDto.setResId(drawableImages[i]);
            headerDto.setPermName(permissionNames[i]);

            childDto.setPermDesc(permissionDescription[i]);

            this.headerData.add(headerDto);
            this.childData.add(childDto);
        }
    }

    @Override
    public int getCount() {
        return headerData.size();
    }

    @Override
    public Object getItem(int position) {
        return headerData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PermHeaderDto permHeaderDto = headerData.get(position);
        final PermChildDto permChildDto = childData.get(position);
        final DialogViewHolder dialogViewHolder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.perm_header_row, null);
            dialogViewHolder = new DialogViewHolder();
            dialogViewHolder.imgVwPermImage = (ImageView) convertView.findViewById(R.id.imgVwPermImage);
            dialogViewHolder.tvPermName = (TextView) convertView.findViewById(R.id.tvPermName);
            dialogViewHolder.tvPermDesc = (TextView) convertView.findViewById(R.id.tvPermDesc);
            dialogViewHolder.imgVwShowMore = (ImageView) convertView.findViewById(R.id.imgVwShowMore);
            dialogViewHolder.lChildView = (LinearLayout) convertView.findViewById(R.id.lChildView);
            // Don't show permission description
            dialogViewHolder.lChildView.setVisibility(View.GONE);
            convertView.setTag(dialogViewHolder);
        } else {
            dialogViewHolder = (DialogViewHolder) convertView.getTag();
        }

        // Setting values
        dialogViewHolder.imgVwPermImage.setImageResource(permHeaderDto.getResId());
        dialogViewHolder.tvPermName.setText(permHeaderDto.getPermName());
        dialogViewHolder.tvPermDesc.setText(permChildDto.getPermDesc());

        // If clicked on show more button, then expand the child item
        dialogViewHolder.imgVwShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogViewHolder.lChildView.getVisibility() == View.GONE) {
                    dialogViewHolder.lChildView.setVisibility(View.VISIBLE);
                } else if (dialogViewHolder.lChildView.getVisibility() == View.VISIBLE) {
                    dialogViewHolder.lChildView.setVisibility(View.GONE);
                }
            }
        });

        return convertView;
    }

    private static class DialogViewHolder {
        ImageView imgVwPermImage;
        TextView tvPermName;
        TextView tvPermDesc;
        ImageView imgVwShowMore;
        LinearLayout lChildView;
    }
}
