package rs.highlande.app.diplomatici.features.timeline.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.realm.RealmList
import kotlinx.android.synthetic.main.bottom_sheet_survey.*
import rs.highlande.app.diplomatici.R
import rs.highlande.app.diplomatici.models.HLPosts
import rs.highlande.app.diplomatici.models.PostSurvey
import rs.highlande.app.diplomatici.models.SurveyType
import rs.highlande.app.diplomatici.utilities.Constants

/**
 * TODO - Class description
 * @author mbaldrighi on 2019-12-02.
 */
class SurveyBottomFragment : BottomSheetDialogFragment() {

    private lateinit var postId: String
    private lateinit var survey: PostSurvey

    private var radioGroup: RadioGroup? = null
    private var voteManager: SurveyVoteManager? = null

    private val tmpAnswers = RealmList<String>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        postId = (savedInstanceState?.getString(Constants.EXTRA_PARAM_1, "") ?: arguments?.getString(Constants.EXTRA_PARAM_1, "") ?: "")
        survey = HLPosts.getInstance().getPost(postId).survey
        return prepareLayout(inflater.inflate(R.layout.bottom_sheet_survey, container, false))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!::postId.isInitialized) return
        if (!::survey.isInitialized) return

        voteManager = SurveyVoteManager(activity!!, postId, survey, parentFragment as SurveyVoteManager.SurveyVoteListener)
    }

    override fun onStart() {
        super.onStart()

        voteManager?.onStart()

        vote.visibility = if (!survey.hasAnswer() && survey.canVote) View.VISIBLE else View.GONE

        survey_question.text = survey.question
    }

    override fun onStop() {
        voteManager?.onStop()

        super.onStop()
    }


    private fun prepareLayout(baseView: View): View {

        baseView.findViewById<View>(R.id.cancel).setOnClickListener {
            dismiss()
        }
        baseView.findViewById<View>(R.id.vote).setOnClickListener {
            survey.answers.clear()
            survey.answers.addAll(tmpAnswers)
            voteManager?.vote()
        }

        val llayout = baseView.findViewById<LinearLayout>(R.id.optionsContainer)

        for (i in 0 until survey.possibleAnswers.size) {
            survey.possibleAnswers[i]?.let { option ->
                when (survey.getSurveyType()) {
                    SurveyType.CHECK -> {
                        val chBox = LayoutInflater
                                .from(baseView.context)
                                .inflate(R.layout.item_survey_check, llayout, false)
                                as CheckBox
                        chBox.id = i
                        chBox.text = option
                        chBox.tag = option

                        if (!survey.hasAnswer()) {
                            chBox.setOnCheckedChangeListener { view , isChecked ->
                                handleCheckOptions(view.tag as String, isChecked)
                            }
                        } else {
                            chBox.isChecked = survey.answers.contains(option)
                            chBox.setOnTouchListener { _, _ -> true }
                        }

                        llayout.addView(chBox)
                    }
                    SurveyType.RADIO -> {
                        if (radioGroup == null) {
                            radioGroup = RadioGroup(baseView.context)

                        }

                        radioGroup?.let {
                            val rBtn = LayoutInflater
                                    .from(baseView.context)
                                    .inflate(R.layout.item_survey_radio, it, false)
                                    as RadioButton
                            rBtn.id = i
                            rBtn.text = option
                            rBtn.tag = option

                            it.addView(rBtn)

                            if (!survey.hasAnswer()) {
                                it.setOnCheckedChangeListener { _, checkedId ->
                                    handleRadioOptions(it.findViewById<View>(checkedId).tag as String)
                                }
                            } else {
                                if (survey.answers.contains(option)) rBtn.isChecked = true
                                rBtn.setOnTouchListener { _, _ -> true }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        radioGroup?.let {
            llayout.addView(radioGroup)
        }

        return baseView

    }


    private fun handleRadioOptions(selection: String) {
        tmpAnswers.apply {
            clear()
            add(selection)
        }
    }

    private fun handleCheckOptions(selection: String, checked: Boolean) {
        tmpAnswers.apply {
            if (checked) add(selection)
            else remove(selection)
        }
    }


    companion object {

        @JvmStatic
        fun newInstance(postId: String): SurveyBottomFragment {

            val args = Bundle().apply {
                putString(Constants.EXTRA_PARAM_1, postId)
            }

            val fragment = SurveyBottomFragment()
            fragment.arguments = args
            return fragment
        }

    }


}