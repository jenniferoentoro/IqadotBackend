package iqadot.iqadotkit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import iqadot.iqadotkit.business.ArticleUseCases;
import iqadot.iqadotkit.controller.domain.CreateArticleCSVExtra;
import iqadot.iqadotkit.controller.domain.CreateArticlePDF;
import iqadot.iqadotkit.controller.domain.UploadedArticleReponse;
import iqadot.iqadotkit.controller.domain.UserCreateResponseDTO;
import iqadot.iqadotkit.persistence.entity.UploadedArticle;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/", allowedHeaders = "*")
public class ArticleController {
    private final ArticleUseCases articleUseCases;

    private final ModelMapper modelMapper;

    @PostMapping("/create-pdf")
    public ResponseEntity<UploadedArticleReponse> createPdf(@Valid @ModelAttribute CreateArticlePDF pdf, HttpServletRequest httpServletRequest) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(articleUseCases.createArticleFromPDF(pdf, httpServletRequest), UploadedArticleReponse.class));
    }

    @PostMapping("/create-csv")
    public ResponseEntity<List<UploadedArticleReponse>> uploadFile(@Valid @ModelAttribute CreateArticlePDF csv, HttpServletRequest httpServletRequest) throws Exception {

        List<UploadedArticle> uploadedArticles = articleUseCases.createArticleFromCSV(csv, httpServletRequest);
        List<UploadedArticleReponse> uploadedArticleResponses = new ArrayList<>();
        for (UploadedArticle uploadedArticle : uploadedArticles) {
            uploadedArticleResponses.add(UploadedArticleReponse.builder()
                    .articleGuid(uploadedArticle.getArticleGuid())
                    .adminId(UserCreateResponseDTO.builder().username(uploadedArticle.getAdminId().getUsername()).firstName(uploadedArticle.getAdminId().getFirstName()).lastName(uploadedArticle.getAdminId().getLastName()).email(uploadedArticle.getAdminId().getEmail()).build())
                    .pathFile(uploadedArticle.getPathFile())
                    .title(uploadedArticle.getTitle())
                    .dateCreated(uploadedArticle.getDateCreated())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedArticleResponses);
    }

    @PostMapping("/preview-csv")
    public ResponseEntity<List<String>> previewColumns(@Valid @ModelAttribute CreateArticlePDF csv) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED).body(articleUseCases.previewColumnsFromCSV(csv));
    }

    @PostMapping("/preview-csv-choose")
    public ResponseEntity<List<Map<String, String>>> previewArticleFromCSVChoose(@Valid @ModelAttribute CreateArticleCSVExtra csv, HttpServletRequest httpServletRequest) throws Exception {

        return ResponseEntity.status(HttpStatus.CREATED).body(articleUseCases.previewArticleFromCSVChoose(csv, httpServletRequest));
    }

    @PostMapping("/create-csv-choose")
    public ResponseEntity<List<UploadedArticleReponse>> uploadFileChoose(@Valid @ModelAttribute CreateArticleCSVExtra csv, HttpServletRequest httpServletRequest) throws Exception {

        List<UploadedArticle> uploadedArticles = articleUseCases.createArticleFromCSVChoose(csv, httpServletRequest);
        List<UploadedArticleReponse> uploadedArticleResponses = new ArrayList<>();
        for (UploadedArticle uploadedArticle : uploadedArticles) {
            uploadedArticleResponses.add(UploadedArticleReponse.builder()
                    .articleGuid(uploadedArticle.getArticleGuid())
                    .adminId(UserCreateResponseDTO.builder().username(uploadedArticle.getAdminId().getUsername()).firstName(uploadedArticle.getAdminId().getFirstName()).lastName(uploadedArticle.getAdminId().getLastName()).email(uploadedArticle.getAdminId().getEmail()).build())
                    .pathFile(uploadedArticle.getPathFile())
                    .title(uploadedArticle.getTitle())
                    .dateCreated(uploadedArticle.getDateCreated())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedArticleResponses);
    }

    @GetMapping
    public ResponseEntity<List<UploadedArticleReponse>> getAllArticles() {
        List<UploadedArticle> uploadedArticles = articleUseCases.getAllArticles();
        List<UploadedArticleReponse> uploadedArticleResponses = new ArrayList<>();
        for (UploadedArticle uploadedArticle : uploadedArticles) {
            uploadedArticleResponses.add(UploadedArticleReponse.builder()
                    .articleGuid(uploadedArticle.getArticleGuid())
                    .adminId(UserCreateResponseDTO.builder().username(uploadedArticle.getAdminId().getUsername()).firstName(uploadedArticle.getAdminId().getFirstName()).lastName(uploadedArticle.getAdminId().getLastName()).email(uploadedArticle.getAdminId().getEmail()).build())
                    .pathFile(uploadedArticle.getPathFile())
                    .title(uploadedArticle.getTitle())
                    .dateCreated(uploadedArticle.getDateCreated())
                    .isDeleted(uploadedArticle.getIsDeleted())
                    .publish(uploadedArticle.getPublish())
                    .build());
        }
        return ResponseEntity.ok(uploadedArticleResponses);
    }


    @GetMapping("/getColumns")
    public ResponseEntity<List<String>> getColumns(@RequestParam String apiUrl, @RequestParam(required = false) String apiKey) {
        return ResponseEntity.ok(articleUseCases.getColumns(apiUrl, apiKey));
    }

    @GetMapping("/getColumnsFromList/{id}")
    public ResponseEntity<List<String>> getColumnsFromList(@PathVariable Long id) {
//        System.out.println("aawewae"+id);
        return ResponseEntity.ok(articleUseCases.getColumnsFromList(id));
    }

    @GetMapping("/getData")
    public ResponseEntity<JsonNode> getData(@RequestParam String apiUrl, @RequestParam String field, @RequestParam(required = false) String apiKey) {
        return ResponseEntity.ok(articleUseCases.getData(apiUrl, field, apiKey));
    }

    @GetMapping("/getColumnsInResults")
    public ResponseEntity<List<String>> getColumnsInResults(@RequestParam String apiUrl, @RequestParam String resultsField, @RequestParam(required = false) String apiKey) {
        return ResponseEntity.ok(articleUseCases.getColumnsInResults(apiUrl, resultsField, apiKey));
    }

    @GetMapping("/getColumnsInResultsFromList/{id}")
    public ResponseEntity<List<String>> getColumnsInResultsFromList(@PathVariable Long id, @RequestParam String resultsField) {
        return ResponseEntity.ok(articleUseCases.getColumnsInResultsFromList(id, resultsField));
    }

//    @GetMapping("/getColumnsWithSelectedFields")
//    public ResponseEntity<List<JsonNode>> getColumnsWithSelectedFields(@RequestParam String apiUrl, @RequestParam String resultsField, @RequestParam String selectedColumns, @RequestParam(required = false) String apiKey) {
//        return ResponseEntity.ok(articleUseCases.getColumnsWithSelectedFields(apiUrl, resultsField, selectedColumns, apiKey));
//    }

    @GetMapping("/previewSelectedColumns")
    public ResponseEntity<List<Map<String, String>>> preview(@RequestParam String apiUrl, @RequestParam String resultsField, @RequestParam String titleSelectedColumn, @RequestParam String bodySelectedColumns, @RequestParam(required = false) String apiKey, @RequestParam String alignment) {
        return ResponseEntity.ok(articleUseCases.preview(apiUrl, resultsField, titleSelectedColumn, bodySelectedColumns, apiKey, alignment));
    }

    @GetMapping("/previewSelectedColumnsFromList/{id}")
    public ResponseEntity<List<Map<String, String>>> previewFromList(@PathVariable Long id, @RequestParam String resultsField, @RequestParam String titleSelectedColumn, @RequestParam String bodySelectedColumns, @RequestParam String alignment) {
        return ResponseEntity.ok(articleUseCases.previewFromList(id, resultsField, titleSelectedColumn, bodySelectedColumns, alignment));
    }

    @GetMapping("/importSelectedColumns")
    public ResponseEntity<?> importSelectedColumns(@RequestParam String apiUrl, @RequestParam String resultsField, @RequestParam String titleSelectedColumn, @RequestParam String bodySelectedColumns, @RequestParam(required = false) String apiKey, @RequestParam String channel, @RequestParam boolean isPublished, @RequestParam String alignment, HttpServletRequest httpServletRequest) {
        List<UploadedArticle> uploadedArticles = articleUseCases.importSelectedColumns(apiUrl, resultsField, titleSelectedColumn, bodySelectedColumns, apiKey, channel, isPublished, alignment, httpServletRequest);

        List<UploadedArticleReponse> uploadedArticleResponses = new ArrayList<>();
        for (UploadedArticle uploadedArticle : uploadedArticles) {
            uploadedArticleResponses.add(UploadedArticleReponse.builder()
                    .articleGuid(uploadedArticle.getArticleGuid())
                    .adminId(UserCreateResponseDTO.builder().username(uploadedArticle.getAdminId().getUsername()).firstName(uploadedArticle.getAdminId().getFirstName()).lastName(uploadedArticle.getAdminId().getLastName()).email(uploadedArticle.getAdminId().getEmail()).build())
                    .pathFile(uploadedArticle.getPathFile())
                    .title(uploadedArticle.getTitle())
                    .dateCreated(uploadedArticle.getDateCreated())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedArticleResponses);
    }

    @GetMapping("/importSelectedColumnsFromList/{id}")
    public ResponseEntity<?> importSelectedColumnsFromList(@PathVariable Long id, @RequestParam String resultsField, @RequestParam String titleSelectedColumn, @RequestParam String bodySelectedColumns, @RequestParam String channel, @RequestParam boolean isPublished, @RequestParam String alignment, HttpServletRequest httpServletRequest) {
        List<UploadedArticle> uploadedArticles = articleUseCases.importSelectedColumnsFromList(id, resultsField, titleSelectedColumn, bodySelectedColumns, channel, isPublished, alignment, httpServletRequest);

        List<UploadedArticleReponse> uploadedArticleResponses = new ArrayList<>();
        for (UploadedArticle uploadedArticle : uploadedArticles) {
            uploadedArticleResponses.add(UploadedArticleReponse.builder()
                    .articleGuid(uploadedArticle.getArticleGuid())
                    .adminId(UserCreateResponseDTO.builder().username(uploadedArticle.getAdminId().getUsername()).firstName(uploadedArticle.getAdminId().getFirstName()).lastName(uploadedArticle.getAdminId().getLastName()).email(uploadedArticle.getAdminId().getEmail()).build())
                    .pathFile(uploadedArticle.getPathFile())
                    .title(uploadedArticle.getTitle())
                    .dateCreated(uploadedArticle.getDateCreated())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedArticleResponses);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteArticle(@PathVariable String id) {
        return ResponseEntity.ok(articleUseCases.deleteArticle(id));
    }

    @PutMapping("/unpublish/{id}")
    public ResponseEntity<String> unpublishArticle(@PathVariable String id) {
        return ResponseEntity.ok(articleUseCases.unpublishArticle(id));
    }

    @PutMapping("/publish/{id}")
    public ResponseEntity<String> publishArticle(@PathVariable String id) {
        return ResponseEntity.ok(articleUseCases.publishArticle(id));
    }


}
