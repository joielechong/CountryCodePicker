package com.rilixtech;

import android.support.annotation.DrawableRes;

/**
 * Created by hbb20 on 11/1/16.
 *
 * Clean up and moving all Country related code to {@link CountryUtils}.
 * a pojo should be a pojo and no more.
 *
 * Updated by Joielechong 13 May 2017
 */
public class Country {
  private String iso;
  private String phoneCode;
  private String name;

  public Country(String iso, String phoneCode, String name) {
    this.iso = iso;
    this.phoneCode = phoneCode;
    this.name = name;
  }

  public String getIso() {
    return iso;
  }

  public void setIso(String iso) {
    this.iso = iso;
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

  @DrawableRes
  public int getFlagDrawableResId() {
    return CountryUtils.getFlagDrawableResId(this);
  }

  /**
   * If country have query word in name or name code or phone code, this will return true.
   */
  boolean isEligibleForQuery(String query) {
    query = query.toLowerCase();
    return getName().toLowerCase().contains(query)
        || getIso().toLowerCase().contains(query)
        || getPhoneCode().toLowerCase().contains(query);
  }
}
