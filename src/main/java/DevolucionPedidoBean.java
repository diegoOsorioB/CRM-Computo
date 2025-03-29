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
import java.util.Map;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class DevolucionPedidoBean implements Serializable {

    private String direccionIp = "https://5b22-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app";
    private String coleccionPedidos = "pedidos";
    private List<Pedido> pedidos = new ArrayList<>();
    private String correoUsuario;
    private String estadoFiltro;
    
    // Datos para reportes
    private double totalVentas;
    private Map<String, Long> productosMasVendidos;
    private Map<String, Long> ventasPorEstado;
    private Map<String, Double> ventasPorMes;
    
    @Inject
    private EmailService emailService;

    // Token de autenticación
    private String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMyOTg1ODksImlhdCI6MTc0MzIxMjE4OX0.I7RozzWAML_zFw3mmdcYSih3O0FSgK78MgcXOT70FSM";

    @PostConstruct
    public void init() {
        consultarPedidos();
        generarReportes();
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
     * Consulta todos los pedidos para reportes
     */
    public void consultarPedidos() {
        FacesContext context = FacesContext.getCurrentInstance();
        this.correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

        String endpoint = direccionIp + "/DatabaseService/api/" + coleccionPedidos;
        
        // Para reportes generales, obtenemos todos los pedidos
        // (podrías agregar filtros por fecha si es necesario)

        Client client = ClientBuilder.newClient();

        try {
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                Jsonb jsonb = JsonbBuilder.create();
                pedidos = Arrays.asList(jsonb.fromJson(jsonResponse, Pedido[].class));
                jsonb.close();
            } else {
                String errorMsg = response.readEntity(String.class);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error al obtener los pedidos: " + errorMsg, null));
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
     * Genera reportes basados en los pedidos
     */
    public void generarReportes() {
        // 1. Total de ventas (solo pedidos entregados)
        this.totalVentas = pedidos.stream()
            .filter(p -> "Entregado".equals(p.getEstado()))
            .mapToDouble(Pedido::getTotal)
            .sum();
        
        // 2. Productos más vendidos (nombre y cantidad)
        this.productosMasVendidos = pedidos.stream()
            .filter(p -> "Entregado".equals(p.getEstado()))
            .flatMap(p -> p.getItems().stream())
            .collect(Collectors.groupingBy(
                item -> item.getProducto().getNombre(),
                Collectors.summingLong(ItemCarrito::getCantidad)
            ));
            
        // 3. Pedidos por estado
        this.ventasPorEstado = pedidos.stream()
            .collect(Collectors.groupingBy(
                Pedido::getEstado,
                Collectors.counting()
            ));
            
        // 4. Ventas por mes (opcional)
        /*this.ventasPorMes = pedidos.stream()
            .filter(p -> "Entregado".equals(p.getEstado()))
            .collect(Collectors.groupingBy(
                p -> p.getFecha().getMonth().toString(),
                Collectors.summingDouble(Pedido::getTotal)
            ));*/
    }

    /**
     * Filtra los pedidos por estado
     */
    public void filtrarPedidos() {
        if (estadoFiltro == null || estadoFiltro.isEmpty()) {
            consultarPedidos();
        } else {
            List<Pedido> filtrados = pedidos.stream()
                .filter(p -> p.getEstado().equalsIgnoreCase(estadoFiltro))
                .collect(Collectors.toList());
            pedidos = filtrados;
        }
    }

    // Métodos para acceder a los reportes desde la vista
    public double getTotalVentas() {
        return totalVentas;
    }

    public Map<String, Long> getProductosMasVendidos() {
        return productosMasVendidos;
    }

    public Map<String, Long> getVentasPorEstado() {
        return ventasPorEstado;
    }

    public Map<String, Double> getVentasPorMes() {
        return ventasPorMes;
    }

    // Getters y Setters originales
    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
}