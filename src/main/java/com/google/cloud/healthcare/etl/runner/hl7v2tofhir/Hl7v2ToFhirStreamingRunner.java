// Copyright 2020 Google LLC.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.cloud.healthcare.etl.runner.hl7v2tofhir;

import static com.google.cloud.healthcare.etl.model.ErrorEntry.ERROR_ENTRY_TAG;
import static com.google.cloud.healthcare.etl.pipeline.MappingFn.MAPPING_TAG;

import com.google.cloud.healthcare.etl.model.converter.ErrorEntryConverter;
import com.google.cloud.healthcare.etl.model.mapping.HclsApiHl7v2MappableMessage;
import com.google.cloud.healthcare.etl.model.mapping.HclsApiHl7v2MappableMessageCoder;
import com.google.cloud.healthcare.etl.model.mapping.MappedFhirMessageWithSourceTimeCoder;
import com.google.cloud.healthcare.etl.model.mapping.MappingOutput;
import com.google.cloud.healthcare.etl.pipeline.MappingFn;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO.Read;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.json.*;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import static org.junit.Assert.*;

import org.apache.beam.sdk.io.gcp.healthcare.FhirIO;
import org.apache.beam.sdk.io.gcp.healthcare.FhirIOWithMetrics;
import org.apache.beam.sdk.io.gcp.healthcare.HL7v2IO;
import org.apache.beam.sdk.io.gcp.healthcare.HL7v2Message;
import org.apache.beam.sdk.io.gcp.healthcare.HealthcareIOError;
import org.apache.beam.sdk.io.gcp.healthcare.HealthcareIOErrorToTableRow;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.options.ValueProvider.*;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTagList;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.Duration;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The entry point of the pipeline. Right now the pipeline has 3 components,
 * HL7v2 IO, mapping
 * function, and FHIR IO. The code for the IOs are shipped within this project
 * before next Beam
 * release.
 *
 * <p>
 * The errors for each component are handled separately, e.g. you can specify
 * file paths for each
 * of the stage (read - HL7v2 IO, mapping, write - FHIR IO). Right now the shard
 * is set to 1, if you
 * are seeing issues with regard to writing to GCS, feel free to bump it up to a
 * reasonable value.
 *
 * <p>
 * Currently message ids are not passed along to the mapping function. An
 * upcoming update will
 * fix this.
 */
public class Hl7v2ToFhirStreamingRunner {

    // TODO(b/155226578): add more sophisticated validations.
    /** Pipeline options. */
    public interface Options extends DataflowPipelineOptions {
        @Description("The PubSub subscription to listen to, must be of the full format: "
                + "projects/project_id/subscriptions/subscription_id.")
        @Required
        String getPubSubSubscription();

        void setPubSubSubscription(String subSubscription);

        @Description("The path to the mapping configurations. The path will be treated as a GCS path if the"
                + " path starts with the GCS scheme (\"gs\"), otherwise a local file. Please see: "
                + "https://github.com/Lakshmi-Priya-Ramisetty/healthcare-data-harmonization/blob/baa4e0c7849413f7b44505a8410ee7f52745427a/mapping_configs/README.md"
                + " for more details on the mapping configuration structure.")
        @Required
        String getMappingPath();

        void setMappingPath(String gcsPath);

        @Description("The target FHIR Store to write data to, must be of the full format: "
                + "projects/project_id/locations/location/datasets/dataset_id/fhirStores/fhir_store_id")
        @Required
        String getFhirStore();

        void setFhirStore(String fhirStore);

        @Description("The path that is used to record all read errors. The path will be treated as a GCS path"
                + " if the path starts with the GCS scheme (\"gs\"), otherwise a local file.")
        @Required
        String getReadErrorPath();

        void setReadErrorPath(String readErrorPath);

        @Description("The path that is used to record all write errors. The path will be "
                + "treated as a GCS path if the path starts with the GCS scheme (\"gs\"), otherwise a "
                + "local file.")
        @Required
        String getWriteErrorPath();

        void setWriteErrorPath(String writeErrorPath);

