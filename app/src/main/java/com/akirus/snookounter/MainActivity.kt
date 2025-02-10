package com.akirus.snookounter

import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private const val DEBUG_TAG = "Gestures"

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    var p1Score = 0 ; var p2Score = 0
    var p1ScoreLast = 0 ; var p2ScoreLast = 0
    var lastActionByP1 = true
    var swipesNum = 0
    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var p1ScoreText: TextView
    private lateinit var p2ScoreText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())

        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener.
        mDetector = GestureDetectorCompat(this, this)
        // Set the gesture detector as the double-tap
        // listener.
        mDetector.setOnDoubleTapListener(this)

        // Score label setup
        p1ScoreText = findViewById(R.id.p1ScoreLabel)
        p2ScoreText = findViewById(R.id.p2ScoreLabel)

        // Makes undo work again, checks underflow and overflow, and updates the label to equal score for P1
        fun updateP1(num : Int) {
            lastActionByP1 = true
            p1ScoreLast = p1Score
            p1Score += num
            swipesNum = 0

            if(p1Score < 0) {
                p1Score = 0
            }
            if(p1Score >= 147) {
                p1ScoreLast = p1Score
                p1Score = 147
                p1ScoreText.text = "147 !MAX BREAK! 147"
                return
            }

            p1ScoreText.text = p1Score.toString()
            return
        }

        // Makes undo work again, checks underflow and overflow, and updates the label to equal score for P2
        fun updateP2(num : Int){
            lastActionByP1 = false
            p2ScoreLast = p2Score
            p2Score += num
            swipesNum = 0

            if(p2Score < 0) {
                p2Score = 0
            }
            if(p2Score >= 147) {
                p2ScoreLast = p2Score
                p2Score = 147
                p2ScoreText.text = "147 !MAX BREAK! 147"
                return
            }

            p2ScoreText.text = p2Score.toString()
            return
        }

        // Reset button
        val reset = findViewById<Button>(R.id.reset)
        reset.setOnLongClickListener {
            p1Score = 0 ; p2Score = 0
            p1ScoreLast = 0 ; p2ScoreLast = 0

            p1ScoreText.text = p1Score.toString()
            p2ScoreText.text = p2Score.toString()
            true
        }

        // Player 1 balls
        val p1R = findViewById<Button>(R.id.p1R)
        p1R.setOnClickListener {
            updateP1(1)
        }
        p1R.setOnLongClickListener {
            updateP1(-1)
            true
        }

        val p1Y = findViewById<Button>(R.id.p1Y)
        p1Y.setOnClickListener {
            updateP1(2)
        }
        p1Y.setOnLongClickListener {
            updateP1(-2)
            true
        }

        val p1G = findViewById<Button>(R.id.p1G)
        p1G.setOnClickListener {
            updateP1(3)
        }
        p1G.setOnLongClickListener {
            updateP1(-3)
            true
        }

        val p1Br = findViewById<Button>(R.id.p1Br)
        p1Br.setOnClickListener {
            updateP1(4)
        }
        p1Br.setOnLongClickListener {
            updateP1(-4)
            true
        }

        val p1B = findViewById<Button>(R.id.p1B)
        p1B.setOnClickListener {
            updateP1(5)
        }
        p1B.setOnLongClickListener {
            updateP1(-5)
            true
        }

        val p1P = findViewById<Button>(R.id.p1P)
        p1P.setOnClickListener {
            updateP1(6)
        }
        p1P.setOnLongClickListener {
            updateP1(-6)
            true
        }

        val p1Bl = findViewById<Button>(R.id.p1Bl)
        p1Bl.setOnClickListener {
            updateP1(7)
        }
        p1Bl.setOnLongClickListener {
            updateP1(-7)
            true
        }

        val p1W = findViewById<Button>(R.id.p1W)
        p1W.setOnLongClickListener {
            updateP1(-4)
            true
        }

        // Player 2 balls
        val p2R = findViewById<Button>(R.id.p2R)
        p2R.setOnClickListener {
            updateP2(1)
        }
        p2R.setOnLongClickListener {
            updateP2(-1)
            true
        }

        val p2Y = findViewById<Button>(R.id.p2Y)
        p2Y.setOnClickListener {
            updateP2(2)
        }
        p2Y.setOnLongClickListener {
            updateP2(-2)
            true
        }

        val p2G = findViewById<Button>(R.id.p2G)
        p2G.setOnClickListener {
            updateP2(3)
        }
        p2G.setOnLongClickListener {
            updateP2(-3)
            true
        }

        val p2Br = findViewById<Button>(R.id.p2Br)
        p2Br.setOnClickListener {
            updateP2(4)
        }
        p2Br.setOnLongClickListener {
            updateP2(-4)
            true
        }

        val p2B = findViewById<Button>(R.id.p2B)
        p2B.setOnClickListener {
            updateP2(5)
        }
        p2B.setOnLongClickListener {
            updateP2(-5)
            true
        }

        val p2P = findViewById<Button>(R.id.p2P)
        p2P.setOnClickListener {
            updateP2(6)
        }
        p2P.setOnLongClickListener {
            updateP2(-6)
            true
        }

        val p2Bl = findViewById<Button>(R.id.p2Bl)
        p2Bl.setOnClickListener {
            updateP2(7)
        }
        p2Bl.setOnLongClickListener {
            updateP2(-7)
            true
        }

        val p2W = findViewById<Button>(R.id.p2W)
        p2W.setOnLongClickListener {
            updateP2(-4)
            true
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

            if (Math.abs(deltaY) > 1200) {
                if (deltaY > 0) {// Down swipe
                    swipesNum++
                } else {// Up swipe
                    swipesNum++
                }

                if(swipesNum == 1){// Undo
                    if(lastActionByP1) {
                        p1Score = p1ScoreLast
                        p1ScoreText.text = p1Score.toString()
                    }
                    else {
                        p2Score = p2ScoreLast
                        p2ScoreText.text = p2Score.toString()
                    }
                }

                if(swipesNum > 20) {// Reset
                    p1Score = 0; p2Score = 0; p1ScoreLast = 0; p2ScoreLast = 0
                    p1ScoreText.text = "0"
                    p2ScoreText.text = "0"
                    swipesNum = 0
                }
                return true
            }
//            if (Math.abs(deltaX) > Math.abs(deltaY)) {
//                if (Math.abs(deltaX) > 1500) {
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

    // Enables swipe gesture over buttons
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            onTouchEvent(event)  // Pass event to gesture detector
        }
        return super.dispatchTouchEvent(event)  // Let other views (like buttons) handle touch
    }

    // Enables fullscreen if focus lost
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}//         SWIPE ONCE FOR UNDO, MULTIPLE TIMES TO RESET