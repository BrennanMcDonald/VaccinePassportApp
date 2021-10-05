package sh.brennan.universalvaccinepassport.android

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.android.fragments.VaccineListFragment
import sh.brennan.universalvaccinepassport.android.fragments.VaccineStatusFragment
import sh.brennan.universalvaccinepassport.classes.VaccinationStatus
import sh.brennan.universalvaccinepassport.classes.room.PassportDatabase
import sh.brennan.universalvaccinepassport.helpers.JWTHelper


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.elevation = 0f

        val prefs = getSharedPreferences(getString(R.string.VAX_PASSPORT_PREFS), Context.MODE_PRIVATE)

        val widgetId = intent.getIntExtra("WIDGET_ID", -1)
        if (widgetId > 0) {
            val nickname = loadNicknamePref(this, widgetId)
            Log.d("ASD", nickname)
        }

        if (prefs.getBoolean(getString(R.string.FIRST_LAUNCH_PREF),true)) {
            val startCameraIntent = Intent(this, CameraActivity::class.java)
            prefs.edit().putBoolean(getString(R.string.FIRST_LAUNCH_PREF), false).apply()
            startActivity(startCameraIntent)
        }
    }


    override fun onResume() {
        super.onResume()
        val passports = PassportDatabase.getInstance(this).passportDao().getAll()
        passports.observe(this, { passportList ->
            run {
                val fragment: Fragment = if (passportList.count() == 1) {
                    when (JWTHelper.vaccineCount(passportList[0].jwt)) {
                        0 -> {
                            VaccineStatusFragment.newInstance(
                                passportList[0].nickname,
                                VaccinationStatus.UNVACCINATED
                            )
                        }
                        1 -> {
                            VaccineStatusFragment.newInstance(
                                passportList[0].nickname,
                                VaccinationStatus.PARTLY_VACCINATED
                            )
                        }
                        else -> {
                            VaccineStatusFragment.newInstance(
                                passportList[0].nickname,
                                VaccinationStatus.VACCINATED
                            )
                        }
                    }
                }else if (passportList.count() == 0) {
                    val startCameraIntent = Intent(this, CameraActivity::class.java)
                    startActivity(startCameraIntent)
                    VaccineListFragment()
                }else {
                    VaccineListFragment()
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_add_passport -> {
                val startCameraIntent = Intent(this, CameraActivity::class.java)
                startActivity(startCameraIntent)
                true
            }
            R.id.action_verify -> {
                val startCameraIntent = Intent(this, CameraActivity::class.java)
                startCameraIntent.putExtra(getString(R.string.VERIFY_PASSPORT_FLAG), true)
                startActivity(startCameraIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun saveNicknamePref(context: Context, appWidgetId: Int, text: String?) {
        val prefs: SharedPreferences.Editor = context.getSharedPreferences("WIDGET_PREFS", 0).edit()
        prefs.putString("WIDGET_NICKNAME_$appWidgetId", text)
        prefs.apply()
    }

    fun loadNicknamePref(context: Context, appWidgetId: Int): String {
        val prefs: SharedPreferences = context.getSharedPreferences("WIDGET_PREFS", 0)
        val prefix = prefs.getString("WIDGET_NICKNAME_$appWidgetId", null)
        return prefix ?: context.getString(R.string.appwidget_prefix_default)
    }
}