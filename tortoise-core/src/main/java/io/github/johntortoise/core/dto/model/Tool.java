package io.github.johntortoise.core.dto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("function")
    private FunctionDetail function;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionDetail {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("parameters")
        private Parameters parameters;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters {
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("properties")
        private Map<String, Property> properties;
        
        @JsonProperty("required")
        private List<String> required;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Property {

        @JsonProperty("type")
        private String type;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("enum")
        private List<String> enumValues;
    }
}