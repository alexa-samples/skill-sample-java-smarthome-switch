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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class AlexaHandlerTest {

    private static String sampleUri = "https://raw.githubusercontent.com/alexa/alexa-smarthome/master/sample_messages/";

    private JSONObject GetResponse(String json) {

        InputStream inputStream = new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8")) );
        OutputStream outputStream = new OutputStream()
        {
            private StringBuilder sb = new StringBuilder();
            @Override
            public void write(int b) throws IOException {
                this.sb.append((char) b );
            }

            public String toString(){
                return this.sb.toString();
            }
        };

        AlexaHandler.handler(inputStream, outputStream, null);

        String responseString = outputStream.toString();
        return new JSONObject(responseString);
    }

    private String GetSample(String url)
    {
        StringBuilder sb = new StringBuilder();
        try {
            URL iurl = new URL(url);
            HttpURLConnection c = (HttpURLConnection)iurl.openConnection();
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                case 202:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    @Test
    public void TestAuthorization()
    {
        JSONObject response = GetResponse(GetSample(sampleUri + "Authorization/Authorization.AcceptGrant.request.json"));

        String namespace = response.getJSONObject("event").getJSONObject("header").get("namespace").toString();
        String name = response.getJSONObject("event").getJSONObject("header").get("name").toString();

        assertEquals("Namespace should be Alexa.Authorization", "Alexa.Authorization", namespace);
        assertEquals("Name should be AcceptGrant", "AcceptGrant", name);

    }

    @Test
    public void TestDiscovery()
    {
        JSONObject response = GetResponse(GetSample(sampleUri + "Discovery/Discovery.request.json"));

        String namespace = response.getJSONObject("event").getJSONObject("header").get("namespace").toString();
        String name = response.getJSONObject("event").getJSONObject("header").get("name").toString();

        assertEquals("Namespace should be Alexa.Discovery", "Alexa.Discovery", namespace);
        assertEquals("Name should be Discover.Response", "Discover.Response", name);
    }

    @Test
    public void TestPowerControllerOff()
    {
        JSONObject response = GetResponse(GetSample(sampleUri + "PowerController/PowerController.TurnOff.request.json"));

        String namespace = response.getJSONObject("event").getJSONObject("header").get("namespace").toString();
        String name = response.getJSONObject("event").getJSONObject("header").get("name").toString();

        assertEquals("Namespace should be Alexa", "Alexa", namespace);
        assertEquals("Name should be Response", "Response", name);
    }

}
