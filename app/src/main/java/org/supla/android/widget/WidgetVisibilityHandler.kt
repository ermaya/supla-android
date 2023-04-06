package org.supla.android.widget

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

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.extensions.getAllWidgetIds
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetVisibilityHandler @Inject constructor(
        @ApplicationContext private val context: Context,
        private val appWidgetManager: AppWidgetManager,
        private val widgetPreferences: WidgetPreferences
) {

    fun onProfileRemoved(profileId: Long) {
        appWidgetManager.getAllWidgetIds(context).forEach {
            val widgetConfig = widgetPreferences.getWidgetConfiguration(it) ?: return@forEach
            if (widgetConfig.profileId == profileId) {
                widgetPreferences.setWidgetConfiguration(it, widgetConfig.copy(profileId = INVALID_PROFILE_ID))
                updateWidget(it)
            }
        }
    }

    private fun updateWidget(widgetId: Int) {
        val intent = Intent().apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        }
        context.sendBroadcast(intent)
    }
}