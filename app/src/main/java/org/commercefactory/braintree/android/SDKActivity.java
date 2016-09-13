package org.commercefactory.braintree.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class SDKActivity extends Activity {
    private static final String SERVER_BASE = "http://192.168.0.18:80/braintree/braintreetest.php"; // Replace with your own server
    //http://macride.ca/braintree/braintreetest.php
    private static final int REQUEST_CODE = Menu.FIRST;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String clientToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk);
        getToken();
    }

    //region Action Methods
    public void onStartClick(View view) {
        if (clientToken != null && clientToken.length() > 0) {
            PaymentRequest paymentRequest = new PaymentRequest()
                    .clientToken(clientToken);
//                .amount("$10.00")
//                .primaryDescription("Awesome payment")
//                .secondaryDescription("Using the Client SDK")
//                .submitButtonText("Pay");

            startActivityForResult(paymentRequest.getIntent(this), REQUEST_CODE);
        } else {
            Toast.makeText(this, "Client token in null", Toast.LENGTH_LONG).show();
        }

    }
    //endregion

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_CODE) {
//            if (resultCode == BraintreePaymentActivity.RESULT_OK) {
//                PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
//
//                RequestParams requestParams = new RequestParams();
//                requestParams.put("payment_method_nonce", paymentMethodNonce.getNonce());
//                requestParams.put("amount", "10.00");
//
//                client.post(SERVER_BASE + "?action=payment", requestParams, new TextHttpResponseHandler() {
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        Toast.makeText(SDKActivity.this, responseString, Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                        Toast.makeText(SDKActivity.this, responseString, Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//
//        }
//
//    }

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
            }
        });
    }

    private void sendNonceToServer(RequestParams requestParams) {
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
    }
    //endregion
}
