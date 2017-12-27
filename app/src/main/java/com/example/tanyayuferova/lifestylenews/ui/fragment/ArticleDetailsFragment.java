package com.example.tanyayuferova.lifestylenews.ui.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.tanyayuferova.lifestylenews.R;
import com.example.tanyayuferova.lifestylenews.analitics.AnalyticsApplication;
import com.example.tanyayuferova.lifestylenews.data.ArticlesContract;
import com.example.tanyayuferova.lifestylenews.databinding.FragmentArticleDetailsBinding;
import com.example.tanyayuferova.lifestylenews.entity.Article;
import com.example.tanyayuferova.lifestylenews.utils.DataUtils;
import com.example.tanyayuferova.lifestylenews.utils.PaletteUtils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;

/**
 * Created by Tanya Yuferova on 12/17/2017.
 */

public class ArticleDetailsFragment extends Fragment {

    private FragmentArticleDetailsBinding binding;
    protected Tracker tracker;
    private static final int DEFAULT_MUTED_DARK_COLOR = 0xFF333333;
    public static final String ARGUMENT_ARTICLE = "arg.article";

    public ArticleDetailsFragment() {
    }

    public static ArticleDetailsFragment newInstance(Article article) {
        ArticleDetailsFragment fragment = new ArticleDetailsFragment();
        fragment.setArguments(new Bundle());
        fragment.getArguments().putParcelable(ARGUMENT_ARTICLE, article);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Article article = getArguments().getParcelable(ARGUMENT_ARTICLE);
        binding = FragmentArticleDetailsBinding.inflate(inflater, container, false);
        binding.tvSource.setMovementMethod(LinkMovementMethod.getInstance());
        binding.fabAddToFavorite.setImageDrawable(getContext().getResources()
                .getDrawable(article.isFavorite() ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_empty));
        initNavigationOnClickListener();
        initImageOnLayoutChangeListener();
        initShareOnClickListener();
        initAddToFavoriteOnClickListener();
        binding.setArticle(article);
        binding.setContext(getContext());

        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        tracker = application.getDefaultTracker();

        return binding.getRoot();
    }

    protected void initNavigationOnClickListener() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    protected void initShareOnClickListener() {
        binding.fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(binding.getArticle().getTitle())
                        .getIntent(), getString(R.string.action_share)));

                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction(getString(R.string.action_share))
                        .build());
            }
        });
    }

    protected void initAddToFavoriteOnClickListener() {
        binding.fabAddToFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean favorite = !binding.getArticle().isFavorite();

                binding.fabAddToFavorite.setImageDrawable(getContext().getResources()
                        .getDrawable(favorite ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_empty));
                binding.getArticle().setFavorite(favorite);
                binding.getArticle().setAddedToFavored(Calendar.getInstance().getTime());

                ContentResolver contentResolver = getContext().getContentResolver();
                contentResolver.update(
                        ArticlesContract.CONTENT_RECENT_URI,
                        DataUtils.getContentValues(binding.getArticle()),
                        ArticlesContract.ArticleEntry._ID + " = ?",
                        new String[]{String.valueOf(binding.getArticle().getId())}
                );

                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction(getString(R.string.action_add_to_favorite))
                        .build());
            }
        });
    }

    protected void initImageOnLayoutChangeListener() {
        binding.ivImage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // When image is change set scrim color generated from image palette
                if (v instanceof ImageView && ((ImageView) v).getDrawable() != null) {
                    Drawable drawable = ((ImageView) v).getDrawable();
                    if (drawable instanceof BitmapDrawable) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) ((ImageView) v).getDrawable();
                        binding.collapsingToolbar.setContentScrimColor(PaletteUtils.getDarkMutedColor(bitmapDrawable.getBitmap(), DEFAULT_MUTED_DARK_COLOR));
                    } else {
                        binding.collapsingToolbar.setContentScrimColor(getResources().getColor(R.color.colorPrimaryDark));
                    }
                }
            }
        });
    }
}
