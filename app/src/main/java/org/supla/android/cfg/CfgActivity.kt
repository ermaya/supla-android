package org.supla.android.cfg
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

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.NavigationActivity.INTENTSENDER
import org.supla.android.NavigationActivity.INTENTSENDER_MAIN
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.core.ui.BaseActivity
import org.supla.android.data.ValuesFormatter
import org.supla.android.databinding.ActivityCfgBinding
import org.supla.android.features.createaccount.CreateAccountFragment
import org.supla.android.navigator.CfgActivityNavigator
import org.supla.android.profile.ProfileManager
import org.supla.android.ui.AppBar
import javax.inject.Inject

@AndroidEntryPoint
class CfgActivity : BaseActivity() {

  companion object {
    const val ACTION_PROFILE = "org.supla.android.CfgActivity.PROFILE"
    const val ACTION_CONFIG = "org.supla.android.CfgActivity.CONFIG"
    const val ACTION_AUTH = "org.supla.android.CfgActivity.AUTH"
  }

  private val viewModel: CfgViewModel by viewModels()

  @Inject
  lateinit var profileManager: ProfileManager

  @Inject
  lateinit var valuesFormatter: ValuesFormatter

  @Inject
  lateinit var navigator: CfgActivityNavigator

  private lateinit var binding: ActivityCfgBinding
  private var shouldShowBack = false

  private val navToolbar: AppBar
    get() = binding.navToolbar

  override fun getLoadingIndicator() = binding.loadingIndicator

  override fun onCreate(sis: Bundle?) {
    super.onCreate(sis)

    SuplaApp.getApp().initTypefaceCollection(this)

    binding = DataBindingUtil.setContentView(this, R.layout.activity_cfg)
    binding.lifecycleOwner = this

    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ResourcesCompat.getColor(
      resources,
      R.color.splash_bg,
      null
    )

    setSupportActionBar(navToolbar)

    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
      as NavHostFragment
    val navController = navHostFragment.navController
    val navInflater = navController.navInflater
    val graph = navInflater.inflate(R.navigation.nav_graph)

    val action = intent.action
    val startLoc = when (action) {
      ACTION_CONFIG -> R.id.cfgMain
      ACTION_AUTH -> R.id.cfgAuth
      ACTION_PROFILE -> R.id.cfgProfiles
      else -> null
    }

    if (startLoc != null) {
      /* Reconfigure navigation graph to dynamic start location */
      val args: Bundle? = if (action == ACTION_AUTH) {
        CreateAccountFragment.bundle(null)
      } else {
        null
      }

      graph.setStartDestination(startLoc)
      navController.setGraph(graph, args)
    }

    val cfg = AppBarConfiguration(navController.graph)
    NavigationUI.setupWithNavController(
      navToolbar,
      navController,
      cfg
    )

    navController.addOnDestinationChangedListener { _, _, _ -> configureNavBar() }
  }

  override fun onResume() {
    super.onResume()
    val navController = findNavController(R.id.nav_host_fragment)
    val dest = navController.currentDestination
    if (dest != null) {
      // Temporary hack to match look and feel of the rest of the app
      // prior to moving everything into navigation graph.
      if (dest.id == R.id.cfgAuth) {
        supportActionBar?.setTitle(dest.label ?: "")
      } else {
        supportActionBar?.setSubtitle(dest.label ?: "")
      }
    }

    val sender = intent.getStringExtra(INTENTSENDER)
    if (sender != null && sender == INTENTSENDER_MAIN) {
      // show back button
      shouldShowBack = true
      binding.navToolbar.setNavigationIcon(R.drawable.navbar_back)
      binding.navToolbar.setNavigationOnClickListener {
        onBackPressed()
      }
    }
  }

  private fun configureNavBar() {
    if (shouldShowBack) {
      binding.navToolbar.setNavigationIcon(R.drawable.navbar_back)
    }
  }

  override fun onBackPressed() {
    if (navigator.back()) {
      return
    }

    if (Preferences(this).isAnyAccountRegistered) {
      val client = SuplaApp.getApp().suplaClient

      if (client != null && client.registered()) {
        navigator.navigateToMain()
      } else {
        navigator.navigateToStatus()
      }
    }
    finish()
  }
}
