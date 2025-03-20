import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Named
@RequestScoped
public class ProductoController {

    private List<Producto> productos = new ArrayList<>();

    public List<Producto> getProductos() {
        if (productos.isEmpty()) {
            cargarProductos();
        }
        return productos;
    }

    private void cargarProductos() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/ApiCRM/api/productos")) // Reemplaza con la URL de tu API de productos
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                productos = gson.fromJson(response.body(), new TypeToken<List<Producto>>() {}.getType());
            } else {
                System.out.println("Error al cargar productos: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}