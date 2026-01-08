package com.rehome.main.controller;

import com.rehome.main.dto.response.ApiResponse;
import com.rehome.main.dto.websocket.ChatMessageDTO;
import com.rehome.main.dto.request.ChatListResponseDTO;
import com.rehome.main.entity.ChatRoom;
import com.rehome.main.entity.Member;
import com.rehome.main.entity.StoreMesg;
import com.rehome.main.repository.ChatRoomRepo;
import com.rehome.main.repository.MemberRepo;
import com.rehome.main.repository.MesgRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatroom")
@CrossOrigin(origins = "*") // 開發階段允許跨域
public class MessageController {

    @Autowired
    private ChatRoomRepo chatRoomRepo;
    
    @Autowired
    private MemberRepo memberRepo;
    
    @Autowired
    private MesgRepo mesgRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ==========================================
    // 1. 傳送訊息 (文字 + 圖片) + WebSocket 推播
    // ==========================================
    @PostMapping("/mesg")
    public ResponseEntity<ApiResponse<?>> sendMessage(
            @RequestParam("senderId") Long senderId,
            @RequestParam("receiverId") Long receiverId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {

        // 1. 驗證
        if (senderId == null || receiverId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("必須提供 senderId 與 receiverId"));
        }

        // 2. 找寄件者
        Member sender = memberRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException("找不到寄件者 ID: " + senderId));

        // 3. 找房間或建立房間
        ChatRoom room = chatRoomRepo.findExistingRoom(senderId, receiverId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .userAId(senderId)
                            .userBId(receiverId)
                            .roomType(ChatRoom.RoomType.USER_TO_USER)
                            .createDate(LocalDateTime.now())
                            .build();
                    return chatRoomRepo.save(newRoom);
                });

        // 4. 建立並儲存訊息 Entity
        StoreMesg message = new StoreMesg();
        message.setSender(sender);
        message.setChatRoom(room);
        message.setContent(content); 
        message.setSentAt(LocalDateTime.now()); 

        // 處理圖片 (存入 byte[])
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                message.setImg(imageFile.getBytes()); 
            } catch (IOException e) {
                return ResponseEntity.status(500).body(ApiResponse.fail("圖片上傳失敗"));
            }
        }

        // 存入 DB
        mesgRepo.save(message);

        // 更新房間最後訊息時間
        room.setLastMesgDate(LocalDateTime.now());
        chatRoomRepo.save(room);

        // 5. ★關鍵轉折★：轉換成 DTO 再推播
        // 這樣做可以避免 Hibernate Proxy 序列化錯誤，也不會傳送巨大的圖片 byte[]
        ChatMessageDTO responseDTO = new ChatMessageDTO(message);

        // 6. WebSocket 推播 (給接收者)
        messagingTemplate.convertAndSendToUser(
            String.valueOf(receiverId), 
            "/queue/messages",          
            responseDTO  // 這裡傳送的是乾淨的 JSON 物件
        );
        
        // 也可以順便推播給寄件者自己 (這樣多視窗同步時才看得到)
        messagingTemplate.convertAndSendToUser(
             String.valueOf(senderId), 
             "/queue/messages",          
             responseDTO
        );

        return ResponseEntity.ok(ApiResponse.success("成功！訊息 ID: " + message.getId()));
    }

    // ==========================================
    // 2. 取得聊天列表 (左側名單)
    // ==========================================
    @GetMapping("/list")
    public ResponseEntity<List<ChatListResponseDTO>> getChatList(@RequestParam Integer myId) {
        // 這裡將 Integer 轉為 Long 以符合 Repository 定義
        Long userId = myId.longValue();
        
        List<ChatRoom> rooms = chatRoomRepo.findByUserAIdOrUserBId(userId, userId);
        List<ChatListResponseDTO> responseList = new ArrayList<>();

        for (ChatRoom room : rooms) {
            // 判斷對方 ID
            Long otherId = room.getUserAId().equals(userId) ? room.getUserBId() : room.getUserAId();
            
            // 建議：使用 Optional 安全取值，避免 NullPointerException
            String name = memberRepo.findById(otherId)
                          .map(Member::getName)
                          .orElse("未知用戶");

            // 抓取最新訊息內容
            String content = "暫無訊息";
            String time = "";

            StoreMesg lastMsg = mesgRepo.findFirstByChatRoom_IdOrderBySentAtDesc(room.getId());

            if (lastMsg != null) {
                if (lastMsg.getContent() != null && !lastMsg.getContent().trim().isEmpty()) {
                    content = lastMsg.getContent();
                    // 截短過長的訊息
                    if (content.length() > 20) content = content.substring(0, 20) + "...";
                } else if (lastMsg.getImg() != null && lastMsg.getImg().length > 0) {
                    content = "[圖片]";
                }
                
                if (lastMsg.getSentAt() != null) {
                    time = lastMsg.getSentAt().toString().replace("T", " ").substring(5, 16);
                }
            }

            responseList.add(new ChatListResponseDTO(otherId, name, content, time));
        }

        return ResponseEntity.ok(responseList);
    }

    // ==========================================
    // 3. 取得兩人對話歷史紀錄
    // ==========================================
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @RequestParam Long myId, 
            @RequestParam Long otherId) {

        ChatRoom room = chatRoomRepo.findExistingRoom(myId, otherId).orElse(null);

        if (room == null) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<StoreMesg> historyEntities = mesgRepo.findByChatRoom_IdOrderBySentAtAsc(room.getId());
        
        // ★關鍵轉折★：將 Entity 列表轉為 DTO 列表
        // 這樣回傳給前端的 JSON 就不會有無限迴圈錯誤
        List<ChatMessageDTO> historyDTOs = historyEntities.stream()
                .map(ChatMessageDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(historyDTOs);
    }
    
    // ==========================================
    // 4. 讀取「訊息圖片」的 API
    // 前端用法: <img src="/api/chatroom/message/{id}/img" />
    // ==========================================
    @GetMapping("/message/{id}/img")
    public ResponseEntity<byte[]> getMessageImage(@PathVariable Integer id) {
        StoreMesg mesg = mesgRepo.findById(id).orElse(null);
        if (mesg == null || mesg.getImg() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 假設大多是 JPEG，也可動態判斷
                .body(mesg.getImg());
    }
}