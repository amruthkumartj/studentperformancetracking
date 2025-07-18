package com.portal;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {

	 public static void sendOtpEmail(String recipientEmail, String otp) {
	        // Read credentials securely from Environment Variables
	        final String fromEmail = System.getenv("GMAIL_USER");
	        final String password = System.getenv("GMAIL_APP_PASSWORD");

	        // Check if the environment variables are set
	        if (fromEmail == null || password == null) {
	            System.err.println("FATAL ERROR: GMAIL_USER or GMAIL_APP_PASSWORD environment variables not set.");
	            return; // Stop the method if credentials aren't found
	        }

	        // Set up mail server properties for Gmail
	    Properties props = new Properties();
	    props.put("mail.smtp.host", "smtp.gmail.com"); // Gmail SMTP Server
        props.put("mail.smtp.port", "587"); // TLS Port
        props.put("mail.smtp.auth", "true"); // Enable authentication
        props.put("mail.smtp.starttls.enable", "true"); // Enable STARTTLS

        // Create a Session with an Authenticator
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };
        Session session = Session.getInstance(props, auth);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            msg.setSubject("Your Password Reset OTP");

            String htmlContent = "<html><body>"
                + "<div style='font-family: Arial, sans-serif; text-align: center; color: #333;'>"
                + "<h2>Password Reset Request</h2>"
                + "<p>Use the following One-Time Password (OTP) to complete the process:</p>"
                + "<p style='font-size: 24px; font-weight: bold; letter-spacing: 2px; margin: 20px; padding: 10px; background-color: #f2f2f2; border-radius: 5px;'>"
                + otp
                + "</p>"
                + "<p>This OTP is valid for 10 minutes. If you did not request this, please ignore this email.</p>"
                + "</div>"
                + "</body></html>";
            msg.setContent(htmlContent, "text/html");

            Transport.send(msg);
            System.out.println("OTP Email sent successfully via Gmail to " + recipientEmail);

        } catch (Exception e) {
            System.err.println("Error sending OTP email via Gmail: " + e.getMessage());
            e.printStackTrace();
        }
    }
}