package com.rilixtech.countrycodepicker;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

/**
 * A simple {@link Fragment} subclass.
 */
public class IntroductionFragment extends Fragment {


    Button buttonGo;
    public IntroductionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_introduction, container, false);

        CountryCodePicker ccp = view.findViewById(R.id.introduction_ccp);
        ccp.setArrowSize(50);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assignViews();
        setClickListener();
    }

    private void setClickListener() {
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ExampleActivity)getActivity()).viewPager.setCurrentItem(1);
            }
        });
    }

    private void assignViews() {
        buttonGo=(Button)getView().findViewById(R.id.button_letsGo);
    }
}
