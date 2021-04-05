/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmObject;
import rs.highlande.app.diplomatici.R;
import rs.highlande.app.diplomatici.base.HLFragment;
import rs.highlande.app.diplomatici.connection.HLServerCalls;
import rs.highlande.app.diplomatici.connection.OnMissingConnectionListener;
import rs.highlande.app.diplomatici.connection.ServerMessageReceiver;
import rs.highlande.app.diplomatici.features.profile.SelectLanguageActivity;
import rs.highlande.app.diplomatici.features.webView.CommonWebViewActivity;
import rs.highlande.app.diplomatici.features.webView.WebViewType;
import rs.highlande.app.diplomatici.models.HLIdentity;
import rs.highlande.app.diplomatici.utilities.AnalyticsUtils;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.DialogUtils;
import rs.highlande.app.diplomatici.utilities.Utils;

public class SettingsFragment extends HLFragment implements View.OnClickListener,
		ListView.OnItemClickListener, OnMissingConnectionListener {

	public static final String LOG_TAG = SettingsFragment.class.getCanonicalName();

	private static final int INDEX_ACCOUNT = 0;
	private static final int INDEX_ANAGRAPHICA = 1;
	//	private static final int INDEX_INNER_CIRCLE = 1;
	private static final int INDEX_ADDRESSES = 2;
	//	private static final int INDEX_FOLDERS = 2;
	private static final int INDEX_LANGUAGES = 3;
	private static final int INDEX_PRIVACY = 4;
	private static final int INDEX_SECURITY = 5;
	private static final int INDEX_PAYMENT = 6;

	// TODO: 3/30/2018    restore 6 as soon as payment setting is available
	private static int INDEX_HELP;

	private static String[] mainSettingsItems;

	//	private ImageView profilePicture;
	private ListView baseList;
	private ArrayAdapter<Object> baseAdapter;
	private List<Object> mItems = new ArrayList<>();


	public SettingsFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment ProfileFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static SettingsFragment newInstance() {
		SettingsFragment fragment = new SettingsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		onRestoreInstanceState(savedInstanceState != null ? savedInstanceState : getArguments());

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.home_fragment_settings, container, false);

		configureLayout(view);

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (Utils.isContextValid(getActivity())) {
			mainSettingsItems = getResources().getStringArray(R.array.settings_main_entries);
			baseAdapter = new SettingsMainListViewAdapter(getActivity(), R.layout.item_settings_entry, mItems, null);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		configureResponseReceiver();
	}

	@Override
	public void onResume() {
		super.onResume();

		AnalyticsUtils.trackScreen(getContext(), AnalyticsUtils.SETTINGS_MAIN);

		setLayout();
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button_logout) {
			logOut();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (Utils.isContextValid(getContext())) {
			if (position == INDEX_HELP) {

				////////////////////////////////////////////
				// TODO: 2019-11-15		For demo: DISABLE //
				////////////////////////////////////////////

//				settingsActivityListener.showSettingsHelpListFragment(new SettingsHelpElement(), false);
			}
			else if (parent.getItemAtPosition(position) instanceof HLIdentity) {
				HLIdentity identity = (HLIdentity) parent.getItemAtPosition(position);
				settingsActivityListener.showSettingsRedeemHeartsSelectFragment(
						RealmObject.isManaged(identity) ? realm.copyFromRealm(identity) : identity);
			}
			else {
				switch (position) {
					case INDEX_ACCOUNT:
						settingsActivityListener.showSettingsAccountFragment();
						break;

					case INDEX_ANAGRAPHICA:
						CommonWebViewActivity.openCommonWebView(
								view.getContext(),
								WebViewType.PERSONAL,
								mUser.getUserId(),
								null,
								R.anim.slide_in_right,
								R.anim.no_animation
						);
						break;

					case INDEX_ADDRESSES:
						CommonWebViewActivity.openCommonWebView(
								view.getContext(),
								WebViewType.CONTACT,
								mUser.getUserId(),
								null,
								R.anim.slide_in_right,
								R.anim.no_animation
						);
						break;

					case INDEX_LANGUAGES:
						startActivity(
								new Intent(view.getContext(), SelectLanguageActivity.class) {
									{ putExtra(Constants.EXTRA_PARAM_1, new ArrayList<>(mUser.getMoreAboutLanguages())); }
								}
						);
						break;


					case INDEX_PRIVACY:

						//////////////////////////////////////////////////////////////////
						// TODO: 2019-11-15		For demo point to google.com -> no Docs //
						//////////////////////////////////////////////////////////////////

						// TODO: 2019-10-23    check if still ok calling DOCS webview
						Utils.fireBrowserIntent/*ForDocs*/(view.getContext(), Constants.URL_PRIVACY, getString(R.string.click_agreement_privacy)/*, null*/);
						break;


					case INDEX_SECURITY:

						////////////////////////////////////////////
						// TODO: 2019-11-15		For demo: DISABLE //
						////////////////////////////////////////////

//						settingsActivityListener.showSettingsSecurityFragment();
						break;

					// TODO: 3/19/2018    soon to be done
//				case INDEX_PAYMENT:
//					SettingsActivity.openSettingsPaymentFragment(getContext());
//					break;

				}
			}
		}
	}


	@Override
	public void handleSuccessResponse(int operationId, JSONArray responseObject) {
		super.handleSuccessResponse(operationId, responseObject);
	}

	@Override
	public void handleErrorResponse(int operationId, int errorCode) {
		super.handleErrorResponse(operationId, errorCode);
	}

	@Override
	public void onMissingConnection(int operationId) {

	}


	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

	}

	@Override
	protected void configureResponseReceiver() {
		if (serverMessageReceiver == null)
			serverMessageReceiver = new ServerMessageReceiver();
		serverMessageReceiver.setListener(this);
	}

	@Override
	protected void configureLayout(@NonNull View view) {
//		profilePicture = view.findViewById(R.id.profile_picture);

		baseList = view.findViewById(R.id.base_list);
		baseList.setOnItemClickListener(this);

		view.findViewById(R.id.button_logout).setOnClickListener(this);
	}

	@Override
	protected void setLayout() {
//		MediaHelper.loadProfilePictureWithPlaceholder(getContext(), mUser.getAvatarURL(), profilePicture);
		settingsActivityListener.setToolbarTitle(R.string.title_activity_settings);
		baseList.setAdapter(baseAdapter);

		if (mItems != null)
			mItems.clear();

		mItems.addAll(Arrays.asList(mainSettingsItems));

		// INFO: 2/14/19    LUISS no CLAIM -> no PAGE MANAGEMENT
//		List<HLIdentity> nonProfits = mUser.getNonProfitIdentities();
//		if (nonProfits != null && !nonProfits.isEmpty())
//			mItems.addAll(mItems.size() - 1, nonProfits);

		INDEX_HELP = mItems.size() - 1;
	}


	private void logOut() {

		if (Utils.isContextValid(getActivity())) {
			final MaterialDialog dialog = DialogUtils.createGenericAlertCustomView(getActivity(), R.layout.custom_dialog_logout);
			if (dialog != null) {
				View view = dialog.getCustomView();
				if (view != null) {
					DialogUtils.setPositiveButton(
							view.findViewById(R.id.button_positive),
							R.string.action_logout,
							v -> {
								try {
									HLServerCalls.logout(getActivity(), mUser.getId());
								} catch (JSONException e) {
									e.printStackTrace();
								}

								Utils.logOut(getActivity(), dialog);
							}
					);

					view.findViewById(R.id.button_negative).setOnClickListener(v -> dialog.dismiss());
				}

				dialog.show();
			}

		}
	}

}
