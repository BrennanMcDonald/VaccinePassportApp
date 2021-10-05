package sh.brennan.universalvaccinepassport.classes.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import sh.brennan.universalvaccinepassport.classes.JWT

class JWTSerializer {
    val gson = Gson()
    @TypeConverter
    fun storedStringToJWT(value: String): JWT? {
        return gson.fromJson(value, JWT::class.java)
    }

    @TypeConverter
    fun jwtToStoredString(input: JWT): String {
        return gson.toJson(input)
    }
}