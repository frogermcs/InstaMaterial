package io.github.froger.instamaterial;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by froger_mcs on 05.11.14.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    private static final int ANIMATED_ITEMS_COUNT = 2;

    private Context context;
    private int lastAnimatedPosition = -1;
    private int itemsCount = 0;
    private boolean animateItems = false;

    private final Map<Integer, Integer> likesCount = new HashMap<>();
    private final Map<RecyclerView.ViewHolder, AnimatorSet> likeAnimations = new HashMap<>();
    private final ArrayList<Integer> likedPositions = new ArrayList<>();

    private OnFeedItemClickListener onFeedItemClickListener;

    public FeedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        return new CellFeedViewHolder(view);
    }

    private void runEnterAnimation(View view, int position) {
        if (!animateItems || position >= ANIMATED_ITEMS_COUNT - 1) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(Utils.getScreenHeight(context));
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        runEnterAnimation(viewHolder.itemView, position);
        CellFeedViewHolder holder = (CellFeedViewHolder) viewHolder;
        if (position % 2 == 0) {
            holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_1);
            holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_1);
        } else {
            holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_2);
            holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_2);
        }
        updateLikesCounter(holder, false);
        updateHeartButton(holder, false);

        holder.btnComments.setOnClickListener(this);
        holder.btnComments.setTag(position);
        holder.btnMore.setOnClickListener(this);
        holder.btnMore.setTag(position);
        holder.btnLike.setOnClickListener(this);
        holder.btnLike.setTag(holder);

        if (likeAnimations.containsKey(holder)) {
            likeAnimations.get(holder).cancel();
        }
        resetLikeAnimationState(holder);
    }

    @Override
    public int getItemCount() {
        return itemsCount;
    }

    private void updateLikesCounter(CellFeedViewHolder holder, boolean animated) {
        int currentLikesCount = likesCount.get(holder.getPosition()) + 1;
        String likesCountText = context.getResources().getQuantityString(
                R.plurals.likes_count, currentLikesCount, currentLikesCount
        );

        if (animated) {
            holder.tsLikesCounter.setText(likesCountText);
        } else {
            holder.tsLikesCounter.setCurrentText(likesCountText);
        }

        likesCount.put(holder.getPosition(), currentLikesCount);
    }

    private void updateHeartButton(final CellFeedViewHolder holder, boolean animated) {
        if (animated) {
            if (!likeAnimations.containsKey(holder)) {
                AnimatorSet animatorSet = new AnimatorSet();
                likeAnimations.put(holder, animatorSet);

                ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder.btnLike, "rotation", 0f, 360f);
                rotationAnim.setDuration(300);
                rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

                ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder.btnLike, "scaleX", 0.2f, 1f);
                bounceAnimX.setDuration(300);
                bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

                ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder.btnLike, "scaleY", 0.2f, 1f);
                bounceAnimY.setDuration(300);
                bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
                bounceAnimY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        holder.btnLike.setImageResource(R.drawable.ic_heart_red);
                    }
                });

                animatorSet.play(rotationAnim);
                animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        resetLikeAnimationState(holder);
                    }
                });

                animatorSet.start();
            }
        } else {
            if (likedPositions.contains(holder.getPosition())) {
                holder.btnLike.setImageResource(R.drawable.ic_heart_red);
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart_outline_grey);
            }
        }
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.btnComments) {
            if (onFeedItemClickListener != null) {
                onFeedItemClickListener.onCommentsClick(view, (Integer) view.getTag());
            }
        } else if (viewId == R.id.btnMore) {
            if (onFeedItemClickListener != null) {
                onFeedItemClickListener.onMoreClick(view, (Integer) view.getTag());
            }
        } else if (viewId == R.id.btnLike) {
            CellFeedViewHolder holder = (CellFeedViewHolder) view.getTag();
            if (!likedPositions.contains(holder.getPosition())) {
                likedPositions.add(holder.getPosition());
                updateLikesCounter(holder, true);
                updateHeartButton(holder, true);
            }
            }
        }
    }

    private void resetLikeAnimationState(CellFeedViewHolder holder) {
        likeAnimations.remove(holder);
        holder.vBgLike.setVisibility(View.GONE);
        holder.ivLike.setVisibility(View.GONE);
    }

    public void updateItems(boolean animated) {
        itemsCount = 10;
        animateItems = animated;
        fillLikesWithRandomValues();
        notifyDataSetChanged();
    }

    private void fillLikesWithRandomValues() {
        for (int i = 0; i < getItemCount(); i++) {
            likesCount.put(i, new Random().nextInt(100));
        }
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public static class CellFeedViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.ivFeedCenter)
        ImageView ivFeedCenter;
        @InjectView(R.id.ivFeedBottom)
        ImageView ivFeedBottom;
        @InjectView(R.id.btnComments)
        ImageButton btnComments;
        @InjectView(R.id.btnLike)
        ImageButton btnLike;
        @InjectView(R.id.btnMore)
        ImageButton btnMore;
        @InjectView(R.id.vBgLike)
        View vBgLike;
        @InjectView(R.id.ivLike)
        ImageView ivLike;
        @InjectView(R.id.tsLikesCounter)
        TextSwitcher tsLikesCounter;

        public CellFeedViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    public interface OnFeedItemClickListener {
        public void onCommentsClick(View v, int position);

        public void onMoreClick(View v, int position);
    }
}
