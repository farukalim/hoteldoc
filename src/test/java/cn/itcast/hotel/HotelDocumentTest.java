package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

/**
 * @Author: Faruk
 * @CreateTime: 2025-04-15  14:22
 * @Description: TODO
 * @Version: 1.0
 */
@SpringBootTest
public class HotelDocumentTest {

    @Autowired
    private IHotelService service;

    private RestHighLevelClient client;


    @Test
    public void testAddDocument() throws IOException {
        Hotel hotel = service.getById(36934L);
        HotelDoc hotelDoc=new HotelDoc(hotel);
        IndexRequest request=new IndexRequest("hotel").id(hotelDoc.getId().toString());
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    public void testGetDocument() throws IOException {
        GetRequest getRequest=new GetRequest("hotel").id("36934");
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        String jsonResponse = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(jsonResponse, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest=new UpdateRequest("hotel","36934");
        updateRequest.doc("city","北京","score","100");
        client.update(updateRequest,RequestOptions.DEFAULT);
    }

    @Test
    public void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest=new DeleteRequest("hotel").id("36934");
        client.delete(deleteRequest,RequestOptions.DEFAULT);
    }

    @Test
    public void testBulkDocument() throws IOException {
        List<Hotel> hotels = service.list();
        BulkRequest bulkRequest=new BulkRequest();
        for (Hotel hotel : hotels) {
            HotelDoc hotelDoc=new HotelDoc(hotel);
            bulkRequest.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc),XContentType.JSON));
        }
        client.bulk(bulkRequest,RequestOptions.DEFAULT);
    }


    @BeforeEach
    public void init() {
        client=new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.101.129:9200")
        ));
    }

    @AfterEach
    public void destroy() throws IOException {
        client.close();
    }
}
