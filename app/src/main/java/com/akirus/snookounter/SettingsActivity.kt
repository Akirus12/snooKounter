package com.akirus.snookounter

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
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
        rulesButton = findViewById<Button>(R.id.rulesButton)
        aboutButton = findViewById<Button>(R.id.aboutButton)

        loadSharedPreferences()
        initSwitchListener()

        // Disables the back nav bar / gesture (because fullscreen wouldn't switch :( )
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {} })
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
        updateView()
    }

    // Changes the config file when any of the switches are pressed
    private fun initSwitchListener() {
        fullscreenSwitch.setOnCheckedChangeListener { _, checked ->
            UserSettings.fullScreenSwitch = checked
            saveBooleanPreference(UserSettings.FULL_SCREEN_KEY, UserSettings.fullScreenSwitch)
//            OLD VERSION
//            if (checked) UserSettings.fullScreenSwitch = true
//            else UserSettings.fullScreenSwitch = false
//            val editor = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit()
//            editor.putBoolean(UserSettings.FULL_SCREEN_KEY, UserSettings.fullScreenSwitch)
//            editor.apply()
//            updateView()
        }
        animationSwitch.setOnCheckedChangeListener { _, checked ->
            UserSettings.animationSwitch = checked
            saveBooleanPreference(UserSettings.ANIMATION_KEY, UserSettings.animationSwitch)
        }
        leadFlairSwitch.setOnCheckedChangeListener { _, checked ->
            UserSettings.leadFlairSwitch = checked
            saveBooleanPreference(UserSettings.LEAD_FLAIR_KEY, UserSettings.leadFlairSwitch)
        }
        updateView()
    }

    // Helper function to save the new bool value
    private fun saveBooleanPreference(key: String, value: Boolean) {
        getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit().putBoolean(key, value).apply()
    }

    // Updates the full screen switch based on UserSettings.java
    private fun updateView() {
        if (UserSettings.fullScreenSwitch == UserSettings.FULL_SCREEN_ON) fullscreenSwitch.isChecked = true
        else fullscreenSwitch.isChecked = false
        if (UserSettings.animationSwitch == UserSettings.ANIMATION_ON) animationSwitch.isChecked = true
        else animationSwitch.isChecked = false
        if (UserSettings.leadFlairSwitch == UserSettings.LEAD_FLAIR_ON) leadFlairSwitch.isChecked = true
        else leadFlairSwitch.isChecked = false
    }
}