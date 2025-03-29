import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ReporteBean implements Serializable {

    private String direccionIp = "https://c0c6-2806-104e-16-1f1-12e1-6efa-4429-523f.ngrok-free.app";
    private String coleccion = "pedidos";
    private List<Pedido> todosPedidos = new ArrayList<>();
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estadoFiltro;
    private double totalVentas;
    private int totalPedidos;
    private Map<String, Integer> ventasPorEstado = new HashMap<>();
    private Map<String, Double> ventasPorProducto = new HashMap<>();

    private String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMxOTU0NTgsImlhdCI6MTc0MzEwOTA1OH0.SY9bv8fRAOiLEzc2W5pO_HCjJxP3DgrZeMdht1A7Mhw";

    @PostConstruct
    public void init() {
        fechaInicio = LocalDate.now().withDayOfMonth(1);
        fechaFin = LocalDate.now();
        consultarTodosPedidos();
    }

    public void consultarTodosPedidos() {
        FacesContext context = FacesContext.getCurrentInstance();
        String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccion;

        Client client = ClientBuilder.newClient();

        try {
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                Jsonb jsonb = JsonbBuilder.create();
                todosPedidos = Arrays.asList(jsonb.fromJson(jsonResponse, Pedido[].class));
                jsonb.close();
                generarEstadisticas();
            } else {
                String errorMsg = response.readEntity(String.class);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error al obtener los pedidos: " + errorMsg, null));
            }
            response.close();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error de conexión: " + e.getMessage(), null));
        } finally {
            client.close();
        }
    }

    private void generarEstadisticas() {
        // Filtrar por rango de fechas
        List<Pedido> pedidosFiltrados = todosPedidos.stream()
                .filter(p -> !p.getFecha().isBefore(fechaInicio) && !p.getFecha().isAfter(fechaFin))
                .collect(Collectors.toList());

        // Aplicar filtro de estado si existe
        if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
            pedidosFiltrados = pedidosFiltrados.stream()
                    .filter(p -> p.getEstado().equals(estadoFiltro))
                    .collect(Collectors.toList());
        }

        // Calcular totales
        totalPedidos = pedidosFiltrados.size();
        totalVentas = pedidosFiltrados.stream()
                .mapToDouble(Pedido::getTotal)
                .sum();

        // Estadísticas por estado
        ventasPorEstado = pedidosFiltrados.stream()
                .collect(Collectors.groupingBy(
                        Pedido::getEstado,
                        Collectors.summingInt(p -> 1)
                ));

        // Estadísticas por producto
        ventasPorProducto = new HashMap<>();
        pedidosFiltrados.forEach(pedido -> {
            pedido.getItems().forEach(item -> {
                String nombreProducto = item.getProducto().getNombre();
                double totalProducto = item.getProducto().getPrecio() * item.getCantidad();
                ventasPorProducto.merge(nombreProducto, totalProducto, Double::sum);
            });
        });
    }

    public void filtrarReporte() {
        generarEstadisticas();
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Reporte actualizado", null));
    }

    // Getters y Setters
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getEstadoFiltro() { return estadoFiltro; }
    public void setEstadoFiltro(String estadoFiltro) { this.estadoFiltro = estadoFiltro; }
    public double getTotalVentas() { return totalVentas; }
    public int getTotalPedidos() { return totalPedidos; }
    public Map<String, Integer> getVentasPorEstado() { return ventasPorEstado; }
    public Map<String, Double> getVentasPorProducto() { return ventasPorProducto; }
    public List<Pedido> getTodosPedidos() { return todosPedidos; }
}