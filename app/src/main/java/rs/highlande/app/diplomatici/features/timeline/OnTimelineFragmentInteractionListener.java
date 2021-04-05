/*
 * Copyright (c) 2017. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.timeline;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import io.realm.Realm;
import rs.highlande.app.diplomatici.base.HLActivity;
import rs.highlande.app.diplomatici.features.HomeActivity;
import rs.highlande.app.diplomatici.features.timeline.interactions.FullScreenHelper;
import rs.highlande.app.diplomatici.features.timeline.survey.SurveyActivity;
import rs.highlande.app.diplomatici.features.timeline.survey.SurveyBottomFragment;
import rs.highlande.app.diplomatici.models.HLPosts;
import rs.highlande.app.diplomatici.models.Post;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.media.MediaHelper;


/**
 * This interface must be implemented by activities that contain this
 * {@link TimelineFragment} to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p/>
 *
 * @author mbaldrighi on 10/18/2017.
 */
public interface OnTimelineFragmentInteractionListener {

	/**
	 * Communicates taps on Fragment's mainView to {@link HomeActivity} when media ratio would better
	 * fit in a landscape mode.
	 */
	void actionsForLandscape(@NonNull String postId, View view);

	void setLastAdapterPosition(int position);
	Integer getLastAdapterPosition();

	FullScreenHelper getFullScreenListener();
	Toolbar getToolbar();
	View getBottomBar();

	MediaHelper getMediaHelper();

	void goToInteractionsActivity(@NonNull String postId);
	void goToProfile(@NonNull String userId, boolean isInterest);
	void saveFullScreenState();

	int getPageIdToCall();
	int getLastPageID();
	void setLastPageID(int lastPageID);

	default int getFeedSkip(@Nullable Realm realm, @Nullable TimelineFragment.FragmentUsageType type,
							boolean wantsTop) {
		if (wantsTop)
			return 0;
		else {
			HLPosts posts = HLPosts.getInstance();
			return posts.getFeedPostsSkip(type);
		}
	}

	/**
	 * @return The {@link HLActivity} instance underneath the interface.
	 */
	HLActivity getActivity();
	/**
	 * @return The {@link Resources} instance underneath the interface.
	 */
	Resources getResources();

	void setFsStateListener(FullScreenHelper.RestoreFullScreenStateListener fsStateListener);

	boolean isPostSheetOpen();
	void closePostSheet();
	void openPostSheet(@NonNull String postId, boolean isUserAuthor);

	void viewAllTags(Post post);


	/**
	 * Opens survey Activity for free text answers.
	 * @param activity The calling activity.
	 * @param postId The provided id of the related Post object.
	 */
	default void openSurveyPanel(Activity activity, String postId) {
		activity.startActivity(new Intent(activity, SurveyActivity.class) {{
			putExtra(Constants.EXTRA_PARAM_1, postId);
		}});
	}

	/**
	 * Opens survey panel for radio and check answers.
	 * @param frgManager The FragmentManager used to open the bottom fragment.
	 * @param postId The provided id of the related Post object.
	 * @return The newly created fragment.
	 */
	default SurveyBottomFragment openSurveyPanel(FragmentManager frgManager, String postId) {
		SurveyBottomFragment frg = SurveyBottomFragment.newInstance(postId);
		frg.show(frgManager, "surveyBottomSheet");

		return frg;
	}

}
