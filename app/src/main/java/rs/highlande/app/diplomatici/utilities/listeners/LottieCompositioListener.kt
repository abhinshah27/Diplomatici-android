/*
 * Copyright (c) 2019. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.utilities.listeners

import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieListener
import rs.highlande.app.diplomatici.base.DiplomaticiApp

abstract class LottieCompositioListener: LottieListener<LottieComposition> {

    override fun onResult(result: LottieComposition?) {
        DiplomaticiApp.siriComposition = result
    }
}