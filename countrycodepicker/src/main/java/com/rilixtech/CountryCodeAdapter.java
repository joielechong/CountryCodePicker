package com.rilixtech;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;
import com.rilixtech.CountryCodeDialog.ItemRecyclerViewClickListener;

/**
 * Created by hbb20 on 11/1/16. item
 *
 * Move all code unrelated with RecyclerView item to parent dialog.
 * Updated by joielechong on 6 June 2017
 *
 * Invert if and make code efficient.
 * Don't enable click for divider (thanks for the code from @kamiox)
 * Updated by joielechong on 22 August 2018
 */
class CountryCodeAdapter extends RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder> {
  private final List<Country> mCountries;
  private final CountryCodePicker mCountryCodePicker;
  private final ItemRecyclerViewClickListener mListener;

  CountryCodeAdapter(List<Country> countries, CountryCodePicker codePicker,
      ItemRecyclerViewClickListener listener) {
    mCountries = countries;
    mCountryCodePicker = codePicker;
    mListener = listener;
  }

  @Override public CountryCodeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
    View view = inflater.inflate(R.layout.layout_recycler_country_tile, viewGroup, false);
    return new CountryCodeViewHolder(view);
  }

  @Override public void onBindViewHolder(CountryCodeViewHolder viewHolder, final int i) {
    final int position = viewHolder.getAdapterPosition();
    Country country = mCountries.get(position);
    viewHolder.setCountry(mCountryCodePicker, country);
    viewHolder.rlyMain.setOnClickListener(country != null ? mListener : null);
  }

  @Override public int getItemCount() {
    return mCountries.size();
  }

  class CountryCodeViewHolder extends RecyclerView.ViewHolder {
    RelativeLayout rlyMain;
    TextView tvName, tvCode;
    ImageView imvFlag;
    LinearLayout llyFlagHolder;
    View viewDivider;

    CountryCodeViewHolder(View itemView) {
      super(itemView);
      rlyMain = (RelativeLayout) itemView;
      tvName = rlyMain.findViewById(R.id.country_name_tv);
      tvCode = rlyMain.findViewById(R.id.code_tv);
      imvFlag = rlyMain.findViewById(R.id.flag_imv);
      llyFlagHolder = rlyMain.findViewById(R.id.flag_holder_lly);
      viewDivider = rlyMain.findViewById(R.id.preference_divider_view);
    }

    private void setCountry(CountryCodePicker picker, Country country) {
      if (country == null) {
        viewDivider.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.GONE);
        tvCode.setVisibility(View.GONE);
        llyFlagHolder.setVisibility(View.GONE);
      } else {
        viewDivider.setVisibility(View.GONE);
        tvName.setVisibility(View.VISIBLE);
        tvCode.setVisibility(View.VISIBLE);
        llyFlagHolder.setVisibility(View.VISIBLE);
        Context ctx = tvName.getContext();
        String name = country.getName();
        String iso = country.getIso().toUpperCase();
        String countryNameAndCode = ctx.getString(R.string.country_name_and_code, name, iso);
        tvName.setText(countryNameAndCode);
        if (picker.isHidePhoneCode()) {
          tvCode.setVisibility(View.GONE);
        } else {
          tvCode.setText(ctx.getString(R.string.phone_code, country.getPhoneCode()));
        }
        Typeface typeface = picker.getTypeFace();
        if (typeface != null) {
          tvCode.setTypeface(typeface);
          tvName.setTypeface(typeface);
        }
        imvFlag.setImageResource(CountryUtils.getFlagDrawableResId(country));
        int color = picker.getDialogTextColor();
        if (color != picker.getDefaultContentColor()) {
          tvCode.setTextColor(color);
          tvName.setTextColor(color);
        }
      }
    }
  }
}

