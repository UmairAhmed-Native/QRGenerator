package com.ambit.qrgenerator


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import com.ambit.qrgenerator.data.remote.dto.QrGenerationRequest
import com.ambit.qrgenerator.data.remote.dto.QrGenerationResponse
import com.ambit.qrgenerator.data.remote.services.ServicesClient
import com.ambit.qrgenerator.databinding.ActivityMainBinding
import com.github.alexzhirkevich.customqrgenerator.QrData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    enum class Action {
        SAVE,
        SHARE,
        NONE
    }

    private var recentSavedReceipt: File? = null
    private lateinit var binding: ActivityMainBinding
    private var isSavedImage: Boolean = false;

    val qrGenerator: (String) -> QrData = { iban: String ->
        QrData.Url(iban)

    }


    private var action: Action = Action.NONE

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Handle Permission granted/rejected
            if (isGranted) {
                saveQrInGallery()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Required Permission to Save QR",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_main
        )
        binding.btnGenerate.visibility = View.VISIBLE
        binding.btnSave.visibility = View.GONE
        binding.btnShare.visibility = View.GONE
        binding.rrQrImage.visibility = View.GONE

        binding.txtEdit.restrictAlphabetAndCharacter()

        binding.txtEdit.doOnTextChanged { _, _, _, count ->
            binding.btnGenerate.apply {
                isEnabled = count > 0
                alpha = if (count > 0) 1.0f else 0.5f
                isClickable = count > 0
            }
        }
        binding.btnGenerate.setOnClickListener {
            generateQr()
        }

    }


    private fun generateQr() {
        binding.progressDialog.visibility = View.VISIBLE
        val call = ServicesClient.apiService.generateQr(
            QrGenerationRequest(binding.txtEdit.text?.trim().toString())
        )
        call.enqueue(object : Callback<QrGenerationResponse> {
            override fun onResponse(
                call: Call<QrGenerationResponse>,
                response: Response<QrGenerationResponse>
            ) {
                if (response.isSuccessful) {
                    binding.progressDialog.visibility = View.GONE
                    response.body()?.content?.let {
                        showQR(it)
                    }

                } else {
                    binding.progressDialog.visibility = View.GONE
                    // Handle error
                }
            }

            override fun onFailure(call: Call<QrGenerationResponse>, t: Throwable) {
                binding.progressDialog.visibility = View.GONE
                // Handle failure
            }
        })

    }

    private fun showQR(qrString: String) {
        binding.qrImage.generateQRDrawable(
            data = qrGenerator(
                qrString
            )
        )
        binding.rrQrImage.visibility = View.VISIBLE
        binding.btnGenerate.visibility = View.GONE
        binding.btnSave.visibility = View.VISIBLE
        binding.btnShare.visibility = View.VISIBLE
        binding.btnShare.setOnClickListener {
            shareQr()
        }
        binding.btnSave.setOnClickListener {
            saveQrInGallery()
        }
    }

    private fun shareQr() {
        action = Action.SHARE
        try {
            val cachePath = File(this.cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream =
                FileOutputStream("$cachePath/image.png") // overwrites this image every time
            binding.rrQrImage.loadBitmapFromView().compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val imagePath = File(this.cacheDir, "images")
        val newFile = File(imagePath, "image.png")
        val contentUri =
            FileProvider.getUriForFile(
                applicationContext,
                "com.ambit.qrgenerator" + ".provider", newFile
            );
        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.setAction(Intent.ACTION_SEND)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            //            shareIntent.setDataAndType(contentUri, requireContext().getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            shareIntent.setType("image/*")
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        }
    }


    private fun saveQrInGallery() {
        if (checkPermission(activityResultLauncher)) {
            action = Action.SAVE
            val file = getImageFile()
            saveFile(binding.rrQrImage.loadBitmapFromView(), file)
        } else {
            Toast.makeText(this, "No Permission to Save QR", Toast.LENGTH_SHORT).show()
        }


    }

    private fun saveFile(bitmap: Bitmap, file: File) {
        try {
            val out: FileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
            //will be used for email
            recentSavedReceipt = file
            isSavedImage = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (action === Action.SAVE) {
            if (isSavedImage) {
                isSavedImage = false
                Toast.makeText(this, "Qr Saved", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Qr unable to save", Toast.LENGTH_SHORT).show()
        }
    }


}