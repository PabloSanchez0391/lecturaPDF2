package org.example.dao;

import java.sql.*;
import java.time.LocalDate;

public class ProductoDAO {

    public void insertarProducto(String codigo, String descripcion, LocalDate fecha) {
        String sql = "INSERT INTO productos (codigo, descripcion, fecha_caducidad) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.setString(2, descripcion);
            stmt.setDate(3, Date.valueOf(fecha));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void listarProductos() {
        String sql = "SELECT * FROM productos ORDER BY fecha_caducidad";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(
                        rs.getString("codigo") + " | " +
                                rs.getString("descripcion") + " | " +
                                rs.getDate("fecha_caducidad")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void borrarProducto(String codigo) {
        String sql = "DELETE FROM productos WHERE codigo = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
