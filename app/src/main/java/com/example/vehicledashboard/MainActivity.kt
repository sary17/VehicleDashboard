package com.example.vehicledashboard

import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.car.VehiclePropertyIds
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity() {

    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    private lateinit var tvSpeed: TextView
    private lateinit var tvGear: TextView
    private lateinit var tvFuel: TextView
    private lateinit var tvBrake: TextView

    private val permissions = arrayOf(Car.PERMISSION_SPEED, Car.PERMISSION_ENERGY)

    override fun onResume() {
        super.onResume()

        for (i in 0..1)
        {
            if (checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                //your code here
            } else {
                requestPermissions(permissions, 0)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSpeed = findViewById(R.id.tvSpeed)
        tvGear = findViewById(R.id.tvGear)
        tvFuel = findViewById(R.id.tvFuel)
        tvBrake = findViewById(R.id.tvBrake)

        connectToCar()
    }

    private fun connectToCar() {
        // Synchronous connection — fine for a simple dashboard app.
        car = Car.createCar(this)
        carPropertyManager =
            car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager

        registerListeners()

    }

    private fun registerListeners() {
        val cpm = carPropertyManager ?: return

        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                runOnUiThread { updateUi(value) }
            }

            override fun onErrorEvent(propId: Int, zone: Int) {}
        }

        cpm.registerCallback(
            callback,
            VehiclePropertyIds.PERF_VEHICLE_SPEED,
            CarPropertyManager.SENSOR_RATE_NORMAL
        )
        cpm.registerCallback(
            callback,
            VehiclePropertyIds.GEAR_SELECTION,
            CarPropertyManager.SENSOR_RATE_ONCHANGE
        )
        cpm.registerCallback(
            callback,
            VehiclePropertyIds.PARKING_BRAKE_ON,
            CarPropertyManager.SENSOR_RATE_ONCHANGE
        )

        cpm.registerCallback(
            callback,
            VehiclePropertyIds.FUEL_LEVEL_LOW,
            CarPropertyManager.SENSOR_RATE_ONCHANGE
        )


    }


    private fun updateUi(value: CarPropertyValue<*>) {
        when (value.propertyId) {
            VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                val speedMps = value.value as? Float ?: 0f
                val speedKph = speedMps * 3.6f
                tvSpeed.text = speedKph.toInt().toString()
            }

            VehiclePropertyIds.GEAR_SELECTION -> {
                tvGear.text = gearToLabel(value.value as? Int)
            }

            VehiclePropertyIds.FUEL_LEVEL_LOW -> {
                val level = value.value as Boolean
                if (level) {
                    tvFuel.setTextColor(Color.parseColor("#F0C807"))
                    tvFuel.text = "ON"
                }
                else{
                    tvFuel.setTextColor(Color.parseColor("#FFFFFF"))
                    tvFuel.text = "OFF"
                }
            }

            VehiclePropertyIds.PARKING_BRAKE_ON -> {
                val brake = value.value as Boolean
                if (brake) {
                    tvBrake.setTextColor(Color.parseColor("#FF0000"))
                    tvBrake.text = "ON"
                }
                else{
                    tvBrake.setTextColor(Color.parseColor("#FFFFFF"))
                    tvBrake.text = "OFF"
                }
            }
        }
    }

    private fun gearToLabel(gear: Int?): String = when (gear) {
        android.car.VehicleGear.GEAR_PARK -> "P (Park)"
        android.car.VehicleGear.GEAR_REVERSE -> "R (Reverse)"
        android.car.VehicleGear.GEAR_NEUTRAL -> "N (Neutral)"
        android.car.VehicleGear.GEAR_DRIVE -> "D (Drive)"
        else -> "--"
    }


    override fun onDestroy() {
        car?.disconnect()
        super.onDestroy()
    }

    private fun registerSpeedCallback() {

        val propertyManager = carPropertyManager ?: return

        val properties = propertyManager.getPropertyList()

        Log.d(
            "VehicleDashboard",
            "========== CAR PROPERTY LIST =========="
        )

        for (property in properties) {
            Log.d(
                "VehicleDashboard",
                "propertyId=${property.propertyId}"
            )
        }

        Log.d(
            "VehicleDashboard",
            "PERF_VEHICLE_SPEED ID = ${VehiclePropertyIds.PERF_VEHICLE_SPEED}"
        )

        val speedConfig =
            propertyManager.getCarPropertyConfig(
                VehiclePropertyIds.PERF_VEHICLE_SPEED
            )

        if (speedConfig == null) {

            Log.e(
                "VehicleDashboard",
                "PERF_VEHICLE_SPEED is NOT available to this application"
            )

            return
        }

        Log.d(
            "VehicleDashboard",
            "PERF_VEHICLE_SPEED IS AVAILABLE!"
        )
    }

}