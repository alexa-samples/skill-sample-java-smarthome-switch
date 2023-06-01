// -*- coding: utf-8 -*-

// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

// Licensed under the Amazon Software License (the "License"). You may not use this file except in
// compliance with the License. A copy of the License is located at

//    http://aws.amazon.com/asl/

// or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for the specific
// language governing permissions and limitations under the License.

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.json.*;

public class AlexaHandler {

    public static void handler(InputStream inputStream, OutputStream outputStream, Context context) {

        try {

            String request = getRequest(inputStream);

            System.out.println("Request:");
            System.out.println(request);

            JSONObject jsonRequest = new JSONObject(request);
            JSONObject directive = (JSONObject) jsonRequest.get("directive");
            JSONObject header = (JSONObject) directive.get("header");

            AlexaResponse alexaResponse;

            String namespace = header.optString("namespace", "INVALID");
            String correlationToken = header.optString("correlationToken", "INVALID");

            switch(namespace) {

                case "Alexa.Authorization":

                    System.out.println("Found Alexa.Authorization Namespace");
                    alexaResponse = new AlexaResponse("Alexa.Authorization","AcceptGrant", "INVALID", "INVALID", correlationToken);

                    break;

                case "Alexa.Discovery":

                    System.out.println("Found Alexa.Discovery Namespace");

                    alexaResponse = new AlexaResponse("Alexa.Discovery", "Discover.Response");

                    String capabilityAlexa = alexaResponse.createPayloadEndpointCapability("AlexaInterface", "Alexa", "3", null);
                    String capabilityAlexaPowerController = alexaResponse.createPayloadEndpointCapability("AlexaInterface", "Alexa.PowerController", "3", "{\"supported\": [ { \"name\": \"powerState\" } ] }");
                    String capabilities = "[" + capabilityAlexa + ", " + capabilityAlexaPowerController + "]";

                    alexaResponse.addPayloadEndpoint("Sample Switch", "sample-switch-01", capabilities);

                    // For another way to see how to craft an AlexaResponse, have a look at AlexaResponseTest:ResponseDiscovery

                    break;

                case "Alexa.PowerController":

                    System.out.println("Found Alexa.PowerController Namespace");

                    String endpointId = directive.getJSONObject("endpoint").optString("endpointId", "INVALID");
                    String token = directive.getJSONObject("endpoint").getJSONObject("scope").optString("token", "INVALID");
                    String powerStateValue = directive.getJSONObject("header").optString("name", "TurnOn");
                    String value = powerStateValue.equals("TurnOn") ? "ON" : "OFF";

                    // Set the value in the DynamodDB table SampleSmartHome
                    if(sendDeviceState(endpointId, "powerState", value)) {

                        alexaResponse = new AlexaResponse("Alexa", "Response", endpointId, token, correlationToken);
                        alexaResponse.addContextProperty("Alexa.PowerController", "powerState", value, 200);

                    } else {

                        alexaResponse = new AlexaResponse("Alexa", "ErrorResponse");
                    }

                    break;

                default:

                    System.out.println("INVALID Namespace");
                    alexaResponse = new AlexaResponse();

                    break;
            }

            System.out.println("Response:");
            System.out.println(alexaResponse);

            outputStream.write(alexaResponse.toString().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean sendDeviceState(String endpointId, String state, String value) {

        String attributeValue = state + "Value";

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("SampleSmartHome");

        UpdateItemSpec updateItemSpec =
                new UpdateItemSpec()
                        .withPrimaryKey("ItemId", endpointId)
                        .withUpdateExpression("set #v = :val1")
                        .withNameMap(new NameMap().with("#v", attributeValue))
                        .withValueMap(new ValueMap().withString(":val1", value))
                        .withReturnValues(ReturnValue.ALL_NEW);

        UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
//        System.out.println(outcome.getItem().toJSONPretty());

        return true;
    }

    static String getRequest(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
