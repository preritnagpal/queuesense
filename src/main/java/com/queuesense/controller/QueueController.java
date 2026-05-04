package com.queuesense.controller;

import com.queuesense.model.User;
import com.queuesense.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.Queue;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/queue")
@CrossOrigin
public class QueueController {

    private final QueueService service;

    public QueueController(QueueService service) {
        this.service = service;
    }

    // 🔥 JOIN (UPDATED WITH EMAIL)
    @PostMapping("/join")
    public User join(
            @RequestParam String name,
            @RequestParam String email
    ) {
        return service.joinQueue(name, email);
    }

    // 🔹 NEXT
    @PostMapping("/next")
    public User next() {
        return service.next();
    }

    // 🔹 GET QUEUE
    @GetMapping
    public Queue<User> getQueue() {
        return service.getQueue();
    }

    // 🔹 CURRENT SERVING
    @GetMapping("/current")
    public User current() {
        return service.getCurrentServing();
    }

    // 🔹 SET SERVICE TIME
    @PostMapping("/service-time")
    public String setServiceTime(@RequestParam int time) {
        service.setServiceTime(time);
        return "Updated";
    }

    // 🔹 RESET QUEUE
    @PostMapping("/reset")
    public String reset() {
        service.resetQueue();
        return "Reset Done";
    }
    @PostMapping("/send-otp")
public String sendOtp(@RequestParam String email) {
    service.sendOtp(email);
    return "OTP Sent";
}

    @PostMapping("/verify-otp")
public String verifyOtp(
        @RequestParam String name,
        @RequestParam String email,
        @RequestParam String otp
) {
    service.verifyAndJoin(name, email, otp);
    return "Joined Successfully";
}
    @GetMapping("/stats")
public Map<String, Object> stats() {
    Map<String, Object> map = new HashMap<>();
    map.put("totalServed", service.getTotalServed());
    map.put("queueSize", service.getQueue().size());
    return map;
}
@PostMapping("/toggle")
public String toggle(@RequestParam boolean status) {
    service.toggleQueue(status);
    return "Updated";
}
}