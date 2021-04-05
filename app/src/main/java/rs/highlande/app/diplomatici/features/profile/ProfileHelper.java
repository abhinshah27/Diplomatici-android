/*
 * Copyright (c) 2017. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.anko.IntentsKt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmList;
import kotlin.Pair;
import rs.highlande.app.diplomatici.R;
import rs.highlande.app.diplomatici.base.DiplomaticiApp;
import rs.highlande.app.diplomatici.base.HLActivity;
import rs.highlande.app.diplomatici.connection.HLRequestTracker;
import rs.highlande.app.diplomatici.connection.HLServerCallsChat;
import rs.highlande.app.diplomatici.connection.OnMissingConnectionListener;
import rs.highlande.app.diplomatici.connection.OnServerMessageReceivedListener;
import rs.highlande.app.diplomatici.features.HomeActivity;
import rs.highlande.app.diplomatici.features.chat.ChatActivity;
import rs.highlande.app.diplomatici.features.createPost.CreatePostActivityMod;
import rs.highlande.app.diplomatici.features.notifications.NotificationAndRequestHelper;
import rs.highlande.app.diplomatici.features.settings.SettingsActivity;
import rs.highlande.app.diplomatici.features.voiceVideoCalls.NotificationUtils;
import rs.highlande.app.diplomatici.features.voiceVideoCalls.tmp.VoiceVideoCallType;
import rs.highlande.app.diplomatici.features.webView.CommonWebViewActivity;
import rs.highlande.app.diplomatici.features.webView.WebViewType;
import rs.highlande.app.diplomatici.models.HLNotifications;
import rs.highlande.app.diplomatici.models.HLUser;
import rs.highlande.app.diplomatici.models.HLUserGeneric;
import rs.highlande.app.diplomatici.models.Interest;
import rs.highlande.app.diplomatici.models.chat.ChatRoom;
import rs.highlande.app.diplomatici.models.enums.ProfileTypeEnum;
import rs.highlande.app.diplomatici.models.enums.RequestsStatusEnum;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.RealmUtils;
import rs.highlande.app.diplomatici.utilities.Utils;
import rs.highlande.app.diplomatici.utilities.media.MediaHelper;

/**
 * @author mbaldrighi on 12/4/2017.
 */
