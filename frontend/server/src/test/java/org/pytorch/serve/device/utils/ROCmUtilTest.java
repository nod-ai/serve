package org.pytorch.serve.device.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.pytorch.serve.device.Accelerator;
import org.pytorch.serve.device.AcceleratorVendor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ROCmUtilTest {

  private ROCmUtil rocmUtil;
  private JsonObject sampleMetricsJsonObject;
  private JsonObject sampleDiscoveryJsonObject;

  @BeforeClass
  public void setUp() {
    rocmUtil = new ROCmUtil();
    String sampleDiscoveryJsonString = """
        {
          "card0": {
            "Average Graphics Package Power (W)": "101.0",
            "Card Series": "AMD INSTINCT MI250 (MCM) OAM AC MBA",
            "Card Model": "0x740c",
            "Card Vendor": "Advanced Micro Devices, Inc. [AMD/ATI]",
            "Card SKU": "D65210V",
            "Subsystem ID": "0x0b0c",
            "Device Rev": "0x01",
            "Node ID": "4",
            "GUID": "57586",
            "GFX Version": "gfx9010"
          },
          "card1": {
            "Average Graphics Package Power (W)": "N/A (Secondary die)",
            "Card Series": "AMD INSTINCT MI250 (MCM) OAM AC MBA",
            "Card Model": "0x740c",
            "Card Vendor": "Advanced Micro Devices, Inc. [AMD/ATI]",
            "Card SKU": "D65210V",
            "Subsystem ID": "0x0b0c",
            "Device Rev": "0x01",
            "Node ID": "5",
            "GUID": "45873",
            "GFX Version": "gfx9010"
          }
        }
            """;

    String sampleMetricsJson = """
            {
              "card0": {
                "Device Name": "AMD INSTINCT MI250 (MCM) OAM AC MBA",
                "Device ID": "0x740c",
                "Device Rev": "0x01",
                "Subsystem ID": "0x0b0c",
                "GUID": "57586",
                "Average Graphics Package Power (W)": "102.0",
                "GPU use (%)": "50",
                "GFX Activity": "81364057",
                "GPU Memory Allocated (VRAM%)": "0",
                "GPU Memory Read/Write Activity (%)": "0",
                "Memory Activity": "12131476",
                "Avg. Memory Bandwidth": "0",
                "VRAM Total Memory (B)": "68702699520",
                "VRAM Total Used Memory (B)": "51527024640",
                "Card Series": "AMD INSTINCT MI250 (MCM) OAM AC MBA",
                "Card Model": "0x740c",
                "Card Vendor": "Advanced Micro Devices, Inc. [AMD/ATI]",
                "Card SKU": "D65210V",
                "Node ID": "4",
                "GFX Version": "gfx9010"
              },
              "card1": {
                "Device Name": "AMD INSTINCT MI250 (MCM) OAM AC MBA",
                "Device ID": "0x740c",
                "Device Rev": "0x01",
                "Subsystem ID": "0x0b0c",
                "GUID": "45873",
                "Average Graphics Package Power (W)": "N/A (Secondary die)",
                "GPU use (%)": "0",
                "GFX Activity": "87427303",
                "GPU Memory Allocated (VRAM%)": "0",
                "GPU Memory Read/Write Activity (%)": "0",
                "Memory Activity": "13102149",
                "Avg. Memory Bandwidth": "0",
                "VRAM Total Memory (B)": "68702699520",
                "VRAM Total Used Memory (B)": "11665408",
                "Card Series": "AMD INSTINCT MI250 (MCM) OAM AC MBA",
                "Card Model": "0x740c",
                "Card Vendor": "Advanced Micro Devices, Inc. [AMD/ATI]",
                "Card SKU": "D65210V",
                "Node ID": "5",
                "GFX Version": "gfx9010"
              }
            }
        """;

    sampleDiscoveryJsonObject = JsonParser.parseString(sampleDiscoveryJsonString).getAsJsonObject();
    sampleMetricsJsonObject = JsonParser.parseString(sampleMetricsJson).getAsJsonObject();
  }

  @Test
  public void testGetGpuEnvVariableName() {
    assertEquals(rocmUtil.getGpuEnvVariableName(), "HIP_VISIBLE_DEVICES");
  }

  @Test
  public void testGetUtilizationSmiCommand() {
    String[] expectedCommand = {
        "rocm-smi",
        "--showid",
        "--showproductname",
        "--showuse",
        "--showmemuse",
        "--showmeminfo", "vram",
        "-P",
        "--json"
    };
    assertEqualsNoOrder(rocmUtil.getUtilizationSmiCommand(), expectedCommand);
  }

  @Test
  public void testExtractAccelerators() {
    List<JsonObject> accelerators = rocmUtil.extractAccelerators(sampleMetricsJsonObject);
    assertEquals(accelerators.size(), 2);
    assertEquals(accelerators.get(0).get("cardId").getAsString(), "card0");
    assertEquals(accelerators.get(1).get("cardId").getAsString(), "card1");
  }

  @Test
  public void testExtractAcceleratorId() {
    JsonObject card0Object = rocmUtil.extractAccelerators(sampleMetricsJsonObject).get(0);
    JsonObject card1Object = rocmUtil.extractAccelerators(sampleMetricsJsonObject).get(1);

    Integer acceleratorId0 = rocmUtil.extractAcceleratorId(card0Object);
    Integer acceleratorId1 = rocmUtil.extractAcceleratorId(card1Object);

    assertEquals(acceleratorId0, Integer.valueOf(0));
    assertEquals(acceleratorId1, Integer.valueOf(1));
  }

  @Test
  public void testJsonMetricsObjectToAccelerator() {
    JsonObject card0Object = rocmUtil.extractAccelerators(sampleMetricsJsonObject).get(0);
    Accelerator accelerator = rocmUtil.jsonObjectToAccelerator(card0Object);

    assertEquals(accelerator.getAcceleratorId(), Integer.valueOf(0));
    assertEquals(accelerator.getAcceleratorModel(), "AMD INSTINCT MI250 (MCM) OAM AC MBA");
    assertEquals(accelerator.getVendor(), AcceleratorVendor.AMD);
    assertEquals((float) accelerator.getUsagePercentage(), 50.0f);
    assertEquals((float) accelerator.getMemoryUtilizationPercentage(), 75.0f);
    assertEquals(accelerator.getMemoryAvailableMegaBytes(), Integer.valueOf(65520));
    assertEquals(accelerator.getMemoryUtilizationMegabytes(), Integer.valueOf(49140));
  }

  @Test
  public void testJsonDiscoveryObjectToAccelerator() {
    JsonObject card0Object = rocmUtil.extractAccelerators(sampleDiscoveryJsonObject).get(0);
    Accelerator accelerator = rocmUtil.jsonObjectToAccelerator(card0Object);

    assertEquals(accelerator.getAcceleratorId(), Integer.valueOf(0));
    assertEquals(accelerator.getAcceleratorModel(), "AMD INSTINCT MI250 (MCM) OAM AC MBA");
    assertEquals(accelerator.getVendor(), AcceleratorVendor.AMD);
  }

  @Test
  public void testSmiOutputToUpdatedAccelerators() {
    String smiOutput = sampleMetricsJsonObject.toString();
    LinkedHashSet<Integer> parsedGpuIds = new LinkedHashSet<>();
    parsedGpuIds.add(0);
    parsedGpuIds.add(1);

    ArrayList<Accelerator> accelerators = rocmUtil.smiOutputToUpdatedAccelerators(smiOutput, parsedGpuIds);

    assertEquals(accelerators.size(), 2);

    Accelerator accelerator0 = accelerators.get(0);
    assertEquals(accelerator0.getAcceleratorId(), Integer.valueOf(0));
    assertEquals(accelerator0.getAcceleratorModel(), "AMD INSTINCT MI250 (MCM) OAM AC MBA");
    assertEquals(accelerator0.getVendor(), AcceleratorVendor.AMD);
    assertEquals((float) accelerator0.getUsagePercentage(), 50.0f);
    assertEquals((float) accelerator0.getMemoryUtilizationPercentage(), 75.0f);
    assertEquals(accelerator0.getMemoryAvailableMegaBytes(), Integer.valueOf(65520));
    assertEquals(accelerator0.getMemoryUtilizationMegabytes(), Integer.valueOf(49140));

    Accelerator accelerator1 = accelerators.get(1);
    assertEquals(accelerator1.getAcceleratorId(), Integer.valueOf(1));
    assertEquals(accelerator1.getAcceleratorModel(), "AMD INSTINCT MI250 (MCM) OAM AC MBA");
    assertEquals(accelerator1.getVendor(), AcceleratorVendor.AMD);
    assertEquals((float) accelerator1.getUsagePercentage(), 0.0f);
    assertEquals((float) accelerator1.getMemoryUtilizationPercentage(), 0.016788768f);
    assertEquals(accelerator1.getMemoryAvailableMegaBytes(), Integer.valueOf(65520));
    assertEquals(accelerator1.getMemoryUtilizationMegabytes(), Integer.valueOf(11));
  }
}
