package io.github.johntortoise.core.utils;

import io.github.johntortoise.core.dto.model.Tool;
import io.github.johntortoise.core.dto.sys.FieldDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionCallToolUtil {

    public static Tool quickCreateTool(String name, String desc, List<FieldDTO> fieldDTOS){

        Tool tool = new Tool();
        tool.setType("function");

        Tool.FunctionDetail functionDetail = new Tool.FunctionDetail();
        tool.setFunction(functionDetail);


        functionDetail.setName(name);
        functionDetail.setDescription(desc);

        Tool.Parameters parameters = new Tool.Parameters();
        parameters.setType("object");
        functionDetail.setParameters(parameters);


        List<String> requireFieldList = new ArrayList<>();

        Map<String, Tool.Property> properties = new HashMap<>();
        if(EmptyUtil.isNotEmpty(fieldDTOS)){
            for(FieldDTO fieldDTO:fieldDTOS){
                if(fieldDTO.getRequired()){
                    requireFieldList.add(fieldDTO.getName());
                }
                Tool.Property property = new Tool.Property();
                property.setType(fieldDTO.getType());
                property.setDescription(fieldDTO.getDescription());
                property.setEnumValues(fieldDTO.getEnumValues());
                properties.put(fieldDTO.getName(),property);
            }
        }
        parameters.setRequired(requireFieldList);
        parameters.setProperties(properties);
        parameters.setType("object");

        return tool;

    }
}
