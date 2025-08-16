package phankhanh.book_store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    public void sendOtp (String to, String opt) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Verify your email");
        msg.setText("Your OTP is: " + opt + "\nThis OTP will expire in 5 minutes.");
        mailSender.send(msg);
    }
}
