/*
 * Copyright (c) 2017. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.models.enums;

import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;

import rs.highlande.app.diplomatici.R;
import rs.highlande.app.diplomatici.utilities.Utils;

/**
 * @author mbaldrighi on 10/9/2019.
 */
public enum ProfileTypeEnum {

	NORMAL("userSubscribed"),			// user
	STAFF("staff"),					// user
	TEACHER("teacher"),				// user

	GENERAL("interestGeneral"),		// interest
	EVENT("interestEvent"),			// interest
	LOCATION("interestLocation"),		// interest
	SPEAKER("interestSpeaker");		// interest

	private String value;

	ProfileTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.getValue();
	}

	public static ProfileTypeEnum toEnum(String value) {
		if (!Utils.isStringValid(value))
			return null;

        ProfileTypeEnum[] statuses = ProfileTypeEnum.values();
		for (ProfileTypeEnum status : statuses)
			if (status.getValue().equalsIgnoreCase(value))
				return status;
		return null;
	}


	@ArrayRes
	public static int getProfileStringsArray(ProfileTypeEnum type, boolean isNormalUserMe) {
		switch (type) {
			case GENERAL:
			case SPEAKER:
				return R.array.profile_buttons_interest;
			case EVENT:
				return R.array.profile_buttons_event;
			case LOCATION:
				return R.array.profile_buttons_location;

			// INFO: 2019-11-04    now behaving like NORMAL interest
//			case SPEAKER:
//				return R.array.profile_buttons_speaker;

			case NORMAL:
				return isNormalUserMe ? R.array.profile_buttons_user_my : R.array.profile_buttons_user;
			case TEACHER:
			case STAFF:
				return R.array.profile_buttons_teacher_staff;
		}

		return -1;
	}

	@DrawableRes
	public static int getBadgeIcon(ProfileTypeEnum type) {
		if (type == null) return -1;

		switch (type) {
			case STAFF:
				return R.drawable.ic_staff;
			case TEACHER:
				return R.drawable.ic_teacher;
			case EVENT:
				return R.drawable.ic_event;
			case LOCATION:
				return R.drawable.ic_location;
			case SPEAKER:
				return R.drawable.ic_speaker;
			default:
				return -1;
		}
	}

	@ArrayRes
	public static int getProfileActions(ProfileTypeEnum type) {
		switch (type) {
			case NORMAL:
				return R.array.profile_action_key_user;
			case EVENT:
				return R.array.profile_action_key_event;
			case LOCATION:
				return R.array.profile_action_key_location;

			// INFO: 2019-11-04    now behaving like GENERAL interest
//			case SPEAKER:
//				return R.array.profile_action_key_speaker;
			default:
				return R.array.profile_action_key_staff_teacher_interest;
		}
	}

}
