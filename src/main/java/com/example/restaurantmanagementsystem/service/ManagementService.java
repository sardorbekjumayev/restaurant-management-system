package com.example.restaurantmanagementsystem.service;

import com.example.restaurantmanagementsystem.Model.DashboardStats;
import com.example.restaurantmanagementsystem.Model.Orders.Order;
import com.example.restaurantmanagementsystem.Model.Payments.PaymentRecord;
import com.example.restaurantmanagementsystem.Model.Restaurant.Branch;
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuItem;
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuSection;
import com.example.restaurantmanagementsystem.Model.Tables.Table;
import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.Model.Users.Customer;
import com.example.restaurantmanagementsystem.Model.Users.Employee;
import com.example.restaurantmanagementsystem.Model.Users.Reservation;
import com.example.restaurantmanagementsystem.Enums.ReservationStatus;
import com.example.restaurantmanagementsystem.repository.ManagementRepository;

import java.time.LocalDateTime;
import java.util.List;

public class ManagementService {
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_PATTERN = "^[+0-9][0-9\\-() ]{6,19}$";

    private final ManagementRepository repository;

    public ManagementService(User currentUser) {
        this.repository = new ManagementRepository(currentUser);
    }

    public DashboardStats loadStats() {
        return repository.loadStats();
    }


    public List<Employee> getEmployees() {
        return repository.findEmployees();
    }

    public Employee createEmployee(Employee employee, String username, String password) {
        validateText(employee.getFullName(), "Employee name");
        validateText(username, "Username");
        validateText(password, "Password");
        validateEmail(employee.getEmail(), "Employee email");
        validatePhone(employee.getPhone(), "Employee phone");
        return repository.createEmployee(employee, username, password);
    }

    public void updateEmployee(Employee employee, String username, String password) {
        validateText(employee.getFullName(), "Employee name");
        validateText(username, "Username");
        validateEmail(employee.getEmail(), "Employee email");
        validatePhone(employee.getPhone(), "Employee phone");
        repository.updateEmployee(employee, username, password);
    }

    public void deleteEmployee(Employee employee) {
        repository.deleteEmployee(employee.getEmployeeID(), employee.getAccount().getId());
    }

    public List<Customer> getCustomers() {
        return repository.findCustomers();
    }

    public List<Branch> getBranches() {
        return repository.findBranches();
    }

    public Branch createBranch(Branch branch) {
        validateText(branch.getName(), "Branch name");
        if (branch.getLocation() == null) {
            throw new IllegalArgumentException("Branch address bo'sh bo'lmasligi kerak");
        }
        validateText(branch.getLocation().getStreet(), "Street");
        validateText(branch.getLocation().getCity(), "City");
        validateText(branch.getLocation().getCountry(), "Country");
        return repository.createBranch(branch);
    }

    public void assignManagerToBranch(int employeeId, int branchId) {
        repository.assignManagerToBranch(employeeId, branchId);
    }

    public void deleteBranch(Branch branch) {
        if (branch == null || branch.getId() <= 0) {
            throw new IllegalArgumentException("Branch tanlanishi kerak");
        }
        repository.deleteBranch(branch.getId());
    }

    public Customer createCustomer(Customer customer) {
        validateText(customer.getFullName(), "Customer name");
        validateEmail(customer.getEmail(), "Customer email");
        validatePhone(customer.getPhone(), "Customer phone");
        return repository.createCustomer(customer);
    }

    public void updateCustomer(Customer customer) {
        validateText(customer.getFullName(), "Customer name");
        validateEmail(customer.getEmail(), "Customer email");
        validatePhone(customer.getPhone(), "Customer phone");
        repository.updateCustomer(customer);
    }

    public void deleteCustomer(Customer customer) {
        repository.deleteCustomer(customer.getCustomerId());
    }

    public List<MenuItem> getMenuItems() {
        return repository.findMenuItems();
    }

    public List<MenuSection> getMenuSections() {
        return repository.findMenuSections();
    }

    public MenuItem createMenuItem(MenuItem item) {
        validateText(item.getTitle(), "Menu title");
        validatePositive(item.getSectionId(), "Menu section");
        return repository.createMenuItem(item);
    }

