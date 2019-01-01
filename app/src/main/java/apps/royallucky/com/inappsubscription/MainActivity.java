package apps.royallucky.com.inappsubscription;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {


    static final String SKU_WEEK_1 = "sub_1week";
    static String SKU_MESSAGE = "";

    Boolean isFirstLogin = true, isNewPurchase = false,Subscription = false;

    String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3AQsanmrDgJRk3Fy5+/Ey1M3aJZ+txqt99ArQRCf5ZrGqW+dCSVUn1e4Rvxj6Gi3F/a+qGor6vSSSjS5dyViVesHVR/YNOOwBkEgLU1s4TlpWfDN9aWqGXpHkydw3O61cefwEBZ8KB2MXggAWcbVzw0X5JFhPmV6gpxxNC+pSb68M2rzdJBFdreAVbokfYjPmoU+RaOQd0tfFF2ob0vs4RAWAR+6LjyytCrwWvFHR1sUr75eq+l7ILam5E7oO8bZ1HYdKtfq3c9cgxfCw6u2bldM19ZZGOT4gAcZcoBsa0G+ZwOVhj/MiSAdrD295bnXwnysVzWs+nKCnh73+HGvMQIDAQAB";
    Purchase purchase;
    Purchase.PurchasesResult purchasesResult;
    BillingClient mBillingClient;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    long expiry_time = 0;

    String TAG_FIRST_LOGIN = "flogin", TAG_EXPIRY_TIME = "exp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.btn);

        sharedPreferences = getSharedPreferences("subs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        isFirstLogin = sharedPreferences.getBoolean("", true);
        expiry_time = Long.parseLong(sharedPreferences.getString(TAG_EXPIRY_TIME, "0"));

        mBillingClient = BillingClient.newBuilder(this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {

            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Subscription)
                {
                    Toast.makeText(MainActivity.this, " Subscription Active ",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    makeSubsription();
                    checkPurchases();
                }
            }
        });
       // checkPurchases();
    }

    public void makeSubsription() {

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(SKU_WEEK_1)
                .setType(BillingClient.SkuType.SUBS)
                .build();
        int responseCode = mBillingClient.launchBillingFlow(MainActivity.this, flowParams);


    }

    private void checkPurchases() {
        try {
            purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS);
            Log.e("aaa", "" + purchasesResult.getPurchasesList().size());
            if (purchasesResult != null && purchasesResult.getPurchasesList().size() > 0) {
                purchase = purchasesResult.getPurchasesList().get(0);
                handlePurchase(purchasesResult.getPurchasesList().get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (com.android.billingclient.api.Purchase purchase : purchases) {
                isNewPurchase = true;
                this.purchase = purchase;
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private void handlePurchase(com.android.billingclient.api.Purchase purchase) {

        this.purchase = purchase;
        if (verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
            switch (purchase.getSku()) {
                case SKU_WEEK_1:
                    SKU_MESSAGE = "1 Week";
                    //Constant.isAdFree = true;
                    break;
            }
            if (isNewPurchase) {
                isNewPurchase = false;
                purchaseDialog();
            }


            Toast.makeText(this, " Purchased Successfully..!! ",
                    Toast.LENGTH_LONG).show();

        }
    }

    private boolean verifyValidSignature(String signedData, String signature) {
        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please update your app's public key at: "
                    + "BASE_64_ENCODED_PUBLIC_KEY");
        }

        try {
            return Security.verifyPurchase(base64EncodedPublicKey, signedData, signature);
        } catch (Exception e) {

            return false;
        }
    }

    private void purchaseDialog() {

        Subscription = true;

    }
}
