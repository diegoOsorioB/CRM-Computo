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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class DevolucionPedidoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // Configuración de conexión
    private String direccionIp = "https://5b22-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app";
    private String coleccionPedidos = "pedidos";
    private final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMxOTU0NTgsImlhdCI6MTc0MzEwOTA1OH0.SY9bv8fRAOiLEzc2W5pO_HCjJxP3DgrZeMdht1A7Mhw";
    
    // Datos y estado
    private List<Pedido> pedidos = new ArrayList<>();
    private String correoUsuario;
    private String estadoFiltro;
    private boolean usarDatosPrueba = false; // Por defecto intenta conectar a la API

    // Datos para reportes
    private double totalVentas;
    private Map<String, Long> productosMasVendidos;
    private Map<String, Long> ventasPorEstado;
    private Map<String, Double> ventasPorMes;
    
    private List<Devolucion> devoluciones = new ArrayList<>();
    private Devolucion devolucionActual = new Devolucion();

    @PostConstruct
    public void init() {
        if (usarDatosPrueba) {
            cargarDatosDePrueba();
        } else {
            consultarPedidos();
        }
        generarReportes();
    }

    public void consultarPedidos() {
        FacesContext context = FacesContext.getCurrentInstance();
        String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccionPedidos;

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
                pedidos = Arrays.asList(pedidosArray);
            } else {
                String errorMsg = "Error en el servicio: " + response.getStatus();
                try {
                    errorMsg += " - " + response.readEntity(String.class);
                } catch (Exception e) {
                    errorMsg += " (No se pudo obtener mensaje de error)";
                }
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, null));
                
                // Fallback a datos de prueba
                cargarDatosDePrueba();
            }
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión: " + e.getMessage(), null));
            // Fallback a datos de prueba
            cargarDatosDePrueba();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private void cargarDatosDePrueba() {
        // Crear productos de prueba
        Producto producto1 = new Producto("1", "Laptop HP", 15000.0);
        Producto producto2 = new Producto("2", "Mouse inalámbrico", 500.0);
        Producto producto3 = new Producto("3", "Teclado mecánico", 1200.0);

        // Crear items de carrito
        List<ItemCarrito> itemsPedido1 = Arrays.asList(
                new ItemCarrito(producto1, 1),
                new ItemCarrito(producto2, 2)
        );

        List<ItemCarrito> itemsPedido2 = Arrays.asList(
                new ItemCarrito(producto2, 3),
                new ItemCarrito(producto3, 1)
        );

        List<ItemCarrito> itemsPedido3 = Arrays.asList(
                new ItemCarrito(producto1, 2)
        );

        // Crear pedidos de prueba SIN FECHA
        pedidos = Arrays.asList(
                new Pedido("PED-001", itemsPedido1, 16000.0, "Entregado", "Calle Falsa 123", "cliente1@test.com"),
                new Pedido("PED-002", itemsPedido2, 2700.0, "Procesando", "Avenida Siempre Viva 456", "cliente2@test.com"),
                new Pedido("PED-003", itemsPedido3, 30000.0, "Entregado", "Boulevard Los Olivos 789", "cliente1@test.com"),
                new Pedido("PED-004", itemsPedido2, 2700.0, "Cancelado", "Calle Primavera 321", "cliente3@test.com")
        );
    }

    public void generarReportes() {
        this.totalVentas = pedidos.stream()
                .filter(p -> "Entregado".equals(p.getEstado()))
                .mapToDouble(Pedido::getTotal)
                .sum();

        this.productosMasVendidos = pedidos.stream()
                .filter(p -> "Entregado".equals(p.getEstado()))
                .flatMap(p -> p.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProducto().getNombre(),
                        Collectors.summingLong(ItemCarrito::getCantidad)
                ));

        this.ventasPorEstado = pedidos.stream()
                .collect(Collectors.groupingBy(
                        Pedido::getEstado,
                        Collectors.counting()
                ));
    }

    public void filtrarPedidos() {
        if (estadoFiltro == null || estadoFiltro.isEmpty()) {
            if (usarDatosPrueba) {
                cargarDatosDePrueba();
            } else {
                consultarPedidos();
            }
        } else {
            pedidos = pedidos.stream()
                    .filter(p -> p.getEstado().equalsIgnoreCase(estadoFiltro))
                    .collect(Collectors.toList());
        }
        generarReportes();
    }

    public String redirigirADevoluciones(String idPedido) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("pedidoSeleccionado", idPedido);
        return "Devoluciones?faces-redirect=true";
    }
    
    public void agregarDevolucion(Pedido pedido, String motivo, String estatus) {
        Devolucion nuevaDevolucion = new Devolucion(
            devoluciones.size() + 1, // ID autoincremental
            new Compra(pedido), // Asumo que Compra puede crearse a partir de Pedido
            motivo,
            estatus,
            "Devolución creada"
        );
        
        devoluciones.add(nuevaDevolucion);
        guardarDevolucionEnBaseDatos(nuevaDevolucion);
    }

    // Método para guardar en base de datos
    private void guardarDevolucionEnBaseDatos(Devolucion devolucion) {
        String endpoint = direccionIp + "/DatabaseService/api/service/devoluciones";
        Client client = ClientBuilder.newClient();
        
        try {
            Jsonb jsonb = JsonbBuilder.create();
            String jsonDevolucion = jsonb.toJson(devolucion);
            
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.json(jsonDevolucion));

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error al guardar devolución: " + response.getStatus(), null));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error de conexión: " + e.getMessage(), null));
        } finally {
            client.close();
        }
    }

    // Método para cargar devoluciones desde la base de datos
    public void cargarDevoluciones() {
        String endpoint = direccionIp + "/DatabaseService/api/service/devoluciones";
        Client client = ClientBuilder.newClient();

        try {
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonResponse = response.readEntity(String.class);
                Jsonb jsonb = JsonbBuilder.create();
                Devolucion[] devolucionesArray = jsonb.fromJson(jsonResponse, Devolucion[].class);
                devoluciones = Arrays.asList(devolucionesArray);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error al cargar devoluciones: " + e.getMessage(), null));
        } finally {
            client.close();
        }
    }
    
    // Getters y Setters adicionales
    public List<Devolucion> getDevoluciones() {
        return devoluciones;
    }

    public Devolucion getDevolucionActual() {
        return devolucionActual;
    }

    public void setDevolucionActual(Devolucion devolucionActual) {
        this.devolucionActual = devolucionActual;
    }

    // Getters y Setters
    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }
    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }
    public String getEstadoFiltro() { return estadoFiltro; }
    public void setEstadoFiltro(String estadoFiltro) { this.estadoFiltro = estadoFiltro; }
    public double getTotalVentas() { return totalVentas; }
    public Map<String, Long> getProductosMasVendidos() { return productosMasVendidos; }
    public Map<String, Long> getVentasPorEstado() { return ventasPorEstado; }
    public Map<String, Double> getVentasPorMes() { return ventasPorMes; }
    public String getDireccionIp() { return direccionIp; }
    public void setDireccionIp(String direccionIp) { this.direccionIp = direccionIp; }
    public String getColeccionPedidos() { return coleccionPedidos; }
    public void setColeccionPedidos(String coleccionPedidos) { this.coleccionPedidos = coleccionPedidos; }
    public boolean isUsarDatosPrueba() { return usarDatosPrueba; }
    public void setUsarDatosPrueba(boolean usarDatosPrueba) { this.usarDatosPrueba = usarDatosPrueba; }
}