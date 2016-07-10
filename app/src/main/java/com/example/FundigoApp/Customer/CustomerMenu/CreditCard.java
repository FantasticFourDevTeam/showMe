package com.example.FundigoApp.Customer.CustomerMenu;

import com.parse.ParseClassName;
import com.parse.ParseObject;


@ParseClassName("creditCards")
public class CreditCard extends ParseObject
{
    public String getIdCostumer() {
        return getString ("IdCostumer");
    }

    public void setIdCustomer(String Id) {
        put ("Id", Id);
    }

    public String getCreditCardNumber() {
        return getString("number");
    }

    public void setCreditCardNumber(String creditCardNumber) {
        put ("number", creditCardNumber);
    }
}
