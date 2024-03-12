package iqadot.iqadotkit.business.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import iqadot.iqadotkit.business.ArticleUseCases;
import iqadot.iqadotkit.business.SourcesUseCases;
import iqadot.iqadotkit.business.exception.CustomException;
import iqadot.iqadotkit.config.security.token.AccessToken;
import iqadot.iqadotkit.config.security.token.impl.AccessTokenEncoderDecoderImpl;
import iqadot.iqadotkit.configuration.PDFGenerator.HtmlGenerator;
import iqadot.iqadotkit.controller.domain.CreateArticleCSVExtra;
import iqadot.iqadotkit.controller.domain.CreateArticlePDF;
import iqadot.iqadotkit.persistence.UserRepository;
import iqadot.iqadotkit.persistence.entity.Method;
import iqadot.iqadotkit.persistence.entity.Sources;
import iqadot.iqadotkit.persistence.entity.UploadedArticle;
import iqadot.iqadotkit.persistence.entity.UserEntity;
import iqadot.iqadotkit.persistence.repository.SourcesRepository;
import iqadot.iqadotkit.persistence.repository.UploadedArticleRepository;
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


import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

@Transactional
@Service
public class ArticleServiceImpl implements ArticleUseCases {

    private final SourcesRepository sourcesRepository;

    private final UploadedArticleRepository uploadedArticleRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;
    @Value("${basic.auth.password}")
    private String basicAuthPassword;

    private static final String PATH = "C:/xampp/htdocs/pdf";

    ArticleServiceImpl(UploadedArticleRepository uploadedArticleRepository, UserRepository userRepository, SourcesRepository sourcesRepository) {
        this.uploadedArticleRepository = uploadedArticleRepository;
        this.userRepository = userRepository;
        this.sourcesRepository = sourcesRepository;
        this.webClient = WebClient.builder()
                .baseUrl("https://fs3-admin.iqadot.com/api/v3/admin")
                .build();
    }


    @Override
    public UploadedArticle createArticleFromPDF(CreateArticlePDF createArticlePDF, HttpServletRequest httpServletRequest) throws IOException, ParserConfigurationException {
        String token = httpServletRequest.getHeader("Authorization").substring(7);
        AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);

        String originalFilename = StringUtils.cleanPath(createArticlePDF.getFile().getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + fileExtension;

        String filePath = PATH + "/" + filename;
        File uploadDir = new File(PATH);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        try {
            FileCopyUtils.copy(createArticlePDF.getFile().getBytes(), new File(filePath));
        } catch (IOException e) {
            throw new CustomException("Error when copying file");
        }

        String htmlResult = HtmlGenerator.generateHtmlFromPdf2(createArticlePDF.getFile().getInputStream());

        String textResult = HtmlGenerator.convertPdfToText(createArticlePDF.getFile().getInputStream());
        String[] lines = textResult.split("\n");

        String itemValue = "";
        String title;
        if (lines.length > 0) {
            title = lines[0];
        } else {
            title = "";
        }
        if (htmlResult.contains("<img")) {

        } else {
            String[] restOfLines = Arrays.copyOfRange(lines, 1, lines.length);
            htmlResult = String.join("\n", restOfLines);

        }

        JSONObject dataToSend = new JSONObject();
        dataToSend.put("body", htmlResult);
        dataToSend.put("channel", createArticlePDF.getChannel());
        dataToSend.put("subject", title);

        if (createArticlePDF.getAuthor() != null) {
            dataToSend.put("author", createArticlePDF.getAuthor());
        }

        if (createArticlePDF.getTags() != null) {
            dataToSend.put("tags", createArticlePDF.getTags());
        }

        if (createArticlePDF.isAllow_comments()) {
            dataToSend.put("allow_comments", true);
        }

        String response = webClient.post()
                .uri("/article/add")
                .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dataToSend.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JSONObject jsonResponse = new JSONObject(response);
        itemValue = jsonResponse.getJSONObject("data").getString("item");

        UserEntity user = userRepository.getById(accessToken.getUserId());

        uploadedArticleRepository.save(UploadedArticle.builder().isDeleted(false).publish(createArticlePDF.isPublish()).pathFile(filename).title(title).dateCreated(new Date()).articleGuid(itemValue).adminId(user).build());
        System.out.println("Response item: " + itemValue);
        if (createArticlePDF.isPublish()) {
            String publishResponse = webClient.get()
                    .uri("/article/publish/" + itemValue)
                    .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Make the call synchronous
            System.out.println("Publish Response: " + publishResponse);
        }

        return uploadedArticleRepository.findByArticleGuid(itemValue);
    }


