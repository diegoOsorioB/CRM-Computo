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
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Named
@SessionScoped
public class PedidoBean implements Serializable {

    private List<Pedido> listaPedidos;
    private Pedido pedidoSeleccionado;            
    String token;

    @Inject
    private HttpSession session;
    @Inject
    private EmailService emailService;    
    @Inject
    private APISController urlDB;  

    @PostConstruct
    public void init() {
        token = getToken();
        if (token == null) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            "Token no encontrado. Por favor, inicie sesión nuevamente.", ""));
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        cargarPedidosDesdeAPI();
    }
    
    public String getToken() {
        return (String) session.getAttribute("authToken");
    }
    

    private void cargarPedidosDesdeAPI() {
        listaPedidos = new ArrayList<>();
       
        try {
            System.out.println("Token"+token);
            URL url = new URL(urlDB.getURLBD()+"/pedidos");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();
            System.out.println("📡 Código de respuesta de la API: " + responseCode);

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Leer la respuesta completa
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Mostrar la respuesta JSON completa
                System.out.println("📦 Respuesta completa de la API:");
                System.out.println(response.toString());

                // Convertir la respuesta a JSON
                JsonReader jsonReader = Json.createReader(new java.io.StringReader(response.toString()));
                JsonArray jsonArray = jsonReader.readArray();

                for (JsonValue jsonValue : jsonArray) {
                    JsonObject jsonPedido = jsonValue.asJsonObject();
                    Pedido pedido = new Pedido();
                    pedido.setId(jsonPedido.getString("_id"));
                    pedido.setTotal(jsonPedido.getJsonNumber("total").doubleValue());
                    pedido.setEstado(jsonPedido.getString("estado"));

                    // ✅ Asignar el estado inicial al cargar el pedido
                    pedido.setEstadoInicial(jsonPedido.getString("estado"));

                    if (jsonPedido.containsKey("fecha")) {
                        pedido.setFecha(LocalDate.parse(jsonPedido.getString("fecha")));
                    }

                    pedido.setDireccion(jsonPedido.getString("direccion"));
                    pedido.setCorreoUsuario(jsonPedido.getString("correoUsuario"));

                    listaPedidos.add(pedido);
                }

                reader.close();
                connection.disconnect();

            } else {
                System.out.println("❌ Error: " + connection.getResponseMessage());
                System.out.println("Código de respuesta: " + responseCode);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al obtener los pedidos. Código de respuesta: " + responseCode, ""));
                connection.disconnect();
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
        // Verificar si el estado ha cambiado antes de enviar la solicitud
        System.out.println("📌 Estado actual: " + pedido.getEstado());
        System.out.println("🔍 Estado inicial: " + pedido.getEstadoInicial());

        if (pedido.getEstado().equals(pedido.getEstadoInicial())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                    "No se realizó ningún cambio. El estado es el mismo.", ""));
            return; // No continuar si no hay cambios
        }                
        String urldb = urlDB.getURLBD()+"/pedidos/"+pedido.getId();
        try {
            URL url = new URL(urldb);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

            // Crear JSON con la nueva información
            JsonObject json = Json.createObjectBuilder()
                    .add("estado", pedido.getEstado())
                    .build();

            OutputStream os = connection.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            System.out.println("📡 Código de respuesta al actualizar: " + responseCode);

            if (responseCode == 200) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Estado actualizado: Pedido #" + pedido.getId() + " ahora está en estado: " + pedido.getEstado(), ""));

                // Enviar correo solo si el estado realmente cambió
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
                System.out.println("❌ Error al actualizar: " + connection.getResponseMessage());
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

    public List<Pedido> getListaPedidos() {
        return listaPedidos;
    }
}