public class ProfileHelper implements Serializable, View.OnClickListener, OnServerMessageReceivedListener,
		OnMissingConnectionListener
		/*NotificationAndRequestHelper.OnNotificationHelpListener*//* HomeActivity.ViewProviderForNotifications*/ {

	public static final String LOG_TAG = ProfileHelper.class.getCanonicalName();

	private static final float WEIGHT_ME_FRIEND_SHOW = .3333333333333333f;
	private static final float WEIGHT_ME_FRIEND_NO = .5f;

	private ImageView mainBackground;

	private Toolbar toolbar1;
	private View notificationBtn;
	private View notificationDot;
	//	private TextView notificationsCount;
	private View settingsBtn;

	private Toolbar toolbar2;
	private View toolbarDots;

	private View bottomBar;
	private View l1, l2, l3, l4;
	private TransitionDrawable td1, td2, td3, td4;
	private View bottomBarNotificationDot;

	/* MAIN CARD */
	private ViewGroup usersCard;
	private ImageView profilePicture;
	private ImageView wallPicture;
	private TextView personName, personAboutMe;
	private TextView inviteBtn;
	private View heartsSection;
	private View heartsAvailLayout;
	private TextView heartsAvailable;
	//	private ImageView wishesButton;
	private View lowerLayout, heartsNameLayout;
//	private View documentationBtn;

	private View locationOrFreeTextLayout;
	private TextView locationOrFreeText;
	private ImageView iconLocation;
	private ImageView profileBadge;

	/* card friends */
	private View friendHideable;
	private TextView heartsGiven;
	private TextView heartsReceived;

	/* BOTTOM SECTION */
	private View layoutButtons;
	private TextView buttonAction1, buttonAction2, buttonAction3;
//	private View buttonsMeFriend;
//	private TextView meFriendDiary, icMeFriend, intMeFriend;
//	private View buttonsNotFriend;

	/* INTEREST HANDLING */
	private View interestCard;
	private ImageView interestPicture;
	private ImageView interestWall;
	private TextView unFollowBtn;
	private View claimInterestStatus;

	private View preferredLabel;
	private View preferredLabelInvis;

	private TextView interestName;
	private TextView interestDescription;
	//	private View interestHeartsLayout;
//	private TextView interestHeartsTotal;
//	private TextView interestFollowers;
	private View interestButtonsSection;
	//	private TextView interestBtnPromotions, interestBtnFollowers;
	private View interestHeartsFollowersLayout;

	private View interestLocationOrFreeTextLayout;
	private TextView interestLocationOrFreeText;
	private ImageView interestIconLocation;
	private ImageView interestProfileBadge;

	private View chatCallsLayout;
	private View groupChat, groupVoice, groupVideo, groupEmail;
	private ChatRoom chatRoom = null;

	public enum ProfileType implements Serializable {
		ME, FRIEND, NOT_FRIEND,
		INTEREST_ME, INTEREST_CLAIMED, INTEREST_NOT_CLAIMED
	}

	private ProfileType mType;

	private HLUserGeneric userGen;
	private Interest interest;

//	private NotificationAndRequestHelper notificationsHelper;

	private OnProfileInteractionsListener mListener;

	private String callsToken = null;


	public ProfileHelper(OnProfileInteractionsListener listener) {
		this.mListener = listener;

//		if (mListener instanceof HomeActivity)
//			((HomeActivity) mListener).setViewProvider(this);
//		else
//		if (mListener instanceof ProfileFragment &&
//				((ProfileFragment) mListener).getActivity() instanceof HLActivity) {
//
//			this.notificationsHelper = new NotificationAndRequestHelper(
//					(HLActivity) ((ProfileFragment) mListener).getActivity(),
//					this
//			);
//		}

	}

	public void configureLayout(View view) {
		if (view != null) {
			mainBackground = view.findViewById(R.id.profile_background);

			// TOOLBAR #1
			toolbar1 = view.findViewById(R.id.toolbar_1);
			notificationBtn = toolbar1.findViewById(R.id.notification_btn_layout);
			notificationBtn.setOnClickListener(this);
			notificationDot = toolbar1.findViewById(R.id.notification_dot);
//			notificationsCount = toolbar1.findViewById(R.id.notifications_count);

			settingsBtn = toolbar1.findViewById(R.id.settings_btn);
			settingsBtn.setOnClickListener(this);

			// TOOLBAR #2
			toolbar2 = view.findViewById(R.id.toolbar_2);
			toolbar2.findViewById(R.id.back_arrow).setOnClickListener(this);
			toolbarDots = toolbar2.findViewById(R.id.dots);
			toolbarDots.setOnClickListener(this);

			// BOTTOM BAR
			bottomBar = view.findViewById(R.id.bottom_bar);
			configureBottomBar(bottomBar);

			usersCard = view.findViewById(R.id.card_profile);
			configureUsersCard(usersCard);
			interestCard = view.findViewById(R.id.card_interest);
			configureInterestCard(interestCard);

			configureActionButtons(view);


//			// for USERS
//			buttonsMeFriend = view.findViewById(R.id.screen_selection_me_friend);
//			meFriendDiary = view.findViewById(R.id.me_friend_diary);
//			buttonsNotFriend = view.findViewById(R.id.screen_selection_not_friend);
//
//			meFriendDiary.setOnClickListener(this);
//			View notFriendDiary = view.findViewById(R.id.not_friend_diary);
//			notFriendDiary.setOnClickListener(this);
//
//			icMeFriend = view.findViewById(R.id.me_friend_inner_C);
//			icMeFriend.setOnClickListener(this);
//			View icNotFriend = view.findViewById(R.id.not_friend_inner_c);
//			icNotFriend.setOnClickListener(this);
//
//			intMeFriend = view.findViewById(R.id.me_friend_inner_interests);
//			intMeFriend.setOnClickListener(this);
//
//
//			// for INTERESTS
//			interestButtonsSection = view.findViewById(R.id.screen_selection_interest);
//			View interestBtnDiary = view.findViewById(R.id.interest_diary);
//			interestBtnDiary.setOnClickListener(this);
//			interestBtnFollowers = view.findViewById(R.id.interest_followers);
//			interestBtnFollowers.setOnClickListener(this);
//
//			interestBtnPromotions = view.findViewById(R.id.interest_promotions);
//			interestBtnPromotions.setOnClickListener(this);
		}
	}

	private void configureActionButtons(View view) {
		layoutButtons = view.findViewById(R.id.layout_options);
		buttonAction1 = view.findViewById(R.id.btn_profile_1);
		buttonAction2 = view.findViewById(R.id.btn_profile_2);
		buttonAction3 = view.findViewById(R.id.btn_profile_3);
	}

	private void configureUsersCard(final ViewGroup view) {
		profilePicture = view.findViewById(R.id.profile_picture);
		profilePicture.setOnClickListener(this);

		profilePicture.setOnLongClickListener(v -> {
			// INFO: 2/14/19    LUISS - NO IDENTITIES
//			Context context = profilePicture.getContext();
//			if (context instanceof Activity &&
//					(mType == ProfileType.ME || mType == ProfileType.INTEREST_ME)) {
//				Utils.openIdentitySelection((Activity) context, null);
//				return true;
//			}
			return false;
		});

		wallPicture = view.findViewById(R.id.wall_image);
		personName = view.findViewById(R.id.name);
		personName.setOnClickListener(this);
		personAboutMe = view.findViewById(R.id.about_me);
		inviteBtn = view.findViewById(R.id.invite_to_circle);
		inviteBtn.setOnClickListener(this);
		heartsSection = view.findViewById(R.id.hearts_section);
		locationOrFreeTextLayout = view.findViewById(R.id.profile_location_free_text_layout);
		heartsAvailLayout = view.findViewById(R.id.hearts_avail_layout);
		locationOrFreeText = view.findViewById(R.id.location_free_text);
		heartsAvailable = view.findViewById(R.id.count_heart_available);
		friendHideable = view.findViewById(R.id.layout_hideable_friend);
		heartsGiven = view.findViewById(R.id.count_heart_given);
		heartsReceived = view.findViewById(R.id.count_heart_received);

		iconLocation = view.findViewById(R.id.icon_location);
		profileBadge = view.findViewById(R.id.profile_badge);

//		wishesButton = new ImageView(view.getContext());
//		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
//				Utils.dpToPx(50f, usersCard.getResources()),
//				Utils.dpToPx(50f, usersCard.getResources()),
//				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
//		);
//		wishesButton.setLayoutParams(lp);
//		wishesButton.setOnClickListener(v -> {
//
//				// TODO: 2/8/19    REPLACE WITH LUISS CUSTOM ACTION
////				Context context = wishesButton.getContext();
////				if (Utils.isContextValid(context)) {
////					context.startActivity(new Intent(context, WishesAccessActivity.class));
////
////					if (context instanceof Activity)
////						((Activity) context).overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
////				}
//
//		});
		// INFO: 2/15/19    LUISS - NO BTN
//		usersCard.addView(wishesButton);


//		documentationBtn = LayoutInflater.from(view.getContext()).inflate(R.layout.custom_docs_btn, view, false);
//		FrameLayout.LayoutParams lp2 = (FrameLayout.LayoutParams) documentationBtn.getLayoutParams();
//		lp2.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
//		documentationBtn.setLayoutParams(lp2);
//		documentationBtn.setOnClickListener(v -> {
//
//			Context context = wishesButton.getContext();
//			if (Utils.isContextValid(context)) {
//				context.startActivity(new Intent(context, LuissDocumentationActivity1List.class) {{
//					putExtra(Constants.EXTRA_PARAM_1, mListener.getUser().getDocUrl());
//				}});
//
//				if (context instanceof Activity)
//					((Activity) context).overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
//			}
//
//		});
//		usersCard.addView(documentationBtn);


		chatCallsLayout = LayoutInflater.from(view.getContext()).inflate(R.layout.layout_user_profile_chat_calls_fab, view, false);
		FrameLayout.LayoutParams lp1 = (FrameLayout.LayoutParams) chatCallsLayout.getLayoutParams();
		lp1.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		if (chatCallsLayout != null) {
			chatCallsLayout.setLayoutParams(lp1);
			View btnChat = chatCallsLayout.findViewById(R.id.backgroundChat);
			btnChat.setOnClickListener(v -> {
				if (userGen != null) {
					// friend != null
					if (userGen.hasValidChatRoom()) {

						Pair<Boolean, ChatRoom> pair = ChatRoom.checkRoom(userGen.getChatRoomID(), ((HLActivity) mListener.getProfileFragment().getActivity()).getRealm());
						if (pair.getFirst()) {
							// the room exists and we are both in: just open
							ChatActivity.openChatMessageFragment(
									chatCallsLayout.getContext(),
									userGen.getChatRoomID(),
									userGen.getCompleteName(),
									userGen.getAvatarURL(),
									false
							);
						}
						else {
							// likely I deleted the room. it has to be re-initialized, already has ID
							initializeNewRoom(false);
						}
					}
					else {
						// need to initialize new ChatRoom instance on server, no ID to pass
						initializeNewRoom(true);
					}
				}
			});
//			View btnVoice = chatCallsLayout.findViewById(R.id.btnCallVoice);
//			btnVoice.setOnClickListener(v -> goToCallActivity(VoiceVideoCallType.VOICE));
//			View btnVideo = chatCallsLayout.findViewById(R.id.btnCallVideo);
//			btnVideo.setOnClickListener(v -> goToCallActivity(VoiceVideoCallType.VIDEO));

			View btnEmail = chatCallsLayout.findViewById(R.id.backgroundEmail);
			btnEmail.setOnClickListener(v ->
					IntentsKt.email(v.getContext(), userGen.getEmail(), "", "")
			);

			groupChat = chatCallsLayout.findViewById(R.id.groupChat);
			groupVoice = chatCallsLayout.findViewById(R.id.groupVoice);
			groupVideo = chatCallsLayout.findViewById(R.id.groupVideo);
			groupEmail = chatCallsLayout.findViewById(R.id.groupEmail);

			usersCard.addView(chatCallsLayout);
		}

		lowerLayout = this.usersCard.findViewById(R.id.lower_section);
		lowerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new CardGlobalLayoutObserver(lowerLayout, chatCallsLayout, usersCard));

		heartsNameLayout = view.findViewById(R.id.name_hearts_layout);

		view.setOnClickListener(this);
	}

	private void configureInterestCard(View view) {
		interestPicture = view.findViewById(R.id.interest_profile_picture);
		interestPicture.setOnClickListener(this);

		interestPicture.setOnLongClickListener(v -> {
			// INFO: 2/14/19    LUISS - NO IDENTITIES
//			Context context = interestPicture.getContext();
//			if (context instanceof Activity &&
//					(mType == ProfileType.ME || mType == ProfileType.INTEREST_ME)) {
//				Utils.openIdentitySelection((Activity) context, null);
//				return true;
//			}
			return false;
		});

		interestWall = view.findViewById(R.id.wall_image);
		unFollowBtn = view.findViewById(R.id.follow_unfollow_btn);
		unFollowBtn.setOnClickListener(this);
		claimInterestStatus = view.findViewById(R.id.claim_status);

		preferredLabel = view.findViewById(R.id.preferred_icon);
		preferredLabelInvis = view.findViewById(R.id.preferred_icon_invis);
		interestName = view.findViewById(R.id.interest_name);
		interestName.setOnClickListener(this);
		interestDescription = view.findViewById(R.id.interest_headline);
//		interestHeartsLayout = view.findViewById(R.id.hearts_total_layout);
//		interestHeartsTotal = view.findViewById(R.id.count_heart_total);
//		interestFollowers = view.findViewById(R.id.count_followers);

		interestHeartsFollowersLayout = view.findViewById(R.id.hearts_followers_section);

		View interestLocationOrFreeTextLayout = view.findViewById(R.id.profile_location_free_text_layout);
		interestLocationOrFreeTextLayout.setOnClickListener( v -> {

				// INFO: 2019-11-18    NO-OPS
//				Utils.fireMapIntent(
//						v.getContext(),
//						0.0,
//						0.0,
//						interest.getHeadline().replaceAll("\n", " ")
//				)
		});
		interestLocationOrFreeText = view.findViewById(R.id.location_free_text);
		interestIconLocation = view.findViewById(R.id.icon_location);
		interestProfileBadge = view.findViewById(R.id.profile_badge);

		view.setOnClickListener(this);

		// INFO: 2/11/19    custom actions from mockup pptx
		view.findViewById(R.id.talk_to_us).setOnClickListener(v -> {
			// TODO: 2/11/19    fill with action
		});
		view.findViewById(R.id.job_openings).setOnClickListener(v -> {
			// TODO: 2/11/19    fill with action
		});
	}

	private void configureBottomBar(final View bar) {
		if (bar != null) {
			l1 = bar.findViewById(R.id.bottom_timeline);
			l1.setOnClickListener(this);
			l2 = bar.findViewById(R.id.bottom_profile);
			l2.setOnClickListener(this);
			l2.setSelected(true);
			l3 = bar.findViewById(R.id.bottom_chats);
			l3.setOnClickListener(this);
			l4 = bar.findViewById(R.id.bottom_global_search);
			l4.setOnClickListener(this);

			ImageView ib1 = bar.findViewById(R.id.icon_timeline);
			td1 = (TransitionDrawable) ib1.getDrawable();
			td1.setCrossFadeEnabled(true);
			ImageView ib2 = bar.findViewById(R.id.icon_profile);
			td2 = (TransitionDrawable) ib2.getDrawable();
			td2.setCrossFadeEnabled(true);
			td2.startTransition(0);
			ImageView ib3 = bar.findViewById(R.id.icon_chats);
			td3 = (TransitionDrawable) ib3.getDrawable();
			td3.setCrossFadeEnabled(true);
			ImageView ib4 = bar.findViewById(R.id.icon_global_search);
			td4 = (TransitionDrawable) ib4.getDrawable();
			td4.setCrossFadeEnabled(true);

			View main = bar.findViewById(R.id.main_action_btn);
			main.setOnClickListener(this);

			bottomBarNotificationDot = bar.findViewById(R.id.notification_dot);
		}
	}

	private void setBottomBar(int currentSelItem) {
		if (currentSelItem > -1) {
			switch (currentSelItem) {
				case HomeActivity.PAGER_ITEM_GLOBAL_SEARCH:
					l1.setSelected(false);
					l2.setSelected(false);
					l3.setSelected(false);
					l4.setSelected(true);
					td4.startTransition(0);
					td1.resetTransition();
					td2.resetTransition();
					td3.resetTransition();
					break;
				case HomeActivity.PAGER_ITEM_TIMELINE:
					l1.setSelected(true);
					l2.setSelected(false);
					l3.setSelected(false);
					l4.setSelected(false);
					td1.startTransition(0);
					td4.resetTransition();
					td2.resetTransition();
					td3.resetTransition();
					break;
				case HomeActivity.PAGER_ITEM_PROFILE:
					l1.setSelected(false);
					l2.setSelected(true);
					l3.setSelected(false);
					l4.setSelected(false);
					td2.startTransition(0);
					td4.resetTransition();
					td1.resetTransition();
					td3.resetTransition();
					break;
				case HomeActivity.PAGER_ITEM_CHATS:
					l1.setSelected(false);
					l2.setSelected(false);
					l3.setSelected(true);
					l4.setSelected(false);
					td3.startTransition(0);
					td4.resetTransition();
					td2.resetTransition();
					td1.resetTransition();
					break;
			}
		}
		else {
			l1.setSelected(false);
			l2.setSelected(false);
			l3.setSelected(false);
			l4.setSelected(false);
			td3.resetTransition();
			td4.resetTransition();
			td2.resetTransition();
			td1.resetTransition();
		}
	}

	public void setLayout(@NonNull ProfileType type, @Nullable HLUserGeneric userGen,
						  @Nullable Interest interest, int bottomBarSelItem) {
		mType = type;

		boolean condition = HLNotifications.getInstance().getUnreadCount(true) > 0 && mListener.getUser().isValid();
		notificationDot.setVisibility(condition ? View.VISIBLE : View.GONE);

		toolbar1.setVisibility((type == ProfileType.ME || type == ProfileType.INTEREST_ME) ? View.VISIBLE : View.GONE);
		toolbar2.setVisibility((type == ProfileType.ME || type == ProfileType.INTEREST_ME) ? View.GONE : View.VISIBLE);

		// INFO: 2019-11-18    Fixes #9.28
		toolbarDots.setVisibility(/*mListener.getUser().isActingAsInterest() ? */View.GONE/* : View.VISIBLE*/);

		int margin = bottomBar.getContext().getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);
		if (type == ProfileType.ME || type == ProfileType.INTEREST_ME) {
			bottomBar.setVisibility(View.GONE);

			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) layoutButtons.getLayoutParams();
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			lp.setMargins(0, 0, 0, margin);
			layoutButtons.setLayoutParams(lp);
		}
		else {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) layoutButtons.getLayoutParams();
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			layoutButtons.setLayoutParams(lp);
		}

		switch (type) {
			case ME:
			case FRIEND:
			case NOT_FRIEND:
//				buttonsNotFriend.setVisibility(View.VISIBLE);
//				buttonsMeFriend.setVisibility(View.GONE);
//				interestButtonsSection.setVisibility(View.GONE);
				usersCard.setVisibility(View.VISIBLE);
				interestCard.setVisibility(View.GONE);
				setLayoutForUsers(type, userGen);
				break;

			case INTEREST_ME:
			case INTEREST_CLAIMED:
			case INTEREST_NOT_CLAIMED:
//				buttonsNotFriend.setVisibility(View.GONE);
//				buttonsMeFriend.setVisibility(View.GONE);
//				interestButtonsSection.setVisibility(View.VISIBLE);
				usersCard.setVisibility(View.GONE);
				interestCard.setVisibility(View.VISIBLE);
				if (interest != null)
					setLayoutForInterests(type, interest);
				break;
		}
	}

	private void setLayoutForUsers(@NonNull ProfileType type, @Nullable HLUserGeneric userCard) {
		String name = userCard != null ? userCard.getName() : "";
		String aboutMe = userCard != null ? userCard.getAboutDescription() : "";

		String[] labelsArr = null;
		String[] actionsArr = null;
		@DrawableRes int badgeRes;

		if (type == ProfileType.ME) {
			HLUser user = mListener.getUser();

			NotificationAndRequestHelper.handleDotVisibility(bottomBarNotificationDot, user.isValid());

			if (user.getAvatar() != null) {
				Drawable d = new BitmapDrawable(profilePicture.getResources(), user.getAvatar());
				profilePicture.setImageDrawable(d);
			} else {
				if (Utils.isStringValid(user.getAvatarURL()))
					loadPictureWithConversion(user.getAvatarURL(), profilePicture, PictureType.PROFILE);
			}

			if (user.getWall() != null) {
				Drawable d = new BitmapDrawable(mainBackground.getResources(), user.getWall());
				wallPicture.setImageDrawable(d);
				mainBackground.setImageDrawable(d);
			}
			else if (Utils.isStringValid(user.getCoverPhotoURL())) {
				loadPictureWithConversion(user.getCoverPhotoURL(), wallPicture, PictureType.WALL);
			}

			// INFO: 2/12/19    main users don't want it
			heartsAvailLayout.setVisibility(View.GONE);
			heartsAvailable.setText(Utils.getReadableCount(heartsAvailable.getResources(), user.getTotHeartsAvailable()));

			inviteBtn.setVisibility(View.GONE);

			friendHideable.setVisibility(View.GONE);

//			buttonsMeFriend.setVisibility(View.VISIBLE);
//			buttonsNotFriend.setVisibility(View.GONE);
//
//			intMeFriend.setText(R.string.my_interests);
//			meFriendDiary.setText(R.string.profile_diary_me);

			name = user.getCompleteName();
			aboutMe = user.getAboutDescription();

//			if (wishesButton != null) {
//				wishesButton.setImageResource(R.drawable.ic_wishes_button);
//				wishesButton.setVisibility(View.VISIBLE);
//			}
//			if (documentationBtn != null) {
//				documentationBtn.setVisibility(Utils.isStringValid(user.getDocUrl()) ? View.VISIBLE : View.GONE);
//			}

			// TODO: 2019-10-01    check if staff and teachers can chat with each other
			if (chatCallsLayout != null)
				chatCallsLayout.setVisibility(View.GONE);

			iconLocation.setVisibility(View.GONE);
			if (Utils.isStringValid(aboutMe)) {
				locationOrFreeTextLayout.setVisibility(View.VISIBLE);
				locationOrFreeText.setText(aboutMe);
			} else {
				locationOrFreeTextLayout.setVisibility(View.GONE);
			}

			ProfileTypeEnum userEnum = ProfileTypeEnum.toEnum(user.getType());

			labelsArr = profileBadge.getResources().getStringArray(ProfileTypeEnum.getProfileStringsArray(userEnum, user.isUserNormal()));
			actionsArr = profileBadge.getResources().getStringArray(ProfileTypeEnum.getProfileActions(userEnum));
			badgeRes = ProfileTypeEnum.getBadgeIcon(userEnum);

			if (badgeRes != -1) {
				profileBadge.setImageResource(badgeRes);
				profileBadge.setVisibility(View.VISIBLE);
			} else {
				profileBadge.setVisibility(View.GONE);
			}
		}
		else if (userCard != null) {
			this.userGen = userCard;
			String avatar = userCard.getAvatarURL();
			String wall = userCard.getWallImageLink();
			MediaHelper.loadProfilePictureWithPlaceholder(profilePicture.getContext(),
					avatar, profilePicture);

			loadPictureWithConversion(wall, wallPicture, PictureType.WALL);

			type = userCard.isFriend() ? ProfileType.FRIEND : ProfileType.NOT_FRIEND;

			inviteBtn.setVisibility(
					/*(
							!DiplomaticiApp.getSocketConnection().isConnected() || type == ProfileType.FRIEND ||
									!userCard.canBeInvitedToIC() ||
									mListener.getUser().isActingAsInterest()
					) ? */View.GONE/* : View.VISIBLE*/
			);

			LinearLayout.LayoutParams heartsLp = (LinearLayout.LayoutParams) heartsNameLayout.getLayoutParams();
			heartsLp.gravity = Gravity.CENTER_VERTICAL;
			heartsNameLayout.setLayoutParams(heartsLp);

			// INFO: 2/12/19    users don't want it
			friendHideable.setVisibility(/*type == ProfileType.FRIEND ? View.VISIBLE : */View.GONE);
//			buttonsMeFriend.setVisibility(type == ProfileType.FRIEND ? View.VISIBLE : View.GONE);
//			buttonsNotFriend.setVisibility(type == ProfileType.FRIEND ? View.GONE : View.VISIBLE);
			heartsSection.setVisibility(type == ProfileType.FRIEND ? View.VISIBLE : View.GONE);
			locationOrFreeTextLayout.setVisibility(type == ProfileType.FRIEND ? View.VISIBLE : View.GONE);
			heartsAvailLayout.setVisibility(View.GONE);

			if (type == ProfileType.FRIEND) {
				heartsGiven.setText(
						heartsGiven.getContext().getString(
								R.string.profile_hearts_given,
								Utils.getReadableCount(heartsGiven.getResources(), userCard.getHeartsGiven())
						)
				);
				heartsReceived.setText(
						heartsReceived.getContext().getString(
								R.string.profile_hearts_received,
								Utils.getReadableCount(heartsGiven.getResources(), userCard.getHeartsReceived())
						)
				);
				heartsAvailable.setVisibility(View.GONE);

//				float weightMeFriend = userCard.isShowInnerCircle() ? WEIGHT_ME_FRIEND_SHOW : WEIGHT_ME_FRIEND_NO;
//				icMeFriend.setVisibility(userCard.isShowInnerCircle() ? View.VISIBLE : View.GONE);
//
//				LinearLayout.LayoutParams lp6 = (LinearLayout.LayoutParams) meFriendDiary.getLayoutParams();
//				lp6.weight = weightMeFriend;
//				meFriendDiary.setLayoutParams(lp6);
//				meFriendDiary.setText(R.string.profile_diary_friend);
//
//				LinearLayout.LayoutParams lp7 = (LinearLayout.LayoutParams) intMeFriend.getLayoutParams();
//				lp7.weight = weightMeFriend;
//				intMeFriend.setLayoutParams(lp7);
//				intMeFriend.setText(R.string.interests);

				if (chatCallsLayout != null) {
					groupChat.setVisibility(userGen.canChat() ? View.VISIBLE : View.GONE);
					groupEmail.setVisibility(userGen.canEmail() ? View.VISIBLE : View.GONE);

					// INFO: 2/11/19    SERVER FORCED BOOL to FALSE
					groupVoice.setVisibility(userGen.canAudiocall() ? View.VISIBLE : View.GONE);
					groupVideo.setVisibility(
							Utils.hasDeviceCamera(chatCallsLayout.getContext()) && userGen.canVideocall() ?
									View.VISIBLE : View.GONE
					);
				}
			}
			else chatCallsLayout.setVisibility(View.GONE);

			inviteBtn.setText(userCard.getRightStringForStatus());
			inviteBtn.setEnabled(userCard.getRequestsStatus() != RequestsStatusEnum.PENDING);

//			if (wishesButton != null)
//				wishesButton.setVisibility(View.GONE);

			// INFO: 2019-10-25    location icon visibility enabled only for INTEREST and of type LOCATION and EVENT (as per mockup)
			iconLocation.setVisibility(View.GONE);
			if (Utils.isStringValid(aboutMe)) {
				locationOrFreeTextLayout.setVisibility(View.VISIBLE);
				locationOrFreeText.setText(aboutMe);
			} else {
				locationOrFreeTextLayout.setVisibility(View.GONE);
			}

			ProfileTypeEnum userEnum = ProfileTypeEnum.toEnum(userCard.getType());

			labelsArr = profileBadge.getResources().getStringArray(ProfileTypeEnum.getProfileStringsArray(userEnum, false));
			actionsArr = profileBadge.getResources().getStringArray(ProfileTypeEnum.getProfileActions(userEnum));
			badgeRes = ProfileTypeEnum.getBadgeIcon(userEnum);

			if (badgeRes != -1) {
				profileBadge.setImageResource(badgeRes);
				profileBadge.setVisibility(View.VISIBLE);
			} else {
				profileBadge.setVisibility(View.GONE);
			}
		}

		personName.setText(name);

//		lowerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new CardGlobalLayoutObserver(wishesButton, usersCard));
		if (!Utils.isStringValid(aboutMe) || aboutMe.equalsIgnoreCase(personAboutMe.getContext().getString(R.string.not_defined_yet))) {
			personAboutMe.setVisibility(View.GONE);
		}
		else {
			personAboutMe.setText(aboutMe);
			personAboutMe.setVisibility(View.GONE);
		}

		heartsNameLayout.setPadding(
				0,
				Utils.dpToPx(/*type == ProfileType.ME ? 30f : 10f*/ 30f, heartsNameLayout.getResources()),
				0,
				0
		);

		if (labelsArr != null && labelsArr.length == 3) {
			buttonAction1.setText(labelsArr[0]);
			buttonAction2.setText(labelsArr[1]);
			buttonAction3.setText(labelsArr[2]);
		}

		if (actionsArr != null && actionsArr.length == 3) configBtnActions(actionsArr, mListener.getUser(), userCard);
	}



	private void setLayoutForInterests(@NonNull ProfileType type, @NonNull Interest interest) {
		this.interest = interest;
		boolean isHighlanders = Utils.isStringValid(interest.getId()) && interest.getId().equals(Constants.ID_INTEREST_HIGHLANDERS);

		String[] labelsArr;
		String[] actionsArr;
		@DrawableRes int badgeRes;

		if (Utils.isStringValid(interest.getAvatarURL()))
			loadPictureWithConversion(interest.getAvatarURL(), interestPicture, PictureType.PROFILE);

		if (Utils.isStringValid(interest.getWallPictureURL()))
			loadPictureWithConversion(interest.getWallPictureURL(), interestWall, PictureType.WALL);

		interestName.setText(interest.getName());
		interestDescription.setText(interest.getHeadline());
		// INFO: 2019-09-27    don't remove the view for legacy. just hide it
		interestDescription.setVisibility(/*Utils.isStringValid(interest.getHeadline()) ? View.VISIBLE :*/ View.GONE);

//		Resources res = interestBtnFollowers.getResources();
//		// INFO: 2/11/19    followers info in CARD replaced by btn. "Hearts" info hidden
//		interestBtnFollowers.setText(res.getString(R.string.interest_followers, interest.getFollowersWithNumber(res)));
//		interestHeartsTotal.setText(interest.getHeartsWithNumber(interestHeartsTotal.getResources()));

		// INFO: 2019-11-18    dots removed like for USERS in the setLayout() method
//		toolbarDots.setVisibility(interest.isFollowed() ? View.VISIBLE : View.GONE);
		unFollowBtn.setText(interest.isFollowed() ? R.string.action_unfollow : R.string.action_follow);

		preferredLabel.setVisibility(
				(interest.isPreferred() && !mListener.getUser().isActingAsInterest()) ?
						View.VISIBLE : View.GONE
		);
		preferredLabelInvis.setVisibility(
				(interest.isPreferred() && !mListener.getUser().isActingAsInterest()) ?
						View.INVISIBLE : View.GONE
		);


		if (type == ProfileType.INTEREST_ME) {
			// TODO: 1/15/2018    fill in IF NECESSARY
			unFollowBtn.setVisibility(View.GONE);
		}
		else if (type == ProfileType.INTEREST_CLAIMED) {
			unFollowBtn.setVisibility(mListener.getUser().isActingAsInterest() ? View.GONE : View.VISIBLE);
		}

		// INFO: 2/25/19    for release no more buttons
		interestHeartsFollowersLayout.setVisibility(View.GONE);
//		interestBtnPromotions.setVisibility(View.GONE);


		claimInterestStatus.setVisibility(interest.isClaimedByYou() && interest.isClaimedPending() ? View.VISIBLE : View.GONE);

		// INFO: 2019-10-25    location icon visibility enabled only for INTEREST and of type LOCATION and EVENT (as per mockup)
		// TODO: 2019-09-27    implement icon visibility... from server???
		interestIconLocation.setVisibility(interest.wantsLocation() ? View.VISIBLE : View.GONE);
		interestLocationOrFreeText.setText(interest.getHeadline());

		ProfileTypeEnum intEnum = ProfileTypeEnum.toEnum(interest.getType());

		labelsArr = interestProfileBadge.getResources().getStringArray(ProfileTypeEnum.getProfileStringsArray(intEnum, false));
		actionsArr = interestProfileBadge.getResources().getStringArray(ProfileTypeEnum.getProfileActions(intEnum));
		badgeRes = ProfileTypeEnum.getBadgeIcon(intEnum);

		if (badgeRes != -1) {
			interestProfileBadge.setImageResource(badgeRes);
			interestProfileBadge.setVisibility(View.VISIBLE);
		} else {
			interestProfileBadge.setVisibility(View.GONE);
		}

		if (labelsArr.length == 3) {
			buttonAction1.setText(labelsArr[0]);
			buttonAction2.setText(labelsArr[1]);
			buttonAction3.setText(labelsArr[2]);
		}

		if (actionsArr.length == 3) configBtnActionsInterest(actionsArr, interest);

	}


	/**
	 * Maps buttons and related actions for HLUser and HLUserGeneric classes.
	 * @param actions the array of actions.
	 * @param user the HLUser instance if present.
	 * @param uGen the HLUserGeneric instance if present.
	 */
	private void configBtnActions(String[] actions, @Nullable HLUser user, @Nullable HLUserGeneric uGen) {

		String tmpOtherUserId = mListener.getUser().getUserId();
		if (uGen != null)
			tmpOtherUserId = uGen.getId();

		final String otherUserId = tmpOtherUserId;

		final boolean isActivityValid = mListener.getProfileFragment().getActivity() != null;

		buttonAction1.setOnClickListener(
				view -> {
					if (isActivityValid && !(Utils.checkAndOpenLogin(
							mListener.getProfileFragment().getActivity(),
							mListener.getUser(),
							HomeActivity.PAGER_ITEM_TIMELINE
					))) {
						if (actions[0].equals(Constants.PROFILE_ACTION_PROFILE_MY)) {

							CommonWebViewActivity.openCommonWebView(
									view.getContext(),
									WebViewType.PROFILE,
									otherUserId,
									null,
									R.anim.slide_in_right,
									R.anim.no_animation
							);

						} else if (actions[0].equals(Constants.PROFILE_ACTION_NEWSFEED)) {
							mListener.goToDiary();
						}
					}
				}
		);

		buttonAction2.setOnClickListener(
				view -> {
					if (isActivityValid && !(Utils.checkAndOpenLogin(
							mListener.getProfileFragment().getActivity(),
							mListener.getUser(),
							HomeActivity.PAGER_ITEM_TIMELINE
					))) {
						if (actions[1].equals(Constants.PROFILE_ACTION_EVENTS) ||
								actions[1].equals(Constants.PROFILE_ACTION_EVENTS_MY)) {

							CommonWebViewActivity.openCommonWebView(
									view.getContext(),
									actions[1].equals(Constants.PROFILE_ACTION_EVENTS_MY) ? WebViewType.EVENT : WebViewType.RT_EVENTS,
									otherUserId,
									null,
									R.anim.slide_in_right,
									R.anim.no_animation
							);
						}
					}
				}
		);

		buttonAction3.setOnClickListener(
				view -> {
					if (isActivityValid && !(Utils.checkAndOpenLogin(
							mListener.getProfileFragment().getActivity(),
							mListener.getUser(),
							HomeActivity.PAGER_ITEM_TIMELINE
					))) {
						if (actions[2].equals(Constants.PROFILE_ACTION_INTERESTS)) {
							mListener.goToInnerInterests();
						} else if (actions[2].equals(Constants.PROFILE_ACTION_DOCUMENTS)) {

							CommonWebViewActivity.openCommonWebView(
									view.getContext(),
									WebViewType.RT_DOCS,
									otherUserId,
									null,
									R.anim.slide_in_right,
									R.anim.no_animation
							);

						}
					}
				}
		);
	}


	/**
	 * Maps buttons and related actions for Interest class.
	 * @param actions the array of actions.
	 * @param interest the Interest instance.
	 */
	private void configBtnActionsInterest(String[] actions, @NonNull Interest interest) {

		final boolean isActivityValid = mListener.getProfileFragment().getActivity() != null;

		buttonAction1.setOnClickListener(
				view -> {
					if (isActivityValid && !(Utils.checkAndOpenLogin(
							mListener.getProfileFragment().getActivity(),
							mListener.getUser(),
							HomeActivity.PAGER_ITEM_TIMELINE
					))) {
						if (actions[0].equals(Constants.PROFILE_ACTION_DETAILS)) {

							CommonWebViewActivity.openCommonWebView(
									view.getContext(),
									WebViewType.RT_DETAILS,
									interest.getId(),
									null,
									R.anim.slide_in_right,
									R.anim.no_animation
							);

						} else if (actions[0].equals(Constants.PROFILE_ACTION_NEWSFEED)) {
							mListener.goToDiaryForInterest();
						}
					}
				}
		);

		buttonAction2.setOnClickListener(
				view -> {

					if (isActivityValid && !(Utils.checkAndOpenLogin(
							mListener.getProfileFragment().getActivity(),
							mListener.getUser(),
							HomeActivity.PAGER_ITEM_TIMELINE
					))) {

						WebViewType type = null;

						if (actions[1].equals(Constants.PROFILE_ACTION_EVENTS)) {
							type = WebViewType.RT_EVENTS;
						} else if (actions[1].equals(Constants.PROFILE_ACTION_AGENDA)) {
							type = WebViewType.RT_AGENDA;
						}

						if (type != null) {
							CommonWebViewActivity.openCommonWebView(
									view.getContext(),
									type,
									interest.getId(),
									null,
									R.anim.slide_in_right,
									R.anim.no_animation
							);
						}
					}
				}
		);

		buttonAction3.setOnClickListener(
				view -> {

					if (isActivityValid && !(Utils.checkAndOpenLogin(
							mListener.getProfileFragment().getActivity(),
							mListener.getUser(),
							HomeActivity.PAGER_ITEM_TIMELINE
					))) {

						WebViewType type = null;

						switch (actions[2]) {
							case Constants.PROFILE_ACTION_DOCUMENTS:
								type = WebViewType.RT_DOCS;
								break;

							case Constants.PROFILE_ACTION_CONTACTS:
								type = WebViewType.RT_CONTACTS;
								break;

							case Constants.PROFILE_ACTION_BIOGRAPHY:
								type = WebViewType.RT_BIO;
								break;
						}

						if (type != null) {
							CommonWebViewActivity.openCommonWebView(
									view.getContext(),
									type,
									interest.getId(),
									null,
									R.anim.slide_in_right,
									R.anim.no_animation
							);
						}
					}
				}
		);
	}


	void showAuthorizationRequestResult(boolean success) {
		if (inviteBtn != null) {
			userGen.setRequestsStatus(success ? RequestsStatusEnum.PENDING : RequestsStatusEnum.NOT_AVAILABLE);
			inviteBtn.setText(userGen.getRightStringForStatus());
			inviteBtn.setEnabled(userGen.getRequestsStatus() != RequestsStatusEnum.PENDING);
			inviteBtn.setVisibility(View.GONE);
		}
	}

	void handleMissingConnectionForInviteButton() {
		userGen.setRequestsStatus(RequestsStatusEnum.NOT_AVAILABLE);
		inviteBtn.setVisibility(View.GONE);
	}


	private void loadPictureWithConversion(final String pictureUrl, final ImageView view, final PictureType type) {
		if (Utils.isStringValid(pictureUrl) && view != null) {
			Glide.with(view).asBitmap().load(pictureUrl).into(new SimpleTarget<Bitmap>() {

				@Override
				public void onLoadFailed(@Nullable Drawable errorDrawable) {
					super.onLoadFailed(errorDrawable);
				}

				@Override
				public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
					Drawable d = new BitmapDrawable(view.getResources(), resource);

					if (Utils.isContextValid(view.getContext())) {
						if (Utils.hasLollipop())
							view.setImageDrawable(d);
						else
							MediaHelper.roundPictureCorners(view, d, 8f, false);
					}
					if (type == PictureType.WALL && mainBackground != null)
						MediaHelper.blurWallPicture(mainBackground.getContext(), d, mainBackground);
//  					mainBackground.setImageDrawable(d);


					/*
					Realm realm = RealmUtils.getCheckedRealm();
					try {
						HLUser user = new HLUser().readUser(realm);
						if (user != null) {
							if ((type == PictureType.PROFILE && user.getAvatarBase64() == null) ||
									(type == PictureType.WALL && user.getWallBase64() == null))
								new ConvertPictureToBase64(type).execute(resource);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (RealmUtils.isValid(realm))
							realm.close();
					}
					*/

				}
			});
		}
	}

	private void callServer() {
		Object[] result = null;
		try {
			roomInitializationCalled = true;
			result = HLServerCallsChat.initializeNewRoom(chatRoom);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		HLRequestTracker.getInstance(((DiplomaticiApp) mListener.getProfileFragment().getActivity().getApplication()))
				.handleCallResult(this, mListener.getProfileFragment().getActivity(), result, true, false);
	}


	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.back_arrow) {
			// only accessible button in GUEST MODE
			mListener.onBackClick();
		}
		else if (view.getContext() instanceof Activity &&
				!Utils.checkAndOpenLogin(((Activity) view.getContext()), mListener.getUser(), HomeActivity.PAGER_ITEM_TIMELINE)) {

			switch (view.getId()) {
				// toolbar #1
				case R.id.notification_btn_layout:
					mListener.onNotificationClick();
					break;
				case R.id.settings_btn:
					SettingsActivity.openSettingsMainFragment(view.getContext());
					break;

				// toolbar #2
//				case R.id.back_arrow:
//					mListener.onBackClick();
//					break;
				case R.id.dots:
					if (mType == ProfileType.INTEREST_NOT_CLAIMED || mType == ProfileType.INTEREST_CLAIMED)
						mListener.onDotsClick(view, null);
					else if (mType == ProfileType.FRIEND || mType == ProfileType.NOT_FRIEND)
						mListener.onDotsClickUser(view);
					break;

				// users card
				case R.id.card_profile:
				case R.id.profile_picture:
					// INFO: 2019-10-14    Diplomatici: NO OP
//					if (mType != ProfileType.NOT_FRIEND)
//						mListener.goToMyUserDetails();
					break;
				case R.id.invite_to_circle:
					// disables button as soon as it's tapped to prevent other requests
					showAuthorizationRequestResult(true);
					mListener.inviteToInnerCircle();
					break;
//				case R.id.me_friend_diary:
//				case R.id.not_friend_diary:
//					mListener.goToDiary();
//					break;
//				case R.id.me_friend_inner_C:
//				case R.id.not_friend_inner_c:
//					mListener.goToInnerCircle();
//					break;
//				case R.id.me_friend_inner_interests:
//					mListener.goToInnerInterests();
//					break;

				// interest card
				case R.id.card_interest:
				case R.id.interest_profile_picture:
					mListener.goToInterestDetails();
					break;
				case R.id.follow_unfollow_btn:
					if (interest != null && interest.isFollowed())
						mListener.unfollowInterest();
					else
						mListener.followInterest();
					break;
				case R.id.claim_button:
					mListener.goToClaimPage();
					break;

//				case R.id.interest_diary:
//					mListener.goToDiaryForInterest();
//					break;
//				case R.id.interest_followers:
//					mListener.goToFollowers();
//					break;
//				case R.id.interest_promotions:
//					// TODO: 2/8/19    REPLACE WITH LUISS CUSTOM ACTION
////					mListener.goToSimilar();
//					break;

				case R.id.main_action_btn:
				case R.id.bottom_global_search:
				case R.id.bottom_profile:
				case R.id.bottom_chats:
					onBottomBarClick(view);
					break;
			}
		}
	}


	private void onBottomBarClick(View view) {
		Context context = view.getContext();
		Intent intent = new Intent(context, HomeActivity.class);
		int page = 0;
		switch(view.getId()) {
			case R.id.main_action_btn:
				if (context instanceof Activity) {
					Intent createPostIntent = new Intent(context, CreatePostActivityMod.class);
					((Activity) context).startActivityForResult(createPostIntent, Constants.RESULT_CREATE_POST);
				}
				return;

			case R.id.bottom_global_search:
				page = HomeActivity.PAGER_ITEM_GLOBAL_SEARCH;
				break;
			case R.id.bottom_profile:
				page = HomeActivity.PAGER_ITEM_PROFILE;
				break;
			case R.id.bottom_chats:
				page = HomeActivity.PAGER_ITEM_CHATS;
				break;

			// TODO: 4/22/2018    understand what to do with bottom bat TIMELINE cta

//			case R.id.bottom_settings:
//				page = HomeActivity.PAGER_ITEM_SETTINGS;
//				break;
		}

		intent.putExtra(Constants.EXTRA_PARAM_1, page);

		context.startActivity(intent);
		if (context instanceof Activity)
			((Activity) context).finish();
	}

	// TODO: 4/23/2018     TEMPORARILY DISABLED
	@Override
	public void handleSuccessResponse(int operationId, JSONArray responseObject) {

		if (operationId == Constants.SERVER_OP_GET_NOTIFICATION_COUNT) {
			NotificationAndRequestHelper.handleDotVisibility(bottomBarNotificationDot, mListener.getUser().isValid());
			NotificationAndRequestHelper.handleDotVisibility(bottomBarNotificationDot, mListener.getUser().isValid());
		}
	}

	@Override
	public void handleErrorResponse(int operationId, int errorCode) {}

	@Override
	public void onMissingConnection(int operationId) {

	}

	void handleRoomInitialization(JSONArray responseObject) {
		JSONObject j = responseObject.optJSONObject(0);
		if (j != null) {
			chatRoom = ChatRoom.getRoom(j);
			chatRoom.setOwnerID(mListener.getUser().getUserId());
		}

		((HLActivity) mListener.getProfileFragment().getActivity()).getRealm().executeTransaction(realm -> realm.insertOrUpdate(chatRoom));

		if (chatRoom.isValid()) {
			ChatActivity.openChatMessageFragment(
					chatCallsLayout.getContext(),
					Objects.requireNonNull(chatRoom.getChatRoomID()),
					userGen.getCompleteName(),
					userGen.getAvatarURL(),
					false
			);
		}
	}

	private boolean roomInitializationCalled = false;
	private void initializeNewRoom(boolean noID) {
		RealmList<String> ids = new RealmList<>();
		ids.add(userGen.getId());
		chatRoom = new ChatRoom(
				mListener.getUser().getUserId(),
				ids,
				noID ? null : userGen.getChatRoomID()
		);

		callServer();
	}

	void setCallsToken(String callsToken) {
		this.callsToken = callsToken;
	}

	private void goToCallActivity(VoiceVideoCallType callType) {
		if (mListener.getProfileFragment().getActivity() instanceof HLActivity) {
			NotificationUtils.sendCallNotificationAndGoToActivity(
					((HLActivity) mListener.getProfileFragment().getActivity()),
					mListener.getUser().getUserId(),
					mListener.getUser().getUserCompleteName(),
					userGen,
					callType
			);
		}

	}


	//region == Getters and setters ==

	public boolean isRoomInitializationCalled() {
		return roomInitializationCalled;
	}

	public void setRoomInitializationCalled(boolean roomInitializationCalled) {
		this.roomInitializationCalled = roomInitializationCalled;
	}

	//endregion


	//region - Interface to Home for notifications -

