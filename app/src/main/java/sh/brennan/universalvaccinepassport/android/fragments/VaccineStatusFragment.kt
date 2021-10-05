package sh.brennan.universalvaccinepassport.android.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.caverock.androidsvg.SVG
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.classes.room.PassportDatabase
import sh.brennan.universalvaccinepassport.helpers.JWTHelper
import sh.brennan.universalvaccinepassport.helpers.QRCodeHelper
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import android.graphics.drawable.ColorDrawable
import sh.brennan.universalvaccinepassport.classes.VaccinationStatus

class VaccineStatusFragment : Fragment() {
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vaccineStatus = arguments?.getSerializable("VACCINE_STATUS")
        val vaccineStatusView = inflater.inflate(R.layout.fragment_vaccine_status, container, false)

        vaccineStatusView.setBackgroundColor(when(vaccineStatus) {
            VaccinationStatus.VACCINATED -> Color.parseColor("#2E8540")
            VaccinationStatus.PARTLY_VACCINATED -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#F44336")
        });

        vaccineStatusView.findViewById<TextView>(R.id.vaccinated_status).text = vaccineStatus.toString()
        return vaccineStatusView;
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = context

        arguments?.getString(getString(R.string.fragment_nickname_data))?.let {
            val passports = PassportDatabase
                .getInstance(requireContext())
                .passportDao()
                .findByNickname(it)

            passports.observe(viewLifecycleOwner, Observer { passport ->
                run {
                    // apply passport
                    val issuedDate = view.findViewById<TextView>(R.id.issued_date)
                    val patientDetails = view.findViewById<TextView>(R.id.patient_details)
                    val date = Date(BigDecimal(passport.jwt.payload.nbf).toLong() * 1000L)
                    val df: DateFormat = SimpleDateFormat("dd/MM/yyyy")
                    issuedDate.text = "Issued: ${df.format(date)}"
                    patientDetails.text = JWTHelper.patientName(passport.jwt)[0]

                    val qrImageView = view.findViewById<ImageView>(R.id.qr_code)
                    val svgString = QRCodeHelper.createQRCode(passport.jwt.rawSHC)
                    val svg = SVG.getFromString(svgString)
                    val drawable = PictureDrawable(svg.renderToPicture(1024, 1024))
                    qrImageView.setImageDrawable(drawable)
                    qrImageView.setOnClickListener {
                        val displayMetrics = DisplayMetrics()
                        (context as Activity?)!!.windowManager
                            .defaultDisplay
                            .getMetrics(displayMetrics)
                        qrImageView.visibility = View.INVISIBLE
                        val height = displayMetrics.heightPixels
                        val width = displayMetrics.widthPixels
                        val popupView = ImageView(context)
                        popupView.setImageDrawable(drawable)
                        val popup = PopupWindow(
                            popupView,
                            (width * 0.90).roundToInt(),
                            (height * 0.50).roundToInt()
                        )
                        popupView.setOnClickListener {
                            popup.dismiss()
                        }
                        popup.setOnDismissListener {
                            qrImageView.visibility = View.VISIBLE
                        }
                        popup.isOutsideTouchable = true;
                        popup.isFocusable = true;
                        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                        popup.update()
                        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0)
                    }
                }
            })
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(nickname: String, vaccineStatus: VaccinationStatus) = VaccineStatusFragment().apply {
            arguments = Bundle().apply {
                putSerializable("VACCINE_STATUS", vaccineStatus)
                putString("FRAGMENT_NICKNAME_DATA", nickname)
            }
        }
    }
}