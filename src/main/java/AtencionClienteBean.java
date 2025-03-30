import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class AtencionClienteBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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
        estadoTicket = "Esperando consulta...";
    }

    // Chat en Vivo
    public void enviarMensajeChat() {
        if (isValidInput(userInput)) {
            chatMensajes.add("Usuario: " + userInput);
            chatMensajes.add("Soporte: Gracias por tu mensaje, un asesor te responderá pronto.");
            userInput = ""; // Limpia la entrada después de enviarla
        }
    }
    
    // Validación de la entrada del usuario
    private boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty();
    }
    
    // Formulario de Contacto
    public void enviarFormulario() {
        if (isValidInput(nombre) && isValidInput(email) && isValidInput(mensaje)) {
            System.out.println("Formulario enviado por: " + nombre + ", Email: " + email + ", Mensaje: " + mensaje);
            // Lógica para enviar el formulario (ej. guardar en base de datos o enviar por correo)
            limpiarFormulario();
        } else {
            System.out.println("Por favor, complete todos los campos.");
        }
    }

    // Limpieza del formulario después de enviarlo
    private void limpiarFormulario() {
        nombre = "";
        email = "";
        mensaje = "";
    }
    
    // Seguimiento de Ticket
    public void consultarTicket() {
        if (isValidInput(numeroTicket)) {
            // Simulamos la consulta del estado del ticket con un ticket fijo
            estadoTicket = "12345".equals(numeroTicket) ? "En proceso" : "No encontrado";
        } else {
            estadoTicket = "Número de ticket no válido.";
        }
    }
    
    // Evaluación del Servicio
    public void enviarCalificacion() {
        if (calificacion >= 1 && calificacion <= 5) {
            calificaciones.add(calificacion);
        } else {
            System.out.println("Calificación inválida. Debe ser entre 1 y 5.");
        }
    }
    
    // Obtener el promedio de las calificaciones
    public double getPromedioCalificaciones() {
        if (calificaciones.isEmpty()) {
            return 0.0;
        }
        return calificaciones.stream().mapToInt(Integer::intValue).average().orElse(0.0);
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

    public void setEstadoTicket(String estadoTicket) {
        this.estadoTicket = estadoTicket;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }
}
