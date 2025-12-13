package com.example.keeplearning.service;

import jakarta.annotation.PostConstruct;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBookingConfirmationToStudent(
            String studentEmail,
            String teacherName,
            String subject,
            LocalDate date,
            LocalTime start,
            int durationMinutes
    ) {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("keeplearning.oth@gmail.com");
        mail.setTo(studentEmail);
        mail.setSubject("Deine Nachhilfestunde wurde gebucht");

        mail.setText("""
                Hallo,

                deine Nachhilfestunde wurde erfolgreich gebucht.

                Lehrer: %s
                Fach: %s
                Datum: %s
                Uhrzeit: %s (%d Minuten)

                Der Lehrer wurde informiert und der Termin findet wie geplant statt.

                Viel Erfolg!
                KeepLearning
                """.formatted(
                teacherName,
                subject,
                date,
                start,
                durationMinutes
        ));

        mailSender.send(mail);
    }
}
