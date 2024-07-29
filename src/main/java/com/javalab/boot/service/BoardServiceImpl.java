package com.javalab.boot.service;

import com.javalab.boot.dto.*;
import com.javalab.boot.entity.Board;
import com.javalab.boot.entity.BoardImage;
import com.javalab.boot.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Service : 본 클래스는 서비스 객체로서 빈으로 생성되어 스프링 컨테이너에
 * 의해서 관리됨.
 * @Transactional : 모든 메소드에 트랜잭션이 적용되어 메소드 실행 전후에
 * 트랜잭션 관련 로직을 실행시킬 수 있음.
 */
@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService{

    /*
      @RequiredArgsConstructor + final 변수를 매개변수로 받는
      생성자를 자동으로 만들어줌.
     */
    private final ModelMapper modelMapper;
    private final BoardRepository boardRepository;

    // 다음과 같은 생성자가 자동으로 생성됨.
    /*
    public BoardServiceImpl(ModelMapper modelMapper,
                        BoardRepository boardRepository) {
        this.modelMapper = modelMapper;
        this.boardRepository = boardRepository;
    }
    */

    /*
    @Override
    public Long register(BoardDTO boardDTO) {
        // 전달된 Dto -> Entity로 변환
        Board board = modelMapper.map(boardDTO, Board.class);
        // 변환된 Entity 객체 저장하고 저장된 엔티티 반환
        board = boardRepository.save(board);
        // 반환된 엔티티 객체에서 bno 값 추출
        Long bno = board.getBno();

        return bno;
    }
    */

    @Override
    public Long register(BoardDTO boardDTO) {
        /*
        Board board = dtoToEntity(boardDTO);
        Long bno = boardRepository.save(board).getBno();
        return bno;
        */

        Board board = boardDTO.dtoToEntity();
        Long bno = boardRepository.save(board).getBno();
        return bno;
    }

    /*
      게시물 상세조회
       - BoardNotFoundException 예외처리 클래스 필요
     */
    /*
    @Override
    public BoardDTO readOne(Long bno) {
        // Board 엔티티 객체 얻기
        Optional<Board> result = boardRepository.findById(bno);
        // 이때까지는 BoardImage가 로딩되지 않은 상태이다.
        Board board = result.orElseThrow();
        // 엔티티 객체 -> Dto 로 변환
        //BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);
        // 다음 메소드를 호출하면 그 곳에서 BoardIMage가 로딩된다.
        BoardDTO boardDTO = board.entityToDto();
        log.info("boardDTO : " + boardDTO.toString());
        return boardDTO;
    }
    */

    @Override
    public BoardDTO readOne(Long bno) {
        /*
          [Board 엔티티 객체 얻기]
          findByIdWithImages() 메소드에는 @EntityGraph가 있어서
          한번 쿼리할 때 BoardImage들도 동시에 Left Join 해서 갖고옴.
         */
        Optional<Board> result = boardRepository.findByIdWithImages(bno);
        Board board = result.orElseThrow();
        log.info("readOne EntityGraph를 사용한 findByIdWithImages 메소드 호출후");

        // [디버깅] @EntityGraph 사용의 결과로 BoardImage를 동시에 조회했는지 확인
        result.ifPresent(board1 -> {
            Set<BoardImage> images = board1.getImageSet();
            images.forEach(image -> {
                log.info(image);
            });
        });

        // 엔티티 -> Dto 로 변환
        BoardDTO boardDTO = board.entityToDto();
        log.info("boardDTO : " + boardDTO.toString());
        return boardDTO;
    }

    // 게시물 수정 메소드
    @Override
    public void modify(BoardDTO boardDTO) {
        // 수정할 게시물 조회해서 영속 영역에 보관
        Optional<Board> result = boardRepository.findById(boardDTO.getBno());

        // 영속 영역의 엔티티 참조 얻기
        Board board = result.orElseThrow();

        /*
          영속 영역에 있는 엔티티 객체의 값 변경하면 디티체킹 대상이 됨.
          더티 체킹은 최초의 상태와 지금의 상태 비교해서 서로 달라진 경우.
          JPA는 트랜잭션의 커밋 시점에 이 더티체킹을 수행하여, 변경된
          엔티티에 대한 UPDATE SQL을 데이터베이스에 실행.
        */
        board.change(boardDTO.getTitle(), boardDTO.getContent());

        // 기존 첨부 파일들은 정리 즉, 참조하고 있는 BoardImage 객체들의
        // board 속성에 null 처리, 상위 참조 주소가 없어져버림. 고아객체
        // orphanRemoval = true로 자동 삭제 대상
        board.clearImages();
        log.info("clearImages 호출됨 - 기존 파일들 삭제");

        // 저장할 파일들이 전달되어 온 경우
        if(boardDTO.getFileNames() != null){
            log.info("첨부 이미지 존재");
            for (String fileName : boardDTO.getFileNames()) {
                log.info("fileName : " + fileName);
                String[] arr = fileName.split("_");
                board.addImage(arr[0], arr[1]);
            }
        }

        log.info("board imageSet size() : " + board.getImageSet().size());
        boardRepository.save(board);
    }

    @Override
    public void remove(Long bno) {

        boardRepository.deleteById(bno);
    }


//    @Override
//    public PageResponseDTO<BoardDTO> list(PageRequestDTO pageRequestDTO) {
//
//        String[] types = pageRequestDTO.getTypes();
//        String keyword = pageRequestDTO.getKeyword();
//        Pageable pageable = pageRequestDTO.getPageable("bno");
//
//        Page<Board> result = boardRepository.searchAll(types, keyword, pageable);
//
//        return null;
//    }

    /**
     * PageRequestDTO : 검색어, 페이징 정보 포함 되어 있음.
     * @return PageResponseDTO : BoardDTO 목록 + 검색 및 페이징 정보
     */
    @Override
    public PageResponseDTO<BoardDTO> list(PageRequestDTO pageRequestDTO) {

        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");

        // result : 1.엔티티 목록, 2.페이징 관련 정보
        Page<Board> result = boardRepository.searchAll(types, keyword, pageable);

        /*
          1. 엔티티를 DTO로 변환하는 작업
          2. Page<T>에서 값을 뽑아 낼때는 getContent()
         */
        List<BoardDTO> dtoList = result.getContent().stream()
                .map(board -> modelMapper.map(board, BoardDTO.class))
                .collect(Collectors.toList());

        /*
           컨트롤러로 전달할 PageResponseDTO 객체 생성.
           1. 빌더 패턴으로 객체 생성
           2. 생성자의 파라미터중에서 제네릭 타입이 있기 때문에
             .<BoardDTO>builder() 한다.
         */
        PageResponseDTO<BoardDTO> pageResponseDTO =
                PageResponseDTO.<BoardDTO>builder()
                .pageRequestDTO(pageRequestDTO) /* 페이지 요청정보 */
                .dtoList(dtoList) /* 목록 데이터 */
                .total((int)result.getTotalElements())
                .build();

        return pageResponseDTO;
    }

    @Override
    public PageResponseDTO<BoardListReplyCountDTO> listWithReplyCount(PageRequestDTO pageRequestDTO) {

        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");

        Page<BoardListReplyCountDTO> result = boardRepository
                                                .searchWithReplyCount(
                                                        types,
                                                        keyword,
                                                        pageable);

        PageResponseDTO<BoardListReplyCountDTO> pageResponseDTO =
                PageResponseDTO.<BoardListReplyCountDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(result.getContent())
                .total((int)result.getTotalElements())
                .build();

        return pageResponseDTO;
    }

    @Override
    public PageResponseDTO<BoardListAllDTO> listWithAll(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");

        Page<BoardListAllDTO> result = boardRepository.searchWithAll(types, keyword, pageable);

        PageResponseDTO<BoardListAllDTO> pageResponseDTO = PageResponseDTO.<BoardListAllDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(result.getContent())
                .total((int)result.getTotalElements())
                .build();

        return pageResponseDTO;
    }
}
