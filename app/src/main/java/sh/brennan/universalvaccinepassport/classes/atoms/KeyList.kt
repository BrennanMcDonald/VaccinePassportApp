package sh.brennan.universalvaccinepassport.classes.atoms

import com.google.gson.annotations.SerializedName
import sh.brennan.universalvaccinepassport.classes.room.Key

data class KeyList(
    @SerializedName("keys") val keys : List<Key>

)
