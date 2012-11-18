package com.shockwave.clockproj;

import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;

//fix numberpickers
public class StopwatchFragment extends SherlockFragment implements View.OnClickListener {
    Intent stopwatchIntent;

    Button btnStart, btnStop, btnReset, btnLoop;
    TextView txtStopwatch, txtStopwatchMillis;

    //Listview Vars
    ListView lvStopwatch;
    ArrayList<String> stopwatchTimes = new ArrayList<String>();

    //Stopwatch Vars
    private StopwatchReceiver receiver;
    long customMillis;
    boolean valueEntered = false;
    boolean stopwatchRunning = false;

    private void updateStopwatch(long time) {
        int seconds = (int) time / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        long millis = time % 1000;
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        txtStopwatch.setText(String.format("%d : %02d : %02d", hours, minutes,
                seconds));
        txtStopwatchMillis.setText(String.format(". %03d", millis));
    }

    private SharedPreferences prefs;
    ArrayAdapter<String> stopwatchAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        prefs = getSherlockActivity().getSharedPreferences("StopwatchFragmentPrefs", 0);
        prefs.getBoolean("stopwatchViewSaves", true);
        if (savedInstanceState != null) {
            stopwatchRunning = savedInstanceState.getBoolean("stopwatchRunning", false);
            stopwatchTimes = savedInstanceState.getStringArrayList("stopwatchTimes");
        } else {
            stopwatchRunning = prefs.getBoolean("stopwatchRunning", false);
            Log.d("savedPASS", String.valueOf(stopwatchRunning));
            String[] stopwatchSaveTimes = new String[prefs.getInt("stopwatchTimesLength", 0)];
            for (int i = 0; i < stopwatchSaveTimes.length - 1; i++) {
                stopwatchSaveTimes[i] = prefs.getString("stopwatchTimes_" + i, " ");
                stopwatchTimes.add(stopwatchSaveTimes[i]);
            }
            /*    Set<String> set;
            set = prefs.getStringSet("stopwatchTimes", null);
            stopwatchTimes.addAll(set);*/
        }
        stopwatchAdapter = new ArrayAdapter<String>(getSherlockActivity().getApplicationContext(), android.R.layout.simple_list_item_1, stopwatchTimes);

        IntentFilter filter = new IntentFilter(StopwatchService.START_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new StopwatchReceiver();
        getSherlockActivity().getApplicationContext().registerReceiver(receiver, filter);
        stopwatchIntent = new Intent(getSherlockActivity().getApplicationContext(), StopwatchService.class);
        super.onCreate(savedInstanceState);
    }

    public boolean isFree() {
        return getSherlockActivity().getPackageName().toLowerCase().contains("free");
    }

    private void setupListView() {
        lvStopwatch.setAdapter(stopwatchAdapter);
        registerForContextMenu(lvStopwatch);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stopwatch_fragment, null);

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

        lvStopwatch = (ListView) view.findViewById(R.id.list_stopwatch);
        setupListView();

        if (stopwatchRunning)
            showStopButton();

        final SwipeDetector swipeDetector = new SwipeDetector();

