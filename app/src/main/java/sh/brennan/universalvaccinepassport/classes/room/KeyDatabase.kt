package sh.brennan.universalvaccinepassport.classes.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.helpers.SingletonHolder

@Database(entities = arrayOf(Key::class), version = 1)
abstract class KeyDatabase : RoomDatabase() {
    abstract fun keyDao(): KeyDao

    companion object : SingletonHolder<KeyDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, KeyDatabase::class.java, it.applicationContext.getString(
            R.string.KEY_DATABASE_NAME)).build()
    })
}