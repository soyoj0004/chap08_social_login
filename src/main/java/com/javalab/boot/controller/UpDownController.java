package com.javalab.boot.controller;

import com.javalab.boot.dto.upload.UploadFileDTO;
import com.javalab.boot.dto.upload.UploadResultDTO;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 업로드 레스트 컨트롤러
 */
@RestController
@Log4j2
public class UpDownController {

    //  환경설정파일에 com.javalab.boot.upload.path 이름으로 설정해놓은 값 조회
    @Value("${com.javalab.boot.upload.path}")
    private String uploadPath;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<UploadResultDTO> upload(UploadFileDTO uploadFileDTO){

        log.info("업로드 파일명 : " + uploadFileDTO);

        if(uploadFileDTO.getFiles() != null){

            final List<UploadResultDTO> list = new ArrayList<>();

            uploadFileDTO.getFiles().forEach(multipartFile -> {

                String originalName = multipartFile.getOriginalFilename();
                log.info(originalName);

                String uuid = UUID.randomUUID().toString();

                Path savePath = Paths.get(uploadPath, uuid+"_"+ originalName);

                boolean image = false;

                try {
                    multipartFile.transferTo(savePath);

                    //이미지 파일의 종류라면
                    if(Files.probeContentType(savePath).startsWith("image")){

                        image = true;

                        File thumbFile = new File(uploadPath, "s_" + uuid+"_"+ originalName);

                        Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200,200);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                list.add(UploadResultDTO.builder()
                        .uuid(uuid)
                        .fileName(originalName)
                        .img(image).build()
                );

            });//end each

            return list;
        }//end if

        return null;
    }


    /**
     * ResponseEntity :
     *  - HTTP응답을 표현하는 클래스로, 상태 코드, 헤더, 본문 등을 포함.
     * ResponseEntity.ok() :
     *  - 메서드는 HTTP 상태 코드 200 (OK)를 가진 ResponseEntity 객체를
     *    생성하는 메서드.
     *  ResponseEntity<String> responseEntity = ResponseEntity
     *         .status(HttpStatus.OK) // 상태 코드 설정
     *         .header("Custom-Header", "foo") // 헤더 설정
     *         .body("Hello World"); // 본문 설정
     */
    @GetMapping("/view/{fileName}")
    public ResponseEntity<Resource> viewFileGET(@PathVariable("fileName") String fileName){

        /**
         * new FileSystemResource(파라미터) : 파라미터로 주어진 경로의
         * 파일을 핸들링 할 수 있는 Resource 객체 생성.
         * Resource 객체는 파일 시스템에 있는 파일을 나타냄.
         * 요청Url => http://localhost:8080/view/aaa.jpg
         */
        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
        String resourceName = resource.getFilename();
        HttpHeaders headers = new HttpHeaders();

        try{
            headers.add("Content-Type", Files.probeContentType( resource.getFile().toPath() ));
        } catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }
        /**
         * ResponseEntity.ok(): 상태 코드 200 (OK)로 응답 객체를 생성.
         * body(resource): 리소스 (파일)를 응답 본문에 추가.
         * ResponseEntity 객체에 추가적인 정보를 체이닝 방식으로 설정
         */
         //return ResponseEntity.ok().headers(headers).body(resource); // 동일한 결과

        return ResponseEntity.status(HttpStatus.OK) // 상태 코드 설정
                .headers(headers) // 헤더 설정
                .body(resource); // 본문 설정
    }

    @DeleteMapping("/remove/{fileName}")
    public Map<String,Boolean> removeFile(@PathVariable("fileName") String fileName){

        /**
         * new FileSystemResource(파라미터) : 파라미터로 주어진 경로의
         * 파일을 핸들링 할 수 있는 Resource 객체 생성.
         * Resource 객체는 파일 시스템에 있는 파일을 나타냄.
         * http://localhost:8080/view/aaa.jpg(이미지 url)
         */
        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
        String resourceName = resource.getFilename();

        Map<String, Boolean> resultMap = new HashMap<>();
        boolean removed = false;

        try {
            //
            String contentType = Files.probeContentType(resource.getFile().toPath());
            // resource에 해당하는 파일을 삭제하고 삭제 여부를 반환
            removed = resource.getFile().delete();

            //섬네일이 존재한다면 삭제
            if(contentType.startsWith("image")){
                File thumbnailFile = new File(uploadPath+File.separator +"s_" + fileName);
                thumbnailFile.delete();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        resultMap.put("result", removed);
        return resultMap;
    }

}
