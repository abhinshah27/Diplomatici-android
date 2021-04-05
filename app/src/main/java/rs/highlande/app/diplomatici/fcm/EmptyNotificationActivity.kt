package rs.highlande.app.diplomatici.fcm

import android.content.Intent
import android.os.Bundle
import rs.highlande.app.diplomatici.base.HLActivity
import rs.highlande.app.diplomatici.features.HomeActivity
import rs.highlande.app.diplomatici.features.profile.ProfileActivity
import rs.highlande.app.diplomatici.utilities.Constants

/**
 * TODO - Class description
 * @author mbaldrighi on 2019-09-25.
 */
class EmptyNotificationActivity : HLActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manageIntent()

        finish()

    }


    override fun configureResponseReceiver() {}

    override fun manageIntent() {

        intent?.let {

            val foreground = intent.getBooleanExtra(Constants.KEY_NOTIFICATION_FOREGROUND, true)

            when (it.extras?.getString(Constants.KEY_NOTIFICATION_RECEIVED)) {
                Constants.CODE_NOTIFICATION_GENERIC -> ProfileActivity.openNotificationsFragmentFromNotification(this, foreground)
                Constants.CODE_NOTIFICATION_CHAT -> {
                    val extra = Intent(this, HomeActivity::class.java)
                    extra.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    extra.putExtra(Constants.KEY_NOTIFICATION_RECEIVED, Constants.CODE_NOTIFICATION_CHAT)
                    extra.putExtra(Constants.KEY_NOTIFICATION_FOREGROUND, foreground)
                    startActivity(extra)
                }

                else -> return@let

            }

            val notifId = intent.getStringExtra(Constants.KEY_NOTIFICATION_ID)
            SendNotificationOpenService.startService(this, notifId)

        }

    }

}