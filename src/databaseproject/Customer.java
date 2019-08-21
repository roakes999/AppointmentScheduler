/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databaseproject;

/**
 *
 * @author Rob
 */
public class Customer {
    int customerID;
    String customerName;
    String address;
    String city;
    String postalCode;
    String phone;
    
    public Customer (int customerID, String customerName, String address, String city, String postalCode, String phone){
        setCustomerID(customerID);
        setCustomerName(customerName);
        setAddress(address);
        setCity(city);
        setPostalCode(postalCode);
        setPhone(phone);
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public int getCustomerID(){
        return customerID;
    }
    public String getCustomerName(){
        return customerName;
    }
    public String getAddress(){
        return address;
    }
    public String getCity(){
        return city;
    }
    public String getPostalCode(){
        return postalCode;
    }
    public String getPhone(){
        return phone;
    }
}
