package iqadot.iqadotkit.business;

import com.fasterxml.jackson.databind.JsonNode;
import iqadot.iqadotkit.controller.domain.CreateArticleCSVExtra;
import iqadot.iqadotkit.controller.domain.CreateArticlePDF;
import iqadot.iqadotkit.controller.domain.UploadedArticleReponse;
import iqadot.iqadotkit.persistence.entity.UploadedArticle;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ArticleUseCases {
    UploadedArticle createArticleFromPDF(CreateArticlePDF createArticlePDF, HttpServletRequest httpServletRequest) throws IOException, ParserConfigurationException;

    List<UploadedArticle> createArticleFromCSV(CreateArticlePDF createArticlePDF, HttpServletRequest httpServletRequest) throws IOException;

    List<UploadedArticle> createArticleFromCSVChoose(CreateArticleCSVExtra createArticlePDF, HttpServletRequest httpServletRequest) throws IOException;

    List<Map<String, String>>  previewArticleFromCSVChoose(CreateArticleCSVExtra createArticlePDF, HttpServletRequest httpServletRequest) throws IOException;

    List<String> previewColumnsFromCSV(CreateArticlePDF createArticlePDF) throws IOException;

    List<UploadedArticle> getAllArticles();


    List<String> getColumns(String apiUrl, String apiKey);

    List<String> getColumnsFromList(Long id);

    JsonNode getData(String apiUrl, String field, String apiKey);

    List<String> getColumnsInResults(String apiUrl, String resultsField, String apiKey);

    List<String> getColumnsInResultsFromList(Long id, String resultsField);
//
//    List<JsonNode> getColumnsWithSelectedFields(String apiUrl, String resultsField, String selectedColumns, String apiKey);

    List<Map<String, String>> preview(String apiUrl, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String apiKey, String alignment);

    List<Map<String,String>> previewFromList(Long id, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String alignment);
    List<UploadedArticle> importSelectedColumns(String apiUrl, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String apiKey, String channel, boolean isPublished, String alignment, HttpServletRequest httpServletRequest);

    List<UploadedArticle> importSelectedColumnsFromList(Long id, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String channel, boolean isPublished, String alignment, HttpServletRequest httpServletRequest);

    String deleteArticle(String articleGuid);

    String unpublishArticle(String articleGuid);

    String publishArticle(String articleGuid);

}
