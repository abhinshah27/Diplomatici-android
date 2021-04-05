package rs.highlande.app.diplomatici.features.voiceVideoCalls.model

import rs.highlande.app.diplomatici.features.voiceVideoCalls.tmp.VoiceVideoCallType

data class IncomingCall(
        val id: String?,
        val fromIdentity: String?,
        val fromIdentityName: String?,
        val fromIdentityAvatar: String?,
        val fromIdentityWall: String?,
        val roomName: String?,
        val callType: VoiceVideoCallType = VoiceVideoCallType.VOICE
)
