import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@RequestScoped
public class ReporteController implements Serializable {

    private String tipoReporte;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<Pedido> pedidos;
     private String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWVnb0BnbWFpbC5jb20iLCJiYXNlRGF0b3MiOiJDUk0iLCJleHAiOjE3NDMxOTU0NTgsImlhdCI6MTc0MzEwOTA1OH0.SY9bv8fRAOiLEzc2W5pO_HCjJxP3DgrZeMdht1A7Mhw";


    public void generarVistaPrevia() {
        consultarPedidos(); // Obtener pedidos desde el backend

        Map<String, Object> datos = new HashMap<>();
        if ("totalVentas".equals(tipoReporte)) {
            double total = pedidos.stream().mapToDouble(Pedido::getTotal).sum();
            datos.put("labels", List.of("Total de Ventas"));
            datos.put("valores", List.of(total));
        } else if ("pedidosPorEstado".equals(tipoReporte)) {
            Map<String, Long> pedidosPorEstado = pedidos.stream()
                    .collect(Collectors.groupingBy(Pedido::getEstado, Collectors.counting()));
            datos.put("labels", pedidosPorEstado.keySet());
            datos.put("valores", pedidosPorEstado.values());
        } else if ("pedidosPorFecha".equals(tipoReporte)) {
            Map<String, Long> pedidosPorFecha = pedidos.stream()
                    .collect(Collectors.groupingBy(p -> p.getFecha().toString(), Collectors.counting()));
            datos.put("labels", pedidosPorFecha.keySet());
            datos.put("valores", pedidosPorFecha.values());
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Vista previa generada.", null));

        // Aquí podrías pasar los datos a una variable en JavaScript para actualizar el gráfico
    }

    public void generarReporte() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Reporte generado con éxito.", null));
    }

    private void consultarPedidos() {
        FacesContext context = FacesContext.getCurrentInstance();
        String correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

        // Verificar si el usuario está autenticado
        if (correoUsuario == null || correoUsuario.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: Usuario no autenticado", null));
            return;
        }

        // Construcción del endpoint con el filtro por correoUsuario
        String endpoint = "https://c0c6-2806-104e-16-1f1-12e1-6efa-4429-523f.ngrok-free.app/DatabaseService/api/service/pedidos";//?correoUsuario=" + correoUsuario;

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

    // Getters y Setters
    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
}
