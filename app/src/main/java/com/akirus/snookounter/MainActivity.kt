package com.akirus.snookounter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import kotlin.math.abs

private const val DEBUG_TAG = "Gestures"

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private var p1Score = 0 ; private var p2Score = 0; private var p1ScoreLast = 0 ; private var p2ScoreLast = 0
    private var lastActionByP1 = true
    private var swipesNum = 0
    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var p1ScoreText: TextView; private lateinit var p2ScoreText: TextView; private lateinit var p1NameText: TextView; private lateinit var p2NameText: TextView
    // For black screen overlay
    private lateinit var blackOverlay: View
    private var inactivityTimeout = 15_000L // this is defined just to avoid an error, because it gets quickly overwritten with either default or saved value from UserSettings
    private val handler = Handler(Looper.getMainLooper())
    private val hideScreenRunnable = Runnable { showBlackOverlay(true) }
    private var blackOverlayAnimating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Score label, main activity findViewById setup (and toast)
        p1ScoreText = findViewById(R.id.p1ScoreLabel)
        p2ScoreText = findViewById(R.id.p2ScoreLabel)
        p1NameText = findViewById(R.id.p1NameLabel)
        p2NameText = findViewById(R.id.p2NameLabel)
        blackOverlay = findViewById(R.id.blackOverlay)
        val mainActivityView = findViewById<ConstraintLayout>(R.id.main)
        val toastManager = ToastManager(this)

        // Instantiate the gesture detector with the application context and an implementation of GestureDetector.OnGestureListener, and set the gesture detector as the double-tap listener.
        mDetector = GestureDetectorCompat(this, this)
        mDetector.setOnDoubleTapListener(this)

        // Setup for fullscreen
        ViewCompat.setOnApplyWindowInsetsListener(mainActivityView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load the saved full screen setting, and check if first time boot (defaults to on for both)
        val sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE)
        UserSettings.fullScreenSwitch = sharedPreferences.getBoolean(UserSettings.FULL_SCREEN_KEY, UserSettings.FULL_SCREEN_ON)
        UserSettings.isFirstBoot = sharedPreferences.getBoolean(UserSettings.IS_FIRST_BOOT_KEY, true)
        UserSettings.animationSwitch = sharedPreferences.getBoolean(UserSettings.ANIMATION_KEY, UserSettings.ANIMATION_ON)
        UserSettings.leadFlairSwitch = sharedPreferences.getBoolean(UserSettings.LEAD_FLAIR_KEY, UserSettings.LEAD_FLAIR_ON)
        UserSettings.dimScreenSwitch = sharedPreferences.getBoolean(UserSettings.DIM_SCREEN_KEY, UserSettings.DIM_SCREEN_ON)
        UserSettings.dimTimeoutButton = sharedPreferences.getLong(UserSettings.DIM_TIMEOUT_KEY, UserSettings.DIM_TIMEOUT_DEFAULT)

        if(UserSettings.fullScreenSwitch)
            WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
        else
            WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
        if(UserSettings.isFirstBoot){
            showAlertDialog("Welcome to snooKounter!",
                "Thank you for trying out my app! Make sure to check the \"How to Use\" section in the settings to learn about all of the features!")
            UserSettings.isFirstBoot = false
            getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit().putBoolean(UserSettings.IS_FIRST_BOOT_KEY, UserSettings.isFirstBoot).apply()
        }
        inactivityTimeout = UserSettings.dimTimeoutButton
        if(UserSettings.dimScreenSwitch)
            resetInactivityTimer()

        /*
        Makes undo work again, checks underflow and overflow, updates the label to equal score for P1,
        and (if enabled) animates the score popup and changes the color if P1 is leading
         */
        fun updateP1(num : Int) {
            if(p1Score != 147 && p2Score != 147){
                // Makes sure that undo doesn't get overridden by same number, e.g. from 0 to 0
                if(p1Score != 0 || num > 0){
                    lastActionByP1 = true
                    p1ScoreLast = p1Score
                }
                p1Score += num
                swipesNum = 0

                if(p1Score < 0)
                    p1Score = 0

                if(p1Score >= 147)
                    p1Score = 147

                p1ScoreText.text = p1Score.toString()

                // Animate the score popup
                if(UserSettings.animationSwitch){
                    val constraintSet = ConstraintSet()
                    val animatorSet = AnimatorSet()
                    val p1AnimTextView = TextView(this).apply {
                        id = ViewCompat.generateViewId() // Generate a unique ID
                        layoutParams = ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                            topMargin = 300
                        }
                        if(UserSettings.leadFlairSwitch && p1Score > p2Score)
                            setTextColor(Color.YELLOW)
                        else
                            setTextColor(Color.WHITE)
                        textSize = 148f
                        typeface = Typeface.create("sans-serif-black", Typeface.ITALIC)
                    }
                    mainActivityView.addView(p1AnimTextView)
                    p1AnimTextView.text = p1ScoreText.text

                    constraintSet.clone(mainActivityView)
                    constraintSet.connect(p1AnimTextView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                    constraintSet.connect(p1AnimTextView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                    constraintSet.connect(p1AnimTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                    constraintSet.applyTo(mainActivityView)

                    val scaleUpP1 = ObjectAnimator.ofFloat(p1AnimTextView, TextView.SCALE_X, 0f, 1f)
                    val rotateP1 = ObjectAnimator.ofFloat(p1AnimTextView, TextView.ROTATION, 270f, (320..360).random() / 1f)
                    val translationP1 = ObjectAnimator.ofFloat(p1AnimTextView, TextView.TRANSLATION_X, 600f, 100f)
                    val fadeOutP1 = ObjectAnimator.ofFloat(p1AnimTextView, TextView.ALPHA, 1f, 0f).setDuration(3000)

                    animatorSet.playTogether(scaleUpP1, rotateP1, translationP1, fadeOutP1)
                    animatorSet.start()
                }

                leadCheck()
                return
            }
            else
                toastManager.showToast("The maximum score has been reached!")
        }

        /*
        Makes undo work again, checks underflow and overflow, updates the label to equal score for P2,
        and (if enabled) animates the score popup and changes the color if P2 is leading
         */
        fun updateP2(num : Int){
            if(p2Score != 147 && p1Score != 147){
                // Makes sure that undo doesn't get overridden by same number, e.g. from 0 to 0
                if(p2Score != 0 || num > 0){
                    lastActionByP1 = false
                    p2ScoreLast = p2Score
                    p2Score += num
                }
                swipesNum = 0

                if(p2Score < 0)
                    p2Score = 0

                if(p2Score >= 147)
                    p2Score = 147

                p2ScoreText.text = p2Score.toString()

                // Animate the score popup
                if(UserSettings.animationSwitch){
                    val constraintSet = ConstraintSet()
                    val animatorSet = AnimatorSet()
                    val p2AnimTextView = TextView(this).apply {
                        id = ViewCompat.generateViewId() // Generate a unique ID
                        layoutParams = ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                            bottomMargin = 300
                        }
                        if(UserSettings.leadFlairSwitch && p2Score > p1Score)
                            setTextColor(Color.YELLOW)
                        else
                            setTextColor(Color.WHITE)
                        textSize = 148f
                        typeface = Typeface.create("sans-serif-black", Typeface.ITALIC)
                    }

                    mainActivityView.addView(p2AnimTextView)

                    constraintSet.clone(mainActivityView)
                    constraintSet.connect(p2AnimTextView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                    constraintSet.connect(p2AnimTextView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                    constraintSet.connect(p2AnimTextView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                    constraintSet.applyTo(mainActivityView)

                    val scaleUpP2 = ObjectAnimator.ofFloat(p2AnimTextView, TextView.SCALE_X, 0f, 1f)
                    val rotateP2 = ObjectAnimator.ofFloat(p2AnimTextView, TextView.ROTATION, 90f, (0..40).random() / 1f)
                    val translationP2 = ObjectAnimator.ofFloat(p2AnimTextView, TextView.TRANSLATION_X, -300f, 100f)
                    val fadeOutP2 = ObjectAnimator.ofFloat(p2AnimTextView, TextView.ALPHA, 1f, 0f).setDuration(3000)

                    p2AnimTextView.text = p2ScoreText.text
                    animatorSet.playTogether(scaleUpP2, rotateP2, translationP2, fadeOutP2)
                    animatorSet.start()
                }

                leadCheck()
                return
            }
            else
                toastManager.showToast("The maximum score has been reached!")
        }

        // Settings button
        val settings = findViewById<Button>(R.id.settings)
        settings.setOnClickListener {
            startActivity(Intent(this,SettingsActivity::class.java))
        }
        // Reset button
//        val reset = findViewById<Button>(R.id.reset)
//        reset.setOnLongClickListener {
//            p1Score = 0 ; p2Score = 0
//            p1ScoreLast = 0 ; p2ScoreLast = 0
//
//            p1ScoreText.text = p1Score.toString()
//            p2ScoreText.text = p2Score.toString()
//            true
//        }

        blackOverlay.setOnClickListener {
            if (!blackOverlayAnimating) {
                resetInactivityTimer()// Dismiss and restart timer
            }
        }

        // P1 name label
        p1NameText.setOnClickListener {
            handler.removeCallbacks(hideScreenRunnable)// Pauses timer for black screen
            val editText = EditText(this)
            editText.hint = "(max 5 characters)"
            editText.filters = arrayOf(InputFilter.LengthFilter(5))
            editText.setSingleLine(true)

            AlertDialog.Builder(this)
                .setTitle("P1 Name")
                .setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    if(UserSettings.dimScreenSwitch)
                        resetInactivityTimer()// Resumes timer for black screen
                    val name = editText.text.toString()
                    if (name.isNotBlank()) {
                        p1NameText.text = name
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // P2 name label
        p2NameText.setOnClickListener {
            handler.removeCallbacks(hideScreenRunnable)// Pauses timer for black screen
            val editText = EditText(this)
            editText.hint = "(max 5 characters)"
            editText.filters = arrayOf(InputFilter.LengthFilter(5))
            editText.setSingleLine(true)

            AlertDialog.Builder(this)
                .setTitle("P2 Name")
                .setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    if(UserSettings.dimScreenSwitch)
                        resetInactivityTimer()// Resumes timer for black screen
                    val name = editText.text.toString()
                    if (name.isNotBlank()) {
                        p2NameText.text = name
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Player 1 balls
        val p1R = findViewById<Button>(R.id.p1R)
        p1R.setOnClickListener {
            updateP1(1)
            if(UserSettings.animationSwitch) p1R.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p1R.setOnLongClickListener {
            updateP1(-2)
            if(UserSettings.animationSwitch) p1R.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p1Y = findViewById<Button>(R.id.p1Y)
        p1Y.setOnClickListener {
            updateP1(2)
            if(UserSettings.animationSwitch) p1Y.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p1Y.setOnLongClickListener {
            toastManager.showToast("The minimum points you can give for a penalty is 4!")
            if(UserSettings.animationSwitch) p1Y.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
            true
        }

        val p1G = findViewById<Button>(R.id.p1G)
        p1G.setOnClickListener {
            updateP1(3)
            if(UserSettings.animationSwitch) p1G.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p1G.setOnLongClickListener {
            toastManager.showToast("The minimum points you can give for a penalty is 4!")
            if(UserSettings.animationSwitch) p1G.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
            true
        }

        val p1Br = findViewById<Button>(R.id.p1Br)
        p1Br.setOnClickListener {
            updateP1(4)
            if(UserSettings.animationSwitch) p1Br.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p1Br.setOnLongClickListener {
            updateP2(4)
            if(UserSettings.animationSwitch) p1Br.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p1B = findViewById<Button>(R.id.p1B)
        p1B.setOnClickListener {
            updateP1(5)
            if(UserSettings.animationSwitch) p1B.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p1B.setOnLongClickListener {
            updateP2(5)
            if(UserSettings.animationSwitch) p1B.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p1P = findViewById<Button>(R.id.p1P)
        p1P.setOnClickListener {
            updateP1(6)
            if(UserSettings.animationSwitch) p1P.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p1P.setOnLongClickListener {
            updateP2(6)
            if(UserSettings.animationSwitch) p1P.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p1Bl = findViewById<Button>(R.id.p1Bl)
        p1Bl.setOnClickListener {
            updateP1(7)
            if(UserSettings.animationSwitch) p1Bl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p1Bl.setOnLongClickListener {
            updateP2(7)
            if(UserSettings.animationSwitch) p1Bl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p1W = findViewById<Button>(R.id.p1W)
        p1W.setOnClickListener {
            toastManager.showToast("You have to hold if you want to give penalty points!")
            if(UserSettings.animationSwitch) p1W.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
        }
        p1W.setOnLongClickListener {
            updateP2(4)
            if(UserSettings.animationSwitch) p1W.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        // Player 2 balls
        val p2R = findViewById<Button>(R.id.p2R)
        p2R.setOnClickListener {
            updateP2(1)
            if(UserSettings.animationSwitch) p2R.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p2R.setOnLongClickListener {
            updateP2(-2)
            if(UserSettings.animationSwitch) p2R.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p2Y = findViewById<Button>(R.id.p2Y)
        p2Y.setOnClickListener {
            updateP2(2)
            if(UserSettings.animationSwitch) p2Y.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p2Y.setOnLongClickListener {
            toastManager.showToast("The minimum points you can give for a penalty is 4!")
            if(UserSettings.animationSwitch) p2Y.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
            true
        }

        val p2G = findViewById<Button>(R.id.p2G)
        p2G.setOnClickListener {
            updateP2(3)
            if(UserSettings.animationSwitch) p2G.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p2G.setOnLongClickListener {
            toastManager.showToast("The minimum points you can give for a penalty is 4!")
            if(UserSettings.animationSwitch) p2G.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
            true
        }

        val p2Br = findViewById<Button>(R.id.p2Br)
        p2Br.setOnClickListener {
            updateP2(4)
            if(UserSettings.animationSwitch) p2Br.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p2Br.setOnLongClickListener {
            updateP1(4)
            if(UserSettings.animationSwitch) p2Br.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p2B = findViewById<Button>(R.id.p2B)
        p2B.setOnClickListener {
            updateP2(5)
            if(UserSettings.animationSwitch) p2B.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p2B.setOnLongClickListener {
            updateP1(5)
            if(UserSettings.animationSwitch) p2B.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p2P = findViewById<Button>(R.id.p2P)
        p2P.setOnClickListener {
            updateP2(6)
            if(UserSettings.animationSwitch) p2P.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p2P.setOnLongClickListener {
            updateP1(6)
            if(UserSettings.animationSwitch) p2P.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p2Bl = findViewById<Button>(R.id.p2Bl)
        p2Bl.setOnClickListener {
            updateP2(7)
            if(UserSettings.animationSwitch) p2Bl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up_down))
        }
        p2Bl.setOnLongClickListener {
            updateP1(7)
            if(UserSettings.animationSwitch) p2Bl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }

        val p2W = findViewById<Button>(R.id.p2W)
        p2W.setOnClickListener {
            toastManager.showToast("You have to hold if you want to give penalty points!")
            if(UserSettings.animationSwitch) p2W.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
        }
        p2W.setOnLongClickListener {
            updateP1(4)
            if(UserSettings.animationSwitch) p2W.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_up))
            true
        }
    }

    // Checks who's leading to update the score style
    private fun leadCheck(){
        if(UserSettings.leadFlairSwitch){
            // Makes the score text yellow for P1
            if(p1Score > p2Score){
                p1ScoreText.setTextColor(Color.YELLOW)
                p1ScoreText.typeface = Typeface.create("sans-serif-black", Typeface.ITALIC)
                p2ScoreText.setTextColor(Color.WHITE)
                p2ScoreText.typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
            }
            // Makes the score text yellow for P2
            else if(p2Score > p1Score){
                p2ScoreText.setTextColor(Color.YELLOW)
                p2ScoreText.typeface = Typeface.create("sans-serif-black", Typeface.ITALIC)
                p1ScoreText.setTextColor(Color.WHITE)
                p1ScoreText.typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
            }
            else{
                p1ScoreText.setTextColor(Color.WHITE)
                p1ScoreText.typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
                p2ScoreText.setTextColor(Color.WHITE)
                p2ScoreText.typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    // Used for single swipe detection
    override fun onFling(
        event1: MotionEvent?,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(DEBUG_TAG, "onFling: $event1 $event2")
        return true
    }

    // Used for consecutive swipe detection
    override fun onScroll(
        event1: MotionEvent?,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (event1 != null) {
//            val deltaX = event2.x - event1.x
            val deltaY = event2.y - event1.y

            if (abs(deltaY) > 1600 && (p1Score != 0 || p2Score != 0)) {
                if (deltaY > 0) {// Down swipe
                    swipesNum++
                } else {// Up swipe
                    swipesNum++
                }

                val undoAnimImageView = findViewById<ImageView>(R.id.undoAnimImageView)

                if(swipesNum == 1){// Undo
                    if(lastActionByP1) {
                        p1Score = p1ScoreLast
                        p1ScoreText.text = p1Score.toString()
                        leadCheck()
                    }
                    else {
                        p2Score = p2ScoreLast
                        p2ScoreText.text = p2Score.toString()
                        leadCheck()
                    }

                    if(UserSettings.animationSwitch){
                        undoAnimImageView.setImageResource(R.drawable.undo_icon)
                        val fadeIn = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.ALPHA, 0f, 1f)
                        val rotate = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.ROTATION, 0f,  (320..360).random() / 1f)
                        val scaleX = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.SCALE_X, 1f, 1.75f).setDuration(2000)
                        val scaleY = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.SCALE_Y, 1f, 1.75f).setDuration(2000)
                        val fadeOut = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.ALPHA, 1f, 0f).setDuration(2000)

                        undoAnimImageView.isVisible = true
                        val animatorSet = AnimatorSet()
                        animatorSet.playTogether(fadeIn, rotate, scaleX, scaleY, fadeOut)
                        animatorSet.start()
                    }
                }

                if(swipesNum > 20) {// Reset
                    p1Score = 0; p2Score = 0; p1ScoreLast = 0; p2ScoreLast = 0
                    p1ScoreText.text = "0"
                    p2ScoreText.text = "0"
                    swipesNum = 0

                    if(UserSettings.leadFlairSwitch){
                        p1ScoreText.setTextColor(Color.WHITE)
                        p2ScoreText.setTextColor(Color.WHITE)
                        p1ScoreText.typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
                        p2ScoreText.typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
                    }
                    if(UserSettings.animationSwitch){
                        undoAnimImageView.setImageResource(R.drawable.reset_icon)
                        val fadeIn = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.ALPHA, 0f, 1f)
                        val rotate = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.ROTATION, 0f,  720f).setDuration(3000)
                        val scaleX = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.SCALE_X, 1.75f, 1f).setDuration(3000)
                        val scaleY = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.SCALE_Y, 1.75f, 1f).setDuration(3000)
                        val fadeOut = ObjectAnimator.ofFloat(undoAnimImageView, ImageView.ALPHA, 1f, 0f).setDuration(3000)

                        undoAnimImageView.isVisible = true
                        val animatorSet = AnimatorSet()
                        animatorSet.playTogether(fadeIn, rotate, scaleX, scaleY, fadeOut)
                        animatorSet.start()
                    }

                }
                return true
            }
//            if (abs(deltaX) > abs(deltaY)) {
//                if (abs(deltaX) > 1500) {
//                    if (deltaX > 0) {// Right swipe
//                        swipesNum++
//                    } else {// Left swipe
//                        swipesNum++
//                    }
//                    return true
//                }
//            } else {
//
//            }
        }
        return true
    }

    override fun onDown(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDown: $event")
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onLongPress: $event")
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onShowPress: $event")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapUp: $event")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTap: $event")
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: $event")
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: $event")
        return true
    }

    // Enables swipe gesture over buttons and resets black screen timer
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if(UserSettings.dimScreenSwitch)
            resetInactivityTimer()
        if (event != null) {
            onTouchEvent(event)  // Pass event to gesture detector
        }
        return super.dispatchTouchEvent(event)  // Let other views (like buttons) handle touch
    }

    // Enables fullscreen if focus lost
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && UserSettings.fullScreenSwitch)
            WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
    }

    // Separate class for toasts to fix spam issue
    class ToastManager(private val context: Context) {
        private var currentToast: Toast? = null

        fun showToast(message: String) {
            currentToast?.cancel() // Cancel the previous toast if it exists
            currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            currentToast?.show()
        }
    }

    // Function for showing an alert dialog
    private fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("Got it") { dialog, which ->
            // Action when the "OK" button is clicked
            dialog.dismiss()
        }

//        builder.setNegativeButton("Cancel") { dialog, which ->
//            // Action when the "Cancel" button is clicked
//            dialog.dismiss()
//        }

        // Show the dialog
        builder.show()
    }

    // For black screen overlay
    private fun showBlackOverlay(show: Boolean) {
        if (show && blackOverlay.visibility != View.VISIBLE && !blackOverlayAnimating) {
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            blackOverlay.visibility = View.VISIBLE
            blackOverlay.startAnimation(fadeIn)

        } else if (!show && blackOverlay.visibility == View.VISIBLE && !blackOverlayAnimating) {
            val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            blackOverlayAnimating = true
            blackOverlay.startAnimation(fadeOut)

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    blackOverlay.visibility = View.GONE
                    blackOverlayAnimating = false
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
    }

    private fun resetInactivityTimer() {
        handler.removeCallbacks(hideScreenRunnable)
        showBlackOverlay(false)
        handler.postDelayed(hideScreenRunnable, inactivityTimeout)
    }
    override fun onUserInteraction() {
        super.onUserInteraction()
        if(UserSettings.dimScreenSwitch)
            resetInactivityTimer()
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(hideScreenRunnable)
    }
}