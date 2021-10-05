package sh.brennan.universalvaccinepassport.classes.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.helpers.SingletonHolder

@Database(entities = arrayOf(Passport::class), version = 1)
@TypeConverters(JWTSerializer::class)
abstract class PassportDatabase : RoomDatabase() {
    abstract fun passportDao(): PassportDao

    companion object : SingletonHolder<PassportDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, PassportDatabase::class.java, it.applicationContext.getString(
            R.string.PASSPORT_DATABASE_NAME)).build()
    })
}