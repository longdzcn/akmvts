    package kd.cosmic.Test;

    import javax.mail.*;
    import javax.mail.internet.AddressException;
    import javax.mail.internet.InternetAddress;
    import javax.mail.internet.MimeMessage;
    import java.util.Properties;

    public class EmailUtil {
    public static void sendEmail(String host, String port, String senderEmail, String password, String recipientEmail, String subject, String emailBody) throws AddressException, MessagingException {
        // 设置SMTP服务器属性
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
 
        // 新建一个认证器
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, password);
            }
        };
 
        // 新建一个会话
        Session session = Session.getInstance(properties, auth);
 
        // 新建一个消息
        MimeMessage message = new MimeMessage(session);
 
        // 设置发件人和收件人
        message.setFrom(new InternetAddress(senderEmail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
 
        // 设置邮件主题和内容
        message.setSubject(subject);
        message.setText(emailBody);
 
        // 发送邮件
        Transport.send(message);
    }
}