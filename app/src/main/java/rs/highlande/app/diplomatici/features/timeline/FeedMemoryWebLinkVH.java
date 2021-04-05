/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.timeline;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;

import rs.highlande.app.diplomatici.R;
import rs.highlande.app.diplomatici.features.HomeActivity;
import rs.highlande.app.diplomatici.features.webView.CommonWebViewActivity;
import rs.highlande.app.diplomatici.models.Post;
import rs.highlande.app.diplomatici.models.enums.PostTypeEnum;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.LogUtils;
import rs.highlande.app.diplomatici.utilities.Utils;
import rs.highlande.app.diplomatici.utilities.listeners.ResizingTextWatcher;
import rs.highlande.app.diplomatici.utilities.media.MediaHelper;
import rs.highlande.app.diplomatici.widgets.RoundedCornersBackgroundSpan;

/**
 * @author mbaldrighi on 9/28/2017.
 */
class FeedMemoryWebLinkVH extends FeedMemoryViewHolder {

	public static final String LOG_TAG = FeedMemoryWebLinkVH.class.getCanonicalName();

	private static RoundedCornersBackgroundSpan backgrSpan;
	private static RoundedCornersBackgroundSpan backgrSpanAlpha;

	private ImageView mainView;
	private TextView webLinkTitle, webLinkSource;
	private View webLinkSourceIcon;


	FeedMemoryWebLinkVH(View view, TimelineFragment fragment,
						final OnTimelineFragmentInteractionListener mListener) {
		super(view, fragment, mListener);

		mainView = (ImageView) mMainView;

		webLinkTitle = super.maskLower.findViewById(R.id.weblink_title);
		webLinkTitle.setOnClickListener(this);
		webLinkSource = super.maskLower.findViewById(R.id.weblink_source);
		webLinkSource.setOnClickListener(this);
		webLinkSourceIcon = super.maskLower.findViewById(R.id.weblink_source_icon);

		if (backgrSpan == null) {
			backgrSpan = new RoundedCornersBackgroundSpan(
					Utils.getColor(getActivity(), R.color.dipl_orange_alpha_dark),
					10,
					0
			);
		}
		if (backgrSpanAlpha == null) {
			backgrSpanAlpha = new RoundedCornersBackgroundSpan(
					Utils.getColor(getActivity(), R.color.dipl_orange_alpha_med),
					10,
					0
			);
		}
	}


	public void onBindViewHolder(@NonNull Post object) {
		super.onBindViewHolder(object);

		LogUtils.d(LOG_TAG, "onBindViewHolder called for object: " + object.hashCode());

		if (mainView != null && mainView.getContext() != null && super.mItem != null) {
			String url = Utils.isStringValid(super.mItem.getWebLinkImage()) ? super.mItem.getWebLinkImage() : Constants.WEB_LINK_PLACEHOLDER_URL;
			MediaHelper.loadPictureWithGlide(mainView.getContext(),
					url, null, 0, 0, new CustomViewTarget<ImageView, Drawable>(mainView) {
						@Override
						protected void onResourceCleared(@Nullable Drawable placeholder) {
							MediaHelper.loadPictureWithGlide(mainView.getContext(), Constants.WEB_LINK_PLACEHOLDER_URL, getView());
						}

						@Override
						public void onLoadFailed(@Nullable Drawable errorDrawable) {
							MediaHelper.loadPictureWithGlide(mainView.getContext(), Constants.WEB_LINK_PLACEHOLDER_URL, getView());
						}

						@Override
						public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
							mainView.setImageDrawable(resource);
						}
					});

			if (Utils.isStringValid(mItem.getWebLinkTitle())) {
				webLinkTitle.setShadowLayer(10f, 0, 0, Color.TRANSPARENT);
				webLinkTitle.setPadding(10, 10, 10, 10);
				webLinkTitle.setLineSpacing(2, 1);

				Spannable spanned = new SpannableString(mItem.getWebLinkTitle());
				spanned.setSpan(
						backgrSpan,
						0,
						mItem.getWebLinkTitle().length(),
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE
				);
				webLinkTitle.setText(spanned);
				ResizingTextWatcher.resizeTextInView(webLinkTitle);
			}
			else
				webLinkTitle.setText("");

			if (Utils.isStringValid(mItem.getWebLinkSource())) {
				webLinkSource.setShadowLayer(10f, 0, 0, Color.TRANSPARENT);
				webLinkSource.setPadding(5, 5, 5, 5);
				webLinkSource.setLineSpacing(2, 1);

				Spannable spanned = new SpannableString(mItem.getWebLinkSource());
				spanned.setSpan(
						backgrSpanAlpha,
						0,
						mItem.getWebLinkSource().length(),
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE
				);
				webLinkSource.setText(spanned);
			}
			else
				webLinkSource.setText("");

			webLinkSourceIcon.setVisibility(mItem.getTypeEnum() == PostTypeEnum.WEB_LINK ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (canProcessEvents) {
			switch (view.getId()) {
				case R.id.weblink_title:
				case R.id.weblink_source:

					if (!Utils.checkAndOpenLogin(getActivity(), getActivity().getUser(), HomeActivity.PAGER_ITEM_GLOBAL_SEARCH)) {
						mListener.saveFullScreenState();
						openWebView();
					}
					break;
			}
		}
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (super.onSingleTapConfirmed(e)) {
			mListener.saveFullScreenState();
			openWebView();
			return true;
		}
		return false;
	}


	private void openWebView() {
		mListener.setLastAdapterPosition(itemPosition);

		if (mItem.getTypeEnum() == PostTypeEnum.WEB_LINK) {
			Utils.fireBrowserIntentWithShare(
					webLinkSource.getContext(),
					mItem.getWebLinkUrl(),
					mItem.getAuthor(),
					mItem.getId(),
					false
			);
		} else if (mItem.getTypeEnum() == PostTypeEnum.WEB_LINK_CUSTOM) {
			CommonWebViewActivity.openCommonWebView(
					webLinkSource.getContext(),
					null,
					mItem.getAuthorId(),
					mItem.getWebLinkUrl(),
					R.anim.slide_in_right,
					R.anim.no_animation
			);
		}
	}

}