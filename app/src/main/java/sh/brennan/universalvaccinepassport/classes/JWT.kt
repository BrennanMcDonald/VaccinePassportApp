package sh.brennan.universalvaccinepassport.classes

import com.google.gson.annotations.SerializedName

data class JWT(
    @SerializedName("header") val header: JWTHeader,
    @SerializedName("payload") val payload: JWTPayload,
    @SerializedName("rawSHC") val rawSHC: String,
)
