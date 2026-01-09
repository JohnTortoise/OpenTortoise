package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Tool;
import io.github.johntortoise.core.dto.sys.*;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.*;
import io.github.johntortoise.dto.*;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.mapper.TortoiseToolMapper;
import io.github.johntortoise.mapper.TortoiseUserMapper;
import io.github.johntortoise.model.TortoiseConversation;
import io.github.johntortoise.model.TortoiseTool;
import io.github.johntortoise.model.TortoiseUser;
import io.github.johntortoise.service.TortoiseToolService;
import io.github.johntortoise.service.TortoiseUserService;
import io.github.johntortoise.utils.PageConvertUtil;
import io.github.johntortoise.utils.ToolHttpClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
public class TortoiseToolImpl extends ServiceImpl<TortoiseToolMapper, TortoiseTool> implements TortoiseToolService {


    @Override
    public boolean createTool(CreateToolDTO createToolDTO) {
        try {
            validParams(createToolDTO);

            TortoiseTool tortoiseTool = new TortoiseTool();
            tortoiseTool.setName(createToolDTO.getName());
            tortoiseTool.setDesc(createToolDTO.getDesc());
            if(EmptyUtil.isNotEmpty(createToolDTO.getFieldConfig())){
                tortoiseTool.setFieldConfig(ObjectMapperUtil.createObjectMapper().writeValueAsString(createToolDTO.getFieldConfig()));
            }
            tortoiseTool.setUrl(createToolDTO.getUrl());
            tortoiseTool.setMethod(createToolDTO.getMethod());
            if(EmptyUtil.isNotEmpty(createToolDTO.getHeaders())){
                tortoiseTool.setHeaders(ObjectMapperUtil.createObjectMapper().writeValueAsString(createToolDTO.getHeaders()));
            }
            this.save(tortoiseTool);
            return true;

        }catch (Exception e){
            LogUtil.error("创建工具出错",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"创建工具出错:"+e.getMessage());
        }

    }

    @Override
    public boolean updateTool(TortoiseToolDetailDTO updateToolDTO) {
        try {
            if (EmptyUtil.isEmpty(updateToolDTO.getId())) {
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "工具ID不能为空");
            }

            TortoiseTool tortoiseTool = this.getById(updateToolDTO.getId());
            if (tortoiseTool == null) {
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "工具不存在");
            }

