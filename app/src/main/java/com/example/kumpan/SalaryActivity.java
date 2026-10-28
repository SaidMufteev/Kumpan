package com.example.kumpan;

import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class SalaryActivity extends AppCompatActivity {

    private TextView tvTotalEarned, tvFirstHalf, tvSecondHalf;
    private ListView listSalary;
    private List<WorkLog> logs = new ArrayList<>();
    private LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvTotalEarned = findViewById(R.id.tvTotalEarned);
        tvFirstHalf = findViewById(R.id.tvFirstHalf);
        tvSecondHalf = findViewById(R.id.tvSecondHalf);
        listSalary = findViewById(R.id.listSalary);

        adapter = new LogAdapter();
        listSalary.setAdapter(adapter);

        loadSalaryData();
    }

    private void loadSalaryData() {
        SharedPreferences prefs = getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        String userId = prefs.getString("user_login", "");
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        FirebaseHelper.getMyShifts(userId).addOnSuccessListener(querySnapshot -> {
            logs.clear();
            double totalEarned = 0, firstHalf = 0, secondHalf = 0;

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String dateStr = doc.getString("date");
                if (dateStr == null || !dateStr.startsWith(currentMonth)) continue;

                Double hours = doc.getDouble("totalHours");
                Double earnings = doc.getDouble("earnings");
                if (hours == null) hours = 0.0;
                if (earnings == null) earnings = 0.0;

                logs.add(new WorkLog(dateStr, hours, earnings));
                totalEarned += earnings;

                int day = Integer.parseInt(dateStr.split("-")[2]);
                if (day <= 15) firstHalf += earnings;
                else secondHalf += earnings;
            }

            tvTotalEarned.setText("Всего за месяц: " + (int)totalEarned + " руб.");
            tvFirstHalf.setText("1-15: " + (int)firstHalf + " руб.");
            tvSecondHalf.setText("16-31: " + (int)secondHalf + " руб.");

            if (logs.isEmpty()) Toast.makeText(this, "В этом месяце смен еще не было", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
        });
    }

    private static class WorkLog {
        String date; double hours, earnings;
        WorkLog(String date, double hours, double earnings) { this.date = date; this.hours = hours; this.earnings = earnings; }
    }

    private class LogAdapter extends ArrayAdapter<WorkLog> {
        LogAdapter() { super(SalaryActivity.this, 0, logs); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = LayoutInflater.from(SalaryActivity.this).inflate(R.layout.item_worklog, parent, false);
            WorkLog log = logs.get(position);
            ((TextView)convertView.findViewById(R.id.tvDate)).setText(log.date);
            ((TextView)convertView.findViewById(R.id.tvHours)).setText(String.format(Locale.getDefault(), "%.1f ч.", log.hours));
            ((TextView)convertView.findViewById(R.id.tvEarned)).setText((int)log.earnings + " ₽");
            return convertView;
        }
    }
}