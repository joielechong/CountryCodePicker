package com.rilixtech;

import android.util.Log;

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

//  /**
//   * This function parses the raw/countries.xml file, and get list of all the countries.
//   *
//   * @param context: required to access application resources (where country.xml is).
//   * @return List of all the countries available in xml file.
//   */
//  private static List<Country> readXMLofCountries(Context context) {
//    List<Country> countries = new ArrayList<>();
//    try {
//      XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
//      XmlPullParser xmlPullParser = xmlFactoryObject.newPullParser();
//      InputStream ins = context.getResources().openRawResource(R.raw.countries);
//      xmlPullParser.setInput(ins, null);
//      int event = xmlPullParser.getEventType();
//      while (event != XmlPullParser.END_DOCUMENT) {
//        String name = xmlPullParser.getName();
//        switch (event) {
//          case XmlPullParser.START_TAG:
//            break;
//          case XmlPullParser.END_TAG:
//            if (name.equals("country")) {
//              Country country = new Country();
//              country.setNameCode(xmlPullParser.getAttributeValue(null, "code").toUpperCase());
//              country.setPhoneCode(xmlPullParser.getAttributeValue(null, "phoneCode"));
//              country.setName(xmlPullParser.getAttributeValue(null, "name"));
//              countries.add(country);
//            }
//            break;
//        }
//        event = xmlPullParser.next();
//      }
//    } catch (XmlPullParserException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    } finally {
//
//    }
//    return countries;
//  }

//  /**
//   * Search a country which matches @param nameCode.
//   *
//   * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
//   * @return Country that has phone code as @param code.
//   * or returns null if no country matches given code.
//   */
//  private static Country getCountryForNameCodeTrial(String nameCode, Context context) {
//    List<Country> countries = Country.readXMLofCountries(context);
//    for (Country country : countries) {
//      if (country.getNameCode().equalsIgnoreCase(nameCode)) {
//        return country;
//      }
//    }
//    return null;
//  }


  public int getFlagDrawableResId() {
    return CountryUtils.getFlagDrawableResId(this);
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
