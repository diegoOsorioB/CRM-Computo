
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author diego
 */

@Named
@RequestScoped
public class LoginData {


    private String username;
    private String password;

    // Getters y setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Método de login
    public String login() {
        if ("admin".equals(username) && "password".equals(password)) {
            return "home.xhtml"; // Redirige a la página home si el login es correcto
        } else {
            return "login.xhtml?faces-redirect=true"; // Redirige al login si es incorrecto
        }
    }
    
    public String register() {
        
            return "RegisterForm.xhtml"; // Redirige a la página home si el login es correcto
 
    }
}


