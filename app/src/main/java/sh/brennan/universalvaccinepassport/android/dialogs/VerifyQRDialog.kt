package sh.brennan.universalvaccinepassport.android.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.classes.JWT
import sh.brennan.universalvaccinepassport.helpers.JWTHelper
import java.util.concurrent.atomic.AtomicBoolean

class VerifyQRDialog(val processingBarcode: AtomicBoolean, val jwt: JWT) : DialogFragment() {

    internal lateinit var listener: VerifyQRDialogListener

    interface VerifyQRDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as VerifyQRDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            val view = inflater.inflate(R.layout.dialog_qr_results, null);

            (view.findViewById<TextView>(R.id.vaccine_data)).text = JWTHelper.patientInfo(jwt).joinToString(separator = "")

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(view)
                .setTitle(getString(R.string.set_passport_name))
                // Add action buttons
                .setPositiveButton(R.string.confirm,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogPositiveClick(this);
                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this);
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}