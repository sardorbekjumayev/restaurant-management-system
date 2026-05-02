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
import com.example.restaurantmanagementsystem.Model.Restaurant.Branch;
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuItem;
import com.example.restaurantmanagementsystem.Model.Restaurant.MenuSection;
import com.example.restaurantmanagementsystem.Model.Tables.Table;
import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.Model.Users.Address;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Separator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.stage.FileChooser;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Pos;

public class ManagerDashboardController extends BaseDashboardController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ManagementService managementService;
    private User currentUser;

    private final ObservableList<OrderItemRow> orderItemRows = FXCollections.observableArrayList();
    private final Map<Integer, String> customerNameMap = new HashMap<>();
    private final Map<Integer, String> employeeNameMap = new HashMap<>();
    private final Map<Integer, String> orderPaymentStatusMap = new HashMap<>();
    private final Map<Integer, String> menuSectionNameMap = new HashMap<>();
    private List<Table> cachedBranchTables = List.of();

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
    private Label employeeOverviewTitleLabel;
    @FXML
    private Label customerOverviewTitleLabel;
    @FXML
    private Label menuOverviewTitleLabel;
    @FXML
    private Label reservationOverviewTitleLabel;
    @FXML
    private Label activeOrderOverviewTitleLabel;
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
    private Tab branchTab;
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
    private VBox diningTableForm;
    @FXML
    private VBox employeeOverviewCard;
    @FXML
    private VBox customerOverviewCard;
    @FXML
    private VBox menuOverviewCard;
    @FXML
    private VBox reservationOverviewCard;
    @FXML
    private VBox activeOrderOverviewCard;

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
    private Label employeeBranchLabel;
    @FXML
    private ChoiceBox<Branch> employeeBranchBox;

    @FXML
    private TableView<Branch> branchTable;
    @FXML
    private TableColumn<Branch, String> branchNameColumn;
    @FXML
    private TableColumn<Branch, String> branchAddressColumn;
    @FXML
    private TableColumn<Branch, String> branchManagerColumn;
    @FXML
    private TextField branchNameField;
    @FXML
    private TextField branchStreetField;
    @FXML
    private TextField branchCityField;
    @FXML
    private TextField branchDistrictField;
    @FXML
    private TextField branchCountryField;
    @FXML
    private ChoiceBox<Employee> branchManagerBox;

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
    private ChoiceBox<MenuSection> menuSectionBox;
    @FXML
    private TextField menuTitleField;
    @FXML
    private TextArea menuDescriptionField;
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
    private TableColumn<Reservation, String> reservationCustomerIdColumn;
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
    private DatePicker searchDatePicker;
    @FXML
    private TextField searchTimeField;

    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, String> orderCustomerIdColumn;
    @FXML
    private TableColumn<Order, String> orderWaiterIdColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;
    @FXML
    private TableColumn<Order, String> orderCashStatusColumn;
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
    private Label orderCustomerLabel;
    @FXML
    private Label orderWaiterLabel;
    @FXML
    private Label orderTableLabel;

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

    // Initializes shared choice-box values and wires shared listeners for the loaded dashboard.
    @FXML
    public void initialize() {
        if (employeeRoleBox != null) {
            employeeRoleBox.setItems(FXCollections.observableArrayList("Waiter", "Receptionist", "Chef", "Cashier"));
        }
        if (diningTableStatusBox != null) {
            diningTableStatusBox.setItems(FXCollections.observableArrayList(
                    TableStatus.FREE.name(), TableStatus.RESERVED.name(), TableStatus.OCCUPIED.name(), TableStatus.OUT_OF_SERVICE.name()));
        }
        if (reservationStatusBox != null) {
            reservationStatusBox.setItems(FXCollections.observableArrayList(
                    ReservationStatus.requested.name(), ReservationStatus.pending.name(), ReservationStatus.confirmed.name(),
                    ReservationStatus.checkedIn.name(), ReservationStatus.canceled.name(), ReservationStatus.abandoned.name()));
        }
        if (orderStatusBox != null) {
            orderStatusBox.setItems(FXCollections.observableArrayList(
                    OrderStatus.RECEIVED.name(), OrderStatus.PREPARING.name(), OrderStatus.COMPLETE.name(), OrderStatus.CANCELED.name()));
        }
        if (paymentMethodBox != null) {
            paymentMethodBox.setItems(FXCollections.observableArrayList(
                    PaymentMethod.CASH.name(), PaymentMethod.CARD.name(), PaymentMethod.CHECK.name()));
        }
        if (paymentStatusBox != null) {
            paymentStatusBox.setItems(FXCollections.observableArrayList(
                    PaymentStatus.PENDING.name(), PaymentStatus.COMPLETED.name(), PaymentStatus.FAILED.name()));
        }

        configureReservationFormControls();
        configureOrderFormControls();
        configurePaymentFormControls();
        configureAdminFormControls();
        configureTables();
        configureSelectionListeners();
        setDefaultFormValues();
    }

    // Injects the logged-in user, prepares the service layer, and loads dashboard data.
    @Override
    public void setUser(User user) {
        super.currentUser = user;
        super.managementService = new ManagementService(user);
        this.currentUser = user;
        this.managementService = super.managementService;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Xush kelibsiz, " + user.getDisplayName() + " (" + user.getRole() + ")");
        }
        afterUserLoaded();
    }

    // Finishes dashboard setup after login by applying role rules and refreshing visible data.
    @Override
    protected void afterUserLoaded() {
        applyRoleAccess();
        refreshAll();
    }

    // Configures table columns that are present in the current dashboard FXML.
    private void configureTables() {
        if (branchNameColumn != null) {
            branchNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
            branchAddressColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAddressSummary()));
            branchManagerColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getManagerName())));
        }
        if (employeeNameColumn != null) {
            employeeNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getFullName()));
            employeeRoleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getRole()));
            employeeUsernameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAccount().getUsername()));
        }
        if (customerNameColumn != null) {
            customerNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getFullName()));
            customerEmailColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(data.getValue().getEmail())));
            customerPhoneColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPhone()));
        }
        if (menuSectionIdColumn != null) {
            menuSectionIdColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                    menuSectionNameMap.getOrDefault(data.getValue().getSectionId(), String.valueOf(data.getValue().getSectionId()))
            ));
            menuTitleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitle()));
            menuPriceColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));
            menuAvailableColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().isAvailable()));
        }
        if (diningTableNumberColumn != null) {
            diningTableNumberColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTableNumber()));
            diningTableStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
            diningTableCapacityColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMaxCapacity()));
        }
        if (reservationCustomerIdColumn != null) {
            reservationCustomerIdColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCustomer().getFullName()));
            reservationTableIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTableId()));
            reservationTimeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatDateTime(data.getValue().getTimeOfReservation())));
            reservationStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
        }
        if (orderCustomerIdColumn != null) {
            orderCustomerIdColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(customerNameMap.getOrDefault(data.getValue().getCustomerId(), String.valueOf(data.getValue().getCustomerId()))));
            orderWaiterIdColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(employeeNameMap.getOrDefault(data.getValue().getWaiterId(), String.valueOf(data.getValue().getWaiterId()))));
            orderStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
            orderCashStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(orderPaymentStatusMap.getOrDefault(data.getValue().getOrderID(), "UNPAID")));
            orderTotalColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalAmount()));
        }
        if (paymentOrderIdColumn != null) {
            paymentOrderIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getOrderId()));
            paymentMethodColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMethod().name()));
            paymentStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
            paymentAmountColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getAmount()));
        }
    }

    // Configures order form converters, item table editing, and image preview behavior.
    private void configureOrderFormControls() {
        if (orderCustomerBox == null || orderWaiterBox == null || orderTableBox == null || orderMenuItemBox == null
                || orderItemTable == null || orderItemImageColumn == null || orderItemTitleColumn == null
                || orderItemUnitPriceColumn == null || orderItemQtyColumn == null || orderItemLineTotalColumn == null
                || orderItemImageView == null || orderItemQtyField == null) {
            return;
        }
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
        orderCustomerBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                refreshOrderTableOptions(newVal, null));

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

    // Configures reservation-related dropdown rendering and search defaults.
    private void configureReservationFormControls() {
        if (reservationCustomerBox == null || reservationTableBox == null) {
            return;
        }
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
    }

    // Configures payment dropdown labels so cashiers can identify orders quickly.
    private void configurePaymentFormControls() {
        if (paymentOrderBox == null) {
            return;
        }
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

    // Configures admin-only dropdown rendering for branch and manager management.
    private void configureAdminFormControls() {
        if (employeeBranchBox == null || branchManagerBox == null) {
            return;
        }
        employeeBranchBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Branch branch) {
                return branch == null ? "" : branch.getName();
            }

            @Override
            public Branch fromString(String string) {
                return null;
            }
        });
        branchManagerBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employee employee) {
                return employee == null ? "" : employee.getFullName() + " (" + employee.getAccount().getUsername() + ")";
            }

            @Override
            public Employee fromString(String string) {
                return null;
            }
        });
        if (menuSectionBox != null) {
            menuSectionBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(MenuSection section) {
                    return section == null ? "" : section.getTitle();
                }

                @Override
                public MenuSection fromString(String string) {
                    return null;
                }
            });
        }
    }

    // Connects table selections to form population methods for edit workflows.
    private void configureSelectionListeners() {
        if (branchTable != null) {
            branchTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillBranchForm(newValue));
        }
        if (employeeTable != null) {
            employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillEmployeeForm(newValue));
        }
        if (customerTable != null) {
            customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillCustomerForm(newValue));
        }
        if (menuItemTable != null) {
            menuItemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillMenuItemForm(newValue));
        }
        if (diningTableTable != null) {
            diningTableTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillDiningTableForm(newValue));
        }
        if (reservationTable != null) {
            reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillReservationForm(newValue));
        }
        if (orderTable != null) {
            orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillOrderForm(newValue));
        }
        if (paymentTable != null) {
            paymentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillPaymentForm(newValue));
        }
    }

    // Reloads overview counts, table contents, and form dropdown data from the database.
    private void refreshAll() {
        try {
            DashboardStats stats = managementService.loadStats();
            if (employeeCountLabel != null) employeeCountLabel.setText(String.valueOf(stats.getEmployeeCount()));
            if (customerCountLabel != null) customerCountLabel.setText(String.valueOf(stats.getCustomerCount()));
            if (menuCountLabel != null) menuCountLabel.setText(String.valueOf(stats.getMenuItemCount()));
            if (reservationCountLabel != null) reservationCountLabel.setText(String.valueOf(stats.getReservationCount()));
            if (activeOrderCountLabel != null) activeOrderCountLabel.setText(String.valueOf(stats.getActiveOrderCount()));

            if ("Admin".equalsIgnoreCase(currentUser.getRole())) {
                List<Branch> branches = managementService.getBranches();
                List<Employee> employees = managementService.getEmployees();

                if (employeeTable != null) {
                    employeeTable.setItems(FXCollections.observableArrayList(
                            employees.stream().filter(e -> "Manager".equalsIgnoreCase(e.getRole())).toList()
                    ));
                }
                if (branchTable != null) {
                    branchTable.setItems(FXCollections.observableArrayList(branches));
                }
                if (employeeBranchBox != null) {
                    employeeBranchBox.setItems(FXCollections.observableArrayList(branches));
                }
                if (branchManagerBox != null) {
                    branchManagerBox.setItems(FXCollections.observableArrayList(
                            employees.stream().filter(e -> "Manager".equalsIgnoreCase(e.getRole())).toList()
                    ));
                }
                return;
            }

            List<MenuSection> menuSections = managementService.getMenuSections();
            List<MenuItem> menuItems = managementService.getMenuItems();
            List<Table> tables = managementService.getTables();
            List<Customer> customers = managementService.getCustomers();
            List<Employee> employees = managementService.getEmployees();
            List<Order> orders = managementService.getOrders();
            List<Reservation> reservations = managementService.getReservations();
            List<PaymentRecord> payments = managementService.getPayments();
            List<Branch> branches = managementService.getBranches();

            cachedBranchTables = List.copyOf(tables);

            customerNameMap.clear();
            customers.forEach(c -> customerNameMap.put(c.getCustomerId(), c.getFullName()));
            employeeNameMap.clear();
            employees.forEach(e -> employeeNameMap.put(e.getEmployeeID(), e.getFullName()));
            menuSectionNameMap.clear();
            menuSections.forEach(section -> menuSectionNameMap.put(section.getMenuSectionID(), section.getTitle()));
            orderPaymentStatusMap.clear();
            payments.forEach(payment -> orderPaymentStatusMap.put(payment.getOrderId(), payment.getStatus().name()));

            if (employeeTable != null) employeeTable.setItems(FXCollections.observableArrayList(employees));
            if (branchTable != null) branchTable.setItems(FXCollections.observableArrayList(branches));
            if (customerTable != null) customerTable.setItems(FXCollections.observableArrayList(customers));
            if (menuItemTable != null) menuItemTable.setItems(FXCollections.observableArrayList(menuItems));
            if (diningTableTable != null) diningTableTable.setItems(FXCollections.observableArrayList(tables));
            if (reservationTable != null) reservationTable.setItems(FXCollections.observableArrayList(reservations));
            if (orderTable != null) orderTable.setItems(FXCollections.observableArrayList(orders));
            if (paymentTable != null) paymentTable.setItems(FXCollections.observableArrayList(payments));

            if (menuSectionBox != null) menuSectionBox.setItems(FXCollections.observableArrayList(menuSections));
            if (orderCustomerBox != null) orderCustomerBox.setItems(FXCollections.observableArrayList(customers));
            List<Employee> waiters = employees
                    .stream()
                    .filter(e -> "Waiter".equalsIgnoreCase(e.getRole()))
                    .toList();
            if (orderWaiterBox != null) orderWaiterBox.setItems(FXCollections.observableArrayList(waiters));
            if (orderMenuItemBox != null) orderMenuItemBox.setItems(FXCollections.observableArrayList(menuItems.stream().filter(MenuItem::isAvailable).toList()));
            if (orderCustomerBox != null && orderTableBox != null) {
                refreshOrderTableOptions(orderCustomerBox.getValue(), orderTableBox.getValue() == null ? null : orderTableBox.getValue().getTableId());
            }
            if (employeeBranchBox != null) employeeBranchBox.setItems(FXCollections.observableArrayList(branches));
            if (branchManagerBox != null) {
                branchManagerBox.setItems(FXCollections.observableArrayList(
                        employees.stream().filter(e -> "Manager".equalsIgnoreCase(e.getRole())).toList()
                ));
            }

            if (reservationCustomerBox != null) reservationCustomerBox.setItems(FXCollections.observableArrayList(customers));
            if (reservationTableBox != null) reservationTableBox.setItems(FXCollections.observableArrayList(tables));

            if (paymentOrderBox != null) paymentOrderBox.setItems(FXCollections.observableArrayList(orders));

            if ("Admin".equalsIgnoreCase(currentUser.getRole())) {
                if (employeeTable != null) {
                    employeeTable.setItems(FXCollections.observableArrayList(
                            employees.stream().filter(e -> "Manager".equalsIgnoreCase(e.getRole())).toList()
                    ));
                }
            } else if ("Manager".equalsIgnoreCase(currentUser.getRole())) {
                if (employeeTable != null) {
                    employeeTable.setItems(FXCollections.observableArrayList(
                            employees.stream().filter(e -> !"Manager".equalsIgnoreCase(e.getRole())).toList()
                    ));
                }
            }
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    // Applies safe default values to visible forms after screen initialization.
    private void setDefaultFormValues() {
        if (employeeRoleBox != null) employeeRoleBox.setValue("Waiter");
        if (employeeDateJoinedField != null) employeeDateJoinedField.setText(LocalDate.now().toString());
        if (diningTableStatusBox != null) diningTableStatusBox.setValue(TableStatus.FREE.name());
        if (reservationStatusBox != null) reservationStatusBox.setValue(ReservationStatus.requested.name());
        if (reservationTimeField != null) reservationTimeField.setText(formatDateTime(LocalDateTime.now().plusHours(2)));
        if (reservationCustomerBox != null) reservationCustomerBox.setValue(null);
        if (reservationTableBox != null) reservationTableBox.setValue(null);
        if (orderStatusBox != null) orderStatusBox.setValue(OrderStatus.RECEIVED.name());
        if (orderCreatedAtField != null) orderCreatedAtField.setText(formatDateTime(LocalDateTime.now()));
        if (orderTotalField != null) orderTotalField.setText("0.0");
        if (orderItemQtyField != null) orderItemQtyField.setText("1");
        if (paymentMethodBox != null) paymentMethodBox.setValue(PaymentMethod.CASH.name());
        if (paymentStatusBox != null) paymentStatusBox.setValue(PaymentStatus.PENDING.name());
        if (paymentCreatedAtField != null) paymentCreatedAtField.setText(formatDateTime(LocalDateTime.now()));
        if (paymentOrderBox != null) paymentOrderBox.setValue(null);
        if (branchManagerBox != null) branchManagerBox.setValue(null);
        if (employeeBranchBox != null) employeeBranchBox.setValue(null);
    }


    // Shows only the tabs and controls that belong to the current user role.
    private void applyRoleAccess() {
        if (mainTabPane == null || overviewTab == null) {
            return;
        }
        // Reset visibility for all roles
        if (orderCustomerIdColumn != null) orderCustomerIdColumn.setVisible(true);
        if (orderWaiterIdColumn != null) orderWaiterIdColumn.setVisible(true);
        if (orderCustomerLabel != null) {
            orderCustomerLabel.setVisible(true);
            orderCustomerLabel.setManaged(true);
        }
        if (orderCustomerBox != null) {
            orderCustomerBox.setVisible(true);
            orderCustomerBox.setManaged(true);
        }
        if (orderWaiterLabel != null) {
            orderWaiterLabel.setVisible(true);
            orderWaiterLabel.setManaged(true);
        }
        if (orderWaiterBox != null) {
            orderWaiterBox.setVisible(true);
            orderWaiterBox.setManaged(true);
        }
        if (orderTableLabel != null) {
            orderTableLabel.setVisible(true);
            orderTableLabel.setManaged(true);
        }
        if (orderTableBox != null) {
            orderTableBox.setVisible(true);
            orderTableBox.setManaged(true);
        }

        if (diningTableForm != null) {
            diningTableForm.setVisible(true);
            diningTableForm.setManaged(true);
        }
        if (employeeBranchLabel != null) {
            employeeBranchLabel.setVisible(false);
            employeeBranchLabel.setManaged(false);
        }
        if (employeeBranchBox != null) {
            employeeBranchBox.setVisible(false);
            employeeBranchBox.setManaged(false);
        }

        setOverviewAdminMode(false);

        mainTabPane.getTabs().clear();
        mainTabPane.getTabs().add(overviewTab);
        switch (currentUser.getRole()) {
            case "Admin" -> {
                addTabsIfPresent(branchTab, employeeTab);
                if (employeeTab != null) employeeTab.setText("Managers");
                if (employeeRoleBox != null) {
                    employeeRoleBox.setItems(FXCollections.observableArrayList("Manager"));
                    employeeRoleBox.setValue("Manager");
                }
                if (employeeBranchLabel != null) {
                    employeeBranchLabel.setVisible(true);
                    employeeBranchLabel.setManaged(true);
                }
                if (employeeBranchBox != null) {
                    employeeBranchBox.setVisible(true);
                    employeeBranchBox.setManaged(true);
                }
                setOverviewAdminMode(true);
            }
            case "Manager" -> addTabsIfPresent(employeeTab, customerTabPane, menuTab, tablesTab, reservationsTab, ordersTab, paymentsTab);
            case "Receptionist" -> {
                addTabsIfPresent(customerTabPane, tablesTab, reservationsTab);
                if (diningTableForm != null) {
                    diningTableForm.setVisible(false);
                    diningTableForm.setManaged(false);
                }
            }
            case "Waiter" -> addTabsIfPresent(customerTabPane, ordersTab);
            case "Chef" -> {
                addTabsIfPresent(ordersTab);
                if (orderCustomerLabel != null) {
                    orderCustomerLabel.setVisible(false);
                    orderCustomerLabel.setManaged(false);
                }
                if (orderCustomerBox != null) {
                    orderCustomerBox.setVisible(false);
                    orderCustomerBox.setManaged(false);
                }
                if (orderWaiterLabel != null) {
                    orderWaiterLabel.setVisible(false);
                    orderWaiterLabel.setManaged(false);
                }
                if (orderWaiterBox != null) {
                    orderWaiterBox.setVisible(false);
                    orderWaiterBox.setManaged(false);
                }
                if (orderTableLabel != null) {
                    orderTableLabel.setVisible(false);
                    orderTableLabel.setManaged(false);
                }
                if (orderTableBox != null) {
                    orderTableBox.setVisible(false);
                    orderTableBox.setManaged(false);
                }
            }
            case "Cashier" -> {
                mainTabPane.getTabs().clear();
                addTabsIfPresent(paymentsTab);
            }
            default -> addTabsIfPresent(customerTabPane, reservationsTab);
        }
        if (!"Admin".equalsIgnoreCase(currentUser.getRole())) {
            if (employeeTab != null) employeeTab.setText("Employees");
            if (employeeRoleBox != null) {
                employeeRoleBox.setItems(FXCollections.observableArrayList("Waiter", "Receptionist", "Chef", "Cashier"));
            }
        }
    }

    // Adjusts overview cards when the dashboard is in admin-only summary mode.
    private void setOverviewAdminMode(boolean adminMode) {
        if (employeeOverviewTitleLabel != null) employeeOverviewTitleLabel.setText(adminMode ? "Total Employees" : "Employees");
        if (customerOverviewTitleLabel != null) customerOverviewTitleLabel.setText(adminMode ? "Branches" : "Customers");
        if (menuOverviewCard != null) {
            menuOverviewCard.setVisible(!adminMode);
            menuOverviewCard.setManaged(!adminMode);
        }
        if (reservationOverviewCard != null) {
            reservationOverviewCard.setVisible(!adminMode);
            reservationOverviewCard.setManaged(!adminMode);
        }
        if (activeOrderOverviewCard != null) {
            activeOrderOverviewCard.setVisible(!adminMode);
            activeOrderOverviewCard.setManaged(!adminMode);
        }
    }


    // Creates or updates an employee record using the employee form.
    @FXML
    public void saveEmployee() {
        try {
            Employee selected = employeeTable.getSelectionModel().getSelectedItem();
            Branch selectedBranch = "Admin".equalsIgnoreCase(currentUser.getRole())
                    ? requireSelection(employeeBranchBox.getValue(), "Branch")
                    : null;
            Employee employee = new Employee(
                    employeeNameField.getText().trim(),
                    employeeEmailField.getText().trim(),
                    employeePhoneField.getText().trim(),
                    selected == null ? 0 : selected.getEmployeeID(),
                    employeeDateJoinedField.getText().trim(),
                    employeeRoleBox.getValue(),
                    selected == null ? null : selected.getAccount(),
                    selectedBranch == null ? currentUser.getBranchId() : selectedBranch.getId()
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

    // Deletes the currently selected employee from the employee table.
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

    // Clears employee selection and prepares the form for a new employee.
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
        employeeRoleBox.setValue("Admin".equalsIgnoreCase(currentUser.getRole()) ? "Manager" : "Waiter");
        employeeBranchBox.setValue(null);
    }

    // Creates or updates a customer from the customer form fields.
    @FXML
    public void saveCustomer() {
        try {
            Customer selected = customerTable.getSelectionModel().getSelectedItem();
            Customer customer = new Customer(
                    selected == null ? 0 : selected.getCustomerId(),
                    customerNameField.getText().trim(),
                    customerEmailField.getText().trim(),
                    customerPhoneField.getText().trim(),
                    currentUser.getBranchId()
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

    // Deletes the selected customer record.
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

    // Clears the customer form and removes the current table selection.
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

    // Creates or updates a menu item inside the selected menu section.
    @FXML
    public void saveMenuItem() {
        try {
            MenuItem selected = menuItemTable.getSelectionModel().getSelectedItem();
            MenuSection selectedSection = requireSelection(menuSectionBox.getValue(), "Menu section");
            MenuItem item = new MenuItem(
                    selected == null ? 0 : selected.getMenuItemID(),
                    selectedSection.getMenuSectionID(),
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

    // Deletes the selected menu item from the branch menu.
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

    // Clears the menu item form so a new menu item can be entered.
    @FXML
    public void clearMenuItemForm() {
        menuItemTable.getSelectionModel().clearSelection();
        clearMenuItemFormFields();
    }

    private void clearMenuItemFormFields() {
        menuSectionBox.setValue(null);
        menuTitleField.clear();
        menuDescriptionField.clear();
        menuPriceField.clear();
        menuAvailableCheck.setSelected(true);
        menuImageLabel.setText("No file selected");
        selectedMenuImageFile = null;
    }

    // Opens a file chooser and stores the selected image for the menu item.
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

    // Creates or updates a restaurant table for the current branch.
    @FXML
    public void saveDiningTable() {
        try {
            Table selected = diningTableTable.getSelectionModel().getSelectedItem();
            Table table = new Table(
                    selected == null ? 0 : selected.getTableId(),
                    currentUser.getBranchId(),
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

    // Deletes the selected restaurant table.
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

    // Clears the dining-table form for a fresh insert.
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

    // Creates a new branch and optionally assigns the selected manager to it.
    @FXML
    public void saveBranch() {
        try {
            Branch branch = new Branch(
                    branchNameField.getText().trim(),
                    new Address(0,
                            branchStreetField.getText().trim(),
                            branchCityField.getText().trim(),
                            branchDistrictField.getText().trim(),
                            "",
                            branchCountryField.getText().trim())
            );
            Branch created = managementService.createBranch(branch);
            Employee selectedManager = branchManagerBox.getValue();
            if (selectedManager != null) {
                managementService.assignManagerToBranch(selectedManager.getEmployeeID(), created.getId());
            }
            setStatus("Branch qo'shildi", false);
            refreshAll();
            clearBranchForm();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    // Assigns the selected manager to the selected branch.
    @FXML
    public void assignBranchManager() {
        try {
            Branch branch = requireSelection(branchTable.getSelectionModel().getSelectedItem(), "Branch");
            Employee manager = requireSelection(branchManagerBox.getValue(), "Manager");
            managementService.assignManagerToBranch(manager.getEmployeeID(), branch.getId());
            setStatus("Manager branchga biriktirildi", false);
            refreshAll();
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    // Deletes an empty branch after repository safety checks pass.
    @FXML
    public void deleteBranch() {
        try {
            Branch selected = requireSelection(branchTable.getSelectionModel().getSelectedItem(), "Branch");
            managementService.deleteBranch(selected);
            refreshAll();
            clearBranchForm();
            setStatus("Branch o'chirildi", false);
        } catch (Exception e) {
            setStatus(e.getMessage(), true);
        }
    }

    // Clears the branch form and removes the selected branch row.
    @FXML
    public void clearBranchForm() {
        branchTable.getSelectionModel().clearSelection();
        clearBranchFormFields();
    }

    private void clearBranchFormFields() {
        branchNameField.clear();
        branchStreetField.clear();
        branchCityField.clear();
        branchDistrictField.clear();
        branchCountryField.clear();
        branchManagerBox.setValue(null);
    }

    // Creates or updates a reservation for the selected customer and table.
    @FXML
    public void saveReservation() {
        try {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            Customer customer = requireSelection(reservationCustomerBox.getValue(), "Customer");
            Table table = reservationTableBox.getValue();
            Integer tableId = table == null ? null : table.getTableId();

            Reservation reservation = new Reservation(
                    selected == null ? 0 : selected.getReservationId(),
                    parseDateTime(reservationTimeField.getText()),
                    parseInt(reservationPeopleCountField.getText(), "People count"),
                    reservationNotesField.getText().trim(),
                    customer,
                    tableId
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

    // Deletes the currently selected reservation.
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

    // Clears reservation form fields for a new reservation entry.
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

    // Adds the chosen menu item to the in-progress order item list.
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

    // Removes all temporary order items from the current order form.
    @FXML
    public void clearOrderItems() {
        orderItemRows.clear();
        recalculateOrderTotal();
    }

    // Creates or updates an order with the currently selected order items.
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
                    currentUser.getBranchId(),
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
                        row.getMenuItem(),
                        1
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

    // Deletes the selected order from the orders table.
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

    // Clears the order form and resets temporary order items.
    @FXML
    public void clearOrderForm() {
        orderTable.getSelectionModel().clearSelection();
        clearOrderFormFields();
    }

    private void clearOrderFormFields() {
        orderCustomerBox.setValue(null);
        orderWaiterBox.setValue(null);
        orderTableBox.setValue(null);
        refreshOrderTableOptions(null, null);
        orderMenuItemBox.setValue(null);
        orderItemQtyField.setText("1");
        orderItemRows.clear();
        orderStatusBox.setValue(OrderStatus.RECEIVED.name());
        orderCreatedAtField.setText(formatDateTime(LocalDateTime.now()));
        orderTotalField.setText("0.0");
    }

    // Searches available tables for the reservation search date and time.
    @FXML
    public void searchAvailableTables() {
        try {
            LocalDate date = searchDatePicker.getValue();
            if (date == null) throw new IllegalArgumentException("Sana tanlanmagan");
            String timeStr = searchTimeField.getText();
            if (isBlank(timeStr)) throw new IllegalArgumentException("Vaqt kiritilmagan");
            
            LocalDateTime dateTime = LocalDateTime.of(date, java.time.LocalTime.parse(timeStr));
            List<Table> available = managementService.getAvailableTables(dateTime, 120);
            
            reservationTableBox.setItems(FXCollections.observableArrayList(available));
            if (!available.isEmpty()) {
                reservationTableBox.setValue(available.get(0));
                setStatus(available.size() + " ta bo'sh stol topildi", false);
            } else {
                setStatus("Bo'sh stol topilmadi", true);
            }
        } catch (Exception e) {
            setStatus("Qidiruvda xatolik: " + e.getMessage(), true);
        }
    }

    // Creates or updates a payment record for the selected order.
    @FXML
    public void savePayment() {
        try {
            PaymentRecord selected = paymentTable.getSelectionModel().getSelectedItem();
            Order order = requireSelection(paymentOrderBox.getValue(), "Order");
            PaymentRecord existingForOrder = managementService.getPaymentByOrderId(order.getOrderID());
            int paymentId = selected != null
                    ? selected.getId()
                    : existingForOrder == null ? 0 : existingForOrder.getId();
            double amount = isBlank(paymentAmountField.getText())
                    ? order.getTotalAmount()
                    : parseDouble(paymentAmountField.getText(), "Amount");
            PaymentRecord payment = new PaymentRecord(
                    paymentId,
                    order.getOrderID(),
                    amount,
                    PaymentMethod.valueOf(paymentMethodBox.getValue()),
                    PaymentStatus.valueOf(paymentStatusBox.getValue()),
                    parseDateTime(paymentCreatedAtField.getText()),
                    paymentDetailsField.getText().trim()
            );
            if (paymentId == 0) {
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

    // Deletes the currently selected payment record.
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

    // Clears payment form values to start a new payment transaction.
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

    // Returns the user to the login screen and resets the active scene.
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
        if ("Admin".equalsIgnoreCase(currentUser.getRole())) {
            Branch branch = employeeBranchBox.getItems()
                    .stream()
                    .filter(candidate -> candidate.getId() == employee.getBranchId())
                    .findFirst()
                    .orElse(null);
            employeeBranchBox.setValue(branch);
        }
    }

    private void fillBranchForm(Branch branch) {
        if (branch == null) {
            clearBranchFormFields();
            return;
        }
        branchNameField.setText(branch.getName());
        branchStreetField.setText(branch.getLocation() == null ? "" : safe(branch.getLocation().getStreet()));
        branchCityField.setText(branch.getLocation() == null ? "" : safe(branch.getLocation().getCity()));
        branchDistrictField.setText(branch.getLocation() == null ? "" : safe(branch.getLocation().getDistrict()));
        branchCountryField.setText(branch.getLocation() == null ? "" : safe(branch.getLocation().getCountry()));
        Employee manager = branchManagerBox.getItems()
                .stream()
                .filter(candidate -> branch.getManagerName() != null && branch.getManagerName().equalsIgnoreCase(candidate.getFullName()))
                .findFirst()
                .orElse(null);
        branchManagerBox.setValue(manager);
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
        menuSectionBox.setValue(
                menuSectionBox.getItems().stream()
                        .filter(section -> section.getMenuSectionID() == item.getSectionId())
                        .findFirst()
                        .orElse(null)
        );
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

        Table table = null;
        if (reservation.getTableId() != null) {
            table = reservationTableBox.getItems()
                    .stream()
                    .filter(candidate -> candidate.getTableId() == reservation.getTableId())
                    .findFirst()
                    .orElse(null);
        }
        reservationTableBox.setValue(table);

        reservationTimeField.setText(formatDateTime(reservation.getTimeOfReservation()));
        reservationPeopleCountField.setText(String.valueOf(reservation.getPeopleCount()));
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
        refreshOrderTableOptions(customer, order.getTableId());

        Employee waiter = null;
        if (order.getWaiterId() != null) {
            waiter = orderWaiterBox.getItems()
                    .stream()
                    .filter(candidate -> candidate.getEmployeeID() == order.getWaiterId())
                    .findFirst()
                    .orElse(null);
        }
        orderWaiterBox.setValue(waiter);

        orderItemRows.clear();
        for (MealItem mealItem : order.getItems()) {
            orderItemRows.add(new OrderItemRow(mealItem.getMenuItem(), mealItem.getQuantity()));
        }
        orderItemQtyField.setText("1");
        orderStatusBox.setValue(order.getStatus().name());
        orderCreatedAtField.setText(formatDateTime(order.getCreatedAt()));
        orderTotalField.setText(String.valueOf(order.getTotalAmount()));
    }

    private void refreshOrderTableOptions(Customer customer, Integer selectedTableId) {
        List<Table> availableTables = getOrderTablesForCustomer(customer, selectedTableId);
        orderTableBox.setItems(FXCollections.observableArrayList(availableTables));

        Table selectedTable = selectedTableId == null
                ? null
                : availableTables.stream()
                .filter(candidate -> candidate.getTableId() == selectedTableId)
                .findFirst()
                .orElse(null);

        if (selectedTable != null) {
            orderTableBox.setValue(selectedTable);
        } else if (availableTables.size() == 1) {
            orderTableBox.setValue(availableTables.get(0));
        } else {
            orderTableBox.setValue(null);
        }
    }

    private List<Table> getOrderTablesForCustomer(Customer customer, Integer selectedTableId) {
        if (!"Waiter".equalsIgnoreCase(currentUser.getRole())) {
            return cachedBranchTables;
        }
        if (customer == null) {
            return selectedTableId == null ? List.of() : cachedBranchTables.stream()
                    .filter(table -> table.getTableId() == selectedTableId)
                    .toList();
        }

        List<Table> filteredTables = managementService.getAssignedTablesForCustomer(customer.getCustomerId());

        if (filteredTables.isEmpty() && selectedTableId != null) {
            return cachedBranchTables.stream()
                    .filter(table -> table.getTableId() == selectedTableId)
                    .toList();
        }

        return filteredTables;
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

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " son bo'lishi kerak");
        }
    }

    @Override
    protected <T> T requireSelection(T value, String entityName) {
        if (value == null) {
            throw new IllegalArgumentException(entityName + " tanlanmagan");
        }
        return value;
    }

    @Override
    protected void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error ? "-fx-text-fill: #dc2626;" : "-fx-text-fill: #0f766e;");
    }

    @Override
    protected String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    protected boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @SafeVarargs
    private final void addTabsIfPresent(Tab... tabs) {
        for (Tab tab : tabs) {
            if (tab != null && !mainTabPane.getTabs().contains(tab)) {
                mainTabPane.getTabs().add(tab);
            }
        }
    }
}
