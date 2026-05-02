package com.example.restaurantmanagementsystem.DAO;

import com.example.restaurantmanagementsystem.util.PasswordUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS addresses (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        street VARCHAR(255) NOT NULL,
                        city VARCHAR(100) NOT NULL,
                        district VARCHAR(100),
                        postal_code VARCHAR(50),
                        country VARCHAR(100) NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS branches (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(150) NOT NULL UNIQUE,
                        address_id INT NOT NULL,
                        CONSTRAINT fk_branch_address
                            FOREIGN KEY (address_id) REFERENCES addresses(id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        username VARCHAR(80) NOT NULL UNIQUE,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(50) NOT NULL,
                        status VARCHAR(30) NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS employees (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        full_name VARCHAR(150) NOT NULL,
                        email VARCHAR(150) NOT NULL,
                        phone VARCHAR(50) NOT NULL,
                        role VARCHAR(50) NOT NULL,
                        date_joined DATE NOT NULL,
                        account_id INT NOT NULL UNIQUE,
                        branch_id INT NOT NULL,
                        CONSTRAINT fk_employee_account
                            FOREIGN KEY (account_id) REFERENCES accounts(id)
                            ON DELETE CASCADE,
                        CONSTRAINT fk_employee_branch
                            FOREIGN KEY (branch_id) REFERENCES branches(id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS customers (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        full_name VARCHAR(150) NOT NULL,
                        email VARCHAR(150),
                        phone VARCHAR(50) NOT NULL,
                        branch_id INT NOT NULL,
                        CONSTRAINT fk_customer_branch
                            FOREIGN KEY (branch_id) REFERENCES branches(id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS menus (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        branch_id INT NOT NULL,
                        title VARCHAR(150) NOT NULL,
                        description TEXT,
                        CONSTRAINT fk_menu_branch
                            FOREIGN KEY (branch_id) REFERENCES branches(id)
                            ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS menu_sections (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        menu_id INT NOT NULL,
                        title VARCHAR(150) NOT NULL,
                        description TEXT,
                        CONSTRAINT fk_menu_section_menu
                            FOREIGN KEY (menu_id) REFERENCES menus(id)
                            ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS menu_items (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        section_id INT NOT NULL,
                        title VARCHAR(150) NOT NULL,
                        description TEXT,
                        price DECIMAL(12, 2) NOT NULL,
                        available BOOLEAN NOT NULL DEFAULT TRUE,
                        image_url VARCHAR(500),
                        CONSTRAINT fk_menu_item_section
                            FOREIGN KEY (section_id) REFERENCES menu_sections(id)
                            ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS restaurant_tables (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        branch_id INT NOT NULL,
                        table_number VARCHAR(30) NOT NULL UNIQUE,
                        status VARCHAR(30) NOT NULL,
                        max_capacity INT NOT NULL,
                        location_id INT NOT NULL,
                        CONSTRAINT fk_table_branch
                            FOREIGN KEY (branch_id) REFERENCES branches(id)
                            ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS reservations (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        customer_id INT NOT NULL,
                        table_id INT,
                        reservation_time DATETIME NOT NULL,
                        people_count INT NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        notes TEXT,
                        check_in_time DATETIME,
                        branch_id INT NOT NULL,
                        CONSTRAINT fk_reservation_customer
                            FOREIGN KEY (customer_id) REFERENCES customers(id)
                            ON DELETE CASCADE,
                        CONSTRAINT fk_reservation_table
                            FOREIGN KEY (table_id) REFERENCES restaurant_tables(id),
                        CONSTRAINT fk_reservation_branch
                            FOREIGN KEY (branch_id) REFERENCES branches(id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        branch_id INT NOT NULL,
                        customer_id INT,
                        waiter_id INT,
                        table_id INT,
                        status VARCHAR(30) NOT NULL,
                        created_at DATETIME NOT NULL,
                        total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
                        CONSTRAINT fk_order_branch
                            FOREIGN KEY (branch_id) REFERENCES branches(id),
                        CONSTRAINT fk_order_customer
                            FOREIGN KEY (customer_id) REFERENCES customers(id),
                        CONSTRAINT fk_order_waiter
                            FOREIGN KEY (waiter_id) REFERENCES employees(id),
                        CONSTRAINT fk_order_table
                            FOREIGN KEY (table_id) REFERENCES restaurant_tables(id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS order_items (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        order_id INT NOT NULL,
                        menu_item_id INT NOT NULL,
                        quantity INT NOT NULL,
                        CONSTRAINT fk_order_item_order
                            FOREIGN KEY (order_id) REFERENCES orders(id)
                            ON DELETE CASCADE,
                        CONSTRAINT fk_order_item_menu
                            FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS payments (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        order_id INT NOT NULL,
                        amount DECIMAL(12, 2) NOT NULL,
                        method VARCHAR(30) NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        created_at DATETIME NOT NULL,
                        details TEXT,
                        CONSTRAINT fk_payment_order
                            FOREIGN KEY (order_id) REFERENCES orders(id)
                            ON DELETE CASCADE
                    )
                    """);

            addColumnIfMissing(connection, "customers", "branch_id", "ALTER TABLE customers ADD COLUMN branch_id INT NULL");
            addColumnIfMissing(connection, "reservations", "branch_id", "ALTER TABLE reservations ADD COLUMN branch_id INT NULL");
            addColumnIfMissing(connection, "orders", "branch_id", "ALTER TABLE orders ADD COLUMN branch_id INT NULL");

            seedDefaultData(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("MySQL schema yaratishda xatolik yuz berdi", e);
        }
    }

    private static void seedDefaultData(Connection connection) throws SQLException {
        insertAddress(connection, 1, "Amir Temur ko'chasi 10", "Tashkent", "Yunusobod", "100000", "Uzbekistan");
        insertAddress(connection, 2, "Buyuk Ipak Yo'li 42", "Tashkent", "Mirzo Ulug'bek", "100077", "Uzbekistan");
        insertBranch(connection, 1, "Main Branch", 1);
        insertBranch(connection, 2, "East Branch", 2);

        insertAccount(connection, 2, "manager", PasswordUtil.hash("pass"), "Manager", "ACTIVE");
        insertAccount(connection, 3, "waiter", PasswordUtil.hash("123"), "Waiter", "ACTIVE");
        insertAccount(connection, 4, "chef", PasswordUtil.hash("chef123"), "Chef", "ACTIVE");
        insertAccount(connection, 5, "reception", PasswordUtil.hash("recep123"), "Receptionist", "ACTIVE");
        insertAccount(connection, 6, "cashier", PasswordUtil.hash("cash123"), "Cashier", "ACTIVE");
        insertAccount(connection, 7, "manager2", PasswordUtil.hash("pass2"), "Manager", "ACTIVE");

        insertEmployee(connection, 1, "Ali Manager", "manager@restaurant.uz", "+998901112233",
                "Manager", "2025-01-10", 2, 1);
        insertEmployee(connection, 2, "Vali Waiter", "waiter@restaurant.uz", "+998901112244",
                "Waiter", "2025-02-12", 3, 1);
        insertEmployee(connection, 3, "Bek Chef", "chef@restaurant.uz", "+998901112255",
                "Chef", "2025-02-14", 4, 1);
        insertEmployee(connection, 4, "Malika Reception", "reception@restaurant.uz", "+998901112266",
                "Receptionist", "2025-02-17", 5, 1);
        insertEmployee(connection, 5, "Dilshod Cashier", "cashier@restaurant.uz", "+998901112277",
                "Cashier", "2025-02-17", 6, 1);
        insertEmployee(connection, 6, "Sardor Manager", "manager2@restaurant.uz", "+998901112288",
                "Manager", "2025-03-01", 7, 2);

        insertCustomer(connection, 1, "Aziza Karimova", "aziza@mail.com", "+998971112233", 1);
        insertCustomer(connection, 2, "Bekzod Aliyev", "bekzod@mail.com", "+998971112244", 2);

        insertMenu(connection, 1, 1, "Main Menu", "Daily restaurant menu");
        insertMenu(connection, 2, 2, "East Menu", "Branch specific menu");
        insertMenuSection(connection, 1, 1, "Main Dishes", "Signature dishes");
        insertMenuSection(connection, 2, 1, "Drinks", "Fresh drinks");
        insertMenuSection(connection, 3, 1, "Desserts", "Sweet desserts");
        insertMenuSection(connection, 4, 2, "Main Dishes", "East branch dishes");
        insertMenuSection(connection, 5, 2, "Drinks", "East branch drinks");
        insertMenuItem(connection, 1, 1, "Plov", "Traditional Uzbek plov", 45000, true);
        insertMenuItem(connection, 2, 1, "Steak", "Medium grilled steak", 95000, true);
        insertMenuItem(connection, 3, 2, "Lemonade", "Homemade lemonade", 18000, true);
        insertMenuItem(connection, 4, 4, "Lagman", "Hand-pulled noodle soup", 42000, true);
        insertMenuItem(connection, 5, 5, "Tea", "Green tea pot", 12000, true);

        insertTable(connection, 1, 1, "T1", "FREE", 4, 1);
        insertTable(connection, 2, 1, "T2", "RESERVED", 6, 1);
        insertTable(connection, 3, 1, "T3", "OCCUPIED", 2, 2);
        insertTable(connection, 4, 2, "E1", "FREE", 4, 1);
        insertTable(connection, 5, 2, "E2", "FREE", 6, 2);
        migrateExistingRows(connection);
        addColumnIfMissing(connection, "menu_items", "image_url", "ALTER TABLE menu_items ADD COLUMN image_url VARCHAR(500)");
    }

    private static void insertAddress(Connection connection, int id, String street, String city,
                                      String district, String postalCode, String country) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT IGNORE INTO addresses (id, street, city, district, postal_code, country)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setString(2, street);
            statement.setString(3, city);
            statement.setString(4, district);
            statement.setString(5, postalCode);
            statement.setString(6, country);
            statement.executeUpdate();
        }
    }

    private static void insertBranch(Connection connection, int id, String name, int addressId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT IGNORE INTO branches (id, name, address_id)
                VALUES (?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setInt(3, addressId);
            statement.executeUpdate();
        }
    }

    private static void insertAccount(Connection connection, int id, String username, String passwordHash,
                                      String role, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT IGNORE INTO accounts (id, username, password_hash, role, status)
                VALUES (?, ?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setString(2, username);
            statement.setString(3, passwordHash);
            statement.setString(4, role);
            statement.setString(5, status);
            statement.executeUpdate();
        }
    }

    private static void insertEmployee(Connection connection, int id, String fullName, String email,
                                       String phone, String role, String dateJoined,
                                       int accountId, int branchId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT IGNORE INTO employees
                (id, full_name, email, phone, role, date_joined, account_id, branch_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setString(2, fullName);
            statement.setString(3, email);
            statement.setString(4, phone);
            statement.setString(5, role);
            statement.setString(6, dateJoined);
            statement.setInt(7, accountId);
            statement.setInt(8, branchId);
            statement.executeUpdate();
        }
    }

    private static void insertCustomer(Connection connection, int id, String fullName, String email,
                                       String phone, int branchId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT IGNORE INTO customers (id, full_name, email, phone, branch_id)
                VALUES (?, ?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setString(2, fullName);
            statement.setString(3, email);
            statement.setString(4, phone);
            statement.setInt(5, branchId);
            statement.executeUpdate();
        }
    }

    private static void insertMenu(Connection connection, int id, int branchId, String title,
                                   String description) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT IGNORE INTO menus (id, branch_id, title, description)
                VALUES (?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setInt(2, branchId);
            statement.setString(3, title);
            statement.setString(4, description);
            statement.executeUpdate();
        }
    }

    private static void insertMenuSection(Connection connection, int id, int menuId, String title,
                                          String description) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT IGNORE INTO menu_sections (id, menu_id, title, description)
                VALUES (?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setInt(2, menuId);
            statement.setString(3, title);
            statement.setString(4, description);
            statement.executeUpdate();
        }
    }

    private static void insertMenuItem(Connection connection, int id, int sectionId, String title,
                                       String description, double price, boolean available) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT IGNORE INTO menu_items (id, section_id, title, description, price, available)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setInt(2, sectionId);
            statement.setString(3, title);
            statement.setString(4, description);
            statement.setDouble(5, price);
            statement.setBoolean(6, available);
            statement.executeUpdate();
        }
    }

    private static void insertTable(Connection connection, int id, int branchId, String tableNumber,
                                    String status, int maxCapacity, int locationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT IGNORE INTO restaurant_tables
                (id, branch_id, table_number, status, max_capacity, location_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setInt(2, branchId);
            statement.setString(3, tableNumber);
            statement.setString(4, status);
            statement.setInt(5, maxCapacity);
            statement.setInt(6, locationId);
            statement.executeUpdate();
        }
    }

    private static void migrateExistingRows(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE customers SET branch_id = 1 WHERE branch_id IS NULL");
            statement.executeUpdate("UPDATE reservations SET branch_id = 1 WHERE branch_id IS NULL");
            statement.executeUpdate("UPDATE orders SET branch_id = 1 WHERE branch_id IS NULL");
            statement.executeUpdate("""
                    UPDATE accounts
                    SET username = 'manager', role = 'Manager'
                    WHERE id = 1
                      AND role = 'Manager'
                      AND NOT EXISTS (SELECT 1 FROM (SELECT username FROM accounts WHERE username = 'manager') AS t)
                    """);
        }
    }

    private static void addColumnIfMissing(Connection connection, String tableName,
                                           String columnName, String alterSql) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            if (!resultSet.next()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(alterSql);
                }
            }
        }
    }
}
