package com.rilixtech;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hbb20 on 11/1/16.
 *
 * Updated code style and remove unused code
 *
 * Updated by Joielechong 13 May 2017
 */
public class CountryCodePicker extends RelativeLayout {

  private static String TAG = CountryCodePicker.class.getSimpleName();

  private static final int DEFAULT_COUNTRY_CODE = 62; // Indonesia

  private int mDefaultCountryCode;
  private String mDefaultCountryNameCode;

  private View mViewHolder;
  private LayoutInflater mInflater;
  private AppCompatTextView mTvSelectedCountry;
  private AppCompatEditText mEdtRegisteredCarrierNumber;
  private RelativeLayout mRlyHolder;
  private AppCompatImageView mImvArrow;
  private AppCompatImageView mImvFlag;
  private LinearLayout mLlyFlagHolder;
  private Country mSelectedCountry;
  private Country mDefaultCountry;
  private RelativeLayout mRlyClickConsumer;

  private boolean mHideNameCode = false;
  private boolean mShowFlag = true;
  private boolean mShowFullName = false;
  private boolean mUseFullName = false;
  private boolean mSelectionDialogShowSearch = true;

  private List<Country> mPreferredCountries;
  //this will be "AU,ID,US"
  private String mCountryPreference;
  private List<Country> mCustomMasterCountriesList;
  //this will be "AU,ID,US"
  private String mCustomMasterCountries;
  private boolean mKeyboardAutoPopOnSearch = true;
  private boolean mIsClickable = true;
  private CountryCodeDialog mCountryCodeDialog;

  private boolean mHidePhoneCode = false;

  private static final int DEFAULT_TEXT_COLOR = 0;
  private int mTextColor = DEFAULT_TEXT_COLOR;
  private static final int  DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;
  private int mBackgroundColor = DEFAULT_BACKGROUND_COLOR;

  // Font typeface
  private Typeface mTypeFace;

  private OnCountryChangeListener mOnCountryChangeListener;

