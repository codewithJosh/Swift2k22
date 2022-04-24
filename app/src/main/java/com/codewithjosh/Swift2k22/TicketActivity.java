package com.codewithjosh.Swift2k22;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TicketActivity extends AppCompatActivity {

    ImageView _barcode;
    TextView _busFare, _totalAmt, _ref, _busTimestamp;

    String bus_fare, bus_id, ticket_id;

    FirebaseFirestore firebaseFirestore;

    DateFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        _barcode = findViewById(R.id.barcode);
        _busFare = findViewById(R.id.bus_fare);
        _totalAmt = findViewById(R.id.total_amt);
        _ref = findViewById(R.id.ref);
        _busTimestamp = findViewById(R.id.bus_timestamp);

        bus_fare = getIntent().getStringExtra("bus_fare");
        bus_id = getIntent().getStringExtra("bus_id");
        ticket_id = getIntent().getStringExtra("ticket_id");

        firebaseFirestore = FirebaseFirestore.getInstance();

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorBlueJeans));
        getWindow().setStatusBarColor(ContextCompat.getColor(TicketActivity.this, R.color.colorBlueJeans));

        _busFare.setText("PHP " + bus_fare + ".00");
        _totalAmt.setText("PHP " + bus_fare + ".00");
        _ref.setText("Ref No: " + ticket_id.toUpperCase());

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore
                .collection("Buses")
                .document(bus_id)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        DocumentSnapshot doc = task.getResult();
                        formatter = new SimpleDateFormat("dd MMMM yyyy h:mm a");
                        _busTimestamp.setText(formatter.format(doc.getDate("bus_timestamp")));
                    }
                });

        firebaseFirestore
                .collection("Buses")
                .document(bus_id)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        DocumentSnapshot doc = task.getResult();
                        formatter = new SimpleDateFormat("dd MMMM yyyy h:mm a");
                        _busTimestamp.setText(formatter.format(doc.getDate("bus_timestamp")));
                    }
                });

        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            // Initialize bit matrix
            BitMatrix matrix = writer.encode(ticket_id, BarcodeFormat.CODE_128, 400, 100);
            //initialize barcode encoder
            BarcodeEncoder encoder = new BarcodeEncoder();
            //Initialize Bitmap
            Bitmap bitmap = encoder.createBitmap(matrix);
            //set bitmap on imageview
            _barcode.setImageBitmap(bitmap);
            // initialize input manager
            InputMethodManager manager = (InputMethodManager)  getSystemService(
                    Context.INPUT_METHOD_SERVICE
            );
            // hide soft keyboard
            // manager.hideSoftInputFromWindow(etInput.getApplicationWindowToken(),0);
        }
        catch (WriterException e){
            e.printStackTrace();
        }

    }

}