package iqadot.iqadotkit.business.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iqadot.iqadotkit.business.StaticItemUseCases;
import iqadot.iqadotkit.business.exception.CustomException;
import iqadot.iqadotkit.config.security.token.AccessToken;
import iqadot.iqadotkit.config.security.token.impl.AccessTokenEncoderDecoderImpl;
import iqadot.iqadotkit.controller.domain.*;
import iqadot.iqadotkit.persistence.UserRepository;
import iqadot.iqadotkit.persistence.entity.*;
import iqadot.iqadotkit.persistence.repository.SourcesRepository;
import iqadot.iqadotkit.persistence.repository.UploadedStaticItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StaticItemServiceImpl implements StaticItemUseCases {
    private final UploadedStaticItemRepository uploadedStaticItemRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;
    @Value("${basic.auth.password}")
    private String basicAuthPassword;

    private final ModelMapper modelMapper;

    private final SourcesRepository sourcesRepository;


    StaticItemServiceImpl(ModelMapper modelMapper, UploadedStaticItemRepository uploadedStaticItemRepository, UserRepository userRepository, SourcesRepository sourcesRepository) {
        this.sourcesRepository = sourcesRepository;
        this.modelMapper = modelMapper;
        this.uploadedStaticItemRepository = uploadedStaticItemRepository;
        this.userRepository = userRepository;
        this.webClient = WebClient.builder()
                .baseUrl("https://fs3-admin.iqadot.com/api/v3/admin")
                .build();
    }

    @Override
    public List<UploadedStaticItemResponse> createStaticItems(MultipleCreateStaticItems createStaticItemsList, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);

        List<UploadedStaticItemResponse> uploadedStaticItemResponses = new ArrayList<>();

        for (CreateStaticItems createStaticItems : createStaticItemsList.getStaticItems()) {
            String itemValue = null;
            JSONObject dataToSend = new JSONObject();
            dataToSend.put("body", createStaticItems.getBody());
            dataToSend.put("channel", createStaticItems.getChannel());
            dataToSend.put("subject", createStaticItems.getSubject());
            dataToSend.put("answer", createStaticItems.getAnswer());

            if (createStaticItems.getAuthor() != null) {
                dataToSend.put("author", createStaticItems.getAuthor());
            }

            if (createStaticItems.getTags() != null) {
                dataToSend.put("tags", createStaticItems.getTags());
            }

            if (createStaticItems.getRemarks() != null) {
                dataToSend.put("remarks", createStaticItems.getRemarks());
            }

            String response = webClient.post()
                    .uri("/static/add")
                    .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(dataToSend.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONObject jsonResponse = new JSONObject(response);

            System.out.println("Response: " + jsonResponse.toString());
            itemValue = jsonResponse.getJSONObject("data").getString("item");


            UserEntity user = userRepository.getById(accessToken.getUserId());
            UploadedStaticItem saved = uploadedStaticItemRepository.save(UploadedStaticItem.builder().isDeleted(false).publish(createStaticItems.isPublish()).staticItemGuid(itemValue).dateCreated(new Date()).subject(createStaticItems.getSubject()).adminId(user).build());

            UploadedStaticItemResponse uploadedStaticItemResponse1 = modelMapper.map(saved, UploadedStaticItemResponse.class);
            uploadedStaticItemResponse1.setStaticItemGuid(itemValue);


            if (createStaticItems.isPublish()) {
                webClient.get()
                        .uri("/static/publish/" + itemValue)
                        .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }

            uploadedStaticItemResponses.add(uploadedStaticItemResponse1);

        }

        return uploadedStaticItemResponses;
    }

    @Override
    public UploadedStaticItemResponse createStaticItem(CreateStaticItems uploadedStaticItemResponse, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);
        String itemValue = null;

        JSONObject dataToSend = new JSONObject();
        dataToSend.put("body", uploadedStaticItemResponse.getBody());
        dataToSend.put("channel", uploadedStaticItemResponse.getChannel());
        dataToSend.put("subject", uploadedStaticItemResponse.getSubject());
        dataToSend.put("answer", uploadedStaticItemResponse.getAnswer());

        if (uploadedStaticItemResponse.getAuthor() != null) {
            dataToSend.put("author", uploadedStaticItemResponse.getAuthor());
        }

        if (uploadedStaticItemResponse.getTags() != null) {
            dataToSend.put("tags", uploadedStaticItemResponse.getTags());
        }

        if (uploadedStaticItemResponse.getRemarks() != null) {
            dataToSend.put("remarks", uploadedStaticItemResponse.getRemarks());
        }

        String response = webClient.post()
                .uri("/static/add")
                .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dataToSend.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JSONObject jsonResponse = new JSONObject(response);
        itemValue = jsonResponse.getJSONObject("data").getString("item");

        UserEntity user = userRepository.getById(accessToken.getUserId());
        UploadedStaticItem saved = uploadedStaticItemRepository.save(UploadedStaticItem.builder().isDeleted(false).publish(uploadedStaticItemResponse.isPublish()).staticItemGuid(itemValue).dateCreated(new Date()).subject(uploadedStaticItemResponse.getSubject()).adminId(user).build());

        UploadedStaticItemResponse uploadedStaticItemResponse1 = modelMapper.map(saved, UploadedStaticItemResponse.class);
        uploadedStaticItemResponse1.setStaticItemGuid(itemValue);


        if (uploadedStaticItemResponse.isPublish()) {
            webClient.get()
                    .uri("/static/publish/" + itemValue)
                    .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }

        return uploadedStaticItemResponse1;


    }

    @Override
    public List<UploadedStaticItemResponse> createStaticItemFromCSV(CreateStaticItemsCSV uploadedStaticItemResponse, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);

            InputStream inputStream = uploadedStaticItemResponse.getFile().getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String originalFilename = StringUtils.cleanPath(uploadedStaticItemResponse.getFile().getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + fileExtension;

            String uploadPath = "C:/xampp/htdocs/static/csv";
            String filePath = uploadPath + "/" + filename;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            try {
                FileCopyUtils.copy(uploadedStaticItemResponse.getFile().getBytes(), new File(filePath));
            } catch (IOException e) {
//                e.printStackTrace();
            }

            List<String> articleGuids = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                String subject = values[0];
                String body = values[1];
                String answer = values[2];

                JSONObject dataToSend = new JSONObject();
                dataToSend.put("body", body);
                dataToSend.put("channel", uploadedStaticItemResponse.getChannel());
                dataToSend.put("subject", subject);
                dataToSend.put("answer", answer);

                String author = "";
                String remarks = "";
                String tags = "";

                if (values.length > 3) {
                    author = values[3];
                    dataToSend.put("author", author);
                }

                if (values.length > 4) {
                    remarks = values[4];
                    dataToSend.put("allow_comments", remarks);
                }

                if (values.length > 5) {
                    tags = values[5];
                    dataToSend.put("tags", tags);
                }

                System.out.println("Data to send: " + dataToSend.toString());
                String response = webClient.post()
                        .uri("/static/add")
                        .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dataToSend.toString())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(); // Make the call synchronous

                JSONObject jsonResponse = new JSONObject(response);
                String itemValue = jsonResponse.getJSONObject("data").getString("item");

                UserEntity user = userRepository.getById(accessToken.getUserId());
                uploadedStaticItemRepository.save(UploadedStaticItem.builder().pathFile(filename).isDeleted(false).publish(uploadedStaticItemResponse.isPublish()).staticItemGuid(itemValue).dateCreated(new Date()).subject(subject).adminId(user).build());
                System.out.println("Response item: " + itemValue);

                articleGuids.add(itemValue);
                if (uploadedStaticItemResponse.isPublish()) {
                    String publishResponse = webClient.get()
                            .uri("/static/publish/" + itemValue)
                            .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block(); // Make the call synchronous
                    System.out.println("Publish Response: " + publishResponse);
                }
            }

            List<UploadedStaticItem> uploadedArticles = uploadedStaticItemRepository.findAllByStaticItemGuidIn(articleGuids);
            List<UploadedStaticItemResponse> uploadedArticleResponses = new ArrayList<>();
            for (UploadedStaticItem uploadedArticle : uploadedArticles) {
                uploadedArticleResponses.add(UploadedStaticItemResponse.builder().staticItemGuid(uploadedArticle.getStaticItemGuid()).subject(uploadedArticle.getSubject()).dateCreated(uploadedArticle.getDateCreated()).build());
            }
            return uploadedArticleResponses;
        } catch (Exception e) {
//            e.printStackTrace();
            // Handle any exceptions and return an appropriate response or null
            return null; // Modify this to return an appropriate response
        }
    }

    @Override
    public List<UploadedStaticItem> getAllStaticItems() {
        return uploadedStaticItemRepository.findAll();
    }

    @Override
    public List<String> getColumns(String apiUrl, String apiKey) {
        RestTemplate restTemplate = new RestTemplate();


        String jsonString = restTemplate.getForObject(apiUrl, String.class);
        ObjectMapper mapper = new ObjectMapper();
        List<String> columnNames = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(jsonString);
            findColumnNames(root, columnNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return columnNames;
    }

    @Override
    public List<String> getColumnsFromList(Long id) {
        Optional<Sources> sources = sourcesRepository.findById(id);
        if (sources.isEmpty()) {
            throw new CustomException("Source not found");
        }
        RestTemplate restTemplate = new RestTemplate();
        String jsonString = "";
        if (sources.get().getMethod() == Method.GET) {
            jsonString = restTemplate.getForObject(sources.get().getUrl(), String.class);

        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", "66abeabab664e14b51c3390788575a8b");
            HttpEntity<String> requestEntity = new HttpEntity<>(sources.get().getBody(), headers);

            jsonString = restTemplate.postForObject(sources.get().getUrl(), requestEntity, String.class);
        }
        ObjectMapper mapper = new ObjectMapper();
        List<String> columnNames = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(jsonString);
            findColumnNames(root, columnNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return columnNames;
    }

    private void findColumnNames(JsonNode node, List<String> columnNames) {
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            columnNames.add(fieldName);
            JsonNode fieldValue = node.get(fieldName);

            if (fieldValue.isObject() || fieldValue.isArray()) {
                findColumnNames(fieldValue, columnNames);
            }
        }
    }

    private JsonNode findField(JsonNode node, String field) {
        JsonNode fieldValue = node.get(field);

        if (fieldValue != null) {
            return fieldValue;
        }

        Iterator<JsonNode> elements = node.elements();
        while (elements.hasNext()) {
            JsonNode element = elements.next();
            if (element.isObject() || element.isArray()) {
                JsonNode result = findField(element, field);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private String appendApiKey(String apiUrl, String apiKey) {
        if (apiKey != null && !apiKey.isEmpty()) {
            if (apiUrl.contains("?")) {
                apiUrl += "&apiKey=" + apiKey;
            } else {
                apiUrl += "?apiKey=" + apiKey;
            }
        }
        return apiUrl;
    }


    @Override
    public JsonNode getData(String apiUrl, String field, String apiKey) {
        RestTemplate restTemplate = new RestTemplate();

        String apiUrlWithApiKey = appendApiKey(apiUrl, apiKey);

        String jsonString = restTemplate.getForObject(apiUrlWithApiKey, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;

        try {
            root = mapper.readTree(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (root != null) {
            return findField(root, field);
        } else {
            return null;
        }

    }

    @Override
    public List<String> getColumnsInResults(String apiUrl, String resultsField, String apiKey) {
        RestTemplate restTemplate = new RestTemplate();

        String apiUrlWithApiKey = appendApiKey(apiUrl, apiKey);

        String jsonString = restTemplate.getForObject(apiUrlWithApiKey, String.class);

        ObjectMapper mapper = new ObjectMapper();
        List<String> columnNames = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(jsonString);
            JsonNode resultsNode = root.path(resultsField);

            if (resultsNode.isArray() && resultsNode.size() > 0 && resultsNode.get(0).isObject()) {
                findColumnNamesInResults(resultsNode.get(0), columnNames, "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return columnNames;
    }

    @Override
    public List<String> getColumnsInResultsFromList(Long id, String resultsField) {
        Optional<Sources> sources = sourcesRepository.findById(id);

        if (sources.isEmpty()) {
            throw new CustomException("Source not found");
        }

        RestTemplate restTemplate = new RestTemplate();


        String jsonString = restTemplate.getForObject(sources.get().getUrl(), String.class);

        ObjectMapper mapper = new ObjectMapper();
        List<String> columnNames = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(jsonString);
            JsonNode resultsNode = root.path(resultsField);

            if (resultsNode.isArray() && resultsNode.size() > 0 && resultsNode.get(0).isObject()) {
                findColumnNamesInResults(resultsNode.get(0), columnNames, "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return columnNames;
    }

    private void findColumnNamesInResults(JsonNode node, List<String> columnNames, String parent) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();

            if (fieldValue.isObject() || fieldValue.isArray()) {
                findColumnNamesInResults(fieldValue, columnNames, parent.isEmpty() ? fieldName : parent + "." + fieldName);
            } else {
                columnNames.add(parent.isEmpty() ? fieldName : parent + "." + fieldName);
            }
        }
    }

    private JsonNode findField(JsonNode node, String field, boolean skipFirst) {
        if (skipFirst) {
            field = field.substring(field.indexOf('.') + 1);
        }

        String[] fieldNames = field.split("\\.");
        JsonNode currentNode = node;

        for (String fieldName : fieldNames) {
            currentNode = currentNode.path(fieldName);

            if (currentNode.isMissingNode()) {
                return null;
            }
        }

        return currentNode;
    }

    @Override
    public List<Map<String, String>> preview(String apiUrl, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String apiKey, String alignment) {
        RestTemplate restTemplate = new RestTemplate();

        String apiUrlWithApiKey = appendApiKey(apiUrl, apiKey);

        String jsonString = restTemplate.getForObject(apiUrlWithApiKey, String.class);

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> resultNodes = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(jsonString);
            JsonNode resultsNode = root.path(resultsField);

            if (resultsNode.isArray() && resultsNode.size() > 0 && resultsNode.get(0).isObject()) {
                for (JsonNode node : resultsNode) {
                    Map<String, String> result = getSelectedFieldsWithFormatting(node, titleSelectedColumn, bodySelectedColumns, alignment);
                    resultNodes.add(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultNodes;
    }

    @Override
    public List<Map<String, String>> previewFromList(Long id, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String alignment) {
        Optional<Sources> sources = sourcesRepository.findById(id);

        if (sources.isEmpty()) {
            throw new CustomException("Source not found");
        }

        RestTemplate restTemplate = new RestTemplate();


        String jsonString = restTemplate.getForObject(sources.get().getUrl(), String.class);

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> resultNodes = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(jsonString);
            JsonNode resultsNode = root.path(resultsField);

            if (resultsNode.isArray() && resultsNode.size() > 0 && resultsNode.get(0).isObject()) {
                for (JsonNode node : resultsNode) {
                    Map<String, String> result = getSelectedFieldsWithFormatting(node, titleSelectedColumn, bodySelectedColumns, alignment);
                    resultNodes.add(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultNodes;
    }

    @Override
    public List<UploadedStaticItem> importSelectedColumns(String apiUrl, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String apiKey, String channel, boolean isPublished, String alignment, HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization").substring(7);
        AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);

        RestTemplate restTemplate = new RestTemplate();
        List<String> articleGuids = new ArrayList<>();

        String apiUrlWithApiKey = appendApiKey(apiUrl, apiKey);

        String jsonString = restTemplate.getForObject(apiUrlWithApiKey, String.class);

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> resultNodes = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(jsonString);
            JsonNode resultsNode = root.path(resultsField);

            if (resultsNode.isArray() && resultsNode.size() > 0 && resultsNode.get(0).isObject()) {
                for (JsonNode node : resultsNode) {
                    Map<String, String> result = getSelectedFieldsWithFormatting(node, titleSelectedColumn, bodySelectedColumns, alignment);
                    resultNodes.add(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (Map<String, String> resultNode : resultNodes) {
            String title = resultNode.get("title");
            String body = resultNode.get("body");

            JSONObject dataToSend = new JSONObject();
            dataToSend.put("body", title);
            dataToSend.put("channel", channel);
            dataToSend.put("subject", title);
            dataToSend.put("answer", body);

//            dataToSend.put("body", body);
//            dataToSend.put("channel", channel);
//            dataToSend.put("subject", title);
//            dataToSend.put("isPublished", isPublished);


            String response = webClient.post()
                    .uri("/static/add")
                    .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(dataToSend.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONObject jsonResponse = new JSONObject(response);
            String itemValue = jsonResponse.getJSONObject("data").getString("item");


            UserEntity user = userRepository.getById(accessToken.getUserId());
            uploadedStaticItemRepository.save(UploadedStaticItem.builder().isDeleted(false).publish(isPublished).subject(title).pathFile(apiUrl).dateCreated(new Date()).staticItemGuid(itemValue).adminId(user).build());


            articleGuids.add(itemValue);
            if (isPublished) {
                String publishResponse = webClient.get()
                        .uri("/static/publish/" + itemValue)
                        .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                System.out.println("Publish Response: " + publishResponse);
            }
        }

        return uploadedStaticItemRepository.findAllByStaticItemGuidIn(articleGuids);
    }

    @Override
    public List<UploadedStaticItem> importSelectedColumnsFromList(Long id, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String channel, boolean isPublished, String alignment, HttpServletRequest httpServletRequest) {
        Optional<Sources> sources = sourcesRepository.findById(id);
        if (sources.isEmpty()) {
            throw new CustomException("Source not found");
        }

        String token = httpServletRequest.getHeader("Authorization").substring(7);
        AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);

        RestTemplate restTemplate = new RestTemplate();
        List<String> articleGuids = new ArrayList<>();


        String jsonString = restTemplate.getForObject(sources.get().getUrl(), String.class);

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> resultNodes = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(jsonString);
            JsonNode resultsNode = root.path(resultsField);

            if (resultsNode.isArray() && resultsNode.size() > 0 && resultsNode.get(0).isObject()) {
                for (JsonNode node : resultsNode) {
                    Map<String, String> result = getSelectedFieldsWithFormatting(node, titleSelectedColumn, bodySelectedColumns, alignment);
                    resultNodes.add(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (Map<String, String> resultNode : resultNodes) {
            String title = resultNode.get("title");
            String body = resultNode.get("body");

            JSONObject dataToSend = new JSONObject();
//            dataToSend.put("body", body);
//            dataToSend.put("channel", channel);
//            dataToSend.put("subject", title);
//            dataToSend.put("isPublished", isPublished);
            dataToSend.put("body", title);
            dataToSend.put("channel", channel);
            dataToSend.put("subject", title);
            dataToSend.put("answer", body);


            String response = webClient.post()
                    .uri("/static/add")
                    .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(dataToSend.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Make the call synchronous

            JSONObject jsonResponse = new JSONObject(response);
            String itemValue = jsonResponse.getJSONObject("data").getString("item");


            UserEntity user = userRepository.getById(accessToken.getUserId());
            uploadedStaticItemRepository.save(UploadedStaticItem.builder().isDeleted(false).publish(isPublished).subject(title).pathFile(sources.get().getUrl()).dateCreated(new Date()).staticItemGuid(itemValue).adminId(user).build());


            articleGuids.add(itemValue);
            if (isPublished) {
                String publishResponse = webClient.get()
                        .uri("/static/publish/" + itemValue)
                        .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                System.out.println("Publish Response: " + publishResponse);
            }
        }

        return uploadedStaticItemRepository.findAllByStaticItemGuidIn(articleGuids);
    }

    @Override
    public String deleteArticle(String staticItemGuid) {
        UploadedStaticItem uploadedStaticItem = uploadedStaticItemRepository.findByStaticItemGuid(staticItemGuid);
        if (uploadedStaticItem == null) {
            throw new CustomException("Static item not found");
        }

        String response = webClient.delete()
                .uri("/static/delete/" + uploadedStaticItem.getStaticItemGuid())
                .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        uploadedStaticItem.setIsDeleted(true);
        uploadedStaticItemRepository.save(uploadedStaticItem);
        return response;

    }

    @Override
    public String unpublishArticle(String staticItemGuid) {
        UploadedStaticItem uploadedStaticItem = uploadedStaticItemRepository.findByStaticItemGuid(staticItemGuid);
        if (uploadedStaticItem == null) {
            throw new CustomException("Static item not found");
        }
        String response = webClient.get()
                .uri("/static/unpublish/" + uploadedStaticItem.getStaticItemGuid())
                .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        uploadedStaticItem.setPublish(false);
        uploadedStaticItemRepository.save(uploadedStaticItem);
        return response;

    }

    @Override
    public String publishArticle(String staticItemGuid) {
        UploadedStaticItem uploadedStaticItem = uploadedStaticItemRepository.findByStaticItemGuid(staticItemGuid);
        if (uploadedStaticItem == null) {
            throw new CustomException("Static item not found");
        }
        String response = webClient.get()
                .uri("/static/publish/" + uploadedStaticItem.getStaticItemGuid())
                .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        uploadedStaticItem.setPublish(true);
        uploadedStaticItemRepository.save(uploadedStaticItem);
        return response;
    }

    private Map<String, String> getSelectedFieldsWithFormatting(JsonNode node, String titleSelectedColumn, String bodySelectedColumns, String alignment) {
        String[] titleColumns = titleSelectedColumn.split(",");
        String[] bodyColumns = bodySelectedColumns.split(",");

        Map<String, String> result = new HashMap<>();

        for (String column : titleColumns) {
            JsonNode fieldValue = findField(node, column, false);
            if (fieldValue != null) {
                result.put("title", fieldValue.asText());
            }
        }

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("<div align=\"" + alignment + "\">");
        for (String columnWithType : bodyColumns) {
            String[] columnParts = columnWithType.split("#");
            if (columnParts.length == 2) {
                String columnName = columnParts[0].trim();
                String additionalInfoStr = columnParts[1].trim();

                String[] additionalInfo = additionalInfoStr.split(";");
                String htmlElement = additionalInfo[0].trim();
                String position = "";
                String infoLabel = "";
                JsonNode fieldValue = findField(node, columnName, false);

                if (additionalInfo.length == 3) {
                    htmlElement = additionalInfo[0].trim();
                    position = additionalInfo[1].trim();
                    infoLabel = additionalInfo[2].trim();
                }


                if (fieldValue != null) {
                    String value = fieldValue.asText();

                    // "before" content inline with the value
                    if (position.equals("before")) {
                        bodyBuilder.append("<p style=\"display: inline;\">" + infoLabel + ": </p> ");
                    }

                    // Add the HTML element with inline style
                    if ("img".equals(htmlElement) || "video".equals(htmlElement)) {
                        bodyBuilder.append("<" + htmlElement + " src=\"" + value +
                                "\" style=\"height: 300px !important; display: inline;\"" +
                                (htmlElement.equals("video") ? " controls" : "") + "/> ");
                    } else {
                        bodyBuilder.append("<" + htmlElement + " style=\"display: inline;\">").append(value).append("</" + htmlElement + "> ");
                    }

                    if (position.equals("after")) {
                        bodyBuilder.append("<p style=\"display: inline;\"> : " + infoLabel + "</p> ");
                    }
                }
            }
            bodyBuilder.append("<br/>");
        }

        bodyBuilder.append("</div>");


        String htmlWithoutBackslashes = bodyBuilder.toString().replace("\\", "");
        result.put("body", htmlWithoutBackslashes);

        System.out.println(result);
        return result;
    }
}
