package org.supla.android.features.appsettings
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

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.NotificationManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.permissions.PermissionsHelper
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.runtime.appsettings.ChannelHeight
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val preferences: Preferences,
  private val notificationManager: NotificationManager,
  private val permissionsHelper: PermissionsHelper,
  schedulers: SuplaSchedulers
) : BaseViewModel<SettingsViewState, SettingsViewEvent>(SettingsViewState(), schedulers) {

  fun loadSettings() {
    configObservable()
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(settingsItems = it) } }
      )
      .disposeBySelf()
  }

  private fun configObservable() = Observable.fromCallable {
    return@fromCallable listOf(
      SettingItem.HeaderItem(headerResource = R.string.menubar_appsettings),
      SettingItem.ChannelHeightItem(height = getChannelHeight(), this::updateChannelHeight),
      SettingItem.TemperatureUnitItem(unit = preferences.temperatureUnit, this::updateTemperatureUnit),
      SettingItem.ButtonAutoHide(active = preferences.isButtonAutohide, this::updateButtonAutoHide),
      SettingItem.InfoButton(visible = preferences.isShowChannelInfo, this::updateInfoButton),
      SettingItem.RollerShutterOpenClose(showOpeningPercentage = preferences.isShowOpeningPercent, this::updateShowingOpeningPercentage),
      SettingItem.LocalizationOrdering { sendEvent(SettingsViewEvent.NavigateToLocalizationsOrdering) },

      SettingItem.HeaderItem(headerResource = R.string.settings_permissions),
      SettingItem.NotificationsItem(allowed = areNotificationsEnabled(), this::goToSettings),
      SettingItem.LocalizationItem(allowed = isLocationPermissionGranted(), this::goToSettings)
    )
  }

  private fun getChannelHeight() = ChannelHeight.values()
    .firstOrNull { it.percent == preferences.channelHeight } ?: ChannelHeight.HEIGHT_100

  private fun areNotificationsEnabled() = if (VERSION.SDK_INT > VERSION_CODES.N) {
    notificationManager.areNotificationsEnabled()
  } else {
    true
  }

  private fun updateChannelHeight(position: Int) {
    preferences.channelHeight = ChannelHeight.forPosition(position).percent
  }

  private fun updateTemperatureUnit(position: Int) {
    preferences.temperatureUnit = TemperatureUnit.forPosition(position)
  }

  private fun updateButtonAutoHide(value: Boolean) {
    preferences.isButtonAutohide = value
  }

  private fun updateInfoButton(value: Boolean) {
    preferences.isShowChannelInfo = value
  }

  private fun updateShowingOpeningPercentage(value: Boolean) {
    preferences.isShowOpeningPercent = value
  }

  private fun goToSettings() {
    sendEvent(SettingsViewEvent.NavigateToSettings)
  }

  private fun isLocationPermissionGranted() =
    permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) ||
      permissionsHelper.checkPermissionGranted(ACCESS_COARSE_LOCATION)
}

sealed class SettingsViewEvent : ViewEvent {
  object NavigateToLocalizationsOrdering : SettingsViewEvent()
  object NavigateToSettings : SettingsViewEvent()
}

data class SettingsViewState(
  val settingsItems: List<SettingItem> = emptyList()
) : ViewState()
