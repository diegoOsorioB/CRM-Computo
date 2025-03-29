import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class DevolucionPedidoBean implements Serializable {

    private String direccionIp = "http://localhost";  // Dirección del servidor
    private String coleccion = "devoluciones"; // Colección en la base de datos
    private Devolucion devolucion = new Devolucion();
    private List<Devolucion> devoluciones = new ArrayList<>();
    private String correoUsuario;
    private String estadoFiltro;
    
    @Inject
    private EmailService emailService;

    // Token de autenticación
    private String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMyOTg1ODksImlhdCI6MTc0MzIxMjE4OX0.I7RozzWAML_zFw3mmdcYSih3O0FSgK78MgcXOT70FSM";

    @PostConstruct
    public void init() {
        consultarDevoluciones(); // Llamar automáticamente al método de consulta
    }

    // Getter y Setter para correoUsuario
    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setEstadoFiltro(String estadoFiltro) {
        this.estadoFiltro = estadoFiltro;
    }

    public String getEstadoFiltro() {
        return estadoFiltro;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    /**
     * Inserta una nueva devolución con una solicitud POST y autenticación por token.
     */
    public void insertarDevolucion() throws Exception {
        FacesContext context = FacesContext.getCurrentInstance();
        String correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

        // Verificar si el usuario está autenticado
        if (correoUsuario == null || correoUsuario.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: Usuario no autenticado", null));
            return;
        }

        String endpoint = direccionIp + "https://5b22-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/ " + coleccion;

        Client client = ClientBuilder.newClient();
        Jsonb jsonb = JsonbBuilder.create();

        try {
            String json = jsonb.toJson(Arrays.asList(devolucion));

            System.out.println("JSON Enviado: " + json);

            Response response = client.target(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            System.out.println("Código de estado: " + response.getStatus());

            if (response.getStatus() == 200 || response.getStatus() == 201) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Devolución registrada con éxito", null));
                consultarDevoluciones(); // Actualizar la lista después de insertar
            } else {
                String errorMsg = response.readEntity(String.class);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al registrar la devolución: " + errorMsg, null));
            }
            response.close();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error de conexión: " + e.getMessage(), null));
        } finally {
            client.close();
            jsonb.close();
        }
    }

    /**
     * Consulta las devoluciones mediante una solicitud GET con autenticación por token.
     */
    public void consultarDevoluciones() {
        FacesContext context = FacesContext.getCurrentInstance();
        String correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

        // Verificar si el usuario está autenticado
        if (correoUsuario == null || correoUsuario.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: Usuario no autenticado", null));
            return;
        }

        String endpoint = direccionIp + "https://5b22-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/ " + coleccion + "?correoUsuario=" + correoUsuario;

        Client client = ClientBuilder.newClient();

        try {
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            System.out.println("Código de estado de la respuesta: " + response.getStatus());

            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                System.out.println("Respuesta JSON recibida: " + jsonResponse);

                Jsonb jsonb = JsonbBuilder.create();
                devoluciones = Arrays.asList(jsonb.fromJson(jsonResponse, Devolucion[].class));
                jsonb.close();
            } else {
                String errorMsg = response.readEntity(String.class);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al obtener las devoluciones: " + errorMsg, null));
            }
            response.close();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error de conexión: " + e.getMessage(), null));
        } finally {
            client.close();
        }
    }

    /**
     * Actualiza el estado de una devolución y envía notificación por correo
     */
    public void actualizarEstatus(Devolucion devolucion) throws Exception {    
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (devolucion.getRazonCambioEstatus() == null || devolucion.getRazonCambioEstatus().trim().isEmpty()) {
            context.addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Debe ingresar una razón para actualizar la devolución #" + devolucion.getIdDevolucion(), ""));
            return;
        } else if (devolucion.getEstatus().equals(devolucion.getEstatusInicial())) {
            context.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "No se realizó ningún cambio. El estatus es el mismo", ""));
            return;
        }
        
        String endpoint = direccionIp + "https://5b22-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/ " + coleccion;

        Client client = ClientBuilder.newClient();
        Jsonb jsonb = JsonbBuilder.create();

        try {
            String json = jsonb.toJson(Arrays.asList(devolucion));

            Response response = client.target(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .put(Entity.entity(json, MediaType.APPLICATION_JSON));

            if (response.getStatus() == 200) {
                context.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Estado actualizado: La devolución #" + devolucion.getIdDevolucion() + " ahora está en estado: " + devolucion.getEstatus(), ""));

                // Enviar correo de notificación
                emailService.enviarCorreoDevolucion(
                    devolucion.getPedido().getUsuario().getEmail(),
                    devolucion.getPedido().getUsuario().getNombre(),
                    devolucion.getIdDevolucion(),
                    devolucion.getEstatus(),
                    devolucion.getRazonCambioEstatus()
                );

                context.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Correo enviado correctamente", ""));

                // Actualizar lista de devoluciones
                consultarDevoluciones();
            } else {
                String errorMsg = response.readEntity(String.class);
                context.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al actualizar la devolución: " + errorMsg, ""));
            }
            response.close();
        } catch (Exception e) {
            context.addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de conexión: " + e.getMessage(), ""));
        } finally {
            client.close();
            jsonb.close();
        }
    }

    public void filtrarDevolucionesPorEstado() {
        FacesContext context = FacesContext.getCurrentInstance();

        // Primero recargamos todas las devoluciones
        consultarDevoluciones();

        // Si hay un filtro aplicado, lo procesamos
        if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
            devoluciones = devoluciones.stream()
                    .filter(d -> d.getEstatus().equals(estadoFiltro))
                    .collect(Collectors.toList());
        }

        context.getPartialViewContext().getRenderIds().add("devolucionesTabla");
    }

    // Getters y Setters
    public Devolucion getDevolucion() {
        return devolucion;
    }

    public void setDevolucion(Devolucion devolucion) {
        this.devolucion = devolucion;
    }

    public List<Devolucion> getDevoluciones() {
        return devoluciones;
    }

    public void setDevoluciones(List<Devolucion> devoluciones) {
        this.devoluciones = devoluciones;
    }
}