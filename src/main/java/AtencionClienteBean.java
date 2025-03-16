import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class AtencionClienteBean implements Serializable {
    
    private String userInput;
    private List<String> chatMensajes;
    private String nombre;
    private String email;
    private String mensaje;
    private String numeroTicket;
    private String estadoTicket;
    private int calificacion;
    private List<Integer> calificaciones;
    
    public AtencionClienteBean() {
        chatMensajes = new ArrayList<>();
        calificaciones = new ArrayList<>();
    }

    // Chat en Vivo
    public void enviarMensajeChat() {
        if (userInput != null && !userInput.trim().isEmpty()) {
            chatMensajes.add("Usuario: " + userInput);
            chatMensajes.add("Soporte: Gracias por tu mensaje, un asesor te responderá pronto.");
            userInput = "";
        }
    }
    
    // Formulario de Contacto
    public void enviarFormulario() {
        if (nombre != null && email != null && mensaje != null) {
            System.out.println("Formulario enviado por: " + nombre + ", Email: " + email + ", Mensaje: " + mensaje);
            nombre = "";
            email = "";
            mensaje = "";
        }
    }
    
    // Seguimiento de Ticket
    public void consultarTicket() {
        if (numeroTicket != null && numeroTicket.equals("12345")) {
            estadoTicket = "En proceso";
        } else {
            estadoTicket = "No encontrado";
        }
    }
    
    // Evaluación del Servicio
    public void enviarCalificacion() {
        calificaciones.add(calificacion);
    }
    
    public double getPromedioCalificaciones() {
        if (calificaciones.isEmpty()) {
            return 0.0;
        }
        int suma = calificaciones.stream().mapToInt(Integer::intValue).sum();
        return (double) suma / calificaciones.size();
    }
    
    // Getters y Setters
    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public List<String> getChatMensajes() {
        return chatMensajes;
    }
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }

    public String getEstadoTicket() {
        return estadoTicket;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }
}
