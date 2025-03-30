public class ItemCarrito {

    private Producto producto;
    private int cantidad;
    private int stockTemporal; // Nuevo atributo

    public ItemCarrito() {
    }
    
    public ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        
        this.stockTemporal = producto.getStock(); // Copia el stock original
    }

    // Getters y setters
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    
    public int getStockTemporal() {
        return stockTemporal;
    }

    public void setStockTemporal(int stockTemporal) {
        this.stockTemporal = stockTemporal;
    }
}
