package com.example.kumpan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;

public class TasksFragment extends Fragment {

    // РОЛЬ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ (пока захардкожено)
    // Позже будем брать из Firebase
    // Варианты: "manager", "cook", "waiter", "bartender", "chef"
    private static final String CURRENT_USER_ROLE = "cook";

    private ListView listTasks;
    private EditText etNewTask;
    private Button btnAddTask;
    private LinearLayout addTaskBlock;

    private ArrayList<Task> taskList;
    private TaskAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        listTasks = view.findViewById(R.id.listTasks);
        etNewTask = view.findViewById(R.id.etNewTask);
        btnAddTask = view.findViewById(R.id.btnAddTask);
        addTaskBlock = view.findViewById(R.id.addTaskBlock);

        // Показываем блок добавления только менеджеру
        if (CURRENT_USER_ROLE.equals("manager")) {
            addTaskBlock.setVisibility(View.VISIBLE);
        } else {
            addTaskBlock.setVisibility(View.GONE);
        }

        // Тестовые задачи
        taskList = new ArrayList<>();
        taskList.add(new Task("Проверить маркировки на десертах", false));
        taskList.add(new Task("Сделать заявки на завтра", true));
        taskList.add(new Task("Сделать чек-лист", false));
        taskList.add(new Task("Проверить чистоту на баре", false));
        taskList.add(new Task("Сделать заготовки", false));

        adapter = new TaskAdapter(requireContext());
        listTasks.setAdapter(adapter);

        // Клик — переключить статус
        listTasks.setOnItemClickListener((parent, view1, position, id) -> {
            Task task = taskList.get(position);
            task.done = !task.done;
            adapter.notifyDataSetChanged();
        });

        // Долгое нажатие — удалить (только менеджер)
        listTasks.setOnItemLongClickListener((parent, view1, position, id) -> {
            if (!CURRENT_USER_ROLE.equals("manager")) {
                Toast.makeText(getContext(), "Только менеджер может удалять задачи", Toast.LENGTH_SHORT).show();
                return true;
            }
            taskList.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Задача удалена", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Кнопка "Добавить"
        btnAddTask.setOnClickListener(v -> {
            String text = etNewTask.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(getContext(), "Введите задачу", Toast.LENGTH_SHORT).show();
                return;
            }
            taskList.add(new Task(text, false));
            adapter.notifyDataSetChanged();
            etNewTask.setText("");
        });

        return view;
    }

    private static class Task {
        String text;
        boolean done;

        Task(String text, boolean done) {
            this.text = text;
            this.done = done;
        }
    }

    private class TaskAdapter extends ArrayAdapter<Task> {
        TaskAdapter(android.content.Context context) {
            super(context, 0, taskList);
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

            tvTaskText.setText(task.text);
            cbTask.setChecked(task.done);

            if (task.done) {
                tvTaskText.setPaintFlags(tvTaskText.getPaintFlags()
                        | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskText.setTextColor(0xFF999999);
            } else {
                tvTaskText.setPaintFlags(tvTaskText.getPaintFlags()
                        & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskText.setTextColor(0xFF2D3436);
            }

            return convertView;
        }
    }
}