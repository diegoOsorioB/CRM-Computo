import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.mail.MessagingException;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

@Named
@RequestScoped
public class PasswordRecoveryBean {
    
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Método para enviar el enlace de recuperación al correo electrónico
    public void sendRecoveryLink() {
        if (email == null || email.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El correo electrónico es obligatorio"));
            return;
        }
        
        if (!isValidEmail(email)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El correo electrónico no es válido"));
            return;
        }

        // Enviar correo de recuperación
        try {
            sendEmail(email);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Se ha enviado el enlace de recuperación al correo electrónico"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al enviar el correo"));
        }
    }

    // Validación simple de correo electrónico
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    // Método para enviar el correo electrónico
   
    private void sendEmail(String toEmail) {
    try {
        String fromEmail = "andchaosorder@gmail.com";  // Reemplazar con tu correo
        String password = "rupn nowv vqbb lfrt";  // Reemplazar con tu contraseña de correo

        // Configuración del servidor SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Crear sesión con autenticación
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        // Crear mensaje de correo
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Recuperación de Contraseña");
        message.setText("Haz clic en el siguiente enlace para recuperar tu contraseña:https://99e0-2806-2f0-9020-9bac-87fd-55fb-7f53-4ce3.ngrok-free.app/CRM-ComputoEN2/ResetPassword.xhtml");

        // Enviar el mensaje
        Transport.send(message);
        
        System.out.println("Correo enviado correctamente a: " + toEmail);
    } catch (MessagingException e) {
        e.printStackTrace();  // Imprime el error completo
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al enviar el correo: " + e.getMessage()));
    }
}

}
