package io.sensify.sensor.domains.sensors.metadata

import android.hardware.Sensor
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.sqrt

data class SensorPhysicsMeta(
    val title: String,
    val whatItMeasures: String,
    val fusionDetails: String,
    val differenceFromOthers: String,
    val unit: String,
    val axisNames: List<String>,
    val typicalRange: String
)

object SensorMath {
    /**
     * Converts a 3 or 4-element rotation vector / quaternion to azimuth heading degrees [0, 360).
     */
    fun quaternionToAzimuthDegrees(values: FloatArray): Float {
        if (values.isEmpty()) return 0f
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        try {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthRad = orientation[0]
            val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            return ((azimuthDeg % 360f) + 360f) % 360f
        } catch (_: Exception) {
            return 0f
        }
    }

    /**
     * Estimates 2D planar magnetic compass heading degrees [0, 360) from X and Y flux in microteslas.
     */
    fun magneticToHeadingDegrees(values: FloatArray): Float {
        if (values.size < 2) return 0f
        val x = values[0]
        val y = values[1]
        val rad = atan2(-x.toDouble(), y.toDouble())
        val deg = Math.toDegrees(rad).toFloat()
        return ((deg % 360f) + 360f) % 360f
    }

    /**
     * Computes vector magnitude sqrt(x^2 + y^2 + z^2).
     */
    fun vectorMagnitude(values: FloatArray): Float {
        if (values.isEmpty()) return 0f
        var sum = 0f
        for (v in values) {
            sum += v * v
        }
        return sqrt(sum.toDouble()).toFloat()
    }
}

