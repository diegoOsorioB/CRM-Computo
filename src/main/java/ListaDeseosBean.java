import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("listaDeseosBean") // Agrega un nombre para referenciar en JSF
@SessionScoped
public class ListaDeseosBean implements Serializable {

    private List<Producto> listaDeseos;

    public ListaDeseosBean() {
        listaDeseos = new ArrayList<>();
    }

    public List<Producto> getListaDeseos() {
        return new ArrayList<>(listaDeseos); // Devuelve una copia para evitar modificaciones externas
    }

    public void agregarAListaDeseos(Producto producto) {
        if (producto != null && !listaDeseos.contains(producto)) {
            listaDeseos.add(producto);
            System.out.println("Producto agregado a la lista de deseos: " + producto.getNombre());
        }
    }

    public void eliminarDeListaDeseos(Producto producto) {
        listaDeseos.removeIf(p -> p.equals(producto));
    }
}