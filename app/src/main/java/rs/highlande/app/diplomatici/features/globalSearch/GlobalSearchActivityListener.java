/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.globalSearch;

import rs.highlande.app.diplomatici.models.enums.GlobalSearchTypeEnum;

/**
 * @author mbaldrighi on 4/10/2018.
 */
public interface GlobalSearchActivityListener {

	void showInterestsUsersListFragment(String query, GlobalSearchTypeEnum returnType, String title);
	void showGlobalTimelineFragment(String listName, String postId, String userId, String name,
	                                String avatarUrl, String query);
}
