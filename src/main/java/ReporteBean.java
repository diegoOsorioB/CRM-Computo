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

    private static final long serialVersionUID = 1L;
    
    // Configuración de conexión
    private final String direccionIp = "https://c0c6-2806-104e-16-1f1-12e1-6efa-4429-523f.ngrok-free.app";
    private final String coleccion = "pedidos";
    private final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMxOTU0NTgsImlhdCI6MTc0MzEwOTA1OH0.SY9bv8fRAOiLEzc2W5pO_HCjJxP3DgrZeMdht1A7Mhw";

    // Datos y estadísticas
    private List<Pedido> todosPedidos = new ArrayList<>();
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estadoFiltro;
    private double totalVentas;
    private int totalPedidos;
    private Map<String, Integer> ventasPorEstado = new HashMap<>();
    private Map<String, Double> ventasPorProducto = new HashMap<>();

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

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonResponse = response.readEntity(String.class);
                Jsonb jsonb = JsonbBuilder.create();
                Pedido[] pedidosArray = jsonb.fromJson(jsonResponse, Pedido[].class);
                todosPedidos = Arrays.asList(pedidosArray);
                generarEstadisticas();
            } else {
                String errorMsg = "Error en el servicio: " + response.getStatus();
                try {
                    errorMsg += " - " + response.readEntity(String.class);
                } catch (Exception e) {
                    errorMsg += " (No se pudo obtener mensaje de error)";
                }
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, null));
                
                // Cargar datos de prueba como fallback
                cargarDatosDePrueba();
            }
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión: " + e.getMessage(), null));
            // Cargar datos de prueba como fallback
            cargarDatosDePrueba();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private void cargarDatosDePrueba() {
        // Crear productos de prueba
        Producto producto1 = new Producto("1", "Laptop HP EliteBook", 18500.0);
        Producto producto2 = new Producto("2", "Mouse Logitech MX Master", 1250.0);
        Producto producto3 = new Producto("3", "Teclado mecánico Redragon", 2200.0);
        Producto producto4 = new Producto("4", "Monitor Samsung 24\"", 4500.0);
        Producto producto5 = new Producto("5", "Disco SSD 1TB", 1800.0);

        // Crear items de carrito
        List<ItemCarrito> itemsPedido1 = Arrays.asList(
                new ItemCarrito(producto1, 1),
                new ItemCarrito(producto2, 1),
                new ItemCarrito(producto5, 2)
        );

        List<ItemCarrito> itemsPedido2 = Arrays.asList(
                new ItemCarrito(producto2, 3),
                new ItemCarrito(producto3, 1),
                new ItemCarrito(producto4, 2)
        );

        List<ItemCarrito> itemsPedido3 = Arrays.asList(
                new ItemCarrito(producto1, 2),
                new ItemCarrito(producto4, 1)
        );

        List<ItemCarrito> itemsPedido4 = Arrays.asList(
                new ItemCarrito(producto3, 1),
                new ItemCarrito(producto5, 3)
        );

        List<ItemCarrito> itemsPedido5 = Arrays.asList(
                new ItemCarrito(producto2, 5),
                new ItemCarrito(producto3, 2),
                new ItemCarrito(producto4, 1),
                new ItemCarrito(producto5, 1)
        );

        // Crear pedidos de prueba
        todosPedidos = Arrays.asList(
                new Pedido("PED-001", itemsPedido1, 22300.0, "Entregado", "Av. Reforma 150", "cliente1@empresa.com"),
                new Pedido("PED-002", itemsPedido2, 14750.0, "Procesando", "Calle Juárez 45", "cliente2@mail.com"),
                new Pedido("PED-003", itemsPedido3, 41500.0, "Entregado", "Blvd. López Mateos 1200", "cliente3@correo.com"),
                new Pedido("PED-004", itemsPedido4, 7800.0, "Cancelado", "Paseo de la Rosas 67", "cliente4@example.com"),
                new Pedido("PED-005", itemsPedido5, 19250.0, "Enviado", "Calle Central 89", "cliente5@negocio.com")
        );

        // Asignar fechas
        todosPedidos.get(0).setFecha(LocalDate.now().minusDays(5));
        todosPedidos.get(1).setFecha(LocalDate.now().minusDays(3));
        todosPedidos.get(2).setFecha(LocalDate.now().minusDays(10));
        todosPedidos.get(3).setFecha(LocalDate.now().minusDays(15));
        todosPedidos.get(4).setFecha(LocalDate.now().minusDays(1));

        generarEstadisticas();
    }

    private void generarEstadisticas() {
        // Filtrar por rango de fechas
        List<Pedido> pedidosFiltrados = todosPedidos.stream()
                .filter(p -> p.getFecha() != null)
                .filter(p -> !p.getFecha().isBefore(fechaInicio) && !p.getFecha().isAfter(fechaFin))
                .collect(Collectors.toList());

        // Aplicar filtro de estado si existe
        if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
            pedidosFiltrados = pedidosFiltrados.stream()
                    .filter(p -> estadoFiltro.equals(p.getEstado()))
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
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstadoFiltro() {
        return estadoFiltro;
    }

    public void setEstadoFiltro(String estadoFiltro) {
        this.estadoFiltro = estadoFiltro;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public int getTotalPedidos() {
        return totalPedidos;
    }

    public Map<String, Integer> getVentasPorEstado() {
        return ventasPorEstado;
    }

    public Map<String, Double> getVentasPorProducto() {
        return ventasPorProducto;
    }

    public List<Pedido> getTodosPedidos() {
        return todosPedidos;
    }
}
