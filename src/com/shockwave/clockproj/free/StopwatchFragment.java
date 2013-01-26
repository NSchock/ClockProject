package com.shockwave.clockproj.free;

import android.content.*;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;

public class StopwatchFragment extends SherlockFragment implements View.OnClickListener {
    Intent stopwatchIntent, swLoopIntent;

    Button btnStart, btnStop, btnReset, btnLoop;
    TextView txtStopwatch, txtStopwatchMillis, txtStopwatchLoopMain, txtStopwatchLoopMillis;

    //Listview Vars
    ListView lvStopwatch;
    ArrayList<HashMap<String, String>> stopwatchTimes = new ArrayList<HashMap<String, String>>();
    private static ArrayList<HashMap<String, String>> saveTimes;

    ArrayList<Long> longRawTimes = new ArrayList<Long>();
    private static ArrayList<Long> staticRawTimes = new ArrayList<Long>();

    long customMillis;
    boolean valueEntered = false, stopwatchRunning = false, loopRunning = false;

    private long elapsedTime = 0;

    private SharedPreferences prefs;
    private SimpleAdapter simpleAdapter;


    private void updateStopwatch(long time) {
        int seconds = (int) time / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        long millis = time % 1000;
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        txtStopwatch.setText(String.format("%d : %02d : %02d", hours, minutes, seconds));
        txtStopwatchMillis.setText(String.format(". %03d", millis));
    }

    private void updateLoop(long time) {
        int seconds = (int) time / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        long millis = time % 1000;
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        txtStopwatchLoopMain.setText(String.format("%d : %02d : %02d", hours, minutes, seconds));
        txtStopwatchLoopMillis.setText(String.format(". %03d", millis));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        restoreMain(savedInstanceState);
        simpleAdapter = new SimpleAdapter(getSherlockActivity().getApplicationContext(), stopwatchTimes,
                R.layout.two_line_listview, new String[]{"line1", "line2"}, new int[]{R.id.line_main,
                R.id.line_millis});
        setupReceivers();
        super.onCreate(savedInstanceState);
    }

    private void restoreMain(Bundle savedInstanceState) {
        //Restore data for main stopwatch
        prefs = getSherlockActivity().getSharedPreferences("StopwatchFragmentPrefs", 0);
        prefs.getBoolean("stopwatchViewSaves", true);
        if (savedInstanceState != null) {
            stopwatchRunning = savedInstanceState.getBoolean("stopwatchRunning", false);
            loopRunning = savedInstanceState.getBoolean("loopRunning", false);
        } else {
            stopwatchRunning = prefs.getBoolean("stopwatchRunning", false);
            loopRunning = prefs.getBoolean("loopRunning", false);
        }
        if (saveTimes != null) {
            stopwatchTimes = saveTimes;
        }
        if (staticRawTimes != null) {
            longRawTimes = staticRawTimes;
        }
    }

