// LANE STUB — deleted by the orchestrator when lane A merges. Do not build on it.
package com.astraedus.nudge.ui.qr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContract

class ScanQrContract : ActivityResultContract<ScanQrContract.Request, String?>() {
    data class Request(val title: String, val subtitle: String? = null)

    override fun createIntent(context: Context, input: Request): Intent =
        Intent(Intent.ACTION_VIEW)

    override fun parseResult(resultCode: Int, intent: Intent?): String? = null
}

object QrCodeGenerator {
    fun generate(content: String, sizePx: Int = 1024): Bitmap =
        Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
}

object QrShare {
    fun share(context: Context, bitmap: Bitmap, fileName: String, chooserTitle: String) = Unit
}
