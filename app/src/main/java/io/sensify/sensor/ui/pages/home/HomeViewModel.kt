package io.sensify.sensor.ui.pages.home

import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.sensify.sensor.domains.chart.mpchart.MpChartDataManager
import io.sensify.sensor.domains.sensors.packets.SensorPacketConfig
import io.sensify.sensor.domains.sensors.packets.SensorPacketsProvider
import io.sensify.sensor.domains.sensors.provider.SensorsProvider
import io.sensify.sensor.ui.pages.home.model.ModelHomeSensor
import io.sensify.sensor.ui.pages.home.state.HomeUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SensorCategory(val displayName: String) {
    ALL("All"),
    MOTION("Motion"),
    POSITION("Position"),
    ENVIRONMENT("Environment")
}

class HomeViewModel : ViewModel() {

    private var mSensors: MutableList<ModelHomeSensor> = mutableListOf()

    private val _uiState = MutableStateFlow(HomeUiState())
    val mUiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(SensorCategory.ALL)
    val selectedCategory: StateFlow<SensorCategory> = _selectedCategory.asStateFlow()

    private val _mSensorsList = mutableStateListOf<ModelHomeSensor>()
    val mSensorsList: SnapshotStateList<ModelHomeSensor> = _mSensorsList

    // Live sensor values map for instant UI telemetry without recomposing whole list
    private val _liveSensorValues = mutableStateMapOf<Int, FloatArray>()
    val liveSensorValues: SnapshotStateMap<Int, FloatArray> = _liveSensorValues

    private val _mActiveSensorListFlow = MutableStateFlow<MutableList<ModelHomeSensor>>(
        mutableListOf()
    )
    val mActiveSensorListFlow: StateFlow<MutableList<ModelHomeSensor>> = _mActiveSensorListFlow
    private val _mActiveSensorList = mutableListOf<ModelHomeSensor>()

    private val mIsActiveMap = mutableMapOf<Int, Boolean>(
        Pair(Sensor.TYPE_ACCELEROMETER, true),
        Pair(Sensor.TYPE_GYROSCOPE, true),
        Pair(Sensor.TYPE_MAGNETIC_FIELD, true)
    )

    private val mChartDataManagerMap = mutableMapOf<Int, MpChartDataManager>()

    init {
        viewModelScope.launch {
            SensorsProvider.getInstance().mSensorsFlow.map { value ->
                value.map {
                    ModelHomeSensor(
                        it.type,
                        it.sensor,
                        it.info,
                        0f,
                        mIsActiveMap.getOrDefault(it.type, false)
                    )
                }.toMutableList()
            }.collectLatest {
                mSensors = it
                if (_mSensorsList.isEmpty()) {
                    _mSensorsList.addAll(mSensors)
                    val activeSensors = it.filter { modelHomeSensor -> modelHomeSensor.isActive }
                    _mActiveSensorList.addAll(activeSensors)
                    _mActiveSensorListFlow.emit(_mActiveSensorList.toMutableList())
                    getInitialChartData()
                    initializeFlow()
                }
            }
        }
        SensorsProvider.getInstance().listenSensors()
    }

