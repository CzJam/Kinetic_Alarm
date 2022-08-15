package com.cz.alarm


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.AudioManager.FLAG_SHOW_UI
import android.media.AudioManager.STREAM_MUSIC
import android.media.SoundPool
import android.net.Uri
import android.os.*
import android.os.PowerManager.WakeLock
import android.provider.Settings
import android.text.style.BackgroundColorSpan
import android.widget.*
import androidx.core.view.isVisible
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.properties.Delegates


class MainActivity : Activity(), SensorEventListener {
    private var mTvShow: TextView? = null
    private var start: CheckBox? = null
    private var audalarm1: RadioButton? = null
    private var audalarm2: RadioButton? = null
    private var aud: RadioButton? = null
    private var txt: RadioButton? = null
    private var audg:RadioGroup?=null
    private var audalarmmax: CheckBox? = null
    private var audsound:TextView?=null



    private var accu: TextView? = null
    private var tres: TextView? = null
    private var starttime: TextView? = null
    private var alarmtime: TextView? = null
    private var imgarm: ImageView? = null




    private var mSensorManager: SensorManager? = null
    private var x:Double = 0.0
    private var y:Double = 0.0
    private var z:Double = 0.0
    private var total:Double=0.0
    private var totaldelay:Double=0.0
    private var delta:Double=0.0
    private var exceed:Boolean=false
    private var timeflashing:Boolean=true
    private var timeflashing2:Boolean=true

    private lateinit var sp: SoundPool
    private lateinit var hm: HashMap<Int, Int>
    private var currStaeamId by Delegates.notNull<Int>()
    private lateinit var wakeLock: PowerManager.WakeLock



    private fun initSoundPoollow() {
        sp = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        hm = HashMap<Int, Int>()
            hm[1] = sp.load(this, R.raw.low, 1)
    }
    private fun initSoundPoolhigh() {
        sp = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        hm = HashMap<Int, Int>()
        hm[1] = sp.load(this, R.raw.high, 1)
    }

