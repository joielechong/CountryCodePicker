package com.rilixtech.widget.countrycodepicker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import java.util.List;
import java.util.Locale;

public class CountryCodeArrayAdapter extends ArrayAdapter<Country> {
  private final CountryCodePicker mCountryCodePicker;
  private String mDefaultLocaleLanguage;

  CountryCodeArrayAdapter(Context ctx, List<Country> countries, CountryCodePicker picker) {
    super(ctx, 0, countries);
    mCountryCodePicker = picker;
    mDefaultLocaleLanguage = Locale.getDefault().getLanguage();
  }

  private static class ViewHolder {
    RelativeLayout rlyMain;
    TextView tvName, tvCode;
    ImageView imvFlag;
    LinearLayout llyFlagHolder;
    View viewDivider;
  }

  @Override
  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    Country country = getItem(position);

    ViewHolder viewHolder;
    if (convertView == null) {
      viewHolder = new ViewHolder();
      LayoutInflater inflater = LayoutInflater.from(getContext());
      convertView = inflater.inflate(R.layout.country_code_picker_item_country, parent, false);

      viewHolder.rlyMain = convertView.findViewById(R.id.item_country_rly);
      viewHolder.tvName = convertView.findViewById(R.id.country_name_tv);
      viewHolder.tvCode = convertView.findViewById(R.id.code_tv);
      viewHolder.imvFlag = convertView.findViewById(R.id.flag_imv);
      viewHolder.llyFlagHolder = convertView.findViewById(R.id.flag_holder_lly);
      viewHolder.viewDivider = convertView.findViewById(R.id.preference_divider_view);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    setData(country, viewHolder);
    return convertView;
  }

  private void setData(Country country, ViewHolder viewHolder) {
    if (country == null) {
      viewHolder.viewDivider.setVisibility(View.VISIBLE);
      viewHolder.tvName.setVisibility(View.GONE);
      viewHolder.tvCode.setVisibility(View.GONE);
      viewHolder.llyFlagHolder.setVisibility(View.GONE);
      return;
    }

    viewHolder.viewDivider.setVisibility(View.GONE);
    viewHolder.tvName.setVisibility(View.VISIBLE);
    viewHolder.tvCode.setVisibility(View.VISIBLE);
    viewHolder.llyFlagHolder.setVisibility(View.VISIBLE);
    Context ctx = viewHolder.tvName.getContext();
    String name = country.getName();
    String iso = country.getIso().toUpperCase();
    String countryName;
    String countryNameAndCode;
    try {
      countryName = getLocale(iso).getDisplayCountry();
    } catch (NullPointerException exception) {
      countryName = name;
    }
    if (mCountryCodePicker.isHideNameCode()) {
      countryNameAndCode = countryName;
    } else {
      countryNameAndCode = ctx.getString(R.string.country_name_and_code, countryName, iso);
    }
    viewHolder.tvName.setText(countryNameAndCode);

    if (mCountryCodePicker.isHidePhoneCode()) {
      viewHolder.tvCode.setVisibility(View.GONE);
    } else {
      viewHolder.tvCode.setText(ctx.getString(R.string.phone_code, country.getPhoneCode()));
    }

    Typeface typeface = mCountryCodePicker.getTypeFace();
    if (typeface != null) {
      viewHolder.tvCode.setTypeface(typeface);
      viewHolder.tvName.setTypeface(typeface);
    }
    viewHolder.imvFlag.setImageResource(CountryUtils.getFlagDrawableResId(country));
    int color = mCountryCodePicker.getDialogTextColor();
    if (color != mCountryCodePicker.getDefaultContentColor()) {
      viewHolder.tvCode.setTextColor(color);
      viewHolder.tvName.setTextColor(color);
    }
  }

  private Locale getLocale(String iso) throws NullPointerException {
    return new Locale(mDefaultLocaleLanguage, iso);
  }
}
