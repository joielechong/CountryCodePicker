package com.rilixtech.countrycodepicker;

import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rilixtech.widget.countrycodepicker.CountryCodePicker;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomColorFragment extends Fragment {

    private Button buttonNext;
    private TextView textViewTitle;
    private EditText editTextPhone;
    private CountryCodePicker ccp;
    private RelativeLayout relativeColor1,relativeColor2,relativeColor3;

    public CustomColorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_color, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assignViews(view);
        setClickListener();
    }

    private void assignViews(View view) {
        textViewTitle =(TextView)view.findViewById(R.id.textView_title);
        editTextPhone =(EditText)view.findViewById(R.id.editText_phone);
        ccp=(CountryCodePicker)view.findViewById(R.id.ccp);
        relativeColor1=(RelativeLayout)view.findViewById(R.id.relative_color1);
        relativeColor2=(RelativeLayout)view.findViewById(R.id.relative_color2);
        relativeColor3=(RelativeLayout)view.findViewById(R.id.relative_color3);
        buttonNext = (Button) view.findViewById(R.id.button_next);
    }

    private void setClickListener() {
        relativeColor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(1, getActivity().getResources().getColor(R.color.color1));
            }
        });

        relativeColor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(2, getActivity().getResources().getColor(R.color.color2));
            }
        });

        relativeColor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(3,getActivity().getResources().getColor(R.color.color3));
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ExampleActivity) getActivity()).viewPager.setCurrentItem(((ExampleActivity) getActivity()).viewPager.getCurrentItem() + 1);
            }
        });
    }

    private void setColor(int selection,int color) {
        ccp.setTextColor(color);
        //textView
        textViewTitle.setTextColor(color);

        //editText
        editTextPhone.setTextColor(color);
        editTextPhone.setHintTextColor(color);
        editTextPhone.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        //remove selected bg
        resetBG();

        //set selected bg
        int selectedBGColor=getActivity().getResources().getColor(R.color.selectedTile);
        switch (selection){
            case 1:
                relativeColor1.setBackgroundColor(selectedBGColor);
                break;
            case 2:
                relativeColor2.setBackgroundColor(selectedBGColor);
                break;
            case 3:
                relativeColor3.setBackgroundColor(selectedBGColor);
                break;
        }
    }

    private void resetBG() {
        relativeColor1.setBackgroundColor(getActivity().getResources().getColor(R.color.dullBG));
        relativeColor2.setBackgroundColor(getActivity().getResources().getColor(R.color.dullBG));
        relativeColor3.setBackgroundColor(getActivity().getResources().getColor(R.color.dullBG));
    }

}
