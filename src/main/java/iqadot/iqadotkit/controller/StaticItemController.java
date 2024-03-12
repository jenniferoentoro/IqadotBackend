package iqadot.iqadotkit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import iqadot.iqadotkit.business.StaticItemUseCases;
import iqadot.iqadotkit.controller.domain.*;
import iqadot.iqadotkit.persistence.entity.UploadedArticle;
import iqadot.iqadotkit.persistence.entity.UploadedStaticItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/static-item")
@CrossOrigin(origins = "http://localhost:5173/", allowedHeaders = "*")
@RequiredArgsConstructor
public class StaticItemController {
    private final StaticItemUseCases staticItemUseCases;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<?> createStaticItem(@Valid @RequestBody CreateStaticItems createStaticItems, HttpServletRequest httpServletRequest) {


        return ResponseEntity.ok(staticItemUseCases.createStaticItem(createStaticItems, httpServletRequest));

    }

    @PostMapping("/create-csv")
    public ResponseEntity<?> createStaticItemFromCSV(@Valid @ModelAttribute CreateStaticItemsCSV createStaticItems, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(staticItemUseCases.createStaticItemFromCSV(createStaticItems, httpServletRequest));

    }

    @PostMapping("/multiple")
    public ResponseEntity<?> createStaticItems(@Valid @RequestBody MultipleCreateStaticItems createStaticItems, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(staticItemUseCases.createStaticItems(createStaticItems, httpServletRequest));

    }

    @GetMapping
    public ResponseEntity<List<UploadedStaticItemResponse>> getAllStaticItems() {
        List<UploadedStaticItem> uploadedStaticItems = staticItemUseCases.getAllStaticItems();
        return ResponseEntity.ok(uploadedStaticItems.stream().map(uploadedStaticItem -> modelMapper.map(uploadedStaticItem, UploadedStaticItemResponse.class)).collect(Collectors.toList()));
    }


    @GetMapping("/getColumns")
    public ResponseEntity<List<String>> getColumns(@RequestParam String apiUrl, @RequestParam(required = false) String apiKey) {
        return ResponseEntity.ok(staticItemUseCases.getColumns(apiUrl, apiKey));
    }

    @GetMapping("/getColumnsFromList/{id}")
    public ResponseEntity<List<String>> getColumnsFromList(@PathVariable Long id) {
//        System.out.println("aawewae"+id);
        return ResponseEntity.ok(staticItemUseCases.getColumnsFromList(id));
    }

    @GetMapping("/getData")
    public ResponseEntity<JsonNode> getData(@RequestParam String apiUrl, @RequestParam String field, @RequestParam(required = false) String apiKey) {
        return ResponseEntity.ok(staticItemUseCases.getData(apiUrl, field, apiKey));
    }

    @GetMapping("/getColumnsInResults")
    public ResponseEntity<List<String>> getColumnsInResults(@RequestParam String apiUrl, @RequestParam String resultsField, @RequestParam(required = false) String apiKey) {
        return ResponseEntity.ok(staticItemUseCases.getColumnsInResults(apiUrl, resultsField, apiKey));
    }

    @GetMapping("/getColumnsInResultsFromList/{id}")
    public ResponseEntity<List<String>> getColumnsInResultsFromList(@PathVariable Long id, @RequestParam String resultsField) {
        return ResponseEntity.ok(staticItemUseCases.getColumnsInResultsFromList(id, resultsField));
    }

//    @GetMapping("/getColumnsWithSelectedFields")
//    public ResponseEntity<List<JsonNode>> getColumnsWithSelectedFields(@RequestParam String apiUrl, @RequestParam String resultsField, @RequestParam String selectedColumns, @RequestParam(required = false) String apiKey) {
//        return ResponseEntity.ok(articleUseCases.getColumnsWithSelectedFields(apiUrl, resultsField, selectedColumns, apiKey));
//    }

    @GetMapping("/previewSelectedColumns")
    public ResponseEntity<List<Map<String, String>>> preview(@RequestParam String apiUrl, @RequestParam String resultsField, @RequestParam String titleSelectedColumn, @RequestParam String bodySelectedColumns, @RequestParam(required = false) String apiKey, @RequestParam String alignment) {
        return ResponseEntity.ok(staticItemUseCases.preview(apiUrl, resultsField, titleSelectedColumn, bodySelectedColumns, apiKey, alignment));
    }

