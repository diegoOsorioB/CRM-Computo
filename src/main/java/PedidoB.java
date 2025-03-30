
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class PedidoB implements Serializable {

    private String direccionIp = "http://localhost";  // Dirección del servidor
    private String coleccion = "pedidos"; // Colección en la base de datos
    private Pedido pedido = new Pedido();
    private List<Pedido> pedidos = new ArrayList<>();
    private String correoUsuario;
    private String estadoFiltro;

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

    // Token de autenticación
    private String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMxOTU0NTgsImlhdCI6MTc0MzEwOTA1OH0.SY9bv8fRAOiLEzc2W5pO_HCjJxP3DgrZeMdht1A7Mhw";

    @PostConstruct
    public void init() {
        consultarPedidos(); // Llamar automáticamente al método de consulta
    }

    /**
     * Inserta un nuevo pedido con una solicitud POST y autenticación por token.
     */
    public void insertarPedido() throws Exception {
        FacesContext context = FacesContext.getCurrentInstance();
        String correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

        // Verificar si el usuario está autenticado
        if (correoUsuario == null || correoUsuario.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: Usuario no autenticado", null));
            return;
        }

        pedido.setCorreoUsuario(correoUsuario);
        pedido.setFecha(LocalDate.now());  // Asegura que la fecha esté asignada

        String endpoint = "https://c0c6-2806-104e-16-1f1-12e1-6efa-4429-523f.ngrok-free.app/DatabaseService/api/service/pedidos";

        Client client = ClientBuilder.newClient();
        Jsonb jsonb = JsonbBuilder.create();

        try {
            String json = jsonb.toJson(Arrays.asList(pedido));

            // Ver el JSON antes de enviarlo
            System.out.println("JSON Enviado: " + json);

            Response response = client.target(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token) // Token de autorización
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            // Ver el código de estado de la respuesta
            System.out.println("Código de estado--: " + response.getStatus());
            

            if (response.getStatus() == 200 || response.getStatus() == 201) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Pedido insertado con éxito", null));
            } else {
                String errorMsg = response.readEntity(String.class);
                System.out.println("Respuesta del servidor: " + errorMsg);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al insertar el pedido: " + errorMsg, null));
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
     * Consulta los pedidos mediante una solicitud GET con autenticación por
     * token.
     */
    public void consultarPedidos() {
        FacesContext context = FacesContext.getCurrentInstance();
        String correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

        // Verificar si el usuario está autenticado
        if (correoUsuario == null || correoUsuario.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: Usuario no autenticado", null));
            return;
        }

        // Construcción del endpoint con el filtro por correoUsuario
        String endpoint = "https://c0c6-2806-104e-16-1f1-12e1-6efa-4429-523f.ngrok-free.app/DatabaseService/api/service/pedidos?correoUsuario=" + correoUsuario;

        Client client = ClientBuilder.newClient();

        try {
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token) // Token de autorización
                    .get();

            // Verificar el código de estado de la respuesta
            System.out.println("Código de estado de la respuesta: " + response.getStatus());

            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                System.out.println("Respuesta JSON recibida: " + jsonResponse);  // Ver contenido de la respuesta

                Jsonb jsonb = JsonbBuilder.create();
                pedidos = Arrays.asList(jsonb.fromJson(jsonResponse, Pedido[].class));

                jsonb.close();
            } else {
                String errorMsg = response.readEntity(String.class);
                System.out.println("Error en la respuesta: " + errorMsg);  // Ver error de la respuesta
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al obtener los pedidos: " + errorMsg, null));
            }
            response.close();
        } catch (Exception e) {
            System.out.println("Error de conexión: " + e.getMessage());  // Ver detalles del error de conexión
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error de conexión: " + e.getMessage(), null));
        } finally {
            client.close();
        }
    }

    public void filtrarPedidosPorEstado() {
        FacesContext context = FacesContext.getCurrentInstance();

        // Verificar si el filtro está vacío, en cuyo caso cargar todos los pedidos
        if (estadoFiltro == null || estadoFiltro.isEmpty()) {
            consultarPedidos(); // Recargar todos los pedidos
        } else {
            // Filtrar los pedidos por el estado seleccionado
            pedidos = pedidos.stream()
                    .filter(p -> p.getEstado().equals(estadoFiltro))
                    .collect(Collectors.toList());
        }

        // Después de filtrar o recargar, actualizar la vista (opcional, puede hacer render)
        context.getPartialViewContext().getRenderIds().add("pedidosTabla");
        System.out.println("Se realizo  un  cambio      " + "**********");
    }

    // Getters y Setters
    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

}
