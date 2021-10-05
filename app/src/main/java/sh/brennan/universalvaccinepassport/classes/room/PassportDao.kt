package sh.brennan.universalvaccinepassport.classes.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PassportDao {
    @Query("SELECT * FROM passport")
    fun getAll(): LiveData<List<Passport>>

    @Query(
        "SELECT * FROM passport WHERE uuid LIKE :uuid LIMIT 1"
    )
    fun findByUUID(uuid: String): LiveData<Passport>

    @Query(
        "SELECT * FROM passport WHERE nickname LIKE :nickname LIMIT 1"
    )
    fun findByNickname(nickname: String): LiveData<Passport>

    @Insert
    suspend fun insertAll(vararg passports: Passport)

    @Delete
    suspend fun delete(passport: Passport)
}
