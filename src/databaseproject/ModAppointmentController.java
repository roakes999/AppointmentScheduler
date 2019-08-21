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
import static databaseproject.MainScreenController.selectedAppointment;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Rob
 */
public class ModAppointmentController implements Initializable {

    ObservableList<LocalTime> DropDownTimeList = FXCollections.observableArrayList();
    ObservableList<LocalTime> DropDownDurationList = FXCollections.observableArrayList();
    static String consultant;
    
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
        PopulateDropDowns();
        PopulateCustomerTable();
        
        // Prefills all fields   
        int selectedID = selectedAppointment.get(0).getAppointmentID();
        String sqlStatement;
        Statement stmt;
        
        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT start, end, createdBy "
                    + "FROM appointment "
                    + "WHERE appointmentId = " + selectedID + ";";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
                consultant = result.getString("createdBy"); // gets assigned consultant (modify will not change consultant assigned to customer)
                // convert UTC to users time zone for start and end times
                // start time
                Timestamp startTimestamp = Timestamp.valueOf(result.getString("start"));
                LocalDateTime startLocalDateTime = startTimestamp.toLocalDateTime();
                ZonedDateTime startZonedDateTimeUTC = ZonedDateTime.of(startLocalDateTime, ZoneId.of("UTC"));
                ZonedDateTime startDateTimeZonedAdjusted = startZonedDateTimeUTC.withZoneSameInstant(ZoneId.systemDefault());
                //end time
                Timestamp endTimestamp = Timestamp.valueOf(result.getString("end"));
                LocalDateTime endLocalDateTime = endTimestamp.toLocalDateTime();
                ZonedDateTime endZonedDateTimeUTC = ZonedDateTime.of(endLocalDateTime, ZoneId.of("UTC"));
                ZonedDateTime endDateTimeZonedAdjusted = endZonedDateTimeUTC.withZoneSameInstant(ZoneId.systemDefault());
                
                DateChooser.setValue(startDateTimeZonedAdjusted.toLocalDate());
                StartTimeDropDown.setValue(startDateTimeZonedAdjusted.toLocalTime());
                
                // determine duration from start and end time
                LocalTime start1 = startDateTimeZonedAdjusted.toLocalTime();
                LocalTime end1 = endDateTimeZonedAdjusted.toLocalTime();
                int startTotal = start1.getHour()*60 + start1.getMinute();
                int endTotal = end1.getHour()*60 + end1.getMinute();
                int durationInt = endTotal - startTotal;
                LocalTime duration = LocalTime.of(durationInt / 60, durationInt % 60);
                AppointmentDurationDropDown.setValue(duration);
                               
                //PreSelect customer in table
                int matchedCustomerID = -1;
                sqlStatement = "SELECT customerId "
                    + "FROM appointment "
                    + "WHERE appointmentId = " + selectedID + ";";
                result = stmt.executeQuery(sqlStatement);
                while (result.next()){
                    matchedCustomerID = result.getInt("customerId");
                }
                for (int i = 0; i < CustomerList.size(); ++i){
                    if (CustomerList.get(i).getCustomerID() == matchedCustomerID) {
                       CustomerTable.getSelectionModel().select(i);
                    }
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }   
        
        
    }    

    @FXML
    private void ModifyAppointmentBtnHandler(ActionEvent event) throws IOException {

        String sqlStatement;
        Statement stmt;
        
        if (DateChooser.getValue() == null || StartTimeDropDown.getValue() == null || AppointmentDurationDropDown.getValue() == null){ // alert for empty user fields
            Alert missingFields = new Alert(AlertType.INFORMATION);
            missingFields.setTitle("Missing Information");
            missingFields.setHeaderText("Please Make a Selection For All Fields");
            missingFields.showAndWait();
        }
        else if (DateChooser.getValue().getDayOfWeek() == SATURDAY | DateChooser.getValue().getDayOfWeek() == SUNDAY){ // alert for appointment made on weekend (9 - 5 business hours is handled seperately with a time selection drop down)
            Alert missingFields = new Alert(AlertType.INFORMATION);
            missingFields.setTitle("Invalid Selection");
            missingFields.setHeaderText("Appointments Cannot Be Made On Weekends");
            missingFields.showAndWait();
        }
        else if (CustomerTable.getSelectionModel().getSelectedItems().isEmpty()) { //  alert for not selecting a customer
            Alert missingCustomer = new Alert(AlertType.INFORMATION);
            missingCustomer.setTitle("Missing Information");
            missingCustomer.setHeaderText("Please Select a Customer");
            missingCustomer.showAndWait();
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
            ZonedDateTime startDateTimeZonedUTC = startDateTimeZoned.withZoneSameInstant(ZoneId.of("UTC"));
            ZonedDateTime endDateTimeZoned = ZonedDateTime.of(endDateTime, ZoneId.systemDefault());
            ZonedDateTime endDateTimeZonedUTC = endDateTimeZoned.withZoneSameInstant(ZoneId.of("UTC"));

            // Checks for overlapping appointments
            boolean overlappingAppointmentCheck = false;
            try {
                stmt = conn.createStatement();
                sqlStatement = "SELECT start, end, createdBy FROM appointment;";
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
                    if ((startDateTimeZonedUTC.isAfter(startZonedDateTimeUTCDB)) & (startDateTimeZonedUTC.isBefore(endZonedDateTimeUTCDB)) & consultant.equals(consultantFromDB)) { 
                        overlappingAppointmentCheck = true;                     
                    }
                    if ((endDateTimeZonedUTC.isAfter(startZonedDateTimeUTCDB)) & (endDateTimeZonedUTC.isBefore(endZonedDateTimeUTCDB)) & consultant.equals(consultantFromDB)) {
                        overlappingAppointmentCheck = true;
                    }
                    if ((startDateTimeZonedUTC.isBefore(startZonedDateTimeUTCDB)) & (endDateTimeZonedUTC.isAfter(endZonedDateTimeUTCDB)) & consultant.equals(consultantFromDB)) {
                        overlappingAppointmentCheck = true;
                    }
                    if (((startDateTimeZonedUTC.isEqual(startZonedDateTimeUTCDB)) | (endDateTimeZonedUTC.isEqual(endZonedDateTimeUTCDB))) & consultant.equals(consultantFromDB)) {
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
                    sqlStatement = "UPDATE appointment "
                        + "SET customerId = " + customerId + ", start = '" + startDateTimeZonedUTC + "', end = '" + endDateTimeZonedUTC + "', lastUpdateBy = '" + currentUser + "' " 
                        + "WHERE appointmentId = " + selectedAppointment.get(0).getAppointmentID() + ";";
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