    private fun playSound() {

        currStaeamId = sp.play(1, 1.0F, 1.0F, 1, -1, 1.0f) ;
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTvShow=findViewById(R.id.tv_show)
        start=findViewById(R.id.start)
        accu=findViewById(R.id.accu)
        tres=findViewById(R.id.tress)
        val p5:Button=findViewById(R.id.button)
        val m5:Button=findViewById(R.id.button2)
        val p1:Button=findViewById(R.id.button3)
        val m1:Button=findViewById(R.id.button4)
        val gw:Button=findViewById(R.id.buttongw)
        audalarm1= findViewById(R.id.audalarm1)
        audalarm2= findViewById(R.id.audalarm2)
        aud= findViewById(R.id.aud)
        txt= findViewById(R.id.txt)
        starttime=findViewById(R.id.starttime)
        alarmtime=findViewById(R.id.alarmtime)
        audsound=findViewById(R.id.audsound)
        audalarmmax=findViewById(R.id.audalarmmax)
        imgarm=findViewById(R.id.imagealarm)
        audg=findViewById(R.id.radioGroup2)

        val gy:Button=findViewById(R.id.buttonabout)
        val settings = getSharedPreferences("Alarm", 0)
        var tval=DecimalFormat("#.00").format(settings.getString("tval","0.3" ).toString().toDouble()).toDouble()
        tres!!.text=tval.toString()
        if (!settings.getBoolean("confirm",false )) {

            AlertDialog.Builder(this)
                .setTitle("使用说明")
                .setMessage(
                    "本工具通过加速度传感器监测手机运动状态并发出警报，可用于防盗，移动监听等多种应用场景" +
                            "\n\n支持xyz三轴监测，支持无线耳机监听与锁屏/后台监听，支持老旧机型(Android4.1及以上系统)\n\n" +
                            "由于不同机型的性能与传感器精度不同，因此在静止状态下加速度变化率Δa可能不等于0，可调节灵敏度后再使用\n\n" +
                            "请注意使用场合，作者不承担由本工具导致的一切后果"
                )
                .setNegativeButton("退出软件") { _, _ ->
                    finishAffinity()
                }
                .setNeutralButton("同意") { _, _ ->
                    bat()

                    settings.edit().putBoolean("confirm", true).apply()

                }
                .setCancelable(false)

                .create().show()
        }
        else{
            bat()
        }



        initSoundPoollow()

        audalarm1!!.setOnClickListener {
            initSoundPoollow()
        }
        audalarm2!!.setOnClickListener {
            initSoundPoolhigh()
        }
        audalarmmax!!.setOnClickListener {

            if (audalarmmax!!.isChecked) {
                audalarmmax!!.isChecked = false
                AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setIcon(R.drawable.ic_round_warning_224)
                    .setMessage("该选项会将系统媒体音量强制设定为最大值，在任意界面均不可调节。\n" +
                            "\n请勿佩戴耳机使用此功能，以免损伤听力！" +
                            "\n请在使用后手动恢复合适的媒体音量，以免损伤听力或影响他人！\n\n" +
                            "请注意使用场合，作者不承担由本功能导致的一切后果")
                    .setPositiveButton("同意使用") { _, _ -> audalarmmax!!.isChecked = true

                    }
                    .create().show()
            }
        }
        aud!!.isChecked=true
        starttime!!.isVisible=false
        alarmtime!!.isVisible=false
        audalarmmax!!.isVisible=true
        audsound!!.isVisible=true
        audg!!.isVisible=true
        imgarm!!.isVisible=false




        gw.setOnClickListener {
            val uri: Uri = Uri.parse("http://jamcz.com")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        gy.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("关于 V1.2")
                .setMessage("作者：\n酷安@晨钟酱 \nB站@晨钟酱Official\n\n更多玩机工具可进入晨钟软件官网")
                .setPositiveButton("进入官网"){_,_->
                    val uri: Uri = Uri.parse("http://jamcz.com")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                .setNegativeButton("加入软件群"){_,_->
                    val uri: Uri = Uri.parse("http://jamcz.com/joingroup.html")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                .setNeutralButton("关注作者"){_,_->
                    val uri: Uri = Uri.parse("https://space.bilibili.com/251013709")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }

                .create().show()

        }
        txt!!.setOnClickListener {
            starttime!!.isVisible=true
            alarmtime!!.isVisible=true
            audalarmmax!!.isVisible=false
            audsound!!.isVisible=false
            audg!!.isVisible=false
            imgarm!!.isVisible=false
        }

        aud!!.setOnClickListener {
            starttime!!.isVisible=false
            alarmtime!!.isVisible=false
            audalarmmax!!.isVisible=true
            audsound!!.isVisible=true
            audg!!.isVisible=true
            imgarm!!.isVisible=false

        }


        p5.setOnClickListener {
            tval+=0.05
            tres!!.text=kotlin.math.abs(DecimalFormat("#.00").format(tval).toDouble()).toString()
            settings.edit().putString("tval",tval.toString()).apply()
        }
        m5.setOnClickListener {
            if (tval>=0.05) {
                tval -= 0.05
                tres!!.text =
                    kotlin.math.abs(DecimalFormat("#.00").format(tval).toDouble()).toString()
                settings.edit().putString("tval",tval.toString()).apply()

            }
        }
        p1.setOnClickListener {
            tval+=0.01
            tres!!.text=kotlin.math.abs(DecimalFormat("#.00").format(tval).toDouble()).toString()
            settings.edit().putString("tval",tval.toString()).apply()

        }
        m1.setOnClickListener {
            if (tval>=0.01) {
                tval -= 0.01
                tres!!.text =
                    kotlin.math.abs(DecimalFormat("#.00").format(tval).toDouble()).toString()
                settings.edit().putString("tval",tval.toString()).apply()

            }

        }
//        val vibrator = getSystemService(Service.VIBRATOR_SERVICE)
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                acquire(3*1000L /*10 minutes*/)
            }
        }
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        val sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }


    override fun onDestroy() {
        wakeLock.release()
        super.onDestroy();
        }

