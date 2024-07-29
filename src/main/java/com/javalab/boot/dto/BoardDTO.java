package com.javalab.boot.dto;

import com.javalab.boot.entity.Board;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
public class BoardDTO {

    private Long bno;

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    @Size(min = 3, max = 100, message = "제목은 3자 이상 10자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 값입니다.")
    private String content;

    @NotBlank(message = "작성자는 필수 입력 값입니다.")
    private String writer;

    private LocalDateTime regDate;
    private LocalDateTime modDate;

    //첨부파일의 이름들
    private List<String> fileNames;

    // Dto -> Entity
    // 이 객체가 대부분의 정보를 갖고 있어서 여기에 만듦.
    public Board dtoToEntity() {
        Board board = Board.builder()
                .bno(this.getBno())
                .title(this.getTitle())
                .content(this.getContent())
                .writer(this.getWriter())
                .build();

        // 첨부 이미지 파일이 있을 경우
        if(this.getFileNames() != null){
            this.getFileNames().forEach(fileName -> {
                String[] arr = fileName.split("_");
                board.addImage(arr[0], arr[1]);
            });
        }
        log.info("변환된 board : " + board.toString());

        return board;
    }

}
