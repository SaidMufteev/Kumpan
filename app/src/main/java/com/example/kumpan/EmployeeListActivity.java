package com.example.kumpan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EmployeeListActivity extends AppCompatActivity {

    private ListView listEmployees;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<Employee> employees = new ArrayList<>();
    private List<Position> positions = new ArrayList<>();
    private EmployeeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees);

        listEmployees = findViewById(R.id.listEmployees);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new EmployeeAdapter();
        listEmployees.setAdapter(adapter);

        listEmployees.setOnItemClickListener((parent, view, position, id) -> {
            showPositionDialog(employees.get(position));
        });

        loadPositions();
    }

    private void loadPositions() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.loadPositions().addOnSuccessListener(querySnapshot -> {
            positions.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String title = doc.getString("title");
                String role = doc.getString("role");
                Long rate = doc.getLong("rate");
                positions.add(new Position(title, role, rate != null ? rate : 0));
            }
            loadEmployees();
        });
    }

    private void loadEmployees() {
        tvEmpty.setVisibility(View.GONE);
        FirebaseHelper.loadAllUsers().addOnSuccessListener(querySnapshot -> {
            progressBar.setVisibility(View.GONE);
            employees.clear();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String login = doc.getId();
                String fullName = doc.getString("fullName");
                String positionTitle = doc.getString("positionTitle");

                if (positionTitle == null) positionTitle = "Ожидает назначения";

                employees.add(new Employee(login, fullName != null ? fullName : "Без имени", positionTitle));
            }

            if (employees.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        });
    }

    private void showPositionDialog(Employee emp) {
        if (positions.isEmpty()) return;

        String[] titles = new String[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            titles[i] = positions.get(i).title + " (" + positions.get(i).rate + " ₽/ч)";
        }

        new AlertDialog.Builder(this)
                .setTitle("Назначить: " + emp.fullName)
                .setItems(titles, (dialog, which) -> {
                    Position selectedPos = positions.get(which);
                    FirebaseHelper.assignPositionToUser(emp.login, selectedPos.role, selectedPos.rate, selectedPos.title)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Назначено: " + selectedPos.title, Toast.LENGTH_SHORT).show();
                                loadEmployees();
                            });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private static class Employee {
        String login, fullName, positionTitle;
        Employee(String login, String fullName, String positionTitle) {
            this.login = login; this.fullName = fullName; this.positionTitle = positionTitle;
        }
    }

    private static class Position {
        String title, role; long rate;
        Position(String title, String role, long rate) { this.title = title; this.role = role; this.rate = rate; }
    }

    private class EmployeeAdapter extends ArrayAdapter<Employee> {
        EmployeeAdapter() { super(EmployeeListActivity.this, 0, employees); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(EmployeeListActivity.this).inflate(R.layout.item_employee, parent, false);
            }
            Employee emp = employees.get(position);
            TextView tvFullName = convertView.findViewById(R.id.tvFullName);
            TextView tvLogin = convertView.findViewById(R.id.tvLogin);
            TextView tvRole = convertView.findViewById(R.id.tvRole);

            tvFullName.setText(emp.fullName);
            tvLogin.setText("Код: " + emp.login);
            tvRole.setText(emp.positionTitle);

            return convertView;
        }
    }
}