package sh.brennan.universalvaccinepassport.classes.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface KeyDao {
    @Query("SELECT * FROM `key`")
    fun getAll(): LiveData<List<Key>>

    @Query(
        "SELECT * FROM `key` WHERE kid LIKE :kid LIMIT 1"
    )
    fun findByKid(kid: String): LiveData<Key>

    @Insert
    suspend fun insertAll(vararg keys: Key)

    @Delete
    suspend fun delete(key: Key)
}
