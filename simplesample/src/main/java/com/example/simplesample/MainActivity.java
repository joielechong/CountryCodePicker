package com.example.simplesample;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    final CountryCodePicker ccp = findViewById(R.id.ccp);
    final EditText edtPhone = findViewById(R.id.phone_number_edt);

    ccp.registerPhoneNumberTextView(edtPhone);

    Button btnReset = findViewById(R.id.reset_btn);
    btnReset.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        ccp.registerPhoneNumberTextView(edtPhone);
      }
    });
  }
}
