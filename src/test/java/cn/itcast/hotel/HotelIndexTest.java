package cn.itcast.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static cn.itcast.hotel.constant.MyConstants.MAPPING_TEMPLATE;

/**
 * @Author: Faruk
 * @CreateTime: 2025-04-15  13:53
 * @Description: TODO
 * @Version: 1.0
 */

public class HotelIndexTest {

    private RestHighLevelClient client;

    @Test
    public void testCreateHotelIndex() throws IOException {
        CreateIndexRequest createIndexRequest=new CreateIndexRequest("hotel");
        createIndexRequest.source(MAPPING_TEMPLATE, XContentType.JSON);
        client.indices().create(createIndexRequest,RequestOptions.DEFAULT);
    }

    @Test
    public void testDeleteHotelIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest=new DeleteIndexRequest("hotel");
        client.indices().delete(deleteIndexRequest,RequestOptions.DEFAULT);
    }

    @Test
    public void testExistsHotelIndex() throws IOException {
        GetIndexRequest getIndexRequest=new GetIndexRequest("hotel");
        System.out.println(client.indices().exists(getIndexRequest, RequestOptions.DEFAULT));
    }




    @BeforeEach
    private void init() {
        client=new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.101.129:9200")
        ));
    }

    @AfterEach
    private void close() throws IOException {
        client.close();
    }
}
