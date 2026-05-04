package com.queuesense.service;

import com.queuesense.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class QueueService {

    private LinkedList<User> queue = new LinkedList<>();
    private int tokenCounter = 1;

    private int serviceTime = 10;
    private User currentServing = null;

    private boolean emailSent = false;

    private int totalServed = 0; // 🔥 stats

    private boolean isOpen = true; // 🔥 queue control

    private Map<String, Long> lastJoin = new HashMap<>(); // 🔥 rate limit

    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    private Map<String, String> otpStore = new HashMap<>();
    private Map<String, Long> otpTime = new HashMap<>();

    public QueueService(SimpMessagingTemplate messagingTemplate, EmailService emailService) {
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
    }

    private void broadcastQueue() {
        messagingTemplate.convertAndSend("/topic/queue", queue);
    }

    // 🔥 FILE SAVE
    private void saveToFile(User user, String status) {
        try (FileWriter fw = new FileWriter("queue-log.txt", true)) {
            fw.write(user.getName() + "," + user.getEmail() + "," + user.getToken() + "," + status + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔥 SEND OTP
    public void sendOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        otpStore.put(email, otp);
        otpTime.put(email, System.currentTimeMillis());
        emailService.sendOtpMail(email, otp);
    }

    // 🔥 VERIFY OTP + JOIN
    public User verifyAndJoin(String name, String email, String otp) {

        if (!otpStore.containsKey(email)) throw new RuntimeException("OTP not found");

        if (System.currentTimeMillis() - otpTime.get(email) > 120000) {
            otpStore.remove(email);
            otpTime.remove(email);
            throw new RuntimeException("OTP expired");
        }

        if (!otpStore.get(email).equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        otpStore.remove(email);
        otpTime.remove(email);

        return joinQueue(name, email);
    }

    // 🔹 JOIN
    public User joinQueue(String name, String email) {

        if (!isOpen) throw new RuntimeException("Queue Closed");

        // 🔥 rate limit
        long now = System.currentTimeMillis();
        if (lastJoin.containsKey(email) && now - lastJoin.get(email) < 60000) {
            throw new RuntimeException("Wait before rejoining");
        }
        lastJoin.put(email, now);

        // 🔥 duplicate
        boolean exists = queue.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

        if (exists) throw new RuntimeException("Already in queue");

        if (queue.size() >= 10) throw new RuntimeException("Queue Full");

        int waitTime = queue.isEmpty()
                ? serviceTime * 60
                : queue.getLast().getWaitTime() + (serviceTime * 60);

        User user = new User(name, email, tokenCounter++, queue.size() + 1, waitTime);

        queue.add(user);
        saveToFile(user, "ACTIVE");

        broadcastQueue();
        return user;
    }

    // 🔹 NEXT
    public User next() {

        if (queue.isEmpty()) return null;

        User served = queue.poll();

        if (served != null) {
            totalServed++; // 🔥 stats
            saveToFile(served, "DONE");
            System.out.println("📱 SMS: " + served.getName() + " served"); // 🔥 fake SMS
        }

        currentServing = served;

        LinkedList<User> updated = new LinkedList<>();
        int pos = 1;

        for (User u : queue) {

            int newWait = Math.max(0, u.getWaitTime() - (serviceTime * 60));

            updated.add(new User(u.getName(), u.getEmail(), u.getToken(), pos, newWait));
            pos++;
        }

        queue = updated;
        emailSent = false;

        broadcastQueue();
        return currentServing;
    }

    // 🔥 REAL-TIME
    @Scheduled(fixedRate = 1000)
    public void updateWaitTime() {

        if (queue.isEmpty()) return;

        LinkedList<User> updated = new LinkedList<>();
        int pos = 1;

        for (User u : queue) {

            int wait = Math.max(0, u.getWaitTime() - 1);

            updated.add(new User(u.getName(), u.getEmail(), u.getToken(), pos, wait));
            pos++;
        }

        queue = updated;

        // 🔥 ALERT
        if (queue.size() >= 2) {

            User nextUser = queue.get(1);

            if (nextUser.getWaitTime() <= 60 && !emailSent) {

                emailService.sendTurnAlert(nextUser.getEmail(), nextUser.getName());
                emailSent = true;
            }

            if (nextUser.getWaitTime() > 60) {
                emailSent = false;
            }
        }

        broadcastQueue();
    }

    // 🔹 ADMIN CONTROLS
    public void setServiceTime(int time) { this.serviceTime = time; }

    public void toggleQueue(boolean status) { this.isOpen = status; }

    public int getTotalServed() { return totalServed; }

    public void resetQueue() {
        queue.clear();
        tokenCounter = 1;
        currentServing = null;
        emailSent = false;
        totalServed = 0;
        broadcastQueue();
    }

    public Queue<User> getQueue() { return queue; }

    public User getCurrentServing() { return currentServing; }
}