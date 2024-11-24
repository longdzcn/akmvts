package kd.cosmic.Test;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class Main {
    public static void main(String[] args) {
        // SMTP服务器的地址
        String host = "smtp.gmail.com";
        // SMTP服务器的端口
        String port = "587";
        // 发件人邮箱用户名
        String senderEmail = "3383174411@qq.com";
        // 发件人邮箱密码
        String password = "your_password";
        // 收件人邮箱地址
        String recipientEmail = "yijiawei@djserp.cn";
        // 邮件主题
        String subject = "Test Email";
        // 邮件内容
        String emailBody = "This is a test email.";
 
        try {
            EmailUtil.sendEmail(host, port, senderEmail, password, recipientEmail, subject, emailBody);
            System.out.println("Email sent successfully.");
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}