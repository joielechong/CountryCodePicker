package com.rilixtech.countrycodepicker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.rilixtech.CountryCodePicker;

public class PerformClickActivity extends AppCompatActivity {


  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_perform_click);

    final CountryCodePicker ccp = findViewById(R.id.ccp);
    Button btnPerformClick = findViewById(R.id.perform_click_btn);
    btnPerformClick.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        ccp.performClick();
      }
    });
  }
}
