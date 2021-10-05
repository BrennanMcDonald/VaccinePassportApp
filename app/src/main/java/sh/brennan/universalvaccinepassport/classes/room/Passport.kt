package sh.brennan.universalvaccinepassport.classes.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import sh.brennan.universalvaccinepassport.classes.JWT

@Entity
data class Passport (
    @PrimaryKey
    @SerializedName("uuid")
    @ColumnInfo(name = "uuid")
    var uuid: String,

    @SerializedName("nickname")
    @ColumnInfo(name = "nickname")
    var nickname: String,

    @SerializedName("jwt")
    @ColumnInfo(name = "jwt")
    var jwt: JWT
)