package com.example.restaurantmanagementsystem.Controller;

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
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuSection;
import com.example.restaurantmanagementsystem.Model.Tables.Table;
import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.Model.Users.Account;
import com.example.restaurantmanagementsystem.Model.Users.Customer;
import com.example.restaurantmanagementsystem.Model.Users.Employee;
import com.example.restaurantmanagementsystem.Model.Users.Reservation;
import com.example.restaurantmanagementsystem.service.ManagementService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.stage.FileChooser;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Pos;

public class ManagerController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ManagementService managementService;
    private User currentUser;

    private final ObservableList<OrderItemRow> orderItemRows = FXCollections.observableArrayList();
    private final Map<Integer, MenuSection> menuSectionById = new HashMap<>();

    public static final class OrderItemRow {
        private final MenuItem menuItem;
        private final SimpleStringProperty title = new SimpleStringProperty();
        private final SimpleDoubleProperty unitPrice = new SimpleDoubleProperty();
        private final SimpleIntegerProperty quantity = new SimpleIntegerProperty();
        private final SimpleDoubleProperty lineTotal = new SimpleDoubleProperty();

        public OrderItemRow(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem;
            this.title.set(menuItem.getTitle());
            this.unitPrice.set(menuItem.getPrice());
            this.quantity.set(Math.max(1, quantity));
            recalc();
            this.quantity.addListener((obs, oldV, newV) -> recalc());
        }

        private void recalc() {
            int qty = Math.max(1, quantity.get());
            quantity.set(qty);
            lineTotal.set(unitPrice.get() * qty);
        }

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public String getTitle() {
            return title.get();
        }

        public double getUnitPrice() {
            return unitPrice.get();
        }

        public int getQuantity() {
            return quantity.get();
        }

        public void setQuantity(int value) {
            quantity.set(value);
        }

        public double getLineTotal() {
            return lineTotal.get();
        }
    }

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label employeeCountLabel;
    @FXML
    private Label customerCountLabel;
    @FXML
    private Label menuCountLabel;
    @FXML
    private Label reservationCountLabel;
    @FXML
    private Label activeOrderCountLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button logOutButton;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab employeeTab;
    @FXML
    private Tab customerTabPane;
    @FXML
    private Tab menuTab;
    @FXML
    private Tab tablesTab;
    @FXML
    private Tab reservationsTab;
    @FXML
    private Tab ordersTab;
    @FXML
    private Tab paymentsTab;

    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, String> employeeNameColumn;
    @FXML
    private TableColumn<Employee, String> employeeRoleColumn;
    @FXML
    private TableColumn<Employee, String> employeeUsernameColumn;
    @FXML
    private TextField employeeNameField;
    @FXML
    private TextField employeeEmailField;
    @FXML
    private TextField employeePhoneField;
    @FXML
    private TextField employeeUsernameField;
    @FXML
    private TextField employeePasswordField;
    @FXML
    private TextField employeeDateJoinedField;
    @FXML
    private ChoiceBox<String> employeeRoleBox;

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, String> customerNameColumn;
    @FXML
    private TableColumn<Customer, String> customerEmailColumn;
    @FXML
    private TableColumn<Customer, String> customerPhoneColumn;
    @FXML
    private TextField customerNameField;
    @FXML
    private TextField customerEmailField;
    @FXML
    private TextField customerPhoneField;

    @FXML
    private TableView<MenuItem> menuItemTable;
    @FXML
    private TableColumn<MenuItem, String> menuSectionIdColumn;
    @FXML
    private TableColumn<MenuItem, String> menuTitleColumn;
    @FXML
    private TableColumn<MenuItem, Number> menuPriceColumn;
    @FXML
    private TableColumn<MenuItem, Boolean> menuAvailableColumn;
    @FXML
    private TextField menuTitleField;
    @FXML
    private TextArea menuDescriptionField;
    @FXML
    private ChoiceBox<MenuSection> menuSectionBox;
    @FXML
    private TextField menuPriceField;
    @FXML
    private CheckBox menuAvailableCheck;
    @FXML
    private Label menuImageLabel;

    private File selectedMenuImageFile;

    @FXML
    private TableView<Table> diningTableTable;
    @FXML
    private TableColumn<Table, String> diningTableNumberColumn;
    @FXML
    private TableColumn<Table, String> diningTableStatusColumn;
    @FXML
    private TableColumn<Table, Number> diningTableCapacityColumn;
    @FXML
    private TextField diningTableNumberField;
    @FXML
    private ChoiceBox<String> diningTableStatusBox;
    @FXML
    private TextField diningTableCapacityField;
    @FXML
    private TextField diningTableLocationField;

    @FXML
    private TableView<Reservation> reservationTable;
    @FXML
    private TableColumn<Reservation, Number> reservationCustomerIdColumn;
    @FXML
    private TableColumn<Reservation, Number> reservationTableIdColumn;
    @FXML
    private TableColumn<Reservation, String> reservationTimeColumn;
    @FXML
    private TableColumn<Reservation, String> reservationStatusColumn;
    @FXML
    private ChoiceBox<Customer> reservationCustomerBox;
    @FXML
    private ChoiceBox<Table> reservationTableBox;
    @FXML
    private TextField reservationTimeField;
    @FXML
    private TextField reservationPeopleCountField;
    @FXML
    private ChoiceBox<String> reservationStatusBox;
    @FXML
    private TextArea reservationNotesField;
    @FXML
    private TextField reservationCheckInField;

    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Number> orderCustomerIdColumn;
    @FXML
    private TableColumn<Order, Number> orderWaiterIdColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;
    @FXML
    private TableColumn<Order, Number> orderTotalColumn;
    @FXML
    private ChoiceBox<Customer> orderCustomerBox;
    @FXML
    private ChoiceBox<Employee> orderWaiterBox;
    @FXML
    private ChoiceBox<Table> orderTableBox;
    @FXML
    private ComboBox<MenuItem> orderMenuItemBox;
    @FXML
    private javafx.scene.image.ImageView orderItemImageView;
    @FXML
    private TextField orderItemQtyField;
    @FXML
    private TableView<OrderItemRow> orderItemTable;
    @FXML
    private TableColumn<OrderItemRow, String> orderItemTitleColumn;
    @FXML
    private TableColumn<OrderItemRow, MenuItem> orderItemImageColumn;
    @FXML
    private TableColumn<OrderItemRow, Double> orderItemUnitPriceColumn;
    @FXML
    private TableColumn<OrderItemRow, Integer> orderItemQtyColumn;
    @FXML
    private TableColumn<OrderItemRow, Double> orderItemLineTotalColumn;
    @FXML
    private ChoiceBox<String> orderStatusBox;
    @FXML
    private TextField orderCreatedAtField;
    @FXML
    private TextField orderTotalField;

    @FXML
    private TableView<PaymentRecord> paymentTable;
    @FXML
    private TableColumn<PaymentRecord, Number> paymentOrderIdColumn;
    @FXML
    private TableColumn<PaymentRecord, String> paymentMethodColumn;
    @FXML
    private TableColumn<PaymentRecord, String> paymentStatusColumn;
    @FXML
    private TableColumn<PaymentRecord, Number> paymentAmountColumn;
    @FXML
    private ChoiceBox<Order> paymentOrderBox;
    @FXML
    private TextField paymentAmountField;
    @FXML
    private ChoiceBox<String> paymentMethodBox;
    @FXML
    private ChoiceBox<String> paymentStatusBox;
    @FXML
    private TextField paymentCreatedAtField;
    @FXML
    private TextArea paymentDetailsField;

    @FXML
    public void initialize() {
        employeeRoleBox.setItems(FXCollections.observableArrayList("Manager", "Waiter", "Receptionist", "Chef", "Cashier"));
        diningTableStatusBox.setItems(FXCollections.observableArrayList(
                TableStatus.FREE.name(), TableStatus.RESERVED.name(), TableStatus.OCCUPIED.name(), TableStatus.OUT_OF_SERVIS.name()));
        reservationStatusBox.setItems(FXCollections.observableArrayList(
                ReservationStatus.requested.name(), ReservationStatus.pending.name(), ReservationStatus.confirmed.name(),
                ReservationStatus.checkedIn.name(), ReservationStatus.canceled.name(), ReservationStatus.abandoned.name()));
        orderStatusBox.setItems(FXCollections.observableArrayList(
                OrderStatus.RECEIVED.name(), OrderStatus.PREPARING.name(), OrderStatus.COMPLETE.name(), OrderStatus.CANCELED.name()));
        paymentMethodBox.setItems(FXCollections.observableArrayList(
                PaymentMethod.CASH.name(), PaymentMethod.CARD.name(), PaymentMethod.CHECK.name()));
        paymentStatusBox.setItems(FXCollections.observableArrayList(
                PaymentStatus.PENDING.name(), PaymentStatus.COMPLETED.name(), PaymentStatus.FAILED.name()));

        configureReservationFormControls();
        configureOrderFormControls();
        configurePaymentFormControls();
        configureTables();
        configureSelectionListeners();
        setDefaultFormValues();
    }

    public void setUser(User user) {
        this.currentUser = user;
        this.managementService = new ManagementService(user);
        welcomeLabel.setText("Xush kelibsiz, " + user.getDisplayName() + " (" + user.getRole() + ")");
        applyRoleAccess();
        refreshAll();
    }

    private void configureTables() {
        employeeNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getFullName()));
        employeeRoleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getRole()));
        employeeUsernameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAccount().getUsername()));

        customerNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getFullName()));
        customerEmailColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getEmail())));
        customerPhoneColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPhone()));

        menuSectionIdColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                resolveMenuSectionName(data.getValue().getSectionId())
        ));
        menuTitleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitle()));
        menuPriceColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));
        menuAvailableColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().isAvailable()));

        diningTableNumberColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTableNumber()));
        diningTableStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
        diningTableCapacityColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMaxCapacity()));

        reservationCustomerIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCustomer().getCustomerId()));
        reservationTableIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTableId()));
        reservationTimeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatDateTime(data.getValue().getTimeOfReservation())));
        reservationStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));

        orderCustomerIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCustomerId()));
        orderWaiterIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getWaiterId()));
        orderStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
        orderTotalColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalAmount()));

        paymentOrderIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getOrderId()));
        paymentMethodColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMethod().name()));
        paymentStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
        paymentAmountColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getAmount()));
    }

    private void configureOrderFormControls() {
        orderCustomerBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                if (customer == null) {
                    return "";
                }
                return customer.getFullName() + " (" + customer.getPhone() + ")";
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });

        orderWaiterBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employee employee) {
                if (employee == null) {
                    return "";
                }
                return employee.getFullName() + " (" + employee.getRole() + ")";
            }

            @Override
            public Employee fromString(String string) {
                return null;
            }
        });

        orderTableBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Table table) {
                if (table == null) {
                    return "";
                }
                return table.getTableNumber() + " (" + table.getStatus().name() + ")";
            }

            @Override
            public Table fromString(String string) {
                return null;
            }
        });

        orderMenuItemBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MenuItem item) {
                if (item == null) {
                    return "";
                }
                return item.getTitle() + " - " + item.getPrice();
            }

            @Override
            public MenuItem fromString(String string) {
                return null;
            }
        });

        javafx.util.Callback<javafx.scene.control.ListView<MenuItem>, ListCell<MenuItem>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(10);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    ImageView imageView = new ImageView();
                    imageView.setFitHeight(40);
                    imageView.setFitWidth(40);
                    imageView.setPreserveRatio(true);
                    if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                        File imgFile = new File(item.getImageUrl());
                        if (imgFile.exists()) {
                            imageView.setImage(new Image(imgFile.toURI().toString()));
                        }
                    }
                    Label label = new Label(item.getTitle() + " - " + item.getPrice());
                    hBox.getChildren().addAll(imageView, label);
                    setText(null);
                    setGraphic(hBox);
                }
            }
        };

        orderMenuItemBox.setCellFactory(cellFactory);
        orderMenuItemBox.setButtonCell(cellFactory.call(null));

        orderMenuItemBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getImageUrl() != null && !newVal.getImageUrl().isEmpty()) {
                File imgFile = new File(newVal.getImageUrl());
                if (imgFile.exists()) {
                    orderItemImageView.setImage(new javafx.scene.image.Image(imgFile.toURI().toString()));
                } else {
                    orderItemImageView.setImage(null);
                }
            } else {
                orderItemImageView.setImage(null);
            }
        });

        orderItemTable.setItems(orderItemRows);
        orderItemTable.setEditable(true);

        orderItemImageColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMenuItem()));
        orderItemImageColumn.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                        File imgFile = new File(item.getImageUrl());
                        if (imgFile.exists()) {
                            imageView.setImage(new Image(imgFile.toURI().toString()));
                            setGraphic(imageView);
                            return;
                        }
                    }
                    setGraphic(null);
                }
            }
        });

        orderItemTitleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitle()));
        orderItemUnitPriceColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getUnitPrice()));
        orderItemQtyColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));
        orderItemQtyColumn.setCellFactory(column -> new javafx.scene.control.cell.TextFieldTableCell<>(new IntegerStringConverter()));
        orderItemQtyColumn.setOnEditCommit(event -> {
            OrderItemRow row = event.getRowValue();
            Integer value = event.getNewValue();
            row.setQuantity(value == null ? 1 : value);
            recalculateOrderTotal();
            orderItemTable.refresh();
        });
        orderItemLineTotalColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getLineTotal()));
    }

    private void configureReservationFormControls() {
        reservationCustomerBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                if (customer == null) {
                    return "";
                }
                return customer.getFullName() + " (" + customer.getPhone() + ")";
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });

        reservationTableBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Table table) {
                if (table == null) {
                    return "";
                }
                return table.getTableNumber() + " (" + table.getStatus().name() + ")";
            }

            @Override
            public Table fromString(String string) {
                return null;
            }
        });

        reservationTimeField.textProperty().addListener((obs, oldValue, newValue) -> refreshAvailableReservationTables());
        reservationPeopleCountField.textProperty().addListener((obs, oldValue, newValue) -> refreshAvailableReservationTables());
    }

    private void configurePaymentFormControls() {
        paymentOrderBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Order order) {
                if (order == null) {
                    return "";
                }
                String table = order.getTableId() == null ? "-" : "T" + order.getTableId();
                return "#" + order.getOrderID() + " " + table + " " + order.getStatus().name() + " " + order.getTotalAmount();
            }

            @Override
            public Order fromString(String string) {
                return null;
            }
        });

        paymentOrderBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            if (isBlank(paymentAmountField.getText())) {
                paymentAmountField.setText(String.valueOf(newV.getTotalAmount()));
            }
        });
    }

    private void configureSelectionListeners() {
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillEmployeeForm(newValue));
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillCustomerForm(newValue));
        menuItemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillMenuItemForm(newValue));
        diningTableTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillDiningTableForm(newValue));
        reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillReservationForm(newValue));
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillOrderForm(newValue));
        paymentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillPaymentForm(newValue));
    }

    private void refreshAll() {
        try {
            DashboardStats stats = managementService.loadStats();
            employeeCountLabel.setText(String.valueOf(stats.getEmployeeCount()));
            customerCountLabel.setText(String.valueOf(stats.getCustomerCount()));
            menuCountLabel.setText(String.valueOf(stats.getMenuItemCount()));
            reservationCountLabel.setText(String.valueOf(stats.getReservationCount()));
            activeOrderCountLabel.setText(String.valueOf(stats.getActiveOrderCount()));

            List<MenuItem> menuItems = managementService.getMenuItems();
            List<MenuSection> menuSections = managementService.getMenuSections();
            List<Table> tables = managementService.getTables();
            List<Customer> customers = managementService.getCustomers();
            List<Employee> employees = managementService.getEmployees();
            List<Order> orders = managementService.getOrders();

            menuSectionById.clear();
            for (MenuSection section : menuSections) {
                menuSectionById.put(section.getMenuSectionID(), section);
            }

            employeeTable.setItems(FXCollections.observableArrayList(employees));
            customerTable.setItems(FXCollections.observableArrayList(customers));
            menuItemTable.setItems(FXCollections.observableArrayList(menuItems));
            menuSectionBox.setItems(FXCollections.observableArrayList(menuSections));
            if (menuSectionBox.getValue() == null && !menuSections.isEmpty()) {
                menuSectionBox.setValue(menuSections.getFirst());
            }
            diningTableTable.setItems(FXCollections.observableArrayList(tables));
            reservationTable.setItems(FXCollections.observableArrayList(managementService.getReservations()));
            orderTable.setItems(FXCollections.observableArrayList(orders));
            paymentTable.setItems(FXCollections.observableArrayList(managementService.getPayments()));

            orderTableBox.setItems(FXCollections.observableArrayList(tables));
            orderCustomerBox.setItems(FXCollections.observableArrayList(customers));
            List<Employee> waiters = employees
                    .stream()
                    .filter(e -> "Waiter".equalsIgnoreCase(e.getRole()))
                    .toList();
            orderWaiterBox.setItems(FXCollections.observableArrayList(waiters));
            orderMenuItemBox.setItems(FXCollections.observableArrayList(menuItems.stream().filter(MenuItem::isAvailable).toList()));

            reservationCustomerBox.setItems(FXCollections.observableArrayList(customers));
            refreshAvailableReservationTables();

            paymentOrderBox.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    private void setDefaultFormValues() {
        employeeRoleBox.setValue("Waiter");
        employeeDateJoinedField.setText(LocalDate.now().toString());
        diningTableStatusBox.setValue(TableStatus.FREE.name());
        reservationStatusBox.setValue(ReservationStatus.requested.name());
        reservationTimeField.setText(formatDateTime(LocalDateTime.now().plusHours(2)));
        reservationCustomerBox.setValue(null);
        reservationTableBox.setValue(null);
        orderStatusBox.setValue(OrderStatus.RECEIVED.name());
        orderCreatedAtField.setText(formatDateTime(LocalDateTime.now()));
        orderTotalField.setText("0.0");
        orderItemQtyField.setText("1");
        paymentMethodBox.setValue(PaymentMethod.CASH.name());
        paymentStatusBox.setValue(PaymentStatus.PENDING.name());
        paymentCreatedAtField.setText(formatDateTime(LocalDateTime.now()));
        paymentOrderBox.setValue(null);
    }

    private void refreshAvailableReservationTables() {
        if (managementService == null) {
            return;
        }

        LocalDateTime reservationTime;
        try {
            reservationTime = parseDateTime(reservationTimeField.getText());
        } catch (Exception e) {
            return;
        }

        int peopleCount;
        try {
            peopleCount = parseOptionalPositiveInt(reservationPeopleCountField.getText(), 1);
        } catch (Exception e) {
            return;
        }

        try {
            Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
            Integer excludeReservationId = selectedReservation == null ? null : selectedReservation.getReservationId();
            Table currentSelection = reservationTableBox.getValue();
            List<Table> availableTables = managementService.getAvailableTables(reservationTime, peopleCount, excludeReservationId);
            reservationTableBox.setItems(FXCollections.observableArrayList(availableTables));
            if (currentSelection != null) {
                Table matched = availableTables.stream()
                        .filter(table -> table.getTableId() == currentSelection.getTableId())
                        .findFirst()
                        .orElse(null);
                reservationTableBox.setValue(matched);
            } else if (!availableTables.isEmpty()) {
                reservationTableBox.setValue(availableTables.getFirst());
            } else {
                reservationTableBox.setValue(null);
            }
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }


    private void applyRoleAccess() {
        mainTabPane.getTabs().setAll(overviewTab);
        switch (currentUser.getRole()) {
            case "Manager" -> mainTabPane.getTabs().addAll(employeeTab, customerTabPane, menuTab, tablesTab, reservationsTab, ordersTab, paymentsTab);
            case "Receptionist" -> mainTabPane.getTabs().addAll(customerTabPane, tablesTab, reservationsTab);
            case "Waiter" -> mainTabPane.getTabs().addAll(customerTabPane, ordersTab);
            case "Chef" -> mainTabPane.getTabs().add(ordersTab);
            case "Cashier" -> mainTabPane.getTabs().addAll(ordersTab, paymentsTab);
            default -> mainTabPane.getTabs().addAll(customerTabPane, reservationsTab);
        }
    }


    @FXML
    public void saveEmployee() {
        try {
            Employee selected = employeeTable.getSelectionModel().getSelectedItem();
            Employee employee = new Employee(
                    employeeNameField.getText().trim(),
                    employeeEmailField.getText().trim(),
                    employeePhoneField.getText().trim(),
                    selected == null ? 0 : selected.getEmployeeID(),
                    employeeDateJoinedField.getText().trim(),
                    employeeRoleBox.getValue(),
                    selected == null ? null : selected.getAccount(),
                    1 // Default branch ID
            );

            if (selected == null) {
                managementService.createEmployee(employee, employeeUsernameField.getText().trim(), employeePasswordField.getText().trim());
                setStatus("Employee qo'shildi", false);
            } else {
                managementService.updateEmployee(employee, employeeUsernameField.getText().trim(), employeePasswordField.getText().trim());
                setStatus("Employee yangilandi", false);
            }
            refreshAll();
            clearEmployeeForm();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void deleteEmployee() {
        try {
            Employee selected = requireSelection(employeeTable.getSelectionModel().getSelectedItem(), "Employee");
            managementService.deleteEmployee(selected);
            refreshAll();
            clearEmployeeForm();
            setStatus("Employee o'chirildi", false);
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void clearEmployeeForm() {
        employeeTable.getSelectionModel().clearSelection();
        clearEmployeeFormFields();
    }

    private void clearEmployeeFormFields() {
        employeeNameField.clear();
        employeeEmailField.clear();
        employeePhoneField.clear();
        employeeUsernameField.clear();
        employeePasswordField.clear();
        employeeDateJoinedField.setText(LocalDate.now().toString());
        employeeRoleBox.setValue("Waiter");
    }

    @FXML
    public void saveCustomer() {
        try {
            Customer selected = customerTable.getSelectionModel().getSelectedItem();
            Customer customer = new Customer(
                    selected == null ? 0 : selected.getCustomerId(),
                    customerNameField.getText().trim(),
                    customerEmailField.getText().trim(),
                    customerPhoneField.getText().trim(),
                    1 // Default branch ID
            );
            if (selected == null) {
                managementService.createCustomer(customer);
                setStatus("Customer qo'shildi", false);
            } else {
                managementService.updateCustomer(customer);
                setStatus("Customer yangilandi", false);
            }
            refreshAll();
            clearCustomerForm();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void deleteCustomer() {
        try {
            Customer selected = requireSelection(customerTable.getSelectionModel().getSelectedItem(), "Customer");
            managementService.deleteCustomer(selected);
            refreshAll();
            clearCustomerForm();
            setStatus("Customer o'chirildi", false);
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void clearCustomerForm() {
        customerTable.getSelectionModel().clearSelection();
        clearCustomerFormFields();
    }

    private void clearCustomerFormFields() {
        customerNameField.clear();
        customerEmailField.clear();
        customerPhoneField.clear();
    }

    @FXML
    public void saveMenuItem() {
        try {
            MenuItem selected = menuItemTable.getSelectionModel().getSelectedItem();
            MenuSection section = requireSelection(menuSectionBox.getValue(), "Menu section");
            MenuItem item = new MenuItem(
                    selected == null ? 0 : selected.getMenuItemID(),
                    section.getMenuSectionID(),
                    menuTitleField.getText().trim(),
                    menuDescriptionField.getText().trim(),
                    parseDouble(menuPriceField.getText(), "Price"),
                    menuAvailableCheck.isSelected(),
                    null
            );

            if (selectedMenuImageFile != null) {
                File imagesDir = new File("images");
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs();
                }
                String ext = selectedMenuImageFile.getName().substring(selectedMenuImageFile.getName().lastIndexOf("."));
                String newFileName = "menu_" + System.currentTimeMillis() + ext;
                File destFile = new File(imagesDir, newFileName);
                Files.copy(selectedMenuImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                item.setImageUrl("images/" + newFileName);
            } else if (selected != null) {
                item.setImageUrl(selected.getImageUrl());
            }

            if (selected == null) {
                managementService.createMenuItem(item);
                setStatus("Menu item qo'shildi", false);
            } else {
                managementService.updateMenuItem(item);
                setStatus("Menu item yangilandi", false);
            }
            refreshAll();
            clearMenuItemForm();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void deleteMenuItem() {
        try {
            MenuItem selected = requireSelection(menuItemTable.getSelectionModel().getSelectedItem(), "Menu item");
            managementService.deleteMenuItem(selected);
            refreshAll();
            clearMenuItemForm();
            setStatus("Menu item o'chirildi", false);
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void clearMenuItemForm() {
        menuItemTable.getSelectionModel().clearSelection();
        clearMenuItemFormFields();
    }

    private void clearMenuItemFormFields() {
        if (!menuSectionBox.getItems().isEmpty()) {
            menuSectionBox.setValue(menuSectionBox.getItems().getFirst());
        } else {
            menuSectionBox.setValue(null);
        }
        menuTitleField.clear();
        menuDescriptionField.clear();
        menuPriceField.clear();
        menuAvailableCheck.setSelected(true);
        menuImageLabel.setText("No file selected");
        selectedMenuImageFile = null;
    }

    @FXML
    public void chooseMenuImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Rasm tanlang");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedMenuImageFile = file;
            menuImageLabel.setText(file.getName());
        }
    }

    @FXML
    public void saveDiningTable() {
        try {
            Table selected = diningTableTable.getSelectionModel().getSelectedItem();
            Table table = new Table(
                    selected == null ? 0 : selected.getTableId(),
                    1, // Default branch ID
                    diningTableNumberField.getText().trim(),
                    TableStatus.valueOf(diningTableStatusBox.getValue()),
                    parseInt(diningTableCapacityField.getText(), "Capacity"),
                    parseInt(diningTableLocationField.getText(), "Location ID")
            );
            if (selected == null) {
                managementService.createTable(table);
                setStatus("Stol qo'shildi", false);
            } else {
                managementService.updateTable(table);
                setStatus("Stol yangilandi", false);
            }
            refreshAll();
            clearDiningTableForm();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void deleteDiningTable() {
        try {
            Table selected = requireSelection(diningTableTable.getSelectionModel().getSelectedItem(), "Table");
            managementService.deleteTable(selected);
            refreshAll();
            clearDiningTableForm();
            setStatus("Stol o'chirildi", false);
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void clearDiningTableForm() {
        diningTableTable.getSelectionModel().clearSelection();
        clearDiningTableFormFields();
    }

    private void clearDiningTableFormFields() {
        diningTableNumberField.clear();
        diningTableStatusBox.setValue(TableStatus.FREE.name());
        diningTableCapacityField.clear();
        diningTableLocationField.clear();
    }

    @FXML
    public void saveReservation() {
        try {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            Customer customer = requireSelection(reservationCustomerBox.getValue(), "Customer");
            Table table = requireSelection(reservationTableBox.getValue(), "Available table");
            int peopleCount = parseInt(reservationPeopleCountField.getText(), "People count");
            if (peopleCount <= 0) {
                throw new IllegalArgumentException("People count 0 dan katta bo'lishi kerak");
            }

            Reservation reservation = new Reservation(
                    selected == null ? 0 : selected.getReservationId(),
                    parseDateTime(reservationTimeField.getText()),
                    peopleCount,
                    reservationNotesField.getText().trim(),
                    customer,
                    table.getTableId()
            );
            reservation.setStatus(ReservationStatus.valueOf(reservationStatusBox.getValue()));
            if (!reservationCheckInField.getText().isBlank()) {
                reservation.setCheckInTime(parseDateTime(reservationCheckInField.getText()));
            }

            if (selected == null) {
                managementService.createReservation(reservation);
                setStatus("Reservation qo'shildi", false);
            } else {
                managementService.updateReservation(reservation);
                setStatus("Reservation yangilandi", false);
            }
            refreshAll();
            clearReservationForm();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void deleteReservation() {
        try {
            Reservation selected = requireSelection(reservationTable.getSelectionModel().getSelectedItem(), "Reservation");
            managementService.deleteReservation(selected);
            refreshAll();
            clearReservationForm();
            setStatus("Reservation o'chirildi", false);
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void clearReservationForm() {
        reservationTable.getSelectionModel().clearSelection();
        clearReservationFormFields();
    }

    private void clearReservationFormFields() {
        reservationCustomerBox.setValue(null);
        reservationTableBox.setValue(null);
        reservationTimeField.setText(formatDateTime(LocalDateTime.now().plusHours(2)));
        reservationPeopleCountField.clear();
        reservationStatusBox.setValue(ReservationStatus.requested.name());
        reservationNotesField.clear();
        reservationCheckInField.clear();
    }

    @FXML
    public void addOrderItem() {
        try {
            MenuItem menuItem = requireSelection(orderMenuItemBox.getValue(), "Menu item");
            int qty = isBlank(orderItemQtyField.getText()) ? 1 : parseInt(orderItemQtyField.getText(), "Qty");
            if (qty <= 0) {
                throw new IllegalArgumentException("Qty 1 dan katta bo'lishi kerak");
            }

            OrderItemRow existing = orderItemRows.stream()
                    .filter(row -> row.getMenuItem().getMenuItemID() == menuItem.getMenuItemID())
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                orderItemRows.add(new OrderItemRow(menuItem, qty));
            } else {
                existing.setQuantity(existing.getQuantity() + qty);
                orderItemTable.refresh();
            }

            orderItemQtyField.setText("1");
            recalculateOrderTotal();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void clearOrderItems() {
        orderItemRows.clear();
        recalculateOrderTotal();
    }

    @FXML
    public void saveOrder() {
        try {
            Order selected = orderTable.getSelectionModel().getSelectedItem();
            Table selectedTable = requireSelection(orderTableBox.getValue(), "Table");
            if (orderItemRows.isEmpty()) {
                throw new IllegalArgumentException("Kamida bitta item qo'shing");
            }

            Integer customerId = orderCustomerBox.getValue() == null ? null : orderCustomerBox.getValue().getCustomerId();
            Integer waiterId = orderWaiterBox.getValue() == null ? null : orderWaiterBox.getValue().getEmployeeID();

            Order order = new Order(
                    selected == null ? 0 : selected.getOrderID(),
                    customerId,
                    waiterId,
                    selectedTable.getTableId(),
                    OrderStatus.valueOf(orderStatusBox.getValue()),
                    parseDateTime(orderCreatedAtField.getText()),
                    0
            );

            for (OrderItemRow row : orderItemRows) {
                order.getItems().add(new MealItem(
                        0,
                        selected == null ? 0 : selected.getOrderID(),
                        row.getQuantity(),
                        row.getMenuItem()
                ));
            }
            order.setTotalAmount(calculateSelectedOrderTotal());

            if (selected == null) {
                managementService.createOrder(order);
                setStatus("Order qo'shildi", false);
            } else {
                managementService.updateOrder(order);
                setStatus("Order yangilandi", false);
            }
            refreshAll();
            clearOrderForm();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void deleteOrder() {
        try {
            Order selected = requireSelection(orderTable.getSelectionModel().getSelectedItem(), "Order");
            managementService.deleteOrder(selected);
            refreshAll();
            clearOrderForm();
            setStatus("Order o'chirildi", false);
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void clearOrderForm() {
        orderTable.getSelectionModel().clearSelection();
        clearOrderFormFields();
    }

    private void clearOrderFormFields() {
        orderCustomerBox.setValue(null);
        orderWaiterBox.setValue(null);
        orderTableBox.setValue(null);
        orderMenuItemBox.setValue(null);
        orderItemQtyField.setText("1");
        orderItemRows.clear();
        orderStatusBox.setValue(OrderStatus.RECEIVED.name());
        orderCreatedAtField.setText(formatDateTime(LocalDateTime.now()));
        orderTotalField.setText("0.0");
    }

    @FXML
    public void savePayment() {
        try {
            PaymentRecord selected = paymentTable.getSelectionModel().getSelectedItem();
            Order order = requireSelection(paymentOrderBox.getValue(), "Order");
            double amount = isBlank(paymentAmountField.getText())
                    ? order.getTotalAmount()
                    : parseDouble(paymentAmountField.getText(), "Amount");
            PaymentRecord payment = new PaymentRecord(
                    selected == null ? 0 : selected.getId(),
                    order.getOrderID(),
                    amount,
                    PaymentMethod.valueOf(paymentMethodBox.getValue()),
                    PaymentStatus.valueOf(paymentStatusBox.getValue()),
                    parseDateTime(paymentCreatedAtField.getText()),
                    paymentDetailsField.getText().trim()
            );
            if (selected == null) {
                managementService.createPayment(payment);
                setStatus("Payment qo'shildi", false);
            } else {
                managementService.updatePayment(payment);
                setStatus("Payment yangilandi", false);
            }
            refreshAll();
            clearPaymentForm();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void deletePayment() {
        try {
            PaymentRecord selected = requireSelection(paymentTable.getSelectionModel().getSelectedItem(), "Payment");
            managementService.deletePayment(selected);
            refreshAll();
            clearPaymentForm();
            setStatus("Payment o'chirildi", false);
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    public void clearPaymentForm() {
        paymentTable.getSelectionModel().clearSelection();
        clearPaymentFormFields();
    }

    private void clearPaymentFormFields() {
        paymentOrderBox.setValue(null);
        paymentAmountField.clear();
        paymentMethodBox.setValue(PaymentMethod.CASH.name());
        paymentStatusBox.setValue(PaymentStatus.PENDING.name());
        paymentCreatedAtField.setText(formatDateTime(LocalDateTime.now()));
        paymentDetailsField.clear();
    }

    public void logOut() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurantmanagementsystem/login.fxml"));
        Scene scene = new Scene(loader.load(), 720, 520);
        Stage stage = (Stage) logOutButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }


    private void fillEmployeeForm(Employee employee) {
        if (employee == null) {
            clearEmployeeFormFields();
            return;
        }
        employeeNameField.setText(employee.getFullName());
        employeeEmailField.setText(employee.getEmail());
        employeePhoneField.setText(employee.getPhone());
        employeeUsernameField.setText(employee.getAccount().getUsername());
        employeePasswordField.clear();
        employeeDateJoinedField.setText(employee.getDateJoined());
        employeeRoleBox.setValue(employee.getRole());
    }

    private void fillCustomerForm(Customer customer) {
        if (customer == null) {
            clearCustomerFormFields();
            return;
        }
        customerNameField.setText(customer.getFullName());
        customerEmailField.setText(safe(customer.getEmail()));
        customerPhoneField.setText(customer.getPhone());
    }

    private void fillMenuItemForm(MenuItem item) {
        if (item == null) {
            clearMenuItemFormFields();
            return;
        }
        menuSectionBox.setValue(menuSectionById.get(item.getSectionId()));
        menuTitleField.setText(item.getTitle());
        menuDescriptionField.setText(safe(item.getDescription()));
        menuPriceField.setText(String.valueOf(item.getPrice()));
        menuAvailableCheck.setSelected(item.isAvailable());
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            menuImageLabel.setText(new File(item.getImageUrl()).getName());
        } else {
            menuImageLabel.setText("No file selected");
        }
        selectedMenuImageFile = null;
    }

    private void fillDiningTableForm(Table table) {
        if (table == null) {
            clearDiningTableFormFields();
            return;
        }
        diningTableNumberField.setText(table.getTableNumber());
        diningTableStatusBox.setValue(table.getStatus().name());
        diningTableCapacityField.setText(String.valueOf(table.getMaxCapacity()));
        diningTableLocationField.setText(String.valueOf(table.getLocationId()));
    }

    private void fillReservationForm(Reservation reservation) {
        if (reservation == null) {
            clearReservationFormFields();
            return;
        }
        Customer customer = reservationCustomerBox.getItems()
                .stream()
                .filter(candidate -> candidate.getCustomerId() == reservation.getCustomer().getCustomerId())
                .findFirst()
                .orElse(null);
        reservationCustomerBox.setValue(customer);

        reservationTimeField.setText(formatDateTime(reservation.getTimeOfReservation()));
        reservationPeopleCountField.setText(String.valueOf(reservation.getPeopleCount()));
        refreshAvailableReservationTables();

        Table table = null;
        if (reservation.getTableId() != null) {
            table = reservationTableBox.getItems()
                    .stream()
                    .filter(candidate -> candidate.getTableId() == reservation.getTableId())
                    .findFirst()
                    .orElse(null);
        }
        reservationTableBox.setValue(table);

        reservationStatusBox.setValue(reservation.getStatus().name());
        reservationNotesField.setText(safe(reservation.getNotes()));
        reservationCheckInField.setText(reservation.getCheckInTime() == null ? "" : formatDateTime(reservation.getCheckInTime()));
    }

    private void fillOrderForm(Order order) {
        if (order == null) {
            clearOrderFormFields();
            return;
        }
        Customer customer = null;
        if (order.getCustomerId() != null) {
            customer = orderCustomerBox.getItems()
                    .stream()
                    .filter(candidate -> candidate.getCustomerId() == order.getCustomerId())
                    .findFirst()
                    .orElse(null);
        }
        orderCustomerBox.setValue(customer);

        Employee waiter = null;
        if (order.getWaiterId() != null) {
            waiter = orderWaiterBox.getItems()
                    .stream()
                    .filter(candidate -> candidate.getEmployeeID() == order.getWaiterId())
                    .findFirst()
                    .orElse(null);
        }
        orderWaiterBox.setValue(waiter);

        Table table = null;
        if (order.getTableId() != null) {
            table = orderTableBox.getItems()
                    .stream()
                    .filter(candidate -> candidate.getTableId() == order.getTableId())
                    .findFirst()
                    .orElse(null);
        }
        orderTableBox.setValue(table);

        orderItemRows.clear();
        for (MealItem mealItem : order.getItems()) {
            orderItemRows.add(new OrderItemRow(mealItem.getMenuItem(), mealItem.getQuantity()));
        }
        orderItemQtyField.setText("1");
        orderStatusBox.setValue(order.getStatus().name());
        orderCreatedAtField.setText(formatDateTime(order.getCreatedAt()));
        orderTotalField.setText(String.valueOf(order.getTotalAmount()));
    }

    private void fillPaymentForm(PaymentRecord payment) {
        if (payment == null) {
            clearPaymentFormFields();
            return;
        }
        Order order = paymentOrderBox.getItems()
                .stream()
                .filter(candidate -> candidate.getOrderID() == payment.getOrderId())
                .findFirst()
                .orElse(null);
        paymentOrderBox.setValue(order);
        paymentAmountField.setText(String.valueOf(payment.getAmount()));
        paymentMethodBox.setValue(payment.getMethod().name());
        paymentStatusBox.setValue(payment.getStatus().name());
        paymentCreatedAtField.setText(formatDateTime(payment.getCreatedAt()));
        paymentDetailsField.setText(safe(payment.getDetails()));
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMATTER);
    }

    private String resolveMenuSectionName(int sectionId) {
        MenuSection section = menuSectionById.get(sectionId);
        return section == null ? String.valueOf(sectionId) : section.getTitle();
    }

    private double calculateSelectedOrderTotal() {
        return orderItemRows.stream().mapToDouble(OrderItemRow::getLineTotal).sum();
    }

    private void recalculateOrderTotal() {
        orderTotalField.setText(String.valueOf(calculateSelectedOrderTotal()));
    }

    private LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
    }

    private int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " raqam bo'lishi kerak");
        }
    }

    private int parseOptionalPositiveInt(String value, int defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        int parsed = parseInt(value, "People count");
        if (parsed <= 0) {
            throw new IllegalArgumentException("People count 0 dan katta bo'lishi kerak");
        }
        return parsed;
    }

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " son bo'lishi kerak");
        }
    }

    private <T> T requireSelection(T value, String entityName) {
        if (value == null) {
            throw new IllegalArgumentException(entityName + " tanlanmagan");
        }
        return value;
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error ? "-fx-text-fill: #dc2626;" : "-fx-text-fill: #0f766e;");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
