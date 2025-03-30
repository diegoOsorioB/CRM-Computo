
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.io.OutputStream;
import java.time.LocalDate;

@Named
@SessionScoped
public class PedidoBean implements Serializable {

    private List<Pedido> listaPedidos;
    private Pedido pedidoSeleccionado;

    @PostConstruct
    public void init() {
        cargarPedidosDesdeAPI();
    }

    @Inject
    private EmailService emailService;

    private void cargarPedidosDesdeAPI() {
        listaPedidos = new ArrayList<>();
        String apiUrl = "http://localhost:8080/api/pedidos";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                JsonReader jsonReader = Json.createReader(reader);
                JsonArray jsonArray = jsonReader.readArray();

                for (JsonValue jsonValue : jsonArray) {
                    JsonObject jsonPedido = jsonValue.asJsonObject();
                    Pedido pedido = new Pedido();
                    pedido.setId(jsonPedido.getString("_id"));
                    pedido.setTotal(jsonPedido.getJsonNumber("total").doubleValue());
                    pedido.setEstado(jsonPedido.getString("estado"));
                    pedido.setFecha(LocalDate.parse(jsonPedido.getString("fecha")));
                    pedido.setDireccion(jsonPedido.getString("direccion"));
                    pedido.setCorreoUsuario(jsonPedido.getString("correoUsuario"));

                    listaPedidos.add(pedido);
                }
                reader.close();
                connection.disconnect();
            } else {
                System.out.println("Error al conectar con la API: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al conectar con la API", ""));
        }
    }

    public void seleccionarPedido(Pedido pedido) {
        this.pedidoSeleccionado = pedido;
        actualizarEstado(pedido);
    }

    public void actualizarEstado(Pedido pedido) {
        if (pedido.getEstado().equals(pedido.getEstadoInicial())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "No se realizó ningún cambio. El estatus es el mismo", ""));
        } else {
            String apiUrl = "http://localhost:8080/api/pedidos/" + pedido.getId();
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Crear JSON con la nueva información
                JsonObject json = Json.createObjectBuilder()
                        .add("estado", pedido.getEstado())
                        .build();

                OutputStream os = connection.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                if (connection.getResponseCode() == 200) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Estado actualizado: Pedido #" + pedido.getId() + " ahora está en estado: " + pedido.getEstado(), ""));

                    try {
                        emailService.enviarCorreoActualizacion(
                                pedido.getCorreoUsuario(),
                                "Cliente",
                                pedido.getId(),
                                pedido.getEstado());
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO, "Correo enviado correctamente", ""));
                    } catch (Exception e) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al enviar el correo", ""));
                        e.printStackTrace();
                    }

                    cargarPedidosDesdeAPI(); // Recargar pedidos después de la actualización
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al actualizar pedido", ""));
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al conectar con la API", ""));
            }
        }
    }

    public List<Pedido> getListaPedidos() {
        return listaPedidos;
    }

}