    private void setupReceivers() {
        //Set up intents and receiver for main stopwatch service
        IntentFilter filter = new IntentFilter(StopwatchService.START_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        StopwatchReceiver receiver = new StopwatchReceiver();
        getSherlockActivity().getApplicationContext().registerReceiver(receiver, filter);
        stopwatchIntent = new Intent(getSherlockActivity().getApplicationContext(), StopwatchService.class);

        //Set up intents and receiver for loop service
        IntentFilter loopFilter = new IntentFilter(StopwatchLoopService.LOOP_START_ACTION);
        loopFilter.addCategory(Intent.CATEGORY_DEFAULT);
        StopwatchLoopReceiver loopReceiver = new StopwatchLoopReceiver();
        getSherlockActivity().getApplicationContext().registerReceiver(loopReceiver, loopFilter);
        swLoopIntent = new Intent(getSherlockActivity().getApplicationContext(), StopwatchLoopService.class);
    }

    private void initVars(View view) {
        btnStart = (Button) view.findViewById(R.id.bStart);
        btnStop = (Button) view.findViewById(R.id.bStop);
        btnReset = (Button) view.findViewById(R.id.bReset);
        btnLoop = (Button) view.findViewById(R.id.bLoop);
        btnStart.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnLoop.setOnClickListener(this);

        txtStopwatch = (TextView) view.findViewById(R.id.tvStopwatch);
        txtStopwatchMillis = (TextView) view.findViewById(R.id.tvStopwatchMillis);
        txtStopwatch.setText(prefs.getString("stopwatchMain", "0 : 00 : 00"));
        txtStopwatchMillis.setText(prefs.getString("stopwatchMillis", ". 000"));

        txtStopwatchLoopMain = (TextView) view.findViewById(R.id.tvLapMain);
        txtStopwatchLoopMillis = (TextView) view.findViewById(R.id.tvLapMillis);
        txtStopwatchLoopMain.setText(prefs.getString("stopwatchLoopMain", "0 : 00 : 00"));
        txtStopwatchLoopMillis.setText(prefs.getString("stopwatchLoopMillis", ". 000"));

        lvStopwatch = (ListView) view.findViewById(R.id.list_stopwatch);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stopwatch_fragment, null);

        initVars(view);
        lvStopwatch.setAdapter(simpleAdapter);
        registerForContextMenu(lvStopwatch);

        if (stopwatchRunning)
            showStopButton();

        //Set up swipe to dismiss for listview
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(lvStopwatch,
                new SwipeDismissListViewTouchListener.OnDismissCallback() {
                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            stopwatchTimes.remove(position);
                        }
                        simpleAdapter.notifyDataSetChanged();
                    }
                });
        lvStopwatch.setOnTouchListener(touchListener);
        lvStopwatch.setOnScrollListener(touchListener.makeScrollListener());

        //Set up Item Click Listeners for listview
        lvStopwatch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                useTime(position);
            }
        });

        lvStopwatch.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                lvStopwatch.showContextMenu();
                return false;
            }
        });
        return view;
    }

    private void useTime(int position) {
        valueEntered = true;
        if (!longRawTimes.isEmpty()) {
            customMillis = longRawTimes.get(position);
        }
        prefs.edit().putBoolean("newLoop", true);
        prefs.edit().commit();
        stopwatchIntent.putExtra("customMillis", customMillis);
        stopwatchIntent.putExtra("valueEntered", valueEntered);
        updateStopwatch(customMillis);
        updateLoop(0);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Choose Action");
        getSherlockActivity().getMenuInflater().inflate(R.menu.context_menu_stopwatch, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (id == R.id.delete_time) {
            stopwatchTimes.remove(info.position);
            simpleAdapter.notifyDataSetChanged();
        } else if (id == R.id.use_time) {
            useTime(info.position);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_delete_times).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_delete_times) {
            stopwatchTimes.clear();
            longRawTimes.clear();
            simpleAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        HashMap<String, String> item = new HashMap<String, String>();
        if (id == R.id.bStart) {
            startAction();
        } else if (id == R.id.bStop) {
            stopAction(item);
        } else if (id == R.id.bReset) {
            resetAction();
        } else if (id == R.id.bLoop) {
            loopAction(item);
        }
        simpleAdapter.notifyDataSetChanged();
        lvStopwatch.invalidateViews();
    }

    private void startAction() {
        showStopButton();
        stopwatchRunning = true;
        loopRunning = true;
        getSherlockActivity().getApplicationContext().startService(stopwatchIntent);
        stopwatchIntent.removeExtra("valueEntered");
        getSherlockActivity().getApplicationContext().startService(swLoopIntent);
    }

    private void stopAction(HashMap<String, String> item) {
        hideStopButton();
        stopwatchRunning = false;
        loopRunning = false;
        getSherlockActivity().getApplicationContext().stopService(stopwatchIntent);
        item.put("line1", txtStopwatch.getText().toString() + txtStopwatchMillis.getText().toString());
        getSherlockActivity().getApplicationContext().stopService(swLoopIntent);
        updateLoop(findLoopValue());
        item.put("line2", txtStopwatchLoopMain.getText().toString() + txtStopwatchLoopMillis.getText().toString());
        longRawTimes.add(0, elapsedTime);
        stopwatchTimes.add(0, item);
    }

    private void loopAction(HashMap<String, String> item) {
        item.put("line1", txtStopwatch.getText().toString() + txtStopwatchMillis.getText().toString());
        updateLoop(findLoopValue());
        longRawTimes.add(0, elapsedTime);
        item.put("line2", txtStopwatchLoopMain.getText().toString() + txtStopwatchLoopMillis.getText().toString());
        stopwatchTimes.add(0, item);
        prefs.edit().putBoolean("newLoop", true);
        prefs.edit().commit();
        getSherlockActivity().getApplicationContext().startService(swLoopIntent);
    }

    private void resetAction() {
        //Reset main stopwatch
        if (valueEntered) {
            stopwatchIntent.removeExtra("valueEntered");
        }
        txtStopwatch.setText("0 : 00 : 00");
        txtStopwatchMillis.setText(". 000");
        StopwatchService service = new StopwatchService();
        service.preferences = getSherlockActivity().getSharedPreferences("StopwatchServicePrefs", 0);
        SharedPreferences.Editor editor = service.preferences.edit();
        editor.clear();
        editor.commit();

        //Reset Loop Stopwatch
        txtStopwatchLoopMain.setText("0 : 00 : 00");
        txtStopwatchLoopMillis.setText(". 000");
        StopwatchLoopService loopService = new StopwatchLoopService();
        loopService.preferences = getSherlockActivity().getSharedPreferences("StopwatchLoopServicePrefs", 0);
        editor = loopService.preferences.edit();
        editor.clear();
        editor.commit();
    }

    private long findLoopValue() {
        if (!longRawTimes.isEmpty())
            return elapsedTime - longRawTimes.get(0);
        else
            return elapsedTime;

    }

    private void showStopButton() {
        btnStart.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
        btnLoop.setVisibility(View.VISIBLE);
    }

    private void hideStopButton() {
        btnStart.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
        btnLoop.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("stopwatchRunning", stopwatchRunning);
        outState.putBoolean("loopRunning", loopRunning);
        saveTimes = stopwatchTimes;
        staticRawTimes = longRawTimes;
    }

    @Override
    public void onPause() {
        super.onPause();
        //Save data for main stopwatch
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("stopwatchRunning", stopwatchRunning);
        editor.putBoolean("loopRunning", loopRunning);
        editor.putString("stopwatchMain", txtStopwatch.getText().toString());
        editor.putString("stopwatchMillis", txtStopwatchMillis.getText().toString());

        //Save data for loop stopwatch
        editor.putString("stopwatchLoopMain", txtStopwatchLoopMain.getText().toString());
        editor.putString("stopwatchLoopMillis", txtStopwatchLoopMillis.getText().toString());
        saveTimes = stopwatchTimes;
        staticRawTimes = longRawTimes;
        editor.commit();
    }

    public class StopwatchReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (stopwatchRunning) {
                elapsedTime = intent.getLongExtra("elapsedTime", 0);
                updateStopwatch(elapsedTime);
            }
        }

    }

    public class StopwatchLoopReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (loopRunning) {
                long elapsedLoopTime = intent.getLongExtra("elapsedLoopTime", 0);
                updateLoop(elapsedLoopTime);
            }
        }
    }

}
