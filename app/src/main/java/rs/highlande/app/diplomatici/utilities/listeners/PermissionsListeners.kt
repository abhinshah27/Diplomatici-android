/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package rs.highlande.app.diplomatici.utilities.listeners

/**
 * @author mbaldrighi on 11/13/2018.
 */
interface OnPermissionsDenied {
    fun handlePermissionsDenied(requestedPermission: Int)
}

interface OnPermissionsNeeded {
    fun handlePermissionsNeeded(neededPermissions: Int): Boolean
}