        lvStopwatch.setOnTouchListener(swipeDetector);
        lvStopwatch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!isFree()) {
                    if (swipeDetector.swipeDetected()) {
                        stopwatchTimes.remove(position);
                        stopwatchAdapter.notifyDataSetChanged();
                    } else {
                        if (!stopwatchTimes.get(position).equals(" ")) {
                            valueEntered = true;
                            char[] stopwatchMain = stopwatchTimes.get(position).toCharArray();
                            int hrs = Integer.parseInt(String.valueOf(stopwatchMain[0]));
                            int mins = Integer.parseInt(String.valueOf(stopwatchMain[4]) + String.valueOf(stopwatchMain[5]));
                            int secs = Integer.parseInt(String.valueOf(stopwatchMain[9]) + String.valueOf(stopwatchMain[10]));
                            char[] stopwatchMilli = stopwatchTimes.get(position).toCharArray();
                            int milli = Integer.parseInt(String.valueOf(stopwatchMilli[13]) + String.valueOf(stopwatchMilli[14]) + String.valueOf(stopwatchMilli[15])) + 1000 * (secs + 60 * (mins + hrs * 60));
                            customMillis = milli;
                            stopwatchIntent.putExtra("customMillis", customMillis);
                            stopwatchIntent.putExtra("valueEntered", valueEntered);
                            updateStopwatch(milli);
                        }
                    }
                } else {

                }
            }
        });

        lvStopwatch.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (isFree()) {
                    lvStopwatch.showContextMenu();
                } else {
                    if (swipeDetector.swipeDetected()) {
                        stopwatchTimes.remove(position);
                        stopwatchAdapter.notifyDataSetChanged();
                    } else {
                        lvStopwatch.showContextMenu();
                    }
                }

                return false;
            }
        });
        return view;
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
            stopwatchAdapter.notifyDataSetChanged();
        } else if (id == R.id.use_time) {
            if (!isFree()) {
                if (!stopwatchTimes.get(info.position).equals(" ")) {
                    valueEntered = true;
                    char[] stopwatchMain = stopwatchTimes.get(info.position).toCharArray();
                    int hrs = Integer.parseInt(String.valueOf(stopwatchMain[0]));
                    int mins = Integer.parseInt(String.valueOf(stopwatchMain[4]) + String.valueOf(stopwatchMain[5]));
                    int secs = Integer.parseInt(String.valueOf(stopwatchMain[9]) + String.valueOf(stopwatchMain[10]));
                    char[] stopwatchMilli = stopwatchTimes.get(info.position).toCharArray();
                    int milli = Integer.parseInt(String.valueOf(stopwatchMilli[13]) + String.valueOf(stopwatchMilli[14]) + String.valueOf(stopwatchMilli[15])) + 1000 * (secs + 60 * (mins + hrs * 60));
                    customMillis = milli;
                    stopwatchIntent.putExtra("customMillis", customMillis);
                    stopwatchIntent.putExtra("valueEntered", valueEntered);
                    updateStopwatch(milli);
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
                builder.setTitle("Feature Unavailable");
                builder.setMessage("This feature requires the pro version of Clock Project. Would you like to buy it now?");
                builder.setPositiveButton("Buy Pro", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String appName = "market://details?id=com.shockwave.clockproj.paid";
                        Intent goToMarket;
                        goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(appName));
                        startActivity(goToMarket);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
                AlertDialog alertDialog = builder.create();

                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                alertDialog.show();
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stopwatch, menu);
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
            stopwatchAdapter.notifyDataSetChanged();
        } else if (id == R.id.menu_buy_pro) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
            builder.setTitle("Buy Pro Version");
            builder.setMessage("If you buy the pro version of Clock Project, this button will allow you to set the saved times limit of the stopwatch. Buy now?");
            builder.setPositiveButton("Buy Pro", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String appName = "market://details?id=com.shockwave.clockproj.paid";
                    Intent goToMarket;
                    goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(appName));
                    startActivity(goToMarket);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });
            AlertDialog alertDialog = builder.create();

            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.bStart) {
            showStopButton();
            stopwatchRunning = true;
            getSherlockActivity().getApplicationContext().startService(stopwatchIntent);
            stopwatchIntent.removeExtra("valueEntered");
        } else if (id == R.id.bStop) {
            hideStopButton();
            stopwatchRunning = false;
            stopwatchTimes.add(0, txtStopwatch.getText().toString() + txtStopwatchMillis.getText().toString());
            getSherlockActivity().getApplicationContext().stopService(stopwatchIntent);
        } else if (id == R.id.bReset) {
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
        } else if (id == R.id.bLoop) {
            stopwatchTimes.add(0, txtStopwatch.getText().toString() + txtStopwatchMillis.getText().toString());
        }
        stopwatchAdapter.notifyDataSetChanged();
        lvStopwatch.invalidateViews();
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
//        outState.putStringArray("stopwatchTimes", stopwatchTimes);
        outState.putStringArrayList("stopwatchTimes", stopwatchTimes);
        outState.putBoolean("stopwatchRunning", stopwatchRunning);
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("stopwatchRunning", stopwatchRunning);
        editor.putString("stopwatchMain", txtStopwatch.getText().toString());
        editor.putString("stopwatchMillis", txtStopwatchMillis.getText().toString());
        String[] stopwatchSaveTimes = stopwatchTimes.toArray(new String[stopwatchTimes.size()]);
        editor.putInt("stopwatchTimesLength", stopwatchSaveTimes.length);
        for (int i = 0; i < stopwatchSaveTimes.length - 1; i++)
            editor.putString("stopwatchTimes_" + i, stopwatchSaveTimes[i]);
/*        Set<String> set = new HashSet<String>();
        set.addAll(stopwatchTimes);
        editor.putStringSet("stopwatchTimes", set);*/
        editor.commit();
    }

    public class StopwatchReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String stopwatchMain = intent.getStringExtra("stopwatchMain");
            txtStopwatch.setText(stopwatchMain);
            String stopwatchMillis = intent.getStringExtra("stopwatchMillis");
            txtStopwatchMillis.setText(stopwatchMillis);
        }

    }
}
