package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.context.TortoiseContext;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.model.TortoiseImportFileRecord;
import io.github.johntortoise.service.TortoiseConversationService;
import io.github.johntortoise.service.TortoiseImportFileRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@RestController
@Slf4j
@RequestMapping("/api/conversations")
public class TortoiseConversationController {

    @Resource
    private TortoiseConversationService conversationService;


    @Resource
    private TortoiseImportFileRecordService tortoiseImportFileRecordService;

    @Value("${temp.file.path}")
    private String tempFilePath;

    
    @GetMapping("/page")
    public TortoiseBaseResult<Page<TortoiseConversationDTO>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String conversationId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Long pageSize) {
        try {
            LogUtil.debug("分页查询对话: userId={}, conversationId={}, pageNum={}, pageSize={}",
                    userId, conversationId, pageNum, pageSize);
            Page<TortoiseConversationDTO> page = conversationService.page(conversationId,
                    userId, pageNum, pageSize);
            return TortoiseBaseResult.ok(page);
        } catch (Exception e) {
            LogUtil.error("分页查询对话失败: userId={}, conversationId={}", userId, conversationId, e);
            throw e;
        }
    }

    
    @PostMapping("/import-excel")
    public TortoiseBaseResult<Void> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return TortoiseBaseResult.fail("上传的Excel文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        LogUtil.info("开始导入Excel文件: {}", originalFilename);

        Long recordId = tortoiseImportFileRecordService.saveAndReturnId(TortoiseContext.getCurrentUserId(), originalFilename);

        
        File tempDir = new File(tempFilePath);
        if (!tempDir.exists()) {
            boolean mkdir = tempDir.mkdirs();
            if (!mkdir) {
                return TortoiseBaseResult.fail("创建临时目录失败");
            }
        }

        String tempFileName = UUID.randomUUID() + "_" + originalFilename;
        Path tempFile = Paths.get(tempFilePath, tempFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            LogUtil.info("文件已保存到临时路径: {}", tempFile);
            conversationService.importExcel(recordId,tempFile.toString(), originalFilename);
            LogUtil.info("异步方法后: {}", tempFile);
            return TortoiseBaseResult.ok();
        } catch (IOException e) {
            tortoiseImportFileRecordService.fail(recordId,e.getMessage());
            LogUtil.error("保存临时文件失败: {}", originalFilename, e);
            return TortoiseBaseResult.ok();
        }
    }

    @GetMapping("importRecordPage")
    public TortoiseBaseResult<Page<TortoiseImportFileRecord>>  importRecord(@RequestParam(required = false)Long createId,
                                                                        @RequestParam(required = false)String fileName,
                                                                        @RequestParam(required = false) Integer status,
                                                                        @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long pageNum,
                                                                        @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Long pageSize){
        return TortoiseBaseResult.ok(tortoiseImportFileRecordService.page(createId,fileName,pageNum,pageSize,status));
    }


    
    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        LogUtil.info("下载Excel导入模板");

        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=conversaion_import_template.xlsx");

        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("file/conversaion_import_template.xlsx")) {
            if (inputStream == null) {
                LogUtil.error("模板文件不存在: file/conversaion_import_template.xlsx");
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "模板文件不存在");
                return;
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.getOutputStream().flush();
        }
    }

    @GetMapping("originalFilename")
    public void testInsert(String originalFilename){
        Long recordId = tortoiseImportFileRecordService.saveAndReturnId(TortoiseContext.getCurrentUserId(), originalFilename);
        tortoiseImportFileRecordService.success(recordId);
    }
}