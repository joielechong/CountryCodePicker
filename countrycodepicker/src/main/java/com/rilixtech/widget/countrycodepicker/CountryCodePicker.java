package com.rilixtech.widget.countrycodepicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CountryCodePicker extends RelativeLayout {
  private static String TAG = CountryCodePicker.class.getSimpleName();

  private final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();
  private static final String DEFAULT_ISO_COUNTRY = "ID";
  private static final int DEFAULT_TEXT_COLOR = 0;
  private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;

  private int mBackgroundColor = DEFAULT_BACKGROUND_COLOR;

  private int mDefaultCountryCode;
  private String mDefaultCountryNameCode;

  //Util
  private PhoneNumberUtil mPhoneUtil;
  private PhoneNumberWatcher mPhoneNumberWatcher;
  PhoneNumberInputValidityListener mPhoneNumberInputValidityListener;

  private TextView mTvSelectedCountry;
  private TextView mRegisteredPhoneNumberTextView;
  private RelativeLayout mRlyHolder;
  private ImageView mImvArrow;
  private ImageView mImvFlag;
  private LinearLayout mLlyFlagHolder;
  private Country mSelectedCountry;
  private Country mDefaultCountry;
  private RelativeLayout mRlyClickConsumer;
  private View.OnClickListener mCountryCodeHolderClickListener;

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

  private int mTextColor = DEFAULT_TEXT_COLOR;

  private int mDialogTextColor = DEFAULT_TEXT_COLOR;

  // Font typeface
  private Typeface mTypeFace;

  private boolean mIsHintEnabled = true;
  private boolean mIsEnablePhoneNumberWatcher = true;

  private boolean mSetCountryByTimeZone = true;

  private OnCountryChangeListener mOnCountryChangeListener;

  /**
   * interface to set change listener
   */
  public interface OnCountryChangeListener {
    void onCountrySelected(Country selectedCountry);
  }

  /**
   * Interface for checking when phone number checker validity is finish.
   */
  public interface PhoneNumberInputValidityListener {
    void onFinish(CountryCodePicker ccp, boolean isValid);
  }

  public CountryCodePicker(Context context) {
    super(context);
    //if (!isInEditMode())
      init(null);
  }

  public CountryCodePicker(Context context, AttributeSet attrs) {
    super(context, attrs);
    //if (!isInEditMode())
      init(attrs);
  }

  public CountryCodePicker(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    //if (!isInEditMode())
      init(attrs);
  }

  @SuppressWarnings("unused")
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public CountryCodePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    //if (!isInEditMode())
      init(attrs);
  }

  private void init(AttributeSet attrs) {
    inflate(getContext(), R.layout.country_code_picker_layout_code_picker, this);

    mTvSelectedCountry = findViewById(R.id.selected_country_tv);
    mRlyHolder = findViewById(R.id.country_code_holder_rly);
    mImvArrow = findViewById(R.id.arrow_imv);
    mImvFlag = findViewById(R.id.flag_imv);
    mLlyFlagHolder = findViewById(R.id.flag_holder_lly);
    mRlyClickConsumer = findViewById(R.id.click_consumer_rly);

    applyCustomProperty(attrs);

    mCountryCodeHolderClickListener = new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (isClickable()) {
          showCountryCodePickerDialog();
        }
      }
    };

    mRlyClickConsumer.setOnClickListener(mCountryCodeHolderClickListener);
  }

  private void applyCustomProperty(AttributeSet attrs) {
    mPhoneUtil = PhoneNumberUtil.createInstance(getContext());
    Resources.Theme theme = getContext().getTheme();
    TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0);

    try {
      mHidePhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hidePhoneCode, false);
      mShowFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false);
      mHideNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hideNameCode, false);

      mIsHintEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_enableHint, true);

      // enable auto formatter for phone number input
      mIsEnablePhoneNumberWatcher =
          a.getBoolean(R.styleable.CountryCodePicker_ccp_enablePhoneAutoFormatter, true);

      setKeyboardAutoPopOnSearch(
          a.getBoolean(R.styleable.CountryCodePicker_ccp_keyboardAutoPopOnSearch, true));

      mCustomMasterCountries = a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries);
      refreshCustomMasterList();

      mCountryPreference = a.getString(R.styleable.CountryCodePicker_ccp_countryPreference);
      refreshPreferredCountries();

      applyCustomPropertyOfDefaultCountryNameCode(a);

      showFlag(a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true));

      applyCustomPropertyOfColor(a);

      // text font
      String fontPath = a.getString(R.styleable.CountryCodePicker_ccp_textFont);
      if (fontPath != null && !fontPath.isEmpty()) setTypeFace(fontPath);

      //text size
      int textSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0);
      if (textSize > 0) {
        mTvSelectedCountry.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        setFlagSize(textSize);
        setArrowSize(textSize);
      } else { //no text size specified
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int defaultSize = Math.round(18 * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        setTextSize(defaultSize);
      }

      //if arrow arrow size is explicitly defined
      int arrowSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0);
      if (arrowSize > 0) setArrowSize(arrowSize);

      mSelectionDialogShowSearch =
          a.getBoolean(R.styleable.CountryCodePicker_ccp_selectionDialogShowSearch, true);
      setClickable(a.getBoolean(R.styleable.CountryCodePicker_ccp_clickable, true));

      mSetCountryByTimeZone =
          a.getBoolean(R.styleable.CountryCodePicker_ccp_setCountryByTimeZone, true);

      // Set to default phone code if no country name code set in attribute.
      if (mDefaultCountryNameCode == null || mDefaultCountryNameCode.isEmpty()) {
        setDefaultCountryFlagAndCode();
      }
    } catch (Exception e) {
      Log.d(TAG, "exception = " + e.toString());
      if (isInEditMode()) {
        mTvSelectedCountry.setText(
            getContext().getString(R.string.phone_code,
                getContext().getString(R.string.country_indonesia_number)));
      } else {
        mTvSelectedCountry.setText(e.getMessage());
      }
    } finally {
      a.recycle();
    }
  }

  private void applyCustomPropertyOfDefaultCountryNameCode(TypedArray tar) {
    //default country
    mDefaultCountryNameCode = tar.getString(R.styleable.CountryCodePicker_ccp_defaultNameCode);
    if (BuildConfig.DEBUG) {
      Log.d(TAG, "mDefaultCountryNameCode from attribute = " + mDefaultCountryNameCode);
    }

    if (mDefaultCountryNameCode == null || mDefaultCountryNameCode.isEmpty()) return;

    if (mDefaultCountryNameCode.trim().isEmpty()) {
      mDefaultCountryNameCode = null;
      return;
    }

    setDefaultCountryUsingNameCode(mDefaultCountryNameCode);
    setSelectedCountry(mDefaultCountry);
  }

  private void applyCustomPropertyOfColor(TypedArray arr) {
    //text color
    int textColor;
    if (isInEditMode()) {
      textColor = arr.getColor(R.styleable.CountryCodePicker_ccp_textColor, DEFAULT_TEXT_COLOR);
    } else {
      textColor = arr.getColor(R.styleable.CountryCodePicker_ccp_textColor,
          getColor(getContext(), R.color.defaultTextColor));
    }
    if (textColor != 0) setTextColor(textColor);

    mDialogTextColor =
        arr.getColor(R.styleable.CountryCodePicker_ccp_dialogTextColor, DEFAULT_TEXT_COLOR);

    // background color of view.
    mBackgroundColor =
        arr.getColor(R.styleable.CountryCodePicker_ccp_backgroundColor, Color.TRANSPARENT);

    if (mBackgroundColor != Color.TRANSPARENT) mRlyHolder.setBackgroundColor(mBackgroundColor);
  }

  private Country getDefaultCountry() {
    return mDefaultCountry;
  }

  private void setDefaultCountry(Country defaultCountry) {
    mDefaultCountry = defaultCountry;
  }

  @SuppressWarnings("unused") private Country getSelectedCountry() {
    return mSelectedCountry;
  }

  protected void setSelectedCountry(Country selectedCountry) {
    mSelectedCountry = selectedCountry;

    Context ctx = getContext();

    //as soon as country is selected, textView should be updated
    if (selectedCountry == null) {
      selectedCountry = CountryUtils.getByCode(ctx, mPreferredCountries, mDefaultCountryCode);
    }

    if (mRegisteredPhoneNumberTextView != null) {
      String ISO = selectedCountry.getIso().toUpperCase();
      setPhoneNumberWatcherToTextView(mRegisteredPhoneNumberTextView, ISO);
    }

    if (mOnCountryChangeListener != null) {
      mOnCountryChangeListener.onCountrySelected(selectedCountry);
    }

    mImvFlag.setImageResource(CountryUtils.getFlagDrawableResId(selectedCountry));

    if (mIsHintEnabled) setPhoneNumberHint();

    setSelectedCountryText(ctx, selectedCountry);
  }

  private void setSelectedCountryText(Context ctx, Country selectedCountry) {
    if (mHideNameCode && mHidePhoneCode && !mShowFullName) {
      mTvSelectedCountry.setText("");
      return;
    }

    String phoneCode = selectedCountry.getPhoneCode();
    if (mShowFullName) {
      String countryName = selectedCountry.getName().toUpperCase();

      if (mHidePhoneCode && mHideNameCode) {
        mTvSelectedCountry.setText(countryName);
        return;
      }

      if (mHideNameCode) {
        mTvSelectedCountry.setText(ctx.getString(R.string.country_full_name_and_phone_code,
            countryName, phoneCode));
        return;
      }

      String ISO = selectedCountry.getIso().toUpperCase();
      if (mHidePhoneCode) {
        mTvSelectedCountry.setText(ctx.getString(R.string.country_full_name_and_name_code,
            countryName, ISO));
        return;
      }

      mTvSelectedCountry.setText(ctx.getString(R.string.country_full_name_name_code_and_phone_code,
          countryName, ISO, phoneCode));

      return;
    }

    if (mHideNameCode && mHidePhoneCode) {
      String countryName = selectedCountry.getName().toUpperCase();
      mTvSelectedCountry.setText(countryName);
      return;
    }

    if (mHideNameCode) {
      mTvSelectedCountry.setText(ctx.getString(R.string.phone_code, phoneCode));
      return;
    }

    if (mHidePhoneCode) {
      String iso = selectedCountry.getIso().toUpperCase();
      mTvSelectedCountry.setText(iso);
      return;
    }

    String iso = selectedCountry.getIso().toUpperCase();
    mTvSelectedCountry.setText(ctx.getString(R.string.country_code_and_phone_code, iso, phoneCode));
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
    mKeyboardAutoPopOnSearch = keyboardAutoPopOnSearch;
  }

  /**
   * Get status of phone number formatter.
   *
   * @return enable or not.
   */
  @SuppressWarnings("unused") public boolean isPhoneAutoFormatterEnabled() {
    return mIsEnablePhoneNumberWatcher;
  }

  /**
   * Enable or disable auto formatter for phone number inserted to TextView.
   * You need to set an EditText for phone number with `registerPhoneNumberTextView()`
   * to make use of this.
   *
   * @param isEnable return if phone auto formatter enabled or not.
   */
  @SuppressWarnings("unused") public void enablePhoneAutoFormatter(boolean isEnable) {
    mIsEnablePhoneNumberWatcher = isEnable;
    if (isEnable) {
      if (mPhoneNumberWatcher == null) {
        mPhoneNumberWatcher = new PhoneNumberWatcher(getSelectedCountryNameCode());
      }
    } else {
      mPhoneNumberWatcher = null;
    }
  }

  @SuppressWarnings("unused") private OnClickListener getCountryCodeHolderClickListener() {
    return mCountryCodeHolderClickListener;
  }

  /**
   * this will load mPreferredCountries based on mCountryPreference
   */
  void refreshPreferredCountries() {
    if (mCountryPreference == null || mCountryPreference.length() == 0) {
      mPreferredCountries = null;
      return;
    }

    List<Country> localCountryList = new ArrayList<>();
    for (String nameCode : mCountryPreference.split(",")) {
      Country country =
          CountryUtils.getByNameCodeFromCustomCountries(getContext(), mCustomMasterCountriesList,
              nameCode);
      if (country == null) continue;
      //to avoid duplicate entry of country
      if (isAlreadyInList(country, localCountryList)) continue;
      localCountryList.add(country);
    }

    if (localCountryList.size() == 0) {
      mPreferredCountries = null;
    } else {
      mPreferredCountries = localCountryList;
    }
  }

  /**
   * this will load mPreferredCountries based on mCountryPreference
   */
  void refreshCustomMasterList() {
    if (mCustomMasterCountries == null || mCustomMasterCountries.length() == 0) {
      mCustomMasterCountriesList = null;
      return;
    }

    List<Country> localCountries = new ArrayList<>();
    String[] split = mCustomMasterCountries.split(",");
    for (int i = 0; i < split.length; i++) {
      String nameCode = split[i];
      Country country = CountryUtils.getByNameCodeFromAllCountries(getContext(), nameCode);
      if (country == null) continue;
      //to avoid duplicate entry of country
      if (isAlreadyInList(country, localCountries)) continue;
      localCountries.add(country);
    }

    if (localCountries.size() == 0) {
      mCustomMasterCountriesList = null;
    } else {
      mCustomMasterCountriesList = localCountries;
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
  List<Country> getCustomCountries(@NonNull CountryCodePicker codePicker) {
    codePicker.refreshCustomMasterList();
    if (codePicker.getCustomCountries() == null || codePicker.getCustomCountries().size() <= 0) {
      return CountryUtils.getAllCountries(codePicker.getContext());
    } else {
      return codePicker.getCustomCountries();
    }
  }

  @SuppressWarnings("unused")
  public void setCustomMasterCountriesList(@Nullable List<Country> customMasterCountriesList) {
    mCustomMasterCountriesList = customMasterCountriesList;
  }

  @SuppressWarnings("unused") public String getCustomMasterCountries() {
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
  @SuppressWarnings("unused")
  public void setCustomMasterCountries(@Nullable String customMasterCountries) {
    mCustomMasterCountries = customMasterCountries;
  }

  /**
   * This will match name code of all countries of list against the country's name code.
   *
   * @param countries list of countries against which country will be checked.
   * @return if country name code is found in list, returns true else return false
   */
  private boolean isAlreadyInList(Country country, List<Country> countries) {
    if (country == null || countries == null) return false;

    for (int i = 0; i < countries.size(); i++) {
      if (countries.get(i).getIso().equalsIgnoreCase(country.getIso())) {
        return true;
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

    if (defaultCountry == null) return;

    //if correct country is found, set the country
    mDefaultCountryCode = defaultCountryCode;
    setDefaultCountry(defaultCountry);
  }

  public void setDefaultCountryUsingPhoneCodeAndApply(int defaultCountryCode) {
    Country defaultCountry =
        CountryUtils.getByCode(getContext(), mPreferredCountries, defaultCountryCode);

    if (defaultCountry == null) return;

    //if correct country is found, set the country
    mDefaultCountryCode = defaultCountryCode;
    setDefaultCountry(defaultCountry);

    resetToDefaultCountry();
  }

  /**
   * Default country name code defines your default country.
   * Whenever invalid / improper name code is found in setCountryForNameCode(), CCP will set to
   * default country.
   * This function will not set default country as selected in CCP. To set default country in CCP
   * call resetToDefaultCountry() right after this call.
   * If invalid countryIso is applied, it won't be changed.
   *
   * @param countryIso code of your default country
   * if you want to set IN +91(India) as default country, countryIso =  "IN" or "in"
   * if you want to set JP +81(Japan) as default country, countryIso =  "JP" or "jp"
   */
  public void setDefaultCountryUsingNameCode(@NonNull String countryIso) {
    Country defaultCountry = CountryUtils.getByNameCodeFromAllCountries(getContext(), countryIso);

    if (defaultCountry == null) return;

    //if correct country is found, set the country
    mDefaultCountryNameCode = defaultCountry.getIso();
    setDefaultCountry(defaultCountry);
  }

  /**
   * Set default country as selected in CountryCodePicker.
   *
   * There is no change applied if invalid countryIso is given.
   *
   * @param countryIso code of your default country
   * if you want to set IN +91(India) as default country, countryIso =  "IN" or "in"
   * if you want to set JP +81(Japan) as default country, countryIso =  "JP" or "jp"
   */
  public void setDefaultCountryUsingNameCodeAndApply(@NonNull String countryIso) {
    Country defaultCountry = CountryUtils.getByNameCodeFromAllCountries(getContext(), countryIso);

    if (defaultCountry == null) return;

    //if correct country is found, set the country
    mDefaultCountryNameCode = defaultCountry.getIso();
    setDefaultCountry(defaultCountry);

    //TODO: This part of code need to be optimized!!.

    setEmptyDefault(null);
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
  @SuppressWarnings("unused") public int getDefaultCountryCodeAsInt() {
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
  @SuppressWarnings("unused") public String getDefaultCountryCodeWithPlus() {
    return getContext().getString(R.string.phone_code, getDefaultCountryCode());
  }

  /**
   * To get name of default country.
   *
   * @return String value of country name, default in CCP
   * i.e if default country is IN +91(India)  returns: "India"
   * if default country is JP +81(Japan) returns: "Japan"
   */
  @SuppressWarnings("unused")
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
    return mDefaultCountry.getIso().toUpperCase();
  }

  /**
   * reset the default country as selected country.
   */
  @SuppressWarnings("unused") public void resetToDefaultCountry() {
    setEmptyDefault();
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
  @SuppressWarnings("unused")
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
  @SuppressWarnings("unused") public int getSelectedCountryCodeAsInt() {
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
  @SuppressWarnings("unused")
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
    return mSelectedCountry.getIso().toUpperCase();
  }

  /**
   * This will set country with @param countryCode as country code, in CCP
   *
   * @param countryCode a valid country code.
   * If you want to set IN +91(India), countryCode= 91
   * If you want to set JP +81(Japan), countryCode= 81
   */
  @SuppressWarnings("unused")
  public void setCountryForPhoneCode(int countryCode) {
    Context ctx = getContext();
    Country country = CountryUtils.getByCode(ctx, mPreferredCountries, countryCode);
    if (country == null) {
      if (mDefaultCountry == null) {
        mDefaultCountry = CountryUtils.getByCode(ctx, mPreferredCountries, mDefaultCountryCode);
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
  @SuppressWarnings("unused")
  public void setCountryForNameCode(@NonNull String countryNameCode) {
    Context ctx = getContext();
    Country country = CountryUtils.getByNameCodeFromAllCountries(ctx, countryNameCode);
    if (country == null) {
      if (mDefaultCountry == null) {
        mDefaultCountry = CountryUtils.getByCode(ctx, mPreferredCountries, mDefaultCountryCode);
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
   * @param textView - an editText where user types carrier number ( the part of full
   * number other than country code).
   */
  @SuppressWarnings("unused")
  public void registerPhoneNumberTextView(@NonNull TextView textView) {
    setRegisteredPhoneNumberTextView(textView);
    if (mIsHintEnabled) setPhoneNumberHint();
  }

  @SuppressWarnings("unused") public TextView getRegisteredPhoneNumberTextView() {
    return mRegisteredPhoneNumberTextView;
  }

  void setRegisteredPhoneNumberTextView(@NonNull TextView phoneNumberTextView) {
    mRegisteredPhoneNumberTextView = phoneNumberTextView;
    if (mIsEnablePhoneNumberWatcher) {
      if (mPhoneNumberWatcher == null) {
        mPhoneNumberWatcher = new PhoneNumberWatcher(getDefaultCountryNameCode());
      }
      mRegisteredPhoneNumberTextView.addTextChangedListener(mPhoneNumberWatcher);
    }
  }

  private void setPhoneNumberWatcherToTextView(TextView textView, String countryNameCode) {
    if (!mIsEnablePhoneNumberWatcher) return;

    if (mPhoneNumberWatcher == null) {
      mPhoneNumberWatcher = new PhoneNumberWatcher(countryNameCode);
      textView.addTextChangedListener(mPhoneNumberWatcher);
    } else {
      if (!mPhoneNumberWatcher.getPreviousCountryCode().equalsIgnoreCase(countryNameCode)) {
        textView.removeTextChangedListener(mPhoneNumberWatcher);
        mPhoneNumberWatcher = new PhoneNumberWatcher(countryNameCode);
        textView.addTextChangedListener(mPhoneNumberWatcher);
      }
    }
  }

  /**
   * This function combines selected country code from CCP and carrier number from @param
   * editTextCarrierNumber
   *
   * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number=
   * 8866667722, this will return "918866667722"
   */
  public String getFullNumber() {
    String fullNumber = mSelectedCountry.getPhoneCode();
    if (mRegisteredPhoneNumberTextView == null) {
      Log.w(TAG, getContext().getString(R.string.error_unregister_carrier_number));
    } else {
      fullNumber += mRegisteredPhoneNumberTextView.getText().toString();
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
  @SuppressWarnings("unused")
  public void setFullNumber(@NonNull String fullNumber) {
    Country country = CountryUtils.getByNumber(getContext(), mPreferredCountries, fullNumber);
    setSelectedCountry(country);
    String carrierNumber = detectCarrierNumber(fullNumber, country);
    if (mRegisteredPhoneNumberTextView == null) {
      Log.w(TAG, getContext().getString(R.string.error_unregister_carrier_number));
    } else {
      mRegisteredPhoneNumberTextView.setText(carrierNumber);
    }
  }

  /**
   * This function combines selected country code from CCP and carrier number from @param
   * editTextCarrierNumber and prefix "+"
   *
   * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number=
   * 8866667722, this will return "+918866667722"
   */
  @SuppressWarnings("unused")
  public String getFullNumberWithPlus() {
    return getContext().getString(R.string.phone_code, getFullNumber());
  }

  /**
   * @return content color of CCP's text and small downward arrow.
   */
  @SuppressWarnings("unused")
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
    mTextColor = contentColor;
    mTvSelectedCountry.setTextColor(contentColor);
    mImvArrow.setColorFilter(contentColor, PorterDuff.Mode.SRC_IN);
  }

  public int getBackgroundColor() {
    return mBackgroundColor;
  }

  public void setBackgroundColor(int backgroundColor) {
    mBackgroundColor = backgroundColor;
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
   * @param arrowSizeInDp size in dimension pixels
   */
  public void setArrowSize(int arrowSizeInDp) {
    if (arrowSizeInDp > 0) {
      LayoutParams params = (LayoutParams) mImvArrow.getLayoutParams();
      params.width = arrowSizeInDp;
      params.height = arrowSizeInDp;
      mImvArrow.setLayoutParams(params);
    }
  }

  /**
   * If nameCode of country in CCP view is not required use this to show/hide country name code of
   * ccp view.
   *
   * @param hide true will remove country name code from ccp view, it will result  " +91 "
   * false will show country name code in ccp view, it will result " (IN) +91 "
   */
  @SuppressWarnings("unused") public void hideNameCode(boolean hide) {
    mHideNameCode = hide;
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
   * @param countryPreference is country name codes separated by comma. e.g. "us,in,nz"
   */
  @SuppressWarnings("unused")
  public void setCountryPreference(@NonNull String countryPreference) {
    mCountryPreference = countryPreference;
  }

  /**
   * Set TypeFace for all the text in CCP
   *
   * @param typeFace TypeFace generated from assets.
   */
  @SuppressWarnings("unused") public void setTypeFace(@NonNull Typeface typeFace) {
    mTypeFace = typeFace;
    try {
      mTvSelectedCountry.setTypeface(typeFace);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * set TypeFace for all the text in CCP
   *
   * @param fontAssetPath font path in asset folder.
   */
  public void setTypeFace(@NonNull String fontAssetPath) {
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
  @SuppressWarnings("unused") public void setTypeFace(@NonNull Typeface typeFace, int style) {
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
  @SuppressWarnings("unused")
  public void setOnCountryChangeListener(@NonNull OnCountryChangeListener onCountryChangeListener) {
    mOnCountryChangeListener = onCountryChangeListener;
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
    mShowFlag = showFlag;
    mLlyFlagHolder.setVisibility(showFlag ? VISIBLE : GONE);
  }

  /**
   * Show full country name instead only iso name.
   *
   * @param show show or not.
   */
  @SuppressWarnings("unused") public void showFullName(boolean show) {
    mShowFullName = show;
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
  @SuppressWarnings("unused")
  public void setSelectionDialogShowSearch(boolean selectionDialogShowSearch) {
    mSelectionDialogShowSearch = selectionDialogShowSearch;
  }

  @Override public boolean isClickable() {
    return mIsClickable;
  }

  /**
   * Allow click and open dialog
   */
  public void setClickable(boolean isClickable) {
    mIsClickable = isClickable;
    mRlyClickConsumer.setOnClickListener(isClickable ? mCountryCodeHolderClickListener : null);
    mRlyClickConsumer.setClickable(isClickable);
    mRlyClickConsumer.setEnabled(isClickable);
  }

  public boolean isHidePhoneCode() {
    return mHidePhoneCode;
  }

  public boolean isHideNameCode() {
    return mHideNameCode;
  }

  /**
   * Check whether phone text sample hint is enabled or not.
   *
   * @return is hint enabled or not.
   */
  @SuppressWarnings("unused") public boolean isHintEnabled() {
    return mIsHintEnabled;
  }

  /**
   * Enable hint for phone number sample in registered TextView with registerPhoneNumberTextView()
   *
   * @param hintEnabled disable or enable hint.
   */
  @SuppressWarnings("unused") public void enableHint(boolean hintEnabled) {
    mIsHintEnabled = hintEnabled;
    if (mIsHintEnabled) setPhoneNumberHint();
  }

  /**
   * Hide or show phone code
   *
   * @param hide show or not show the phone code.
   */
  @SuppressWarnings("unused") public void hidePhoneCode(boolean hide) {
    mHidePhoneCode = hide;
    setSelectedCountry(mSelectedCountry);
  }

  private void setPhoneNumberHint() {
    // don't set phone number hint for null textView and country.
    if (mRegisteredPhoneNumberTextView == null
        || mSelectedCountry == null
        || mSelectedCountry.getIso() == null) {
      return;
    }

    String iso = mSelectedCountry.getIso().toUpperCase();
    PhoneNumberUtil.PhoneNumberType mobile = PhoneNumberUtil.PhoneNumberType.MOBILE;
    Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.getExampleNumberForType(iso, mobile);
    if (phoneNumber == null) {
      mRegisteredPhoneNumberTextView.setHint("");
      return;
    }

    if (BuildConfig.DEBUG) {
      Log.d(TAG, "setPhoneNumberHint called");
      Log.d(TAG, "mSelectedCountry.getIso() = " + mSelectedCountry.getIso());
      Log.d(TAG,
          "hint = " + mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
    }

    String hint = mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
    //if (mRegisteredPhoneNumberTextView.getHint() != null) {
    //  mRegisteredPhoneNumberTextView.setHint("");
    //}
    mRegisteredPhoneNumberTextView.setHint(hint);
  }

  private class PhoneNumberWatcher extends PhoneNumberFormattingTextWatcher {
    private boolean lastValidity;
    private String previousCountryCode = "";

    String getPreviousCountryCode() {
      return previousCountryCode;
    }

    @SuppressWarnings("unused") public PhoneNumberWatcher() {
      super();
    }

    //TODO solve it! support for android kitkat
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PhoneNumberWatcher(String countryCode) {
      super(countryCode);
      previousCountryCode = countryCode;
    }

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
      super.onTextChanged(s, start, before, count);
      try {
        String iso = null;
        if (mSelectedCountry != null) iso = mSelectedCountry.getPhoneCode().toUpperCase();
        Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.parse(s.toString(), iso);
        iso = mPhoneUtil.getRegionCodeForNumber(phoneNumber);
        if (iso != null) {
          //int countryIdx = mCountries.indexOfIso(iso);
          //mCountrySpinner.setSelection(countryIdx);
        }
      } catch (NumberParseException ignored) {
      }

      if (mPhoneNumberInputValidityListener != null) {
        boolean validity = isValid();
        if (validity != lastValidity) {
          mPhoneNumberInputValidityListener.onFinish(CountryCodePicker.this, validity);
        }
        lastValidity = validity;
      }
    }
  }

  /**
   * Get number
   *
   * @return Phone number in E.164 format | null on error
   */
  @SuppressWarnings("unused") public String getNumber() {
    Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();

    if (phoneNumber == null) return null;

    if (mRegisteredPhoneNumberTextView == null) {
      Log.w(TAG, getContext().getString(R.string.error_unregister_carrier_number));
      return null;
    }

    return mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
  }

  /**
   * Get PhoneNumber object
   *
   * @return Phone Number | null on error
   */
  @SuppressWarnings("unused") public Phonenumber.PhoneNumber getPhoneNumber() {
    try {
      String iso = null;
      if (mSelectedCountry != null) iso = mSelectedCountry.getIso().toUpperCase();
      if (mRegisteredPhoneNumberTextView == null) {
        Log.w(TAG, getContext().getString(R.string.error_unregister_carrier_number));
        return null;
      }
      return mPhoneUtil.parse(mRegisteredPhoneNumberTextView.getText().toString(), iso);
    } catch (NumberParseException ignored) {
      return null;
    }
  }

  /**
   * Check if number is valid
   *
   * @return boolean
   */
  @SuppressWarnings("unused") public boolean isValid() {
    Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
    return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber);
  }

  @SuppressWarnings("unused")
  public void setPhoneNumberInputValidityListener(PhoneNumberInputValidityListener listener) {
    mPhoneNumberInputValidityListener = listener;
  }

  /**
   * Set default value
   * Will try to retrieve phone number from device
   */
  private void setDefaultCountryFlagAndCode() {
    Context ctx = getContext();
    TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
    if (manager == null) {
      Log.e(TAG, "Can't access TelephonyManager. Using default county code");
      setEmptyDefault(getDefaultCountryCode());
      return;
    }

    try {
      String simCountryIso = manager.getSimCountryIso();
      if (simCountryIso == null || simCountryIso.isEmpty()) {
        String iso = manager.getNetworkCountryIso();
        if (iso == null || iso.isEmpty()) {
          enableSetCountryByTimeZone(true);
        } else {
          setEmptyDefault(iso);
          if (BuildConfig.DEBUG) Log.d(TAG, "isoNetwork = " + iso);
        }
      } else {
        setEmptyDefault(simCountryIso);
        if (BuildConfig.DEBUG) Log.d(TAG, "simCountryIso = " + simCountryIso);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error when getting sim country, error = " + e.toString());
      setEmptyDefault(getDefaultCountryCode());
    }
  }

  /**
   * Alias for setting empty string of default settings from the device (using locale)
   */
  private void setEmptyDefault() {
    setEmptyDefault(null);
  }

  /**
   * Set default value with default locale
   *
   * @param countryCode ISO2 of country
   */
  private void setEmptyDefault(String countryCode) {
    if (countryCode == null || countryCode.isEmpty()) {
      if (mDefaultCountryNameCode == null || mDefaultCountryNameCode.isEmpty()) {
        if (DEFAULT_COUNTRY == null || DEFAULT_COUNTRY.isEmpty()) {
          countryCode = DEFAULT_ISO_COUNTRY;
        } else {
          countryCode = DEFAULT_COUNTRY;
        }
      } else {
        countryCode = mDefaultCountryNameCode;
      }
    }

    if (mIsEnablePhoneNumberWatcher && mPhoneNumberWatcher == null) {
      mPhoneNumberWatcher = new PhoneNumberWatcher(countryCode);
    }

    setDefaultCountryUsingNameCode(countryCode);
    setSelectedCountry(getDefaultCountry());
  }

  /**
   * Set checking for country from time zone. This is used to set country whenever CCP can't
   * detect country from phone setting.
   *
   * @param isEnabled set enable or not.
   */
  public void enableSetCountryByTimeZone(boolean isEnabled) {
    if (isEnabled) {
      if (mDefaultCountryNameCode != null && !mDefaultCountryNameCode.isEmpty()) return;
      if (mRegisteredPhoneNumberTextView != null) return;
      if (mSetCountryByTimeZone) {
        TimeZone tz = TimeZone.getDefault();

        if (BuildConfig.DEBUG) Log.d(TAG, "tz.getID() = " + tz.getID());
        List<String> countryIsos = CountryUtils.getCountryIsoByTimeZone(getContext(), tz.getID());

        if (countryIsos == null) {
          // If no iso country found, fallback to device locale.
          setEmptyDefault();
        } else {
          setDefaultCountryUsingNameCode(countryIsos.get(0));
          setSelectedCountry(getDefaultCountry());
        }
      }
    }
    mSetCountryByTimeZone = isEnabled;
  }

  public int getDialogTextColor() {
    return mDialogTextColor;
  }

  @SuppressWarnings("unused")
  public void setDialogTextColor(int dialogTextColor) {
    mDialogTextColor = dialogTextColor;
  }

  public static int getColor(Context context, int id) {
    final int version = Build.VERSION.SDK_INT;
    if (version >= 23) {
      return context.getColor(id);
    } else {
      return context.getResources().getColor(id);
    }
  }

  public void showCountryCodePickerDialog() {
    if (mCountryCodeDialog == null) mCountryCodeDialog = new CountryCodeDialog(this);
    mCountryCodeDialog.show();
  }
}
