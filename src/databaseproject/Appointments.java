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
public class Appointments {
    int appointmentID;
    String day;
    String start;
    String end;
    String customerName;
    String userName;
    
    public Appointments (int appointmentID, String day, String start, String end, String customerName, String userName){
        setAppointmentID(appointmentID);
        setDay(day);
        setStart(start);
        setEnd(end);
        setCustomerName(customerName);
        setUserName(userName);
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }
    public void setDay(String day) {
        this.day = day;
    }
    public void setStart(String start) {
        this.start = start;    }

    public void setEnd(String end) {
        this.end = end;    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;    }

    public void setUserName(String userName) {
        this.userName = userName;    }
    
    
    public int getAppointmentID(){
        return appointmentID;
    }
    public String getDay(){
        return day;
    }
    public String getStart(){
        return start;
    }
    public String getEnd(){
        return end;
    }
    public String getCustomerName(){
        return customerName;
    }    
    public String getUserName(){
        return userName;
    }

}
