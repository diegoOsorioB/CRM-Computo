
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;


@Named
@RequestScoped
public class ButtonController {

    // private String ip = "https://6a90-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app";
    private User user = new User();

    public User getUser() {
        return user;
    }

    APISController api = new APISController();

    public void setUser(User user) {
        this.user = user;
    }

    // Método para consultar el login del usuario
    public void consultar() {
        try {
            System.out.println(user.getId() + " NO ID");
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"correo\":\"%s\",\"password\":\"%s\"}", user.getEmail(), user.getPassword());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(api.getURLLOGIN()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Cuerpo de la respuesta: " + response.body());
            System.out.println("Código de estado: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("Conexión exitosa");
                consultarP();
                obtenerDatosColeccion(user.getEmail());
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

                // Convertir la respuesta en JSON
                //System.out.println("Usuario: " + user.getEmail() + " | Rol: " + user.getRol() + " | Token: " + token);
                // Redireccionar a la página de productos
                JSONObject jsonResponse = new JSONObject(response.body());

                String token = jsonResponse.getString("token");
                

                session.setAttribute("authToken", token);
                facesContext.getExternalContext().redirect("Product.xhtml");

            } else {
                System.out.println("Error en el login: " + response.body());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales inválidas", null));
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void consultarP() {
        try {
            System.out.println(user.getId() + " NO ID");
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"correo\":\"maximiliano@max.com\",\"password\":\"12345678\" }");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(api.getURLLOGIN()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Cuerpo de la respuesta: Admin  " + response.body());
            System.out.println("Código de estado: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("Conexión exitosa");

                // Convertir la respuesta en JSON
                JSONObject jsonResponse = new JSONObject(response.body());

                String token = jsonResponse.getString("token"); // Asegúrate de que el backend envíe el token
                System.out.println(response.body() + " Max");

                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

                // Guardar datos en la sesión
                session.setAttribute("authTokenA", token);

            } else {
                System.out.println("Error en el login: " + response.body());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales inválidas", null));
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Método para cerrar sesión
    public void cerrarSesion() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    // Método para registrar un nuevo usuario
    public void registrarUsuario() {
        consultarP();
        FacesContext context = FacesContext.getCurrentInstance();
        String token = (String) context.getExternalContext().getSessionMap().get("authTokenA");
        System.out.println("------" + token + "");

        if (!user.isAceptaTerminos()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe aceptar los términos y condiciones.", null));
            return;
        }

        // Validar que las contraseñas coincidan
        if (!user.getPassword().equals(user.getPasswordConfirm())) {
            System.out.println("Las contraseñas no coinciden");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Las contraseñas no coinciden.", null));
            return;
        }

        System.out.println("Datos del usuario: " + user.getNombre() + " " + user.getApellidoPaterno() + " " + user.getApellidoMaterno()
                + " " + user.getEmail() + " " + user.getPassword() + " " + user.getTelefono() + " " + user.getCiudad()
                + " " + user.getCodigoPostal() + " " + user.getNumCuenta());
        user.setRol("cliente");

        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format(
                    "[{"
                    + "\"nombre\": \"%s\","
                    + "\"apellidoPaterno\": \"%s\","
                    + "\"apellidoMaterno\": \"%s\","
                    + "\"correo\": \"%s\","
                    + "\"password\": \"%s\","
                    + "\"telefono\": \"%s\","
                    + "\"direccion\": \"%s\","
                    + "\"ciudad\": \"%s\","
                    + "\"baseDatos\": \"CRM\","
                    + "\"codigoPostal\": \"%s\","
                    + "\"numCuenta\": \"%s\","
                    + "\"roles\": [\"%s\"]" // Cambiado a array
                    + "}]",
                    user.getNombre(), user.getApellidoPaterno(), user.getApellidoMaterno(), user.getEmail(), user.getPassword(),
                    user.getTelefono(), user.getDireccion(), user.getCiudad(), user.getCodigoPostal(),
                    user.getNumCuenta(), user.getRol()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(api.getURLBD() + "/usuarios"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Usuario registrado con éxito");
                System.out.println("Respuesta: Admin " + response.body());
                FacesContext.getCurrentInstance().getExternalContext().redirect("Product.xhtml");
            } else {
                if (response.statusCode() == 409) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Correo ya registrado", null));
                }
                System.out.println("Error al registrar el usuario: " + response.statusCode());
                System.out.println("Detalle: " + response.body());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hubo un error al registrar al usuario", null));
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

  
// O si usas Jackson
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.core.JsonProcessingException;

public void registrarAdministrador() {
    user.setRol("ADMIN");
    String modulo = "CRM";

    try {
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = String.format(
                "{"
                + "\"nombre\": \"%s\","
                + "\"apellido\": \"%s\","
                + "\"rol\": \"%s\","
                + "\"modulo\": \"%s\""
                + "}",
                user.getNombre(), user.getApellidoPaterno(), user.getRol(), modulo
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(api.getURLRYU()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("Usuario registrado con éxito");
            System.out.println("Respuesta: " + response.body());

            // Convertir el cuerpo de la respuesta en un JSON
            JSONObject jsonResponse = new JSONObject(response.body());
            String correo = jsonResponse.optString("correo", "No disponible");
            String contrasena = jsonResponse.optString("password", "No disponible");

            // Guardar en una variable en buttonController
            user.setCorreo(correo);
            user.setContrasena(contrasena);

           // FacesContext.getCurrentInstance().getExternalContext().redirect("Product.xhtml");
        } else {
            if (response.statusCode() == 409) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Correo ya registrado", null));
            }
            System.out.println("Error al registrar el usuario: " + response.statusCode());
            System.out.println("Detalle: " + response.body());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hubo un error al registrar al usuario", null));
        }
    } catch (IOException | InterruptedException e) {
        System.out.println("Error: " + e.getMessage());
    }
}

    

    public void obtenerDatosColeccion(String correo) {
        FacesContext context = FacesContext.getCurrentInstance();
        String token = (String) context.getExternalContext().getSessionMap().get("authTokenA");
        System.out.println("###" + token + "");
        System.out.println(correo + ":**");

        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = api.getURLBD() + "/usuarios?correo=" + correo;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Cuerpo de la respuesta: " + response.body());
            System.out.println("Código de estado: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("Consulta exitosa");

                // Convertir la respuesta en un JSONArray
                JSONArray jsonResponse = new JSONArray(response.body());

                // Acceder al primer objeto dentro del arreglo JSON
                JSONObject userObject = jsonResponse.getJSONObject(0);

                String userId = userObject.getString("_id");
                JSONArray rolesArray = userObject.getJSONArray("roles");
                String userRole = rolesArray.getString(0); // Obtener el primer rol

                user.setId(userId);
                user.setRol(userRole);

                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

                // Guardar datos en la sesión
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userRole", user.getRol());
                session.setAttribute("authToken", token);

            } else {
                System.out.println("Error en la consulta: " + response.body());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en la consulta", null));
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
