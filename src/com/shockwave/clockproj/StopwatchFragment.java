package com.shockwave.clockproj;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class StopwatchFragment extends Fragment implements View.OnClickListener, NumberPicker.OnValueChangeListener {
    Intent stopwatchIntent;

    Button btnStart, btnStop, btnReset, btnLoop;
    TextView txtStopwatch, txtStopwatchMillis;
    NumberPicker npTotalTimes;

    //Listview Vars
    ListView lvStopwatch;
    String[] stopwatchTimes;
    private static int TOTAL_TIMES = 3;
    int stopwatchLooped;

    //Stopwatch Vars
    private StopwatchReceiver receiver;
    long customMillis;
    int j = 0;
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
        prefs = getActivity().getSharedPreferences("StopwatchFragmentPrefs", 0);
        prefs.getBoolean("stopwatchViewSaves", true);
        stopwatchTimes = new String[TOTAL_TIMES];
        for (int i = 0; i < TOTAL_TIMES; i++) {
            stopwatchTimes[i] = " ";
        }
        if (savedInstanceState != null) {
            stopwatchRunning = savedInstanceState.getBoolean("stopwatchRunning", false);
            stopwatchTimes = savedInstanceState.getStringArray("stopwatchTimes");
        } else {
            stopwatchRunning = prefs.getBoolean("stopwatchRunning", false);
            Log.d("savedPASS", String.valueOf(stopwatchRunning));
            for (int i = 0; i < TOTAL_TIMES; i++)
                stopwatchTimes[i] = prefs.getString("stopwatchTimes_" + i, " ");
        }
        stopwatchAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, stopwatchTimes);

        IntentFilter filter = new IntentFilter(StopwatchService.START_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new StopwatchReceiver();
        getActivity().getApplicationContext().registerReceiver(receiver, filter);
        stopwatchIntent = new Intent(getActivity().getApplicationContext(), StopwatchService.class);
        super.onCreate(savedInstanceState);
    }

    public boolean isFree() {
        return getActivity().getPackageName().toLowerCase().contains("free");
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
                        for (int pos = position; pos < stopwatchTimes.length - 1; pos++) {
                            stopwatchTimes[pos] = stopwatchTimes[pos + 1];
                            stopwatchTimes[pos + 1] = " ";
                        }
                        if (position == stopwatchTimes.length - 1) {
                            stopwatchTimes[position] = " ";
                        }
                        stopwatchAdapter.notifyDataSetChanged();
                    } else {
                        if (!stopwatchTimes[position].equals(" ")) {
                            valueEntered = true;
                            char[] stopwatchMain = stopwatchTimes[position].toCharArray();
                            int hrs = Integer.parseInt(String.valueOf(stopwatchMain[0]));
                            int mins = Integer.parseInt(String.valueOf(stopwatchMain[4]) + String.valueOf(stopwatchMain[5]));
                            int secs = Integer.parseInt(String.valueOf(stopwatchMain[9]) + String.valueOf(stopwatchMain[10]));
                            char[] stopwatchMilli = stopwatchTimes[position].toCharArray();
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
                        for (int pos = position; pos < stopwatchTimes.length - 1; pos++) {
                            stopwatchTimes[pos] = stopwatchTimes[pos + 1];
                            stopwatchTimes[pos + 1] = " ";
                        }
                        if (position == stopwatchTimes.length - 1) {
                            stopwatchTimes[position] = " ";
                        }
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
        getActivity().getMenuInflater().inflate(R.menu.context_menu_stopwatch, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (id == R.id.delete_time) {
            int pos = info.position;
            for (pos = info.position; pos < stopwatchTimes.length - 1; pos++) {
                stopwatchTimes[pos] = stopwatchTimes[pos + 1];
                stopwatchTimes[pos + 1] = " ";
            }
            if (pos == stopwatchTimes.length - 1) {
                stopwatchTimes[pos] = " ";
            }
            stopwatchAdapter.notifyDataSetChanged();
        } else if (id == R.id.use_time) {
            if (!isFree()) {
                if (!stopwatchTimes[info.position].equals(" ")) {
                    valueEntered = true;
                    char[] stopwatchMain = stopwatchTimes[info.position].toCharArray();
                    int hrs = Integer.parseInt(String.valueOf(stopwatchMain[0]));
                    int mins = Integer.parseInt(String.valueOf(stopwatchMain[4]) + String.valueOf(stopwatchMain[5]));
                    int secs = Integer.parseInt(String.valueOf(stopwatchMain[9]) + String.valueOf(stopwatchMain[10]));
                    char[] stopwatchMilli = stopwatchTimes[info.position].toCharArray();
                    int milli = Integer.parseInt(String.valueOf(stopwatchMilli[13]) + String.valueOf(stopwatchMilli[14]) + String.valueOf(stopwatchMilli[15])) + 1000 * (secs + 60 * (mins + hrs * 60));
                    customMillis = milli;
                    stopwatchIntent.putExtra("customMillis", customMillis);
                    stopwatchIntent.putExtra("valueEntered", valueEntered);
                    updateStopwatch(milli);
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        if (!isFree()) {
            menu.findItem(R.id.menu_set_saved_times_limit).setVisible(true);
        } else {
            menu.findItem(R.id.menu_set_saved_times_limit).setVisible(false);
            menu.findItem(R.id.menu_buy_pro).setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_delete_times) {
            for (int i = 0; i < stopwatchTimes.length; i++) {
                stopwatchTimes[i] = " ";
            }
            stopwatchAdapter.notifyDataSetChanged();
        } else if (id == R.id.menu_set_saved_times_limit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });
            View v = getActivity().getLayoutInflater().inflate(R.layout.numberpicker_dialog, null);
            builder.setView(v);
            AlertDialog alertDialog = builder.create();

            npTotalTimes = (NumberPicker) v.findViewById(R.id.npSavedTimes);
            npTotalTimes.setMinValue(3);
            npTotalTimes.setMaxValue(25);
            npTotalTimes.setValue(TOTAL_TIMES);
            npTotalTimes.setOnValueChangedListener(this);
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            alertDialog.show();
        } else if (id == R.id.menu_buy_pro) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Buy Pro Version");
            builder.setMessage("If you buy the pro version of Clock Project, this button will be usable as a way to set the saved times limit of the stopwatch. Buy now?");
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
            stopwatchLooped++;
            if (stopwatchLooped >= TOTAL_TIMES) {
                stopwatchLooped = 1;
                j = 0;
            }
            getActivity().getApplicationContext().startService(stopwatchIntent);
            stopwatchIntent.removeExtra("valueEntered");
        } else if (id == R.id.bStop) {
            hideStopButton();
            stopwatchRunning = false;
            while (j < stopwatchLooped) {
                for (int i = stopwatchTimes.length - 1; i > 0; i--) {
                    stopwatchTimes[i] = stopwatchTimes[i - 1];
                }
                stopwatchTimes[0] = txtStopwatch.getText().toString() + txtStopwatchMillis.getText().toString();
                j++;
            }
            getActivity().getApplicationContext().stopService(stopwatchIntent);
        } else if (id == R.id.bReset) {
            if (valueEntered) {
                stopwatchIntent.removeExtra("valueEntered");
            }
            txtStopwatch.setText("0 : 00 : 00");
            txtStopwatchMillis.setText(". 000");
            StopwatchService service = new StopwatchService();
            service.preferences = getActivity().getSharedPreferences("StopwatchServicePrefs", 0);
            SharedPreferences.Editor editor = service.preferences.edit();
            editor.clear();
            editor.commit();
        } else if (id == R.id.bLoop) {
            while (j < stopwatchLooped) {
                for (int i = stopwatchTimes.length - 1; i > 0; i--) {
                    stopwatchTimes[i] = stopwatchTimes[i - 1];
                }
                stopwatchTimes[0] = txtStopwatch.getText().toString() + txtStopwatchMillis.getText().toString();
                j++;
            }
            stopwatchLooped++;
            if (stopwatchLooped >= TOTAL_TIMES) {
                stopwatchLooped = 1;
                j = 0;
            }
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
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
        String[] tempTimes = new String[stopwatchTimes.length];
        for (int i = 0; i < tempTimes.length; i++) {
            tempTimes[i] = stopwatchTimes[i];
        }
        TOTAL_TIMES = npTotalTimes.getValue();
        stopwatchTimes = new String[TOTAL_TIMES];
        for (int i = 0; i < TOTAL_TIMES; i++) {
            stopwatchTimes[i] = " ";
        }
        if (tempTimes.length < stopwatchTimes.length) {
            for (int i = 0; i < tempTimes.length; i++) {
                stopwatchTimes[i] = tempTimes[i];
            }
        } else {
            for (int i = 0; i < stopwatchTimes.length; i++) {
                stopwatchTimes[i] = tempTimes[i];
            }
        }
        stopwatchAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, stopwatchTimes);
        setupListView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("stopwatchTimes", stopwatchTimes);
        outState.putBoolean("stopwatchRunning", stopwatchRunning);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("stopwatchRunning", stopwatchRunning);
        editor.putString("stopwatchMain", txtStopwatch.getText().toString());
        editor.putString("stopwatchMillis", txtStopwatchMillis.getText().toString());
        for (int i = 0; i < TOTAL_TIMES; i++)
            editor.putString("stopwatchTimes_" + i, stopwatchTimes[i]);
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
