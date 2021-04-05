package rs.highlande.app.diplomatici.features.timeline.survey

import android.content.Context
import android.content.IntentFilter
import org.json.JSONArray
import rs.highlande.app.diplomatici.R
import rs.highlande.app.diplomatici.base.BaseHelper
import rs.highlande.app.diplomatici.base.DiplomaticiApp
import rs.highlande.app.diplomatici.base.HLActivity
import rs.highlande.app.diplomatici.connection.*
import rs.highlande.app.diplomatici.models.HLPosts
import rs.highlande.app.diplomatici.models.PostSurvey
import rs.highlande.app.diplomatici.utilities.Constants
import rs.highlande.app.diplomatici.utilities.LogUtils

/**
 * TODO - Class description
 * @author mbaldrighi on 2019-12-04.
 */
class SurveyVoteManager(
        context: Context,
        private val postId: String,
        private val survey: PostSurvey,
        private val listener: SurveyVoteListener
) : BaseHelper(context), OnServerMessageReceivedListener, OnMissingConnectionListener {

    private val messageReceiver = ServerMessageReceiver().apply { setListener(this@SurveyVoteManager) }

    override fun onStart() {
        super.onStart()

        registerReceiver(messageReceiver, IntentFilter(Constants.BROADCAST_SERVER_RESPONSE))
    }

    override fun onStop() {
        unregisterReceiver(messageReceiver)
        super.onStop()
    }


    override fun handleSuccessResponse(operationId: Int, responseObject: JSONArray?) {
        if (operationId == Constants.SERVER_OP_VOTE_SURVEY) {
            LogUtils.d(logTag, "Survey vote: SUCCESS")

            HLPosts.getInstance().apply {
                getPost(postId)?.let {
                    it.survey = this@SurveyVoteManager.survey
                    setPost(it, (contextRef.get() as? HLActivity)?.realm, true)
                }
            }

            listener.closeSurvey(postId)
        }
    }

    override fun handleErrorResponse(operationId: Int, errorCode: Int) {
        if (operationId == Constants.SERVER_OP_VOTE_SURVEY) {
            LogUtils.d(logTag, "Survey vote: FAIL")

            survey.answers.clear()
        }
    }

    override fun onMissingConnection(operationId: Int) {}


    fun vote() {
        val result = (contextRef.get() as? HLActivity)?.user?.let {
                HLServerCalls.voteSurvey(it.userId, postId, survey)
            }
        HLRequestTracker
                .getInstance(contextRef.get()?.applicationContext as? DiplomaticiApp)
                .handleCallResult(this, contextRef.get() as? HLActivity, result, false, true, R.string.voting)
    }


    interface SurveyVoteListener {
        fun closeSurvey(postId: String? = null)
    }


    companion object {
        val logTag = SurveyVoteManager::class.java.simpleName
    }

}