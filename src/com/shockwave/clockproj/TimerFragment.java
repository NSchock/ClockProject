package com.shockwave.clockproj;

import android.content.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import net.simonvt.widget.NumberPicker;


public class TimerFragment extends SherlockFragment implements View.OnClickListener,
        NumberPicker.OnValueChangeListener {
    TextView txtTimer, txtTimerMillis;
    Button btnStartTimer, btnStopTimer, btnResetTimer;
    NumberPicker npHour, npMin, npSec;

/*    GridView gvInput;
    static final String[] numbers = new String[]{
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "0"};*/

    boolean timerRunning = false;

    private TimerReceiver receiver;
    Intent timerIntent;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        setHasOptionsMenu(true);
        IntentFilter filter = new IntentFilter(TimerService.START_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new TimerReceiver();
        getActivity().getApplicationContext().registerReceiver(receiver, filter);
        timerIntent = new Intent(getActivity().getApplicationContext(), TimerService.class);

        prefs = getActivity().getSharedPreferences("TimerFragmentPrefs", 0);
        prefs.getBoolean("timerViewSaves", true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_delete_times).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timer_fragment, null);

        txtTimer = (TextView) view.findViewById(R.id.tvTimer);
        txtTimerMillis = (TextView) view.findViewById(R.id.tvTimerMillis);

        npHour = (NumberPicker) view.findViewById(R.id.npHours);
        npMin = (NumberPicker) view.findViewById(R.id.npMinutes);
        npSec = (NumberPicker) view.findViewById(R.id.npSeconds);
        npHour.setMaxValue(23);
        npHour.setMinValue(0);
        npSec.setMaxValue(59);
        npSec.setMinValue(0);
        npMin.setMaxValue(59);
        npMin.setMinValue(0);
        npHour.setOnValueChangedListener(this);
        npMin.setOnValueChangedListener(this);
        npSec.setOnValueChangedListener(this);

/*        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSherlockActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, numbers);
        gvInput = (GridView) view.findViewById(R.id.gvTimerInput);
        gvInput.setAdapter(adapter);
        gvInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO send input to timer.
            }
        });*/

        btnStartTimer = (Button) view.findViewById(R.id.bStartTimer);
        btnResetTimer = (Button) view.findViewById(R.id.bResetTimer);
        btnStopTimer = (Button) view.findViewById(R.id.bStopTimer);
        btnStartTimer.setOnClickListener(this);
        btnResetTimer.setOnClickListener(this);
        btnStopTimer.setOnClickListener(this);

        if (savedInstanceState != null) {
            timerRunning = savedInstanceState.getBoolean("timerRunning", false);
        } else {
            timerRunning = prefs.getBoolean("timerRunning", false);
        }
        if (timerRunning) {
            showStopButtonTimer();
        }

        txtTimer.setText(prefs.getString("timerMain", "0 : 00 : 00"));
        txtTimerMillis.setText(prefs.getString("timerMillis", ". 000"));
        return view;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.bStartTimer) {
            showStopButtonTimer();
            timerIntent.putExtra("cdHour", npHour.getValue());
            timerIntent.putExtra("cdMin", npMin.getValue());
            timerIntent.putExtra("cdSec", npSec.getValue());
            getActivity().getApplicationContext().startService(timerIntent);
        } else if (id == R.id.bStopTimer) {
            hideStopButtonTimer();
            getActivity().getApplicationContext().stopService(timerIntent);
            timerRunning = false;
            timerIntent.removeExtra("timeChanged");
        } else if (id == R.id.bResetTimer) {
            if (timerRunning) {
                timerRunning = false;
            }
            npSec.setValue(0);
            npMin.setValue(0);
            npHour.setValue(0);
            TimerService service = new TimerService();
            service.timerPrefs = getActivity().getSharedPreferences("TimerServicePrefs", 0);
            SharedPreferences.Editor editor = service.timerPrefs.edit();
            editor.clear();
            editor.commit();
            timerIntent.removeExtra("cdHour");
            timerIntent.removeExtra("cdMin");
            timerIntent.removeExtra("cdSec");
            updateTimerTextView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("timerRunning", timerRunning);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("timerRunning", timerRunning);
        editor.putString("timerMain", txtTimer.getText().toString());
        editor.putString("timerMillis", txtTimerMillis.getText().toString());
        editor.commit();
    }

    private void updateTimerTextView() {
        txtTimer.setText(String.format("%d : %02d : %02d", npHour.getValue(), npMin.getValue(),
                npSec.getValue()));
        txtTimerMillis.setText(String.format(". %03d", 0));
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
        if (oldVal != newVal) {
            timerIntent.putExtra("timeChanged", true);
        }
        updateTimerTextView();
    }

    public void hideStopButtonTimer() {
        btnStartTimer.setVisibility(View.VISIBLE);
        btnResetTimer.setVisibility(View.VISIBLE);
        btnStopTimer.setVisibility(View.GONE);
    }

    public void showStopButtonTimer() {
        btnStartTimer.setVisibility(View.GONE);
        btnResetTimer.setVisibility(View.GONE);
        btnStopTimer.setVisibility(View.VISIBLE);
    }

    public class TimerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String timerMain = intent.getStringExtra("timerMain");
            txtTimer.setText(timerMain);
            String timerMillis = intent.getStringExtra("timerMillis");
            txtTimerMillis.setText(timerMillis);
            timerRunning = intent.getBooleanExtra("timerRunning", true);
            if (!timerRunning) {
                hideStopButtonTimer();
                txtTimer.setText(String.format("%d : %02d : %02d", 0, 0, 0));
                txtTimerMillis.setText(String.format(". %03d", 0));
                timerIntent.removeExtra("cdHour");
                timerIntent.removeExtra("cdMin");
                timerIntent.removeExtra("cdSec");
                npHour.setValue(0);
                npMin.setValue(0);
                npSec.setValue(0);
            }
        }
    }
}
