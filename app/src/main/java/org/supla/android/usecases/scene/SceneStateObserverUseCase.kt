package org.supla.android.usecases.scene

import org.supla.android.events.ListsEventsManager
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneStateObserverUseCase @Inject constructor(
  private val listsEventsManager: ListsEventsManager,
  private val messageHandler: SuplaClientMessageHandler
) {

  private val listener: OnSuplaClientMessageListener = OnSuplaClientMessageListener { msg ->
    if (msg?.type == SuplaClientMsg.onSceneChanged) {
      listsEventsManager.emitSceneChange(msg.sceneId)
    }
  }

  fun register() {
    messageHandler.registerMessageListener(listener)
  }

  fun unregister() {
    messageHandler.unregisterMessageListener(listener)
  }
}
