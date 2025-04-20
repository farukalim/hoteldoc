package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams params) {

        SearchRequest searchRequest=new SearchRequest("hotel");

        //构建查询条件
        BoolQueryBuilder boolQuery = generateBoolQueryBuilder(params);

        //构建FunctionScore,提高广告的分数
        FunctionScoreQueryBuilder functionScoreQueryBuilder = generateFunctionScoreQueryBuilder(boolQuery);

        //查询
        searchRequest.source().query(functionScoreQueryBuilder);

        //排序
        sort(params, searchRequest);

        //分页
        int size = params.getSize() == null ? 5 : params.getSize();
        int page = params.getPage() == null ? 1 : params.getPage();
        searchRequest.source().size(size).from((page-1)*size);

        //封装整理查询返回的结果
        try {
            SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
            return handleResponse(search);
        } catch (IOException e) {
            throw new RuntimeException("出现异常");
        }

    }

    @Override
    public Map<String, List<String>> filters(RequestParams params) {
        Map<String,List<String>> result=new HashMap<>();

        SearchRequest searchRequest=new SearchRequest("hotel");

        //构建查询条件
        BoolQueryBuilder boolQuery = generateBoolQueryBuilder(params);

        //构建FunctionScore,提高广告的分数
        FunctionScoreQueryBuilder functionScoreQueryBuilder = generateFunctionScoreQueryBuilder(boolQuery);
        searchRequest.source().size(0);
        searchRequest.source().query(functionScoreQueryBuilder);
        buildAggregations(searchRequest,"brandAgg","brand");
        buildAggregations(searchRequest,"starNameAgg","starName");
        buildAggregations(searchRequest,"cityAgg","city");
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<String> brandAgg = buildList(searchResponse, "brandAgg");
            List<String> cityAgg = buildList(searchResponse, "cityAgg");
            List<String> starNameAgg = buildList(searchResponse, "starNameAgg");
            result.put("brand",brandAgg);
            result.put("city",cityAgg);
            result.put("starName",starNameAgg);
        } catch (IOException e) {
            throw new RuntimeException();
        }

        return result;
    }

    private List<String> buildList(SearchResponse searchResponse, String brandName) {
        Aggregations aggregations = searchResponse.getAggregations();
        Terms terms=aggregations.get(brandName);
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        List<String> list=new ArrayList<>();
        buckets.forEach(bucket -> list.add(bucket.getKeyAsString()));
        return list;
    }

    private void buildAggregations(SearchRequest searchRequest,String brandName,String field) {
        searchRequest.source().aggregation(AggregationBuilders
                .terms(brandName)
                .field(field)
                .size(100));
    }


    private FunctionScoreQueryBuilder generateFunctionScoreQueryBuilder(BoolQueryBuilder boolQuery) {
        //获取functionScoreQuery
        FunctionScoreQueryBuilder functionScoreQueryBuilder=QueryBuilders.functionScoreQuery(
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                QueryBuilders.termQuery("isAD",true),
                                ScoreFunctionBuilders.weightFactorFunction(10)
                        )
                }
        );
        return functionScoreQueryBuilder;
    }

    private void sort(RequestParams params, SearchRequest searchRequest) {
        String sortBy = params.getSortBy();
        String location= params.getLocation();
        if(!StringUtils.isEmpty(location)) {
            searchRequest.source().sort(SortBuilders
                    .geoDistanceSort("location",new GeoPoint(location))
                    .order(SortOrder.ASC)
                    .unit(DistanceUnit.KILOMETERS));
        }
        if(sortBy.equals("price")) {
            searchRequest.source().sort(sortBy, SortOrder.ASC);
        }else if(sortBy.equals("score")) {
            searchRequest.source().sort(sortBy,SortOrder.DESC);
        }

    }

    private BoolQueryBuilder generateBoolQueryBuilder(RequestParams params) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        String key = params.getKey();
        if(StringUtils.isEmpty(key)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }else {
            boolQuery.must(QueryBuilders.matchQuery("all",key));
        }

        String brand= params.getBrand();
        if(!StringUtils.isEmpty(brand)) {
            boolQuery.filter(QueryBuilders.termQuery("brand",brand));
        }

        String city= params.getCity();
        if(!StringUtils.isEmpty(city)) {
            boolQuery.filter(QueryBuilders.termQuery("city",city));
        }

        Integer maxPrice= params.getMaxPrice();
        Integer minPrice= params.getMinPrice();
        if(maxPrice!=null && minPrice!=null && maxPrice>=minPrice) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(minPrice).lte(maxPrice));
        }

        String starName= params.getStarName();
        if(!StringUtils.isEmpty(starName)) {
            boolQuery.filter(QueryBuilders.termQuery("starName",starName));
        }
        return boolQuery;
    }

    private PageResult handleResponse(SearchResponse search) {
        PageResult result=new PageResult();
        long value = search.getHits().getTotalHits().value;
        result.setTotal(value);
        SearchHit[] hits = search.getHits().getHits();
        List<HotelDoc> hotels=new ArrayList<>();
        for (SearchHit hit : hits) {
            String jsonHotel = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(jsonHotel, HotelDoc.class);

            Object[] sortValues = hit.getSortValues();
            if(sortValues.length>0) {
                hotelDoc.setDistance(sortValues[0]);
            }
            hotels.add(hotelDoc);
        }
        result.setHotels(hotels);
        return result;
    }
}
