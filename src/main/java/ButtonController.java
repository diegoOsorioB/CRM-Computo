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
import org.json.JSONObject;

@Named
@RequestScoped
public class ButtonController {

    private User user = new User();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Método para consultar el login del usuario
    public void consultar() {
        try {
            System.out.println(user.getId() + " NO ID");
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", user.getEmail(), user.getPassword());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/ApiCRM/api/usuarios/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Cuerpo de la respuesta: " + response.body());
            System.out.println("Código de estado: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("Conexion exitosa");
                
                System.out.println(response.body());
                
                JSONObject jsonResponse = new JSONObject(response.body());
                user.setId(jsonResponse.getString("id"));
                String userRole = jsonResponse.getString("rol");

                // Aquí es donde deberías recibir el id del usuario después del login exitoso.
                // Supongamos que la respuesta contiene el ID y lo asignamos a `user.setId()`
                user.setId(response.body()); // Asigna el id obtenido del backend
                user.setRol(userRole);
                

                FacesContext.getCurrentInstance().getExternalContext().redirect("Product.xhtml");

                // Guardar información del usuario en la sesión
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userRole", user.getRol());
                System.out.println(user.getEmail()+"  Rol  "+user.getRol());

            } else {
                System.out.println("Error en el login: " + response.body());
                
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales Invalidas", null));
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
                 + " " + user.getCodigoPostal()  + " " + user.getNumCuenta());
        user.setRol("cliente");

        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format(
                    "{"
                    + "\"nombre\": \"%s\","
                    + "\"apellidoPaterno\": \"%s\","
                    + "\"apellidoMaterno\": \"%s\","
                    + "\"email\": \"%s\","
                    + "\"password\": \"%s\","
                    + "\"telefono\": \"%s\","
                    + "\"direccion\": \"%s\","
                    + "\"ciudad\": \"%s\","
                    + "\"codigoPostal\": \"%s\","
                    + "\"numCuenta\": \"%s\","
                    + "\"rol\": \"%s\""
                    + "}",
                    user.getNombre(), user.getApellidoPaterno(), user.getApellidoMaterno(), user.getEmail(), user.getPassword(),
                    user.getTelefono(), user.getDireccion(), user.getCiudad(), user.getCodigoPostal(),
                     user.getNumCuenta(),user.getRol()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/ApiCRM/api/usuarios/registrar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Usuario registrado con éxito");
                System.out.println("Respuesta: " + response.body());
                FacesContext.getCurrentInstance().getExternalContext().redirect("Product.xhtml");
            } else {
                if (response.statusCode()==409) {
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
    public void registrarAdministrador() {
        
        user.setRol("ADMIN");
        String modulo="CRM";

        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format(
                    "{"
                    + "\"nombre\": \"%s\","
                    + "\"apellidoPaterno\": \"%s\","
                    + "\"rol\": \"%s\","
                    + "\"modulo\": \"%s\""
                    + "}",
                    user.getNombre(), user.getApellidoPaterno(), user.getRol(),modulo
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://10.250.1.37:8080/ApiCRM/api/usuarios/registrar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Usuario registrado con éxito");
                System.out.println("Respuesta: " + response.body());
                FacesContext.getCurrentInstance().getExternalContext().redirect("Product.xhtml");
            } else {
                if (response.statusCode()==409) {
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
}
    