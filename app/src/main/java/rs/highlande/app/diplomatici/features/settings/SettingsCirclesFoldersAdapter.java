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
import rs.highlande.app.diplomatici.base.BasicAdapterInteractionsListener;
import rs.highlande.app.diplomatici.models.HLCircle;
import rs.highlande.app.diplomatici.utilities.Constants;
import rs.highlande.app.diplomatici.utilities.Utils;

/**
 * @author mbaldrighi on 3/19/2018.
 */
public class SettingsCirclesFoldersAdapter extends ArrayAdapter<Object> {

	public enum ViewType { FOLDERS, CIRCLES }

	@LayoutRes
	private int resourceId;

	private ViewType mViewType;

	private BasicAdapterInteractionsListener mListener;

	public SettingsCirclesFoldersAdapter(@NonNull Context context, int resource, @NonNull CharSequence[] objects,
										 ViewType viewType,
										 BasicAdapterInteractionsListener listener) {
		super(context, resource, objects);

		this.resourceId = resource;
		this.mViewType = viewType;
		this.mListener = listener;
	}

	public SettingsCirclesFoldersAdapter(@NonNull Context context, int resource, @NonNull List<Object> objects,
										 ViewType viewType,
										 BasicAdapterInteractionsListener listener) {
		super(context, resource, objects);

		this.resourceId = resource;
		this.mViewType = viewType;
		this.mListener = listener;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		final Object obj = getItem(position);
		CharSequence entry = null;
		if (mViewType == ViewType.FOLDERS && obj instanceof CharSequence)
			entry = (CharSequence) obj;
		else if (mViewType == ViewType.CIRCLES && obj instanceof HLCircle)
			entry = ((HLCircle) obj).getNameToDisplay();

		SettingCircleVH viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

			viewHolder = new SettingCircleVH();
			viewHolder.circleName = convertView.findViewById(R.id.circle_name);
			viewHolder.iconEdit = convertView.findViewById(R.id.btn_edit);
			viewHolder.iconRemove = convertView.findViewById(R.id.btn_remove);

			viewHolder.iconEdit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onItemClick(obj, v);
				}
			});

			viewHolder.iconRemove.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onItemClick(obj, v);
				}
			});

			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (SettingCircleVH) convertView.getTag();
		}

		if (entry != null && Utils.isStringValid(entry.toString())) {
			viewHolder.circleName.setText(entry);

			if (mViewType == ViewType.CIRCLES) {
				String name = ((HLCircle) obj).getName();
				if ((name.equals(Constants.INNER_CIRCLE_NAME) || name.equals(Constants.CIRCLE_FAMILY_NAME))) {
					viewHolder.iconEdit.setVisibility(View.GONE);
					viewHolder.iconRemove.setVisibility(View.GONE);
				}
			}
		}


		return convertView;
	}


	static class SettingCircleVH {
		TextView circleName;
		View iconEdit, iconRemove;
	}
}
