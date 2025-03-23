
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
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
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

@Named
@ViewScoped
public class PedidoB implements Serializable {

    private String direccionIp = "http://localhost";
    private String coleccion = "productos";
    private Pedido pedido = new Pedido();
    private List<Pedido> pedidos;

        public void insertarPedido() {
    System.out.println(pedido.getCorreoUsuario() + " r");
    System.out.println("Sii");

    // Obtener correo del usuario desde la sesión activa
    FacesContext context = FacesContext.getCurrentInstance();
    String correoUsuario = (String) context.getExternalContext().getSessionMap().get("userEmail");

    if (correoUsuario == null || correoUsuario.isEmpty()) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error: Usuario no autenticado"));
        return;
    }

    // Asignar el correo al pedido
    pedido.setCorreoUsuario(correoUsuario);

    String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccion;
    Client client = ClientBuilder.newClient();
    Jsonb jsonb = JsonbBuilder.create();

    // Convertir el pedido en JSON
    String json = jsonb.toJson(Collections.singletonList(pedido)); 
    System.out.println(json);
    Response response = client.target(endpoint)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(json, MediaType.APPLICATION_JSON));

    if (response.getStatus() == 200 || response.getStatus() == 201) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Pedido insertado con éxito"));
    } else {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error al insertar el pedido"));
    }
    response.close();
    client.close();
}


    public void consultarPedidos() {
        String endpoint = direccionIp + "/DatabaseService/api/service/" + coleccion;
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(endpoint);
        Response response = target.request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {
            String jsonResponse = response.readEntity(String.class);
            Jsonb jsonb = JsonbBuilder.create();
            pedidos = Arrays.asList(jsonb.fromJson(jsonResponse, Pedido[].class));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error al obtener los pedidos"));
        }
        response.close();
        client.close();
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
    
    public List<Pedido> getPedidos() {
        return pedidos;
    }
}
