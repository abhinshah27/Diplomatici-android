/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.timeline;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.lang.ref.WeakReference;

import rs.highlande.app.diplomatici.R;
import rs.highlande.app.diplomatici.models.Post;
import rs.highlande.app.diplomatici.utilities.LogUtils;
import rs.highlande.app.diplomatici.utilities.Utils;
import rs.highlande.app.diplomatici.utilities.caches.PicturesCache;
import rs.highlande.app.diplomatici.utilities.listeners.ResizingTextWatcher;
import rs.highlande.app.diplomatici.utilities.media.GlideApp;
import rs.highlande.app.diplomatici.utilities.media.HLMediaType;
import rs.highlande.app.diplomatici.widgets.RoundedCornersBackgroundSpan;

/**
 * @author mbaldrighi on 12/4/2017.
 */
class FeedMemorySurveyVH extends FeedMemoryViewHolder {

	public static final String LOG_TAG = FeedMemorySurveyVH.class.getCanonicalName();

	private static RoundedCornersBackgroundSpan backgrSpan;
	private static RoundedCornersBackgroundSpan backgrSpanAlpha;

	private ImageView mainView;
	private View placeHolder;
	private TextView surveyQuestion, surveyCta;


	FeedMemorySurveyVH(View view, TimelineFragment fragment,
					   final OnTimelineFragmentInteractionListener mListener) {
		super(view, fragment, mListener);

		mainView = (ImageView) mMainView;
		placeHolder = view.findViewById(R.id.placeholder);
		surveyQuestion = super.maskLower.findViewById(R.id.survey_question);
		surveyCta = super.maskLower.findViewById(R.id.survey_cta);

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

			Object media = PicturesCache.Companion.getInstance(fragment.getContext())
					.getMedia(mItem.getContent(), HLMediaType.PHOTO);

			if (media != null) {
				placeHolder.setVisibility(View.VISIBLE);
				GlideApp.with(fragment).load(media).into(mainView);
//				MediaHelper.loadPictureWithGlide(mainView.getContext(),
//						url, null, 0, 0, new CustomViewTarget<ImageView, Drawable>(mainView) {
//							@Override
//							protected void onResourceCleared(@Nullable Drawable placeholder) {
//								MediaHelper.loadPictureWithGlide(mainView.getContext(), Constants.WEB_LINK_PLACEHOLDER_URL, getView());
//							}
//
//							@Override
//							public void onLoadFailed(@Nullable Drawable errorDrawable) {
//								MediaHelper.loadPictureWithGlide(mainView.getContext(), Constants.WEB_LINK_PLACEHOLDER_URL, getView());
//							}
//
//							@Override
//							public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//								mainView.setImageDrawable(resource);
//							}
//						});
			} else placeHolder.setVisibility(View.GONE);

			if (Utils.isStringValid(mItem.getSurveyQuestion())) {
				surveyQuestion.setShadowLayer(10f, 0, 0, Color.TRANSPARENT);
				surveyQuestion.setPadding(10, 10, 10, 10);
				surveyQuestion.setLineSpacing(2, 1);

				Spannable spanned = new SpannableString(mItem.getSurveyQuestion());
				spanned.setSpan(
						backgrSpan,
						0,
						mItem.getSurveyQuestion().length(),
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE
				);
				surveyQuestion.setText(spanned);
				ResizingTextWatcher.resizeTextInView(surveyQuestion);
			}
			else
				surveyQuestion.setText("");

			@StringRes int s;
			if (mItem.canVoteSurvey()) {
			    if (mItem.hasSurveyAnswer()) s = R.string.survey_cta_already;
                        else s = R.string.survey_cta_answer;
            } else {
			    s = R.string.survey_cta_close;
            }

			surveyCta.setText(s);
			surveyCta.setShadowLayer(10f, 0, 0, Color.TRANSPARENT);
			surveyCta.setPadding(5, 5, 5, 5);
			surveyCta.setLineSpacing(2, 1);

			Spannable spanned = new SpannableString(surveyCta.getText());
			spanned.setSpan(
					backgrSpanAlpha,
					0,
					surveyCta.getText().length(),
					Spannable.SPAN_INCLUSIVE_EXCLUSIVE
			);
			surveyCta.setText(spanned);
		}
	}


	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (super.onSingleTapConfirmed(e)) {
			mListener.saveFullScreenState();
			goToSurvey();
			return true;
		}
		return false;
	}


	private void goToSurvey() {

		// TODO: 2019-12-04    ask how to handle
        if (mItem.canVoteSurvey()) {

            if (mItem.isSurveyOpenAnswer())
                mListener.openSurveyPanel(getActivity(), mItem.getId());
            else {
                fragment.setWeakSurvey(
                        new WeakReference<>(
                                mListener.openSurveyPanel(fragment.getChildFragmentManager(), mItem.getId())
                        )
                );
            }
        }
	}

}