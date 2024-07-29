package com.javalab.boot.service;

import com.javalab.boot.dto.PageRequestDTO;
import com.javalab.boot.dto.PageResponseDTO;
import com.javalab.boot.dto.ReplyDTO;
import com.javalab.boot.entity.Reply;
import com.javalab.boot.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Log4j2
public class ReplyServiceImpl implements ReplyService{

    private final ReplyRepository replyRepository;

    private final ModelMapper modelMapper;

    /**
     * 댓글 등록
     * 모델 Mapper에 다음과 같이 세팅 필수
     *  - .setMatchingStrategy(MatchingStrategies.LOOSE);
     */
    @Override
    public Long register(ReplyDTO replyDTO) {

        // 컨트롤러에서 ReplyDTO 형태로 받아온 데이터를
        // 엔티티 타입으로 변환
        log.info("서비스단 replyDTO.toString() : " + replyDTO.toString());
        Reply reply = modelMapper.map(replyDTO, Reply.class);
        log.info("서비스단 reply.getBoard() : " + reply.getBoard());

        // reply에 board속성에 저장할 객체 생성
        //Board board = Board.builder().bno(replyDTO.getBno()).build();
        //reply.setBoard(board);

        //영속화(데이터베이스 저장)
        Long rno = replyRepository.save(reply).getRno();
        return rno;
    }

    @Override
    public ReplyDTO read(Long rno) {
        // 하나의 댓글 조회해서 Option<T>타입으로 반환
        Optional<Reply> replyOptional =
                replyRepository.findById(rno);
        // 반환값이 널이면 예외발생/있으면 할당
        Reply reply = replyOptional.orElseThrow();
        // ModelMapper.map을 통해서 Entity -> Dto변환
        return modelMapper.map(reply, ReplyDTO.class);
    }

    @Override
    public void modify(ReplyDTO replyDTO) {
        // 수정하기 전에 영속계층에 해당 엔티티가 있는지 확인
        Optional<Reply> replyOptional =
                replyRepository.findById(replyDTO.getRno());

        Reply reply = replyOptional.orElseThrow();
        // 영속계층에서 조회한 엔티티 객체의 값 변경
        // EM의 더티체킹 대상이 되어 트랜잭션 커밋 시점에 DB 반영되도록 체크해놓음
        reply.changeText(replyDTO.getReplyText());
        // save는 영속화 하려는 reply가 영속성 영역에 있으면 내용 갱신
        // 없으면 해당 객체를 영속 영역에 저장함.
        // 그 시점은 본 메소드의 실행이 끝나고 트랜잭션이 커밋되는 시점이다.
        replyRepository.save(reply);
    }

    @Override
    public void remove(Long rno) {
        replyRepository.deleteById(rno);
    }

    // 댓글 목록 조회
    @Override
    public PageResponseDTO<ReplyDTO> getListOfBoard(Long bno, PageRequestDTO pageRequestDTO) {

        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() <= 0 ? 0: pageRequestDTO.getPage() -1,
                pageRequestDTO.getSize(),
                Sort.by("rno").ascending());

        Page<Reply> result = replyRepository.listOfBoard(bno, pageable);

        List<ReplyDTO> dtoList =
                result.getContent().stream().map(reply -> modelMapper.map(reply, ReplyDTO.class))
                        .collect(Collectors.toList());

        return PageResponseDTO.<ReplyDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }
}
