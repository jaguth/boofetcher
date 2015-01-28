package com.jaguth.fetcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jaguth.util.LogUtil;
import com.jaguth.util.StringUtil;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.discovery.MasterNotDiscoveredException;

import java.util.List;
import java.util.Objects;


public class ElasticSearchDataStore extends DataStore
{
    public String uri;
    public String indexName;
    private Client client;
    private boolean dataStoreReady;

    private static String typeName = "tweet"; // statically setting for now to get proof-of-concept out

    public ElasticSearchDataStore()
    {
        dataStoreReady = false;
    }

    public void InitDataStore()
    {
        if (dataStoreReady)
            return;

        if (StringUtil.isNullOrWhitespace(uri))
            throw new DataStoreException("ElasticSearchDataStore Error: uri is null or empty");

        if (StringUtil.isNullOrWhitespace(indexName))
            throw new DataStoreException("ElasticSearchDataStore Error: indexName is null or empty");

        if (!StartClient())
            return;

        if (!GreenToGo())
            return;

        if (!CreateIndex())
            return;

        dataStoreReady = true;
    }

    public boolean StartClient()
    {
        try
        {
            client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
            return true;
        }
        catch (Exception e)
        {
            LogUtil.Log(String.format("Elastic Search client failed to start client: %1%s", e.toString()));
            return false;
        }
    }

    // http://www.lucubratory.eu/the-elasticsearch-java-api/
    // Make sure the cluser is in a "green" state before working on is
    public boolean GreenToGo()
    {
        ClusterHealthResponse hr;

        try
        {
            hr = client.admin().cluster().
                    prepareHealth().
                    setWaitForGreenStatus().
                    setTimeout(TimeValue.timeValueMillis(250)).
                    execute().
                    actionGet();

            if (hr != null)
            {
                System.out.println("Data nodes found:" + hr.getNumberOfDataNodes());
                System.out.println("Timeout? :" + hr.isTimedOut());
                System.out.println("Status:" + hr.getStatus().name());
            }

            return true;
        }
        catch (MasterNotDiscoveredException e)
        {
            LogUtil.Log(String.format("Elastic Search failed in GreenToGo: %1$s", e.toString()));
            return false;
        }
    }

    public boolean DeleteIndex()
    {
        try
        {
            final DeleteIndexRequestBuilder delIdx = client.admin().indices().prepareDelete(indexName);
            delIdx.execute().actionGet();

            return true;
        }
        catch (Exception e)
        {
            LogUtil.Log(String.format("Elastic Search, Failed to delete index: %1$s", e.toString()));
            return false;
        }
    }

    public boolean CreateIndex()
    {

        final IndicesExistsResponse res = client.admin().indices().prepareExists(indexName).execute().actionGet();

        if (res.isExists())
            return true; // index already exists

        try
        {
            XContentBuilder mappingBuilder = XContentFactory.jsonBuilder()
                    .startObject().startObject(typeName).startObject("properties")
                        .startObject("Forwards")
                        .field("type", "long")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("Language")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("Likes")
                        .field("type", "long")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("GeoLocation")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("CountryCode")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("Message")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("MessageDate")
                        .field("type", "date")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("TimeZone")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("UserName")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("DataSource")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .endObject()
                        .startObject("id")
                        .field("type", "long")
                        .field("store", "yes")
                    .field("index", "analyzed")
                    .endObject()
                    .endObject().endObject().endObject();

            final CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(indexName);
            createIndexRequestBuilder.addMapping(typeName, mappingBuilder);
            createIndexRequestBuilder.execute().actionGet();

            return true;
        }
        catch (Exception e)
        {
            LogUtil.Log(String.format("Elastic Search, Failed to create index: %1$s", e.toString()));
            return false;
        }
    }

    public void Dispose()
    {
        client.close();
    }

    public void InsertBulkData(List<Message> messages)
    {
        InitDataStore();

        if (messages == null || messages.size() == 0)
            throw new DataStoreException("ElasticSearchDataStore Error: messages is null or empty");

        if (!dataStoreReady)
            throw new DataStoreException("Unable to initialize Elastic Search client");

        try
        {
            BulkRequestBuilder bulkRequest = client.prepareBulk();

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

            Gson gson = gsonBuilder.create();

            for (Message message : messages)
                bulkRequest.add(client.prepareIndex(indexName, typeName, Objects.toString(message.id, null)).setSource(gson.toJson(message)));

            BulkResponse bulkResponse = bulkRequest.execute().actionGet();

            if (bulkResponse.hasFailures())
                throw new DataStoreException("ElasticSearchDataStore Error:  " + bulkResponse.buildFailureMessage());
            else
                LogUtil.Log(String.format("Inserted %1$d tweets", messages.size()));
        }
        catch (Exception e)
        {
            LogUtil.Log(String.format("Failure to insert data into Elastic Search data store: %1$s.", e.toString()));
            dataStoreReady = false;
        }
    }

}

