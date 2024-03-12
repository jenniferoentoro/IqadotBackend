package iqadot.iqadotkit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/json")
public class JsonController {
    @Value("${basic.auth.password}")
    private String basicAuthPassword;
    private final WebClient webClient;

    JsonController() {
        this.webClient = WebClient.builder()
                .baseUrl("https://fs3-admin.iqadot.com/api/v3/admin")
                .build();
    }

    @GetMapping("/getColumns")
    public List<String> getColumns(@RequestParam String apiUrl, @RequestParam(required = false) String apiKey) {
        RestTemplate restTemplate = new RestTemplate();

        // Append apiKey to apiUrl if provided
        String apiUrlWithApiKey = appendApiKey(apiUrl, apiKey);

        String jsonString = restTemplate.getForObject(apiUrlWithApiKey, String.class);
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

    @GetMapping("/getData")
    public JsonNode getData(@RequestParam String apiUrl, @RequestParam String field, @RequestParam(required = false) String apiKey) {
        RestTemplate restTemplate = new RestTemplate();

        // Append apiKey to apiUrl if provided
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

    @GetMapping("/getColumnsInResults")
    public List<String> getColumnsInResults(
            @RequestParam String apiUrl,
            @RequestParam String resultsField,
            @RequestParam(required = false) String apiKey
    ) {
        RestTemplate restTemplate = new RestTemplate();

        // Append apiKey to apiUrl if provided
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


    @GetMapping("/getSelectedColumns")
    public List<JsonNode> getColumnsWithSelectedFields(
            @RequestParam String apiUrl,
            @RequestParam String resultsField,
            @RequestParam String selectedColumns,
            @RequestParam(required = false) String apiKey
    ) {
        RestTemplate restTemplate = new RestTemplate();

        // Append apiKey to apiUrl if provided
        String apiUrlWithApiKey = appendApiKey(apiUrl, apiKey);

        String jsonString = restTemplate.getForObject(apiUrlWithApiKey, String.class);

        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> resultNodes = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(jsonString);
            JsonNode resultsNode = root.path(resultsField);

            if (resultsNode.isArray() && resultsNode.size() > 0 && resultsNode.get(0).isObject()) {
                for (JsonNode node : resultsNode) {
                    resultNodes.add(getSelectedFields(node, selectedColumns));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultNodes;
    }

    private JsonNode getSelectedFields(JsonNode node, String selectedColumns) {
        String[] columns = selectedColumns.split(",");

        if (columns.length == 1 && columns[0].equals("*")) {
            return node;
        }

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode resultNode = new ObjectNode(factory);

        for (String column : columns) {
            if (column.contains(".")) {
                String[] nestedFields = column.split("\\.");
                JsonNode nestedNode = node;

                for (String nestedField : nestedFields) {
                    nestedNode = findField(nestedNode, nestedField, true);
                    if (nestedNode == null) {
                        break;
                    }
                }

                resultNode.set(column, nestedNode);
            } else {
                JsonNode fieldValue = findField(node, column, true);
                if (fieldValue != null) {
                    resultNode.set(column, fieldValue);
                }
            }
        }

        return resultNode;
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
    @GetMapping("/previewSelectedColumns")
    public List<Map<String, String>> preview(
            @RequestParam String apiUrl,
            @RequestParam String resultsField,
            @RequestParam String titleSelectedColumn,
            @RequestParam String bodySelectedColumns,
            @RequestParam(required = false) String apiKey,
            @RequestParam String alignment
    ) {
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
                    Map<String, String> result = getSelectedFieldsWithFormatting(node, titleSelectedColumn, bodySelectedColumns,alignment);
                    resultNodes.add(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultNodes;

    }

    @GetMapping("/importSelectedColumns")
    public List<Map<String, String>> getColumnsWithSelectedFields(
            @RequestParam String apiUrl,
            @RequestParam String resultsField,
            @RequestParam String titleSelectedColumn,
            @RequestParam String bodySelectedColumns,
            @RequestParam(required = false) String apiKey,
            @RequestParam String channel,
            @RequestParam boolean isPublished,
            @RequestParam String alignment, HttpServletRequest httpServletRequest
    ) {
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
                    Map<String, String> result = getSelectedFieldsWithFormatting(node, titleSelectedColumn, bodySelectedColumns,alignment);
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

        return resultNodes;

    }

    private Map<String, String> getSelectedFieldsWithFormatting(JsonNode node, String titleSelectedColumn, String bodySelectedColumns,String alignment) {
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
        bodyBuilder.append("<div align=\""+alignment+"\">");
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
