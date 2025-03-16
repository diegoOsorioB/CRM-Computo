

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Path("erp/pedido")
public class PedidoResource {

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerEstatusPedido(@Context HttpHeaders headers) {
        // Simulación de una dirección obtenida desde la sesión (normalmente vendría en headers o un token)
        String direccionCliente = headers.getHeaderString("direccionCliente");
        if (direccionCliente == null) {
            direccionCliente = "Dirección no disponible";
        }

        // Simulación de estados de pedido
        String[] estados = {"Pendiente", "En preparación", "Listo para enviar", "En camino", "Entregado"};
        Random random = new Random();
        String estatusPedido = estados[random.nextInt(estados.length)];

        // Construcción de respuesta JSON
        Map<String, String> response = new HashMap<>();
        response.put("direccionCliente", direccionCliente);
        response.put("estatusPedido", estatusPedido);

        return Response.ok(response).build();
    }
}
