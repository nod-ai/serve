package org.pytorch.serve.device;

import org.testng.annotations.Test;
import org.testng.Assert;
import java.util.LinkedHashSet;

import org.pytorch.serve.device.interfaces.IAcceleratorUtility;

public class SystemInfoTest {

  @Test
  public void testDetectVendorType() {
    // This test assumes no accelerator commands are available
    AcceleratorVendor vendor = SystemInfo.detectVendorType();
    Assert.assertEquals(vendor, AcceleratorVendor.UNKNOWN);
  }

  @Test
  public void testHasAccelerators() {
    SystemInfo systemInfo = new SystemInfo();
    Assert.assertFalse(systemInfo.hasAccelerators());
  }

  @Test
  public void testGetNumberOfAccelerators() {
    SystemInfo systemInfo = new SystemInfo();
    Assert.assertEquals(systemInfo.getNumberOfAccelerators(), Integer.valueOf(0));
  }

  @Test
  public void testParseVisibleDevicesEnv() {
    LinkedHashSet<Integer> result = IAcceleratorUtility.parseVisibleDevicesEnv("0,1,2");
    Assert.assertEquals(result.size(), 3);
    Assert.assertTrue(result.contains(0));
    Assert.assertTrue(result.contains(1));
    Assert.assertTrue(result.contains(2));

    result = IAcceleratorUtility.parseVisibleDevicesEnv("0, 1, 2");
    Assert.assertEquals(result.size(), 3);
    Assert.assertTrue(result.contains(0));
    Assert.assertTrue(result.contains(1));
    Assert.assertTrue(result.contains(2));

    result = IAcceleratorUtility.parseVisibleDevicesEnv("0,0,2");
    Assert.assertEquals(result.size(), 2);
    Assert.assertTrue(result.contains(0));
    Assert.assertTrue(result.contains(2));

    result = IAcceleratorUtility.parseVisibleDevicesEnv("");
    Assert.assertTrue(result.isEmpty());

    result = IAcceleratorUtility.parseVisibleDevicesEnv(null);
    Assert.assertTrue(result.isEmpty());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseVisibleDevicesEnvInvalidInput() {
    IAcceleratorUtility.parseVisibleDevicesEnv("0,1,a");
  }

  @Test
  public void testBytesToMegabytes() {
    Assert.assertEquals(IAcceleratorUtility.bytesToMegabytes(1048576L), Integer.valueOf(1));
    Assert.assertEquals(IAcceleratorUtility.bytesToMegabytes(2097152L), Integer.valueOf(2));
    Assert.assertEquals(IAcceleratorUtility.bytesToMegabytes(0L), Integer.valueOf(0));
  }
}
