package com.andela.payonly;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    private static final String CONFIG_CLIENT_ID = "Aa7l9ynGKh_V3MBKXqZjynzjvtH7mYQw0IVONqhdr6Asvy_aiOCzX-ahx15yawkO3E_JNn_R_dcbla_G";

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;

    private static final int REQUEST_CODE_PAYMENT = 1;
    public static final double AMOUNT = 10.00;

    private static PayPalConfiguration config = new PayPalConfiguration()
        .environment(CONFIG_ENVIRONMENT)
        .clientId(CONFIG_CLIENT_ID);

        PayPalPayment thingToBuy;
        Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

    }

    public void makePayment(View view) {
        thingToBuy = new PayPalPayment(new BigDecimal(AMOUNT), "USD",
                "HeadSet", PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(MainActivity.this,
                PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        JSONObject responseObject = confirm.toJSONObject().getJSONObject("response");
                        String paymentID = responseObject.get("id").toString();
                        Log.d("Sauda","paymment id = " + paymentID);

                        // confirm Payment
                        PayPalConfirmation confirmation = new PayPalConfirmation(paymentID, AMOUNT, this);
                        Log.d("Suada", "payPalConfirmation is " + confirmation.toString());

                        confirmation.confirmPayment(new PayPalConfirmation.ConfirmationCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("Suada", "Payment was successful");

                                Toast.makeText(context, "Payment Succesful", Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onFailure() {
                                Log.d("Suada", "Payment Failed");

                                Toast.makeText(context, "Payment Failed", Toast.LENGTH_LONG).show();
                            }
                        });
                        /*System.out.println(confirm.toJSONObject().toString(4));
                        System.out.println(confirm.getPayment().toJSONObject()
                                .toString(4));
                        Toast.makeText(getApplicationContext(), "Order placed",
                                Toast.LENGTH_LONG).show();*/

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                System.out.println("The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                System.out.println("An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    @Override
    public void onDestroy() {
        // Stop service when done
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
   }
