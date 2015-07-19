package io.github.froger.instamaterial.ui.adapter;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.Utils;
import io.github.froger.instamaterial.ui.utils.CircleTransformation;

/**
 * Created by Miroslaw Stanek on 20.01.15.
 */
public class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_PROFILE_HEADER = 0;
    public static final int TYPE_PROFILE_OPTIONS = 1;
    public static final int TYPE_PHOTO = 2;

    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final int MAX_PHOTO_ANIMATION_DELAY = 600;

    private static final int MIN_ITEMS_COUNT = 2;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();


    private final Context context;
    private final int cellSize;
    private final int avatarSize;

    private final String profilePhoto;
    private final List<String> photos;

    private boolean lockedAnimations = false;
    private long profileHeaderAnimationStartTime = 0;
    private int lastAnimatedItem = 0;

    public UserProfileAdapter(Context context) {
        this.context = context;
        this.cellSize = Utils.getScreenWidth(context) / 3;
        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.user_profile_avatar_size);
        this.profilePhoto = context.getString(R.string.user_profile_photo);
        this.photos = Arrays.asList(context.getResources().getStringArray(R.array.user_photos));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_PROFILE_HEADER;
        } else if (position == 1) {
            return TYPE_PROFILE_OPTIONS;
        } else {
            return TYPE_PHOTO;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_PROFILE_HEADER == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_user_profile_header, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);
            view.setLayoutParams(layoutParams);
            return new ProfileHeaderViewHolder(view);
        } else if (TYPE_PROFILE_OPTIONS == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_user_profile_options, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);
            view.setLayoutParams(layoutParams);
            return new ProfileOptionsViewHolder(view);
        } else if (TYPE_PHOTO == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.height = cellSize;
            layoutParams.width = cellSize;
            layoutParams.setFullSpan(false);
            view.setLayoutParams(layoutParams);
            return new PhotoViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (TYPE_PROFILE_HEADER == viewType) {
            bindProfileHeader((ProfileHeaderViewHolder) holder);
        } else if (TYPE_PROFILE_OPTIONS == viewType) {
            bindProfileOptions((ProfileOptionsViewHolder) holder);
        } else if (TYPE_PHOTO == viewType) {
            bindPhoto((PhotoViewHolder) holder, position);
        }
    }

    private void bindProfileHeader(final ProfileHeaderViewHolder holder) {
        Picasso.with(context)
                .load(profilePhoto)
                .placeholder(R.drawable.img_circle_placeholder)
                .resize(avatarSize, avatarSize)
                .centerCrop()
                .transform(new CircleTransformation())
                .into(holder.ivUserProfilePhoto);
        holder.vUserProfileRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.vUserProfileRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                animateUserProfileHeader(holder);
                return false;
            }
        });
    }

    private void bindProfileOptions(final ProfileOptionsViewHolder holder) {
        holder.tlUserProfileTabs.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.tlUserProfileTabs.getViewTreeObserver().removeOnPreDrawListener(this);
                animateUserProfileOptions(holder);
                return false;
            }
        });
    }

    private void bindPhoto(final PhotoViewHolder holder, int position) {
        Picasso.with(context)
                .load(photos.get(position - MIN_ITEMS_COUNT))
                .resize(cellSize, cellSize)
                .centerCrop()
                .into(holder.ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        animatePhoto(holder);
                    }

                    @Override
                    public void onError() {

                    }
                });
        if (lastAnimatedItem < position) lastAnimatedItem = position;
    }

    private void animateUserProfileHeader(ProfileHeaderViewHolder viewHolder) {
        if (!lockedAnimations) {
            profileHeaderAnimationStartTime = System.currentTimeMillis();

            viewHolder.vUserProfileRoot.setTranslationY(-viewHolder.vUserProfileRoot.getHeight());
            viewHolder.ivUserProfilePhoto.setTranslationY(-viewHolder.ivUserProfilePhoto.getHeight());
            viewHolder.vUserDetails.setTranslationY(-viewHolder.vUserDetails.getHeight());
            viewHolder.vUserStats.setAlpha(0);

            viewHolder.vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
            viewHolder.ivUserProfilePhoto.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
            viewHolder.vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
            viewHolder.vUserStats.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();
        }
    }

    private void animateUserProfileOptions(ProfileOptionsViewHolder viewHolder) {
        if (!lockedAnimations) {
            viewHolder.tlUserProfileTabs.setTranslationY(-viewHolder.tlUserProfileTabs.getHeight());
            viewHolder.tlUserProfileTabs.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
        }
    }

    private void animatePhoto(PhotoViewHolder viewHolder) {
        if (!lockedAnimations) {
            if (lastAnimatedItem == viewHolder.getPosition()) {
                setLockedAnimations(true);
            }

            long animationDelay = profileHeaderAnimationStartTime + MAX_PHOTO_ANIMATION_DELAY - System.currentTimeMillis();
            if (profileHeaderAnimationStartTime == 0) {
                animationDelay = viewHolder.getPosition() * 30 + MAX_PHOTO_ANIMATION_DELAY;
            } else if (animationDelay < 0) {
                animationDelay = viewHolder.getPosition() * 30;
            } else {
                animationDelay += viewHolder.getPosition() * 30;
            }

            viewHolder.flRoot.setScaleY(0);
            viewHolder.flRoot.setScaleX(0);
            viewHolder.flRoot.animate()
                    .scaleY(1)
                    .scaleX(1)
                    .setDuration(200)
                    .setInterpolator(INTERPOLATOR)
                    .setStartDelay(animationDelay)
                    .start();
        }
    }

    @Override
    public int getItemCount() {
        return MIN_ITEMS_COUNT + photos.size();
    }

    static class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.ivUserProfilePhoto)
        ImageView ivUserProfilePhoto;
        @InjectView(R.id.vUserDetails)
        View vUserDetails;
        @InjectView(R.id.btnFollow)
        Button btnFollow;
        @InjectView(R.id.vUserStats)
        View vUserStats;
        @InjectView(R.id.vUserProfileRoot)
        View vUserProfileRoot;

        public ProfileHeaderViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    static class ProfileOptionsViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.tlUserProfileTabs)
        TabLayout tlUserProfileTabs;

        public ProfileOptionsViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_grid_on_white));
            tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_list_white));
            tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_place_white));
            tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_label_white));
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.flRoot)
        FrameLayout flRoot;
        @InjectView(R.id.ivPhoto)
        ImageView ivPhoto;

        public PhotoViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    public void setLockedAnimations(boolean lockedAnimations) {
        this.lockedAnimations = lockedAnimations;
    }
}
