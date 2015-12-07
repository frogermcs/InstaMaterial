package io.github.froger.instamaterial.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.Utils;
import io.github.froger.instamaterial.ui.activity.MainActivity;
import io.github.froger.instamaterial.ui.view.SendingProgressView;

/**
 * Created by froger_mcs on 05.11.14.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    public static final String ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button";

    public static final int VIEW_TYPE_DEFAULT = 1;
    public static final int VIEW_TYPE_LOADER = 2;

    private final List<FeedItem> feedItems = new ArrayList<>();
    private Context context;
    private OnFeedItemClickListener onFeedItemClickListener;

    private boolean showLoadingView = false;
    private int loadingViewSize = Utils.dpToPx(200);

    public FeedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        final CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);
        if (viewType == VIEW_TYPE_DEFAULT) {
            setupClickableViews(view, cellFeedViewHolder);
        } else if (viewType == VIEW_TYPE_LOADER) {
            View bgView = new View(context);
            bgView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ));
            bgView.setBackgroundColor(0x77ffffff);
            cellFeedViewHolder.vImageRoot.addView(bgView);
            cellFeedViewHolder.vProgressBg = bgView;

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(loadingViewSize, loadingViewSize);
            params.gravity = Gravity.CENTER;
            SendingProgressView sendingProgressView = new SendingProgressView(context);
            sendingProgressView.setLayoutParams(params);
            cellFeedViewHolder.vImageRoot.addView(sendingProgressView);
            cellFeedViewHolder.vSendingProgress = sendingProgressView;
        }

        return cellFeedViewHolder;
    }

    private void setupClickableViews(final View view, final CellFeedViewHolder cellFeedViewHolder) {
        cellFeedViewHolder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onMoreClick(v, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.ivFeedCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                feedItems.get(adapterPosition).likesCount++;
                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showLikedSnackbar();
                }
            }
        });
        cellFeedViewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                feedItems.get(adapterPosition).likesCount++;
                notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showLikedSnackbar();
                }
            }
        });
        cellFeedViewHolder.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onProfileClick(view);
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final CellFeedViewHolder holder = (CellFeedViewHolder) viewHolder;
        if (getItemViewType(position) == VIEW_TYPE_DEFAULT) {
            holder.bindView(feedItems.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_LOADER) {
            bindLoadingFeedItem(holder);
        }
    }

    private void bindLoadingFeedItem(final CellFeedViewHolder holder) {
        holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_1);
        holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_1);
        holder.vSendingProgress.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.vSendingProgress.getViewTreeObserver().removeOnPreDrawListener(this);
                holder.vSendingProgress.simulateProgress();
                return true;
            }
        });
        holder.vSendingProgress.setOnLoadingFinishedListener(new SendingProgressView.OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                holder.vSendingProgress.animate().scaleY(0).scaleX(0).setDuration(200).setStartDelay(100);
                holder.vProgressBg.animate().alpha(0.f).setDuration(200).setStartDelay(100)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                holder.vSendingProgress.setScaleX(1);
                                holder.vSendingProgress.setScaleY(1);
                                holder.vProgressBg.setAlpha(1);
                                showLoadingView = false;
                                notifyItemChanged(0);
                            }
                        })
                        .start();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoadingView && position == 0) {
            return VIEW_TYPE_LOADER;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public void updateItems(boolean animated) {
        feedItems.clear();
        feedItems.addAll(Arrays.asList(
                new FeedItem(33, false),
                new FeedItem(1, false),
                new FeedItem(223, false),
                new FeedItem(2, false),
                new FeedItem(6, false),
                new FeedItem(8, false),
                new FeedItem(99, false)
        ));
        if (animated) {
            notifyItemRangeInserted(0, feedItems.size());
        } else {
            notifyDataSetChanged();
        }
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public void showLoadingView() {
        showLoadingView = true;
        notifyItemChanged(0);
    }

    public static class CellFeedViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ivFeedCenter)
        ImageView ivFeedCenter;
        @Bind(R.id.ivFeedBottom)
        ImageView ivFeedBottom;
        @Bind(R.id.btnComments)
        ImageButton btnComments;
        @Bind(R.id.btnLike)
        ImageButton btnLike;
        @Bind(R.id.btnMore)
        ImageButton btnMore;
        @Bind(R.id.vBgLike)
        View vBgLike;
        @Bind(R.id.ivLike)
        ImageView ivLike;
        @Bind(R.id.tsLikesCounter)
        TextSwitcher tsLikesCounter;
        @Bind(R.id.ivUserProfile)
        ImageView ivUserProfile;
        @Bind(R.id.vImageRoot)
        FrameLayout vImageRoot;

        SendingProgressView vSendingProgress;
        View vProgressBg;

        FeedItem feedItem;

        public CellFeedViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindView(FeedItem feedItem) {
            this.feedItem = feedItem;
            final int adapterPosition = getAdapterPosition();
            if (adapterPosition % 2 == 0) {
                ivFeedCenter.setImageResource(R.drawable.img_feed_center_1);
                ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_1);
            } else {
                ivFeedCenter.setImageResource(R.drawable.img_feed_center_2);
                ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_2);
            }

            tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                    R.plurals.likes_count, feedItem.likesCount, feedItem.likesCount
            ));
            btnLike.setImageResource(feedItem.isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline_grey);
        }

        public FeedItem getFeedItem() {
            return feedItem;
        }
    }

    public static class FeedItem {
        public int likesCount;
        public boolean isLiked;

        public FeedItem(int likesCount, boolean isLiked) {
            this.likesCount = likesCount;
            this.isLiked = isLiked;
        }
    }

    public interface OnFeedItemClickListener {
        public void onCommentsClick(View v, int position);

        public void onMoreClick(View v, int position);

        public void onProfileClick(View v);
    }
}