    public void updateMenuItem(MenuItem item) {
        validateText(item.getTitle(), "Menu title");
        validatePositive(item.getSectionId(), "Menu section");
        repository.updateMenuItem(item);
    }

    public void deleteMenuItem(MenuItem item) {
        repository.deleteMenuItem(item.getMenuItemID());
    }

    public List<Table> getTables() {
        return repository.findTables();
    }

    public List<Table> getAvailableTables(LocalDateTime reservationTime, int peopleCount, Integer excludeReservationId) {
        if (reservationTime == null) {
            throw new IllegalArgumentException("Reservation time bo'sh bo'lmasligi kerak");
        }
        if (peopleCount <= 0) {
            throw new IllegalArgumentException("People count 0 dan katta bo'lishi kerak");
        }
        return repository.findAvailableTables(reservationTime, peopleCount, excludeReservationId);
    }

    public Table createTable(Table table) {
        validateText(table.getTableNumber(), "Table number");
        return repository.createTable(table);
    }

    public void updateTable(Table table) {
        validateText(table.getTableNumber(), "Table number");
        repository.updateTable(table);
    }

    public void deleteTable(Table table) {
        repository.deleteTable(table.getTableId());
    }

    public List<Reservation> getReservations() {
        return repository.findReservations();
    }

    public Reservation createReservation(Reservation reservation) {
        return repository.createReservation(reservation);
    }

    public void updateReservation(Reservation reservation) {
        repository.updateReservation(reservation);
    }

    public void deleteReservation(Reservation reservation) {
        repository.deleteReservation(reservation.getReservationId());
    }

    public List<Order> getOrders() {
        return repository.findOrders();
    }

    public Order createOrder(Order order) {
        return repository.createOrder(order);
    }

    public void updateOrder(Order order) {
        repository.updateOrder(order);
    }

    public void deleteOrder(Order order) {
        repository.deleteOrder(order.getOrderID());
    }

    public List<PaymentRecord> getPayments() {
        return repository.findPayments();
    }

    public PaymentRecord getPaymentByOrderId(int orderId) {
        return repository.findPaymentByOrderId(orderId);
    }

    public PaymentRecord createPayment(PaymentRecord payment) {
        return repository.createPayment(payment);
    }

    public void updatePayment(PaymentRecord payment) {
        repository.updatePayment(payment);
    }

    public void deletePayment(PaymentRecord payment) {
        repository.deletePayment(payment.getId());
    }

    public List<Table> getAvailableTables(LocalDateTime time, int durationMinutes) {
        return repository.findAvailableTables(time, durationMinutes);
    }

    public List<Table> getAssignedTablesForCustomer(int customerId) {
        return repository.findAssignedTablesForCustomer(customerId);
    }

    public void checkUpcomingReservationsAndNotify() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(30);
        List<Reservation> reservations = getReservations();
        for (Reservation res : reservations) {
            if (res.getStatus() == ReservationStatus.confirmed && 
                res.getTimeOfReservation().isAfter(now) && 
                res.getTimeOfReservation().isBefore(threshold)) {
                sendNotification(res.getCustomer().getCustomerId(), 
                    "Sizning band qilgan vaqtingiz yaqinlashmoqda: " + res.getTimeOfReservation(), 
                    "RESERVATION_REMINDER");
            }
        }
    }

    public void sendNotification(int userId, String message, String type) {
        // Logic to insert into notifications table via repository (to be implemented)
        System.out.println("Notification sent to " + userId + ": " + message);
    }

    private void validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " bo'sh bo'lmasligi kerak");
        }
    }

    private void validateEmail(String value, String fieldName) {
        validateText(value, fieldName);
        if (!value.trim().matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException(fieldName + " to'g'ri email formatida bo'lishi kerak");
        }
    }

    private void validatePhone(String value, String fieldName) {
        validateText(value, fieldName);
        if (!value.trim().matches(PHONE_PATTERN)) {
            throw new IllegalArgumentException(fieldName + " faqat raqamga mos formatda bo'lishi kerak");
        }
    }

    private void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " tanlanishi kerak");
        }
    }
}
