package com.rilixtech;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hbb20 on 11/1/16.
 */
class CountryCodeAdapter extends RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder> {
  private List<Country> filteredCountries = null;
  private List<Country> masterCountries = null;
  private AppCompatTextView mTvNoResult;
  private CountryCodePicker mCountryCodePicker;
  //private LayoutInflater mInflater;
  private AppCompatEditText mEdtSearch;
  private Dialog mDialog;
  private InputMethodManager mInputMethodManager;

  CountryCodeAdapter(List<Country> countries, CountryCodePicker codePicker,
      final AppCompatEditText edtSearch, AppCompatTextView tvNoResult, Dialog dialog) {
    //this.context = context;
    this.masterCountries = countries;
    this.mCountryCodePicker = codePicker;
    this.mDialog = dialog;
    this.mTvNoResult = tvNoResult;
    this.mEdtSearch = edtSearch;
    this.filteredCountries = getFilteredCountries();
    mInputMethodManager = (InputMethodManager) mCountryCodePicker.getContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    setSearchBar();
  }

  private void setSearchBar() {
    if (mCountryCodePicker.isSelectionDialogShowSearch()) {
      setTextWatcher();
    } else {
      mEdtSearch.setVisibility(View.GONE);
    }
  }

  /**
   * add textChangeListener, to apply new query each time editText get text changed.
   */
  private void setTextWatcher() {
    if (mEdtSearch != null) {
      mEdtSearch.addTextChangedListener(new TextWatcher() {

        @Override public void afterTextChanged(Editable s) {
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
          applyQuery(s.toString());
        }
      });

      if (mCountryCodePicker.isKeyboardAutoPopOnSearch()) {
        if (mInputMethodManager != null) {
          mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
      }
    }
  }

  /**
   * Filter country list for given keyWord / query.
   * Lists all countries that contains @param query in country's name, name code or phone code.
   *
   * @param query : text to match against country name, name code or phone code
   */
  private void applyQuery(String query) {
    mTvNoResult.setVisibility(View.GONE);
    query = query.toLowerCase();

    //if query started from "+" ignore it
    if (query.length() > 0 && query.charAt(0) == '+') {
      query = query.substring(1);
    }

    filteredCountries = getFilteredCountries(query);

    if (filteredCountries.size() == 0) {
      mTvNoResult.setVisibility(View.VISIBLE);
    }
    notifyDataSetChanged();
  }

  private List<Country> getFilteredCountries() {
    return getFilteredCountries("");
  }

  private List<Country> getFilteredCountries(String query) {
    List<Country> tempCountryList = new ArrayList<>();
    List<Country> preferredCountries = mCountryCodePicker.getPreferredCountries();
    if (preferredCountries != null && preferredCountries.size() > 0) {
      for (Country country : preferredCountries) {
        if (country.isEligibleForQuery(query)) {
          tempCountryList.add(country);
        }
      }

      if (tempCountryList.size() > 0) { //means at least one preferred country is added.
        Country divider = null;
        tempCountryList.add(divider);
      }
    }

    for (Country country : masterCountries) {
      if (country.isEligibleForQuery(query)) {
        tempCountryList.add(country);
      }
    }
    return tempCountryList;
  }

  @Override public CountryCodeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
    View rootView = inflater.inflate(R.layout.layout_recycler_country_tile, viewGroup, false);
    return new CountryCodeViewHolder(rootView);
  }

  @Override public void onBindViewHolder(CountryCodeViewHolder viewHolder, final int i) {
    final int position =viewHolder.getAdapterPosition();
    viewHolder.setCountry(filteredCountries.get(position));
    viewHolder.rlyMain.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        mCountryCodePicker.setSelectedCountry(filteredCountries.get(position));
        if (view != null && filteredCountries.get(position) != null) {
          //InputMethodManager imm = (InputMethodManager) mCountryCodePicker.getContext()
          //    .getSystemService(Context.INPUT_METHOD_SERVICE);
          mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
          mDialog.dismiss();
        }
      }
    });
  }

  @Override public int getItemCount() {
    return filteredCountries.size();
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

    public void setCountry(Country country) {
      if (country != null) {
        viewDivider.setVisibility(View.GONE);
        tvName.setVisibility(View.VISIBLE);
        tvCode.setVisibility(View.VISIBLE);
        llyFlagHolder.setVisibility(View.VISIBLE);
        String countryNameAndCode = tvName.getContext()
            .getString(R.string.country_name_and_code, country.getName(),
                country.getNameCode().toUpperCase());
        tvName.setText(countryNameAndCode);
        tvCode.setText(tvCode.getContext().getString(R.string.phone_code, country.getPhoneCode()));
        imvFlag.setImageResource(country.getFlagID());
      } else {
        viewDivider.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.GONE);
        tvCode.setVisibility(View.GONE);
        llyFlagHolder.setVisibility(View.GONE);
      }
    }
  }
}

