package com.rilixtech.countrycodepicker.sampletv;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class MainActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    CountryCodePicker ccp = findViewById(R.id.ccp);
    EditText edtPhone = findViewById(R.id.phone_number_edt);

    ccp.registerPhoneNumberTextView(edtPhone);
  }
}
