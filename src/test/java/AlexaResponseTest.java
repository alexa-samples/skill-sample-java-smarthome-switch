// -*- coding: utf-8 -*-

// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

// Licensed under the Amazon Software License (the "License"). You may not use this file except in
// compliance with the License. A copy of the License is located at

//    http://aws.amazon.com/asl/

// or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for the specific
// language governing permissions and limitations under the License.

import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class AlexaResponseTest {

    @Test
    public void ResponseTest(){

        String responseString = new AlexaResponse().toString();
        JSONObject response = new JSONObject(responseString);
        String namespace = response.getJSONObject("event").getJSONObject("header").get("namespace").toString();
        String name = response.getJSONObject("event").getJSONObject("header").get("name").toString();

        assertEquals("Namespace should be Alexa", namespace, "Alexa");
        assertEquals("Name should be Response", name, "Response");
    }

    @Test
    public void ResponseCookieTest() {
        AlexaResponse ar = new AlexaResponse();
        ar.AddCookie("key", "value");
        JSONObject response = new JSONObject(ar.toString());
        String value = response.getJSONObject("event").getJSONObject("endpoint").getJSONObject("cookie").get("key").toString();
        assertEquals("Key value should be value", value, "value");
    }

    @Test
    public void ResponseCookieMultipleTest() {
        AlexaResponse ar = new AlexaResponse();
        ar.AddCookie("key1", "value1");
        ar.AddCookie("key2", "value2");
        JSONObject response = new JSONObject(ar.toString());

        String value1 = response.getJSONObject("event").getJSONObject("endpoint").getJSONObject("cookie").get("key1").toString();
        String value2 = response.getJSONObject("event").getJSONObject("endpoint").getJSONObject("cookie").get("key2").toString();

        assertEquals("Key1 value should be value1", value1, "value1");
        assertEquals("Key2 value should be value2", value2, "value2");
    }

    @Test
    public void ResponseErrorTest() {
        JSONObject payloadErrorObject = new JSONObject();
        payloadErrorObject.put("type", "INVALID_SOMETHING");
        payloadErrorObject.put("message", "ERROR_MESSAGE");

        AlexaResponse ar = new AlexaResponse("Alexa", "ErrorResponse");
        ar.SetPayload(payloadErrorObject.toString());

        JSONObject response = new JSONObject(ar.toString());

        String name = response.getJSONObject("event").getJSONObject("header").get("name").toString();
        assertEquals("Name should be ErrorResponse", name, "ErrorResponse");

        JSONObject payload = response.getJSONObject("event").getJSONObject("payload");
        assertEquals("Type should be INVALID_SOMETHING", payload.getString("type"), "INVALID_SOMETHING");
        assertEquals("Message should be ERROR_MESSAGE", payload.getString("message"), "ERROR_MESSAGE");
    }

    @Test
    public void ResponseDiscovery() {

        AlexaResponse ar = new AlexaResponse("Alexa.Discovery", "Discover.Response");

        JSONObject capability_alexa = new JSONObject(ar.CreatePayloadEndpointCapability("AlexaInterface", "Alexa", "3", null));

        JSONObject propertyPowerState = new JSONObject();
        propertyPowerState.put("name", "powerState");
        JSONObject capability_alexa_powercontroller = new JSONObject(ar.CreatePayloadEndpointCapability("AlexaInterface", "Alexa.PowerController", propertyPowerState.toString(), null));

        JSONArray capabilities = new JSONArray();
        capabilities.put(capability_alexa);
        capabilities.put(capability_alexa_powercontroller);

        ar.AddPayloadEndpoint("Sample Switch", "sample-switch-01", capabilities.toString());

        JSONObject response = new JSONObject(ar.toString());

        String namespace = response.getJSONObject("event").getJSONObject("header").get("namespace").toString();
        String name = response.getJSONObject("event").getJSONObject("header").get("name").toString();
        String friendlyName = response.getJSONObject("event").getJSONObject("payload").getJSONArray("endpoints").getJSONObject(0).getString("friendlyName");
        String type = response.getJSONObject("event").getJSONObject("payload").getJSONArray("endpoints").getJSONObject(0).getJSONArray("capabilities").getJSONObject(0).getString("type");
        String alexaInterface = response.getJSONObject("event").getJSONObject("payload").getJSONArray("endpoints").getJSONObject(0).getJSONArray("capabilities").getJSONObject(0).getString("interface");
        String alexaPowerControllerInterface = response.getJSONObject("event").getJSONObject("payload").getJSONArray("endpoints").getJSONObject(0).getJSONArray("capabilities").getJSONObject(1).getString("interface");

        assertEquals("Namespace should be Alexa.Discovery", "Alexa.Discovery", namespace);
        assertEquals("Name should be Discover.Response", "Discover.Response", name);
        assertEquals("Friendly Name should be Sample Switch","Sample Switch", friendlyName);
        assertEquals("Type should be AlexaInterface","AlexaInterface", type);
        assertEquals("Interface should be Alexa","Alexa", alexaInterface);
        assertEquals("Interface should be Alexa.PowerController","Alexa.PowerController", alexaPowerControllerInterface);
    }


}