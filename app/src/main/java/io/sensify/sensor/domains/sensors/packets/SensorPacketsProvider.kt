package io.sensify.sensor.domains.sensors.packets

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.util.SparseArray
import androidx.core.util.valueIterator
import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SensorPacketsProvider : SensorEventListener {

    companion object {
        private var sSensorPacketsProvider: SensorPacketsProvider? = null
        private val lock = Any()

        fun getInstance(): SensorPacketsProvider {
            synchronized(lock) {
                if (sSensorPacketsProvider == null) {
                    sSensorPacketsProvider = SensorPacketsProvider()
                }
                return sSensorPacketsProvider!!
            }
        }
    }

    var mDefaultScope = CoroutineScope(Job() + Dispatchers.Default)

    private var mSensorManager: SensorManager? = null
    private val mSensorConfigs = SparseArray<SensorPacketConfig>()
    private val mConfigLock = Any()

    // Dedicated background HandlerThread so sensor interrupts don't block the UI thread
    private var mSensorThread: HandlerThread? = null
    private var mSensorHandler: Handler? = null

    private fun ensureHandlerThread() {
        if (mSensorThread == null || !mSensorThread!!.isAlive) {
            mSensorThread = HandlerThread("SensifySensorThread", android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE).apply {
                start()
            }
            mSensorHandler = Handler(mSensorThread!!.looper)
        }
    }

    private val _mSensorPacketFlow = MutableSharedFlow<ModelSensorPacket>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val mSensorPacketFlow = _mSensorPacketFlow.asSharedFlow()

    fun setSensorManager(manager: SensorManager): SensorPacketsProvider {
        mSensorManager = manager
        return this
    }

    fun attachSensor(config: SensorPacketConfig): SensorPacketsProvider {
        synchronized(mConfigLock) {
            val prevConfig = mSensorConfigs.get(config.sensorType)
            var shouldRegister = true
            if (prevConfig != null) {
                if (prevConfig.sensorDelay != config.sensorDelay) {
                    unregisterSensor(prevConfig)
                } else {
                    shouldRegister = false
                }
            }
            if (shouldRegister) {
                mSensorConfigs.put(config.sensorType, config)
                registerSensor(config)
            }
        }
        return this
    }

    fun detachSensor(sensorType: Int): SensorPacketsProvider {
        synchronized(mConfigLock) {
            val sensorConfig = mSensorConfigs.get(sensorType)
            if (sensorConfig != null) {
                unregisterSensor(sensorConfig)
                mSensorConfigs.remove(sensorType)
            }
        }
        return this
    }

    private fun unregisterSensor(config: SensorPacketConfig) {
        if (mSensorManager == null) return
        val sensor = mSensorManager?.getDefaultSensor(config.sensorType)
        if (sensor != null) {
            try {
                mSensorManager?.unregisterListener(this, sensor)
            } catch (e: Exception) {
                Log.e("SensorPacketsProvider", "Error unregistering sensor ${config.sensorType}", e)
            }
        }
    }

    private fun registerSensor(config: SensorPacketConfig) {
        if (mSensorManager == null) return
        ensureHandlerThread()
        try {
            val sensor = mSensorManager?.getDefaultSensor(config.sensorType)
            if (sensor != null) {
                mSensorManager?.registerListener(
                    this,
                    sensor,
                    config.sensorDelay,
                    mSensorHandler
                )
            }
        } catch (e: Exception) {
            Log.e("SensorPacketsProvider", "Error registering sensor ${config.sensorType}", e)
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null) {
            val sensorType = p0.sensor.type
            val sensorConfig: SensorPacketConfig?
            synchronized(mConfigLock) {
                sensorConfig = mSensorConfigs.get(sensorType)
            }
            if (sensorConfig != null) {
                val sensorPacket = ModelSensorPacket(
                    p0,
                    p0.values.clone(),
                    sensorType,
                    sensorConfig.sensorDelay,
                    System.currentTimeMillis()
                )
                _mSensorPacketFlow.tryEmit(sensorPacket)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    fun clearAll() {
        synchronized(mConfigLock) {
            if (mSensorConfigs.size() > 0) {
                for (sensorConfig in mSensorConfigs.valueIterator()) {
                    unregisterSensor(sensorConfig)
                }
            }
            mSensorConfigs.clear()
        }
        mSensorThread?.quitSafely()
        mSensorThread = null
        mSensorHandler = null
        mSensorManager = null
    }


}


