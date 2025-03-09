

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("productoBean")
@RequestScoped
public class ProductoBean implements Serializable {
    private List<Producto> productos;
    private List<Producto> productosFiltrados;
    private Producto producto;
    private String criterioBusqueda;

    public ProductoBean() {
        productos = new ArrayList<>();
        productos.add(new Producto(1, "Camisa Blanca", "Camisa de algodón 100% blanca", "camisa_blanca.jpg", 500.00));
        productos.add(new Producto(2, "Zapatos Blancos", "Zapatos deportivos blancos para correr", "zapatos_blancos.jpg", 1200.00));
        productos.add(new Producto(3, "Laptop Blanca", "Laptop ultradelgada con carcasa blanca", "laptop_blanca.jpg", 18000.00));
        productos.add(new Producto(4, "Refrigerador Blanco", "Refrigerador de 400L color blanco", "refrigerador_blanco.jpg", 8500.00));
        productos.add(new Producto(5, "Taza Blanca", "Taza de cerámica blanca con diseño minimalista", "taza_blanca.jpg", 120.00));

        productosFiltrados = new ArrayList<>(productos);

        // Cargar el producto según el parámetro en la URL
        cargarProducto();
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<Producto> getProductosFiltrados() {
        return productosFiltrados;
    }

    public Producto getProducto() {
        return producto;
    }

    public String getCriterioBusqueda() {
        return criterioBusqueda;
    }

    public void setCriterioBusqueda(String criterioBusqueda) {
        this.criterioBusqueda = criterioBusqueda;
    }

    public void buscarProducto() {
        if (criterioBusqueda == null || criterioBusqueda.trim().isEmpty()) {
            productosFiltrados = new ArrayList<>(productos);
        } else {
            String criterio = criterioBusqueda.toLowerCase();
            productosFiltrados = productos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(criterio))
                .collect(Collectors.toList());
        }
    }

    public void cargarProducto() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        String idStr = params.get("id");

        if (idStr != null) {
            int id = Integer.parseInt(idStr);
            for (Producto p : productos) {
                if (p.getId() == id) {
                    producto = p;
                    break;
                }
            }
        }
    }
}
