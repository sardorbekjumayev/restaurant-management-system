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
import com.example.restaurantmanagementsystem.Model.Restaurant.Branch;
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuItem;
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuSection;
import com.example.restaurantmanagementsystem.Model.Tables.Table;
import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.Model.Users.Account;
import com.example.restaurantmanagementsystem.Model.Users.AccountStatus;
import com.example.restaurantmanagementsystem.Model.Users.Address;
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
        if ("Admin".equalsIgnoreCase(currentUser.getRole())) {
            return new DashboardStats(
                    count("employees"),
                    count("branches"),
                    count("accounts WHERE role = 'Manager'"),
                    0,
                    0
            );
        }
        return new DashboardStats(
                count("employees WHERE branch_id = " + currentUser.getBranchId()),
                count("customers WHERE branch_id = " + currentUser.getBranchId()),
                countBranchMenuItems(),
                count("reservations WHERE branch_id = " + currentUser.getBranchId()),
                count("orders WHERE status <> 'COMPLETE' AND branch_id = " + currentUser.getBranchId())
        );
    }

    public List<Employee> findEmployees() {
        String sql = "Admin".equalsIgnoreCase(currentUser.getRole()) ? """
                SELECT e.id, e.full_name, e.email, e.phone, e.role, e.date_joined, e.branch_id,
                       a.id AS account_id, a.username, a.password_hash, a.status
                FROM employees e
                JOIN accounts a ON a.id = e.account_id
                ORDER BY e.id
                """ : """
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
            if (!"Admin".equalsIgnoreCase(currentUser.getRole())) {
                statement.setInt(1, currentUser.getBranchId());
            }
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
        validateSingleManager(0, role, employee.getBranchId());

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
                    employeeStatement.setInt(7, resolveBranchId(employee.getBranchId()));
                    employeeStatement.executeUpdate();
                    employee.setEmployeeID(generatedId(employeeStatement, "Employee ID generatsiya bo'lmadi"));
                }

                employee.setRole(role);
                employee.setBranchId(resolveBranchId(employee.getBranchId()));
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
        validateSingleManager(employee.getEmployeeID(), role, employee.getBranchId());

        String updateAccount = password == null || password.isBlank()
                ? "UPDATE accounts SET username = ?, role = ? WHERE id = ?"
                : "UPDATE accounts SET username = ?, password_hash = ?, role = ? WHERE id = ?";
        String updateEmployee = """
                UPDATE employees
                SET full_name = ?, email = ?, phone = ?, role = ?, date_joined = ?, branch_id = ?
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
                employeeStatement.setInt(6, resolveBranchId(employee.getBranchId()));
                employeeStatement.setInt(7, employee.getEmployeeID());
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

    public List<Branch> findBranches() {
        String sql = """
                SELECT b.id, b.name, a.id AS address_id, a.street, a.city, a.district, a.postal_code, a.country,
                       e.full_name AS manager_name, acc.username AS manager_username
                FROM branches b
                JOIN addresses a ON a.id = b.address_id
                LEFT JOIN employees e ON e.branch_id = b.id AND e.role = 'Manager'
                LEFT JOIN accounts acc ON acc.id = e.account_id
                ORDER BY b.id
                """;
        List<Branch> branches = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Address address = new Address(
                        resultSet.getInt("address_id"),
                        resultSet.getString("street"),
                        resultSet.getString("city"),
                        resultSet.getString("district"),
                        resultSet.getString("postal_code"),
                        resultSet.getString("country")
                );
                Branch branch = new Branch(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        address
                );
                branch.setManagerName(resultSet.getString("manager_name"));
                branch.setManagerUsername(resultSet.getString("manager_username"));
                branches.add(branch);
            }
            return branches;
        } catch (SQLException e) {
            throw new IllegalStateException("Branch listni olishda xatolik yuz berdi", e);
        }
    }

    public Branch createBranch(Branch branch) {
        String insertAddress = """
                INSERT INTO addresses (street, city, district, postal_code, country)
                VALUES (?, ?, ?, ?, ?)
                """;
        String insertBranch = """
                INSERT INTO branches (name, address_id)
                VALUES (?, ?)
                """;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement addressStatement = connection.prepareStatement(insertAddress, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement branchStatement = connection.prepareStatement(insertBranch, Statement.RETURN_GENERATED_KEYS)) {
                Address address = branch.getLocation();
                addressStatement.setString(1, address.getStreet());
                addressStatement.setString(2, address.getCity());
                addressStatement.setString(3, address.getDistrict());
                addressStatement.setString(4, address.getPostalCode());
                addressStatement.setString(5, address.getCountry());
                addressStatement.executeUpdate();
                int addressId = generatedId(addressStatement, "Address ID generatsiya bo'lmadi");

                branchStatement.setString(1, branch.getName());
                branchStatement.setInt(2, addressId);
                branchStatement.executeUpdate();
                int branchId = generatedId(branchStatement, "Branch ID generatsiya bo'lmadi");
                ensureDefaultMenuSections(connection, branchId, branch.getName());
                connection.commit();
                return new Branch(branchId, branch.getName(),
                        new Address(addressId, address.getStreet(), address.getCity(), address.getDistrict(), address.getPostalCode(), address.getCountry()));
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Branch qo'shishda xatolik yuz berdi", e);
        }
    }

    public void assignManagerToBranch(int employeeId, int branchId) {
        validateSingleManager(employeeId, "Manager", branchId);
        String sql = "UPDATE employees SET branch_id = ? WHERE id = ? AND role = 'Manager'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, employeeId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Managerni branchga biriktirishda xatolik yuz berdi", e);
        }
    }

    public void deleteBranch(int branchId) {
        try (Connection connection = DBConnection.getConnection()) {
            ensureBranchCanBeDeleted(connection, branchId);

            Integer menuId = findMenuIdByBranch(connection, branchId);
            Integer addressId = findBranchAddressId(connection, branchId);

            connection.setAutoCommit(false);
            try {
                if (menuId != null) {
                    deleteByForeignKey(connection, "DELETE FROM menu_items WHERE section_id IN (SELECT id FROM menu_sections WHERE menu_id = ?)", menuId);
                    deleteByForeignKey(connection, "DELETE FROM menu_sections WHERE menu_id = ?", menuId);
                    deleteByForeignKey(connection, "DELETE FROM menus WHERE id = ?", menuId);
                }
                deleteByForeignKey(connection, "DELETE FROM branches WHERE id = ?", branchId);
                if (addressId != null) {
                    deleteByForeignKey(connection, "DELETE FROM addresses WHERE id = ?", addressId);
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Branch o'chirishda xatolik yuz berdi", e);
        }
    }

    public List<MenuItem> findMenuItems() {
        String sql = """
                SELECT mi.*
                FROM menu_items mi
                JOIN menu_sections ms ON ms.id = mi.section_id
                JOIN menus m ON m.id = ms.menu_id
                WHERE m.branch_id = ?
                ORDER BY mi.id
                """;
        List<MenuItem> items = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getBranchId());
            try (ResultSet resultSet = statement.executeQuery()) {
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
            }
            return items;
        } catch (SQLException e) {
            throw new IllegalStateException("Menu item listni olishda xatolik yuz berdi", e);
        }
    }

    public List<MenuSection> findMenuSections() {
        String sql = """
                SELECT ms.id, ms.menu_id, ms.title, ms.description
                FROM menu_sections ms
                JOIN menus m ON m.id = ms.menu_id
                WHERE m.branch_id = ?
                ORDER BY ms.id
                """;
        List<MenuSection> sections = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ensureDefaultMenuSections(connection, currentUser.getBranchId(), null);
            statement.setInt(1, currentUser.getBranchId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sections.add(new MenuSection(
                            resultSet.getInt("id"),
                            resultSet.getInt("menu_id"),
                            resultSet.getString("title"),
                            resultSet.getString("description")
                    ));
                }
            }
            return sections;
        } catch (SQLException e) {
            throw new IllegalStateException("Menu section listni olishda xatolik yuz berdi", e);
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
            fillReservationStatement(statement, reservation, currentUser.getBranchId());
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
        String sql = """
                SELECT p.*
                FROM payments p
                JOIN orders o ON o.id = p.order_id
                WHERE o.branch_id = ?
                ORDER BY p.order_id, p.id
                """;
        List<PaymentRecord> payments = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection()) {
            cleanupDuplicatePaymentsForBranch(connection);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, currentUser.getBranchId());
                try (ResultSet resultSet = statement.executeQuery()) {
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
                }
            }
            return payments;
        } catch (SQLException e) {
            throw new IllegalStateException("Payment listni olishda xatolik yuz berdi", e);
        }
    }

    public PaymentRecord findPaymentByOrderId(int orderId) {
        String sql = """
                SELECT p.*
                FROM payments p
                JOIN orders o ON o.id = p.order_id
                WHERE p.order_id = ? AND o.branch_id = ?
                ORDER BY p.id DESC
                LIMIT 1
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, currentUser.getBranchId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new PaymentRecord(
                            resultSet.getInt("id"),
                            resultSet.getInt("order_id"),
                            resultSet.getDouble("amount"),
                            PaymentMethod.valueOf(resultSet.getString("method")),
                            PaymentStatus.valueOf(resultSet.getString("status")),
                            resultSet.getTimestamp("created_at").toLocalDateTime(),
                            resultSet.getString("details")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Paymentni order bo'yicha olishda xatolik yuz berdi", e);
        }
    }

    public PaymentRecord createPayment(PaymentRecord payment) {
        String sql = """
                INSERT INTO payments (order_id, amount, method, status, created_at, details)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection()) {
            cleanupDuplicatePaymentsForBranch(connection);
            PaymentRecord existing = findPaymentByOrderId(payment.getOrderId());
            if (existing != null) {
                updatePayment(new PaymentRecord(
                        existing.getId(),
                        payment.getOrderId(),
                        payment.getAmount(),
                        payment.getMethod(),
                        payment.getStatus(),
                        payment.getCreatedAt(),
                        payment.getDetails()
                ));
                return new PaymentRecord(existing.getId(), payment.getOrderId(),
                        payment.getAmount(), payment.getMethod(), payment.getStatus(), payment.getCreatedAt(), payment.getDetails());
            }

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillPaymentStatement(statement, payment);
                statement.executeUpdate();
                return new PaymentRecord(generatedId(statement, "Payment ID generatsiya bo'lmadi"), payment.getOrderId(),
                        payment.getAmount(), payment.getMethod(), payment.getStatus(), payment.getCreatedAt(), payment.getDetails());
            }
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

    private void cleanupDuplicatePaymentsForBranch(Connection connection) throws SQLException {
        String sql = """
                DELETE p1
                FROM payments p1
                JOIN payments p2 ON p1.order_id = p2.order_id AND p1.id < p2.id
                JOIN orders o ON o.id = p1.order_id
                WHERE o.branch_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getBranchId());
            statement.executeUpdate();
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

    private int countBranchMenuItems() {
        String sql = """
                SELECT COUNT(*)
                FROM menu_items mi
                JOIN menu_sections ms ON ms.id = mi.section_id
                JOIN menus m ON m.id = ms.menu_id
                WHERE m.branch_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getBranchId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Branch menu item sonini olishda xatolik yuz berdi", e);
        }
    }

    private void ensureDefaultMenuSections(Connection connection, int branchId, String branchName) throws SQLException {
        Integer menuId = findMenuIdByBranch(connection, branchId);
        if (menuId == null) {
            String insertMenu = "INSERT INTO menus (branch_id, title, description) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertMenu, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, branchId);
                statement.setString(2, (branchName == null || branchName.isBlank() ? "Branch" : branchName) + " Menu");
                statement.setString(3, "Default menu");
                statement.executeUpdate();
                menuId = generatedId(statement, "Menu ID generatsiya bo'lmadi");
            }
        }

        if (countMenuSections(connection, menuId) > 0) {
            return;
        }

        String insertSection = "INSERT INTO menu_sections (menu_id, title, description) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSection)) {
            insertMenuSection(statement, menuId, "Main Meal", "Asosiy taomlar");
            insertMenuSection(statement, menuId, "Drinks", "Ichimliklar");
            insertMenuSection(statement, menuId, "Dessert", "Shirinliklar");
        }
    }

    private Integer findMenuIdByBranch(Connection connection, int branchId) throws SQLException {
        String sql = "SELECT id FROM menus WHERE branch_id = ? ORDER BY id LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return null;
            }
        }
    }

    private int countMenuSections(Connection connection, int menuId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM menu_sections WHERE menu_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, menuId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        }
    }

    private void insertMenuSection(PreparedStatement statement, int menuId, String title, String description) throws SQLException {
        statement.setInt(1, menuId);
        statement.setString(2, title);
        statement.setString(3, description);
        statement.executeUpdate();
    }

    private void ensureBranchCanBeDeleted(Connection connection, int branchId) throws SQLException {
        if (hasRows(connection, "SELECT COUNT(*) FROM employees WHERE branch_id = ?", branchId)) {
            throw new IllegalStateException("Bu branchda employee bor, oldin ularni boshqa branchga o'tkazing yoki o'chiring");
        }
        if (hasRows(connection, "SELECT COUNT(*) FROM customers WHERE branch_id = ?", branchId)) {
            throw new IllegalStateException("Bu branchda customer bor, oldin ularni o'chiring");
        }
        if (hasRows(connection, "SELECT COUNT(*) FROM restaurant_tables WHERE branch_id = ?", branchId)) {
            throw new IllegalStateException("Bu branchda table bor, oldin ularni o'chiring");
        }
        if (hasRows(connection, "SELECT COUNT(*) FROM reservations WHERE branch_id = ?", branchId)) {
            throw new IllegalStateException("Bu branchda reservation bor, oldin ularni o'chiring");
        }
        if (hasRows(connection, "SELECT COUNT(*) FROM orders WHERE branch_id = ?", branchId)) {
            throw new IllegalStateException("Bu branchda order bor, oldin ularni o'chiring");
        }
    }

    private boolean hasRows(Connection connection, String sql, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private Integer findBranchAddressId(Connection connection, int branchId) throws SQLException {
        String sql = "SELECT address_id FROM branches WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return (Integer) resultSet.getObject(1);
                }
                return null;
            }
        }
    }

    private void deleteByForeignKey(Connection connection, String sql, int value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, value);
            statement.executeUpdate();
        }
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

    public List<Table> findAvailableTables(LocalDateTime reservationTime, int peopleCount, Integer excludeReservationId) {
        String sql = """
                SELECT t.*
                FROM restaurant_tables t
                WHERE t.branch_id = ?
                  AND t.max_capacity >= ?
                  AND t.id NOT IN (
                      SELECT r.table_id
                      FROM reservations r
                      WHERE r.branch_id = ?
                        AND r.table_id IS NOT NULL
                        AND r.status IN ('confirmed', 'requested', 'pending', 'checkedIn')
                        AND (? IS NULL OR r.id <> ?)
                        AND (
                            (r.reservation_time <= ? AND DATE_ADD(r.reservation_time, INTERVAL 120 MINUTE) > ?)
                            OR
                            (r.reservation_time >= ? AND r.reservation_time < DATE_ADD(?, INTERVAL 120 MINUTE))
                        )
                  )
                ORDER BY t.id
                """;
        List<Table> availableTables = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            Timestamp start = Timestamp.valueOf(reservationTime);
            statement.setInt(1, currentUser.getBranchId());
            statement.setInt(2, peopleCount);
            statement.setInt(3, currentUser.getBranchId());
            statement.setObject(4, excludeReservationId);
            statement.setObject(5, excludeReservationId);
            statement.setTimestamp(6, start);
            statement.setTimestamp(7, start);
            statement.setTimestamp(8, start);
            statement.setTimestamp(9, start);

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

    private void validateSingleManager(int employeeId, String role, int branchId) {
        if (!"Manager".equalsIgnoreCase(role)) {
            return;
        }
        String sql = "SELECT COUNT(*) FROM employees WHERE role = 'Manager' AND branch_id = ? AND id <> ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resolveBranchId(branchId));
            statement.setInt(2, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                if (resultSet.getInt(1) > 0) {
                    throw new IllegalStateException("Bu branch uchun manager allaqachon mavjud");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Manager validatsiyasida xatolik yuz berdi", e);
        }
    }

    private int resolveBranchId(int employeeBranchId) {
        if ("Admin".equalsIgnoreCase(currentUser.getRole())) {
            if (employeeBranchId <= 0) {
                throw new IllegalArgumentException("Branch tanlanishi kerak");
            }
            return employeeBranchId;
        }
        return currentUser.getBranchId();
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