  View.OnClickListener countryCodeHolderClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      if (isClickable()) {
        if (mCountryCodeDialog == null) {
          mCountryCodeDialog = new CountryCodeDialog(CountryCodePicker.this);
          mCountryCodeDialog.show();
        } else {
          mCountryCodeDialog.reShow();
        }
      }
    }
  };

  public CountryCodePicker(Context context) {
    super(context);
    init(null);
  }

  public CountryCodePicker(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public CountryCodePicker(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    mInflater = LayoutInflater.from(this.getContext());
    mViewHolder = mInflater.inflate(R.layout.layout_code_picker, this, true);
    mTvSelectedCountry = (AppCompatTextView) mViewHolder.findViewById(R.id.selected_country_tv);
    mRlyHolder = (RelativeLayout) mViewHolder.findViewById(R.id.country_code_holder_rly);
    mImvArrow = (AppCompatImageView) mViewHolder.findViewById(R.id.arrow_imv);
    mImvFlag = (AppCompatImageView) mViewHolder.findViewById(R.id.flag_imv);
    mLlyFlagHolder = (LinearLayout) mViewHolder.findViewById(R.id.flag_holder_lly);
    mRlyClickConsumer = (RelativeLayout) mViewHolder.findViewById(R.id.click_consumer_rly);
    applyCustomProperty(attrs);
    mRlyClickConsumer.setOnClickListener(countryCodeHolderClickListener);
  }

  private void applyCustomProperty(AttributeSet attrs) {
    TypedArray a =
        getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0);
    //default country code
    try {

      // Hiding phone code
      mHidePhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hidePhoneCode, false);

      //hide nameCode. If someone wants only phone code to avoid name collision for same country phone code.
      mHideNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hideNameCode, false);

      //show full name
      mShowFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false);

      //auto pop keyboard
      setKeyboardAutoPopOnSearch(
          a.getBoolean(R.styleable.CountryCodePicker_ccp_keyboardAutoPopOnSearch, true));

      //custom master list
      mCustomMasterCountries = a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries);
      refreshCustomMasterList();

      //preference
      mCountryPreference = a.getString(R.styleable.CountryCodePicker_ccp_countryPreference);
      refreshPreferredCountries();

      //default country
      mDefaultCountryNameCode = a.getString(R.styleable.CountryCodePicker_ccp_defaultNameCode);
      boolean setUsingNameCode = false;
      if (mDefaultCountryNameCode != null && mDefaultCountryNameCode.length() != 0) {
        if (CountryUtils.getByNameCodeFromAllCountries(getContext(), mDefaultCountryNameCode)
            != null) {
          setUsingNameCode = true;
          setDefaultCountry(
              CountryUtils.getByNameCodeFromAllCountries(getContext(), mDefaultCountryNameCode));
          setSelectedCountry(mDefaultCountry);
        }
      }

      //if default country is not set using name code.
      if (!setUsingNameCode) {
        int defaultCountryCode = a.getInteger(R.styleable.CountryCodePicker_ccp_defaultCode, -1);

        //if invalid country is set using xml, it will be replaced with DEFAULT_COUNTRY_CODE
        if (CountryUtils.getByCode(getContext(), mPreferredCountries, defaultCountryCode) == null) {
          defaultCountryCode = DEFAULT_COUNTRY_CODE;
        }
        setDefaultCountryUsingPhoneCode(defaultCountryCode);
        setSelectedCountry(mDefaultCountry);
      }

      //show flag
      showFlag(a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true));

      //text color
      int textColor;
      if (isInEditMode()) {
        textColor = a.getColor(R.styleable.CountryCodePicker_ccp_textColor, 0);
      } else {
        textColor = a.getColor(R.styleable.CountryCodePicker_ccp_textColor,
            ContextCompat.getColor(getContext(), R.color.defaultTextColor));
      }
      if (textColor != 0) {
        setTextColor(textColor);
      }

      mBackgroundColor = a.getColor(R.styleable.CountryCodePicker_ccp_backgroundColor, Color.TRANSPARENT);

      if(mBackgroundColor != Color.TRANSPARENT) {
        mRlyHolder.setBackgroundColor(mBackgroundColor);
      }

      // text font
      String fontPath = a.getString(R.styleable.CountryCodePicker_ccp_textFont);
      if (fontPath != null && fontPath.length() != 0) {
        setTypeFace(fontPath);
      }

      //text size
      int textSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0);
      if (textSize > 0) {
        mTvSelectedCountry.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        setFlagSize(textSize);
        setArrowSize(textSize);
      } else { //no textsize specified
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int defaultSize = Math.round(18 * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        setTextSize(defaultSize);
      }

      //if arrow arrow size is explicitly defined
      int arrowSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0);
      if (arrowSize > 0) {
        setArrowSize(arrowSize);
      }

      mSelectionDialogShowSearch =
          a.getBoolean(R.styleable.CountryCodePicker_ccp_selectionDialogShowSearch, true);
      setClickable(a.getBoolean(R.styleable.CountryCodePicker_ccp_Clickable, true));

    } catch (Exception e) {
      mTvSelectedCountry.setText(e.getMessage());
    } finally {
      a.recycle();
    }
  }

  private Country getDefaultCountry() {
    return mDefaultCountry;
  }

  private void setDefaultCountry(Country defaultCountry) {
    this.mDefaultCountry = defaultCountry;
  }

  private TextView getTvSelectedCountry() {
    return mTvSelectedCountry;
  }

  private void setTvSelectedCountry(AppCompatTextView tvSelectedCountry) {
    this.mTvSelectedCountry = tvSelectedCountry;
  }

  private Country getSelectedCountry() {
    return mSelectedCountry;
  }

  void setSelectedCountry(Country selectedCountry) {
    this.mSelectedCountry = selectedCountry;
    //as soon as country is selected, textView should be updated
    if (selectedCountry == null) {
      selectedCountry =
          CountryUtils.getByCode(getContext(), mPreferredCountries, mDefaultCountryCode);
    }

    if (!mHideNameCode) {
      if (mShowFullName) {
        if (!mHidePhoneCode) {
          mTvSelectedCountry.setText(
              getContext().getString(R.string.country_full_name_and_phone_code,
                  selectedCountry.getName().toUpperCase(), selectedCountry.getPhoneCode()));
        } else {
          mTvSelectedCountry.setText(selectedCountry.getName().toUpperCase());
        }
      } else {
        if (!mHidePhoneCode) {
          mTvSelectedCountry.setText(getContext().getString(R.string.country_code_and_phone_code,
              selectedCountry.getNameCode().toUpperCase(), selectedCountry.getPhoneCode()));
        } else {
          mTvSelectedCountry.setText(selectedCountry.getNameCode().toUpperCase());
        }
      }
    } else {
      mTvSelectedCountry.setText(getContext().getString(R.string.phone_code, selectedCountry.getPhoneCode()));
    }

    if (mOnCountryChangeListener != null) {
      mOnCountryChangeListener.onCountrySelected(selectedCountry);
    }

    mImvFlag.setImageResource(selectedCountry.getFlagDrawableResId());
    //        Log.d(TAG, "Setting selected country:" + mSelectedCountry.logString());
  }

  private View getHolderView() {
    return mViewHolder;
  }

  private void setHolderView(View holderView) {
    this.mViewHolder = holderView;
  }

  private RelativeLayout getHolder() {
    return mRlyHolder;
  }

  private void setHolder(RelativeLayout holder) {
    this.mRlyHolder = holder;
  }

  boolean isKeyboardAutoPopOnSearch() {
    return mKeyboardAutoPopOnSearch;
  }

  /**
   * By default, keyboard is poped every time ccp is clicked and selection dialog is opened.
   *
   * @param keyboardAutoPopOnSearch true: to open keyboard automatically when selection dialog is
   * opened
   * false: to avoid auto pop of keyboard
   */
  public void setKeyboardAutoPopOnSearch(boolean keyboardAutoPopOnSearch) {
    this.mKeyboardAutoPopOnSearch = keyboardAutoPopOnSearch;
  }

  EditText getEdtRegisteredCarrierNumber() {
    return mEdtRegisteredCarrierNumber;
  }

  void setEdtRegisteredCarrierNumber(AppCompatEditText edtRegisteredCarrierNumber) {
    this.mEdtRegisteredCarrierNumber = edtRegisteredCarrierNumber;
  }

  private LayoutInflater getInflater() {
    return mInflater;
  }

  private OnClickListener getCountryCodeHolderClickListener() {
    return countryCodeHolderClickListener;
  }

  /**
   * this will load mPreferredCountries based on mCountryPreference
   */
  void refreshPreferredCountries() {
    if (mCountryPreference == null || mCountryPreference.length() == 0) {
      mPreferredCountries = null;
    } else {
      List<Country> localCountryList = new ArrayList<>();
      for (String nameCode : mCountryPreference.split(",")) {
        Country country =
            CountryUtils.getByNameCodeFromCustomCountries(getContext(), mCustomMasterCountriesList,
                nameCode);
        if (country != null) {
          if (!isAlreadyInList(country, localCountryList)) { //to avoid duplicate entry of country
            localCountryList.add(country);
          }
        }
      }

      if (localCountryList.size() == 0) {
        mPreferredCountries = null;
      } else {
        mPreferredCountries = localCountryList;
      }
    }
  }

  /**
   * this will load mPreferredCountries based on mCountryPreference
   */
  void refreshCustomMasterList() {
    if (mCustomMasterCountries == null || mCustomMasterCountries.length() == 0) {
      mCustomMasterCountriesList = null;
    } else {
      List<Country> localCountryList = new ArrayList<>();
      for (String nameCode : mCustomMasterCountries.split(",")) {
        Country country = CountryUtils.getByNameCodeFromAllCountries(getContext(), nameCode);
        if (country != null) {
          if (!isAlreadyInList(country, localCountryList)) { //to avoid duplicate entry of country
            localCountryList.add(country);
          }
        }
      }

      if (localCountryList.size() == 0) {
        mCustomMasterCountriesList = null;
      } else {
        mCustomMasterCountriesList = localCountryList;
      }
    }
  }

  List<Country> getCustomCountries() {
    return mCustomMasterCountriesList;
  }

  /**
   * Get custom country by preference
   *
   * @param codePicker picker for the source of country
   * @return List of country
   */
  List<Country> getCustomCountries(CountryCodePicker codePicker) {
    codePicker.refreshCustomMasterList();
    if (codePicker.getCustomCountries() != null && codePicker.getCustomCountries().size() > 0) {
      return codePicker.getCustomCountries();
    } else {
      return CountryUtils.getAllCountries(codePicker.getContext());
    }
  }

  void setCustomMasterCountriesList(List<Country> mCustomMasterCountriesList) {
    this.mCustomMasterCountriesList = mCustomMasterCountriesList;
  }

  String getCustomMasterCountries() {
    return mCustomMasterCountries;
  }

  public List<Country> getPreferredCountries() {
    return mPreferredCountries;
  }

  /**
   * To provide definite set of countries when selection dialog is opened.
   * Only custom master countries, if defined, will be there is selection dialog to select from.
   * To set any country in preference, it must be included in custom master countries, if defined
   * When not defined or null or blank is set, it will use library's default master list
   * Custom master list will only limit the visibility of irrelevant country from selection dialog.
   * But all other functions like setCountryForCodeName() or setFullNumber() will consider all the
   * countries.
   *
   * @param customMasterCountries is country name codes separated by comma. e.g. "us,in,nz"
   * if null or "" , will remove custom countries and library default will be used.
   */
  public void setCustomMasterCountries(String customMasterCountries) {
    this.mCustomMasterCountries = customMasterCountries;
  }

  /**
   * This will match name code of all countries of list against the country's name code.
   *
   * @param countryList list of countries against which country will be checked.
   * @return if country name code is found in list, returns true else return false
   */
  private boolean isAlreadyInList(Country country, List<Country> countryList) {
    if (country != null && countryList != null) {
      for (Country iterationCountry : countryList) {
        if (iterationCountry.getNameCode().equalsIgnoreCase(country.getNameCode())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * This function removes possible country code from fullNumber and set rest of the number as
   * carrier number.
   *
   * @param fullNumber combination of country code and carrier number.
   * @param country selected country in CCP to detect country code part.
   */
  private String detectCarrierNumber(String fullNumber, Country country) {
    String carrierNumber;
    if (country == null || fullNumber == null) {
      carrierNumber = fullNumber;
    } else {
      int indexOfCode = fullNumber.indexOf(country.getPhoneCode());
      if (indexOfCode == -1) {
        carrierNumber = fullNumber;
      } else {
        carrierNumber = fullNumber.substring(indexOfCode + country.getPhoneCode().length());
      }
    }
    return carrierNumber;
  }

  /**
   * This method is not encouraged because this might set some other country which have same country
   * code as of yours. e.g 1 is common for US and canada.
   * If you are trying to set US ( and mCountryPreference is not set) and you pass 1 as @param
   * mDefaultCountryCode, it will set canada (prior in list due to alphabetical order)
   * Rather use setDefaultCountryUsingNameCode("us"); or setDefaultCountryUsingNameCode("US");
   * <p>
   * Default country code defines your default country.
   * Whenever invalid / improper number is found in setCountryForPhoneCode() /  setFullNumber(), it
   * CCP will set to default country.
   * This function will not set default country as selected in CCP. To set default country in CCP
   * call resetToDefaultCountry() right after this call.
   * If invalid mDefaultCountryCode is applied, it won't be changed.
   *
   * @param defaultCountryCode code of your default country
   * if you want to set IN +91(India) as default country, mDefaultCountryCode =  91
   * if you want to set JP +81(Japan) as default country, mDefaultCountryCode =  81
   */
  @Deprecated public void setDefaultCountryUsingPhoneCode(int defaultCountryCode) {
    Country defaultCountry =
        CountryUtils.getByCode(getContext(), mPreferredCountries, defaultCountryCode);
    //if correct country is found, set the country
    if (defaultCountry != null) {
      this.mDefaultCountryCode = defaultCountryCode;
      setDefaultCountry(defaultCountry);
    }
  }

  /**
   * Default country name code defines your default country.
   * Whenever invalid / improper name code is found in setCountryForNameCode(), CCP will set to
   * default country.
   * This function will not set default country as selected in CCP. To set default country in CCP
   * call resetToDefaultCountry() right after this call.
   * If invalid mDefaultCountryCode is applied, it won't be changed.
   *
   * @param defaultCountryNameCode code of your default country
   * if you want to set IN +91(India) as default country, mDefaultCountryCode =  "IN" or "in"
   * if you want to set JP +81(Japan) as default country, mDefaultCountryCode =  "JP" or "jp"
   */
  public void setDefaultCountryUsingNameCode(String defaultCountryNameCode) {
    Country defaultCountry =
        CountryUtils.getByNameCodeFromAllCountries(getContext(), defaultCountryNameCode);
    //if correct country is found, set the country
    if (defaultCountry != null) {
      this.mDefaultCountryNameCode = defaultCountry.getNameCode();
      setDefaultCountry(defaultCountry);
    }
  }

  /**
   * Get Country Code of default country
   * i.e if default country is IN +91(India)  returns: "91"
   * if default country is JP +81(Japan) returns: "81"
   */
  public String getDefaultCountryCode() {
    return mDefaultCountry.getPhoneCode();
  }

  /**
   * * To get code of default country as Integer.
   *
   * @return integer value of default country code in CCP
   * i.e if default country is IN +91(India)  returns: 91
   * if default country is JP +81(Japan) returns: 81
   */
  public int getDefaultCountryCodeAsInt() {
    int code = 0;
    try {
      code = Integer.parseInt(getDefaultCountryCode());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return code;
  }

  /**
   * To get code of default country with prefix "+".
   *
   * @return String value of default country code in CCP with prefix "+"
   * i.e if default country is IN +91(India)  returns: "+91"
   * if default country is JP +81(Japan) returns: "+81"
   */
  public String getDefaultCountryCodeWithPlus() {
    return getContext().getString(R.string.phone_code, getDefaultCountryCode());
  }

  /**
   * To get name of default country.
   *
   * @return String value of country name, default in CCP
   * i.e if default country is IN +91(India)  returns: "India"
   * if default country is JP +81(Japan) returns: "Japan"
   */
  public String getDefaultCountryName() {
    return mDefaultCountry.getName();
  }

  /**
   * To get name code of default country.
   *
   * @return String value of country name, default in CCP
   * i.e if default country is IN +91(India)  returns: "IN"
   * if default country is JP +81(Japan) returns: "JP"
   */
  public String getDefaultCountryNameCode() {
    return mDefaultCountry.getNameCode().toUpperCase();
  }

  /**
   * Related to selected country
   */

  /**
   * reset the default country as selected country.
   */
  public void resetToDefaultCountry() {
    setSelectedCountry(mDefaultCountry);
  }

  /**
   * To get code of selected country.
   *
   * @return String value of selected country code in CCP
   * i.e if selected country is IN +91(India)  returns: "91"
   * if selected country is JP +81(Japan) returns: "81"
   */
  public String getSelectedCountryCode() {
    return mSelectedCountry.getPhoneCode();
  }

  /**
   * To get code of selected country with prefix "+".
   *
   * @return String value of selected country code in CCP with prefix "+"
   * i.e if selected country is IN +91(India)  returns: "+91"
   * if selected country is JP +81(Japan) returns: "+81"
   */
  public String getSelectedCountryCodeWithPlus() {
    return getContext().getString(R.string.phone_code, getSelectedCountryCode());
  }

  /**
   * * To get code of selected country as Integer.
   *
   * @return integer value of selected country code in CCP
   * i.e if selected country is IN +91(India)  returns: 91
   * if selected country is JP +81(Japan) returns: 81
   */
  public int getSelectedCountryCodeAsInt() {
    int code = 0;
    try {
      code = Integer.parseInt(getSelectedCountryCode());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return code;
  }

  /**
   * To get name of selected country.
   *
   * @return String value of country name, selected in CCP
   * i.e if selected country is IN +91(India)  returns: "India"
   * if selected country is JP +81(Japan) returns: "Japan"
   */
  public String getSelectedCountryName() {
    return mSelectedCountry.getName();
  }

  /**
   * To get name code of selected country.
   *
   * @return String value of country name, selected in CCP
   * i.e if selected country is IN +91(India)  returns: "IN"
   * if selected country is JP +81(Japan) returns: "JP"
   */
  public String getSelectedCountryNameCode() {
    return mSelectedCountry.getNameCode().toUpperCase();
  }

  /**
   * This will set country with @param countryCode as country code, in CCP
   *
   * @param countryCode a valid country code.
   * If you want to set IN +91(India), countryCode= 91
   * If you want to set JP +81(Japan), countryCode= 81
   */
  public void setCountryForPhoneCode(int countryCode) {
    Country country = CountryUtils.getByCode(getContext(), mPreferredCountries, countryCode);
    if (country == null) {
      if (mDefaultCountry == null) {
        mDefaultCountry =
            CountryUtils.getByCode(getContext(), mPreferredCountries, mDefaultCountryCode);
      }
      setSelectedCountry(mDefaultCountry);
    } else {
      setSelectedCountry(country);
    }
  }

  /**
   * This will set country with @param countryNameCode as country name code, in CCP
   *
   * @param countryNameCode a valid country name code.
   * If you want to set IN +91(India), countryCode= IN
   * If you want to set JP +81(Japan), countryCode= JP
   */
  public void setCountryForNameCode(String countryNameCode) {
    Country country = CountryUtils.getByNameCodeFromAllCountries(getContext(), countryNameCode);
    if (country == null) {
      if (mDefaultCountry == null) {
        mDefaultCountry =
            CountryUtils.getByCode(getContext(), mPreferredCountries, mDefaultCountryCode);
      }
      setSelectedCountry(mDefaultCountry);
    } else {
      setSelectedCountry(country);
    }
  }

  /**
   * All functions that work with fullNumber need an editText to write and read carrier number of
   * full number.
   * An editText for carrier number must be registered in order to use functions like
   * setFullNumber() and getFullNumber().
   *
   * @param editTextCarrierNumber - an editText where user types carrier number ( the part of full
   * number other than country code).
   */
  public void registerCarrierNumberEditText(AppCompatEditText editTextCarrierNumber) {
    setEdtRegisteredCarrierNumber(editTextCarrierNumber);
  }

  /**
   * This function combines selected country code from CCP and carrier number from @param
   * editTextCarrierNumber
   *
   * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number=
   * 8866667722, this will return "918866667722"
   */
  public String getFullNumber() {
    String fullNumber;
    if (mEdtRegisteredCarrierNumber != null) {
      fullNumber =
          mSelectedCountry.getPhoneCode() + mEdtRegisteredCarrierNumber.getText().toString();
    } else {
      fullNumber = mSelectedCountry.getPhoneCode();
      Log.w(TAG, "EditText for carrier number is not registered. Register it "
          + "using registerCarrierNumberEditText() before getFullNumber() or setFullNumber().");
    }
    return fullNumber;
  }

  /**
   * Separate out country code and carrier number from fullNumber.
   * Sets country of separated country code in CCP and carrier number as text of
   * editTextCarrierNumber
   * If no valid country code is found from full number, CCP will be set to default country code and
   * full number will be set as carrier number to editTextCarrierNumber.
   *
   * @param fullNumber is combination of country code and carrier number,
   * (country_code+carrier_number) for example if country is India (+91) and carrier/mobile number
   * is 8866667722 then full number will be 9188666667722 or +918866667722. "+" in starting of
   * number is optional.
   */
  public void setFullNumber(String fullNumber) {
    Country country = CountryUtils.getByNumber(getContext(), mPreferredCountries, fullNumber);
    setSelectedCountry(country);
    String carrierNumber = detectCarrierNumber(fullNumber, country);
    if (mEdtRegisteredCarrierNumber != null) {
      mEdtRegisteredCarrierNumber.setText(carrierNumber);
    } else {
      Log.w(TAG,
          "EditText for carrier number is not registered. Register it using registerCarrierNumberEditText() before getFullNumber() or setFullNumber().");
    }
  }

  /**
   * This function combines selected country code from CCP and carrier number from @param
   * editTextCarrierNumber and prefix "+"
   *
   * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number=
   * 8866667722, this will return "+918866667722"
   */
  public String getFullNumberWithPlus() {
    return getContext().getString(R.string.phone_code, getFullNumber());
  }

  /**
   * @return content color of CCP's text and small downward arrow.
   */
  public int getTextColor() {
    return mTextColor;
  }

  public int getDefaultContentColor() {
    return DEFAULT_TEXT_COLOR;
  }

  /**
   * Sets text and small down arrow color of CCP.
   *
   * @param contentColor color to apply to text and down arrow
   */
  public void setTextColor(int contentColor) {
    this.mTextColor = contentColor;
    mTvSelectedCountry.setTextColor(this.mTextColor);
    mImvArrow.setColorFilter(this.mTextColor, PorterDuff.Mode.SRC_IN);
  }

  public int getBackgroundColor() {
    return mBackgroundColor;
  }

  public void setBackgroundColor(int backgroundColor) {
    this.mBackgroundColor = backgroundColor;
    mRlyHolder.setBackgroundColor(backgroundColor);
  }

  public int getDefaultBackgroundColor() {
    return DEFAULT_BACKGROUND_COLOR;
  }

  /**
   * Modifies size of text in side CCP view.
   *
   * @param textSize size of text in pixels
   */
  public void setTextSize(int textSize) {
    if (textSize > 0) {
      mTvSelectedCountry.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
      setArrowSize(textSize);
      setFlagSize(textSize);
    }
  }

  /**
   * Modifies size of downArrow in CCP view
   *
   * @param arrowSize size in pixels
   */
  private void setArrowSize(int arrowSize) {
    if (arrowSize > 0) {
      LayoutParams params = (LayoutParams) mImvArrow.getLayoutParams();
      params.width = arrowSize;
      params.height = arrowSize;
      mImvArrow.setLayoutParams(params);
    }
  }

  /**
   * If nameCode of country in CCP view is not required use this to show/hide country name code of
   * ccp view.
   *
   * @param hideNameCode true will remove country name code from ccp view, it will result  " +91 "
   * false will show country name code in ccp view, it will result " (IN) +91 "
   */
  public void hideNameCode(boolean hideNameCode) {
    this.mHideNameCode = hideNameCode;
    setSelectedCountry(mSelectedCountry);
  }

  /**
   * This will set preferred countries using their name code. Prior preferred countries will be
   * replaced by these countries.
   * Preferred countries will be at top of country selection box.
   * If more than one countries have same country code, country in preferred list will have higher
   * priory than others. e.g. Canada and US have +1 as their country code. If "us" is set as
   * preferred country then US will be selected whenever setCountryForPhoneCode(1); or
   * setFullNumber("+1xxxxxxxxx"); is called.
   *
   * @param mCountryPreference is country name codes separated by comma. e.g. "us,in,nz"
   */
  public void setCountryPreference(String mCountryPreference) {
    this.mCountryPreference = mCountryPreference;
  }

  /**
   * To change font of ccp views
   */
  public void setTypeFace(Typeface typeFace) {
    mTypeFace = typeFace;
    try {
      mTvSelectedCountry.setTypeface(typeFace);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setTypeFace(String fontAssetPath) {
    try {
      Typeface typeFace = Typeface.createFromAsset(getContext().getAssets(), fontAssetPath);
      mTypeFace = typeFace;
      mTvSelectedCountry.setTypeface(typeFace);
    } catch (Exception e) {
      Log.d(TAG, "Invalid fontPath. " + e.toString());
    }
  }

  /**
   * To change font of ccp views along with style.
   */
  public void setTypeFace(Typeface typeFace, int style) {
    try {
      mTvSelectedCountry.setTypeface(typeFace, style);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Typeface getTypeFace() {
    return mTypeFace;
  }

  /**
   * To get call back on country selection a mOnCountryChangeListener must be registered.
   */
  public void setOnCountryChangeListener(OnCountryChangeListener onCountryChangeListener) {
    this.mOnCountryChangeListener = onCountryChangeListener;
  }

  /**
   * Modifies size of flag in CCP view
   *
   * @param flagSize size in pixels
   */
  public void setFlagSize(int flagSize) {
    mImvFlag.getLayoutParams().height = flagSize;
    mImvFlag.requestLayout();
  }

  public void showFlag(boolean showFlag) {
    this.mShowFlag = showFlag;
    if (showFlag) {
      mLlyFlagHolder.setVisibility(VISIBLE);
    } else {
      mLlyFlagHolder.setVisibility(GONE);
    }
  }

  public void showFullName(boolean showFullName) {
    this.mShowFullName = showFullName;
    setSelectedCountry(mSelectedCountry);
  }

  /**
   * SelectionDialogSearch is the facility to search through the list of country while selecting.
   *
   * @return true if search is set allowed
   */
  public boolean isSelectionDialogShowSearch() {
    return mSelectionDialogShowSearch;
  }

  /**
   * SelectionDialogSearch is the facility to search through the list of country while selecting.
   *
   * @param selectionDialogShowSearch true will allow search and false will hide search box
   */
  public void setSelectionDialogShowSearch(boolean selectionDialogShowSearch) {
    this.mSelectionDialogShowSearch = selectionDialogShowSearch;
  }

  @Override public boolean isClickable() {
    return mIsClickable;
  }

  /**
   * Allow click and open dialog
   */
  public void setClickable(boolean isClickable) {
    this.mIsClickable = isClickable;
    if (!isClickable) {
      mRlyClickConsumer.setOnClickListener(null);
      mRlyClickConsumer.setClickable(false);
      mRlyClickConsumer.setEnabled(false);
    } else {
      mRlyClickConsumer.setOnClickListener(countryCodeHolderClickListener);
      mRlyClickConsumer.setClickable(true);
      mRlyClickConsumer.setEnabled(true);
    }
  }

  public boolean isHidePhoneCode() {
    return mHidePhoneCode;
  }

  //TODO: Check this
  public void setHidePhoneCode(boolean mHidePhoneCode) {
    this.mHidePhoneCode = mHidePhoneCode;

    // Reset the view
    if (!mHideNameCode) {
      if (mShowFullName) {
        if (!mHidePhoneCode) {
          mTvSelectedCountry.setText(
              getContext().getString(R.string.country_full_name_and_phone_code,
                  mSelectedCountry.getName().toUpperCase(), mSelectedCountry.getPhoneCode()));
        } else {
          mTvSelectedCountry.setText(mSelectedCountry.getName().toUpperCase());
        }
      } else {
        if (!mHidePhoneCode) {
          mTvSelectedCountry.setText(getContext().getString(R.string.country_code_and_phone_code,
              mSelectedCountry.getNameCode().toUpperCase(), mSelectedCountry.getPhoneCode()));
        } else {
          mTvSelectedCountry.setText(mSelectedCountry.getNameCode().toUpperCase());
        }
      }
    } else {
      mTvSelectedCountry.setText(getContext().getString(R.string.phone_code, mSelectedCountry.getPhoneCode()));
    }
  }

  /*
    interface to set change listener
     */
  public interface OnCountryChangeListener {
    void onCountrySelected(Country selectedCountry);
  }
}
