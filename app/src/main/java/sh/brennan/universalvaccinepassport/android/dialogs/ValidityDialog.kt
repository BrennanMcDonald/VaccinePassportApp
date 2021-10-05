package sh.brennan.universalvaccinepassport.android.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.classes.JWT
import sh.brennan.universalvaccinepassport.helpers.JWTHelper

class ValidityDialog(val valid: Boolean, val jwt: JWT, val message: String = "") :
    DialogFragment() {
    internal lateinit var listener: ValidityDialogListener

    interface ValidityDialogListener {
        fun onDialogClose(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as ValidityDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement ValidityDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.popup_validator, null)
            val vaccineCount = JWTHelper.vaccineCount(jwt)
            val statusText = view.findViewById<TextView>(R.id.valid_status_text)

            view.findViewById<TextView>(R.id.patient_name).text =
                "Name: " + JWTHelper.patientName(jwt)[0]

            if (valid && vaccineCount > 1) {
                statusText.text =
                    getString(R.string.FULLY_VACCINATED)
                view.setBackgroundColor(Color.parseColor("#4CAF50"))
                view.findViewById<ImageView>(R.id.valid_status_image).setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.valid))
            } else if (vaccineCount <= 1) {
                statusText.text =
                    "Valid passport, however person only has $vaccineCount vaccinations"
                view.setBackgroundColor(Color.parseColor("#FF9800"))
                view.findViewById<ImageView>(R.id.valid_status_image).setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.invalid))
            } else {
                view.findViewById<ImageView>(R.id.valid_status_image).setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.warning))
                if (message != "") {
                    view.setBackgroundColor(Color.parseColor("#FF9800"))
                    statusText.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    statusText.text = message
                } else {
                    view.setBackgroundColor(Color.RED)
                    statusText.text =
                        getString(R.string.POSSIBLE_FORGERY)
                }
            }

            builder.setView(view)
                .setPositiveButton(
                    R.string.confirm
                ) { dialog, id ->
                    listener.onDialogClose(this)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}