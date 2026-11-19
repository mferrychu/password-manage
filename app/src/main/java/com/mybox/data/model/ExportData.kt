package com.mybox.data.model

import com.google.gson.annotations.SerializedName

data class ExportData(
    @SerializedName("appName")
    val appName: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("exportDate")
    val exportDate: Long,
    @SerializedName("passwords")
    val passwords: List<ExportPassword>,
    @SerializedName("events")
    val events: List<ExportEvent>
)

data class ExportPassword(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("account")
    val account: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("remark")
    val remark: String,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("updatedAt")
    val updatedAt: Long
)

data class ExportEvent(
    @SerializedName("id")
    val id: Long,
    @SerializedName("date")
    val date: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("color")
    val color: String? = null,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("updatedAt")
    val updatedAt: Long
)