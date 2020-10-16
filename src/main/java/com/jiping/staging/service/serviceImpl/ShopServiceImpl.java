package com.jiping.staging.service.serviceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jiping.staging.common.BusinessException;
import com.jiping.staging.common.EmBusinessError;
import com.jiping.staging.config.ElasticsearchRestClient;
import com.jiping.staging.dal.ShopModelMapper;
import com.jiping.staging.model.CategoryModel;
import com.jiping.staging.model.SellerModel;
import com.jiping.staging.model.ShopModel;
import com.jiping.staging.service.CategoryService;
import com.jiping.staging.service.SellerService;
import com.jiping.staging.service.ShopService;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ShopServiceImpl implements ShopService{

    @Autowired
    private ShopModelMapper shopModelMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SellerService sellerService;
    
	/*
	 * @Autowired private RestHighLevelClient restHighLevelClient;
	 */

    @Override
    @Transactional
    public ShopModel create(ShopModel shopModel) throws BusinessException {
        shopModel.setCreatedAt(new Date());
        shopModel.setUpdatedAt(new Date());

        //校验商家是否存在正确
        SellerModel sellerModel = sellerService.get(shopModel.getSellerId());
        if(sellerModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商户不存在");
        }

        if(sellerModel.getDisabledFlag().intValue() == 1){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商户已禁用");
        }

        //校验类目
        CategoryModel categoryModel = categoryService.get(shopModel.getCategoryId());
        if(categoryModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"类目不存在");
        }
        shopModelMapper.insertSelective(shopModel);

        return get(shopModel.getId());
    }

    @Override
    public ShopModel get(Integer id) {
        ShopModel shopModel = shopModelMapper.selectByPrimaryKey(id);
        if(shopModel == null){
            return null;
        }
        shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
        shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        return shopModel;
    }

    @Override
    public List<ShopModel> selectAll() {
        List<ShopModel> shopModelList = shopModelMapper.selectAll();
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });
        return shopModelList;
    }

    @Override
    public List<ShopModel> recommend(BigDecimal longitude, BigDecimal latitude) {
        List<ShopModel> shopModelList = shopModelMapper.recommend(longitude, latitude);
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });
        return shopModelList;
    }

    @Override
    public List<Map<String, Object>> searchGroupByTags(String keyword, Integer categoryId, String tags) {
        return shopModelMapper.searchGroupByTags(keyword,categoryId,tags);
    }

    @Override
    public Integer countAllShop() {
        return shopModelMapper.countAllShop();
    }

    @Override
    public List<ShopModel> search(BigDecimal longitude,
                                  BigDecimal latitude, String keyword,Integer orderby,
                                  Integer categoryId,String tags) {
        List<ShopModel> shopModelList = shopModelMapper.search(longitude,latitude,keyword,orderby,categoryId,tags);
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });
        return shopModelList;
    }

	@Override
	public Map<String, Object> searchES(BigDecimal longitude, BigDecimal latitude, String keyword, Integer orderby,
			Integer categoryId, String tags) throws IOException  {
		Map<String, Object> result = new HashMap<String, Object>();
		/*这个方式查询很难放入更多查询元素
		 * SearchRequest request = new SearchRequest(); SearchSourceBuilder
		 * sourceBuilder = new SearchSourceBuilder();
		 * sourceBuilder.query(QueryBuilders.matchQuery("name", keyword));
		 * sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		 * request.source(sourceBuilder); List<Integer> shopIdList = new
		 * ArrayList<Integer>(); SearchResponse searchResponse =
		 * ElasticsearchRestClient.highLevelClient().search(request,
		 * RequestOptions.DEFAULT); SearchHits hits = searchResponse.getHits(); for
		 * (SearchHit searchHit : hits) { shopIdList.add(new
		 * Integer(searchHit.getSourceAsMap().get("id").toString())); }
		 */
		//更好的查询方式：以原始api调用方式执行function_score查询
		Request request = new Request("GET","/shop/_search");
		String reqJson = "{\n" + 
				"  \"query\": {\n" + 
				"    \"function_score\": {\n" + 
				"      \"query\": {\n" + 
				"        \"bool\": {\n" + 
				"          \"must\": [\n" + 
				"            {\"match\": {\"name\": {\"query\": \""+keyword+"\",\"boost\": 0.1}}},\n" + 
				"            {\"term\": {\"seller_disabled_flag\": 0}}\n" +
				
				  (categoryId==null?"":
				"			 ,{\"term\": {\"category_name\": \""+categoryService.get(
				  categoryId).getName()+"\"}}\n" )+
				  (tags==null?"":
				"			 ,{\"term\": {\"tags\": \""+tags+"\"}}\n" )+
						 
				"          ]\n" + 
				"        }\n" + 
				"      },\n" + 
				"      \n" + 
				"      \"functions\": [\n" + 
				"        {\n" + 
				"          \"gauss\": {\n" + 
				"            \"location\": {\n" + 
				"              \"origin\": \""+latitude+","+longitude+"\",\n" + 
				"              \"scale\": \"100km\",\n" + 
				"              \"offset\": \"0km\",\n" + 
				"              \"decay\": 0.5\n" + 
				"            }\n" + 
				"          },\n" + 
				"          \"weight\": 9\n" + 
				"        },\n" + 
				"        {\n" + 
				"          \"field_value_factor\": {\n" + 
				"            \"field\": \"remark_score\"\n" + 
				"          },\n" + 
				"          \"weight\": 0.2\n" + 
				"        },\n" + 
				"        {\n" + 
				"          \"field_value_factor\": {\n" + 
				"            \"field\": \"seller_remark_score\"\n" + 
				"          },\n" + 
				"          \"weight\": 0.1\n" + 
				"        }\n" + 
				"      ],\n" + 
				"      \"score_mode\": \"sum\"\n" + 
				"      , \"boost_mode\": \"sum\"\n" + 
				"    }\n" + 
				"  }\n" + 
				"  , \"_source\": \"*\"\n" + 
				"  , \"script_fields\": {\n" + 
				"    \"distance\": {\n" + 
				"      \"script\": {\n" + 
				"        \"source\": \"haversin(lat,lon,doc['location'].lat,doc['location'].lon)\",\n" + 
				"        \"lang\": \"expression\",\n" + 
				"        \"params\": {\"lat\":"+latitude+",\"lon\":"+longitude+"}\n" + 
				"      }\n" + 
				"    }\n" + 
				"  },\n" + 
				"  \"sort\": [\n" + 
				"    {\n" + 
				"      \"_score\": {\n" + 
				"        \"order\": \"desc\"\n" + 
				"      }\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  \"aggs\": {\n" + 
				"    \"group_by_tags\": {\n" + 
				"      \"terms\": {\n" + 
				"        \"field\": \"tags\"\n" + 
				"      }\n" + 
				"    }\n" + 
				"  }" +
				"}";
		request.setJsonEntity(reqJson);
		Response response = ElasticsearchRestClient.highLevelClient().getLowLevelClient().performRequest(request);
		String responseStr = EntityUtils.toString(response.getEntity());
		JSONObject jsonObject = JSONObject.parseObject(responseStr);
		JSONArray jsonArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
		List<ShopModel> shopModels = new ArrayList<ShopModel>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObj=  jsonArray.getJSONObject(i);
			Integer id = jsonObj.getIntValue("_id");
			BigDecimal distance = new BigDecimal(jsonObj.getJSONObject("fields").getJSONArray("distance").get(0).toString());
			ShopModel shopModel = get(id);
			shopModel.setDistance(distance);
			shopModels.add(shopModel);
			
		}
		List<Map> tagsList = new ArrayList<Map>();
		JSONArray tagsJsonArray = jsonObject.getJSONObject("aggregations").getJSONObject("group_by_tags").getJSONArray("buckets");
		for (int i = 0; i < tagsJsonArray.size(); i++) {
			JSONObject jsonObj = tagsJsonArray.getJSONObject(i);
			Map<String,Object> tagsMap = new HashMap<String, Object>();
			tagsMap.put("tags", jsonObj.getString("key"));
			tagsMap.put("num", jsonObj.getString("doc_count"));
			tagsList.add(tagsMap);
		}
		result.put("tags", tagsList);
		result.put("shop", shopModels);
		return result;
	}
}
