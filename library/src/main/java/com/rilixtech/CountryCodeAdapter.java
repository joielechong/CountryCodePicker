package com.rilixtech;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by hbb20 on 11/1/16. item
 *
 * Move all code unrelated with RecyclerView item to parent dialog.
 * Updated by joielechong on 6 June 2017
 */
class CountryCodeAdapter extends RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder> {

  private List<Country> mCountries;
  private CountryCodePicker mCountryCodePicker;
  private Callback mCallback;

  interface Callback {
    void onItemCountrySelected(Country country);
  }

  CountryCodeAdapter(List<Country> countries, CountryCodePicker codePicker, Callback callback) {
    this.mCountries = countries;
    this.mCountryCodePicker = codePicker;
    this.mCallback = callback;
  }

  @Override public CountryCodeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
    View rootView = inflater.inflate(R.layout.layout_recycler_country_tile, viewGroup, false);
    return new CountryCodeViewHolder(rootView);
  }

  @Override public void onBindViewHolder(CountryCodeViewHolder viewHolder, final int i) {
    final int position = viewHolder.getAdapterPosition();
    viewHolder.setCountry(mCountries.get(position));
    viewHolder.rlyMain.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        mCallback.onItemCountrySelected(mCountries.get(position));
      }
    });
  }

  @Override public int getItemCount() {
    return mCountries.size();
  }

  class CountryCodeViewHolder extends RecyclerView.ViewHolder {
    RelativeLayout rlyMain;
    AppCompatTextView tvName, tvCode;
    AppCompatImageView imvFlag;
    LinearLayout llyFlagHolder;
    View viewDivider;

    CountryCodeViewHolder(View itemView) {
      super(itemView);
      rlyMain = (RelativeLayout) itemView;
      tvName = (AppCompatTextView) rlyMain.findViewById(R.id.country_name_tv);
      tvCode = (AppCompatTextView) rlyMain.findViewById(R.id.code_tv);
      imvFlag = (AppCompatImageView) rlyMain.findViewById(R.id.flag_imv);
      llyFlagHolder = (LinearLayout) rlyMain.findViewById(R.id.flag_holder_lly);
      viewDivider = rlyMain.findViewById(R.id.preference_divider_view);
    }

    private void setCountry(Country country) {
      if (country != null) {
        viewDivider.setVisibility(View.GONE);
        tvName.setVisibility(View.VISIBLE);
        tvCode.setVisibility(View.VISIBLE);
        llyFlagHolder.setVisibility(View.VISIBLE);
        String countryNameAndCode = tvName.getContext()
            .getString(R.string.country_name_and_code, country.getName(),
                country.getIso().toUpperCase());
        tvName.setText(countryNameAndCode);
        if (!mCountryCodePicker.isHidePhoneCode()) {
          tvCode.setText(
              tvCode.getContext().getString(R.string.phone_code, country.getPhoneCode()));
        } else {
          tvCode.setVisibility(View.GONE);
        }
        if (mCountryCodePicker.getTypeFace() != null) {
          tvCode.setTypeface(mCountryCodePicker.getTypeFace());
          tvName.setTypeface(mCountryCodePicker.getTypeFace());
        }
        imvFlag.setImageResource(country.getFlagDrawableResId());

        if (mCountryCodePicker.getTextColor() != mCountryCodePicker.getDefaultContentColor()) {
          int color = mCountryCodePicker.getTextColor();
          tvCode.setTextColor(color);
          tvName.setTextColor(color);
        }
      } else {
        viewDivider.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.GONE);
        tvCode.setVisibility(View.GONE);
        llyFlagHolder.setVisibility(View.GONE);
      }
    }
  }
}

