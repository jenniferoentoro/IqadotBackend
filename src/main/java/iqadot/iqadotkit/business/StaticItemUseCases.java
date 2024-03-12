package iqadot.iqadotkit.business;

import com.fasterxml.jackson.databind.JsonNode;
import iqadot.iqadotkit.controller.domain.CreateStaticItems;
import iqadot.iqadotkit.controller.domain.CreateStaticItemsCSV;
import iqadot.iqadotkit.controller.domain.MultipleCreateStaticItems;
import iqadot.iqadotkit.controller.domain.UploadedStaticItemResponse;
import iqadot.iqadotkit.persistence.entity.UploadedArticle;
import iqadot.iqadotkit.persistence.entity.UploadedStaticItem;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface StaticItemUseCases {
   List<UploadedStaticItem> getAllStaticItems();
    UploadedStaticItemResponse createStaticItem(CreateStaticItems uploadedStaticItemResponse, HttpServletRequest request);

    List<UploadedStaticItemResponse> createStaticItems(MultipleCreateStaticItems uploadedStaticItemResponse, HttpServletRequest request);

    List<UploadedStaticItemResponse> createStaticItemFromCSV(CreateStaticItemsCSV uploadedStaticItemResponse, HttpServletRequest request);






    List<String> getColumns(String apiUrl, String apiKey);

    List<String> getColumnsFromList(Long id);

    JsonNode getData(String apiUrl, String field, String apiKey);

    List<String> getColumnsInResults(String apiUrl, String resultsField, String apiKey);

    List<String> getColumnsInResultsFromList(Long id, String resultsField);
//
//    List<JsonNode> getColumnsWithSelectedFields(String apiUrl, String resultsField, String selectedColumns, String apiKey);

    List<Map<String, String>> preview(String apiUrl, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String apiKey, String alignment);

    List<Map<String,String>> previewFromList(Long id, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String alignment);
    List<UploadedStaticItem> importSelectedColumns(String apiUrl, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String apiKey, String channel, boolean isPublished, String alignment, HttpServletRequest httpServletRequest);

    List<UploadedStaticItem> importSelectedColumnsFromList(Long id, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String channel, boolean isPublished, String alignment, HttpServletRequest httpServletRequest);


 String deleteArticle(String staticItemGuid);

 String unpublishArticle(String staticItemGuid);

 String publishArticle(String staticItemGuid);



}
