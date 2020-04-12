package com.demorealtimedb

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserInfo(
    var name: String? = "",
    var mobile: String? = ""
)