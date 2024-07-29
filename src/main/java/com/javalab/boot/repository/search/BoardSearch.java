package com.javalab.boot.repository.search;

import com.javalab.boot.dto.BoardListAllDTO;
import com.javalab.boot.dto.BoardListReplyCountDTO;
import com.javalab.boot.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardSearch {
    Page<Board> search1(Pageable pageable);

    Page<Board> searchAll(String[] types, String keyword, Pageable pageable);
    // 추가
    public Page<BoardListReplyCountDTO> searchWithReplyCount(String[] types,
                                                             String keyword,
                                                             Pageable pageable);

    // 추가
    Page<BoardListAllDTO> searchWithAll(String[] types,
                                        String keyword,
                                        Pageable pageable);
}
