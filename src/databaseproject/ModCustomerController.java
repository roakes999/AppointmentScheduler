/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databaseproject;

import static databaseproject.CustomerRecordsController.selectedCustomer;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Rob
 */
public class ModCustomerController implements Initializable {
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
        // Prefill fields
        
        int selectedID = selectedCustomer.get(0).getCustomerID();
        
        String sqlStatement;
        Statement stmt;
        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT customerName, address, postalCode, phone, city, country "
                    + "FROM customer, address, city, country "
                    + "WHERE customerId = " + selectedID + " AND customer.addressId = address.addressId AND address.cityId = city.cityId AND city.countryId = country.countryId;";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
                CustomerNameField.setText(result.getString("customerName"));
                PhoneField.setText(result.getString("phone"));
                AddressField.setText(result.getString("address"));
                PostalCodeField.setText(result.getString("postalCode"));
                CountryDropDown.setValue(result.getString("country"));
                CityDropDown.setValue(result.getString("city"));
                LoadCountryDropDown();
                CityDropDownPopulate();
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }        
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
        CityDropDownPopulate();
    }
    
    private void CityDropDownPopulate(){
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
    void SaveBtnHandler(ActionEvent event) throws IOException {
        String userCustomerName = CustomerNameField.getText();
        String userCountry = CountryDropDown.getValue();
        String userAddress = AddressField.getText();
        String userCity = CityDropDown.getValue();
        String userPostalCode = PostalCodeField.getText();
        String userPhone = PhoneField.getText();
        
        String sqlStatement;
        Statement stmt;

        try {
            stmt = conn.createStatement();
            System.out.println(currentUser);

            sqlStatement = "UPDATE address, customer "
                    + "SET cityId = (SELECT cityId FROM city WHERE \"" + userCity + "\" = city.city), address = \"" + userAddress + "\", postalCode = \"" + userPostalCode + "\", phone = \"" + userPhone + "\", address.lastUpdateBy = \"" + currentUser + "\" "
                    + "WHERE address.addressID = customer.addressId AND customer.customerId = " + selectedCustomer.get(0).getCustomerID() + ";";
                    
            stmt.executeUpdate(sqlStatement);
      
            sqlStatement = "UPDATE customer "
                    + "SET customerName = \"" + userCustomerName + "\", addressId = (SELECT addressId FROM address WHERE address.address = \"" + userAddress + "\"), lastUpdateBy = \"" + currentUser + "\" "
                    + "WHERE customerId = " + selectedCustomer.get(0).getCustomerID() + ";";
            stmt.executeUpdate(sqlStatement);
            
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }
        
        Parent root = FXMLLoader.load(getClass().getResource("CustomerRecords.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
        
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