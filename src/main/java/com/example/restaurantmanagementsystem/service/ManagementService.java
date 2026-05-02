package com.example.restaurantmanagementsystem.service;

import com.example.restaurantmanagementsystem.Model.DashboardStats;
import com.example.restaurantmanagementsystem.Model.Orders.Order;
import com.example.restaurantmanagementsystem.Model.Payments.PaymentRecord;
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuItem;
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuSection;
import com.example.restaurantmanagementsystem.Model.Tables.Table;
import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.Model.Users.Customer;
import com.example.restaurantmanagementsystem.Model.Users.Employee;
import com.example.restaurantmanagementsystem.Model.Users.Reservation;
import com.example.restaurantmanagementsystem.repository.ManagementRepository;

import java.util.List;

public class ManagementService {
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
        return repository.createEmployee(employee, username, password);
    }

    public void updateEmployee(Employee employee, String username, String password) {
        validateText(employee.getFullName(), "Employee name");
        validateText(username, "Username");
        repository.updateEmployee(employee, username, password);
    }

    public void deleteEmployee(Employee employee) {
        repository.deleteEmployee(employee.getEmployeeID(), employee.getAccount().getId());
    }

    public List<Customer> getCustomers() {
        return repository.findCustomers();
    }

    public Customer createCustomer(Customer customer) {
        validateText(customer.getFullName(), "Customer name");
        return repository.createCustomer(customer);
    }

    public void updateCustomer(Customer customer) {
        validateText(customer.getFullName(), "Customer name");
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
        return repository.createMenuItem(item);
    }

    public void updateMenuItem(MenuItem item) {
        validateText(item.getTitle(), "Menu title");
        repository.updateMenuItem(item);
    }

    public void deleteMenuItem(MenuItem item) {
        repository.deleteMenuItem(item.getMenuItemID());
    }

    public List<Table> getTables() {
        return repository.findTables();
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

    public PaymentRecord createPayment(PaymentRecord payment) {
        return repository.createPayment(payment);
    }

    public void updatePayment(PaymentRecord payment) {
        repository.updatePayment(payment);
    }

    public void deletePayment(PaymentRecord payment) {
        repository.deletePayment(payment.getId());
    }

    private void validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " bo'sh bo'lmasligi kerak");
        }
    }
}
