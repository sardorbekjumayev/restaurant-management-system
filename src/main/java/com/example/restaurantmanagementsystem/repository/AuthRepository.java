package com.example.restaurantmanagementsystem.repository;

import com.example.restaurantmanagementsystem.DAO.DBConnection;
import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AuthRepository {
    public Optional<User> authenticate(String username, String rawPassword, String role) {
        String sql = """
                SELECT a.id, a.username, a.password_hash, a.role,
                       COALESCE(e.full_name, a.username) AS display_name,
                       e.branch_id,
                       b.name AS branch_name
                FROM accounts a
                LEFT JOIN employees e ON e.account_id = a.id
                LEFT JOIN branches b ON b.id = e.branch_id
                WHERE a.username = ? AND a.role = ? AND a.status = 'ACTIVE'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, role);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && PasswordUtil.matches(rawPassword, resultSet.getString("password_hash"))) {
                    return Optional.of(new User(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            rawPassword,
                            resultSet.getString("role"),
                            resultSet.getString("display_name"),
                            (Integer) resultSet.getObject("branch_id"),
                            resultSet.getString("branch_name")
                    ));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Foydalanuvchini tekshirishda xatolik yuz berdi", e);
        }
    }
}
