package com.example.restaurantmanagementsystem.repository;

import com.example.restaurantmanagementsystem.DAO.DBConnection;
import com.example.restaurantmanagementsystem.Enums.OrderStatus;
import com.example.restaurantmanagementsystem.Enums.PaymentMethod;
import com.example.restaurantmanagementsystem.Enums.PaymentStatus;
import com.example.restaurantmanagementsystem.Enums.ReservationStatus;
import com.example.restaurantmanagementsystem.Enums.TableStatus;
import com.example.restaurantmanagementsystem.Model.DashboardStats;
import com.example.restaurantmanagementsystem.Model.Orders.MealItem;
import com.example.restaurantmanagementsystem.Model.Orders.Order;
import com.example.restaurantmanagementsystem.Model.Payments.PaymentRecord;
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuItem;
import com.example.restaurantmanagementsystem.Model.Tables.Table;
import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.Model.Users.Account;
import com.example.restaurantmanagementsystem.Model.Users.AccountStatus;
import com.example.restaurantmanagementsystem.Model.Users.Customer;
import com.example.restaurantmanagementsystem.Model.Users.Employee;
import com.example.restaurantmanagementsystem.Model.Users.Reservation;
import com.example.restaurantmanagementsystem.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ManagementRepository {
    private final User currentUser;

    public ManagementRepository(User currentUser) {
        this.currentUser = currentUser;
    }

    // Branch management removed

    public DashboardStats loadStats() {
        return new DashboardStats(
                count("employees WHERE branch_id = " + currentUser.getBranchId()),
                count("customers WHERE branch_id = " + currentUser.getBranchId()),
                count("menu_items"), // Menu items are linked via sections -> menus -> branch
                count("reservations WHERE branch_id = " + currentUser.getBranchId()),
                count("orders WHERE status <> 'COMPLETE' AND branch_id = " + currentUser.getBranchId())
        );
    }

    public List<Employee> findEmployees() {
        String sql = """
                SELECT e.id, e.full_name, e.email, e.phone, e.role, e.date_joined, e.branch_id,
                       a.id AS account_id, a.username, a.password_hash, a.status
                FROM employees e
                JOIN accounts a ON a.id = e.account_id
                WHERE e.branch_id = ?
                ORDER BY e.id
                """;
        List<Employee> employees = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getBranchId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    employees.add(mapEmployee(resultSet));
                }
            }
            return employees;
        } catch (SQLException e) {
            throw new IllegalStateException("Employee listni olishda xatolik yuz berdi", e);
        }
    }

    public Employee createEmployee(Employee employee, String username, String password) {
        String role = normalizeEmployeeRole(employee.getRole(), true);
        validateSingleManager(0, role);

        String insertAccount = """
                INSERT INTO accounts (username, password_hash, role, status)
                VALUES (?, ?, ?, 'ACTIVE')
                """;
        String insertEmployee = """
                INSERT INTO employees (full_name, email, phone, role, date_joined, account_id, branch_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement accountStatement = connection.prepareStatement(insertAccount, Statement.RETURN_GENERATED_KEYS)) {
                accountStatement.setString(1, username);
                accountStatement.setString(2, PasswordUtil.hash(password));
                accountStatement.setString(3, role);
                accountStatement.executeUpdate();

                int accountId = generatedId(accountStatement, "Account ID generatsiya bo'lmadi");

                try (PreparedStatement employeeStatement = connection.prepareStatement(insertEmployee, Statement.RETURN_GENERATED_KEYS)) {
                    employeeStatement.setString(1, employee.getFullName());
                    employeeStatement.setString(2, employee.getEmail());
                    employeeStatement.setString(3, employee.getPhone());
                    employeeStatement.setString(4, role);
                    employeeStatement.setString(5, employee.getDateJoined());
                    employeeStatement.setInt(6, accountId);
                    employeeStatement.setInt(7, currentUser.getBranchId());
                    employeeStatement.executeUpdate();
                    employee.setEmployeeID(generatedId(employeeStatement, "Employee ID generatsiya bo'lmadi"));
                }

                employee.setRole(role);
                employee.setBranchId(currentUser.getBranchId());
                employee.setAccount(new Account(accountId, username, PasswordUtil.hash(password), AccountStatus.ACTIVE, role));
                connection.commit();
                return employee;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Employee qo'shishda xatolik yuz berdi", e);
        }
    }

    public void updateEmployee(Employee employee, String username, String password) {
        String role = normalizeEmployeeRole(employee.getRole(), false);
        validateSingleManager(employee.getEmployeeID(), role);

        String updateAccount = password == null || password.isBlank()
                ? "UPDATE accounts SET username = ?, role = ? WHERE id = ?"
                : "UPDATE accounts SET username = ?, password_hash = ?, role = ? WHERE id = ?";
        String updateEmployee = """
                UPDATE employees
                SET full_name = ?, email = ?, phone = ?, role = ?, date_joined = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement accountStatement = connection.prepareStatement(updateAccount);
                 PreparedStatement employeeStatement = connection.prepareStatement(updateEmployee)) {
                accountStatement.setString(1, username);
                int accountIndex = 2;
                if (password != null && !password.isBlank()) {
                    accountStatement.setString(accountIndex++, PasswordUtil.hash(password));
                }
                accountStatement.setString(accountIndex++, role);
                accountStatement.setInt(accountIndex, employee.getAccount().getId());
                accountStatement.executeUpdate();

                employeeStatement.setString(1, employee.getFullName());
                employeeStatement.setString(2, employee.getEmail());
                employeeStatement.setString(3, employee.getPhone());
                employeeStatement.setString(4, role);
                employeeStatement.setString(5, employee.getDateJoined());
                employeeStatement.setInt(6, employee.getEmployeeID());
                employeeStatement.executeUpdate();

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Employee yangilashda xatolik yuz berdi", e);
        }
    }

    public void deleteEmployee(int employeeId, int accountId) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Employee o'chirishda xatolik yuz berdi", e);
        }
    }

    public List<Customer> findCustomers() {
        String sql = "SELECT * FROM customers WHERE branch_id = ? ORDER BY id";
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getBranchId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(mapCustomer(resultSet));
                }
            }
            return customers;
        } catch (SQLException e) {
            throw new IllegalStateException("Customer listni olishda xatolik yuz berdi", e);
        }
    }

    public Customer createCustomer(Customer customer) {
        String sql = "INSERT INTO customers (full_name, email, phone, branch_id) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getFullName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getPhone());
            statement.setInt(4, currentUser.getBranchId());
            statement.executeUpdate();
            customer.setCustomerId(generatedId(statement, "Customer ID generatsiya bo'lmadi"));
            customer.setBranchId(currentUser.getBranchId());
            return customer;
        } catch (SQLException e) {
            throw new IllegalStateException("Customer qo'shishda xatolik yuz berdi", e);
        }
    }

    public void updateCustomer(Customer customer) {
        String sql = """
                UPDATE customers SET full_name = ?, email = ?, phone = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getFullName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getPhone());
            statement.setInt(4, customer.getCustomerId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Customer yangilashda xatolik yuz berdi", e);
        }
    }

    public void deleteCustomer(int customerId) {
        deleteById("DELETE FROM customers WHERE id = ?", customerId,
                "Customer o'chirishda xatolik yuz berdi");
    }

    public List<MenuItem> findMenuItems() {
        String sql = "SELECT * FROM menu_items ORDER BY id";
        List<MenuItem> items = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                items.add(new MenuItem(
                        resultSet.getInt("id"),
                        resultSet.getInt("section_id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getDouble("price"),
                        resultSet.getBoolean("available"),
                        resultSet.getString("image_url")
                ));
            }
            return items;
        } catch (SQLException e) {
            throw new IllegalStateException("Menu item listni olishda xatolik yuz berdi", e);
        }
    }

    public MenuItem createMenuItem(MenuItem item) {
        String sql = "INSERT INTO menu_items (section_id, title, description, price, available, image_url) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillMenuItemStatement(statement, item);
            statement.executeUpdate();
            item.setMenuItemID(generatedId(statement, "Menu item ID generatsiya bo'lmadi"));
            return item;
        } catch (SQLException e) {
            throw new IllegalStateException("Menu item qo'shishda xatolik yuz berdi", e);
        }
    }

    public void updateMenuItem(MenuItem item) {
        String sql = """
                UPDATE menu_items
                SET section_id = ?, title = ?, description = ?, price = ?, available = ?, image_url = ?
                WHERE id = ?""";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillMenuItemStatement(statement, item);
            statement.setInt(7, item.getMenuItemID());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Menu item yangilashda xatolik yuz berdi", e);
        }
    }

    public void deleteMenuItem(int id) {
        deleteById("DELETE FROM menu_items WHERE id = ?", id, "Menu item o'chirishda xatolik yuz berdi");
    }

    public List<Table> findTables() {
        String sql = "SELECT * FROM restaurant_tables WHERE branch_id = ? ORDER BY id";
        List<Table> tables = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getBranchId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tables.add(new Table(
                            resultSet.getInt("id"),
                            resultSet.getInt("branch_id"),
                            resultSet.getString("table_number"),
                            TableStatus.valueOf(resultSet.getString("status")),
                            resultSet.getInt("max_capacity"),
                            resultSet.getInt("location_id")
                    ));
                }
            }
            return tables;
        } catch (SQLException e) {
            throw new IllegalStateException("Table listni olishda xatolik yuz berdi", e);
        }
    }

    public Table createTable(Table table) {
        String sql = """
                INSERT INTO restaurant_tables (table_number, status, max_capacity, location_id, branch_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, table.getTableNumber());
            statement.setString(2, table.getStatus().name());
            statement.setInt(3, table.getMaxCapacity());
            statement.setInt(4, table.getLocationId());
            statement.setInt(5, currentUser.getBranchId());
            statement.executeUpdate();
            return new Table(generatedId(statement, "Table ID generatsiya bo'lmadi"), currentUser.getBranchId(),
                    table.getTableNumber(), table.getStatus(), table.getMaxCapacity(), table.getLocationId());
        } catch (SQLException e) {
            throw new IllegalStateException("Table qo'shishda xatolik yuz berdi", e);
        }
    }

    public void updateTable(Table table) {
        String sql = """
                UPDATE restaurant_tables
                SET table_number = ?, status = ?, max_capacity = ?, location_id = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table.getTableNumber());
            statement.setString(2, table.getStatus().name());
            statement.setInt(3, table.getMaxCapacity());
            statement.setInt(4, table.getLocationId());
            statement.setInt(5, table.getTableId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Table yangilashda xatolik yuz berdi", e);
        }
    }

    public void deleteTable(int id) {
        deleteById("DELETE FROM restaurant_tables WHERE id = ?",
                id, "Table o'chirishda xatolik yuz berdi");
    }

    public List<Reservation> findReservations() {
        String sql = """
                SELECT r.*, c.full_name, c.email, c.phone, c.branch_id AS customer_branch_id
                FROM reservations r
                JOIN customers c ON c.id = r.customer_id
                WHERE r.branch_id = ?
                ORDER BY r.id
                """;
        List<Reservation> reservations = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getBranchId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Customer customer = new Customer(
                            resultSet.getInt("customer_id"),
                            resultSet.getString("full_name"),
                            resultSet.getString("email"),
                            resultSet.getString("phone"),
                            (Integer) resultSet.getObject("customer_branch_id")
                    );
                    Reservation reservation = new Reservation(
                            resultSet.getInt("id"),
                            resultSet.getTimestamp("reservation_time").toLocalDateTime(),
                            resultSet.getInt("people_count"),
                            resultSet.getString("notes"),
                            customer,
                            (Integer) resultSet.getObject("table_id"),
                            resultSet.getInt("branch_id")
                    );
                    reservation.setStatus(ReservationStatus.valueOf(resultSet.getString("status")));
                    Timestamp checkIn = resultSet.getTimestamp("check_in_time");
                    if (checkIn != null) {
                        reservation.setCheckInTime(checkIn.toLocalDateTime());
                    }
                    reservations.add(reservation);
                }
            }
            return reservations;
        } catch (SQLException e) {
            throw new IllegalStateException("Reservation listni olishda xatolik yuz berdi", e);
        }
    }

    public Reservation createReservation(Reservation reservation) {
        String sql = """
                INSERT INTO reservations (customer_id, table_id, reservation_time, people_count, status, notes, check_in_time, branch_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillReservationStatement(statement, reservation, currentUser.getBranchId());
            statement.executeUpdate();
            reservation.setReservationId(generatedId(statement, "Reservation ID generatsiya bo'lmadi"));
            reservation.setBranchId(currentUser.getBranchId());
            return reservation;
        } catch (SQLException e) {
            throw new IllegalStateException("Reservation qo'shishda xatolik yuz berdi", e);
        }
    }

    public void updateReservation(Reservation reservation) {
        String sql = """
                UPDATE reservations
                SET customer_id = ?, table_id = ?, reservation_time = ?, people_count = ?, status = ?, notes = ?, check_in_time = ?, branch_id = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillReservationStatement(statement, reservation, 1);
            statement.setInt(9, reservation.getReservationId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Reservation yangilashda xatolik yuz berdi", e);
        }
    }

    public void deleteReservation(int id) {
        deleteById("DELETE FROM reservations WHERE id = ?",
                id, "Reservation o'chirishda xatolik yuz berdi");
    }

    public List<Order> findOrders() {
        String sql = "SELECT * FROM orders WHERE branch_id = ? ORDER BY id";
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getBranchId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Order order = new Order(
                            resultSet.getInt("id"),
                            (Integer) resultSet.getObject("branch_id"),
                            (Integer) resultSet.getObject("customer_id"),
                            (Integer) resultSet.getObject("waiter_id"),
                            (Integer) resultSet.getObject("table_id"),
                            OrderStatus.valueOf(resultSet.getString("status")),
                            resultSet.getTimestamp("created_at").toLocalDateTime(),
                            resultSet.getDouble("total_amount")
                    );
                    order.getItems().addAll(findOrderItems(connection, order.getOrderID()));
                    orders.add(order);
                }
            }
            return orders;
        } catch (SQLException e) {
            throw new IllegalStateException("Order listni olishda xatolik yuz berdi", e);
        }
    }

    public Order createOrder(Order order) {
        String sql = """
                INSERT INTO orders (branch_id, customer_id, waiter_id, table_id, status, created_at, total_amount)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                double totalAmount = calculateOrderTotal(order);
                order.setTotalAmount(totalAmount);

                statement.setInt(1, currentUser.getBranchId());
                statement.setObject(2, order.getCustomerId());
                statement.setObject(3, order.getWaiterId());
                statement.setObject(4, order.getTableId());
                statement.setString(5, order.getStatus().name());
                statement.setTimestamp(6, Timestamp.valueOf(order.getCreatedAt()));
                statement.setDouble(7, totalAmount);
                statement.executeUpdate();

                int orderId = generatedId(statement, "Order ID generatsiya bo'lmadi");
                insertOrderItems(connection, orderId, order.getItems());
                connection.commit();

                Order createdOrder = new Order(orderId, currentUser.getBranchId(), order.getCustomerId(), order.getWaiterId(),
                        order.getTableId(), order.getStatus(), order.getCreatedAt(), totalAmount);
                createdOrder.getItems().addAll(order.getItems());
                return createdOrder;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Order qo'shishda xatolik yuz berdi", e);
        }
    }

    public void updateOrder(Order order) {
        String sql = """
                UPDATE orders
                SET branch_id = ?, customer_id = ?, waiter_id = ?, table_id = ?, status = ?, created_at = ?, total_amount = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                double totalAmount = calculateOrderTotal(order);
                order.setTotalAmount(totalAmount);

                fillOrderStatement(statement, order);
                statement.setInt(8, order.getOrderID());
                statement.executeUpdate();
                deleteOrderItems(connection, order.getOrderID());
                insertOrderItems(connection, order.getOrderID(), order.getItems());
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Order yangilashda xatolik yuz berdi", e);
        }
    }

    public void deleteOrder(int id) {
        deleteById("DELETE FROM orders WHERE id = ?",
                id, "Order o'chirishda xatolik yuz berdi");
    }

    public List<PaymentRecord> findPayments() {
        String sql = "SELECT * FROM payments ORDER BY id";
        List<PaymentRecord> payments = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                payments.add(new PaymentRecord(
                        resultSet.getInt("id"),
                        resultSet.getInt("order_id"),
                        resultSet.getDouble("amount"),
                        PaymentMethod.valueOf(resultSet.getString("method")),
                        PaymentStatus.valueOf(resultSet.getString("status")),
                        resultSet.getTimestamp("created_at").toLocalDateTime(),
                        resultSet.getString("details")
                ));
            }
            return payments;
        } catch (SQLException e) {
            throw new IllegalStateException("Payment listni olishda xatolik yuz berdi", e);
        }
    }

    public PaymentRecord createPayment(PaymentRecord payment) {
        String sql = """
                INSERT INTO payments (order_id, amount, method, status, created_at, details)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillPaymentStatement(statement, payment);
            statement.executeUpdate();
            return new PaymentRecord(generatedId(statement, "Payment ID generatsiya bo'lmadi"), payment.getOrderId(),
                    payment.getAmount(), payment.getMethod(), payment.getStatus(), payment.getCreatedAt(), payment.getDetails());
        } catch (SQLException e) {
            throw new IllegalStateException("Payment qo'shishda xatolik yuz berdi", e);
        }
    }

    public void updatePayment(PaymentRecord payment) {
        String sql = """
                UPDATE payments
                SET order_id = ?, amount = ?, method = ?, status = ?, created_at = ?, details = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillPaymentStatement(statement, payment);
            statement.setInt(7, payment.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Payment yangilashda xatolik yuz berdi", e);
        }
    }

    public void deletePayment(int id) {
        deleteById("DELETE FROM payments WHERE id = ?", id, "Payment o'chirishda xatolik yuz berdi");
    }



    private Customer mapCustomer(ResultSet resultSet) throws SQLException {
        return new Customer(
                resultSet.getInt("id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                (Integer) resultSet.getObject("branch_id")
        );
    }

    private Employee mapEmployee(ResultSet resultSet) throws SQLException {
        Account account = new Account(
                resultSet.getInt("account_id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                AccountStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("role")
        );
        return new Employee(
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                resultSet.getInt("id"),
                resultSet.getString("date_joined"),
                resultSet.getString("role"),
                account,
                resultSet.getInt("branch_id")
        );
    }



    private void fillMenuItemStatement(PreparedStatement statement, MenuItem item) throws SQLException {
        statement.setInt(1, item.getSectionId());
        statement.setString(2, item.getTitle());
        statement.setString(3, item.getDescription());
        statement.setDouble(4, item.getPrice());
        statement.setBoolean(5, item.isAvailable());
        statement.setString(6, item.getImageUrl());
    }

    private void fillReservationStatement(PreparedStatement statement, Reservation reservation, int branchId) throws SQLException {
        statement.setInt(1, reservation.getCustomer().getCustomerId());
        if (reservation.getTableId() == null) {
            statement.setObject(2, null);
        } else {
            statement.setInt(2, reservation.getTableId());
        }
        statement.setTimestamp(3, Timestamp.valueOf(reservation.getTimeOfReservation()));
        statement.setInt(4, reservation.getPeopleCount());
        statement.setString(5, reservation.getStatus().name());
        statement.setString(6, reservation.getNotes());
        if (reservation.getCheckInTime() == null) {
            statement.setTimestamp(7, null);
        } else {
            statement.setTimestamp(7, Timestamp.valueOf(reservation.getCheckInTime()));
        }
        statement.setInt(8, branchId);
    }

    public List<Table> findAvailableTables(LocalDateTime time, int durationMinutes) {
        String sql = """
                SELECT * FROM restaurant_tables
                WHERE branch_id = ?
                AND id NOT IN (
                    SELECT table_id FROM reservations
                    WHERE branch_id = ?
                    AND table_id IS NOT NULL
                    AND status IN ('CONFIRMED', 'REQUESTED')
                    AND (
                        (reservation_time <= ? AND DATE_ADD(reservation_time, INTERVAL ? MINUTE) > ?)
                        OR
                        (reservation_time >= ? AND reservation_time < DATE_ADD(?, INTERVAL ? MINUTE))
                    )
                )
                ORDER BY id
                """;
        List<Table> availableTables = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            Timestamp start = Timestamp.valueOf(time);
            
            statement.setInt(1, currentUser.getBranchId());
            statement.setInt(2, currentUser.getBranchId());
            statement.setTimestamp(3, start);
            statement.setInt(4, durationMinutes);
            statement.setTimestamp(5, start);
            statement.setTimestamp(6, start);
            statement.setTimestamp(7, start);
            statement.setInt(8, durationMinutes);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    availableTables.add(new Table(
                            resultSet.getInt("id"),
                            resultSet.getInt("branch_id"),
                            resultSet.getString("table_number"),
                            TableStatus.valueOf(resultSet.getString("status")),
                            resultSet.getInt("max_capacity"),
                            resultSet.getInt("location_id")
                    ));
                }
            }
            return availableTables;
        } catch (SQLException e) {
            throw new IllegalStateException("Available table listni olishda xatolik yuz berdi", e);
        }
    }

    public List<Table> findAssignedTablesForCustomer(int customerId) {
        String sql = """
                SELECT DISTINCT t.*
                FROM restaurant_tables t
                JOIN reservations r ON r.table_id = t.id
                WHERE t.branch_id = ?
                  AND r.branch_id = ?
                  AND r.customer_id = ?
                  AND r.table_id IS NOT NULL
                  AND r.status NOT IN ('canceled', 'abandoned')
                ORDER BY r.reservation_time DESC, t.id
                """;
        List<Table> tables = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getBranchId());
            statement.setInt(2, currentUser.getBranchId());
            statement.setInt(3, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tables.add(new Table(
                            resultSet.getInt("id"),
                            resultSet.getInt("branch_id"),
                            resultSet.getString("table_number"),
                            TableStatus.valueOf(resultSet.getString("status")),
                            resultSet.getInt("max_capacity"),
                            resultSet.getInt("location_id")
                    ));
                }
            }
            return tables;
        } catch (SQLException e) {
            throw new IllegalStateException("Customer uchun biriktirilgan stolni olishda xatolik yuz berdi", e);
        }
    }

    private void fillOrderStatement(PreparedStatement statement, Order order) throws SQLException {
        statement.setInt(1, order.getBranchId());
        statement.setObject(2, order.getCustomerId());
        statement.setObject(3, order.getWaiterId());
        statement.setObject(4, order.getTableId());
        statement.setString(5, order.getStatus().name());
        statement.setTimestamp(6, Timestamp.valueOf(order.getCreatedAt()));
        statement.setDouble(7, order.getTotalAmount());
    }

    private List<MealItem> findOrderItems(Connection connection, int orderId) throws SQLException {
        String sql = """
                SELECT oi.id, oi.order_id, oi.quantity, oi.seat_number,
                       mi.id AS menu_item_id, mi.section_id, mi.title, mi.description, mi.price, mi.available, mi.image_url
                FROM order_items oi
                JOIN menu_items mi ON mi.id = oi.menu_item_id
                WHERE oi.order_id = ?
                ORDER BY oi.id
                """;
        List<MealItem> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MenuItem menuItem = new MenuItem(
                            resultSet.getInt("menu_item_id"),
                            resultSet.getInt("section_id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getDouble("price"),
                            resultSet.getBoolean("available"),
                            resultSet.getString("image_url")
                    );
                    items.add(new MealItem(
                            resultSet.getInt("id"),
                            resultSet.getInt("order_id"),
                            resultSet.getInt("quantity"),
                            menuItem,
                            resultSet.getInt("seat_number")
                    ));
                }
            }
        }
        return items;
    }

    private void deleteOrderItems(Connection connection, int orderId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }

    private void insertOrderItems(Connection connection, int orderId, List<MealItem> items) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, seat_number) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (MealItem item : items) {
                statement.setInt(1, orderId);
                statement.setInt(2, item.getMenuItem().getMenuItemID());
                statement.setInt(3, item.getQuantity());
                statement.setInt(4, item.getSeatNumber());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private double calculateOrderTotal(Order order) {
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order uchun kamida bitta item tanlang");
        }
        return order.getItems()
                .stream()
                .mapToDouble(item -> item.getQuantity() * item.getMenuItem().getPrice())
                .sum();
    }

    private void fillPaymentStatement(PreparedStatement statement, PaymentRecord payment) throws SQLException {
        statement.setInt(1, payment.getOrderId());
        statement.setDouble(2, payment.getAmount());
        statement.setString(3, payment.getMethod().name());
        statement.setString(4, payment.getStatus().name());
        statement.setTimestamp(5, Timestamp.valueOf(payment.getCreatedAt()));
        statement.setString(6, payment.getDetails());
    }



    private String normalizeEmployeeRole(String role, boolean creating) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role bo'sh bo'lmasligi kerak");
        }
        return creating ? capitalize(role) : role;
    }

    private void validateSingleManager(int employeeId, String role) {
        if (!"Manager".equalsIgnoreCase(role)) {
            return;
        }
        String sql = "SELECT COUNT(*) FROM employees WHERE role = 'Manager' AND id <> ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                if (resultSet.getInt(1) > 0) {
                    throw new IllegalStateException("Manager allaqachon mavjud");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Manager validatsiyasida xatolik yuz berdi", e);
        }
    }

    private int count(String tableExpression) {
        try (Connection connection = DBConnection.getConnection()) {
            return count(connection, tableExpression);
        } catch (SQLException e) {
            throw new IllegalStateException("Statistikani olishda xatolik yuz berdi", e);
        }
    }

    private int count(Connection connection, String tableExpression) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableExpression;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private int generatedId(PreparedStatement statement, String errorMessage) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException(errorMessage);
    }

    // Scoped branch helpers removed

    private void deleteById(String sql, int id, String errorMessage) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
