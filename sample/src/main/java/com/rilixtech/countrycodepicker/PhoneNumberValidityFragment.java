package com.rilixtech.countrycodepicker;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
public class PhoneNumberValidityFragment extends Fragment {

    Button buttonNext;
    Button btnCheck;
    View rootView;
    private CountryCodePicker ccp;
    private EditText edtPhoneNumber;

    public PhoneNumberValidityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.fragment_phone_number_validity, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assignViews();
        setClickListener();
    }

    private void setClickListener() {

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    private void assignViews() {
        ccp=(CountryCodePicker)rootView.findViewById(R.id.ccp);
        edtPhoneNumber = (EditText) rootView.findViewById(R.id.phone_number_edt);
        ccp.registerPhoneNumberTextView(edtPhoneNumber);
        buttonNext = (Button) rootView.findViewById(R.id.button_next);
        btnCheck = (Button) rootView.findViewById(R.id.check_btn);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(ccp.isValid()) {
                    Toast.makeText(getContext(), "number " + ccp.getFullNumber() + " is valid.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "number " + ccp.getFullNumber() + " not valid!!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
