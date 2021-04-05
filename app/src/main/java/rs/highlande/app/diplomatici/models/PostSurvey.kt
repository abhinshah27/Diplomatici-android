package rs.highlande.app.diplomatici.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import rs.highlande.app.diplomatici.utilities.Utils
import java.io.Serializable

/**
 * Nested class for [Post] containing the new "survey" type.
 * @author mbaldrighi on 2019-12-02.
 */
@RealmClass
open class PostSurvey: RealmModel, Serializable {

    var type = ""
    var question = ""
    var possibleAnswers = RealmList<String>()
    var answers = RealmList<String>()
    var canVote = true


    fun getSurveyType(): SurveyType? = SurveyType.toEnum(type)

    fun isRadio() = getSurveyType() == SurveyType.RADIO
    fun isCheck() = getSurveyType() == SurveyType.CHECK
    fun isOpenAnswer() = getSurveyType() == SurveyType.ANSWER

    fun hasAnswer() = !answers.isNullOrEmpty()
}


enum class SurveyType(val value: String) {
    @SerializedName("answer") ANSWER("answer"),
    @SerializedName("radio") RADIO("radio"),
    @SerializedName("check") CHECK("check");

    companion object {
        fun toEnum(value: String?): SurveyType? {
            if (!Utils.isStringValid(value)) return null

            val statuses = values()
            for (status in statuses) if (status.value.equals(value, ignoreCase = true)) return status
            return null
        }
    }
}