/**
Copyright [2022] [Eugene John, Joseph Matthew Espinas, Ramon Carmelo Y. Calimbahin, Randy Lance O. Zebroff ]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.example.heartrate_monitoring_app_segment

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.telephony.SmsManager
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.heartrate_monitoring_app_segment.ble.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_ble_operations.charaView
import kotlinx.android.synthetic.main.activity_ble_operations.characteristics_recycler_view
import kotlinx.android.synthetic.main.activity_ble_operations.characteristics_title
import kotlinx.android.synthetic.main.activity_ble_operations.homeView
import kotlinx.android.synthetic.main.activity_ble_operations.log_scroll_view
import kotlinx.android.synthetic.main.activity_ble_operations.log_text_view
import kotlinx.android.synthetic.main.activity_home.homeMenu
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BleOperationsActivity : AppCompatActivity() {

    lateinit var flpc : FusedLocationProviderClient
    lateinit var locReq : LocationRequest

    private lateinit var device: BluetoothDevice
    private val dateFormatter = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US)
    private val characteristics by lazy {
        ConnectionManager.servicesOnDevice(device)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }
    private val characteristicProperties by lazy {
        characteristics.map { characteristic ->
            characteristic to mutableListOf<CharacteristicProperty>().apply {
                if (characteristic.isNotifiable()) add(CharacteristicProperty.Notifiable)
            }.toList()
        }.toMap()
    }
    private val characteristicAdapter: CharacteristicAdapter by lazy {
        CharacteristicAdapter(characteristics) { characteristic ->
            showCharacteristicOptions(characteristic)
        }
    }
    private var notifyingCharacteristics = mutableListOf<UUID>()

    override fun onCreate(savedInstanceState: Bundle?) {
        ConnectionManager.registerListener(connectionEventListener)
        super.onCreate(savedInstanceState)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")

        setContentView(R.layout.activity_ble_operations)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = "HMAS prototype v3.2"
        }
        setupRecyclerView()

        val next = findViewById<Button>(R.id.next)
        val homeMenuBtn = findViewById<ImageButton>(R.id.menuBtn)
        next.setOnClickListener{
            next.visibility = View.INVISIBLE
            homeView.visibility = View.VISIBLE
            characteristics_title.visibility = View.INVISIBLE
            charaView.visibility = View.INVISIBLE
            homeMenuBtn.setBackgroundColor(resources.getColor(R.color.white))
        }

        homeMenuBtn.setOnClickListener{
            if(homeMenu.visibility == View.VISIBLE) {
                homeMenu.visibility = View.INVISIBLE
                homeMenuBtn.setBackgroundColor(resources.getColor(R.color.white))
            } else if(homeMenu.visibility != View.VISIBLE) {
                homeMenu.visibility = View.VISIBLE
                homeMenuBtn.setBackgroundColor(resources.getColor(R.color.gray_menu))
            }
        }

        val infoBtn = findViewById<Button>(R.id.infoBtn)
        infoBtn.setOnClickListener{
            val infoPage = Intent(this, information::class.java)
            startActivity(infoPage)
        }

        val conBtn = findViewById<Button>(R.id.conBtn)
        conBtn.setOnClickListener {
            val conPage = Intent(this, contacts::class.java)
            startActivity(conPage)
        }

    }//End of onCreate

    private fun sendSMS() {

        val fullTxt = locationFinder()

        val fullMessage: SharedPreferences = this.getSharedPreferences("fullMessage", MODE_PRIVATE);
        val fullMsg = fullMessage.getString("fullMsg", "The person in this location is currently having a heart attack!")

        val userCons: SharedPreferences = this.getSharedPreferences("MyUserCons", MODE_PRIVATE)
        val conE = userCons.getString("conE", "")
        val con1 = userCons.getString("con1", "")
        val con2 = userCons.getString("con2", "")
        val con3 = userCons.getString("con3", "")
        val con4 = userCons.getString("con4", "")

        val sm = SmsManager.getDefault()
        val parts: ArrayList<String> = sm.divideMessage(fullMsg + fullTxt)

        if(!conE.equals("")){
            sm.sendMultipartTextMessage(conE, null, parts, null, null)
        }
        if(!con1.equals("")){
            sm.sendMultipartTextMessage(con1, null, parts, null, null)
        }
        if(!con2.equals("")){
            sm.sendMultipartTextMessage(con2, null, parts, null, null)
        }
        if(!con3.equals("")){
            sm.sendMultipartTextMessage(con3, null, parts, null, null)
        }
        if(!con4.equals("")){
            sm.sendMultipartTextMessage(con4, null, parts, null, null)
        }

    }

    private fun sendSMSFilter() {
        val sendState = getSharedPreferences("sendState", Context.MODE_PRIVATE)
        var SMSstate = sendState.getString("BopNation", "")

        if(SMSstate == "Nerve") {
            //Do nothing
        } else if(SMSstate == "") {
            sendSMS()
            val sendStateEditor = sendState.edit()
            sendStateEditor.putString("BopNation", "Nerve")
            sendStateEditor.commit()
        }


    }

    lateinit var locText : String
    lateinit var lat : String
    lateinit var lon : String
    lateinit var acc : String
    lateinit var alt : String

    @SuppressLint("MissingPermission")
    fun locationFinder(): String {

        flpc = LocationServices.getFusedLocationProviderClient(this)

        flpc.lastLocation.addOnCompleteListener { task ->
            val location : Location? = task.result
            if(location == null) {
                newLocation()
            }else {
                lat = location.latitude.toString()
                lon = location.longitude.toString()
                acc = location.longitude.toString()

                if(location.hasAltitude()) {
                    alt = location.altitude.toString()
                } else {
                    alt = null.toString()
                }

                if(alt == null) {
                    locText = " The coordinates are: latitude of "+ lat + ", longitude of " + lon + ", and an accuracy of " + acc + "."
                } else {
                    locText = " The coordinates are: latitude of "+ lat + ", longitude of " + lon + ", altitude of " + alt + ", and an accuracy of " + acc + "."
                }
            }
        }
        return locText
    }

    @SuppressLint("MissingPermission")
    private fun newLocation() {
        locReq = LocationRequest()
        locReq.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locReq.interval = 0
        locReq.fastestInterval = 0
        locReq.numUpdates = 2
        Looper.myLooper()?.let {
            flpc.requestLocationUpdates(
                locReq, locationCallback, it
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            var lastLocation = p0.lastLocation

            lat = lastLocation.latitude.toString()
            lon = lastLocation.longitude.toString()
            acc = lastLocation.longitude.toString()

            if(lastLocation.hasAltitude()) {
                alt = lastLocation.altitude.toString()
            } else {
                alt = null.toString()
            }
        }
    }

    override fun onDestroy() {
        ConnectionManager.unregisterListener(connectionEventListener)
        ConnectionManager.teardownConnection(device)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        characteristics_recycler_view.apply {
            adapter = characteristicAdapter
            layoutManager = LinearLayoutManager(
                this@BleOperationsActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = characteristics_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    private fun log(message: String) {
        val formattedMessage = String.format("%s: %s", dateFormatter.format(Date()), message)
        runOnUiThread {
            val currentLogText = if (log_text_view.text.isEmpty()) {
                "Beginning of log."
            } else {
                log_text_view.text
            }
            log_text_view.text = "$currentLogText\n$formattedMessage"
            log_scroll_view.post { log_scroll_view.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun showCharacteristicOptions(characteristic: BluetoothGattCharacteristic) {
        characteristicProperties[characteristic]?.let { properties ->
            selector("Select an action to perform", properties.map { it.action }) { _, i ->
                when (properties[i]) {
                    CharacteristicProperty.Notifiable -> {
                        if (notifyingCharacteristics.contains(characteristic.uuid)) {
                            log("Disabling notifications on ${characteristic.uuid}")
                            ConnectionManager.disableNotifications(device, characteristic)
                        } else {
                            log("Enabling notifications on ${characteristic.uuid}")
                            ConnectionManager.enableNotifications(device, characteristic)
                        }
                    }
                }
            }
        }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                runOnUiThread {
                    alert {
                        title = "Disconnected"
                        message = "Disconnected from device."
                        positiveButton("OK") { onBackPressed() }
                    }.show()
                }
            }

            onCharacteristicChanged = { _, characteristic ->
                log("Value changed on ${characteristic.uuid}: ${characteristic.value.toHexString()}")

                val dataTV : TextView = findViewById(R.id.heartRate) as TextView
                val hRData = Converter.dataToString(characteristic.value)
                dataTV.text = hRData

                val lowHeartRate = 57
                val highHeartRate = 103


                if(hRData != null && hRData == "0" || hRData.toInt() <= lowHeartRate || hRData.toInt() >= highHeartRate) {
                    sendSMSFilter()
                }
            }

            onNotificationsEnabled = { _, characteristic ->
                log("Enabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.add(characteristic.uuid)
            }

            onNotificationsDisabled = { _, characteristic ->
                log("Disabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.remove(characteristic.uuid)
            }
        }
    }

    private enum class CharacteristicProperty {
        Writable,
        WritableWithoutResponse,
        Notifiable;

        val action
            get() = when (this) {
                Writable -> "Write"
                WritableWithoutResponse -> "Write Without Response"
                Notifiable -> "Toggle Notifications"
            }
    }
}