            // Validate basic params (without name uniqueness check)
            String name = updateToolDTO.getName();
            if(EmptyUtil.isEmpty(name)){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"工具名称不能为空");
            }

            List<String> supportMethod = Arrays.asList("GET", "POST");
            if(EmptyUtil.isEmpty(updateToolDTO.getMethod())){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"请求方法不能为空");
            }
            if(!supportMethod.contains(updateToolDTO.getMethod())){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"只支持GET和POST请求");
            }

            // Check name uniqueness only if name changed
            if (!name.equals(tortoiseTool.getName())) {
                TortoiseTool existingTool = this.getOne(new QueryWrapper<TortoiseTool>().lambda()
                        .eq(TortoiseTool::getName, name));
                if(Objects.nonNull(existingTool)){
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"工具名称已存在");
                }
            }

            String desc = updateToolDTO.getDesc();
            if(EmptyUtil.isEmpty(desc)){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"工具描述不能为空");
            }

            // Validate field config
            List<FieldDTO> fieldConfig = updateToolDTO.getFieldConfig();
            if(EmptyUtil.isNotEmpty(fieldConfig)){
                for(FieldDTO fieldDTO:fieldConfig){
                    String fieldName = fieldDTO.getName();
                    String description = fieldDTO.getDescription();
                    String type = fieldDTO.getType();
                    if(EmptyUtil.isEmpty(fieldName)){
                        throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"字段名称不能为空");
                    }
                    if(EmptyUtil.isEmpty(description)){
                        throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"字段描述不能为空");
                    }
                    if(EmptyUtil.isEmpty(type)){
                        throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"字段类型不能为空");
                    }
                }
            }

            // Validate headers
            List<HeadersDTO> headers = updateToolDTO.getHeaders();
            if(EmptyUtil.isNotEmpty(headers)){
                for(HeadersDTO headersDTO:headers){
                    if(EmptyUtil.isEmpty(headersDTO.getName())){
                        throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"请求头名称不能为空");
                    }
                    if(EmptyUtil.isEmpty(headersDTO.getValue())){
                        throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"请求头值不能为空");
                    }
                }
            }

            if(EmptyUtil.isEmpty(updateToolDTO.getUrl())){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"请求的url不能为空");
            }

            // Update tool
            tortoiseTool.setName(updateToolDTO.getName());
            tortoiseTool.setDesc(updateToolDTO.getDesc());
            if(EmptyUtil.isNotEmpty(updateToolDTO.getFieldConfig())){
                tortoiseTool.setFieldConfig(ObjectMapperUtil.createObjectMapper().writeValueAsString(updateToolDTO.getFieldConfig()));
            } else {
                tortoiseTool.setFieldConfig(null);
            }
            tortoiseTool.setUrl(updateToolDTO.getUrl());
            tortoiseTool.setMethod(updateToolDTO.getMethod());
            if(EmptyUtil.isNotEmpty(updateToolDTO.getHeaders())){
                tortoiseTool.setHeaders(ObjectMapperUtil.createObjectMapper().writeValueAsString(updateToolDTO.getHeaders()));
            } else {
                tortoiseTool.setHeaders(null);
            }
            this.updateById(tortoiseTool);
            return true;

        }catch (Exception e){
            LogUtil.error("更新工具出错",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"更新工具出错:"+e.getMessage());
        }
    }

    public BatchGetToolByNamesResp batchGetToolByNames(BatchGetToolByNamesReq batchGetToolByNamesReq){
        try {
            List<Tool> tools = new ArrayList<>();
            List<String> toolsName = batchGetToolByNamesReq.getToolsNameList();
            if(EmptyUtil.isNotEmpty(toolsName)){
                List<TortoiseTool> byNames = getByNames(toolsName);
                for(TortoiseTool tortoiseTool:byNames){
                    String fieldConfig = tortoiseTool.getFieldConfig();
                    List<FieldDTO> fieldDTOS = null;
                    if(EmptyUtil.isNotEmpty(fieldConfig)){
                        fieldDTOS = ObjectMapperUtil.createObjectMapper().readValue(fieldConfig, new TypeReference<List<FieldDTO>>() {});
                    }
                    tools.add(FunctionCallToolUtil.quickCreateTool(tortoiseTool.getName(), tortoiseTool.getDesc(), fieldDTOS));
                }
            }
            return BatchGetToolByNamesResp.builder().tools(tools).build();
        }catch (Exception e){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e.getMessage());
        }
    }

    public String callTool(String name,String args){
        try {
            TortoiseTool tortoiseTool = getByName(name);
            ToolHttpClient toolHttpClient = new ToolHttpClient();
            List<HeadersDTO> headersDTOS = ObjectMapperUtil.createObjectMapper().readValue(tortoiseTool.getHeaders(), new TypeReference<List<HeadersDTO>>() {});
            String url = tortoiseTool.getUrl();
            if(tortoiseTool.getMethod().equals("GET")){
                return toolHttpClient.callGetCurl(name,url,headersDTOS,args);
            }else if(tortoiseTool.getMethod().equals("POST")){
                return toolHttpClient.callPostCurl(name,url,headersDTOS,args);
            }
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,String.format("工具%s的请求方式不为GET或POST",name));
        }catch (Exception e){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e.getMessage());
        }
    }

    @Override
    public Page<TortoiseToolDTO> page(String toolName, Long current, Long size) {
        LambdaQueryWrapper<TortoiseTool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TortoiseTool::getIsDeleted, DeletedEnum.EXIST.getCode())
                .orderByDesc(TortoiseTool::getUpdateTime);
        if (EmptyUtil.isNotEmpty(toolName)) {
            queryWrapper.like(TortoiseTool::getName, toolName);
        }
        Page<TortoiseTool> page = this.baseMapper.selectPage(new Page<>(current, size), queryWrapper);
        return PageConvertUtil.convert(page, TortoiseTool::covertToDTO);
    }

    @Override
    public TortoiseToolDetailDTO detail(Long id) {
        try {
            TortoiseTool tortoiseTool = this.getById(id);

            TortoiseToolDetailDTO tortoiseToolDetailDTO = new TortoiseToolDetailDTO();
            tortoiseToolDetailDTO.setId(id);
            tortoiseToolDetailDTO.setName(tortoiseTool.getName());
            tortoiseToolDetailDTO.setDesc(tortoiseTool.getDesc());
            tortoiseToolDetailDTO.setMethod(tortoiseTool.getMethod());
            tortoiseToolDetailDTO.setUrl(tortoiseTool.getUrl());

            List<FieldDTO> fieldDTOS = null;
            if(EmptyUtil.isNotEmpty(tortoiseTool.getFieldConfig())){
                fieldDTOS = ObjectMapperUtil.createObjectMapper().readValue(tortoiseTool.getFieldConfig(), new TypeReference<List<FieldDTO>>() {});
            }

            List<HeadersDTO> headersDTOS = null;
            if(EmptyUtil.isNotEmpty(tortoiseTool.getHeaders())){
                headersDTOS = ObjectMapperUtil.createObjectMapper().readValue(tortoiseTool.getHeaders(), new TypeReference<List<HeadersDTO>>() {});
            }

            tortoiseToolDetailDTO.setHeaders(headersDTOS);
            tortoiseToolDetailDTO.setFieldConfig(fieldDTOS);

            return tortoiseToolDetailDTO;
        }catch (Exception e){
            LogUtil.error("查询工具详情失败",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"查询工具详情失败");
        }
    }


    public TortoiseTool getByName(String name){
        TortoiseTool tortoiseTool = this.getOne(new QueryWrapper<TortoiseTool>().lambda()
                .eq(TortoiseTool::getName, name));
        if(Objects.isNull(tortoiseTool)){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"找不到工具");
        }
        return tortoiseTool;
    }

    public List<TortoiseTool> getByNames(List<String> names){
        return this.list(new QueryWrapper<TortoiseTool>().lambda()
                .in(TortoiseTool::getName, names));
    }



    @Override
    public ParseCUrlDTO parseCurl(ParseCUrlReq req) {
        try {
            String curl = req.getCurl();

            String decodeUrl = URLDecoder.decode(curl, StandardCharsets.UTF_8.toString());

            CurlRequest parse = CurlParser.parse(decodeUrl);

            ParseCUrlDTO parseCUrlDTO = new ParseCUrlDTO();

            String originalUrl = parse.getUrl();
            String regex = "^([^?#]+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(originalUrl);
            if (matcher.find()) {
                parseCUrlDTO.setUrl(matcher.group(1));
            } else {
                parseCUrlDTO.setUrl(originalUrl);
            }


            parseCUrlDTO.setMethod(parse.getMethod());

            List<HeadersDTO> headersDTOS = new ArrayList<>();
            List<FieldDTO> fieldDTOS = new ArrayList<>();

            if(EmptyUtil.isNotEmpty(parse.getHeaders())){
                parse.getHeaders().forEach((key,value)->{
                    headersDTOS.add(HeadersDTO.builder().name(key).value(value).build());
                });
            }
            parseCUrlDTO.setHeaders(headersDTOS);

            if(EmptyUtil.isNotEmpty(parse.getRequestBody()) && EmptyUtil.isNotEmpty(parse.getQueryParams())){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"请求体的参数和请求参数不能同时使用");
            }

            if(EmptyUtil.isNotEmpty(parse.getRequestBody())){
                ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(parse.getRequestBody());

                if(jsonNode != null && jsonNode.isObject()){
                    Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                    while(fields.hasNext()){
                        Map.Entry<String, JsonNode> entry = fields.next();
                        String key = entry.getKey();
                        JsonNode valueNode = entry.getValue();

                        String valueStr;
                        if(valueNode.isNull()){
                            valueStr = "";
                        } else if(valueNode.isTextual()){
                            valueStr = valueNode.asText();
                        } else {
                            // 对于数字、布尔等其他类型，转换为字符串
                            valueStr = valueNode.toString();
                        }

                        fieldDTOS.add(FieldDTO.builder()
                                .name(key)
                                .description("")
                                .enumValues(null)
                                .required(true)
                                .type(CurlParser.getType(valueStr))
                                .build());
                    }
                }
            }

            if(EmptyUtil.isNotEmpty(parse.getQueryParams())){
                parse.getQueryParams().forEach((key,value)->{
                    fieldDTOS.add(FieldDTO.builder()
                            .name(key)
                            .description("")
                            .enumValues(null)
                            .required(true)
                            .type(CurlParser.getType(value))
                            .build());
                });
            }
            parseCUrlDTO.setFieldConfig(fieldDTOS);
            return parseCUrlDTO;
        }catch (Exception e){
            log.error("解析curl失败",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"解析curl失败");
        }
    }

    public void validParams(CreateToolDTO createToolDTO){
        String name = createToolDTO.getName();

        if(EmptyUtil.isEmpty(name)){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"工具名称不能为空");
        }

        List<String> supportMethod = Arrays.asList("GET", "POST");

        if(EmptyUtil.isEmpty(createToolDTO.getMethod())){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"请求方法不能为空");
        }
        if(!supportMethod.contains(createToolDTO.getMethod())){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"只支持GET和POST请求");
        }

        TortoiseTool tortoiseTool = this.getOne(new QueryWrapper<TortoiseTool>().lambda()
                .eq(TortoiseTool::getName, name));
        if(Objects.nonNull(tortoiseTool)){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"工具名称已存在");
        }
        String desc = createToolDTO.getDesc();
        if(EmptyUtil.isEmpty(desc)){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"工具描述不能为空");
        }
        List<FieldDTO> fieldConfig = createToolDTO.getFieldConfig();
        if(EmptyUtil.isNotEmpty(fieldConfig)){
            for(FieldDTO fieldDTO:fieldConfig){
                String fieldName = fieldDTO.getName();
                String description = fieldDTO.getDescription();
                String type = fieldDTO.getType();
                if(EmptyUtil.isEmpty(fieldName)){
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"字段名称不能为空");
                }
                if(EmptyUtil.isEmpty(description)){
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"字段描述不能为空");
                }
                if(EmptyUtil.isEmpty(type)){
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"字段类型不能为空");
                }
            }
        }
        List<HeadersDTO> headers = createToolDTO.getHeaders();
        if(EmptyUtil.isNotEmpty(headers)){
            for(HeadersDTO headersDTO:headers){
                if(EmptyUtil.isEmpty(headersDTO.getName())){
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"请求头名称不能为空");
                }
                if(EmptyUtil.isEmpty(headersDTO.getValue())){
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"请求头值不能为空");
                }
            }
        }
        if(EmptyUtil.isEmpty(createToolDTO.getUrl())){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"请求的url不能为空");
        }
    }
}