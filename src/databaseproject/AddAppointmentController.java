/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databaseproject;

import static databaseproject.CustomerRecordsController.CustomerList;
import static databaseproject.CustomerRecordsController.selectedCustomer;
import static databaseproject.DBConnection.conn;
import static databaseproject.LoginController.currentUser;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Rob
 */
public class AddAppointmentController implements Initializable {

    ObservableList<LocalTime> DropDownTimeList = FXCollections.observableArrayList();
    ObservableList<LocalTime> DropDownDurationList = FXCollections.observableArrayList();
    
    
    @FXML
    private ComboBox<LocalTime> StartTimeDropDown;
    @FXML
    private ComboBox<LocalTime> AppointmentDurationDropDown;
    @FXML
    private TableView<Customer> CustomerTable;
    @FXML
    private TableColumn<Customer, String> customerName;
    @FXML
    private TableColumn<Customer, String> address;
    @FXML
    private TableColumn<Customer, String> city;
    @FXML
    private TableColumn<Customer, String> postalCode;
    @FXML
    private TableColumn<Customer, String> phone;
    @FXML
    private DatePicker DateChooser;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        PopulateCustomerTable();
        PopulateDropDowns();
        
    }    

    @FXML
    private void CreateNewAppointmentBtnHandler(ActionEvent event) throws IOException, SQLException {
               
        String sqlStatement;
        Statement stmt;     
        
        Interface1 alert1 = () -> {                                       // Lambda expression 1 for removing missing field alerts from main body of code (less clutter)
            Alert missingFields = new Alert(Alert.AlertType.INFORMATION);
            missingFields.setTitle("Missing Information");
            missingFields.setHeaderText("Please Make a Selection For All Fields");
            missingFields.showAndWait();
            return null;
        };
        Interface2 alert2 = () -> {                                      // Lambda expression 2 for removing appoitments on weekend alerts from main body of code (less clutter)
            Alert missingFields = new Alert(AlertType.INFORMATION);       
            missingFields.setTitle("Invalid Selection");
            missingFields.setHeaderText("Appointments Cannot Be Made On Weekends");
            missingFields.showAndWait();
            return null;
        };
        Interface3 alert3 = () -> {
            Alert missingCustomer = new Alert(AlertType.INFORMATION);
            missingCustomer.setTitle("Missing Information");
            missingCustomer.setHeaderText("Please Select a Customer");
            missingCustomer.showAndWait();;
            return null;
        };
        
        if (DateChooser.getValue() == null || StartTimeDropDown.getValue() == null || AppointmentDurationDropDown.getValue() == null){ // alert for empty user fields
            alert1.missingFieldsAlert();
        }
        else if (DateChooser.getValue().getDayOfWeek() == SATURDAY | DateChooser.getValue().getDayOfWeek() == SUNDAY){ // alert for appointment made on weekend (9 - 5 business hours is handled seperately with a time selection drop down)
            alert2.appointmentOnWeekendAlert();
        }
        else if (CustomerTable.getSelectionModel().getSelectedItems().isEmpty()) { //  alert for not selecting a customer
            alert3.selectCustomerAlert();
        }   
        else {
            String title = "not needed";
            String description = "not needed";
            String location = "not needed";
            String contact = "not needed";
            String url = "not needed";
            selectedCustomer = CustomerTable.getSelectionModel().getSelectedItems();
            int customerId = selectedCustomer.get(0).getCustomerID();
            LocalDate userDate = DateChooser.getValue();
            LocalTime start = StartTimeDropDown.getValue();
            LocalTime end = StartTimeDropDown.getValue();
            
            LocalDateTime startDateTime = LocalDateTime.of(userDate, start);
            LocalDateTime endDateTime = LocalDateTime.of(userDate, end);
            // adds duration selected to get end time
            endDateTime = endDateTime.plusHours(AppointmentDurationDropDown.getValue().getHour());
            endDateTime = endDateTime.plusMinutes(AppointmentDurationDropDown.getValue().getMinute());

            // Converts user datetime to UTC for storage in database
            ZonedDateTime startDateTimeZoned = ZonedDateTime.of(startDateTime, ZoneId.systemDefault());
            ZonedDateTime startDateTimeZonedUTC = startDateTimeZoned.withZoneSameInstant(ZoneId.of("UTC")); //  variable to be stored in database as start
            ZonedDateTime endDateTimeZoned = ZonedDateTime.of(endDateTime, ZoneId.systemDefault());
            ZonedDateTime endDateTimeZonedUTC = endDateTimeZoned.withZoneSameInstant(ZoneId.of("UTC")); //  variable to be stored in database as end
            
            // Checks for overlapping appointments
            boolean overlappingAppointmentCheck = false;
            try {
                stmt = conn.createStatement();
                sqlStatement = "SELECT start, end , createdBy FROM appointment;";
                ResultSet result = stmt.executeQuery(sqlStatement);
                while (result.next()) {
                    // converts start time from DB to zonedtime for comparison to user input
                    Timestamp startTimestampDB = Timestamp.valueOf(result.getString("start"));
                    LocalDateTime startLocalDateTimeDB = startTimestampDB.toLocalDateTime();
                    ZonedDateTime startZonedDateTimeUTCDB = ZonedDateTime.of(startLocalDateTimeDB, ZoneId.of("UTC"));
                    // converts end time from DB to zonedtime for comparison to user input
                    Timestamp endTimestampDB = Timestamp.valueOf(result.getString("end"));
                    LocalDateTime endLocalDateTimeDB = endTimestampDB.toLocalDateTime();
                    ZonedDateTime endZonedDateTimeUTCDB = ZonedDateTime.of(endLocalDateTimeDB, ZoneId.of("UTC"));
                    
                    String consultantFromDB = result.getString("createdBy");
                    // checks for overlapping appointments, but allows for overlap if the user(consultant) is different
                    if ((startDateTimeZonedUTC.isAfter(startZonedDateTimeUTCDB)) & (startDateTimeZonedUTC.isBefore(endZonedDateTimeUTCDB)) & currentUser.equals(consultantFromDB)) { 
                        overlappingAppointmentCheck = true;                     
                    }
                    if ((endDateTimeZonedUTC.isAfter(startZonedDateTimeUTCDB)) & (endDateTimeZonedUTC.isBefore(endZonedDateTimeUTCDB)) & currentUser.equals(consultantFromDB)) {
                        overlappingAppointmentCheck = true;
                    }
                    if ((startDateTimeZonedUTC.isBefore(startZonedDateTimeUTCDB)) & (endDateTimeZonedUTC.isAfter(endZonedDateTimeUTCDB)) & currentUser.equals(consultantFromDB)) {
                        overlappingAppointmentCheck = true;
                    }
                    if (((startDateTimeZonedUTC.isEqual(startZonedDateTimeUTCDB)) | (endDateTimeZonedUTC.isEqual(endZonedDateTimeUTCDB))) & currentUser.equals(consultantFromDB)) {
                        overlappingAppointmentCheck = true;
                    }
                }
            }
            catch (SQLException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error: " + ex);
            }
            
            if (overlappingAppointmentCheck){
                Alert missingFields = new Alert(AlertType.INFORMATION);
                missingFields.setTitle("Invalid Selection");
                missingFields.setHeaderText("The Selected Date and Time Overlap With an Existing Appointment");
                missingFields.showAndWait();
            }
            //end overlap check
            
            
            // adds appointment if all other conditions are met
            else {
                try {
                    stmt = conn.createStatement();

                    sqlStatement = "INSERT INTO appointment (customerId, title, description, location, contact, url, start, end, createDate, createdBy, lastUpdateBy) "
                            + "VALUES (" + customerId + ", 'not needed', 'not needed', 'not needed', 'not needed', 'not needed', '" + startDateTimeZonedUTC + "', '" + endDateTimeZonedUTC + "', (SELECT NOW()), '" + currentUser + "', '" + currentUser + "');";
                    stmt.executeUpdate(sqlStatement);

                } catch (SQLException ex) {
                    Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Error: " + ex);
                }
                    Parent root = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
                    Scene scene = new Scene(root);
                    Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.centerOnScreen();
                    stage.show();
            }
        }
    }

    @FXML
    private void CancelBtnHandler(ActionEvent event) throws IOException {
            Parent root = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
    }
    
    private void PopulateCustomerTable() {
        String sqlStatement;
        Statement stmt;
        CustomerList.clear();
        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT customerId, customerName, address, city, postalCode, phone "
                    + "FROM customer, address, city "
                    + "WHERE customer.addressId = address.addressId AND address.cityId = city.cityId  "
                    + "ORDER BY customerId;";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
               CustomerList.add(new Customer(result.getInt("customerId"),
                                             result.getString("customerName"),
                                             result.getString("address"),
                                             result.getString("city"),
                                             result.getString("postalCode"),
                                             result.getString("phone")));
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }
            customerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            address.setCellValueFactory(new PropertyValueFactory<>("address"));
            city.setCellValueFactory(new PropertyValueFactory<>("city"));
            postalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
            phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
            CustomerTable.setItems(CustomerList);
            CustomerTable.refresh();
    }
     private void PopulateDropDowns() {
        //appointment time populate
        LocalTime time1 = LocalTime.of(9, 0);
        DropDownTimeList.clear();
        for (int i = 0; i < 32; ++i){
            DropDownTimeList.add(time1);
            time1 = time1.plusMinutes(15);
        }
        StartTimeDropDown.setItems(DropDownTimeList);
        
        //duration populate
        
        LocalTime duration1 = LocalTime.of(0, 15);
        DropDownDurationList.clear();
        for (int i = 0; i < 4; ++i){
            DropDownDurationList.add(duration1);
            duration1 = duration1.plusMinutes(15);
        }
        AppointmentDurationDropDown.setItems(DropDownDurationList);  
     }

}