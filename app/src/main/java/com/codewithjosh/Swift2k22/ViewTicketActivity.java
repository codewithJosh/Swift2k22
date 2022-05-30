package com.codewithjosh.Swift2k22;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ViewTicketActivity extends AppCompatActivity
{

    ImageView iv_barcode;
    TextView tv_bus_fare;
    TextView tv_total_amount;
    TextView tv_ticket_id;
    TextView tv_bus_timestamp;
    int i_bus_fare;
    String s_ticket_id;
    String s_future_bus_timestamp;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ticket);

        initViews();
        initInstances();
        initSharedPref();
        load();

    }

    private void initViews()
    {

        iv_barcode = findViewById(R.id.iv_barcode);
        tv_bus_fare = findViewById(R.id.tv_bus_fare);
        tv_total_amount = findViewById(R.id.tv_total_amount);
        tv_ticket_id = findViewById(R.id.tv_ticket_id);
        tv_bus_timestamp = findViewById(R.id.tv_bus_timestamp);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorBlueJeans));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBlueJeans));

    }

    private void initInstances()
    {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref()
    {

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);

    }

    private void load()
    {

        s_ticket_id = sharedPref.getString("s_ticket_id", String.valueOf(Context.MODE_PRIVATE));
        s_future_bus_timestamp = sharedPref.getString("s_future_bus_timestamp", String.valueOf(Context.MODE_PRIVATE));
        i_bus_fare = sharedPref.getInt("i_bus_fare", Context.MODE_PRIVATE);

        final String s_bus_fare = "PHP " + i_bus_fare + ".00";
        final String _s_ticket_id = "Ref No: " + s_ticket_id.toUpperCase();

        tv_bus_fare.setText(s_bus_fare);
        tv_total_amount.setText(s_bus_fare);
        tv_ticket_id.setText(_s_ticket_id);
        tv_bus_timestamp.setText(s_future_bus_timestamp);

        MultiFormatWriter writer = new MultiFormatWriter();

        try
        {

            BitMatrix matrix = writer.encode(s_ticket_id, BarcodeFormat.CODE_128, 400, 100);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            iv_barcode.setImageBitmap(bitmap);

        }
        catch (WriterException e)
        {

            e.printStackTrace();

        }

    }

}