    val filteredSensors: StateFlow<List<ModelHomeSensor>> = combine(
        snapshotFlow { _mSensorsList.toList() },
        _selectedCategory
    ) { sensors, category ->
        if (category == SensorCategory.ALL) sensors
        else sensors.filter { sensor ->
            when (category) {
                SensorCategory.MOTION -> sensor.type in listOf(
                    Sensor.TYPE_ACCELEROMETER,
                    Sensor.TYPE_GYROSCOPE,
                    Sensor.TYPE_GRAVITY,
                    Sensor.TYPE_LINEAR_ACCELERATION,
                    Sensor.TYPE_ROTATION_VECTOR,
                    Sensor.TYPE_GAME_ROTATION_VECTOR,
                    Sensor.TYPE_SIGNIFICANT_MOTION,
                    Sensor.TYPE_STEP_DETECTOR,
                    Sensor.TYPE_STEP_COUNTER
                )
                SensorCategory.POSITION -> sensor.type in listOf(
                    Sensor.TYPE_MAGNETIC_FIELD,
                    Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
                    Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
                    Sensor.TYPE_ORIENTATION,
                    Sensor.TYPE_PROXIMITY
                )
                SensorCategory.ENVIRONMENT -> sensor.type in listOf(
                    Sensor.TYPE_LIGHT,
                    Sensor.TYPE_PRESSURE,
                    Sensor.TYPE_TEMPERATURE,
                    Sensor.TYPE_AMBIENT_TEMPERATURE,
                    Sensor.TYPE_RELATIVE_HUMIDITY
                )
                SensorCategory.ALL -> true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategory(category: SensorCategory) {
        _selectedCategory.value = category
    }

    private fun getInitialChartData() {
        for (sensor in _mActiveSensorList) {
            getChartDataManager(sensor.type)
        }
    }

    private fun initializeFlow() {
        val sensorPacketFlow = SensorPacketsProvider.getInstance().mSensorPacketFlow

        for (sensor in _mActiveSensorList) {
            attachPacketListener(sensor)
        }

        viewModelScope.launch {
            sensorPacketFlow.collect { packet ->
                // Update live telemetry values for modern cards
                packet.values?.let { vals ->
                    _liveSensorValues[packet.type] = vals
                }
                // Forward to MPChart
                mChartDataManagerMap[packet.type]?.addEntry(packet)
            }
        }

        mChartDataManagerMap.forEach { (_, mpChartDataManager) ->
            viewModelScope.launch {
                mpChartDataManager.runPeriodically()
            }
        }
    }

    private fun attachPacketListener(sensor: ModelHomeSensor) {
        SensorPacketsProvider.getInstance().attachSensor(
            SensorPacketConfig(sensor.type, SensorManager.SENSOR_DELAY_UI)
        )
    }

    private fun detachPacketListener(sensor: ModelHomeSensor) {
        SensorPacketsProvider.getInstance().detachSensor(sensor.type)
    }

    fun onSensorChecked(type: Int, isChecked: Boolean) {
        val isCheckedPrev = mIsActiveMap.getOrDefault(type, false)

        if (isCheckedPrev != isChecked) {
            mIsActiveMap[type] = isChecked
        }

        val index = mSensors.indexOfFirst { it.type == type }
        if (index >= 0) {
            val sensor = mSensors[index]
            val updatedSensor = ModelHomeSensor(sensor.type, sensor.sensor, sensor.info, sensor.valueRms, isChecked)
            mSensors[index] = updatedSensor

            val listIndex = _mSensorsList.indexOfFirst { it.type == type }
            if (listIndex >= 0) {
                _mSensorsList[listIndex] = updatedSensor
            }
            updateActiveSensor(updatedSensor, isChecked)
        }
    }

    private fun updateActiveSensor(sensor: ModelHomeSensor, isChecked: Boolean = false) {
        val index = _mActiveSensorList.indexOfFirst { it.type == sensor.type }

        if (!isChecked && index >= 0) {
            val manager = mChartDataManagerMap.remove(sensor.type)
            manager?.destroy()
            detachPacketListener(sensor)
            _liveSensorValues.remove(sensor.type)

            _mActiveSensorList.removeAt(index)
            viewModelScope.launch {
                _mActiveSensorListFlow.emit(_mActiveSensorList.toMutableList())
                _uiState.emit(
                    _uiState.value.copy(
                        activeSensorCounts = _mActiveSensorList.size,
                        currentSensor = _mActiveSensorList.firstOrNull()
                    )
                )
            }
        } else if (isChecked && index < 0) {
            _mActiveSensorList.add(sensor)
            attachPacketListener(sensor)
            viewModelScope.launch {
                _mActiveSensorListFlow.emit(_mActiveSensorList.toMutableList())
                _uiState.emit(
                    _uiState.value.copy(
                        activeSensorCounts = _mActiveSensorList.size,
                        currentSensor = _mActiveSensorList.firstOrNull()
                    )
                )
            }
            getChartDataManager(type = sensor.type).runPeriodically()
        }
    }

    fun getChartDataManager(type: Int): MpChartDataManager {
        return mChartDataManagerMap.getOrPut(type) {
            MpChartDataManager(type, onDestroy = {})
        }
    }

    fun setActivePage(page: Int?) {
        viewModelScope.launch {
            if (page != null && _mActiveSensorList.isNotEmpty()) {
                val safeIndex = page.coerceIn(0, _mActiveSensorList.size - 1)
                val sensor = _mActiveSensorList[safeIndex]
                _uiState.emit(
                    _uiState.value.copy(
                        currentSensor = sensor,
                        activeSensorCounts = _mActiveSensorList.size
                    )
                )
            } else {
                _uiState.emit(
                    _uiState.value.copy(
                        currentSensor = null,
                        activeSensorCounts = _mActiveSensorList.size
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mChartDataManagerMap.forEach { (_, mpChartDataManager) -> mpChartDataManager.destroy() }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel() as T
        }
    }
}
