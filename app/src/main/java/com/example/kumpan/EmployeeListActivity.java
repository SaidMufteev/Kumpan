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
    private EmployeeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees);

        listEmployees = findViewById(R.id.listEmployees);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        adapter = new EmployeeAdapter();
        listEmployees.setAdapter(adapter);

        // Клик — диалог выбора роли
        listEmployees.setOnItemClickListener((parent, view, position, id) -> {
            showRoleDialog(employees.get(position));
        });

        loadEmployees();
    }

    private void loadEmployees() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseHelper.loadAllUsers()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    employees.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String login = doc.getId();
                        String fullName = doc.getString("fullName");
                        String role = doc.getString("role");
                        employees.add(new Employee(
                                login,
                                fullName != null ? fullName : "Без имени",
                                role != null ? role : "pending"
                        ));
                    }

                    if (employees.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showRoleDialog(Employee emp) {
        final String[] roles = {"Официант", "Бармен", "Повар", "Шеф-повар", "Менеджер"};
        final String[] roleKeys = {"waiter", "bartender", "cook", "chef", "manager"};

        int checkedItem = -1;
        for (int i = 0; i < roleKeys.length; i++) {
            if (roleKeys[i].equals(emp.role)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(emp.fullName + " (" + emp.login + ")")
                .setSingleChoiceItems(roles, checkedItem, (dialog, which) -> {
                    updateRole(emp, roleKeys[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void updateRole(Employee emp, String newRole) {
        FirebaseHelper.updateUserRole(emp.login, newRole)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Роль обновлена: " + getRoleName(newRole),
                            Toast.LENGTH_SHORT).show();
                    loadEmployees();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private static class Employee {
        String login;
        String fullName;
        String role;

        Employee(String login, String fullName, String role) {
            this.login = login;
            this.fullName = fullName;
            this.role = role;
        }
    }

    private class EmployeeAdapter extends ArrayAdapter<Employee> {
        EmployeeAdapter() {
            super(EmployeeListActivity.this, 0, employees);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(EmployeeListActivity.this)
                        .inflate(R.layout.item_employee, parent, false);
            }
            Employee emp = employees.get(position);
            TextView tvFullName = convertView.findViewById(R.id.tvFullName);
            TextView tvLogin = convertView.findViewById(R.id.tvLogin);
            TextView tvRole = convertView.findViewById(R.id.tvRole);

            tvFullName.setText(emp.fullName);
            tvLogin.setText("Код: " + emp.login);
            tvRole.setText(getRoleName(emp.role));

            return convertView;
        }
    }

    private String getRoleName(String role) {
        switch (role) {
            case "waiter":    return "Официант";
            case "bartender": return "Бармен";
            case "cook":      return "Повар";
            case "chef":      return "Шеф-повар";
            case "manager":   return "Менеджер";
            case "pending":   return "⚠ Ожидает назначения";
            default:          return role;
        }
    }
}