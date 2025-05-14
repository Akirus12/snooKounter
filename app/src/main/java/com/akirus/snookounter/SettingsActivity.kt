package com.akirus.snookounter

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.akirus.snookounter.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySettingsBinding

    private lateinit var settings: UserSettings
    private lateinit var howToUseButton : Button
    private lateinit var fullscreenSwitch: Switch
    private lateinit var animationSwitch : Switch
    private lateinit var leadFlairSwitch : Switch
    private lateinit var dimScreenSwitch : Switch
    private lateinit var dimTimeoutButton : Button
    private lateinit var rulesButton : Button
    private lateinit var aboutButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enables the action bar and its back button
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_activity_settings)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        settings = application as UserSettings

        howToUseButton = findViewById<Button>(R.id.howToUseButton)
        fullscreenSwitch = findViewById<Switch>(R.id.fullscreenSwitch)
        animationSwitch = findViewById<Switch>(R.id.animationsSwitch)
        leadFlairSwitch = findViewById<Switch>(R.id.leadFlairSwitch)
        dimScreenSwitch = findViewById<Switch>(R.id.dimScreenSwitch)
        dimTimeoutButton = findViewById<Button>(R.id.dimTimeoutButton)
        updateDimTimeoutButton()
        rulesButton = findViewById<Button>(R.id.rulesButton)
        aboutButton = findViewById<Button>(R.id.aboutButton)

        loadSharedPreferences()
        initSwitchListener()

        // Disables the back nav bar / gesture (because fullscreen wouldn't switch :( )
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {} })

        dimTimeoutButton.setOnClickListener {
            val options = listOf("5 seconds", "15 seconds", "30 seconds", "60 seconds")
            val values = listOf(5_000L, 15_000L, 30_000L, 60_000L)

            val currentIndex = values.indexOf(UserSettings.dimTimeoutButton)

            AlertDialog.Builder(this)
                .setTitle("Select Dim Timeout")
                .setSingleChoiceItems(options.toTypedArray(), currentIndex) { dialog, which ->
                    val selectedValue = values[which]
                    UserSettings.dimTimeoutButton = selectedValue
                    saveLongPreference(UserSettings.DIM_TIMEOUT_KEY, selectedValue)
                    updateDimTimeoutButton()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Handles the back button on the action bar
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_activity_settings)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Loads the preferences from the UserSettings.java file
    private fun loadSharedPreferences() {
        val sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE)
        UserSettings.fullScreenSwitch = sharedPreferences.getBoolean(UserSettings.FULL_SCREEN_KEY, UserSettings.FULL_SCREEN_ON)
        UserSettings.animationSwitch = sharedPreferences.getBoolean(UserSettings.ANIMATION_KEY, UserSettings.ANIMATION_ON)
        UserSettings.leadFlairSwitch = sharedPreferences.getBoolean(UserSettings.LEAD_FLAIR_KEY, UserSettings.LEAD_FLAIR_ON)
        UserSettings.dimScreenSwitch = sharedPreferences.getBoolean(UserSettings.DIM_SCREEN_KEY, UserSettings.DIM_SCREEN_ON)
        UserSettings.dimTimeoutButton = sharedPreferences.getLong(UserSettings.DIM_TIMEOUT_KEY, UserSettings.DIM_TIMEOUT_DEFAULT)
        updateView()
    }

    // Changes the config file when any of the switches are pressed
    private fun initSwitchListener() {
        fullscreenSwitch.setOnCheckedChangeListener { _, checked ->
            UserSettings.fullScreenSwitch = checked
            saveBooleanPreference(UserSettings.FULL_SCREEN_KEY, UserSettings.fullScreenSwitch)
        }
        animationSwitch.setOnCheckedChangeListener { _, checked ->
            UserSettings.animationSwitch = checked
            saveBooleanPreference(UserSettings.ANIMATION_KEY, UserSettings.animationSwitch)
        }
        leadFlairSwitch.setOnCheckedChangeListener { _, checked ->
            UserSettings.leadFlairSwitch = checked
            saveBooleanPreference(UserSettings.LEAD_FLAIR_KEY, UserSettings.leadFlairSwitch)
        }
        dimScreenSwitch.setOnCheckedChangeListener { _, checked ->
            UserSettings.dimScreenSwitch = checked
            saveBooleanPreference(UserSettings.DIM_SCREEN_KEY, UserSettings.dimScreenSwitch)
            updateDimTimeoutButton()
        }
        updateView()
    }

    // Helper function to save the new bool value
    private fun saveBooleanPreference(key: String, value: Boolean) {
        getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit().putBoolean(key, value).apply()
    }

    private fun saveLongPreference(key: String, value: Long) {
        val prefs = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE)
        prefs.edit().putLong(key, value).apply()
    }

    private fun updateDimTimeoutButton(){
        dimTimeoutButton.setText("Timeout: ${UserSettings.dimTimeoutButton / 1000} seconds")
        if(UserSettings.dimScreenSwitch){
            dimTimeoutButton.isEnabled = true
            dimTimeoutButton.setTextColor(-0x1)// White
        }
        else{
            dimTimeoutButton.isEnabled = false
            dimTimeoutButton.setTextColor(-0xbbbbbc)// Grey
        }
    }

    // Updates the full screen switch based on UserSettings.java
    private fun updateView() {
        if (UserSettings.fullScreenSwitch == UserSettings.FULL_SCREEN_ON) fullscreenSwitch.isChecked = true
        else fullscreenSwitch.isChecked = false
        if (UserSettings.animationSwitch == UserSettings.ANIMATION_ON) animationSwitch.isChecked = true
        else animationSwitch.isChecked = false
        if (UserSettings.leadFlairSwitch == UserSettings.LEAD_FLAIR_ON) leadFlairSwitch.isChecked = true
        else leadFlairSwitch.isChecked = false
        if (UserSettings.dimScreenSwitch == UserSettings.DIM_SCREEN_ON) dimScreenSwitch.isChecked = true
        else dimScreenSwitch.isChecked = false
    }
}