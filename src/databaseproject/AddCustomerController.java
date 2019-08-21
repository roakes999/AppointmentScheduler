/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databaseproject;

import static databaseproject.DBConnection.conn;
import static databaseproject.LoginController.currentUser;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Rob
 */
public class AddCustomerController implements Initializable {
    public static ObservableList<String> CountryList = FXCollections.observableArrayList();
    public static ObservableList<String> CityList = FXCollections.observableArrayList();

    @FXML
    private ComboBox<String> CountryDropDown;
    @FXML
    private ComboBox<String> CityDropDown;
    @FXML
    private TextField CustomerNameField;
    @FXML
    private TextField PhoneField;
    @FXML
    private TextField AddressField;
    @FXML
    private TextField PostalCodeField;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LoadCountryDropDown();
    }    

    private void LoadCountryDropDown() {
        CountryList.clear();
        String sqlStatement;
        Statement stmt;
        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT country FROM country;";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
               CountryList.add(result.getString("country"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }
        CountryDropDown.setItems(CountryList);
    }
    
    @FXML
    void CountryDropDownHandler(ActionEvent event) {
        // Populates city drop down with cities that match country selected
        CityList.clear();
        String sqlStatement;
        Statement stmt;
        String selectedCountry = CountryDropDown.getValue();
        try {
            stmt = conn.createStatement();
            sqlStatement = "Select city from city, country where country.country = \"" + selectedCountry + "\" AND country.countryId = city.countryID;";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
               CityList.add(result.getString("city"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }
        CityDropDown.setItems(CityList);
    }
    
    @FXML
    void AddCustomerRecordBtnHandler(ActionEvent event) throws IOException {
        
        if (CustomerNameField.getText()== null || CountryDropDown.getValue() == null || AddressField.getText()== null || CityDropDown.getValue() == null 
                || PostalCodeField.getText()== null || PhoneField.getText()== null){
            Alert missingFields = new Alert(Alert.AlertType.INFORMATION);
            missingFields.setTitle("Missing Information");
            missingFields.setContentText("Please Make a Selection For All Fields");
            missingFields.showAndWait();
        }
        else {
            try {
                String userCustomerName = CustomerNameField.getText();
                String userCountry = CountryDropDown.getValue();
                String userAddress = AddressField.getText();
                String userCity = CityDropDown.getValue();
                String userPostalCode = PostalCodeField.getText();
                String userPhone = PhoneField.getText();
                      
                String sqlStatement;
                Statement stmt;

                stmt = conn.createStatement();
                System.out.println(currentUser);
                sqlStatement = "INSERT INTO address (cityId, address, postalCode, phone, createdBy, lastUpdateBy, createDate) "
                        + "VALUES ((SELECT cityId FROM city WHERE \"" + userCity + "\" = city.city), \"" + userAddress + "\", \"" + userPostalCode + "\", \"" + userPhone +"\", \"" + currentUser + "\", \"" + currentUser + "\", (SELECT NOW()));";
                stmt.executeUpdate(sqlStatement);

                sqlStatement = "INSERT INTO customer (customerName, addressId, createDate, createdBy, lastUpdateBy) "
                        + "VALUES (\"" + userCustomerName + "\", (SELECT addressId FROM address WHERE address.address = \"" + userAddress + "\"), (SELECT NOW()), \"" + currentUser + "\", \"" + currentUser + "\");";
                stmt.executeUpdate(sqlStatement);

            } catch (SQLException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error: " + ex);
            }
            catch (IllegalArgumentException e) {  // checks for invalid input data
                System.out.print(e);
            }
            Parent root = FXMLLoader.load(getClass().getResource("CustomerRecords.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        }
    }
    
    @FXML
    void CancelBtnHandler(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("CustomerRecords.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    
}
