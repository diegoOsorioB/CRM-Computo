
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

@ApplicationScoped
public class EmailService {

    private final String username = "andchaosorder@gmail.com";
    private final String password = "rupn nowv vqbb lfrt";

    public void enviarCorreo(String destinatario, String asunto, String contenido) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            message.setText(contenido);

            Transport.send(message);
            System.out.println("Correo enviado con éxito a: " + destinatario);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void enviarCorreoActualizacion(String emailTo, String nombreCliente, String idPedido, String nuevoEstado) {
        String asunto = "Actualización de Estado de Pedido #" + idPedido;
        String cuerpo = "Estimado " + nombreCliente + ",\n\n"
                + "Tu pedido con ID " + idPedido + " ha sido actualizado al estado: " + nuevoEstado + ".\n\n"
                + "Gracias por tu compra.\n\n"
                + "Atentamente,\nTienda Online CRM";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
            message.setSubject(asunto);
            message.setText(cuerpo);

            Transport.send(message);
            System.out.println("Correo enviado exitosamente a " + emailTo);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void enviarCorreoDevolucion(String email, String nombre, String idDevolucion, String estatus, String razon) {
        String asunto = "Actualización de tu solicitud de devolución #" + idDevolucion;
        String mensaje = "Estimado " + nombre + ",\n\n"
                + "Tu solicitud de devolución con ID #" + idDevolucion + " ha cambiado de estado a: " + estatus + ".\n"
                + "Razón del cambio: " + razon + "\n\n"
                + "Si tienes dudas, contáctanos.\n\n"
                + "Saludos,\nEquipo de Atención al Cliente.";

        try {
            enviarCorreo(email, asunto, mensaje);
            System.out.println("Correo enviado a " + email + " sobre la devolución #" + idDevolucion);
        } catch (Exception e) {
            System.out.println("Error al enviar correo sobre la devolución #" + idDevolucion);
            e.printStackTrace();
        }
    }
}
