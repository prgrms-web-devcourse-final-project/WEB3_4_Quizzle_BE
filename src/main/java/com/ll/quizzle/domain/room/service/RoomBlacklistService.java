package com.ll.quizzle.domain.room.service;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.room.entity.Room;
import com.ll.quizzle.domain.room.entity.RoomBlacklist;
import com.ll.quizzle.domain.room.repository.RoomBlacklistRepository;
import com.ll.quizzle.domain.room.repository.RoomRepository;
import com.ll.quizzle.global.exceptions.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomBlacklistService {
    private final RoomBlacklistRepository blacklistRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    
    @Transactional
    public void addToBlacklist(Long roomId, Long memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(ROOM_NOT_FOUND::throwServiceException);
                
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MEMBER_NOT_FOUND::throwServiceException);
                
        if (room.isOwner(memberId)) {
            throw ROOM_OWNER_BLACKLIST_FORBIDDEN.throwServiceException();
        }
        
        if (blacklistRepository.existsByRoomAndMember(room, member)) {
            throw MEMBER_ALREADY_BLACKLISTED.throwServiceException();
        }
        
        RoomBlacklist blacklist = RoomBlacklist.builder()
                .room(room)
                .member(member)
                .build();
                
        blacklistRepository.save(blacklist);
        
        if (room.hasPlayer(memberId)) {
            room.removePlayer(memberId);
        }
    }
    
    @Transactional
    public void removeFromBlacklist(Long roomId, Long memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(ROOM_NOT_FOUND::throwServiceException);
                
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MEMBER_NOT_FOUND::throwServiceException);
                
        blacklistRepository.deleteByRoomAndMember(room, member);
    }
    
    public boolean isBlacklisted(Long roomId, Long memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(ROOM_NOT_FOUND::throwServiceException);
                
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MEMBER_NOT_FOUND::throwServiceException);
                
        return blacklistRepository.existsByRoomAndMember(room, member);
    }

    /**
     * 블랙리스트에 등록된 멤버 목록 조회가 필요 할 경우를 대비해서 추가
     */
    public List<RoomBlacklist> getBlacklistedMembers(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(ROOM_NOT_FOUND::throwServiceException);
                
        return blacklistRepository.findByRoom(room);
    }
} 