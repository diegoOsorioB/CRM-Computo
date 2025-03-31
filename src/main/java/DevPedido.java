import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class DevPedido implements Serializable {

    private String direccionIp = "https://4c6e-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/ ";
    private String coleccion = "devoluciones";
    private Devolucion devolucion = new Devolucion();
    private List<Devolucion> devoluciones = new ArrayList<>();
    private String correoUsuario;
    private String estadoFiltro;
    private Compra pedidoSeleccionado;

    // Token de autenticación
    private String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMxOTU0NTgsImlhdCI6MTc0MzEwOTA1OH0.SY9bv8fRAOiLEzc2W5pO_HCjJxP3DgrZeMdht1A7Mhw";

    @PostConstruct
    public void init() {
        consultarDevoluciones();
    }

    public void prepararDevolucion(Compra pedido) {
        FacesContext context = FacesContext.getCurrentInstance();
        this.pedidoSeleccionado = pedido;
        this.correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");
        
        // Inicializar nueva devolución
        devolucion = new Devolucion();
        devolucion.setPedido(pedido);
        devolucion.setEstatus("Solicitada");
        devolucion.setEstatusInicial("Solicitada");
        devolucion.setMotivo(""); // Se establecerá en el modal
    }

    public void insertarDevolucion() throws Exception {
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (devolucion == null || devolucion.getPedido() == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error: No hay datos de devolución válidos", null));
            return;
        }

        if (devolucion.getMotivo() == null || devolucion.getMotivo().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Debe seleccionar un motivo para la devolución", null));
            return;
        }

        String endpoint = "https://4c6e-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/ ";

        Client client = ClientBuilder.newClient();
        Jsonb jsonb = JsonbBuilder.create();

        try {
            String json = jsonb.toJson(Arrays.asList(devolucion));
            System.out.println("JSON Enviado: " + json);

            Response response = client.target(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            if (response.getStatus() == 200 || response.getStatus() == 201) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Devolución registrada con éxito", null));
                devolucion = new Devolucion(); // Resetear formulario
                consultarDevoluciones(); // Actualizar lista
            } else {
                String errorMsg = response.readEntity(String.class);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error al registrar: " + errorMsg, null));
            }
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, 
                    "Error de conexión: " + e.getMessage(), null));
        } finally {
            client.close();
            jsonb.close();
        }
    }

    public void consultarDevoluciones() {
        FacesContext context = FacesContext.getCurrentInstance();
        String correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

        if (correoUsuario == null || correoUsuario.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error: Usuario no autenticado", null));
            return;
        }

        String endpoint = "https://4c6e-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/ " + correoUsuario;

        Client client = ClientBuilder.newClient();

        try {
            Response response = client.target(endpoint)
                    .request(MediaType.APPLICATION_JSON)
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
                        "Error al obtener devoluciones: " + errorMsg, null));
            }
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, 
                    "Error de conexión: " + e.getMessage(), null));
        } finally {
            client.close();
        }
    }

    public void filtrarDevolucionesPorEstado() {
        if (estadoFiltro == null || estadoFiltro.isEmpty()) {
            consultarDevoluciones();
        } else {
            devoluciones = devoluciones.stream()
                    .filter(d -> d.getEstatus().equalsIgnoreCase(estadoFiltro))
                    .collect(Collectors.toList());
        }
    }

    public String redirigirADevoluciones() {
        try {
            insertarDevolucion();
            return "devoluciones?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al procesar devolución", null));
            return null;
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

    public void setDevoluciones(List<Devolucion> devoluciones) {
        this.devoluciones = devoluciones;
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

    public Compra getPedidoSeleccionado() {
        return pedidoSeleccionado;
    }

    public void setPedidoSeleccionado(Compra pedidoSeleccionado) {
        this.pedidoSeleccionado = pedidoSeleccionado;
    }
}