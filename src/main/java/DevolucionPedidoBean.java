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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class DevolucionPedidoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(DevolucionPedidoBean.class.getName());
    
    // Configuración de conexión
    private String direccionIp = "https://5b22-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app";
    private String coleccionPedidos = "pedidos";
    private String coleccionDevoluciones = "devoluciones";
    private final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMxOTU0NTgsImlhdCI6MTc0MzEwOTA1OH0.SY9bv8fRAOiLEzc2W5pO_HCjJxP3DgrZeMdht1A7Mhw";
    
    // Datos y estado
    private List<Pedido> pedidos = new ArrayList<>();
    private List<Pedido> pedidosFiltrados = new ArrayList<>();
    private String correoUsuario;
    private String estadoFiltro;
    private boolean usarDatosPrueba = false;
    private boolean errorConexion = false;

    // Datos para reportes
    private double totalVentas;
    private Map<String, Long> productosMasVendidos;
    private Map<String, Long> ventasPorEstado;
    private Map<String, Double> ventasPorMes;
    
    private List<Devolucion> devoluciones = new ArrayList<>();
    private Devolucion devolucionActual = new Devolucion();
    private Pedido pedidoSeleccionado;

    @PostConstruct
    public void init() {
        try {
            cargarPedidos();
            cargarDevoluciones();
            generarReportes();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error crítico en inicialización: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_FATAL, 
                "Error crítico al inicializar el componente", null));
            cargarDatosDePrueba();
        }
    }

    public void cargarPedidos() {
        if (usarDatosPrueba) {
            cargarDatosDePrueba();
        } else {
            consultarPedidosAPI();
        }
        pedidosFiltrados = new ArrayList<>(pedidos);
    }

    private void consultarPedidosAPI() {
        FacesContext context = FacesContext.getCurrentInstance();
        String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccionPedidos;
        Client client = null;

        try {
            client = ClientBuilder.newClient();
            WebTarget target = client.target(endpoint);
            
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .property("jersey.config.client.connectTimeout", 5000)
                    .property("jersey.config.client.readTimeout", 10000)
                    .get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonResponse = response.readEntity(String.class);
                try {
                    Jsonb jsonb = JsonbBuilder.create();
                    Pedido[] pedidosArray = jsonb.fromJson(jsonResponse, Pedido[].class);
                    pedidos = Arrays.asList(pedidosArray);
                    errorConexion = false;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error al parsear JSON: " + e.getMessage(), e);
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al procesar los datos recibidos", null));
                    cargarDatosDePrueba();
                }
            } else {
                manejarErrorAPI(response);
            }
        } catch (jakarta.ws.rs.ProcessingException e) {
            manejarErrorConexion(e);
        } catch (Exception e) {
            manejarErrorInesperado(e);
        } finally {
            cerrarCliente(client);
        }
    }

    private void cargarDatosDePrueba() {
        try {
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
            
            // Crear devoluciones de prueba
            Compra compra1 = new Compra(pedidos.get(0));
            Compra compra3 = new Compra(pedidos.get(2));
            
            devoluciones = Arrays.asList(
                new Devolucion(1, compra1, "Producto dañado", "Pendiente", "Iniciado por el cliente"),
                new Devolucion(2, compra3, "No era lo que esperaba", "Aprobado", "Aprobado por el departamento de garantías")
            );
            
            logger.info("Datos de prueba cargados exitosamente");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar datos de prueba: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error crítico: No se pudieron cargar los datos", null));
            pedidos = new ArrayList<>();
            devoluciones = new ArrayList<>();
        }
    }

    public void generarReportes() {
        try {
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
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al generar reportes: " + e.getMessage(), e);
            this.totalVentas = 0;
            this.productosMasVendidos = Map.of();
            this.ventasPorEstado = Map.of();
        }
    }

    public void filtrarPedidos() {
        try {
            if (estadoFiltro == null || estadoFiltro.isEmpty()) {
                pedidosFiltrados = new ArrayList<>(pedidos);
            } else {
                pedidosFiltrados = pedidos.stream()
                        .filter(p -> p.getEstado().equalsIgnoreCase(estadoFiltro))
                        .collect(Collectors.toList());
            }
            generarReportes();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al filtrar pedidos: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error al aplicar el filtro", null));
        }
    }

    public String redirigirADevoluciones(String idPedido) {
        try {
            pedidoSeleccionado = pedidos.stream()
                    .filter(p -> p.getId().equals(idPedido))
                    .findFirst()
                    .orElse(null);
            
            if (pedidoSeleccionado == null) {
                throw new IllegalArgumentException("Pedido no encontrado");
            }
            
            return "devoluciones?faces-redirect=true&includeViewParams=true";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al redirigir a devoluciones: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error al redirigir", null));
            return null;
        }
    }
    
    public void agregarDevolucion() {
        try {
            if (pedidoSeleccionado == null) {
                throw new IllegalArgumentException("No hay pedido seleccionado");
            }
            
            if (devolucionActual == null || devolucionActual.getMotivo() == null || devolucionActual.getMotivo().isEmpty()) {
                throw new IllegalArgumentException("Motivo de devolución requerido");
            }
            
            Devolucion nuevaDevolucion = new Devolucion(
                generarNuevoIdDevolucion(),
                new Compra(pedidoSeleccionado),
                devolucionActual.getMotivo(),
                "Pendiente",
                "Devolución creada"
            );
            
            devoluciones.add(nuevaDevolucion);
            guardarDevolucionEnBaseDatos(nuevaDevolucion);
            
            // Actualizar estado del pedido
            actualizarEstadoPedido(pedidoSeleccionado.getId(), "En devolución");
            
            // Limpiar formulario
            devolucionActual = new Devolucion();
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Devolución registrada correctamente", null));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al agregar devolución: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error al crear la devolución: " + e.getMessage(), null));
        }
    }

    private int generarNuevoIdDevolucion() {
        if (devoluciones == null || devoluciones.isEmpty()) {
            return 1;
        }
        return devoluciones.stream()
                .mapToInt(Devolucion::getIdDevolucion)
                .max()
                .orElse(0) + 1;
    }

    private void guardarDevolucionEnBaseDatos(Devolucion devolucion) {
        if (devolucion == null) {
            logger.warning("Intento de guardar devolución nula");
            return;
        }
        
        String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccionDevoluciones;
        Client client = null;
        
        try {
            Jsonb jsonb = JsonbBuilder.create();
            String jsonDevolucion = jsonb.toJson(devolucion);
            
            client = ClientBuilder.newClient();
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.json(jsonDevolucion));

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                manejarErrorAPI(response);
            } else {
                logger.info("Devolución guardada exitosamente en la base de datos");
            }
        } catch (jakarta.ws.rs.ProcessingException e) {
            manejarErrorConexion(e);
        } catch (Exception e) {
            manejarErrorInesperado(e);
        } finally {
            cerrarCliente(client);
        }
    }

    public void actualizarEstadoPedido(String idPedido, String nuevoEstado) {
        if (idPedido == null || nuevoEstado == null) {
            logger.warning("Intento de actualizar pedido con datos nulos");
            return;
        }
        
        String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccionPedidos + "/" + idPedido;
        Client client = null;
        
        try {
            String jsonUpdate = "{\"estado\":\"" + nuevoEstado + "\"}";
            
            client = ClientBuilder.newClient();
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .put(Entity.json(jsonUpdate));

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                manejarErrorAPI(response);
            } else {
                logger.info("Estado de pedido actualizado exitosamente");
                pedidos.stream()
                    .filter(p -> p.getId().equals(idPedido))
                    .findFirst()
                    .ifPresent(p -> p.setEstado(nuevoEstado));
            }
        } catch (jakarta.ws.rs.ProcessingException e) {
            manejarErrorConexion(e);
        } catch (Exception e) {
            manejarErrorInesperado(e);
        } finally {
            cerrarCliente(client);
        }
    }

    public void cargarDevoluciones() {
        if (errorConexion && !usarDatosPrueba) {
            logger.info("No se cargan devoluciones por error previo de conexión");
            return;
        }
        
        if (usarDatosPrueba) {
            // Los datos de prueba ya se cargaron en cargarDatosDePrueba()
            return;
        }
        
        String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccionDevoluciones;
        Client client = null;

        try {
            client = ClientBuilder.newClient();
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonResponse = response.readEntity(String.class);
                Jsonb jsonb = JsonbBuilder.create();
                Devolucion[] devolucionesArray = jsonb.fromJson(jsonResponse, Devolucion[].class);
                devoluciones = Arrays.asList(devolucionesArray);
            } else {
                manejarErrorAPI(response);
            }
        } catch (jakarta.ws.rs.ProcessingException e) {
            manejarErrorConexion(e);
        } catch (Exception e) {
            manejarErrorInesperado(e);
        } finally {
            cerrarCliente(client);
        }
    }

    public void actualizarEstadoDevolucion(int idDevolucion, String nuevoEstado, String razonCambio) {
        if (nuevoEstado == null) {
            logger.warning("Intento de actualizar devolución con estado nulo");
            return;
        }
        
        if (usarDatosPrueba) {
            // Actualización local para datos de prueba
            devoluciones.stream()
                .filter(d -> d.getIdDevolucion() == idDevolucion)
                .findFirst()
                .ifPresent(d -> {
                    d.setEstatus(nuevoEstado);
                    d.setRazonCambioEstatus(razonCambio);
                });
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Devolución actualizada correctamente (modo prueba)", null));
            return;
        }
        
        String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccionDevoluciones + "/" + idDevolucion;
        Client client = null;
        
        try {
            String jsonUpdate = "{\"estatus\":\"" + nuevoEstado + "\",\"razonCambioEstatus\":\"" + (razonCambio != null ? razonCambio : "") + "\"}";
            
            client = ClientBuilder.newClient();
            WebTarget target = client.target(endpoint);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .put(Entity.json(jsonUpdate));

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                manejarErrorAPI(response);
            } else {
                logger.info("Estado de devolución actualizado exitosamente");
                devoluciones.stream()
                    .filter(d -> d.getIdDevolucion() == idDevolucion)
                    .findFirst()
                    .ifPresent(d -> {
                        d.setEstatus(nuevoEstado);
                        d.setRazonCambioEstatus(razonCambio);
                    });
                
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Devolución actualizada correctamente", null));
            }
        } catch (jakarta.ws.rs.ProcessingException e) {
            manejarErrorConexion(e);
        } catch (Exception e) {
            manejarErrorInesperado(e);
        } finally {
            cerrarCliente(client);
        }
    }

    private void manejarErrorAPI(Response response) {
        String errorMsg = "Error en el servicio: " + response.getStatus();
        try {
            errorMsg += " - " + response.readEntity(String.class);
        } catch (Exception e) {
            errorMsg += " (No se pudo obtener mensaje de error)";
        }
        logger.log(Level.WARNING, errorMsg);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, null));
        
        if (usarDatosPrueba) {
            cargarDatosDePrueba();
        }
        errorConexion = true;
    }

    private void manejarErrorConexion(Exception e) {
        logger.log(Level.SEVERE, "Error de conexión o timeout: " + e.getMessage(), e);
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Error de conexión con el servidor. Verifique su conexión a internet o intente más tarde.", null));
        if (usarDatosPrueba) {
            cargarDatosDePrueba();
        }
        errorConexion = true;
    }

    private void manejarErrorInesperado(Exception e) {
        logger.log(Level.SEVERE, "Error inesperado: " + e.getMessage(), e);
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Error inesperado: " + e.getMessage(), null));
        if (usarDatosPrueba) {
            cargarDatosDePrueba();
        }
        errorConexion = true;
    }

    private void cerrarCliente(Client client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error al cerrar el cliente HTTP: " + e.getMessage(), e);
            }
        }
    }

    // Getters y Setters
    public boolean isErrorConexion() {
        return errorConexion;
    }

    public List<Pedido> getPedidos() { 
        return pedidosFiltrados != null ? pedidosFiltrados : pedidos;
    }
    
    public void setPedidos(List<Pedido> pedidos) { 
        this.pedidos = pedidos;
        this.pedidosFiltrados = new ArrayList<>(pedidos);
    }
    
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
    public List<Devolucion> getDevoluciones() { return devoluciones; }
    public void setDevoluciones(List<Devolucion> devoluciones) { this.devoluciones = devoluciones; }
    public Devolucion getDevolucionActual() { return devolucionActual; }
    public void setDevolucionActual(Devolucion devolucionActual) { this.devolucionActual = devolucionActual; }
    public Pedido getPedidoSeleccionado() { return pedidoSeleccionado; }
    public void setPedidoSeleccionado(Pedido pedidoSeleccionado) { this.pedidoSeleccionado = pedidoSeleccionado; }
}