package org.supla.android.features.standarddetail
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

import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.db.ChannelBase
import org.supla.android.events.ListsEventsManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase

abstract class StandardDetailViewModel<S : StandardDetailViewState, E : StandardDetailViewEvent>(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val listsEventsManager: ListsEventsManager,
  defaultState: S,
  schedulers: SuplaSchedulers
) : BaseViewModel<S, E>(defaultState, schedulers) {

  fun observeUpdates(remoteId: Int, itemType: ItemType, initialFunction: Int) {
    getEventsSource(itemType)
      .flatMapMaybe { getDataSource(remoteId, itemType) }
      .attachSilent()
      .subscribeBy(onNext = { handleChannelBase(it, initialFunction) })
      .disposeBySelf()
  }

  fun loadData(remoteId: Int, itemType: ItemType, initialFunction: Int) {
    getDataSource(remoteId, itemType)
      .attach()
      .subscribeBy(onSuccess = { handleChannelBase(it, initialFunction) })
      .disposeBySelf()
  }

  protected abstract fun closeEvent(): E

  protected abstract fun updatedState(state: S, channelBase: ChannelBase): S

  private fun handleChannelBase(channelBase: ChannelBase, initialFunction: Int) {
    if (channelBase.visible > 0 && channelBase.func == initialFunction) {
      updateState { updatedState(it, channelBase) }
    } else {
      sendEvent(closeEvent())
    }
  }

  private fun getDataSource(remoteId: Int, itemType: ItemType) = when (itemType) {
    ItemType.CHANNEL -> readChannelByRemoteIdUseCase(remoteId)
    ItemType.GROUP -> readChannelGroupByRemoteIdUseCase(remoteId)
  }

  private fun getEventsSource(itemType: ItemType) = when (itemType) {
    ItemType.CHANNEL -> listsEventsManager.observeChannelUpdates()
    ItemType.GROUP -> listsEventsManager.observeGroupUpdates()
  }
}

interface StandardDetailViewEvent : ViewEvent

open class StandardDetailViewState() : ViewState()
