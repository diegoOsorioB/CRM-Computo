import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Named
@ViewScoped
public class DevolucionBean implements Serializable {
    private List<Devolucion> listaDevoluciones;
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
        cargarDevoluciones();
    }
    
    public String getToken() {
        return (String) session.getAttribute("authToken");
    }

    private void cargarDevoluciones() {
        listaDevoluciones = new ArrayList<>();
        try {
            String apiUrl = urlDB.getURLBD() + "/devoluciones"; // Usar APISController
            System.out.println("URL de la API: " + apiUrl);
            System.out.println("Token: " + token);

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();
            System.out.println("📡 Código de respuesta de la API: " + responseCode);

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                System.out.println("📦 Respuesta completa de la API:");
                System.out.println(response.toString());

                JsonReader jsonReader = Json.createReader(new java.io.StringReader(response.toString()));
                JsonArray jsonArray = jsonReader.readArray();
                jsonReader.close();

                for (JsonValue jsonValue : jsonArray) {
                    JsonObject jsonDevolucion = jsonValue.asJsonObject();
                    Devolucion devolucion = new Devolucion();

                    // Campos de la devolución
                    devolucion.setIdDevolucion(jsonDevolucion.getString("_id"));
                    devolucion.setMotivo(jsonDevolucion.getString("motivo", "No especificado"));
                    devolucion.setEstatus(jsonDevolucion.getString("estatus"));
                    devolucion.setEstatusInicial(jsonDevolucion.getString("estatusInicial", devolucion.getEstatus()));

                    // Objeto Pedido anidado
                    if (jsonDevolucion.containsKey("pedido")) {
                        JsonObject jsonPedido = jsonDevolucion.getJsonObject("pedido");
                        Pedido pedido = new Pedido();
                        pedido.setId(jsonPedido.getString("_id"));
                        pedido.setTotal(jsonPedido.getJsonNumber("total").doubleValue());
                        pedido.setEstado(jsonPedido.getString("estado"));
                        pedido.setEstadoInicial(jsonPedido.getString("estado"));

                        if (jsonPedido.containsKey("fecha")) {
                            pedido.setFecha(LocalDate.parse(jsonPedido.getString("fecha")));
                        }
                        pedido.setDireccion(jsonPedido.getString("direccion"));
                        pedido.setCorreoUsuario(jsonPedido.getString("correoUsuario"));

                        devolucion.setPedido(pedido);
                    }

                    listaDevoluciones.add(devolucion);
                }

                connection.disconnect();
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.out.println("❌ Error: " + connection.getResponseMessage());
                System.out.println("Detalles del error: " + errorResponse.toString());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error al cargar devoluciones: " + errorResponse.toString(), ""));
            }

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al conectar con la API: " + e.getMessage(), ""));
        }
    }

    public void actualizarEstatus(Devolucion devolucion) {    
        FacesContext context = FacesContext.getCurrentInstance();

        if (devolucion.getRazonCambioEstatus() == null || devolucion.getRazonCambioEstatus().trim().isEmpty()) {
            context.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Debe ingresar una razón para actualizar el pedido " + devolucion.getPedido().getId(), ""));
            return;
        } else if (devolucion.getEstatus().equals(devolucion.getEstatusInicial())) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "No se realizó ningún cambio. El estatus es el mismo", ""));
            return;
        } else {
            try {
                // Configurar la URL y la conexión
                String apiUrl = urlDB.getURLBD() + "/devoluciones/" + devolucion.getIdDevolucion();
                System.out.println("URL para actualización: " + apiUrl);
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setDoOutput(true);

                // Crear el cuerpo JSON con los datos actualizados
                String jsonBody = "{\"estatus\": \"" + devolucion.getEstatus() + "\", " +
                                 "\"razonCambioEstatus\": \"" + devolucion.getRazonCambioEstatus() + "\"}";
                System.out.println("JSON enviado: " + jsonBody);

                // Enviar el cuerpo JSON
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }

                // Obtener la respuesta de la API
                int responseCode = connection.getResponseCode();
                System.out.println("📡 Código de respuesta de la API: " + responseCode);

                if (responseCode == 200 || responseCode == 204) { // 200 OK o 204 No Content son comunes para PUT exitoso
                    context.addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                    "Estado actualizado: El pedido #" + devolucion.getPedido().getId() + 
                                    " ahora está en estado: " + devolucion.getEstatus(), ""));
                    System.out.println("Devolución #" + devolucion.getIdDevolucion() + " actualizada a " + devolucion.getEstatus());

                    // Enviar correo de notificación
                    emailService.enviarCorreoDevolucion(
                            devolucion.getPedido().getCorreoUsuario(),
                            "Cliente",
                            devolucion.getIdDevolucion(),
                            devolucion.getEstatus(),
                            devolucion.getRazonCambioEstatus()
                    );
                    context.addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Correo enviado correctamente", ""));

                    // Recargar la lista de devoluciones para reflejar los cambios
                    cargarDevoluciones();
                } else {
                    // Leer el mensaje de error si la actualización falla
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    System.out.println("❌ Error al actualizar: " + connection.getResponseMessage());
                    System.out.println("Detalles del error: " + errorResponse.toString());
                    context.addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                    "Error al actualizar la devolución: " + errorResponse.toString(), ""));
                }

                connection.disconnect();

                // Configurar mensajes para que no persistan al refrescar
                context.getExternalContext().getFlash().setKeepMessages(false);

            } catch (Exception e) {
                context.addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "No se pudo actualizar el estado del pedido o enviar el correo: " + e.getMessage(), ""));
                e.printStackTrace();
            }
        }
    }

    public List<Devolucion> getListaDevoluciones() {
        return listaDevoluciones;
    }
}