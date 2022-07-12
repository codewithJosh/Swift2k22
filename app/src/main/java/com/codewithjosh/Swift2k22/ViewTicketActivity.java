package com.codewithjosh.Swift2k22;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ViewTicketActivity extends AppCompatActivity {

    ImageView ivBarcode;
    TextView tvBusFare;
    TextView tvTotalAmount;
    TextView tvTicketId;
    TextView tvBusTimestamp;
    int busFare;
    String ticketId;
    String futureBusTimestamp;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ticket);

        initViews();
        initInstances();
        initSharedPref();
        load();

    }

    private void initViews() {

        getWindow().setNavigationBarColor(getResources().getColor(R.color.color_blue_jeans));
        getWindow().setStatusBarColor(getResources().getColor(R.color.color_blue_jeans));

        ivBarcode = findViewById(R.id.iv_barcode);
        tvBusFare = findViewById(R.id.tv_bus_fare);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvTicketId = findViewById(R.id.tv_ticket_id);
        tvBusTimestamp = findViewById(R.id.tv_bus_timestamp);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);

    }

    private void load() {

        ticketId = sharedPref.getString("ticket_id", String.valueOf(Context.MODE_PRIVATE));
        futureBusTimestamp = sharedPref.getString("future_bus_timestamp", String.valueOf(Context.MODE_PRIVATE));
        busFare = sharedPref.getInt("bus_fare", Context.MODE_PRIVATE);

        final String _busFare = "PHP " + busFare + ".00";
        final String _ticketId = "Ref No: " + ticketId.toUpperCase();

        tvBusFare.setText(_busFare);
        tvTotalAmount.setText(_busFare);
        tvTicketId.setText(_ticketId);
        tvBusTimestamp.setText(futureBusTimestamp);

        final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {

            final BitMatrix bitMatrix = multiFormatWriter.encode(ticketId, BarcodeFormat.CODE_128, 400, 100);
            final BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            final Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ivBarcode.setImageBitmap(bitmap);

        } catch (WriterException e) {

            e.printStackTrace();

        }

    }

}