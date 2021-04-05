/*
 * Copyright (c) 2017. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.timeline;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;

import rs.highlande.app.diplomatici.R;
import rs.highlande.app.diplomatici.features.HomeActivity;
import rs.highlande.app.diplomatici.features.profile.ProfileActivity;
import rs.highlande.app.diplomatici.features.profile.ProfileHelper;
import rs.highlande.app.diplomatici.features.viewers.PhotoViewActivity;
import rs.highlande.app.diplomatici.models.Post;
import rs.highlande.app.diplomatici.models.enums.PostTypeEnum;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.LogUtils;
import rs.highlande.app.diplomatici.utilities.Utils;
import rs.highlande.app.diplomatici.utilities.caches.PicturesCache;
import rs.highlande.app.diplomatici.utilities.media.GlideApp;
import rs.highlande.app.diplomatici.utilities.media.GlideRequest;
import rs.highlande.app.diplomatici.utilities.media.GlideRequests;
import rs.highlande.app.diplomatici.utilities.media.HLMediaType;

/**
 * @author mbaldrighi on 9/28/2017.
 */
class FeedMemoryPictureVH extends FeedMemoryViewHolder {

	public static final String LOG_TAG = FeedMemoryPictureVH.class.getCanonicalName();

	private ImageView mainView;
	private View mPlaceholder;

	FeedMemoryPictureVH(View view, TimelineFragment fragment,
						OnTimelineFragmentInteractionListener mListener) {
		super(view, fragment, mListener);

		mainView = (ImageView) mMainView;
		mPlaceholder = view.findViewById(R.id.placeholder);
	}


	public void onBindViewHolder(@NonNull Post object) {
		super.onBindViewHolder(object);

		LogUtils.d(LOG_TAG, "onBindViewHolder called for object: " + object.hashCode());

		// INFO: 2/26/19    adds background color handling here due to following edit
//		mainView.setBackgroundColor(mItem.getBackgroundColor(fragment.getResources()));

		if (super.mItem != null) {
			Object media = PicturesCache.Companion.getInstance(fragment.getContext())
					.getMedia(mItem.getContent(), HLMediaType.PHOTO);

			if (mainView != null) {

				// INFO: 2/26/19    restores MATCH_PARENT for both width and height statically in XML
//				mainView.setLayoutParams(
//						new FrameLayout.LayoutParams(
//								mItem.doesMediaWantFitScale() ? FrameLayout.LayoutParams.WRAP_CONTENT : FrameLayout.LayoutParams.MATCH_PARENT,
//								mItem.doesMediaWantFitScale() ? FrameLayout.LayoutParams.WRAP_CONTENT : FrameLayout.LayoutParams.MATCH_PARENT,
//								Gravity.CENTER
//						)
//				);

				mainView.setScaleType(mItem.doesMediaWantFitScale() ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);

				GlideRequests glide = GlideApp.with(fragment);
				GlideRequest<Drawable> glideRequest;
				if (media instanceof File)
					glideRequest = glide.load(media);
				else if (media instanceof Uri)
					glideRequest = glide.load(new File(((Uri) media).getPath()));
				else
					glideRequest = glide.load(super.mItem.getContent());

				if (mItem.doesMediaWantFitScale())
					glideRequest.fitCenter();
				else
					glideRequest.centerCrop();

				glideRequest.into(new CustomViewTarget<ImageView, Drawable>(mainView) {
					@Override
					protected void onResourceCleared(@Nullable Drawable placeholder) {
						mPlaceholder.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadFailed(@Nullable Drawable errorDrawable) {
						mPlaceholder.setVisibility(View.VISIBLE);

					}

					@Override
					public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
						mPlaceholder.setVisibility(View.INVISIBLE);
						getView().setImageDrawable(resource);
					}
				});
			}
		}
	}


	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (super.onSingleTapConfirmed(e) && mItem != null) {
			mListener.saveFullScreenState();

			if (mItem.getTypeEnum() == PostTypeEnum.FOLLOW_INTEREST) {
				if (mItem.hasNewFollowedInterest())
					ProfileActivity.openProfileCardFragment(
							getActivity(),
							ProfileHelper.ProfileType.INTEREST_NOT_CLAIMED,
							mItem.getFollowedInterestId(),
							HomeActivity.PAGER_ITEM_TIMELINE
					);
			}
			else {

				ViewCompat.setTransitionName(mainView, mItem.getContent());
				String name = ViewCompat.getTransitionName(mainView);

				Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
				intent.putExtra(Constants.EXTRA_PARAM_1, mItem.getContent());
				intent.putExtra(Constants.EXTRA_PARAM_2, name);
				intent.putExtra(Constants.EXTRA_PARAM_3, mItem.getId());
				intent.putExtra(Constants.EXTRA_PARAM_4, false);

				Bundle bundle = null;
				if (Utils.isStringValid(name)) {
					ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
							getActivity(),
							mainView,
							name);

					bundle = options.toBundle();
				}
				mMainView.getContext().startActivity(intent, bundle);
			}
			return true;
		}
		return false;
	}
}