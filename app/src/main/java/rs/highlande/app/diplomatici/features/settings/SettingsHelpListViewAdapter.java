/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import rs.highlande.app.diplomatici.R;
import rs.highlande.app.diplomatici.models.SettingsHelpElement;
import rs.highlande.app.diplomatici.utilities.Utils;

/**
 * @author mbaldrighi on 3/29/2018.
 */
public class SettingsHelpListViewAdapter extends ArrayAdapter<SettingsHelpElement> {

	private @LayoutRes int resourceId;

	public SettingsHelpListViewAdapter(@NonNull Context context, int resource, @NonNull List<SettingsHelpElement> objects) {
		super(context, resource, objects);

		resourceId = resource;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		SettingsHelpElement entry = getItem(position);

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
		}

		TextView text = convertView.findViewById(R.id.action_text);
		if (entry != null && Utils.isStringValid(entry.toString())) {
			text.setText(entry.getName());

			// enables light gray background if row has even position number
			convertView.setActivated((position % 2) == 0);
		}


		return convertView;
	}
}
