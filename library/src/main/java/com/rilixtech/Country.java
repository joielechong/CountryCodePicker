package com.rilixtech;

/**
 * Created by hbb20 on 11/1/16.
 *
 * Clean up and moving all Country related code to {@link CountryUtils}.
 * a pojo should be a pojo and no more.
 *
 * Updated by Joielechong 13 May 2017
 */
public class Country {
  private String nameCode;
  private String phoneCode;
  private String name;

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

  public int getFlagDrawableResId() {
    return CountryUtils.getFlagDrawableResId(this);
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
