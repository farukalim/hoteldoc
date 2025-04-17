package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @Author: Faruk
 * @CreateTime: 2025-04-17  11:32
 * @Description: restclient查询文档
 * @Version: 1.0
 */
public class HotelSearchTest {
    private RestHighLevelClient client;

    @Test
    public void matchAllTest() throws IOException {
        SearchRequest searchRequest=new SearchRequest("hotel")    ;
        searchRequest.source().query(QueryBuilders.matchAllQuery()).size(20);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    @Test
    public void matchTest() throws IOException{
        SearchRequest searchRequest=new SearchRequest("hotel");
        searchRequest.source().query(QueryBuilders.matchQuery("all","外滩"));
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    @Test
    public void termTest() throws IOException{
        SearchRequest searchRequest=new SearchRequest("hotel");
        searchRequest.source().query(QueryBuilders.termQuery("city","北京"));
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    @Test
    public void rangeTest() throws IOException{
        SearchRequest searchRequest=new SearchRequest("hotel");
        searchRequest.source().query(QueryBuilders.rangeQuery("price").lte(300));
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        handleResponse(search);
    }


    @Test
    public void boolTest() throws IOException{
        SearchRequest searchRequest=new SearchRequest("hotel");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("all","如家"));
        boolQueryBuilder.mustNot(QueryBuilders.rangeQuery("price").gt(300));
        boolQueryBuilder.filter(QueryBuilders.termQuery("city","北京"));
        searchRequest.source().query(boolQueryBuilder);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    @Test
    public void sortAndPageTest() throws IOException{
        int page=1;
        int size=5;
        SearchRequest searchRequest=new SearchRequest("hotel");
        //每页显示五条，显示第一页数据，按价格升序排序，如果价格相等则按照分数降序排序
        searchRequest.source()
                .query(QueryBuilders.matchAllQuery())
                .from((page-1)*size)
                .size(size)
                .sort("price", SortOrder.ASC)
                .sort("score",SortOrder.DESC);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    @Test
    public void highlightTest() throws IOException{
        int page=1;
        int size=5;
        SearchRequest searchRequest=new SearchRequest("hotel");
        //每页显示五条，显示第一页数据，按价格升序排序，如果价格相等则按照分数降序排序
        searchRequest.source()
                .query(QueryBuilders.matchQuery("all","如家"))
                .from((page-1)*size)
                .size(size)
                .highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    private void handleResponse(SearchResponse search) {
        SearchHits searchHits = search.getHits();
        TotalHits totalHits = searchHits.getTotalHits();
        long value = totalHits.value;
        System.out.println("总共查询到"+value+"条数据");
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String jsonString = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(jsonString, HotelDoc.class);
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField nameField = highlightFields.get("name");
                if(nameField!=null) {
                    String name = nameField.fragments()[0].string();
                    hotelDoc.setName(name);
                }
            }
            System.out.println(hotelDoc);
        }
    }

    @BeforeEach
    public void init() {
        client=new RestHighLevelClient(
                RestClient.builder(
                        HttpHost.create("http://192.168.101.129:9200")
                )
        );
    }

    @AfterEach
    public void destroy() {

    }
}
