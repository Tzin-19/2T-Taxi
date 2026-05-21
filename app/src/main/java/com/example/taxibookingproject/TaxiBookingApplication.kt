package com.example.taxibookingproject

import android.app.Application
import com.cloudinary.android.MediaManager

class TaxiBookingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Cấu hình Cloudinary
        val config = mapOf(
            "cloud_name" to "dznjtehfo", // Thay bằng cloud_name của bạn
            "api_key" to "857363241591554",       // Thay bằng api_key của bạn
            "api_secret" to "osaitOIinz_m5FAmqgqgGRd0_vE"   // Thay bằng api_secret của bạn
        )
        MediaManager.init(this, config)
    }
}
