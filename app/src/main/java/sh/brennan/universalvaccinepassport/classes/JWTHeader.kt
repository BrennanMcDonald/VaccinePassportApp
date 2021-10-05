package sh.brennan.universalvaccinepassport.classes

import com.google.gson.annotations.SerializedName

data class JWTHeader(
    @SerializedName("zip") val zip : String,
    @SerializedName("alg") val alg : String,
    @SerializedName("kid") val kid : String
)