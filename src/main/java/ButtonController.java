/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


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

/**
 *
 * @author bet10
 */
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

    public String login() {
        if ("admin@admin.com".equals(user.getEmail()) && "password".equals(user.getPassword())) {
            return "Product.xhtml"; // Redirige a la página home si el login es correcto
        } else {
            return "login.xhtml?faces-redirect=true"; // Redirige al login si es incorrecto
        }
    }
    
    public void consultar() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", user.getEmail(), user.getPassword());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://10.128.1.68:8080/ValidacionUsuarios/api/login_crm"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Respuesta exitosa
                System.out.println(response.body());
                System.out.println(response.statusCode());
                System.out.println("Conexion exitosa");
                FacesContext.getCurrentInstance().getExternalContext().redirect("Product.xhtml");
                

                // Guardar información del usuario en la sesión
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute("userEmail", user.getEmail());

            } else {
                System.out.println(response.statusCode());
                System.out.println("No existe el correo");
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error General: " + e);
        }
    }

    public void cerrarSesion() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate(); // Invalida la sesión
        }
    }

    public void registrarUsuario() {

        // Validar que las contraseñas coincidan
        if (!user.getPassword().equals(user.getPasswordConfirm())) {
            System.out.println("Las contraseñas no coinciden");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Las contraseñas no coinciden.", null));
            return; // Salir del método si las contraseñas no coinciden
        }

        String calle = "Avenida las Torres";
        int numero = 1;
        String colonia = "centro";
        String estado = "México";
        String fechaNacimiento = "1999-05-15T00:00:00Z";

        System.out.println("Datos del usuario: " + user.getNombre() + " " + user.getApellidoPaterno() + " " + user.getApellidoMaterno()
                + " " + user.getEmail() + " " + user.getPassword() + " " + user.getTelefono() + " " + calle + " " + numero + " " + colonia + " " + user.getCiudad()
                + " " + estado + " " + user.getCodigoPostal() + " " + fechaNacimiento + " " + user.getNumCuenta());

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
                    + "\"calle\": \"%s\","
                    + "\"numero\": %d,"
                    + "\"colonia\": \"%s\","
                    + "\"ciudad\": \"%s\","
                    + "\"estado\": \"%s\","
                    + "\"cp\": \"%s\","
                    + "\"fechaNacimiento\": \"%s\","
                    + "\"noCuenta\": \"%s\""
                    + "}",
                    user.getNombre(), user.getApellidoPaterno(), user.getApellidoMaterno(), user.getEmail(), user.getPassword(),
                    user.getTelefono(), calle, numero, colonia, user.getCiudad(), estado, user.getCodigoPostal(),
                    fechaNacimiento, user.getNumCuenta()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://10.128.1.68:8080/ValidacionUsuarios/api/validar_crm"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Respuesta exitosa
                System.out.println("Usuario registrado con éxito");
                System.out.println("Respuesta: " + response.body());
                FacesContext.getCurrentInstance().getExternalContext().redirect("Home.xhtml");

            } else {
                // Error en la respuesta
                System.out.println("Error al registrar el usuario: " + response.statusCode());
                System.out.println("Detalle: " + response.body());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hubo un error al registrar al usuario", null));

            }
        } catch (IOException | InterruptedException e) {
            // Error durante la ejecución
            System.out.println("Error: " + e.getMessage());
        }
    }


}