        @Description("The path that is used to record all mapping errors. The path will be "
                + "treated as a GCS path if the path starts with the GCS scheme (\"gs\"), otherwise a "
                + "local file.")
        @Required
        String getMappingErrorPath();

        void setMappingErrorPath(String mappingErrorPath);

        @Description("The number of shards when writing errors to GCS.")
        @Default.Integer(10)
        Integer getErrorLogShardNum();

        void setErrorLogShardNum(Integer shardNum);

        @Description("Whether enable metrics for performance evaluation.")
        @Default.Boolean(false)
        Boolean getEnablePerformanceMetrics();

        void setEnablePerformanceMetrics(Boolean enablePerformanceMetrics);
    }

    static class TypeCheckChannel extends DoFn<String, String> {
        @ProcessElement
        public void processElement(DoFn<String, String>.ProcessContext context) {
          String input = context.element();
          JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();
          JsonObject entityObj = jsonObject.getAsJsonObject("object");
          Boolean hasType = entityObj.has("type");
          if(!hasType){
              System.out.println("Type not exist:" + jsonObject.toString());
              //get type from api and insert into object
              String uri = entityObj.get("uri").getAsString();
              String accessToken = getAccessToken();
              String type = getTypeUsingEntity(uri, accessToken);

              //add type property to the object
              entityObj.addProperty("type", type);
              input = jsonObject.toString();
          }
          context.output(input);
        }


        public static String getTypeUsingEntity(String entity, String accessToken) {
            URI uri = null;
            try {
                String url = "https://gus-sales.reltio.com/reltio/api/SBhbGHGiAFQgp8v/" + entity;
                uri = new URIBuilder(url).build();
            } catch (URISyntaxException e) {
                System.out.println("Error in URI Builder: " + e.getMessage());
                return null;
            }
            HttpUriRequest request = RequestBuilder.get().setUri(uri)
                    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).build();
            
            HttpClient httpClient = HttpClients.createDefault();
            try {
                HttpResponse response = httpClient.execute(request);
                System.out.println("HttpResponse: executed successfully");
                HttpEntity responseEntity = response.getEntity();
                System.out.println("HttpResponse: got responseEntity successfully");
                String content = EntityUtils.toString(responseEntity);
                System.out.println("Got content: " + content);
    
                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                System.out.println(jsonObject.get("type").getAsString());
                
                return jsonObject.get("type").getAsString();
            } catch (Exception e) {
                System.out.println("Error in fetching the type: " + e.getMessage());
                return null;
            }
            
        }
    
