package com.ae.ringophone.remote

data class StatusDataModel(
    val participant: String? = null,
    val type: StatusDataModelTypes? = null,
)

// status of the firebase
enum class StatusDataModelTypes {
    IDLE, LookingForMatch, OfferedMatch, ReceivedMatch, Connected
}