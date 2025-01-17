package org.supla.android.features.thermostatdetail.thermostatgeneral
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.ThermostatState
import org.supla.android.data.source.local.entity.ThermostatValue
import org.supla.android.data.source.local.temperature.TemperatureCorrection
import org.supla.android.data.source.remote.ChannelConfigResult
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacAlgorithm
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaHvacTemperatures
import org.supla.android.data.source.remote.hvac.SuplaHvacThermometerType
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import org.supla.android.db.Channel
import org.supla.android.db.ChannelValue
import org.supla.android.events.ConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.lib.SuplaConst
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ChannelChild
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.thermostat.CreateTemperaturesListUseCase

@RunWith(MockitoJUnitRunner::class)
class ThermostatGeneralViewModelTest :
  BaseViewModelTest<ThermostatGeneralViewState, ThermostatGeneralViewEvent, ThermostatGeneralViewModel>() {

  @Mock
  lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @Mock
  lateinit var createTemperaturesListUseCase: CreateTemperaturesListUseCase

  @Mock
  lateinit var valuesFormatter: ValuesFormatter

  @Mock
  lateinit var delayedThermostatActionSubject: DelayedThermostatActionSubject

  @Mock
  lateinit var configEventsManager: ConfigEventsManager

  @Mock
  lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  lateinit var loadingTimeoutManager: LoadingTimeoutManager

  @Mock
  lateinit var dateProvider: DateProvider

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: ThermostatGeneralViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun shouldLoadHeatThermostatInStandbyState() {
    // given
    val remoteId = 123
    mockHeatThermostat(remoteId, 23.4f)

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      ThermostatGeneralViewState(
        viewModelState = ThermostatGeneralViewModelState(
          remoteId = remoteId,
          function = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
          lastChangedHeat = true,
          configMinTemperature = 10f,
          configMaxTemperature = 40f,
          mode = SuplaHvacMode.HEAT,
          setpointHeatTemperature = 23.4f,
          setpointCoolTemperature = null
        ),
        currentTemperaturePercentage = 0.17666666f,
        configMinTemperatureString = "10,0",
        configMaxTemperatureString = "40,0",
        manualModeActive = true,
        loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false)
      )
    )
  }

  @Test
  fun shouldLoadCoolThermostatInCoolingState() {
    // given
    val remoteId = 123
    val channelWithChildren = mockChannelWithChildren(
      remoteId = remoteId,
      mode = SuplaHvacMode.COOL,
      setpointTemperatureCool = 20.8f,
      flags = listOf(SuplaThermostatFlags.SETPOINT_TEMP_MAX_SET, SuplaThermostatFlags.COOLING)
    )

    whenever(configEventsManager.observerConfig(remoteId)).thenReturn(
      Observable.just(
        ConfigEventsManager.ConfigEvent(
          ChannelConfigResult.RESULT_TRUE,
          mockSuplaChannelHvacConfig(remoteId, ThermostatSubfunction.COOL)
        )
      ),
      Observable.just(
        ConfigEventsManager.ConfigEvent(
          ChannelConfigResult.RESULT_TRUE,
          mockSuplaChannelWeeklyScheduleConfig(remoteId)
        )
      )
    )
    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(
      Maybe.just(channelWithChildren)
    )
    whenever(createTemperaturesListUseCase.invoke(channelWithChildren)).thenReturn(emptyList())
    whenever(valuesFormatter.getTemperatureString(10f)).thenReturn("10,0")
    whenever(valuesFormatter.getTemperatureString(40f)).thenReturn("40,0")

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      ThermostatGeneralViewState(
        viewModelState = ThermostatGeneralViewModelState(
          remoteId = remoteId,
          function = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
          lastChangedHeat = false,
          configMinTemperature = 10f,
          configMaxTemperature = 40f,
          mode = SuplaHvacMode.COOL,
          setpointHeatTemperature = null,
          setpointCoolTemperature = 20.8f
        ),
        showCoolingIndicator = true,
        currentTemperaturePercentage = 0.17666666f,
        configMinTemperatureString = "10,0",
        configMaxTemperatureString = "40,0",
        manualModeActive = true,
        loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false)
      )
    )
  }

  @Test
  fun `should change heat temperature on setpoint position change`() {
    // given
    val remoteId = 321
    mockHeatThermostat(remoteId, 22.4f)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)
    viewModel.setpointTemperatureChanged(0.5f, null)

    // then
    assertThat(events).isEmpty()
    val state = thermostatDefaultState(remoteId, 22.4f)
    val emittedState = state.viewModelState!!.copy(setpointHeatTemperature = 25f)
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change heat temperature on setpoint position change in weekly schedule`() {
    // given
    val remoteId = 321
    mockHeatThermostat(remoteId, 22.4f, weeklyScheduleActive = true)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)
    viewModel.setpointTemperatureChanged(0.5f, null)

    // then
    val state = thermostatDefaultState(remoteId, 22.4f, manualActive = false)
    val emittedState = state.viewModelState!!.copy(setpointHeatTemperature = 25f)
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState.copy(mode = SuplaHvacMode.NOT_SET))
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change cool temperature on setpoint position change`() {
    // given
    val remoteId = 321
    mockCoolThermostat(remoteId, setpointTemperature = 22.4f)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)
    viewModel.setpointTemperatureChanged(null, 0.5f)

    // then
    assertThat(events).isEmpty()
    val state = thermostatDefaultState(remoteId, setpointTemperatureCool = 22.4f, mode = SuplaHvacMode.COOL, currentlyCooling = true)
    val emittedState = state.viewModelState!!.copy(setpointCoolTemperature = 25f)
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change cool temperature on setpoint position change in weekly schedule`() {
    // given
    val remoteId = 321
    mockCoolThermostat(remoteId, 22.4f, weeklyScheduleActive = true)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)
    viewModel.setpointTemperatureChanged(null, 0.5f)

    // then
    val state = thermostatDefaultState(
      remoteId,
      setpointTemperatureCool = 22.4f,
      manualActive = false,
      mode = SuplaHvacMode.COOL,
      currentlyCooling = true
    )
    val emittedState = state.viewModelState!!.copy(setpointCoolTemperature = 25f)
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState.copy(mode = SuplaHvacMode.NOT_SET))
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change heat temperature by step`() {
    // given
    val remoteId = 321
    mockHeatThermostat(remoteId, 22.4f)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)
    viewModel.changeSetpointTemperature(TemperatureCorrection.UP)

    // then
    assertThat(events).isEmpty()
    val state = thermostatDefaultState(remoteId, 22.4f)
    val emittedState = state.viewModelState!!.copy(setpointHeatTemperature = 22.5f)
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should change cool temperature by step with weekly schedule`() {
    // given
    val remoteId = 321
    val state = thermostatDefaultState(
      remoteId,
      setpointTemperatureCool = 22.4f,
      manualActive = false,
      mode = SuplaHvacMode.COOL,
      currentlyCooling = true
    )
    mockCoolThermostat(remoteId, 22.4f, weeklyScheduleActive = true)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)
    viewModel.changeSetpointTemperature(TemperatureCorrection.DOWN)

    // then
    val emittedState = state.viewModelState!!.copy(setpointCoolTemperature = 22.3f)

    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        viewModelState = emittedState,
        lastInteractionTime = currentTimestamp
      )
    )

    verify(delayedThermostatActionSubject).emit(emittedState.copy(mode = SuplaHvacMode.NOT_SET))
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should turn off`() {
    // given
    val remoteId = 321
    mockHeatThermostat(remoteId, 22.4f)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    val state = thermostatDefaultState(remoteId, 22.4f)
    val emittedState = state.viewModelState!!.copy(
      mode = SuplaHvacMode.OFF,
      setpointHeatTemperature = null
    )
    whenever(delayedThermostatActionSubject.sendImmediately(emittedState)).thenReturn(Completable.complete())

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)
    viewModel.turnOnOffClicked()

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        loadingState = state.loadingState.changingLoading(true, dateProvider)
      )
    )

    verify(delayedThermostatActionSubject).sendImmediately(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should turn on`() {
    // given
    val remoteId = 321
    mockHeatThermostat(remoteId, 22.4f, mode = SuplaHvacMode.OFF)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    val state = thermostatDefaultState(remoteId, 22.4f, mode = SuplaHvacMode.OFF)
    val emittedState = state.viewModelState!!.copy(
      mode = SuplaHvacMode.CMD_TURN_ON,
      setpointHeatTemperature = null
    )
    whenever(delayedThermostatActionSubject.sendImmediately(emittedState)).thenReturn(Completable.complete())

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)
    viewModel.turnOnOffClicked()

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        loadingState = state.loadingState.changingLoading(true, dateProvider)
      )
    )

    verify(delayedThermostatActionSubject).sendImmediately(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  @Test
  fun `should turn off when is off but in weekly schedule`() {
    // given
    val remoteId = 321
    mockHeatThermostat(remoteId, 22.4f, mode = SuplaHvacMode.OFF, weeklyScheduleActive = true)
    val currentTimestamp = 123L
    whenever(dateProvider.currentTimestamp()).thenReturn(currentTimestamp)

    val state = thermostatDefaultState(remoteId, 22.4f, mode = SuplaHvacMode.OFF, manualActive = false)
    val emittedState = state.viewModelState!!.copy(
      mode = SuplaHvacMode.OFF,
      setpointHeatTemperature = null
    )
    whenever(delayedThermostatActionSubject.sendImmediately(emittedState)).thenReturn(Completable.complete())

    // when
    viewModel.observeData(remoteId)
    viewModel.triggerDataLoad(remoteId)
    viewModel.turnOnOffClicked()

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      state.copy(
        loadingState = state.loadingState.changingLoading(true, dateProvider)
      )
    )

    verify(delayedThermostatActionSubject).sendImmediately(emittedState)
    verifyNoMoreInteractions(delayedThermostatActionSubject)
  }

  private fun thermostatDefaultState(
    remoteId: Int,
    setpointTemperatureHeat: Float? = null,
    setpointTemperatureCool: Float? = null,
    manualActive: Boolean = true,
    mode: SuplaHvacMode = SuplaHvacMode.HEAT,
    currentlyCooling: Boolean = false
  ) =
    ThermostatGeneralViewState(
      viewModelState = ThermostatGeneralViewModelState(
        remoteId = remoteId,
        function = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
        lastChangedHeat = setpointTemperatureHeat != null,
        configMinTemperature = 10f,
        configMaxTemperature = 40f,
        mode = mode,
        setpointHeatTemperature = setpointTemperatureHeat,
        setpointCoolTemperature = setpointTemperatureCool
      ),
      isOff = mode == SuplaHvacMode.OFF,
      manualModeActive = if (mode == SuplaHvacMode.OFF) false else manualActive,
      programmedModeActive = manualActive.not(),
      currentTemperaturePercentage = 0.17666666f,
      configMinTemperatureString = "10,0",
      configMaxTemperatureString = "40,0",
      showCoolingIndicator = currentlyCooling,
      loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false)
    )

  private fun mockHeatThermostat(
    remoteId: Int,
    setpointTemperatureHeat: Float,
    weeklyScheduleActive: Boolean = false,
    mode: SuplaHvacMode = SuplaHvacMode.HEAT
  ) {
    val flags = mutableListOf(SuplaThermostatFlags.SETPOINT_TEMP_MIN_SET)
    if (weeklyScheduleActive) {
      flags.add(SuplaThermostatFlags.WEEKLY_SCHEDULE)
    }
    val channelWithChildren = mockChannelWithChildren(
      remoteId = remoteId,
      mode = mode,
      setpointTemperatureHeat = setpointTemperatureHeat,
      flags = flags
    )

    whenever(configEventsManager.observerConfig(remoteId)).thenReturn(
      Observable.just(
        ConfigEventsManager.ConfigEvent(
          ChannelConfigResult.RESULT_TRUE,
          mockSuplaChannelHvacConfig(remoteId)
        )
      ),
      Observable.just(
        ConfigEventsManager.ConfigEvent(
          ChannelConfigResult.RESULT_TRUE,
          mockSuplaChannelWeeklyScheduleConfig(remoteId)
        )
      )
    )
    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(
      Maybe.just(channelWithChildren)
    )
    whenever(createTemperaturesListUseCase.invoke(channelWithChildren)).thenReturn(emptyList())
    whenever(valuesFormatter.getTemperatureString(10f)).thenReturn("10,0")
    whenever(valuesFormatter.getTemperatureString(40f)).thenReturn("40,0")
  }

  private fun mockCoolThermostat(remoteId: Int, setpointTemperature: Float, weeklyScheduleActive: Boolean = false) {
    val flags = mutableListOf(SuplaThermostatFlags.SETPOINT_TEMP_MAX_SET, SuplaThermostatFlags.COOLING)
    if (weeklyScheduleActive) {
      flags.add(SuplaThermostatFlags.WEEKLY_SCHEDULE)
    }
    val channelWithChildren = mockChannelWithChildren(
      remoteId = remoteId,
      mode = SuplaHvacMode.COOL,
      setpointTemperatureCool = setpointTemperature,
      flags = flags
    )

    whenever(configEventsManager.observerConfig(remoteId)).thenReturn(
      Observable.just(
        ConfigEventsManager.ConfigEvent(
          ChannelConfigResult.RESULT_TRUE,
          mockSuplaChannelHvacConfig(remoteId, ThermostatSubfunction.COOL)
        )
      ),
      Observable.just(
        ConfigEventsManager.ConfigEvent(
          ChannelConfigResult.RESULT_TRUE,
          mockSuplaChannelWeeklyScheduleConfig(remoteId)
        )
      )
    )
    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(
      Maybe.just(channelWithChildren)
    )
    whenever(createTemperaturesListUseCase.invoke(channelWithChildren)).thenReturn(emptyList())
    whenever(valuesFormatter.getTemperatureString(10f)).thenReturn("10,0")
    whenever(valuesFormatter.getTemperatureString(40f)).thenReturn("40,0")
  }

  private fun mockChannelWithChildren(
    remoteId: Int,
    func: Int = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
    mode: SuplaHvacMode = SuplaHvacMode.HEAT,
    setpointTemperatureHeat: Float? = null,
    setpointTemperatureCool: Float? = null,
    flags: List<SuplaThermostatFlags> = listOf(SuplaThermostatFlags.SETPOINT_TEMP_MIN_SET)
  ): ChannelWithChildren {
    val thermostatValue = ThermostatValue(
      state = ThermostatState(1),
      mode = mode,
      setpointTemperatureHeat = setpointTemperatureHeat ?: 0f,
      setpointTemperatureCool = setpointTemperatureCool ?: 0f,
      flags = mutableListOf<SuplaThermostatFlags>().apply {
        addAll(flags)
        if (setpointTemperatureCool != null) {
          add(SuplaThermostatFlags.HEAT_OR_COOL)
        }
      }
    )

    val value: ChannelValue = mockk()
    every { value.asThermostatValue() } returns thermostatValue

    val channel: Channel = mockk()
    every { channel.remoteId } returns remoteId
    every { channel.func } returns func
    every { channel.value } returns value
    every { channel.onLine } returns true

    val children = listOf(
      ChannelChild(channel = mockk(), relationType = ChannelRelationType.MAIN_THERMOMETER),
      ChannelChild(channel = mockk(), relationType = ChannelRelationType.AUX_THERMOMETER_FLOOR)
    )

    val termometerValue: ChannelValue = mockk()
    every { termometerValue.getTemp(SuplaConst.SUPLA_CHANNELFNC_THERMOMETER) } returns 15.3
    every { children[0].channel.func } returns SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    every { children[0].channel.value } returns termometerValue

    return ChannelWithChildren(channel, children)
  }

  private fun mockSuplaChannelHvacConfig(
    remoteId: Int,
    subfunction: ThermostatSubfunction = ThermostatSubfunction.HEAT
  ): SuplaChannelHvacConfig =
    SuplaChannelHvacConfig(
      remoteId = remoteId,
      func = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
      mainThermometerRemoteId = 234,
      auxThermometerRemoteId = 345,
      auxThermometerType = SuplaHvacThermometerType.FLOOR,
      antiFreezeAndOverheatProtectionEnabled = false,
      availableAlgorithms = listOf(SuplaHvacAlgorithm.ON_OFF_SETPOINT_AT_MOST),
      usedAlgorithm = SuplaHvacAlgorithm.ON_OFF_SETPOINT_AT_MOST,
      minOnTimeSec = 10,
      minOffTimeSec = 20,
      outputValueOnError = 0,
      subfunction = subfunction,
      temperatureSetpointChangeSwitchesToManualMode = false,
      temperatures = SuplaHvacTemperatures(
        freezeProtection = null,
        eco = null,
        comfort = null,
        boost = null,
        heatProtection = null,
        histeresis = null,
        belowAlarm = null,
        aboveAlarm = null,
        auxMinSetpoint = null,
        auxMaxSetpoint = null,
        roomMin = 1000,
        roomMax = 4000,
        auxMin = null,
        auxMax = null,
        histeresisMin = null,
        histeresisMax = null,
        autoOffsetMin = null,
        autoOffsetMax = null
      )
    )

  private fun mockSuplaChannelWeeklyScheduleConfig(remoteId: Int): SuplaChannelWeeklyScheduleConfig =
    SuplaChannelWeeklyScheduleConfig(
      remoteId = remoteId,
      func = SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
      programConfigurations = listOf(),
      schedule = listOf()
    )
}
