package com.tuempresa.proyectocrudg3.controller;

import com.tuempresa.proyectocrudg3.connection.DatabaseConnection;
import com.tuempresa.proyectocrudg3.model.Categoria;
import com.tuempresa.proyectocrudg3.model.Marca;
import com.tuempresa.proyectocrudg3.model.Producto;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;

public class ProductosController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private DatePicker dpFechaCreacion;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<Marca> cmbMarcaForm;

    @FXML private TextField txtFiltroBusqueda;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;
    @FXML private DatePicker dpFiltroDesde;
    @FXML private DatePicker dpFiltroHasta;
    @FXML private TextField txtFiltroPrecioMin;
    @FXML private TextField txtFiltroPrecioMax;
    @FXML private CheckBox chkSoloConStock;
    @FXML private ComboBox<String> cmbOrden;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, LocalDate> colFecha;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private final ObservableList<Marca> listaMarcas = FXCollections.observableArrayList();

    private Producto productoSeleccionado = null;

    @FXML
    public void initialize() {
        configurarTableView();
        configurarSeleccionTableView();
        cargarCategorias();
        cargarMarcas();
        cargarOpcionesOrden();
        cargarProductos();
    }



    private void configurarTableView() {
        colCodigo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCodigo())
        );
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombre())
        );
        colCategoria.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategoria())
        );
        colMarca.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMarca())
        );
        colPrecio.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getPrecio()).asObject()
        );
        colStock.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getStock()).asObject()
        );
        colFecha.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getFechaCreacion())
        );

        tblProductos.setItems(listaProductos);
    }

    private void configurarSeleccionTableView() {
        tblProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        productoSeleccionado = newValue;
                        cargarProductoEnFormulario(newValue);
                    }
                });
    }

    private void cargarProductoEnFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        dpFechaCreacion.setValue(p.getFechaCreacion());

        for (Categoria categoria : listaCategorias) {
            if (categoria.getIdCategoria() == p.getIdCategoria()) {
                cmbCategoria.setValue(categoria);
                break;
            }
        }

        for (Marca marca : listaMarcas) {
            if (marca.getIdMarca() == p.getIdMarca()) {
                cmbMarcaForm.setValue(marca);
                break;
            }
        }
    }

    private void cargarCategorias() {
        listaCategorias.clear();

        String sql = """
                SELECT
                    id_categorias AS id_categoria,
                    nombre
                FROM catalogos.categorias
                WHERE estado = 1
                ORDER BY nombre
                """;

        try (
                Connection conexion = DatabaseConnection.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Categoria categoria = new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre")
                );
                listaCategorias.add(categoria);
            }

            cmbCategoria.setItems(
                    FXCollections.observableArrayList(listaCategorias)
            );

            cmbFiltroCategoria.setItems(
                    FXCollections.observableArrayList(listaCategorias)
            );

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar las categorías.", e);
        }
    }

    private void cargarMarcas() {
        listaMarcas.clear();

        String sql = """
                SELECT
                    id_marca,
                    nombre
                FROM catalogos.marcas
                WHERE estado = 1
                ORDER BY nombre
                """;

        try (
                Connection conexion = DatabaseConnection.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Marca marca = new Marca(
                        rs.getInt("id_marca"),
                        rs.getString("nombre")
                );
                listaMarcas.add(marca);
            }

            cmbMarcaForm.setItems(
                    FXCollections.observableArrayList(listaMarcas)
            );

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar las marcas.", e);
        }
    }

    private void cargarOpcionesOrden() {
        cmbOrden.setItems(
                FXCollections.observableArrayList(
                        "Nombre",
                        "Precio menor a mayor",
                        "Precio mayor a menor",
                        "Fecha más reciente",
                        "Fecha más antigua",
                        "Stock mayor a menor",
                        "Stock menor a mayor"
                )
        );

        cmbOrden.setValue("Nombre");
    }

    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                rs.getDate("fecha_creacion").toLocalDate(),
                rs.getInt("id_categoria"),
                rs.getString("categoria"),
                rs.getInt("id_marca"),
                rs.getString("marca"),
                rs.getInt("estado")
        );
    }


    @FXML
    private void btnBuscarAction() {
        cargarProductos();
    }

    @FXML
    private void btnLimpiarFiltrosAction() {
        txtFiltroBusqueda.clear();
        cmbFiltroCategoria.setValue(null);
        dpFiltroDesde.setValue(null);
        dpFiltroHasta.setValue(null);
        txtFiltroPrecioMin.clear();
        txtFiltroPrecioMax.clear();
        chkSoloConStock.setSelected(false);
        cmbOrden.setValue("Nombre");
        cargarProductos();
    }

    private void cargarProductos() {
        listaProductos.clear();

        StringBuilder sql = new StringBuilder("""
                SELECT 
                    p.id_producto, p.codigo, p.nombre, 
                    COALESCE(p.descripcon, '') AS descripcion, 
                    p.precio, p.stock, p.fecha_creacion,
                    p.id_categoria, c.nombre AS categoria, 
                    p.id_marca, m.nombre AS marca, p.estado
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c ON p.id_categoria = c.id_categorias
                INNER JOIN catalogos.marcas m ON p.id_marca = m.id_marca
                WHERE p.estado = 1\s
                """);

        if (txtFiltroBusqueda.getText() != null && !txtFiltroBusqueda.getText().trim().isEmpty()) {
            sql.append("AND (p.codigo ILIKE ? OR p.nombre ILIKE ?) ");
        }
        if (cmbFiltroCategoria.getValue() != null) {
            sql.append("AND p.id_categoria = ? ");
        }
        if (dpFiltroDesde.getValue() != null) {
            sql.append("AND p.fecha_creacion >= ? ");
        }
        if (dpFiltroHasta.getValue() != null) {
            sql.append("AND p.fecha_creacion <= ? ");
        }
        if (txtFiltroPrecioMin.getText() != null && !txtFiltroPrecioMin.getText().trim().isEmpty()) {
            sql.append("AND p.precio >= ? ");
        }
        if (txtFiltroPrecioMax.getText() != null && !txtFiltroPrecioMax.getText().trim().isEmpty()) {
            sql.append("AND p.precio <= ? ");
        }
        if (chkSoloConStock.isSelected()) {
            sql.append("AND p.stock > 0 ");
        }

        String orden = cmbOrden.getValue();
        if (orden != null) {
            switch (orden) {
                case "Precio menor a mayor" -> sql.append("ORDER BY p.precio ASC");
                case "Precio mayor a menor" -> sql.append("ORDER BY p.precio DESC");
                case "Fecha más reciente" -> sql.append("ORDER BY p.fecha_creacion DESC");
                case "Fecha más antigua" -> sql.append("ORDER BY p.fecha_creacion ASC");
                case "Stock mayor a menor" -> sql.append("ORDER BY p.stock DESC");
                case "Stock menor a mayor" -> sql.append("ORDER BY p.stock ASC");
                default -> sql.append("ORDER BY p.nombre ASC");
            }
        } else {
            sql.append("ORDER BY p.nombre ASC");
        }

        try (
                Connection conexion = DatabaseConnection.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql.toString())
        ) {
            int index = 1;

            if (txtFiltroBusqueda.getText() != null && !txtFiltroBusqueda.getText().trim().isEmpty()) {
                String val = "%" + txtFiltroBusqueda.getText().trim() + "%";
                ps.setString(index++, val);
                ps.setString(index++, val);
            }
            if (cmbFiltroCategoria.getValue() != null) {
                ps.setInt(index++, cmbFiltroCategoria.getValue().getIdCategoria());
            }
            if (dpFiltroDesde.getValue() != null) {
                ps.setDate(index++, Date.valueOf(dpFiltroDesde.getValue()));
            }
            if (dpFiltroHasta.getValue() != null) {
                ps.setDate(index++, Date.valueOf(dpFiltroHasta.getValue()));
            }
            if (txtFiltroPrecioMin.getText() != null && !txtFiltroPrecioMin.getText().trim().isEmpty()) {
                ps.setDouble(index++, Double.parseDouble(txtFiltroPrecioMin.getText().trim()));
            }
            if (txtFiltroPrecioMax.getText() != null && !txtFiltroPrecioMax.getText().trim().isEmpty()) {
                ps.setDouble(index++, Double.parseDouble(txtFiltroPrecioMax.getText().trim()));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaProductos.add(crearProductoDesdeResultSet(rs));
            }

        } catch (SQLException e) {
            mostrarErrorBaseDatos("Error al consultar productos.", e);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "Ingrese números válidos para los precios.");
        }
    }


    @FXML
    private void btnNuevoAction() {
        limpiarFormulario();
    }

    @FXML
    private void btnGuardarAction() {
        if (!validarFormulario()) return;

        String sql = """
                INSERT INTO catalogos.productos 
                (codigo, nombre, descripcon, precio, stock, fecha_creacion, id_categoria, id_marca) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conexion = DatabaseConnection.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, txtCodigo.getText().trim());
            ps.setString(2, txtNombre.getText().trim());
            ps.setString(3, txtDescripcion.getText().trim());
            ps.setDouble(4, Double.parseDouble(txtPrecio.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));

            LocalDate fecha = dpFechaCreacion.getValue() != null ? dpFechaCreacion.getValue() : LocalDate.now();
            ps.setDate(6, Date.valueOf(fecha));

            ps.setInt(7, cmbCategoria.getValue().getIdCategoria());
            ps.setInt(8, cmbMarcaForm.getValue().getIdMarca());

            ps.executeUpdate();
            mostrarAlerta("Éxito", "Producto registrado correctamente.");

            limpiarFormulario();
            cargarProductos();

        } catch (SQLException e) {
            mostrarErrorBaseDatos("Error al guardar el producto.", e);
        }
    }

    @FXML
    private void btnActualizarAction() {
        if (productoSeleccionado == null) {
            mostrarAlerta("Advertencia", "Seleccione un producto de la tabla para actualizar.");
            return;
        }

        if (!validarFormulario()) return;

        String sql = """
                UPDATE catalogos.productos 
                SET codigo = ?, nombre = ?, descripcon = ?, precio = ?, stock = ?, fecha_creacion = ?, id_categoria = ?, id_marca = ? 
                WHERE id_producto = ?
                """;

        try (
                Connection conexion = DatabaseConnection.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, txtCodigo.getText().trim());
            ps.setString(2, txtNombre.getText().trim());
            ps.setString(3, txtDescripcion.getText().trim());
            ps.setDouble(4, Double.parseDouble(txtPrecio.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));

            LocalDate fecha = dpFechaCreacion.getValue() != null ? dpFechaCreacion.getValue() : productoSeleccionado.getFechaCreacion();
            ps.setDate(6, Date.valueOf(fecha));

            ps.setInt(7, cmbCategoria.getValue().getIdCategoria());
            ps.setInt(8, cmbMarcaForm.getValue().getIdMarca());
            ps.setInt(9, productoSeleccionado.getIdProducto());

            ps.executeUpdate();
            mostrarAlerta("Éxito", "Producto actualizado correctamente.");

            limpiarFormulario();
            cargarProductos();

        } catch (SQLException e) {
            mostrarErrorBaseDatos("Error al actualizar el producto.", e);
        }
    }

    @FXML
    private void btnEliminarAction() {
        if (productoSeleccionado == null) {
            mostrarAlerta("Advertencia", "Seleccione un producto de la tabla para eliminar.");
            return;
        }

        String sql = "UPDATE catalogos.productos SET estado = 0 WHERE id_producto = ?";

        try (
                Connection conexion = DatabaseConnection.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setInt(1, productoSeleccionado.getIdProducto());
            ps.executeUpdate();

            mostrarAlerta("Éxito", "Producto eliminado lógicamente.");

            limpiarFormulario();
            cargarProductos();

        } catch (SQLException e) {
            mostrarErrorBaseDatos("Error al eliminar el producto.", e);
        }
    }


    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();
        dpFechaCreacion.setValue(null);
        cmbCategoria.setValue(null);
        cmbMarcaForm.setValue(null);
        productoSeleccionado = null;
        tblProductos.getSelectionModel().clearSelection();
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().trim().isEmpty() ||
                txtNombre.getText().trim().isEmpty() ||
                txtPrecio.getText().trim().isEmpty() ||
                txtStock.getText().trim().isEmpty() ||
                cmbCategoria.getValue() == null ||
                cmbMarcaForm.getValue() == null) {

            mostrarAlerta("Campos requeridos", "Por favor complete todos los campos obligatorios.");
            return false;
        }

        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            if (precio <= 0) {
                mostrarAlerta("Valor inválido", "El precio debe ser mayor a 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Valor inválido", "El precio debe ser un número decimal válido.");
            return false;
        }

        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0) {
                mostrarAlerta("Valor inválido", "El stock debe ser un número entero mayor o igual a 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Valor inválido", "El stock debe ser un número entero válido.");
            return false;
        }

        return true;
    }

    private void mostrarErrorBaseDatos(String mensaje, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Base de Datos");
        alert.setHeaderText(mensaje);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}