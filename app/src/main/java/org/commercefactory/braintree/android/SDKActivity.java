package org.commercefactory.braintree.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.AndroidPayCardNonce;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.PostalAddress;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class SDKActivity extends Activity implements PaymentMethodNonceCreatedListener, BraintreeErrorListener {

    private static final String SERVER_BASE = "http://192.168.0.16:80/braintree/braintreetest.php"; // Replace with your own server
    //http://macride.ca/braintree/braintreetest.php
    private static final int REQUEST_CODE = Menu.FIRST;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String clientToken;
    private BraintreeFragment mBraintreeFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk);
        getToken();

    }

    //region Action Methods
    public void onStartClick(View view) {
        CardBuilder cardBuilder = new CardBuilder()
                .cardNumber("4111111111111111")
                .expirationMonth("11")
                .expirationYear("15");

        Card.tokenize(mBraintreeFragment, cardBuilder);

    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                    PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(
                            BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE
                    );
                    String nonce = paymentMethodNonce.getNonce();
                    RequestParams requestParams = new RequestParams();
                    requestParams.put("payment_method_nonce", nonce);
                    requestParams.put("amount", "10.00");
                    sendNonceToServer(requestParams);
                    break;
                }
                case BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR: {
                    String error = data.getStringExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE);
                    Log.e("error is", error);
                    break;
                }
                case BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR:
                case BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_UNAVAILABLE: {
                    // handle errors here, a throwable may be available in
                    String error = data.getStringExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE);
                    //Log.e("e",error);
                    break;
                }
                default:
                    break;
            }
        }
    }

    //region Custom Methods
    private void getToken() {
        if (Apputility.isNetConnected(this)) {
            client.get(SERVER_BASE + "?action=token", new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    findViewById(R.id.btn_start).setEnabled(false);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    clientToken = responseString.trim();
                    Log.d("token", clientToken);
                    findViewById(R.id.btn_start).setEnabled(true);
                    try {
                        mBraintreeFragment = BraintreeFragment.newInstance(SDKActivity.this, clientToken);
                        // mBraintreeFragment is ready to use!
                    } catch (InvalidArgumentException e) {
                        Toast.makeText(SDKActivity.this, "Oops..something wrong with token here", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(SDKActivity.this, Apputility.checkNwConn, Toast.LENGTH_LONG).show();
        }

    }

    private void sendNonceToServer(RequestParams requestParams) {
        if (Apputility.isNetConnected(this)) {
            client.post(SERVER_BASE + "?action=payment", requestParams, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(SDKActivity.this, responseString, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Toast.makeText(SDKActivity.this, responseString, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(SDKActivity.this, Apputility.checkNwConn, Toast.LENGTH_LONG).show();
        }

    }
    //endregion

    //region Braintree listeners
    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        String nonce = paymentMethodNonce.getNonce();

        if (paymentMethodNonce instanceof PayPalAccountNonce) {
            PostalAddress shippingAddress = ((PayPalAccountNonce) paymentMethodNonce).getShippingAddress();
            // ...
        } else {
            if (paymentMethodNonce instanceof AndroidPayCardNonce) {
                String lastTwo = ((AndroidPayCardNonce) paymentMethodNonce).getLastTwo();
                // ...
            } else {
                if (paymentMethodNonce instanceof CardNonce) {
                    String cardType = ((CardNonce) paymentMethodNonce).getCardType();
                    RequestParams requestParams = new RequestParams();
                    requestParams.put("payment_method_nonce", nonce);
                    requestParams.put("amount", "10.00");
                    requestParams.put("cardtype", cardType);
                    sendNonceToServer(requestParams);
                }
            }
        }
    }

    @Override
    public void onError(Exception error) {
        if (error instanceof ErrorWithResponse) {
            // there was a validation error the user provided data
        }
    }
    //endregion

}
