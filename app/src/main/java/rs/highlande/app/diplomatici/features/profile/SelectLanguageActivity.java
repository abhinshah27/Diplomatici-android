/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import rs.highlande.app.diplomatici.R;
import rs.highlande.app.diplomatici.base.DiplomaticiApp;
import rs.highlande.app.diplomatici.base.HLActivity;
import rs.highlande.app.diplomatici.connection.HLRequestTracker;
import rs.highlande.app.diplomatici.connection.HLServerCalls;
import rs.highlande.app.diplomatici.connection.OnMissingConnectionListener;
import rs.highlande.app.diplomatici.connection.OnServerMessageReceivedListener;
import rs.highlande.app.diplomatici.connection.ServerMessageReceiver;
import rs.highlande.app.diplomatici.models.HLUser;
import rs.highlande.app.diplomatici.utilities.AnalyticsUtils;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.RealmUtils;
import rs.highlande.app.diplomatici.utilities.Utils;
import rs.highlande.app.diplomatici.utilities.media.MediaHelper;

/**
 * @author mbaldrighi on 4/30/2018.
 */
public class SelectLanguageActivity extends HLActivity implements OnServerMessageReceivedListener,
		OnMissingConnectionListener {

	private TextView toolbarTitle;
	private ImageView profilePicture;

	private List<LanguageObject> languages = new ArrayList<>();
	private ListView languagesView;
	private ArrayAdapter<LanguageObject> languagesAdapter;
	private ArrayList<String> selectedLangs = new ArrayList<>();

	private View noResult;


	private boolean forceBack = true;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_languages);
		setRootContent(R.id.root_content);

		manageIntent();

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
		profilePicture = toolbar.findViewById(R.id.profile_picture);

		toolbar.findViewById(R.id.back_arrow).setOnClickListener(v -> {


			saveLanguages();


//				if (selectedLangs != null && !selectedLangs.isEmpty()) {
//					Intent intent = new Intent();
//					intent.putExtra(Constants.EXTRA_PARAM_1, selectedLangs);
//					setResult(RESULT_OK, intent);
//				}
//				finish();
//				overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right);
		});

		languagesView = findViewById(R.id.language_list_view);
		languagesView.setOnItemClickListener((parent, view, position, id) -> {

			// one touch is enough to trigger saving
			forceBack = false;

			LanguageObject obj = languages.get(position);
			String lang = obj.text;
			if (selectedLangs.contains(lang))
				selectedLangs.remove(lang);
			else
				selectedLangs.add(lang);

			obj.setSelected(selectedLangs.contains(lang));

			languagesAdapter.notifyDataSetChanged();

		});
		languagesAdapter = new LanguagesAdapter(this, languages);

		noResult = findViewById(R.id.no_result);
	}

	@Override
	protected void onStart() {
		super.onStart();

		configureResponseReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();

		AnalyticsUtils.trackScreen(this, AnalyticsUtils.ME_LANGUAGES);

		getLanguages();

		languagesView.setAdapter(languagesAdapter);

		// fixes Crashlyt. #39
		if (mUser == null) {
			realm = RealmUtils.checkAndFetchRealm(realm);
			mUser = RealmUtils.checkAndFetchUser(realm, mUser);
		}
		if (mUser != null)
			MediaHelper.loadProfilePictureWithPlaceholder(this, mUser.getUserAvatarURL(), profilePicture);

			toolbarTitle.setText(R.string.title_activity_select_languages);
	}


	@Override
	public void onBackPressed() {
		if (forceBack)
			super.onBackPressed();
		else
			saveLanguages();
	}


	private void saveLanguages() {

		realm.executeTransactionAsync(realm -> {
			HLUser user = new HLUser().readUser(realm);

			RealmList<String> newLangs = new RealmList<>();
			newLangs.addAll(selectedLangs);
			user.getMoreAboutMe().setLanguages(newLangs);
		});

		Object[] result = null;
		try {
			result = HLServerCalls.updateLanguages(mUser.getUserId(), selectedLangs);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		HLRequestTracker.getInstance(((DiplomaticiApp) getApplication()))
				.handleCallResult(this, this, result);
	}



	private void getLanguages() {
		Object[] result = null;
		try {
			result = HLServerCalls.claimInterestGetLanguages();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		HLRequestTracker.getInstance(((DiplomaticiApp) getApplication()))
				.handleCallResult(this, this, result);
	}


	@Override
	protected void configureResponseReceiver() {
		if (serverMessageReceiver == null)
			serverMessageReceiver = new ServerMessageReceiver();
		serverMessageReceiver.setListener(this);
	}

	@Override
	protected void manageIntent() {
		Intent intent = getIntent();
		if (intent != null) {
			if (intent.hasExtra(Constants.EXTRA_PARAM_1))
				selectedLangs = intent.getStringArrayListExtra(Constants.EXTRA_PARAM_1);
		}
	}


	@Override
	public void handleSuccessResponse(int operationId, JSONArray responseObject) {
		super.handleSuccessResponse(operationId, responseObject);

		switch (operationId) {
			case Constants.SERVER_OP_CLAIM_INTEREST_GET_LANG:
				if (responseObject == null || responseObject.optJSONObject(0) == null) {
					handleErrorResponse(operationId, 0);
					return;
				}

				JSONArray languages = responseObject.optJSONObject(0).optJSONArray("languages");
				if (languages != null && languages.length() > 0) {

					languagesView.setVisibility(View.VISIBLE);
					noResult.setVisibility(View.GONE);

					this.languages.clear();
					for (int i = 0; i < languages.length(); i++) {
						LanguageObject lObj = new LanguageObject(languages.optString(i));
						lObj.setSelected(selectedLangs.contains(lObj.text));
						this.languages.add(lObj);
					}

					languagesAdapter.notifyDataSetChanged();
				}
				else
					handleErrorResponse(operationId, 0);

				break;

			case  Constants.SERVER_OP_UPDATE_PROFILE:
				finish();
				overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right);
		}
	}

	@Override
	public void handleErrorResponse(int operationId, int errorCode) {
		super.handleErrorResponse(operationId, errorCode);

		if (operationId == Constants.SERVER_OP_UPDATE_PROFILE) {
			finish();
			overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right);
			return;
		}

		languagesView.setVisibility(View.GONE);
		noResult.setVisibility(View.VISIBLE);
	}

	@Override
	public void onMissingConnection(int operationId) {
		handleErrorResponse(operationId, 0);
	}



	private class LanguagesAdapter extends ArrayAdapter<LanguageObject> {

		List<LanguageObject> items;


		public LanguagesAdapter(@NonNull Context context, @NonNull List<LanguageObject> objects) {
			super(context, R.layout.item_profile_language, objects);

			this.items = objects;
		}


		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
			LanguageObject object = items.get(position);

			LanguageVH holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_profile_language, parent, false);
				holder = new LanguageVH();
				holder.text = (TextView) convertView;
				convertView.setTag(holder);
			}
			else
				holder = (LanguageVH) convertView.getTag();


			if (parent instanceof ListView && object != null) {
				((ListView) parent).setItemChecked(position, object.isSelected());
				holder.text.setText(object.text);
			}

			return convertView;
		}

		@Override
		public int getCount() {
			return items.size();
		}


		private class LanguageVH {
			TextView text;
		}

	}


	private class LanguageObject {

		private final String text;
		private boolean selected;

		public LanguageObject(String text) {
			this.text = text;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof String) {
				return Utils.areStringsValid(text, ((String) obj)) && text.equals(obj);
			}
			return super.equals(obj);
		}

		public boolean isSelected() {
			return selected;
		}
		public void setSelected(boolean selected) {
			this.selected = selected;
		}
	}

}