object SensorPhysicsInfo {
    private val META_MAP = mapOf(
        Sensor.TYPE_ACCELEROMETER to SensorPhysicsMeta(
            title = "Accelerometer",
            whatItMeasures = "Measures total acceleration applied to the device along 3 spatial axes, including gravity (~9.81 m/s²).",
            fusionDetails = "Raw physical MEMS transducer without software fusion.",
            differenceFromOthers = "Includes static gravity vector, unlike Linear Acceleration which subtracts gravity.",
            unit = "m/s²",
            axisNames = listOf("X (Lateral / Left-Right)", "Y (Longitudinal / Up-Down)", "Z (Vertical / Front-Back)"),
            typicalRange = "±19.6 m/s² (±2g)"
        ),
        Sensor.TYPE_GRAVITY to SensorPhysicsMeta(
            title = "Gravity",
            whatItMeasures = "Measures direction and magnitude of Earth's gravity vector pointing toward center of Earth.",
            fusionDetails = "Fused from Accelerometer using high-pass/low-pass filtering to isolate static gravity.",
            differenceFromOthers = "Isolates gravity only. Omits sudden user movements and hand shakes.",
            unit = "m/s²",
            axisNames = listOf("X Gravity", "Y Gravity", "Z Gravity"),
            typicalRange = "9.81 m/s² at rest"
        ),
        Sensor.TYPE_LINEAR_ACCELERATION to SensorPhysicsMeta(
            title = "Linear Acceleration",
            whatItMeasures = "Measures user-induced dynamic acceleration along 3 axes, with gravity removed.",
            fusionDetails = "Fused: Accelerometer minus Gravity vector.",
            differenceFromOthers = "Zero when phone is sitting still on table, regardless of tilt.",
            unit = "m/s²",
            axisNames = listOf("X Dynamic", "Y Dynamic", "Z Dynamic"),
            typicalRange = "0.0 m/s² at rest"
        ),
        Sensor.TYPE_GYROSCOPE to SensorPhysicsMeta(
            title = "Gyroscope",
            whatItMeasures = "Measures rate of rotation (angular velocity) around X, Y, and Z device axes.",
            fusionDetails = "Raw MEMS vibrating ring measuring Coriolis force.",
            differenceFromOthers = "Measures SPEED of turning (rad/s), NOT angles or compass heading.",
            unit = "rad/s",
            axisNames = listOf("X (Pitch rate)", "Y (Roll rate)", "Z (Yaw rate)"),
            typicalRange = "0.0 rad/s stationary"
        ),
        Sensor.TYPE_MAGNETIC_FIELD to SensorPhysicsMeta(
            title = "Magnetometer",
            whatItMeasures = "Measures ambient geomagnetic field strength in microteslas (µT) across 3 dimensions.",
            fusionDetails = "Physical Hall-effect / magneto-resistive sensor.",
            differenceFromOthers = "Measures raw magnetic flux, not degrees. Calibrated to Earth's geomagnetic field.",
            unit = "µT",
            axisNames = listOf("X Field", "Y Field", "Z Field"),
            typicalRange = "30 to 60 µT (Earth normal)"
        ),
        Sensor.TYPE_ROTATION_VECTOR to SensorPhysicsMeta(
            title = "Rotation Vector",
            whatItMeasures = "Measures device spatial orientation as a quaternion relative to Earth's East-North-Up coordinate system.",
            fusionDetails = "Tri-sensor fusion: Accelerometer + Gyroscope + Magnetometer.",
            differenceFromOthers = "Locked to Magnetic North. Yaw angle 0° corresponds to True/Magnetic North.",
            unit = "Unitless (quaternion)",
            axisNames = listOf("x*sin(θ/2)", "y*sin(θ/2)", "z*sin(θ/2)", "cos(θ/2)"),
            typicalRange = "Angles 0° to 360°"
        ),
        Sensor.TYPE_GAME_ROTATION_VECTOR to SensorPhysicsMeta(
            title = "Game Rotation Vector",
            whatItMeasures = "Measures device spatial orientation without using geomagnetic field.",
            fusionDetails = "Dual-sensor fusion: Accelerometer + Gyroscope (No Magnetometer).",
            differenceFromOthers = "Does NOT point to North. Unaffected by local magnetic interference/metals; perfect for VR/games.",
            unit = "Unitless (quaternion)",
            axisNames = listOf("x*sin(θ/2)", "y*sin(θ/2)", "z*sin(θ/2)", "cos(θ/2)"),
            typicalRange = "Arbitrary initial reference"
        ),
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR to SensorPhysicsMeta(
            title = "Geomagnetic Rotation (Compass)",
            whatItMeasures = "Measures orientation relative to Magnetic North without using gyroscope.",
            fusionDetails = "Low-power fusion: Accelerometer + Magnetometer.",
            differenceFromOthers = "Lower power than Rotation Vector; uses no Gyroscope.",
            unit = "Unitless (quaternion)",
            axisNames = listOf("x*sin(θ/2)", "y*sin(θ/2)", "z*sin(θ/2)"),
            typicalRange = "Heading 0° to 360°"
        ),
        Sensor.TYPE_LIGHT to SensorPhysicsMeta(
            title = "Ambient Light",
            whatItMeasures = "Measures illuminance of ambient light hitting the screen photodiode.",
            fusionDetails = "Hardware optical sensor.",
            differenceFromOthers = "Measures brightness in Lux (lx). 0 = Dark, 100-500 = Office, 10,000+ = Daylight.",
            unit = "lx",
            axisNames = listOf("Illuminance"),
            typicalRange = "0 to 100,000+ lx"
        ),
        Sensor.TYPE_PRESSURE to SensorPhysicsMeta(
            title = "Barometer (Pressure)",
            whatItMeasures = "Measures atmospheric air pressure in hectopascals (hPa / mbar).",
            fusionDetails = "MEMS piezoresistive pressure cell.",
            differenceFromOthers = "Can calculate relative altitude changes (1 hPa ≈ 8.5m elevation).",
            unit = "hPa",
            axisNames = listOf("Air Pressure"),
            typicalRange = "1013.25 hPa (Sea level standard)"
        ),
        Sensor.TYPE_PROXIMITY to SensorPhysicsMeta(
            title = "Proximity",
            whatItMeasures = "Measures distance of an object (such as face/hand) from the top speaker bezel.",
            fusionDetails = "Infrared LED emitter + photodiode receiver.",
            differenceFromOthers = "Most phones report binary 0 cm (Near) vs 5 cm (Far).",
            unit = "cm",
            axisNames = listOf("Distance"),
            typicalRange = "0 cm (Near) to 5 cm (Far)"
        )
    )

    fun getMeta(sensorType: Int): SensorPhysicsMeta {
        return META_MAP[sensorType] ?: SensorPhysicsMeta(
            title = "Sensor",
            whatItMeasures = "Measures physical environmental or kinematic telemetry.",
            fusionDetails = "Standard Android hardware sensor pipeline.",
            differenceFromOthers = "Real-time hardware sensor stream.",
            unit = "SI units",
            axisNames = listOf("Value"),
            typicalRange = "Varies by device"
        )
    }
}
