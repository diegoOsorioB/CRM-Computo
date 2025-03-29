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

@Named
@ViewScoped
public class DevolucionPedidoBean implements Serializable {

    private String direccionIp = "https://5b22-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app";
    private String coleccion = "devoluciones";
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
        consultarDevolucionesUsuario();
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    public String getEstadoFiltro() {
        return estadoFiltro;
    }

    public void setEstadoFiltro(String estadoFiltro) {
        this.estadoFiltro = estadoFiltro;
    }

    /**
     * Consulta solo las devoluciones del usuario actual
     */
    public void consultarDevolucionesUsuario() {
        FacesContext context = FacesContext.getCurrentInstance();
        this.correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

        if (this.correoUsuario == null || this.correoUsuario.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error: Debes iniciar sesión para ver tus devoluciones", null));
            return;
        }

        String endpoint = direccionIp + "/DatabaseService/api/" + coleccion + "?correoUsuario=" + this.correoUsuario;

        Client client = ClientBuilder.newClient();

        try {
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                Jsonb jsonb = JsonbBuilder.create();
                devoluciones = Arrays.asList(jsonb.fromJson(jsonResponse, Devolucion[].class));
                jsonb.close();
            } else {
                String errorMsg = response.readEntity(String.class);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error al obtener tus devoluciones: " + errorMsg, null));
            }
            response.close();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error al conectar con el servidor", null));
        } finally {
            client.close();
        }
    }

    /**
     * Registra una nueva devolución
     */
    public void registrarDevolucion() throws Exception {
        FacesContext context = FacesContext.getCurrentInstance();
        this.correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

        if (this.correoUsuario == null || this.correoUsuario.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error: Debes iniciar sesión para registrar una devolución", null));
            return;
        }

        // Validar campos obligatorios
        if (devolucion.getMotivo() == null || devolucion.getMotivo().trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Debes especificar el motivo de la devolución", null));
            return;
        }

        // Configurar estado inicial
        devolucion.setEstatus("Pendiente");
        devolucion.setEstatusInicial("Pendiente");

        String endpoint = direccionIp + "/DatabaseService/api/" + coleccion;

        Client client = ClientBuilder.newClient();
        Jsonb jsonb = JsonbBuilder.create();

        try {
            String json = jsonb.toJson(Arrays.asList(devolucion));

            Response response = client.target(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            if (response.getStatus() == 200 || response.getStatus() == 201) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Devolución registrada con éxito. Te notificaremos por correo sobre su estado.", null));
                
                // Enviar correo de confirmación
                emailService.enviarCorreoDevolucion(
                    this.correoUsuario,
                    "Cliente", // Podrías obtener el nombre del usuario de la sesión
                    devolucion.getIdDevolucion(),
                    devolucion.getEstatus(),
                    "Tu solicitud de devolución ha sido recibida"
                );
                
                // Limpiar el formulario
                devolucion = new Devolucion();
                // Actualizar la lista
                consultarDevolucionesUsuario();
            } else {
                String errorMsg = response.readEntity(String.class);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error al registrar la devolución: " + errorMsg, null));
            }
            response.close();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error al conectar con el servidor", null));
        } finally {
            client.close();
            jsonb.close();
        }
    }

    /**
     * Filtra las devoluciones por estado
     */
    public void filtrarDevoluciones() {
        if (estadoFiltro == null || estadoFiltro.isEmpty()) {
            consultarDevolucionesUsuario();
        } else {
            List<Devolucion> filtradas = new ArrayList<>();
            for (Devolucion d : devoluciones) {
                if (d.getEstatus().equalsIgnoreCase(estadoFiltro)) {
                    filtradas.add(d);
                }
            }
            devoluciones = filtradas;
        }
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
}