    @SuppressLint("BatteryLife", "SimpleDateFormat", "SetTextI18n")
    override fun onSensorChanged(event: SensorEvent) {
//        val vibrator: Vibrator = getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        val values = event.values
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxvol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        val SDF=SimpleDateFormat("MM/dd   HH:mm:ss")
        val DT=Date(System.currentTimeMillis())

        x= kotlin.math.abs(StringBuffer().append((values[0])).toString().toDouble())
        y= kotlin.math.abs(StringBuffer().append((values[1])).toString().toDouble())
        z= kotlin.math.abs(StringBuffer().append((values[2])).toString().toDouble())


        total = ((x+y+z)/3)

        Handler().postDelayed({
            accu!!.text=total.toString()
            totaldelay=accu!!.text.toString().toDouble()
        },100)
        delta= kotlin.math.abs(DecimalFormat("#.00").format(total - totaldelay).toDouble())
        if(delta.toString()=="0.0"){
            mTvShow!!.text="Δa = 0.00"
        }
        else{
            mTvShow!!.text= "Δa = $delta"
        }
        if (delta > tres!!.text.toString().toDouble()) {
//            vibrator.vibrate(10000)
        }


        start!!.setOnClickListener {
            if(Build.VERSION.SDK_INT >= 23) {
                try {
                    val powerManager = getSystemService(POWER_SERVICE) as PowerManager?
                    val hasIgnored = powerManager!!.isIgnoringBatteryOptimizations(this.packageName)
                    if (!hasIgnored) {
                        AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setIcon(R.drawable.ic_round_warning_224)
                            .setMessage("请点击同意以忽略电池优化，以便在锁屏或后台继续监测")
                            .setPositiveButton("同意") { _, _ ->
                                val intent =
                                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            }
                            .create().show()
                    }
                } catch (e: java.lang.Exception) {
                }
            }
            if(start!!.isChecked){
                start!!.isChecked=false
                if(aud!!.isChecked) {
                    AlertDialog.Builder(this)
                        .setTitle("启动确认")
                        .setIcon(R.drawable.ic_round_warning_224)
                        .setMessage("请将手机放置平稳再点击启动监测。\n警报触发后可点击此开关或屏幕中心的三角关闭警报\n\n请注意使用场合，作者不承担由本工具导致的一切后果")
                        .setPositiveButton("启动监测") { _, _ ->
                            Toast.makeText(this, "5s后启动监测...", Toast.LENGTH_LONG).show()
                            Handler().postDelayed({
                            start!!.isChecked = true
                            },5000)
                        }
                        .create().show()
                }
                else{
                    Toast.makeText(this, "5s后启动监测...", Toast.LENGTH_LONG).show()
                    Handler().postDelayed({
                        start!!.isChecked = true
                    },5000)
                }
            }
        }

        if (start!!.isChecked){

            aud!!.isVisible=false
            txt!!.isVisible=false
            audalarmmax!!.isVisible=false
            audsound!!.isVisible=false
            audg!!.isVisible=false

            start!!.text="监测中..."
            if (txt!!.isChecked){
                audalarmmax!!.isChecked=false
                if (timeflashing) {
                    starttime!!.text = "启动时间：" + SDF.format(DT)
                    timeflashing=false
                }
            }
            else{
                if (audalarmmax!!.isChecked){
                    am.setStreamVolume(STREAM_MUSIC,maxvol, FLAG_SHOW_UI)
                }
            }

            if(!exceed) {
                if (delta > tres!!.text.toString().toDouble()) {
//                    sound
                    if (aud!!.isChecked) {
                        imgarm!!.isVisible = true

                        imgarm!!.setColorFilter(Color.rgb(255, 0, 0))
                        playSound()
                    }
                    if (txt!!.isChecked){
                        if (timeflashing2) {
                            alarmtime!!.setTextColor(Color.rgb(255 , 0, 0))
                            alarmtime!!.text = "监测移动：" + SDF.format(DT)
                            timeflashing2=false
                        }
                    }
                    exceed = true
                }
            }
            else{
                start!!.text="监测到移动"
            }
        }
        else{
            if (aud!!.isChecked){
                imgarm!!.isVisible=false
                aud!!.isVisible=true
                txt!!.isVisible=true
                audalarmmax!!.isVisible=true
                audsound!!.isVisible=true
                audg!!.isVisible=true
            }
            if (txt!!.isChecked){
                aud!!.isVisible=true
                txt!!.isVisible=true
                alarmtime!!.setTextColor(Color.rgb(116 , 116, 116))
                alarmtime!!.text = "未监测到运动"
                starttime!!.text = "启动时间：NA"
            }

            start!!.text="启动监测"
            timeflashing2=true
            timeflashing=true



            try{
                sp.stop(currStaeamId);
            } catch (e: Exception){
            }
            finally {
                exceed = false
            }
        }
        imgarm!!.setOnClickListener {
            start!!.isChecked=false
            imgarm!!.isVisible=false
            start!!.text="启动监测"
            try{
                sp.stop(currStaeamId);
            } catch (e: Exception){

            }
            finally {
                exceed = false
            }
        }

    }





    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    @SuppressLint("BatteryLife")
    fun bat() {

        if (Build.VERSION.SDK_INT >= 23) {
            try {
                val powerManager = getSystemService(POWER_SERVICE) as PowerManager?
                val hasIgnored = powerManager!!.isIgnoringBatteryOptimizations(this.packageName)
                if (!hasIgnored) {
                    AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setIcon(R.drawable.ic_round_warning_224)
                        .setMessage("请点击下一步的“允许”以便在锁屏或后台继续监测")
                        .setPositiveButton("下一步") { _, _ ->
                            val intent =
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        }
                        .create().show()
                }
            } catch (e: java.lang.Exception) {
            }
        }
    }

}


