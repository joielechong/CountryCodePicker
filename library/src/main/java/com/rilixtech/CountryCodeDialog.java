package com.rilixtech;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.widget.RelativeLayout;
import java.util.List;

/**
 * Dialog for selecting Country.
 *
 * Created by Joielechong on 11 May 2017.
 */

class CountryCodeDialog extends Dialog {
  private AppCompatEditText mEdtSearch;
  private AppCompatTextView mTvNoResult;
  private AppCompatTextView mTvTitle;
  private RecyclerView mRvCountryDialog;
  private CountryCodePicker mCountryCodePicker;
  private RelativeLayout mRlyDialog;

  public CountryCodeDialog(CountryCodePicker countryCodePicker) {
    super(countryCodePicker.getContext());
    this.mCountryCodePicker = countryCodePicker;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.layout_picker_dialog);
    setupUI();
    setupData();
  }

  private void setupUI() {
    mRlyDialog = (RelativeLayout) this.findViewById(R.id.dialog_rly);
    mRvCountryDialog = (RecyclerView) this.findViewById(R.id.country_dialog_rv);
    mTvTitle = (AppCompatTextView) this.findViewById(R.id.title_tv);
    //mTvTitle.setText(R.string.select_country);
    mEdtSearch = (AppCompatEditText) this.findViewById(R.id.search_edt);
    //mEdtSearch.setHint(R.string.search_hint);
    mTvNoResult = (AppCompatTextView) this.findViewById(R.id.no_result_tv);
    //mTvNoResult.setText(R.string.no_result_found);

  }

  private void setupData() {
    if (mCountryCodePicker.getTypeFace() != null) {
      Typeface typeface = mCountryCodePicker.getTypeFace();
      mTvTitle.setTypeface(typeface);
      mEdtSearch.setTypeface(typeface);
      mTvNoResult.setTypeface(typeface);
    }
    if(mCountryCodePicker.getBackgroundColor() != mCountryCodePicker.getDefaultBackgroundColor()) {
      mRlyDialog.setBackgroundColor(mCountryCodePicker.getBackgroundColor());
    }

    if(mCountryCodePicker.getTextColor() != mCountryCodePicker.getDefaultContentColor()) {
      int color = mCountryCodePicker.getTextColor();
      mTvTitle.setTextColor(color);
      mTvNoResult.setTextColor(color);
      mEdtSearch.setTextColor(color);

      mEdtSearch.setHintTextColor(adjustAlpha(color, 0.7f));
    }

    mCountryCodePicker.refreshCustomMasterList();
    mCountryCodePicker.refreshPreferredCountries();
    List<Country> masterCountries = mCountryCodePicker.getCustomCountries(mCountryCodePicker);
    CountryCodeAdapter cca =
        new CountryCodeAdapter(masterCountries, mCountryCodePicker, mEdtSearch, mTvNoResult, this);
    if (!mCountryCodePicker.isSelectionDialogShowSearch()) {
      RelativeLayout.LayoutParams params =
          (RelativeLayout.LayoutParams) mRvCountryDialog.getLayoutParams();
      params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
      mRvCountryDialog.setLayoutParams(params);
    }
    mRvCountryDialog.setLayoutManager(new LinearLayoutManager(getContext()));
    mRvCountryDialog.setAdapter(cca);
  }

  void reShow() {
    setupData();
    show();
  }

  private int adjustAlpha(int color, float factor) {
    int alpha = Math.round(Color.alpha(color) * factor);
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    return Color.argb(alpha, red, green, blue);
  }
}
