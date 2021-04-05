/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.features.wishes;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author mbaldrighi on 9/24/2018.
 */
public interface OnHandlingScrollPositionListener {
	void saveScrollView(int position, RecyclerView scrollView);
	void restoreScrollView(int position);
}
