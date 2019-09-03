package com.rilixtech.countrycodepicker;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rilixtech.widget.countrycodepicker.CountryCodePicker;


/**
 * A simple {@link Fragment} subclass.
 */
public class DefaultCountryFragment extends Fragment {

    public static final String TAG = DefaultCountryFragment.class.getSimpleName();

    EditText editTextDefaultPhoneCode, edtDefaultNameCode;
    Button buttonSetNewDefaultPhoneCode, btnSetNewDefaultNameCode, buttonResetToDefault;
    CountryCodePicker ccp;
    Button buttonNext;

    public DefaultCountryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_default_country, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assignViews();
        editTextWatcher();
        addClickListeners();
    }

    private void addClickListeners() {
        buttonSetNewDefaultPhoneCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int code = Integer.parseInt(editTextDefaultPhoneCode.getText().toString());
                    ccp.setDefaultCountryUsingPhoneCode(code);
                    Toast.makeText(getActivity(), "Now default country is " + ccp.getDefaultCountryName() + " with phone code " + ccp.getDefaultCountryCode(), Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), "Invalid number format", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSetNewDefaultNameCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameCode;
                try {
                    nameCode = edtDefaultNameCode.getText().toString();
                    Log.d(TAG, "nameCode = " + nameCode);
                    ccp.setDefaultCountryUsingNameCode(nameCode);
                    Toast.makeText(getActivity(), "Now default country is " + ccp.getDefaultCountryName() + " with phone code " + ccp.getDefaultCountryCode(), Toast.LENGTH_LONG).show();
                } catch (Exception ex) {

                }
            }
        });

        buttonResetToDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ccp.resetToDefaultCountry();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ExampleActivity) getActivity()).viewPager.setCurrentItem(((ExampleActivity) getActivity()).viewPager.getCurrentItem() + 1);
            }
        });
    }

    private void editTextWatcher() {
        editTextDefaultPhoneCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSetNewDefaultPhoneCode.setText("set " + s + " as Default Country Code");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtDefaultNameCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSetNewDefaultNameCode.setText("set '" + s + "' as Default Country Name Code");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void assignViews() {
        editTextDefaultPhoneCode = (EditText) getView().findViewById(R.id.editText_defaultCode);
        ccp = (CountryCodePicker) getView().findViewById(R.id.ccp);
        buttonSetNewDefaultPhoneCode = (Button) getView().findViewById(R.id.button_setDefaultCode);
        buttonResetToDefault = (Button) getView().findViewById(R.id.button_resetToDefault);

        edtDefaultNameCode = (EditText) getView().findViewById(R.id.default_country_default_name_code_edt);
        btnSetNewDefaultNameCode = (Button) getView().findViewById(R.id.default_country_set_default_name_code_btn);

        buttonNext = (Button) getView().findViewById(R.id.button_next);
    }
}
