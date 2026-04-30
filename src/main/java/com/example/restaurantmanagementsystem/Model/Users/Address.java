package com.example.restaurantmanagementsystem.Model.Users;

public class Address {
    private int id;
    private String street;
    private String city;
    private String district;
    private String postalCode;
    private String country;

    public Address() {
    }

    public Address(int id, String street, String city, String district, String postalCode, String country) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }
}
