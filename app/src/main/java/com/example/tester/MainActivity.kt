package com.example.tester

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tester.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCall.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                initiateWhatsAppCall()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    READ_CONTACTS_PERMISSION_CODE
                )
            }
        }
    }


    private fun initiateWhatsAppCall() {
        val mimeString = "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"

        val resolver: ContentResolver = applicationContext.contentResolver
        val cursor: Cursor? = resolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME
        )

        while (cursor!!.moveToNext()) {
            var Col_Index =
                cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
            val _id = cursor.getLong(Col_Index)

            Col_Index =
                cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            var number = cursor.getString(Col_Index)

            Col_Index =
                cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val displayName = cursor.getString(Col_Index)

            Col_Index =
                cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.MIMETYPE)
            val mimeType = cursor.getString(Col_Index)

            var my_number = "0704848277"
            my_number = my_number.replace(" ", "")
            my_number = my_number.replace("+", "")

            if (number.isNullOrBlank() == false) {
                number = number.replace(" ", "")
                number = number.replace("+", "")

                if (number.endsWith(my_number.substring(1) + "@s.whatsapp.net")) {
                    if (mimeType.equals(mimeString)) {
                        val data = "content://com.android.contacts/data/$_id"
                        val sendIntent = Intent()
                        sendIntent.action = Intent.ACTION_VIEW
                        sendIntent.setDataAndType(Uri.parse(data), mimeString)
                        sendIntent.setPackage("com.whatsapp")
                        // Set the start time before starting the activity
                        startTime = SystemClock.elapsedRealtime()
                        startActivityForResult(sendIntent, REQUEST_WHATSAPP_CALL)
                        break
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_WHATSAPP_CALL) {
            val elapsedTime = SystemClock.elapsedRealtime() - startTime
            val seconds = elapsedTime / 1000
            val milliseconds = elapsedTime % 1000
            if (resultCode == RESULT_OK) {
                // Call was successfully made in WhatsApp
                // Log the call duration
                val callDuration = "$seconds.${milliseconds}s"
                println("WhatsApp Call Duration: $callDuration")
            } else if (resultCode == RESULT_CANCELED) {
                // Call was not made or was ended prematurely
                println("WhatsApp Call was not successful or ended prematurely.")
            }
            // Reset the start time
            startTime = 0
        }
    }

    companion object {
        private const val READ_CONTACTS_PERMISSION_CODE = 1
        private const val REQUEST_WHATSAPP_CALL = 1001
    }
}

