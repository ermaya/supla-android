package org.supla.android.ui.views
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

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.grey

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextField(
  value: String,
  modifier: Modifier = Modifier,
  onValueChange: (String) -> Unit = { },
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  readOnly: Boolean = false,
  isError: Boolean = false,
  singleLine: Boolean = false,
  trailingIcon: @Composable (() -> Unit)? = null
) =
  androidx.compose.material.TextField(
    value = value,
    onValueChange = onValueChange,
    readOnly = readOnly,
    keyboardOptions = keyboardOptions,
    trailingIcon = trailingIcon,
    isError = isError,
    singleLine = singleLine,
    colors = ExposedDropdownMenuDefaults.textFieldColors(
      backgroundColor = MaterialTheme.colors.surface,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
      errorCursorColor = MaterialTheme.colors.error,
      errorIndicatorColor = Color.Transparent,
      trailingIconColor = MaterialTheme.colors.grey,
      focusedTrailingIconColor = MaterialTheme.colors.grey
    ),
    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default)),
    modifier = modifier.border(
      width = 1.dp,
      color = if (isError) MaterialTheme.colors.error else colorResource(id = R.color.gray_light),
      shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default))
    ),
    textStyle = MaterialTheme.typography.body1
  )
