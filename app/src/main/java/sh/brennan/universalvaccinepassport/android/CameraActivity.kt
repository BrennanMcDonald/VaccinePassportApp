package sh.brennan.universalvaccinepassport.android

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.analyzers.BarcodeAnalyzer
import sh.brennan.universalvaccinepassport.analyzers.SHCAnalyzer
import sh.brennan.universalvaccinepassport.android.dialogs.ValidityDialog
import sh.brennan.universalvaccinepassport.android.dialogs.VerifyQRDialog
import sh.brennan.universalvaccinepassport.classes.JWT
import sh.brennan.universalvaccinepassport.classes.room.Passport
import sh.brennan.universalvaccinepassport.classes.room.PassportDatabase
import sh.brennan.universalvaccinepassport.exceptions.WrongFormatException
import sh.brennan.universalvaccinepassport.helpers.KnownKeys
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class CameraActivity : AppCompatActivity(), VerifyQRDialog.VerifyQRDialogListener, ValidityDialog.ValidityDialogListener {
    private lateinit var fullscreenContent: ConstraintLayout
    private lateinit var fullscreenContentControls: LinearLayout
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var jwt: JWT

    private var processingBarcode: AtomicBoolean = AtomicBoolean(false)
    private var isFullscreen: Boolean = false
    private var isVerifyMode: Boolean = false
    private var gson: Gson = Gson()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_camera)
        supportActionBar?.hide()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        try {
            isVerifyMode = intent.extras?.getBoolean(getString(R.string.VERIFY_PASSPORT_FLAG))!!
        } catch(ex: Exception) {

        }

        viewFinder = findViewById(R.id.viewFinder)
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        isFullscreen = true

        fullscreenContent = findViewById(R.id.fullscreen_content)

        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        imageAnalysis = ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                    if (processingBarcode.compareAndSet(false, true)) {
                        searchBarcode(barcode)
                    }
                })
            }
    }

    override fun onResume() {
        super.onResume()
        processingBarcode.set(false)
    }

    private fun searchBarcode(barcode: String) {
        try {
            jwt = SHCAnalyzer.analyze(barcode)
            if (isVerifyMode) {
                val valid = if (KnownKeys.KnownKeyMap.containsKey(jwt.header.kid)) {
                    val key = KnownKeys.KnownKeyMap[jwt.header.kid]!!
                    SHCAnalyzer.verify(jwt, key)
                } else {
                    false
                }

                val message = if(jwt.payload.iss == "https://covidrecords.alberta.ca/smarthealth/issuer") {
                    "This code was issued by the Province of Alberta, due to this we are unable to verify this qr code at this time."
                } else {
                    ""
                }

                val dialog = ValidityDialog(valid, jwt, message)
                dialog.show(supportFragmentManager, "valid-qr")
            } else {
                val dialog = VerifyQRDialog(processingBarcode, jwt)
                dialog.show(supportFragmentManager, "verify-qr")
            }
        } catch (exception: WrongFormatException) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("This is not a valid vaccine passport QR code.")
                .setPositiveButton(
                    android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, which ->
                        processingBarcode.set(false)
                        dialog.dismiss();
                    })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        } catch (exception: IllegalArgumentException) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("QR Code is malformed, this is probably an issue with your local health authority.")
                .setPositiveButton(
                    android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, which ->
                        processingBarcode.set(false)
                        dialog.dismiss();
                    })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()

        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val context = this
        runBlocking {
            processingBarcode.set(false)
            val passportNickname =
                dialog.dialog?.findViewById<EditText>(R.id.editTextTextPersonName)?.text
            val passportToInsert =
                Passport(UUID.randomUUID().toString(), passportNickname.toString(), jwt)
            PassportDatabase.getInstance(context).passportDao().insertAll(passportToInsert)
            finish()
        }
    }

    override fun onDialogClose(dialog: DialogFragment) {
        processingBarcode.set(false)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dialog?.cancel()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}