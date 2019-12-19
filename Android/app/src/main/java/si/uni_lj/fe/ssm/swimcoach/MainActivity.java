package si.uni_lj.fe.ssm.swimcoach;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.VibrationEffect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;
import android.os.Vibrator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //CONSTANTS
    private static final String TAG = "razhroscevanje";
    private static final Long INTERVAL_DURATION = (long) 5000; //ms
    private static final Float ACTIVITY_THRESHOLD = (float) 20;
    private static final Integer FILTER_LENGTH = 5;
    private static final Integer MIN_DESIRED_NUMBER_OF_KICKS = 3;
    private static final Integer MAX_DESIRED_NUMBER_OF_KICKS = 7;

    //VARIABLES
    private SensorManager sensorManager;
    Sensor accelerometer;
    TextView xValue, yValue, zValue, tvNumOfKicks;
    private ArrayList<Float> accX = new ArrayList<>();
    private ArrayList<Float> accY = new ArrayList<>();
    private ArrayList<Float> accZ = new ArrayList<>();
    private Long tsStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);
        tvNumOfKicks = (TextView) findViewById(R.id.tvNumOfKicks);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME); //game = 22ms on Galaxy S7

        //start the timer
        tsStartTime = System.currentTimeMillis();
        tvNumOfKicks.setText("Number of kicks: 0");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Long tsCurrent = System.currentTimeMillis();
        xValue.setText("X: " + event.values[0]);
        yValue.setText("Y: " + event.values[1]);
        zValue.setText("Z: " + event.values[2]);
//        Log.d(TAG, "Current timestamp = " + tsCurrent.toString());

        if (tsCurrent - tsStartTime > INTERVAL_DURATION){
            tsStartTime = tsCurrent;
            processData();
        }else {
            if (Float.isNaN(event.values[0]) || Float.isNaN(event.values[1]) || Float.isNaN(event.values[2])){
                Log.d(TAG, "Timestamp: " + tsCurrent + "X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
            }else {
                accX.add(event.values[0]);
                accY.add(event.values[1]);
                accZ.add(event.values[2]);
            }
        }

    }

    public void processData(){
        //moving average filter with length M for each axis
        ArrayList<Float> accXFiltered = filter(accX, FILTER_LENGTH);
        ArrayList<Float> accYFiltered = filter(accY, FILTER_LENGTH);
        ArrayList<Float> accZFiltered = filter(accZ, FILTER_LENGTH);
//        Log.d(TAG, "accXFiltered: " + accXFiltered);

        //calculating of absolute value for each timestamp
        ArrayList<Float> accAbs = accXFiltered;
        for (int i = 0; i < accXFiltered.size(); i++){
            accAbs.set(i, calculateAbsValue(accXFiltered.get(i), accYFiltered.get(i), accZFiltered.get(i)));
//            Log.d(TAG, "accAbs: " + accAbs.get(i));
        }

        //check if there is any activity (stationary is accAbs = 10)
        for (int i = 0; i < accAbs.size(); i++){
            if (accAbs.get(i) < ACTIVITY_THRESHOLD){
                accAbs.set(i, (float) 0); //if it is less than ACTIVITY_THRESHOLD, set it to 0
            }else {
                accAbs.set(i, accAbs.get(i) - ACTIVITY_THRESHOLD); //else subtract ACTIVITY_THRESHOLD
            }
        }

        //count all kicks
        Float upCrossThreshold = average(accAbs);
        Integer numOfUpCrossings = 0;
        for (int i = 0; i < accAbs.size() - 1; i++){
//            Log.d(TAG, "accAbs after: " + accAbs.get(i));
            if (accAbs.get(i) < upCrossThreshold && accAbs.get(i+1) >= upCrossThreshold){
                numOfUpCrossings++;
            }
        }
        tvNumOfKicks.setText("Number of kicks: " + numOfUpCrossings);
        Log.d(TAG, "Number of kicks:        " + numOfUpCrossings + "     !!!!!!!");

        if (numOfUpCrossings >= MIN_DESIRED_NUMBER_OF_KICKS && numOfUpCrossings < MAX_DESIRED_NUMBER_OF_KICKS){
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = { 0, 500, 500, 500, 500, 500, 500};
            v.vibrate(pattern , -1);
        }else if(numOfUpCrossings >= MAX_DESIRED_NUMBER_OF_KICKS){
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000); //Vibrate for 400 millisecondsI
        }

        //clear global arrays that contain data
        for (int i = 0; i < accX.size(); i++){
            accX.remove(i);
            accY.remove(i);
            accZ.remove(i);
        }
    }

    public ArrayList<Float> filter(ArrayList<Float> input, Integer M){
        int inputSize = input.size();
        ArrayList<Float> output = input;
        for (int i = 0; i < inputSize-M; i++){
            List<Float> sublist = input.subList(i, i+M);
            Float value = (float) 0;
            for (int j = 0; j < M; j++){
                value += sublist.get(j);
            }
            value = value/M;
            output.set(i, value);
        }

        ArrayList<Float> outputSublist = new ArrayList<Float>(output.subList(0, inputSize-M));
        return outputSublist;
    }

    public Float calculateAbsValue(Float a, Float b, Float c){
        Float out = (float) (Math.sqrt(Math.pow((double) a, 2) + Math.pow((double) b, 2) + Math.pow((double) c, 2)));
        return out;
    }

    public Float average(ArrayList<Float> input){
        float sum = 0;
        for (int i = 0; i < input.size(); i++){
            sum += input.get(i);
        }
        float output = sum / input.size();
        return output;
    }
}
