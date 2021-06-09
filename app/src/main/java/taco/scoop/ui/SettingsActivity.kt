package taco.scoop.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import taco.scoop.R
import taco.scoop.databinding.ActivitySettingsBinding
import taco.scoop.util.readLogsPermissionGranted
import taco.scoop.util.runReadLogsGrantShell

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.settingsToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.commit {
            replace<SettingsFragment>(R.id.settings_container)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)

            val rootStatusPref = findPreference<Preference>("prefKey_root_status")
            rootStatusPref?.setOnPreferenceClickListener {
                runReadLogsGrantShell()
                true
            }

            val permissionStatusPref = findPreference<Preference>("prefKey_permission_status")
            permissionStatusPref?.summary =
                if (requireContext().readLogsPermissionGranted()) {
                    getString(R.string.settings_permission_status_summary_true)
                } else {
                    getString(R.string.settings_permission_status_summary_false)
                }
        }
    }
}
