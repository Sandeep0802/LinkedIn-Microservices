package com.sandeep.searchservice.repository;

import com.sandeep.searchservice.entity.PostDocument;
import com.sandeep.searchservice.entity.UserDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument,String> {

    @Query("{\"match\" : {\"content\" : {\"query\" : \"?0\", " +
            "\"fuzziness\" : \"AUTO\" }}}")
    List<PostDocument> searchPosts(String query);
}