    @Override
    public List<UploadedArticle> createArticleFromCSV(CreateArticlePDF createArticlePDF, HttpServletRequest httpServletRequest) throws IOException {
        try {
            String token = httpServletRequest.getHeader("Authorization").substring(7);
            AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);

            InputStream inputStream = createArticlePDF.getFile().getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String originalFilename = StringUtils.cleanPath(createArticlePDF.getFile().getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + fileExtension;

            String uploadPath = "C:/xampp/htdocs/csv";
            String filePath = uploadPath + "/" + filename;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            try {
                FileCopyUtils.copy(createArticlePDF.getFile().getBytes(), new File(filePath));
            } catch (IOException e) {
                throw new Exception("Error when copying file");
            }

            List<String> articleGuids = new ArrayList<>();
            String line;
            boolean skipHeader = true; // Flag to skip the first line
            while ((line = br.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue; // Skip processing the first line (header)
                }

                String[] values = line.split(";");
                String title = values[0];
                String body = values[1];

                JSONObject dataToSend = new JSONObject();
                dataToSend.put("body", body);
                dataToSend.put("channel", createArticlePDF.getChannel());
                dataToSend.put("subject", title);

                String author = "";
                boolean allow_comments = false;
                String tags = "";

                if (values.length > 2) {
                    author = values[2];
                    dataToSend.put("author", author);
                }

                if (values.length > 3) {
                    allow_comments = true;
                    dataToSend.put("allow_comments", allow_comments);
                }

                if (values.length > 4) {
                    tags = values[4];
                    dataToSend.put("tags", tags);
                }

                String response = webClient.post()
                        .uri("/article/add")
                        .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dataToSend.toString())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JSONObject jsonResponse = new JSONObject(response);
                String itemValue = jsonResponse.getJSONObject("data").getString("item");

                UserEntity user = userRepository.getById(accessToken.getUserId());
                uploadedArticleRepository.save(UploadedArticle.builder().isDeleted(false).publish(createArticlePDF.isPublish()).title(title).pathFile(filename).dateCreated(new Date()).articleGuid(itemValue).adminId(user).build());
                System.out.println("Response item: " + itemValue);

                articleGuids.add(itemValue);
                if (createArticlePDF.isPublish()) {
                    String publishResponse = webClient.get()
                            .uri("/article/publish/" + itemValue)
                            .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    System.out.println("Publish Response: " + publishResponse);
                }
            }

            return uploadedArticleRepository.findAllByArticleGuidIn(articleGuids);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }

//    @Override
//    public List<UploadedArticle> createArticleFromCSVChoose(CreateArticleCSVExtra createArticlePDF, HttpServletRequest httpServletRequest) throws IOException {
//        try {
//            String token = httpServletRequest.getHeader("Authorization").substring(7);
//            AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);
//
//            InputStream inputStream = createArticlePDF.getFile().getInputStream();
//            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//
//            String originalFilename = StringUtils.cleanPath(createArticlePDF.getFile().getOriginalFilename());
//            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
//            String filename = UUID.randomUUID().toString() + fileExtension;
//
//            String uploadPath = "C:/xampp/htdocs/csv";
//            String filePath = uploadPath + "/" + filename;
//            File uploadDir = new File(uploadPath);
//            if (!uploadDir.exists()) {
//                uploadDir.mkdirs();
//            }
//            try {
//                FileCopyUtils.copy(createArticlePDF.getFile().getBytes(), new File(filePath));
//            } catch (IOException e) {
//                throw new Exception("Error when copying file");
//            }
//
//            List<String> articleGuids = new ArrayList<>();
//            String line;
//            boolean skipHeader = true; // Flag to skip the first line
//            List<String> headerLists = new ArrayList<>();
//            while ((line = br.readLine()) != null) {
//                if (skipHeader) {
//                    skipHeader = false;
//                    headerLists = List.of(line.split(";"));
//                    continue; // Skip processing the first line (header)
//                }
//
//                String[] values = line.split(";");
////                String body = values[1];
//                int columnIndexTitle = 0;
//                for (int i = 0; i < headerLists.toArray().length; i++) {
//
//                    if (headerLists.get(i).equals(createArticlePDF.getTitle())) {
//                        columnIndexTitle = i;
//                        break;
//                    }
//                }
//                String title = values[columnIndexTitle];
//
//
//                StringBuilder bodyBuilder = new StringBuilder();
//                bodyBuilder.append("<div align=\"" + createArticlePDF.getAlignment() + "\">");
//                for (String columnWithType : createArticlePDF.getBodyColumns().split(",")) {
//                    String[] columnParts = columnWithType.split("#");
//                    if (columnParts.length == 2) {
//                        String columnName = columnParts[0].trim();
//                        String additionalInfoStr = columnParts[1].trim();
//
//                        String[] additionalInfo = additionalInfoStr.split(";");
//                        String htmlElement = additionalInfo[0].trim();
//                        String position = "";
//                        String infoLabel = "";
//
//                        if (additionalInfo.length == 3) {
//                            htmlElement = additionalInfo[0].trim();
//                            position = additionalInfo[1].trim();
//                            infoLabel = additionalInfo[2].trim();
//                        }
//                        int columnIndex = 0;
//                        for (int i = 0; i < headerLists.toArray().length; i++) {
//
//                            if (headerLists.get(i).equals(columnName)) {
//                                columnIndex = i;
//                                break;
//                            }
//                        }
//
//                        String fieldValue = values[columnIndex];
//                        if (fieldValue != null) {
//                            String value = fieldValue;
//
//                            if (position.equals("before")) {
//                                bodyBuilder.append("<p style=\"display: inline;\">" + infoLabel + ": </p> ");
//                            }
//
//                            // Add the HTML element with inline style
//                            if ("img".equals(htmlElement) || "video".equals(htmlElement)) {
//                                bodyBuilder.append("<" + htmlElement + " src=\"" + value +
//                                        "\" style=\"height: 300px !important; display: inline;\"" +
//                                        (htmlElement.equals("video") ? " controls" : "") + "/> ");
//                            } else {
//                                bodyBuilder.append("<" + htmlElement + " style=\"display: inline;\">").append(value).append("</" + htmlElement + "> ");
//                            }
//
//                            if (position.equals("after")) {
//                                bodyBuilder.append("<p style=\"display: inline;\"> : " + infoLabel + "</p> ");
//                            }
//                        }
//                    }
//                    bodyBuilder.append("<br/>");
//                }
//
//                bodyBuilder.append("</div>");
//
//
//                String body = bodyBuilder.toString().replace("\\", "");
//
//                JSONObject dataToSend = new JSONObject();
//                dataToSend.put("body", body);
//                dataToSend.put("channel", createArticlePDF.getChannel());
//                dataToSend.put("subject", title);
//
//                String author = "";
//                boolean allow_comments = false;
//                String tags = "";
//
//                if (values.length > 2) {
//                    author = values[2];
//                    dataToSend.put("author", author);
//                }
//
//                if (values.length > 3) {
//                    allow_comments = true;
//                    dataToSend.put("allow_comments", allow_comments);
//                }
//
//                if (values.length > 4) {
//                    tags = values[4];
//                    dataToSend.put("tags", tags);
//                }
//
//                String response = webClient.post()
//                        .uri("/article/add")
//                        .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .bodyValue(dataToSend.toString())
//                        .retrieve()
//                        .bodyToMono(String.class)
//                        .block(); // Make the call synchronous
//
//                JSONObject jsonResponse = new JSONObject(response);
//                String itemValue = jsonResponse.getJSONObject("data").getString("item");
//
//                UserEntity user = userRepository.getById(accessToken.getUserId());
//                uploadedArticleRepository.save(UploadedArticle.builder().title(title).pathFile(filename).dateCreated(new Date()).articleGuid(itemValue).adminId(user).build());
//                System.out.println("Response item: " + itemValue);
//
//                articleGuids.add(itemValue);
//                if (createArticlePDF.isPublish()) {
//                    String publishResponse = webClient.get()
//                            .uri("/article/publish/" + itemValue)
//                            .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
//                            .retrieve()
//                            .bodyToMono(String.class)
//                            .block();
//                    System.out.println("Publish Response: " + publishResponse);
//                }
//            }
//
//            return uploadedArticleRepository.findAllByArticleGuidIn(articleGuids);
//        } catch (Exception e) {
//            throw new CustomException(e.getMessage());
//        }
//    }

