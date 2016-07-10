package com.example.FundigoApp.Customer.CustomerMenu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.devmarvel.creditcardentry.library.CardValidCallback;
import com.devmarvel.creditcardentry.library.CreditCard;
import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.parse.ParseException;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.StripeException;

public class SaveCreditCard extends AppCompatActivity implements View.OnClickListener {

    Button saveCardButton;
    private CreditCardForm noZipForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_save_credit_card);
        saveCardButton = (Button) findViewById (R.id.saveCrditCard_buttonApplay);
        saveCardButton.setVisibility (View.INVISIBLE);
        noZipForm = (CreditCardForm) findViewById (R.id.form_no_zip_SaveCard);
        noZipForm.setOnCardValidCallback (cardValidCallback);
    }

    CardValidCallback cardValidCallback = new CardValidCallback () {
        @Override
        public void cardValid(CreditCard card) {
            saveCardButton.setVisibility (View.VISIBLE);
            Toast.makeText (SaveCreditCard.this, R.string.card_valid_and_complete, Toast.LENGTH_SHORT).show ();
        }
    };

    @Override
    public void onClick(View v) {
        Boolean flag = true;
        if (v.getId () == saveCardButton.getId ()) {
            Card card = new Card (noZipForm.getCreditCard ().getCardNumber (), noZipForm.getCreditCard ().getExpMonth (), noZipForm.getCreditCard ().getExpYear (), noZipForm.getCreditCard ().getSecurityCode ());
            if (!card.validateCard ()) {
                Toast.makeText (this, "unValidateCard", Toast.LENGTH_SHORT).show ();
                flag = false;
            } else if (!card.validateCVC ()) {
                Toast.makeText (this, "unValidateCVC", Toast.LENGTH_SHORT).show ();
                flag = false;
            } else if (!card.validateExpMonth ()) {
                Toast.makeText (this, "unValidateMoth", Toast.LENGTH_SHORT).show ();
                flag = false;
            } else if (!card.validateExpYear ()) {
                Toast.makeText (this, "unValidateYear", Toast.LENGTH_SHORT).show ();
                flag = false;
            } else if (!card.validateExpiryDate ()) {
                Toast.makeText (this, "unValidateDate", Toast.LENGTH_SHORT).show ();
                flag = false;
            }
            if (flag) {
                try {
                    final Stripe stripe = new Stripe ("pk_test_YyMy1mvHItPsHftS4iKcoO3O");
                    stripe.createToken (card, new TokenCallback () {
                                public void onSuccess(Token token) {
                                    token.getCard ().setNumber (noZipForm.getCreditCard ().getCardNumber ());
                                    // Send token to your server
                                    com.example.FundigoApp.Customer.CustomerMenu.CreditCard creditCard =
                                            new com.example.FundigoApp.Customer.CustomerMenu.CreditCard ();

                                    try {
                                        creditCard.put ("IdCostumer", GlobalVariables.CUSTOMER_PHONE_NUM);
                                        creditCard.put ("number", token.getCard ().getNumber ());
                                        creditCard.put ("month", 0);
                                        creditCard.put ("year", 0);
                                        creditCard.put ("cvc", "XXX");
                                        creditCard.save ();
                                    } catch (ParseException e) {
                                        e.printStackTrace ();
                                    }
                                    Toast.makeText (SaveCreditCard.this, "Success card save!", Toast.LENGTH_LONG).show ();
                                    finish ();
                                }

                                public void onError(Exception error) {
                                    // Show localized error message
                                    error.printStackTrace ();
                                    Toast.makeText (SaveCreditCard.this, error.getMessage (), Toast.LENGTH_LONG).show ();
                                }
                            }
                    );
                } catch (StripeException e) {
                    e.printStackTrace ();
                }
            }
        }
    }
}
