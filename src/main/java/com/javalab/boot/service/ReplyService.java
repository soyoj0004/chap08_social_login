package com.javalab.boot.service;


import com.javalab.boot.dto.PageRequestDTO;
import com.javalab.boot.dto.PageResponseDTO;
import com.javalab.boot.dto.ReplyDTO;

public interface ReplyService {

    Long register(ReplyDTO replyDTO);

    ReplyDTO read(Long rno);

    void modify(ReplyDTO replyDTO);

    void remove(Long rno);

    PageResponseDTO<ReplyDTO> getListOfBoard(Long bno, PageRequestDTO pageRequestDTO);

}
