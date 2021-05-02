package com.example.foodliapp.Model;

public class User {
    private String Name;
    private  String Password;
    private String Phone;
    private String isStaff;
    private String secureCode;

    private Object balance;

    public Object getBalance() {
        return balance;
    }

    public void setBalance(Object balance) {
        this.balance = balance;
    }


    public User() {

    }

    public User(String name, String password, String secureCode) {
        Name = name;
        Password = password;
        isStaff = "false";
        this.secureCode = secureCode;


    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
    public String getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(String isStaff) {
        this.isStaff = isStaff;
    }

}
