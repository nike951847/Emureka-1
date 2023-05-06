package com.emureka.serialandbluetooth.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.emureka.serialandbluetooth.MyDataStore
import com.emureka.serialandbluetooth.R
import com.emureka.serialandbluetooth.mediapipe.PoseTracking
import kotlinx.coroutines.*

class Bird : Service() {
    private lateinit var bird: MediaPlayer
    private lateinit var serviceScope: CoroutineScope
    private var isSoundOn = false
    @Volatile private var emuState = 0
    private var isActivated = false;

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        bird = MediaPlayer.create(this, R.raw.bird)
        serviceScope = CoroutineScope(Dispatchers.IO)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val dataStore = MyDataStore.getInstance(this@Bird)

        // coroutine for collect isSoundOn
        serviceScope.launch {
            dataStore.settingsFlow.collect {
                this@Bird.isSoundOn = it.isSoundOn
            }
        }

        // coroutine for collect emuState
        serviceScope.launch {
            dataStore.settingsFlow.collect {
//                if (!isActivated) {
                Log.d("Bird", "${this@Bird.emuState} ${it.emuState}")
                    this@Bird.emuState = it.emuState
//                }
            }
        }

        // bird chirping
        serviceScope.launch {
            var cuml = 0;
            while (true) {
                PoseTracking.update_current_state(dataStore)
//                Log.d("Bird", "$isSoundOn $emuState")
                Log.d("Bird"," " + PoseTracking.currStat)
                if(isSoundOn && (PoseTracking.currStat != 0)) {
                    cuml += 1
                    if(cuml>=5) {
                        isActivated = true
                        bird.start()
                        delay(1000)
                        isActivated = false
                    }
                }
                else{
                    cuml = 0
                }
                delay(450)
            }
        }
        return START_STICKY
    }
}