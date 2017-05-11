package com.rilixtech;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hbb20 on 11/1/16.
 */
class Country {
  private static String TAG = Country.class.getSimpleName();

  private String nameCode;
  private String phoneCode;
  private String name;

  public Country() {

  }

  public Country(String nameCode, String phoneCode, String name) {
    this.nameCode = nameCode;
    this.phoneCode = phoneCode;
    this.name = name;
  }

  /**
   * This function parses the raw/countries.xml file, and get list of all the countries.
   *
   * @param context: required to access application resources (where country.xml is).
   * @return List of all the countries available in xml file.
   */
  public static List<Country> readXMLofCountries(Context context) {
    List<Country> countries = new ArrayList<>();
    try {
      XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
      XmlPullParser xmlPullParser = xmlFactoryObject.newPullParser();
      InputStream ins = context.getResources().openRawResource(R.raw.countries);
      xmlPullParser.setInput(ins, null);
      int event = xmlPullParser.getEventType();
      while (event != XmlPullParser.END_DOCUMENT) {
        String name = xmlPullParser.getName();
        switch (event) {
          case XmlPullParser.START_TAG:
            break;
          case XmlPullParser.END_TAG:
            if (name.equals("country")) {
              Country country = new Country();
              country.setNameCode(xmlPullParser.getAttributeValue(null, "code").toUpperCase());
              country.setPhoneCode(xmlPullParser.getAttributeValue(null, "phoneCode"));
              country.setName(xmlPullParser.getAttributeValue(null, "name"));
              countries.add(country);
            }
            break;
        }
        event = xmlPullParser.next();
      }
    } catch (XmlPullParserException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {

    }
    return countries;
  }

  /**
   * Search a country which matches @param code.
   *
   * @param preferredCountries is list of preference countries.
   * @param code phone code. i.e "91" or "1"
   * @return Country that has phone code as @param code.
   * or returns null if no country matches given code.
   * if same code (e.g. +1) available for more than one country ( US, canada) , this function will
   * return preferred country.
   */
  private static Country getCountryForCode(Context context, List<Country> preferredCountries,
      String code) {

    /**
     * check in preferred countries
     */
    if (preferredCountries != null && !preferredCountries.isEmpty()) {
      for (Country country : preferredCountries) {
        if (country.getPhoneCode().equals(code)) {
          return country;
        }
      }
    }

    for (Country country : CountryUtils.getLibraryMasterCountries(context)) {
      if (country.getPhoneCode().equals(code)) {
        return country;
      }
    }
    return null;
  }

  public static List<Country> getCustomMasterCountryList(CountryCodePicker codePicker) {
    codePicker.refreshCustomMasterList();
    if (codePicker.getCustomMasterCountriesList() != null
        && codePicker.getCustomMasterCountriesList().size() > 0) {
      return codePicker.getCustomMasterCountriesList();
    } else {
      return CountryUtils.getLibraryMasterCountries(codePicker.getContext());
    }
  }

  /**
   * Search a country which matches @param nameCode.
   *
   * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
   * @return Country that has phone code as @param code.
   * or returns null if no country matches given code.
   */
  public static Country getCountryForNameCodeFromCustomMasterList(Context context,
      List<Country> customMasterCountriesList, String nameCode) {
    if (customMasterCountriesList == null || customMasterCountriesList.size() == 0) {
      return getCountryForNameCodeFromLibraryMasterList(context, nameCode);
    } else {
      for (Country country : customMasterCountriesList) {
        if (country.getNameCode().equalsIgnoreCase(nameCode)) {
          return country;
        }
      }
    }
    return null;
  }

  /**
   * Search a country which matches @param nameCode.
   *
   * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
   * @return Country that has phone code as @param code.
   * or returns null if no country matches given code.
   */
  public static Country getCountryForNameCodeFromLibraryMasterList(Context context,
      String nameCode) {
    List<Country> countries = CountryUtils.getLibraryMasterCountries(context);
    for (Country country : countries) {
      if (country.getNameCode().equalsIgnoreCase(nameCode)) {
        return country;
      }
    }
    return null;
  }

  /**
   * Search a country which matches @param nameCode.
   *
   * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
   * @return Country that has phone code as @param code.
   * or returns null if no country matches given code.
   */
  public static Country getCountryForNameCodeTrial(String nameCode, Context context) {
    List<Country> countries = Country.readXMLofCountries(context);
    for (Country country : countries) {
      if (country.getNameCode().equalsIgnoreCase(nameCode)) {
        return country;
      }
    }
    return null;
  }

  /**
   * Search a country which matches @param code.
   *
   * @param preferredCountries list of country with priority,
   * @param code phone code. i.e 91 or 1
   * @return Country that has phone code as @param code.
   * or returns null if no country matches given code.
   */
  static Country getCountryForCode(Context context, List<Country> preferredCountries, int code) {
    return getCountryForCode(context, preferredCountries, code + "");
  }

  /**
   * Finds country code by matching substring from left to right from full number.
   * For example. if full number is +819017901357
   * function will ignore "+" and try to find match for first character "8"
   * if any country found for code "8", will return that country. If not, then it will
   * try to find country for "81". and so on till first 3 characters ( maximum number of characters
   * in country code is 3).
   *
   * @param preferredCountries countries of preference
   * @param fullNumber full number ( "+" (optional)+ country code + carrier number) i.e.
   * +819017901357 / 819017901357 / 918866667722
   * @return Country JP +81(Japan) for +819017901357 or 819017901357
   * Country IN +91(India) for  918866667722
   * null for 2956635321 ( as neither of "2", "29" and "295" matches any country code)
   */
  static Country getCountryForNumber(Context context, List<Country> preferredCountries,
      String fullNumber) {
    int firstDigit;
    if (fullNumber.length() != 0) {
      if (fullNumber.charAt(0) == '+') {
        firstDigit = 1;
      } else {
        firstDigit = 0;
      }
      Country country;
      for (int i = firstDigit; i < firstDigit + 4; i++) {
        String code = fullNumber.substring(firstDigit, i);
        country = Country.getCountryForCode(context, preferredCountries, code);
        if (country != null) {
          return country;
        }
      }
    }
    return null;
  }

  public int getFlagID() {
    return CountryUtils.getFlagResID(this);
  }

  public String getNameCode() {
    return nameCode;
  }

  public void setNameCode(String nameCode) {
    this.nameCode = nameCode;
  }

  public String getPhoneCode() {
    return phoneCode;
  }

  public void setPhoneCode(String phoneCode) {
    this.phoneCode = phoneCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void log() {
    try {
      Log.d(TAG, "Country->" + nameCode + ":" + phoneCode + ":" + name);
    } catch (NullPointerException ex) {
      Log.d(TAG, "Null");
    }
  }

  public String logString() {
    return nameCode.toUpperCase() + " +" + phoneCode + "(" + name + ")";
  }

  /**
   * If country have query word in name or name code or phone code, this will return true.
   */
  public boolean isEligibleForQuery(String query) {
    query = query.toLowerCase();
    return getName().toLowerCase().contains(query)
        || getNameCode().toLowerCase().contains(query)
        || getPhoneCode().toLowerCase().contains(query);
  }
}
