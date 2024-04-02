package com.ambit.qrgenerator


import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.text.InputFilter
import android.text.Spanned
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ambit.qrgenerator.Constants.INITIAL_PERMS
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.util.Date


fun TextInputEditText.restrictAlphabetAndCharacter() = run {
    val regex = Regex("^[0-9a-zA-Z ]+$")
    this.filters = arrayOf<InputFilter>(
        object : InputFilter {
            override fun filter(
                source: CharSequence, start: Int,
                end: Int, dest: Spanned?, dstart: Int,
                dend: Int
            ): CharSequence {
                if (source == "") { // for backspace
                    return source
                }
                return if (source.toString().matches(regex)) {
                    source
                } else ""
            }

        }
    )
}

fun ImageView.generateQRDrawable(data: QrData) = run {
    val options by lazy {
        createQrVectorOptions {

            padding = .225f

            fourthEyeEnabled = true

            background {
                color = QrVectorColor.Solid(
                    ContextCompat.getColor(
                        context,
                        android.R.color.transparent
                    )
                )
            }

            logo {
                drawable = ContextCompat
                    .getDrawable(context, R.drawable.alb_logo)
                size = .25f
                padding = QrVectorLogoPadding.Natural(.2f)
                shape = QrVectorLogoShape
                    .Circle
            }
            colors {
                dark = QrVectorColor
                    .RadialGradient(
                        colors = listOf(
                            0f to ContextCompat.getColor(context, R.color.black),
                            1f to ContextCompat.getColor(context, R.color.black),
                        )
                    )
                ball = QrVectorColor.Solid(
                    ContextCompat.getColor(context, R.color.black)
                )
            }
            shapes {
                darkPixel = QrVectorPixelShape
                    .RoundCorners(.5f)
                ball = QrVectorBallShape
                    .RoundCorners(.25f)
                frame = QrVectorFrameShape
                    .RoundCorners(.25f)
            }
        }
    }

    this.setImageDrawable(QrCodeDrawable(data, options))
}


fun ConstraintLayout.loadBitmapFromView(): Bitmap {
    setDrawingCacheEnabled(true)
    buildDrawingCache(true)
    setDrawingCacheEnabled(false)
    val b =
        Bitmap.createBitmap(measuredWidth, measuredHeight + 100, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)
    //        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight() + 100);
    draw(c)
    return b
}

fun Context.getImageFile(): File {
    val root = Environment.getExternalStorageDirectory().absolutePath
    val myDir = File(root + "/Pictures/" + "Qr Generate")
    myDir.mkdirs()
    val fname: String = ("qr-" + Date().getTime()).toString() + ".png"
    val file = File(myDir.absolutePath, fname)
    if (file.exists()) {
        file.delete()
    }
    return file
}

fun Activity.checkPermission(registerForActivityResult: ActivityResultLauncher<String>): Boolean {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
            }

            else -> {
                registerForActivityResult.launch(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }
    return true;
}