    @Override
    public List<UploadedArticle> createArticleFromCSVChoose(CreateArticleCSVExtra createArticlePDF, HttpServletRequest httpServletRequest) throws IOException {
        try {
            String token = httpServletRequest.getHeader("Authorization").substring(7);
            AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);

            InputStream inputStream = createArticlePDF.getFile().getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String originalFilename = StringUtils.cleanPath(createArticlePDF.getFile().getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + fileExtension;

            String uploadPath = "C:/xampp/htdocs/csv";
            String filePath = uploadPath + "/" + filename;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            try {
                FileCopyUtils.copy(createArticlePDF.getFile().getBytes(), new File(filePath));
            } catch (IOException e) {
                throw new Exception("Error when copying file");
            }

            List<String> articleGuids = new ArrayList<>();
            List<Map<String, String>> articles = new ArrayList<>();


            String line;
            boolean skipHeader = true; // Flag to skip the first line
            List<String> headerLists = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                System.out.println("line: " + line);
                if (skipHeader) {
                    skipHeader = false;
                    headerLists = List.of(line.split(";"));
                    continue; // Skip processing the first line (header)
                }


                String[] values = line.split(";");
                int columnIndexTitle = 0;
                for (int i = 0; i < headerLists.toArray().length; i++) {

                    if (headerLists.get(i).equals(createArticlePDF.getTitle())) {
                        columnIndexTitle = i;
                        break;
                    }
                }
                String title = values[columnIndexTitle];


                StringBuilder bodyBuilder = new StringBuilder();
                bodyBuilder.append("<div align=\"" + createArticlePDF.getAlignment() + "\">");
                for (String columnWithType : createArticlePDF.getBodyColumns().split(",")) {
                    String[] columnParts = columnWithType.split("#");
                    if (columnParts.length == 2) {
                        String columnName = columnParts[0].trim();
                        String additionalInfoStr = columnParts[1].trim();

                        String[] additionalInfo = additionalInfoStr.split(";");
                        String htmlElement = additionalInfo[0].trim();
                        String position = "";
                        String infoLabel = "";

                        if (additionalInfo.length == 3) {
                            htmlElement = additionalInfo[0].trim();
                            position = additionalInfo[1].trim();
                            infoLabel = additionalInfo[2].trim();
                        }

                        int columnIndex = 0;
                        for (int i = 0; i < headerLists.toArray().length; i++) {

                            if (headerLists.get(i).equals(columnName)) {
                                columnIndex = i;
                                break;
                            }
                        }

                        String fieldValue = values[columnIndex];

                        if (fieldValue != null) {
                            String value = fieldValue;

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

                String body = bodyBuilder.toString();
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("body", body);
                dataToSend.put("channel", createArticlePDF.getChannel());
                dataToSend.put("subject", title);

                String response = webClient.post()
                        .uri("/article/add")
                        .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dataToSend.toString())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JSONObject jsonResponse = new JSONObject(response);
                String itemValue = jsonResponse.getJSONObject("data").getString("item");

                UserEntity user = userRepository.getById(accessToken.getUserId());
                uploadedArticleRepository.save(UploadedArticle.builder().isDeleted(false).publish(createArticlePDF.isPublish()).title(title).pathFile(filename).dateCreated(new Date()).articleGuid(itemValue).adminId(user).build());
                System.out.println("Response item: " + itemValue);

                articleGuids.add(itemValue);
                if (createArticlePDF.isPublish()) {
                    String publishResponse = webClient.get()
                            .uri("/article/publish/" + itemValue)
                            .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    System.out.println("Publish Response: " + publishResponse);
                }
            }
            return uploadedArticleRepository.findAllByArticleGuidIn(articleGuids);
        } catch (
                Exception e) {
            throw new CustomException(e.getMessage());
        }

    }


    @Override
    public List<Map<String, String>> previewArticleFromCSVChoose(CreateArticleCSVExtra createArticlePDF, HttpServletRequest httpServletRequest) throws IOException {
        try {
            String token = httpServletRequest.getHeader("Authorization").substring(7);
            AccessToken accessToken = new AccessTokenEncoderDecoderImpl().decode(token);

            InputStream inputStream = createArticlePDF.getFile().getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String originalFilename = StringUtils.cleanPath(createArticlePDF.getFile().getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + fileExtension;

            String uploadPath = "C:/xampp/htdocs/csv";
            String filePath = uploadPath + "/" + filename;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            try {
                FileCopyUtils.copy(createArticlePDF.getFile().getBytes(), new File(filePath));
            } catch (IOException e) {
                throw new Exception("Error when copying file");
            }

            List<String> articleGuids = new ArrayList<>();
            List<Map<String, String>> articles = new ArrayList<>();


            String line;
            boolean skipHeader = true; // Flag to skip the first line
            List<String> headerLists = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                System.out.println("line: " + line);
                if (skipHeader) {
                    skipHeader = false;
                    headerLists = List.of(line.split(";"));
                    continue; // Skip processing the first line (header)
                }


                String[] values = line.split(";");
                int columnIndexTitle = 0;
                for (int i = 0; i < headerLists.toArray().length; i++) {

                    if (headerLists.get(i).equals(createArticlePDF.getTitle())) {
                        columnIndexTitle = i;
                        break;
                    }
                }
                String title = values[columnIndexTitle];


                StringBuilder bodyBuilder = new StringBuilder();
                bodyBuilder.append("<div align=\"" + createArticlePDF.getAlignment() + "\">");
                for (String columnWithType : createArticlePDF.getBodyColumns().split(",")) {
                    String[] columnParts = columnWithType.split("#");
                    if (columnParts.length == 2) {
                        String columnName = columnParts[0].trim();
                        String additionalInfoStr = columnParts[1].trim();

                        String[] additionalInfo = additionalInfoStr.split(";");
                        String htmlElement = additionalInfo[0].trim();
                        String position = "";
                        String infoLabel = "";

                        if (additionalInfo.length == 3) {
                            htmlElement = additionalInfo[0].trim();
                            position = additionalInfo[1].trim();
                            infoLabel = additionalInfo[2].trim();
                        }

                        int columnIndex = 0;
                        for (int i = 0; i < headerLists.toArray().length; i++) {

                            if (headerLists.get(i).equals(columnName)) {
                                columnIndex = i;
                                break;
                            }
                        }

                        String fieldValue = values[columnIndex];

                        if (fieldValue != null) {
                            String value = fieldValue;

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

                String body = bodyBuilder.toString();

                Map<String, String> articleData = new HashMap<>();
                articleData.put("body", body);
                articleData.put("subject", title);

                System.out.println("articleData: " + articleData);
                articles.add(articleData);

            }

            return articles;
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }


    @Override
    public List<String> previewColumnsFromCSV(CreateArticlePDF createArticlePDF) throws IOException {
        InputStream inputStream = createArticlePDF.getFile().getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        String headerLine = br.readLine();

        List<String> columnNames = List.of(headerLine.split(";"));

        return columnNames;
    }

    @Override
    public List<UploadedArticle> getAllArticles() {
        return uploadedArticleRepository.findAll();
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
    public List<UploadedArticle> importSelectedColumns(String apiUrl, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String apiKey, String channel, boolean isPublished, String alignment, HttpServletRequest httpServletRequest) {
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
            dataToSend.put("body", body);
            dataToSend.put("channel", channel);
            dataToSend.put("subject", title);
            dataToSend.put("isPublished", isPublished);


            String response = webClient.post()
                    .uri("/article/add")
                    .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(dataToSend.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Make the call synchronous

            JSONObject jsonResponse = new JSONObject(response);
            String itemValue = jsonResponse.getJSONObject("data").getString("item");


            UserEntity user = userRepository.getById(accessToken.getUserId());
            uploadedArticleRepository.save(UploadedArticle.builder().isDeleted(false).publish(isPublished).title(title).pathFile(apiUrl).dateCreated(new Date()).articleGuid(itemValue).adminId(user).build());


            articleGuids.add(itemValue);
            if (isPublished) {
                String publishResponse = webClient.get()
                        .uri("/article/publish/" + itemValue)
                        .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                System.out.println("Publish Response: " + publishResponse);
            }
        }

        return uploadedArticleRepository.findAllByArticleGuidIn(articleGuids);
    }

    @Override
    public List<UploadedArticle> importSelectedColumnsFromList(Long id, String resultsField, String titleSelectedColumn, String bodySelectedColumns, String channel, boolean isPublished, String alignment, HttpServletRequest httpServletRequest) {
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
            dataToSend.put("body", body);
            dataToSend.put("channel", channel);
            dataToSend.put("subject", title);
            dataToSend.put("isPublished", isPublished);


            String response = webClient.post()
                    .uri("/article/add")
                    .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(dataToSend.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Make the call synchronous

            JSONObject jsonResponse = new JSONObject(response);
            String itemValue = jsonResponse.getJSONObject("data").getString("item");


            UserEntity user = userRepository.getById(accessToken.getUserId());
            uploadedArticleRepository.save(UploadedArticle.builder().isDeleted(false).publish(isPublished).title(title).pathFile(sources.get().getUrl()).dateCreated(new Date()).articleGuid(itemValue).adminId(user).build());


            articleGuids.add(itemValue);
            if (isPublished) {
                String publishResponse = webClient.get()
                        .uri("/article/publish/" + itemValue)
                        .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                System.out.println("Publish Response: " + publishResponse);
            }
        }

        return uploadedArticleRepository.findAllByArticleGuidIn(articleGuids);
    }


    @Override
    public String deleteArticle(String articleGuid) {
        UploadedArticle uploadedArticle = uploadedArticleRepository.findByArticleGuid(articleGuid);
        if (uploadedArticle == null) {
            throw new CustomException("Article not found");
        }
        String response = webClient.delete()
                .uri("/article/delete/" + uploadedArticle.getArticleGuid())
                .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        uploadedArticle.setIsDeleted(true);
        uploadedArticleRepository.save(uploadedArticle);
        return response;
    }

    @Override
    public String unpublishArticle(String articleGuid) {
        UploadedArticle uploadedArticle = uploadedArticleRepository.findByArticleGuid(articleGuid);
        if (uploadedArticle == null) {
            throw new CustomException("Article not found");
        }
        String response = webClient.get()
                .uri("/article/unpublish/" + uploadedArticle.getArticleGuid())
                .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        uploadedArticle.setPublish(false);
        uploadedArticleRepository.save(uploadedArticle);
        return response;
    }

    @Override
    public String publishArticle(String articleGuid) {
        UploadedArticle uploadedArticle = uploadedArticleRepository.findByArticleGuid(articleGuid);
        if (uploadedArticle == null) {
            throw new CustomException("Article not found");
        }
        String response = webClient.get()
                .uri("/article/publish/" + uploadedArticle.getArticleGuid())
                .headers(headers -> headers.setBasicAuth("Starship", basicAuthPassword))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        uploadedArticle.setPublish(true);
        uploadedArticleRepository.save(uploadedArticle);
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