        public static String getAccessToken() {
            URI uri = null;
            try {
                uri = new URIBuilder("https://auth.reltio.com/oauth/token")
                        .setParameter("username", "google.hde.fhir.connector").setParameter("password", "DrReLT0L111$")
                        .setParameter("grant_type", "password").build();
            } catch (URISyntaxException e) {
                System.out.println("Error in URI Builder: " + e.getMessage());
                return null;
            }
    
            HttpUriRequest request = RequestBuilder.post().setUri(uri)
                    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .setHeader(HttpHeaders.AUTHORIZATION, "Basic cmVsdGlvX3VpOm1ha2l0YQ==").build();
            HttpClient httpClient = HttpClients.custom().build();
            try {
                HttpResponse response = httpClient.execute(request);
                HttpEntity responseEntity = response.getEntity();
                String content = EntityUtils.toString(responseEntity);
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println(statusCode);
                System.out.println(content);
    
                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                System.out.println(jsonObject.get("access_token").getAsString());
    
                return jsonObject.get("access_token").getAsString();
            } catch (Exception e) {
                System.out.println("Error in fetching the token: " + e.getMessage());
                return null;
            }
        }
    }

    private static Duration ERROR_LOG_WINDOW_SIZE = Duration.standardSeconds(5);

    // createPipeline returns a HL7v2 to FHIR streaming pipeline without run. The
    // integration test
    // can then start and terminate the created pipeline whenever needed.
    public static Pipeline createPipeline(String[] args) {
        Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
        Pipeline pipeline = Pipeline.create(options);

        PCollection<String> readResult = pipeline
                .apply(
                        "ReadHL7v2Messages",
                        PubsubIO.readStrings().fromSubscription(options.getPubSubSubscription()));
        MappingFn<HclsApiHl7v2MappableMessage> mappingFn = MappingFn.of(options.getMappingPath(), options.getEnablePerformanceMetrics());

        SimpleFunction<String, HclsApiHl7v2MappableMessage> toMessage = new SimpleFunction<String, HclsApiHl7v2MappableMessage>() {
            @Override
            public HclsApiHl7v2MappableMessage apply(String input) {
                // JSONObject json = new JSONObject(input);
                JsonObject jsonObject = new JsonParser().parse(input).getAsJsonObject();
                System.out.println("Asserting json object is so");

                if (jsonObject.isJsonObject()) {
                    System.out.println("jsob object is surely A Json Object");
                }
                input = jsonObject.toString();
                System.out.println(input);
                return HclsApiHl7v2MappableMessage.from(input);
            }
        };

        PCollection<String> messages = readResult.apply("CheckType", ParDo.of(new TypeCheckChannel()));

        PCollectionTuple mappingResults = messages
                .apply(MapElements.via(toMessage))
                .apply(
                        "MapMessages",
                        ParDo.of(mappingFn).withOutputTags(MAPPING_TAG, TupleTagList.of(ERROR_ENTRY_TAG)));

        // Report mapping errors.
        mappingResults
                .get(ERROR_ENTRY_TAG)
                .apply(
                        "SerializeMappingErrors",
                        MapElements.into(TypeDescriptors.strings())
                                .via(e -> ErrorEntryConverter.toTableRow(e).toString()))
                .apply(
                        Window.<String>into(FixedWindows.of(ERROR_LOG_WINDOW_SIZE))
                                .triggering(
                                        Repeatedly.forever(
                                                AfterProcessingTime.pastFirstElementInPane()
                                                        .plusDelayOf(ERROR_LOG_WINDOW_SIZE)))
                                .withAllowedLateness(Duration.ZERO)
                                .discardingFiredPanes())
                .apply(
                        "ReportMappingErrors",
                        TextIO.write()
                                .to(options.getMappingErrorPath())
                                .withWindowedWrites()
                                .withNumShards(options.getErrorLogShardNum()));

        PCollection<MappingOutput> mappedMessages = mappingResults.get(MAPPING_TAG)
                .setCoder(MappedFhirMessageWithSourceTimeCoder.of());

        PCollection<HealthcareIOError<String>> failedBodies;
        // Commit FHIR resources.
        if (options.getEnablePerformanceMetrics()) {
            FhirIOWithMetrics.Write.Result writeResult = mappedMessages.apply(
                    "WriteFHIRBundles", FhirIOWithMetrics.Write.executeBundles(options.getFhirStore()));
            failedBodies = writeResult.getFailedBodies();
        } else {
            FhirIO.Write.Result writeResult = mappedMessages
                    .apply(MapElements.into(TypeDescriptors.strings()).via(MappingOutput::getOutput))
                    .apply("WriteFHIRBundles", FhirIO.Write.executeBundles(options.getFhirStore()));
            failedBodies = writeResult.getFailedBodies();
        }

        HealthcareIOErrorToTableRow<String> bundleErrorConverter = new HealthcareIOErrorToTableRow<>();
        failedBodies
                .apply(
                        "ConvertBundleErrors",
                        MapElements.into(TypeDescriptors.strings())
                                .via(resp -> bundleErrorConverter.apply(resp).toString()))
                .apply(
                        Window.<String>into(FixedWindows.of(ERROR_LOG_WINDOW_SIZE))
                                .triggering(
                                        Repeatedly.forever(
                                                AfterProcessingTime.pastFirstElementInPane()
                                                        .plusDelayOf(ERROR_LOG_WINDOW_SIZE)))
                                .withAllowedLateness(Duration.ZERO)
                                .discardingFiredPanes())
                .apply(
                        "RecordWriteErrors",
                        TextIO.write()
                                .to(options.getWriteErrorPath())
                                .withWindowedWrites()
                                .withNumShards(options.getErrorLogShardNum()));
        return pipeline;
    }

    public static void main(String[] args) {
        Pipeline pipeline = createPipeline(args);
        pipeline.run();
    }
}
