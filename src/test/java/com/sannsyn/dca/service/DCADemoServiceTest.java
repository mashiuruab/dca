package com.sannsyn.dca.service;

import com.sannsyn.dca.service.DCADemoService;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mashiur on 2/22/16.
 */
public class DCADemoServiceTest {
  private DCADemoService dcaDemoService;

  public DCADemoServiceTest() {
    dcaDemoService = new DCADemoService();
  }

  @Test
  public void demoMethodTest() {
    String methodParameter = "Hello World";
    String expected = methodParameter;
    Assert.assertEquals("demo testMethod test", expected, dcaDemoService.getString(methodParameter));
  }
}