    @GetMapping("/previewSelectedColumnsFromList/{id}")
    public ResponseEntity<List<Map<String, String>>> previewFromList(@PathVariable Long id, @RequestParam String resultsField, @RequestParam String titleSelectedColumn, @RequestParam String bodySelectedColumns, @RequestParam String alignment) {
        return ResponseEntity.ok(staticItemUseCases.previewFromList(id, resultsField, titleSelectedColumn, bodySelectedColumns, alignment));
    }

    @GetMapping("/importSelectedColumns")
    public ResponseEntity<?> importSelectedColumns(@RequestParam String apiUrl, @RequestParam String resultsField, @RequestParam String titleSelectedColumn, @RequestParam String bodySelectedColumns, @RequestParam(required = false) String apiKey, @RequestParam String channel, @RequestParam boolean isPublished, @RequestParam String alignment, HttpServletRequest httpServletRequest) {
        List<UploadedStaticItem> uploadedArticles = staticItemUseCases.importSelectedColumns(apiUrl, resultsField, titleSelectedColumn, bodySelectedColumns, apiKey, channel, isPublished, alignment, httpServletRequest);

        List<UploadedArticleReponse> uploadedArticleResponses = new ArrayList<>();
        for (UploadedStaticItem uploadedArticle : uploadedArticles) {
            uploadedArticleResponses.add(UploadedArticleReponse.builder()
                    .articleGuid(uploadedArticle.getStaticItemGuid())
                    .adminId(UserCreateResponseDTO.builder().username(uploadedArticle.getAdminId().getUsername()).firstName(uploadedArticle.getAdminId().getFirstName()).lastName(uploadedArticle.getAdminId().getLastName()).email(uploadedArticle.getAdminId().getEmail()).build())
                    .pathFile(uploadedArticle.getPathFile())
                    .title(uploadedArticle.getSubject())
                    .dateCreated(uploadedArticle.getDateCreated())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedArticleResponses);
    }

    @GetMapping("/importSelectedColumnsFromList/{id}")
    public ResponseEntity<?> importSelectedColumnsFromList(@PathVariable Long id, @RequestParam String resultsField, @RequestParam String titleSelectedColumn, @RequestParam String bodySelectedColumns, @RequestParam String channel, @RequestParam boolean isPublished, @RequestParam String alignment, HttpServletRequest httpServletRequest) {
        List<UploadedStaticItem> uploadedArticles = staticItemUseCases.importSelectedColumnsFromList(id, resultsField, titleSelectedColumn, bodySelectedColumns, channel, isPublished, alignment, httpServletRequest);

        List<UploadedArticleReponse> uploadedArticleResponses = new ArrayList<>();
        for (UploadedStaticItem uploadedArticle : uploadedArticles) {
            uploadedArticleResponses.add(UploadedArticleReponse.builder()
                    .articleGuid(uploadedArticle.getStaticItemGuid())
                    .adminId(UserCreateResponseDTO.builder().username(uploadedArticle.getAdminId().getUsername()).firstName(uploadedArticle.getAdminId().getFirstName()).lastName(uploadedArticle.getAdminId().getLastName()).email(uploadedArticle.getAdminId().getEmail()).build())
                    .pathFile(uploadedArticle.getPathFile())
                    .title(uploadedArticle.getSubject())
                    .dateCreated(uploadedArticle.getDateCreated())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedArticleResponses);
    }

    @DeleteMapping("/delete/{staticItemGuid}")
    public ResponseEntity<String> deleteArticle(@PathVariable String staticItemGuid) {
        return ResponseEntity.ok(staticItemUseCases.deleteArticle(staticItemGuid));
    }

    @PutMapping("/unpublish/{staticItemGuid}")
    public ResponseEntity<String> unpublishArticle(@PathVariable String staticItemGuid) {
        return ResponseEntity.ok(staticItemUseCases.unpublishArticle(staticItemGuid));
    }

    @PutMapping("/publish/{staticItemGuid}")
    public ResponseEntity<String> publishArticle(@PathVariable String staticItemGuid) {
        return ResponseEntity.ok(staticItemUseCases.publishArticle(staticItemGuid));
    }

}
