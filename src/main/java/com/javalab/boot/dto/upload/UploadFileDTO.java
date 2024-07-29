package com.javalab.boot.dto.upload;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 업로드하는 파일 목록을 저장하는 역할
 */
@Data
public class UploadFileDTO {

    private List<MultipartFile> files;
}

