package sh.brennan.universalvaccinepassport.android

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.analyzers.SHCAnalyzer
import sh.brennan.universalvaccinepassport.classes.JWT
import sh.brennan.universalvaccinepassport.helpers.KnownKeys.Companion.KnownKeyMap

class VerifyActivity : AppCompatActivity() {
    private lateinit var jwt: JWT

    val gson: Gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        jwt = gson.fromJson(intent.extras?.getString("JWT"), JWT::class.java)

        // The ol' berta bypass
        if (KnownKeyMap.containsKey(jwt.header.kid) || jwt.payload.iss == "https://covidrecords.alberta.ca/smarthealth/issuer") {
            val key = KnownKeyMap[jwt.header.kid]!!
            (findViewById<TextView>(R.id.verify_status)).text = if(SHCAnalyzer.verify(jwt, key)) { "Valid" } else { "Invalid" }
        } else {
            (findViewById<TextView>(R.id.verify_status)).text = "Passport not recognized\nthis is probably because we haven't added your public health region"
        }
    }
}