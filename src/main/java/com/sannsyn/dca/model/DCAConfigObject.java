package com.sannsyn.dca.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiur on 2/24/16.
 */
public class DCAConfigObject {
  private List<String> leftMenuItems = new ArrayList<String>();

  public List<String> getLeftMenuItems() {
    return leftMenuItems;
  }

  public void setLeftMenuItems(final List<String> pLeftMenuItems) {
    leftMenuItems = pLeftMenuItems;
  }
}
