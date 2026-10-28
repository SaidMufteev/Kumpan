package com.example.kumpan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveShiftsActivity extends AppCompatActivity {

    private ListView listActiveShifts;
    private List<ActiveShift> shifts = new ArrayList<>();
    private ShiftsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_shifts);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        listActiveShifts = findViewById(R.id.listActiveShifts);
        adapter = new ShiftsAdapter();
        listActiveShifts.setAdapter(adapter);

        loadActiveShifts();
    }

    private void loadActiveShifts() {
        FirebaseHelper.getAllActiveShifts().addOnSuccessListener(querySnapshot -> {
            shifts.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String userId = doc.getString("userId");
                Long startTime = doc.getLong("startTime");
                if (userId != null && startTime != null) {
                    FirebaseHelper.loadUser(userId).addOnSuccessListener(userDoc -> {
                        String name = userDoc.getString("fullName");
                        String position = userDoc.getString("positionTitle");
                        double hours = (System.currentTimeMillis() - startTime) / (1000.0 * 60 * 60);
                        shifts.add(new ActiveShift(userId, name, position, hours));
                        adapter.notifyDataSetChanged();
                    });
                }
            }
            if (querySnapshot.isEmpty()) Toast.makeText(this, "Сейчас никто не на смене", Toast.LENGTH_SHORT).show();
        });
    }

    private static class ActiveShift {
        String login, name, position; double hours;
        ActiveShift(String login, String name, String position, double hours) { this.login = login; this.name = name; this.position = position; this.hours = hours; }
    }

    private class ShiftsAdapter extends ArrayAdapter<ActiveShift> {
        ShiftsAdapter() { super(ActiveShiftsActivity.this, 0, shifts); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = LayoutInflater.from(ActiveShiftsActivity.this).inflate(R.layout.item_worklog, parent, false);
            ActiveShift shift = shifts.get(position);
            ((TextView)convertView.findViewById(R.id.tvDate)).setText(shift.name);
            ((TextView)convertView.findViewById(R.id.tvHours)).setText(shift.position);
            ((TextView)convertView.findViewById(R.id.tvEarned)).setText(String.format(Locale.getDefault(), "%.1f ч.", shift.hours));
            return convertView;
        }
    }
}