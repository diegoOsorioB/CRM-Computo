import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class ListaDeseosBean implements Serializable {

    private List<Producto> listaDeseos;

    public ListaDeseosBean() {
        listaDeseos = new ArrayList<>();
        listaDeseos.add(new Producto(1,"Camisa Blanca", "Camisa de algodón 100% blanca", "camisa.jpg", 500.00));
        listaDeseos.add(new Producto(2,"Zapatos Blancos", "Zapatos deportivos blancos para correr", "zapatos_blancos.jpg", 1200.00));
        listaDeseos.add(new Producto(3,"Laptop Blanca", "Laptop ultradelgada con carcasa blanca", "laptop_blanca.jpg", 18000.00));
        listaDeseos.add(new Producto(4,"Refrigerador Blanco", "Refrigerador de 400L color blanco", "refrigerador_blanco.jpg", 33500.00));
        listaDeseos.add(new Producto(5,"Taza Blanca", "Taza de cerámica blanca con diseño minimalista", "taza_blanca.jpg", 120.00));
    }

    public List<Producto> getListaDeseos() {
        return new ArrayList<>(listaDeseos); // Devuelve una copia para evitar modificaciones externas
    }

    // Aumentar la cantidad de un producto
    /*
    public void aumentarCantidad(Producto producto) {
        for (Producto p : listaDeseos) {
            if (p.equals(producto)) {
                p.setCantidad(p.getCantidad() + 1);
                break;
            }
        }
    }*/

    // Disminuir la cantidad de un producto (mínimo 1)
    /*
    public void disminuirCantidad(Producto producto) {
        for (Producto p : listaDeseos) {
            if (p.equals(producto) && p.getCantidad() > 1) {
                p.setCantidad(p.getCantidad() - 1);
                break;
            }
        }
    }*/

    // Eliminar un producto de la lista de deseos
    public void eliminarDeListaDeseos(Producto producto) {
        listaDeseos.removeIf(p -> p.equals(producto));
    }
}