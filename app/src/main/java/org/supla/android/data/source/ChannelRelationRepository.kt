package org.supla.android.data.source
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

import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.local.ChannelRelationDao
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRelationRepository @Inject constructor(private val channelRelationDao: ChannelRelationDao) {

  fun insertOrUpdate(channelRelation: ChannelRelationEntity) = channelRelationDao.insertOrUpdate(channelRelation)

  fun findChildren(profileId: Long, parentId: Int) = channelRelationDao.findChildren(profileId, parentId)

  fun markAsRemovable(profileId: Long) = channelRelationDao.markAsRemovable(profileId)

  fun cleanUnused() = channelRelationDao.cleanUnused()

  /**
   * @return List with channel remote ids, which have parents
   */
  fun findListOfParents(profileId: Long): Observable<List<Int>> =
    channelRelationDao.getForProfile(profileId)
      .map { entities ->
        return@map mutableListOf<Int>().apply { entities.map { it.parentId }.forEach { add(it) } }
      }
}
