package sh.brennan.universalvaccinepassport.android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.android.adapters.PassportListAdapter
import sh.brennan.universalvaccinepassport.classes.VaccinationStatus
import sh.brennan.universalvaccinepassport.classes.room.PassportDatabase
import sh.brennan.universalvaccinepassport.helpers.JWTHelper

class VaccineListFragment : Fragment(), PassportListAdapter.ContentListener {
    private lateinit var recyclerView: RecyclerView
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vaccine_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.passport_recycle_view)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    override fun onItemClicked(item: View, nickname: String) {

        /*
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager
            .defaultDisplay
            .getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        val popupView = layoutInflater.inflate(R.layout.popup_vaccinated, null)

        val popup = PopupWindow(popupView, (width * 0.80).roundToInt(), (height * 0.75).roundToInt())
        popup.isFocusable = true
        popup.animationStyle = R.style.PopupWindowAnimation
        popup.update()
        popup.showAtLocation(popupView, Gravity.BOTTOM, 0, 0)
        */


        val passports = PassportDatabase
            .getInstance(requireContext())
            .passportDao()
            .findByNickname(nickname)

        passports.observe(viewLifecycleOwner, { passport ->
            run {
                val fragment = when (JWTHelper.vaccineCount(passport.jwt)) {
                    0 -> {
                        VaccineStatusFragment.newInstance(passport.nickname, VaccinationStatus.UNVACCINATED)
                    }
                    1 -> {
                        VaccineStatusFragment.newInstance(passport.nickname, VaccinationStatus.PARTLY_VACCINATED)
                    }
                    else -> {
                        VaccineStatusFragment.newInstance(passport.nickname, VaccinationStatus.VACCINATED)
                    }
                }
                parentFragmentManager
                    .commit {
                        setCustomAnimations( R.anim.slide_up_in, R.anim.null_animation, R.anim.slide_down_in, R.anim.slide_down_in )
                        replace(R.id.fragment_container, fragment)
                        addToBackStack(null)
                    }
                /*
                val issuedDate = popupView.findViewById<TextView>(R.id.issued_date)
                val patientDetails = popupView.findViewById<TextView>(R.id.patient_details)
                val date = Date(BigDecimal(passport.jwt.payload.nbf).toLong() * 1000L);
                val df: DateFormat = SimpleDateFormat("dd/MM/yyyy")
                issuedDate.text = "Issued: ${df.format(date)}"
                patientDetails.text = JWTHelper.patientName(passport.jwt)[0]

                val qrImageView = popupView.findViewById<ImageView>(R.id.qr_code)
                val svgString = QRCodeHelper.createQRCode(passport.jwt.rawSHC)
                val svg = SVG.getFromString(svgString)
                val drawable = PictureDrawable(svg.renderToPicture(1024, 1024))

                qrImageView.setImageDrawable(drawable)

                val clickForRaw = popupView.findViewById<TextView>(R.id.tap_to_zoom)
                clickForRaw.setOnClickListener {
                    run {
                        val builder = AlertDialog.Builder(context)
                        builder.apply {
                            setPositiveButton(R.string.confirm,
                                DialogInterface.OnClickListener { dialog, id ->
                                    dialog.cancel();
                                })
                            setMessage(gson.toJson(passport.jwt.payload))
                        }
                        builder.create()
                        builder.show();
                    }
                } */
            }
        })
    }

    override fun onResume() {
        super.onResume()

        val passports = PassportDatabase.getInstance(requireContext()).passportDao().getAll()
        passports.observe(this, { passportList ->
            run {
                recyclerView.adapter = PassportListAdapter(passportList, this)
            }
        })
    }
}