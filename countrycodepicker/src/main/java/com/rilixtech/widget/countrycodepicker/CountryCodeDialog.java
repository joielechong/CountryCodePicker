package com.rilixtech.widget.countrycodepicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for selecting Country.
 *
 * Created by Joielechong on 11 May 2017.
 */
class CountryCodeDialog extends Dialog {
  private static final String TAG = "CountryCodeDialog";

  private EditText mEdtSearch;
  private TextView mTvNoResult;
  private TextView mTvTitle;
  private ListView mLvCountryDialog;
  private CountryCodePicker mCountryCodePicker;
  private RelativeLayout mRlyDialog;

  private List<Country> masterCountries;
  private List<Country> mFilteredCountries;
  private InputMethodManager mInputMethodManager;
  private CountryCodeArrayAdapter mArrayAdapter;
  private List<Country> mTempCountries;

  CountryCodeDialog(CountryCodePicker countryCodePicker) {
    super(countryCodePicker.getContext());
    mCountryCodePicker = countryCodePicker;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.country_code_picker_layout_picker_dialog);
    setupUI();
    setupData();
  }

  private void setupUI() {
    mRlyDialog = findViewById(R.id.dialog_rly);
    mLvCountryDialog = findViewById(R.id.country_dialog_lv);
    mTvTitle = findViewById(R.id.title_tv);
    mEdtSearch = findViewById(R.id.search_edt);
    mTvNoResult = findViewById(R.id.no_result_tv);
  }

  private void setupData() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      mLvCountryDialog.setLayoutDirection(mCountryCodePicker.getLayoutDirection());
    }

    if (mCountryCodePicker.getTypeFace() != null) {
      Typeface typeface = mCountryCodePicker.getTypeFace();
      mTvTitle.setTypeface(typeface);
      mEdtSearch.setTypeface(typeface);
      mTvNoResult.setTypeface(typeface);
    }

    if (mCountryCodePicker.getBackgroundColor() != mCountryCodePicker.getDefaultBackgroundColor()) {
      mRlyDialog.setBackgroundColor(mCountryCodePicker.getBackgroundColor());
    }

    if (mCountryCodePicker.getDialogTextColor() != mCountryCodePicker.getDefaultContentColor()) {
      int color = mCountryCodePicker.getDialogTextColor();
      mTvTitle.setTextColor(color);
      mTvNoResult.setTextColor(color);
      mEdtSearch.setTextColor(color);
      mEdtSearch.setHintTextColor(adjustAlpha(color, 0.7f));
    }

    mCountryCodePicker.refreshCustomMasterList();
    mCountryCodePicker.refreshPreferredCountries();
    masterCountries = mCountryCodePicker.getCustomCountries(mCountryCodePicker);

    mFilteredCountries = getFilteredCountries();
    setupListView(mLvCountryDialog);

    Context ctx = mCountryCodePicker.getContext();
    mInputMethodManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
    setSearchBar();
  }

  private void setupListView(ListView listView) {
    mArrayAdapter = new CountryCodeArrayAdapter(getContext(), mFilteredCountries, mCountryCodePicker);

    if (!mCountryCodePicker.isSelectionDialogShowSearch()) {
      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) listView.getLayoutParams();
      params.height = ListView.LayoutParams.WRAP_CONTENT;
      listView.setLayoutParams(params);
    }

    OnItemClickListener listener = new OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mFilteredCountries == null) {
          Log.e(TAG, "no filtered countries found! This should not be happened, Please report!");
          return;
        }

        if (mFilteredCountries.size() < position || position < 0) {
          Log.e(TAG, "Something wrong with the ListView. Please report this!");
          return;
        }

        Country country = mFilteredCountries.get(position);
        /* view is only a separator, so the country is null and we ignore it.
         see {@link #getFilteredCountries(String)} */
        if (country == null) return;

        mCountryCodePicker.setSelectedCountry(country);
        mInputMethodManager.hideSoftInputFromWindow(mEdtSearch.getWindowToken(), 0);
        dismiss();
      }
    };
    listView.setOnItemClickListener(listener);
    listView.setAdapter(mArrayAdapter);
  }

  private int adjustAlpha(int color, float factor) {
    int alpha = Math.round(Color.alpha(color) * factor);
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    return Color.argb(alpha, red, green, blue);
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
    if (mEdtSearch == null) return;

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

    mFilteredCountries = getFilteredCountries(query);

    if (mFilteredCountries.size() == 0) {
      mTvNoResult.setVisibility(View.VISIBLE);
    }

    mArrayAdapter.notifyDataSetChanged();
  }

  private List<Country> getFilteredCountries() {
    return getFilteredCountries("");
  }

  private List<Country> getFilteredCountries(String query) {
    if (mTempCountries == null) {
      mTempCountries = new ArrayList<>();
    } else {
      mTempCountries.clear();
    }

    List<Country> preferredCountries = mCountryCodePicker.getPreferredCountries();
    if (preferredCountries != null && preferredCountries.size() > 0) {
      for (Country country : preferredCountries) {
        if (country.isEligibleForQuery(query)) {
          mTempCountries.add(country);
        }
      }

      if (mTempCountries.size() > 0) { //means at least one preferred country is added.
        mTempCountries.add(null); // this will add separator for preference countries.
      }
    }

    for (Country country : masterCountries) {
      if (country.isEligibleForQuery(query)) {
        mTempCountries.add(country);
      }
    }
    return mTempCountries;
  }
}
