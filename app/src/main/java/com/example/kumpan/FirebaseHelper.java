package com.example.kumpan;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ================== ПОЛЬЗОВАТЕЛИ ==================
    public static Task<DocumentSnapshot> loadUser(String login) {
        return db.collection("users").document(login).get();
    }

    public static Task<QuerySnapshot> loadAllUsers() {
        return db.collection("users").get();
    }

    public static Task<Void> createUser(String login, String fullName) {
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("role", "pending");
        user.put("photoUrl", "");
        user.put("failCount", 0);
        user.put("lockedUntil", 0);
        return db.collection("users").document(login).set(user);
    }

    public static Task<Void> updateUserRole(String login, String newRole) {
        Map<String, Object> data = new HashMap<>();
        data.put("role", newRole);
        return db.collection("users").document(login).set(data, SetOptions.merge());
    }

    // ================== ЗАДАЧИ ==================
    public static Task<QuerySnapshot> loadTasks() {
        return db.collection("tasks").get();
    }

    public static Task<Void> updateTaskStatus(String taskId, String newStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", newStatus);
        return db.collection("tasks").document(taskId).set(data, SetOptions.merge());
    }

    public static Task<Void> rejectTask(String taskId, String comment, String checkedBy) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "rejected");
        data.put("checkComment", comment);
        data.put("checkedBy", checkedBy);
        return db.collection("tasks").document(taskId).set(data, SetOptions.merge());
    }

    // ================== ГРАФИК ==================
    public static Task<QuerySnapshot> loadSchedules() {
        return db.collection("schedules").get();
    }

    public static Task<Void> createSchedule(String userId, String date, String timeStart,
                                            String timeEnd, String position,
                                            String department, String createdBy) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("date", date);
        data.put("timeStart", timeStart);
        data.put("timeEnd", timeEnd);
        data.put("position", position);
        data.put("department", department);
        data.put("createdBy", createdBy);
        return db.collection("schedules").document().set(data);
    }

    // ================== ОБУЧЕНИЕ (ТТК) ==================
    public static Task<QuerySnapshot> loadTtkCollection(String collectionName) {
        return db.collection(collectionName).get();
    }

    public static Task<DocumentSnapshot> loadTtkDetail(String collectionName, String docId) {
        return db.collection(collectionName).document(docId).get();
    }

    // ================== ПРОГРЕСС ОБУЧЕНИЯ ==================
    public static void markAsRead(String userId, String category, String itemId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("category", category);
        data.put("itemId", itemId);
        data.put("timestamp", System.currentTimeMillis());
        db.collection("learningProgress").document(userId + "_" + itemId).set(data);
    }

    public static Task<QuerySnapshot> getReadCount(String userId, String category) {
        return db.collection("learningProgress")
                .whereEqualTo("userId", userId)
                .whereEqualTo("category", category)
                .get();
    }

    // ================== ТЕСТЫ ==================
    public static Task<QuerySnapshot> loadQuestions(String category) {
        return db.collection("questions").whereEqualTo("category", category).get();
    }

    public static Task<Void> saveTestResult(String userId, String category, int score, int total) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("category", category);
        data.put("score", score);
        data.put("total", total);
        data.put("timestamp", System.currentTimeMillis());
        return db.collection("testResults").document().set(data);
    }
}