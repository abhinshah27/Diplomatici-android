package rs.highlande.app.diplomatici.features.timeline.survey

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_survey.*
import rs.highlande.app.diplomatici.R
import rs.highlande.app.diplomatici.base.HLActivity
import rs.highlande.app.diplomatici.models.HLPosts
import rs.highlande.app.diplomatici.models.PostSurvey
import rs.highlande.app.diplomatici.utilities.AnalyticsUtils
import rs.highlande.app.diplomatici.utilities.Constants
import rs.highlande.app.diplomatici.utilities.Utils

/**
 * TODO - Class description
 * @author mbaldrighi on 2019-12-02.
 */
class SurveyActivity : HLActivity(), SurveyVoteManager.SurveyVoteListener {

    private var survey: PostSurvey? = null
    private var voteManager: SurveyVoteManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        manageIntent()

        vote.setOnClickListener {
            survey?.answers?.add(optionFreeText.text.toString())
            voteManager?.vote()
        }

        cancel.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down)
        }
    }


    override fun onStart() {
        super.onStart()

        surveyQuestion.text = survey?.question

        vote.visibility = if (survey?.hasAnswer() == false && survey?.canVote == true) View.VISIBLE else View.GONE

        optionFreeText.apply {
            visibility = View.VISIBLE

            if (survey?.hasAnswer() == true) {
                setText(survey!!.answers[0])
                setOnTouchListener { _, _ -> true }
            }
        }


        voteManager?.onStart()
    }


    override fun onResume() {
        super.onResume()

        AnalyticsUtils.trackScreen(this, AnalyticsUtils.FEED_SURVEY_TEXT)
    }


    override fun onStop() {
        voteManager?.onStop()

        super.onStop()
    }


    override fun configureResponseReceiver() {}

    override fun manageIntent() {
        intent?.let {
            val postId = it.getStringExtra(Constants.EXTRA_PARAM_1)
            if (Utils.isStringValid(postId)) {
                survey = HLPosts.getInstance().getPost(postId).survey

                if (survey != null) {
                    voteManager = SurveyVoteManager(this@SurveyActivity, postId, survey!!, this)
                }
            }
        }
    }


    override fun closeSurvey(postId: String?) {
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down)
    }
}