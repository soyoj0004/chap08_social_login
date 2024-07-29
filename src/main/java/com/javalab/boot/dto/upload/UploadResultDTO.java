package com.javalab.boot.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [업로드 처리 결과 반환]
 *  - 링크(link) 만들기 : UUID + 파일명을 연결해서 만들고
 *    이미지 표시에 사용
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResultDTO {

    private String uuid;

    private String fileName;

    private boolean img;

    // 생략도 가능, @Data + getLink()메소드가 있으므로
    // 자동으로 link 속성이 만들어진다.
    private String link;

    // 자신의 속성들을 조합해서 link 생성
    public String getLink(){
        if(img){
            return "s_"+ uuid +"_"+fileName; //이미지인 경우 섬네일
        }else {
            return uuid+"_"+fileName;
        }
    }
}
