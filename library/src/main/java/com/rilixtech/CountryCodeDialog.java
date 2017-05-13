package com.rilixtech;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.util.List;

/**
 * Created by rio on 5/11/17.
 */

public class CountryCodeDialog extends Dialog {
  private AppCompatEditText mEdtSearch;
  private AppCompatTextView mTvNoResult;
  private AppCompatTextView mTvTitle;
  private RecyclerView mRvCountryDialog;
  private CountryCodePicker mCountryCodePicker;

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
    mRvCountryDialog = (RecyclerView) this.findViewById(R.id.country_dialog_rv);
    mTvTitle = (AppCompatTextView) this.findViewById(R.id.title_tv);
    mTvTitle.setText(R.string.select_country);
    mEdtSearch = (AppCompatEditText) this.findViewById(R.id.search_edt);
    mEdtSearch.setHint(R.string.search_hint);
    mTvNoResult = (AppCompatTextView) this.findViewById(R.id.no_result_tv);
    mTvNoResult.setText(R.string.no_result_found);
    if(mCountryCodePicker.getTypeFace() != null) {
      Typeface typeface = mCountryCodePicker.getTypeFace();
      mTvTitle.setTypeface(typeface);
      mEdtSearch.setTypeface(typeface);
      mTvNoResult.setTypeface(typeface);
    }
  }

  private void setupData() {
    mCountryCodePicker.refreshCustomMasterList();
    mCountryCodePicker.refreshPreferredCountries();
    List<Country> masterCountries = mCountryCodePicker.getCustomCountries(mCountryCodePicker);
    final CountryCodeAdapter cca = new CountryCodeAdapter(masterCountries, mCountryCodePicker, mEdtSearch,
            mTvNoResult, this);
    if (!mCountryCodePicker.isSelectionDialogShowSearch()) {
      Toast.makeText(getContext(), "Found not to show search", Toast.LENGTH_SHORT).show();
      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRvCountryDialog.getLayoutParams();
      params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
      mRvCountryDialog.setLayoutParams(params);
    }
    mRvCountryDialog.setLayoutManager(new LinearLayoutManager(getContext()));
    mRvCountryDialog.setAdapter(cca);
  }

  public void reShow() {
    setupData();
    show();
  }
}
