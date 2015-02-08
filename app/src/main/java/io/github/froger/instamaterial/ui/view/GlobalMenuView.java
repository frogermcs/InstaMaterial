package io.github.froger.instamaterial.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import io.github.froger.instamaterial.ui.utils.CircleTransformation;
import io.github.froger.instamaterial.ui.adapter.GlobalMenuAdapter;
import io.github.froger.instamaterial.R;

/**
 * Created by Miroslaw Stanek on 30.01.15.
 */
public class GlobalMenuView extends ListView implements View.OnClickListener {

    private OnHeaderClickListener onHeaderClickListener;
    private GlobalMenuAdapter globalMenuAdapter;
    private ImageView ivUserProfilePhoto;
    private int avatarSize;
    private String profilePhoto;

    public GlobalMenuView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setChoiceMode(CHOICE_MODE_SINGLE);
        setDivider(getResources().getDrawable(android.R.color.transparent));
        setDividerHeight(0);
        setBackgroundColor(Color.WHITE);

        setupHeader();
        setupAdapter();
    }

    private void setupAdapter() {
        globalMenuAdapter = new GlobalMenuAdapter(getContext());
        setAdapter(globalMenuAdapter);
    }

    private void setupHeader() {
        this.avatarSize = getResources().getDimensionPixelSize(R.dimen.global_menu_avatar_size);
        this.profilePhoto = getResources().getString(R.string.user_profile_photo);

        setHeaderDividersEnabled(true);
        View vHeader = LayoutInflater.from(getContext()).inflate(R.layout.view_global_menu_header, null);
        ivUserProfilePhoto = (ImageView) vHeader.findViewById(R.id.ivUserProfilePhoto);
        Picasso.with(getContext())
                .load(profilePhoto)
                .placeholder(R.drawable.img_circle_placeholder)
                .resize(avatarSize, avatarSize)
                .centerCrop()
                .transform(new CircleTransformation())
                .into(ivUserProfilePhoto);
        addHeaderView(vHeader);
        vHeader.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (onHeaderClickListener != null) {
            onHeaderClickListener.onGlobalMenuHeaderClick(v);
        }
    }

    public interface OnHeaderClickListener {
        public void onGlobalMenuHeaderClick(View v);
    }

    public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener) {
        this.onHeaderClickListener = onHeaderClickListener;
    }
}