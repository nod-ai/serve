package org.pytorch.serve.device.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.pytorch.serve.device.Accelerator;
import org.pytorch.serve.device.AcceleratorVendor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AppleUtilTest {

                private AppleUtil appleUtil;
                private String jsonString;
                private JsonObject sampleOutputJson;

                @BeforeClass
                public void setUp() {
                                appleUtil = new AppleUtil();

                                jsonString = "{"
                                                                + "\"SPDisplaysDataType\" : ["
                                                                + "{"
                                                                + "\"_name\" : \"kHW_AppleM1Item\","
                                                                + "\"spdisplays_metalfamily\" : \"spdisplays_mtlgpufamilyapple7\","
                                                                + "\"spdisplays_ndrvs\" : ["
                                                                + "{"
                                                                + "\"_name\" : \"Color LCD\","
                                                                + "\"_spdisplays_display-product-id\" : \"a045\","
                                                                + "\"_spdisplays_display-serial-number\" : \"fd626d62\","
                                                                + "\"_spdisplays_display-vendor-id\" : \"610\","
                                                                + "\"_spdisplays_display-week\" : \"0\","
                                                                + "\"_spdisplays_display-year\" : \"0\","
                                                                + "\"_spdisplays_displayID\" : \"1\","
                                                                + "\"_spdisplays_pixels\" : \"2880 x 1800\","
                                                                + "\"_spdisplays_resolution\" : \"1440 x 900 @ 60.00Hz\","
                                                                + "\"spdisplays_ambient_brightness\" : \"spdisplays_no\","
                                                                + "\"spdisplays_connection_type\" : \"spdisplays_internal\","
                                                                + "\"spdisplays_main\" : \"spdisplays_yes\","
                                                                + "\"spdisplays_mirror\" : \"spdisplays_off\","
                                                                + "\"spdisplays_online\" : \"spdisplays_yes\","
                                                                + "\"spdisplays_pixelresolution\" : \"2880 x 1800\","
                                                                + "\"spdisplays_resolution\" : \"1440 x 900 @ 60.00Hz\""
                                                                + "}"
                                                                + "],"
                                                                + "\"spdisplays_vendor\" : \"sppci_vendor_Apple\","
                                                                + "\"sppci_bus\" : \"spdisplays_builtin\","
                                                                + "\"sppci_cores\" : \"7\","
                                                                + "\"sppci_device_type\" : \"spdisplays_gpu\","
                                                                + "\"sppci_model\" : \"Apple M1\""
                                                                + "}"
                                                                + "]"
                                                                + "}";
                                sampleOutputJson = JsonParser.parseString(jsonString).getAsJsonObject();
                }

                @Test
                public void testGetGpuEnvVariableName() {
                                assertNull(appleUtil.getGpuEnvVariableName());
                }

                @Test
                public void testGetUtilizationSmiCommand() {
                                String[] expectedCommand = {
                                                                "system_profiler",
                                                                "-json",
                                                                "-detailLevel",
                                                                "mini",
                                                                "SPDisplaysDataType"
                                };
                                assertEqualsNoOrder(appleUtil.getUtilizationSmiCommand(), expectedCommand);
                }

                @Test
                public void testJsonObjectToAccelerator() {
                                JsonObject gpuObject = sampleOutputJson.getAsJsonArray("SPDisplaysDataType").get(0)
                                                                .getAsJsonObject();
                                Accelerator accelerator = appleUtil.jsonObjectToAccelerator(gpuObject);

                                assertNotNull(accelerator);
                                assertEquals(accelerator.getAcceleratorModel(), "Apple M1");
                                assertEquals(accelerator.getVendor(), AcceleratorVendor.APPLE);
                                assertEquals(accelerator.getAcceleratorId(), Integer.valueOf(0));
                                assertEquals(accelerator.getUsagePercentage(), Float.valueOf(0f));
                                assertEquals(accelerator.getMemoryUtilizationPercentage(), Float.valueOf(0f));
                                assertEquals(accelerator.getMemoryUtilizationMegabytes(), Integer.valueOf(0));
                }

                @Test
                public void testExtractAcceleratorId() {
                                JsonObject gpuObject = sampleOutputJson.getAsJsonArray("SPDisplaysDataType").get(0)
                                                                .getAsJsonObject();
                                assertEquals(appleUtil.extractAcceleratorId(gpuObject), Integer.valueOf(0));
                }

                @Test
                public void testExtractAccelerators() {
                                List<JsonObject> accelerators = appleUtil.extractAccelerators(sampleOutputJson);

                                assertEquals(accelerators.size(), 1);
                                assertEquals(accelerators.get(0).get("sppci_model").getAsString(), "Apple M1");
                }

                @Test
                public void testSmiOutputToUpdatedAccelerators() {
                                LinkedHashSet<Integer> parsedGpuIds = new LinkedHashSet<>();
                                parsedGpuIds.add(0);

                                ArrayList<Accelerator> updatedAccelerators = appleUtil.smiOutputToUpdatedAccelerators(
                                                                jsonString, parsedGpuIds);

                                assertEquals(updatedAccelerators.size(), 1);
                                Accelerator accelerator = updatedAccelerators.get(0);
                                assertEquals(accelerator.getAcceleratorModel(), "Apple M1");
                                assertEquals(accelerator.getVendor(), AcceleratorVendor.APPLE);
                                assertEquals(accelerator.getAcceleratorId(), Integer.valueOf(0));
                }

                @Test
                public void testGetAvailableAccelerators() {
                                LinkedHashSet<Integer> availableAcceleratorIds = new LinkedHashSet<>();
                                availableAcceleratorIds.add(0);

                                // Mock the callSMI method to return our sample output
                                AppleUtil spyAppleUtil = new AppleUtil() {
                                                @Override
                                                public String[] getUtilizationSmiCommand() {
                                                                return new String[] { "echo", sampleOutputJson
                                                                                                .toString() };
                                                }
                                };

                                ArrayList<Accelerator> availableAccelerators = spyAppleUtil
                                                                .getAvailableAccelerators(availableAcceleratorIds);

                                assertEquals(availableAccelerators.size(), 1);
                                Accelerator accelerator = availableAccelerators.get(0);
                                assertEquals(accelerator.getAcceleratorModel(), "Apple M1");
                                assertEquals(accelerator.getVendor(), AcceleratorVendor.APPLE);
                                assertEquals(accelerator.getAcceleratorId(), Integer.valueOf(0));
                }
}