//	@Override
//	public String getUserId() {
//		return null;
//	}

	public View getBottomBarNotificationDot() {
		return bottomBarNotificationDot;
	}

	//endregion



	public interface OnProfileInteractionsListener {
		@NonNull HLUser getUser();
		void goToMyUserDetails();
		void goToDiary();
		void goToInnerCircle();
		void goToInnerInterests();
		void inviteToInnerCircle();
		void onNotificationClick();
		void onBackClick();
		void onDotsClick(View dots, View preferredLabel);
		void onDotsClickUser(View dots);

		ProfileFragment getProfileFragment();

		void goToInterestDetails();
		void goToInitiatives();
		void goToDiaryForInterest();
		void goToSimilar();
		void goToClaimPage();
		void goToFollowers();
		void followInterest();
		void unfollowInterest();
	}


	private enum PictureType { PROFILE, WALL }
	private static class ConvertPictureToBase64 extends AsyncTask<Bitmap, Void, Void> {

		private PictureType type;

		ConvertPictureToBase64(PictureType type) {
			this.type = type;
		}

		@Override
		protected Void doInBackground(final Bitmap... bitmaps) {
			if (bitmaps[0] != null) {
				final String encoded = Utils.encodeBitmapToBase64(bitmaps[0]);

				if (Utils.isStringValid(encoded)) {
					Realm realm = RealmUtils.getCheckedRealm();
					if (RealmUtils.isValid(realm)) {
						realm.executeTransaction(new Realm.Transaction() {
							@Override
							public void execute(@NonNull Realm realm) {
								HLUser user = new HLUser().readUser(realm);
								if (type == PictureType.PROFILE) {
									user.setAvatarBase64(encoded);
									user.setAvatar(bitmaps[0]);
								}
								else if (type == PictureType.WALL) {
									user.setWallBase64(encoded);
									user.setWall(bitmaps[0]);
								}
							}
						});
					}
				}
			}

			return null;
		}
	}


	private class CardGlobalLayoutObserver implements ViewTreeObserver.OnGlobalLayoutListener {

		private WeakReference<View> lowerLayout;
		//		private WeakReference<View> centralButton;
		private WeakReference<View> chatCallsLayout;
		private WeakReference<ViewGroup> usersCard;

		public CardGlobalLayoutObserver(View lowerLayout, View chatCallsLayout, ViewGroup usersCard) {
			this.lowerLayout = new WeakReference<>(lowerLayout);
//			this.centralButton = new WeakReference<>(centralButton);
			this.chatCallsLayout = new WeakReference<>(chatCallsLayout);
			this.usersCard = new WeakReference<>(usersCard);
		}

		@Override
		public void onGlobalLayout() {

			int height = 0;
			if (lowerLayout != null && lowerLayout.get() != null)
				height = lowerLayout.get().getHeight();

//			LogUtils.d(LOG_TAG, "Hearts/Names layout height: " + height);

//			if (centralButton != null && centralButton.get() != null) {
//				if (height > 0) {
//					FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) centralButton.get().getLayoutParams();
//					lp.bottomMargin = height - Utils.dpToPx(18f, usersCard.get().getResources());
//					centralButton.get().setLayoutParams(lp);
//					centralButton.get().requestLayout();
//
//				}
//				else centralButton.get().setVisibility(View.GONE);
//			}

			if (chatCallsLayout != null && chatCallsLayout.get() != null) {
				if (height > 0) {
					FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) chatCallsLayout.get().getLayoutParams();
					lp.bottomMargin = height - Utils.dpToPx(18f, usersCard.get().getResources());
					chatCallsLayout.get().setLayoutParams(lp);
					chatCallsLayout.get().requestLayout();
				}
				else chatCallsLayout.get().setVisibility(View.GONE);
			}
		}
	}

}
