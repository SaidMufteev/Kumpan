package com.example.kumpan;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TasksFragment extends Fragment {

    private ListView listTasks;
    private ProgressBar progressBar;
    private TextView tvEmptyTasks;
    private Button btnToggleMode;
    private List<Task> taskList = new ArrayList<>();
    private TaskAdapter adapter;

    private String currentRole;
    private String currentLogin;
    private boolean isReviewMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        listTasks = view.findViewById(R.id.listTasks);
        progressBar = view.findViewById(R.id.progressBarTasks);
        tvEmptyTasks = view.findViewById(R.id.tvEmptyTasks);
        btnToggleMode = view.findViewById(R.id.btnToggleMode);

        SharedPreferences prefs = requireContext().getSharedPreferences("KumpanPrefs", MODE_PRIVATE);
        currentRole = prefs.getString("user_role", "guest");
        currentLogin = prefs.getString("user_login", "");

        if ("manager".equals(currentRole) || "chef".equals(currentRole)) {
            btnToggleMode.setVisibility(View.VISIBLE);
        }

        btnToggleMode.setOnClickListener(v -> {
            isReviewMode = !isReviewMode;
            btnToggleMode.setText(isReviewMode ? "Все задачи" : "На проверке");
            tvEmptyTasks.setText(isReviewMode ? "Нет задач на проверке" : "У вас нет активных задач");
            loadTasks();
        });

        adapter = new TaskAdapter();
        listTasks.setAdapter(adapter);

        listTasks.setOnItemClickListener((parent, view1, position, id) -> {
            Task task = taskList.get(position);
            if (isReviewMode) {
                showReviewDialog(task);
            } else {
                if ("pending".equals(task.status) || "rejected".equals(task.status)) {
                    showCompleteTaskDialog(task);
                } else {
                    Toast.makeText(getContext(), "Задача уже на проверке", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadTasks();
        return view;
    }

    private void loadTasks() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyTasks.setVisibility(View.GONE);

        FirebaseHelper.loadTasks()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    taskList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String id = doc.getId();
                        String text = doc.getString("text");
                        String role = doc.getString("role");
                        String department = doc.getString("department");
                        String assignedTo = doc.getString("assignedTo");
                        String status = doc.getString("status");
                        String priority = doc.getString("priority");
                        String checkComment = doc.getString("checkComment");

                        boolean isVisible = false;

                        if (isReviewMode) {
                            if (!"completed".equals(status)) continue;
                            if ("manager".equals(currentRole)) isVisible = true;
                            else if ("chef".equals(currentRole) && "kitchen".equals(department)) isVisible = true;
                        } else {
                            if (!"pending".equals(status) && !"rejected".equals(status)) continue;
                            if ("manager".equals(currentRole)) isVisible = true;
                            else if ("chef".equals(currentRole) && ("cook".equals(role) || "chef".equals(role) || "all".equals(role))) isVisible = true;
                            else if (currentRole.equals(role) || "all".equals(role)) isVisible = true;

                            if (isVisible && assignedTo != null && !assignedTo.isEmpty()
                                    && !assignedTo.equals(currentLogin) && !"manager".equals(currentRole)) {
                                isVisible = false;
                            }
                        }

                        if (isVisible) {
                            taskList.add(new Task(id, text, status, priority, checkComment));
                        }
                    }

                    if (taskList.isEmpty()) tvEmptyTasks.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // === ЛОГИКА СОТРУДНИКА ===
    private void showCompleteTaskDialog(Task task) {
        new AlertDialog.Builder(getContext())
                .setTitle(task.text)
                .setMessage("Вы уверены, что задача выполнена?")
                .setPositiveButton("Да, выполнено", (dialog, which) -> {
                    FirebaseHelper.updateTaskStatus(task.id, "completed")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Задача отправлена на проверку!", Toast.LENGTH_SHORT).show();
                                loadTasks();
                            });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // === ЛОГИКА МЕНЕДЖЕРА ===
    private void showReviewDialog(Task task) {
        new AlertDialog.Builder(getContext())
                .setTitle(task.text)
                .setMessage("Сотрудник выполнил эту задачу. Принять?")
                .setPositiveButton("Принять", (dialog, which) -> approveTask(task))
                .setNegativeButton("Отклонить", (dialog, which) -> showRejectDialog(task))
                .setNeutralButton("Закрыть", null)
                .show();
    }

    private void approveTask(Task task) {
        FirebaseHelper.updateTaskStatus(task.id, "approved")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Задача принята!", Toast.LENGTH_SHORT).show();
                    loadTasks();
                });
    }

    private void showRejectDialog(Task task) {
        EditText etComment = new EditText(getContext());
        etComment.setHint("Причина отклонения");
        etComment.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(getContext())
                .setTitle("Отклонить задачу")
                .setView(etComment)
                .setPositiveButton("Отправить", (dialog, which) -> {
                    String comment = etComment.getText().toString().trim();
                    if (comment.isEmpty()) comment = "Без комментария";
                    FirebaseHelper.rejectTask(task.id, comment, currentLogin)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Задача отклонена", Toast.LENGTH_SHORT).show();
                                loadTasks();
                            });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private static class Task {
        String id, text, status, priority, checkComment;

        Task(String id, String text, String status, String priority, String checkComment) {
            this.id = id;
            this.text = text;
            this.status = status;
            this.priority = priority;
            this.checkComment = checkComment;
        }
    }

    private class TaskAdapter extends ArrayAdapter<Task> {
        TaskAdapter() {
            super(requireContext(), 0, taskList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_task, parent, false);
            }
            Task task = taskList.get(position);
            CheckBox cbTask = convertView.findViewById(R.id.cbTask);
            TextView tvTaskText = convertView.findViewById(R.id.tvTaskText);
            TextView tvUrgent = convertView.findViewById(R.id.tvUrgent);
            TextView tvStatus = convertView.findViewById(R.id.tvStatus);

            tvTaskText.setText(task.text);
            tvUrgent.setVisibility("urgent".equals(task.priority) ? View.VISIBLE : View.GONE);

            if ("completed".equals(task.status)) {
                cbTask.setChecked(true);
                tvTaskText.setPaintFlags(tvTaskText.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskText.setTextColor(0xFF999999);
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("Ждёт проверки");
            } else if ("rejected".equals(task.status)) {
                cbTask.setChecked(false);
                tvTaskText.setPaintFlags(tvTaskText.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                tvTaskText.setTextColor(0xFFFF0000);
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("Отклонена: " + (task.checkComment != null ? task.checkComment : ""));
            } else {
                cbTask.setChecked(false);
                tvTaskText.setPaintFlags(tvTaskText.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                tvTaskText.setTextColor(0xFF2D3436);
                tvStatus.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
}